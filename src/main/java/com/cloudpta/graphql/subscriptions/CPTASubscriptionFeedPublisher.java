/*

Copyright 2017-2019 Advanced Products Limited, 
Copyright 2021-2022 Liquid Markets Limited, 
github.com/dannyb2018

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package com.cloudpta.graphql.subscriptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.cloudpta.utilites.exceptions.CPTAException;
import com.cloudpta.graphql.common.CPTAGraphQLAPIConstants;
import com.cloudpta.graphql.common.CPTAGraphQLInput;
import graphql.GraphQLContext;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.observables.ConnectableObservable;
import jakarta.json.JsonObject;

public abstract class CPTASubscriptionFeedPublisher<ResultType,RequestType extends CPTAGraphQLInput> 
{
    public CPTASubscriptionFeedPublisher(RequestType newRequest) 
    {
        // Get the context from the request
        context = newRequest.getInputContext();
        // Get the timeout
        String timeoutAsString = context.get(CPTAGraphQLAPIConstants.SUBSCRIPTION_TIMEOUT);
        // If there is one
        if(null != timeoutAsString)
        {
            timeout = Long.parseLong(timeoutAsString);
        }

    }
    
    protected void start()
    {
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
                // if there was a result
                if(null != currentResult)
                {
                    // Add to list
                    updates.add(currentResult);
                }
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

    protected abstract ResultType convertFromJson(JsonObject recordAsJson);
    protected abstract void setupSource();
    protected abstract void subscribeToSource();
    protected abstract void unsubscribeFromSource();
    protected abstract List<JsonObject> readFromSource(long timeout) throws IOException;

    // For testing purposes
    protected CPTASubscriptionFeedPublisher()
    {

        publisher = null;
    }

    protected GraphQLContext context;
    protected RequestType request;
    protected Flowable<ResultType> publisher;
    protected long timeout = 500; 
    protected AtomicBoolean shouldRun = new AtomicBoolean();
    protected ObservableEmitter<ResultType> currentEmitter;
    GetResultsThread<ResultType, RequestType> resultsThread;
}

class ObservableOnSubscribeHandler<ResultType,RequestType extends CPTAGraphQLInput> implements ObservableOnSubscribe<ResultType>
{
    public ObservableOnSubscribeHandler(CPTASubscriptionFeedPublisher<ResultType,RequestType> thePublisher)
    {
        publisher = thePublisher;
    }

    @Override
    public void subscribe(ObservableEmitter<ResultType> emitter) throws Throwable
    {
        publisher.handleSubscribe(emitter);
    }

    CPTASubscriptionFeedPublisher<ResultType,RequestType> publisher;
}

class CancelSubscriptionHandler<ResultType, RequestType extends CPTAGraphQLInput> implements Action
{
    public CancelSubscriptionHandler(CPTASubscriptionFeedPublisher<ResultType,RequestType> newPublisher)
    {
        publisher = newPublisher;
    }
    
    @Override
    public void run() throws Throwable
    {
        publisher.handleUnsubscribe();
    }

    CPTASubscriptionFeedPublisher<ResultType,RequestType> publisher;
}

class GetResultsThread<ResultType, RequestType extends CPTAGraphQLInput> extends Thread
{
    public GetResultsThread(ObservableEmitter<ResultType> newEmitter, CPTASubscriptionFeedPublisher<ResultType,RequestType> newPublisher)
    {
        this.emitter = newEmitter;
        this.publisher = newPublisher;
    }

    @Override
    public void run()
    {
        publisher.getNewResults(emitter);
    }

    CPTASubscriptionFeedPublisher<ResultType,RequestType> publisher;
    ObservableEmitter<ResultType> emitter;
}