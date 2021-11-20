////////////////////////////////////////////////////////////////////////////////
//
//                                 NOTICE:
//  THIS PROGRAM CONSISTS OF TRADE SECRECTS THAT ARE THE PROPERTY OF
//  Advanced Products Ltd. THE CONTENTS MAY NOT BE USED OR DISCLOSED
//  WITHOUT THE EXPRESS WRITTEN PERMISSION OF THE OWNER.
//
//               COPYRIGHT Advanced Products Ltd 2016-2019
//
////////////////////////////////////////////////////////////////////////////////
package com.cloudpta.graphql.subscriptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.cloudpta.utilites.exceptions.CPTAException;
import com.cloudpta.graphql.common.QPGraphQLAPIConstants;
import com.cloudpta.graphql.common.QPGraphQLInput;
import graphql.GraphQLContext;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.observables.ConnectableObservable;
import jakarta.json.JsonObject;

public abstract class QPSubscriptionFeedPublisher<ResultType,RequestType extends QPGraphQLInput> 
{
    public QPSubscriptionFeedPublisher(RequestType newRequest) 
    {
        // Get the context from the request
        context = newRequest.getInputContext();
        // Get the timeout
        String timeoutAsString = context.get(QPGraphQLAPIConstants.SUBSCRIPTION_TIMEOUT);
        // If there is one
        if(null != timeoutAsString)
        {
            timeout = Long.parseLong(timeoutAsString);
        }

        // Set up where the data for the feed
        setupSource();

        ObservableOnSubscribeHandler<ResultType,RequestType> observableOnSubscribe = new ObservableOnSubscribeHandler<>(this);
        Observable<ResultType> resultObservable = Observable.create(observableOnSubscribe);

        ConnectableObservable<ResultType> connectableObservable = resultObservable.share().publish();
        connectableObservable.connect();

        // going to wait to be told when the subscription is cancelled
        CancelSubscriptionHandler<ResultType, RequestType> cancelHandler = new CancelSubscriptionHandler<>(this);
        publisher = connectableObservable.toFlowable(BackpressureStrategy.BUFFER).doOnCancel(cancelHandler);
    }
    
    public Flowable<ResultType> getPublisher() 
    {
        return publisher;
    }

    protected void handleSubscribe(ObservableEmitter<ResultType> emitter) throws Throwable
    {
        // Start subscribing to source
        subscribeToSource();

        shouldRun.set(true);
        currentEmitter = emitter;
        resultsThread = new GetResultsThread<>(currentEmitter, this);
        resultsThread.start();        
    }

    protected void handleUnsubscribe()
    {
        // Tell result getter thread to stop getting results
        shouldRun.set(false);

        // Wait twice timeout for thread to stop
        try
        {
            resultsThread.join(timeout * 2);
        }
        catch(Exception E)
        {

        }

        // Finally unsubscribe from source
        unsubscribeFromSource();

        // wipe our memory of the emitter and thread
        currentEmitter = null;
        resultsThread = null;
    }

    protected void getNewResults(ObservableEmitter<ResultType> emitter) 
    {
        try
        {
            // Whilst we should still get results
            while(true == shouldRun.get())
            {
                // Get results
                List<ResultType> results = getResults();

                // pass on to subscriber
                for (ResultType result : results) 
                {
                    emitter.onNext(result);
                }
            }
        }
        // Any errors, let the subscriber know
        catch(CPTAException E)
        {
            emitter.onError(E);
        }
        catch (RuntimeException rte) 
        {
            emitter.onError(rte);
        }
    }  
    
    protected List<ResultType> getResults() throws CPTAException
    {
        List<ResultType> updates = new ArrayList<>();

        try
        {
            List<JsonObject> results = readFromSource(timeout);
            // Convert the json Objects to results
            for(JsonObject currentResultAsJson: results)
            {
                // convert to a result
                ResultType currentResult = convertFromJson(currentResultAsJson);
                // Add to list
                updates.add(currentResult);
            }
        }
        catch(Exception E)
        {
            CPTAException wrappedException = new CPTAException(E);
            throw wrappedException;
        }

        // If it is not null
        return updates;
    }

    protected abstract ResultType convertFromJson(JsonObject quoteAsJson);
    protected abstract void setupSource();
    protected abstract void subscribeToSource();
    protected abstract void unsubscribeFromSource();
    protected abstract List<JsonObject> readFromSource(long timeout) throws IOException;

    // For testing purposes
    protected QPSubscriptionFeedPublisher()
    {

        publisher = null;
    }

    protected GraphQLContext context;
    protected RequestType request;
    protected final Flowable<ResultType> publisher;
    protected long timeout = 500; 
    protected AtomicBoolean shouldRun = new AtomicBoolean();
    protected ObservableEmitter<ResultType> currentEmitter;
    GetResultsThread<ResultType, RequestType> resultsThread;
}

class ObservableOnSubscribeHandler<ResultType,RequestType extends QPGraphQLInput> implements ObservableOnSubscribe<ResultType>
{
    public ObservableOnSubscribeHandler(QPSubscriptionFeedPublisher<ResultType,RequestType> thePublisher)
    {
        publisher = thePublisher;
    }

    @Override
    public void subscribe(ObservableEmitter<ResultType> emitter) throws Throwable
    {
        publisher.handleSubscribe(emitter);
    }

    QPSubscriptionFeedPublisher<ResultType,RequestType> publisher;
}

class CancelSubscriptionHandler<ResultType, RequestType extends QPGraphQLInput> implements Action
{
    public CancelSubscriptionHandler(QPSubscriptionFeedPublisher<ResultType,RequestType> newPublisher)
    {
        publisher = newPublisher;
    }
    
    @Override
    public void run() throws Throwable
    {
        publisher.handleUnsubscribe();
    }

    QPSubscriptionFeedPublisher<ResultType,RequestType> publisher;
}

class GetResultsThread<ResultType, RequestType extends QPGraphQLInput> extends Thread
{
    public GetResultsThread(ObservableEmitter<ResultType> newEmitter, QPSubscriptionFeedPublisher<ResultType,RequestType> newPublisher)
    {
        this.emitter = newEmitter;
        this.publisher = newPublisher;
    }

    @Override
    public void run()
    {
        publisher.getNewResults(emitter);
    }

    QPSubscriptionFeedPublisher<ResultType,RequestType> publisher;
    ObservableEmitter<ResultType> emitter;
}