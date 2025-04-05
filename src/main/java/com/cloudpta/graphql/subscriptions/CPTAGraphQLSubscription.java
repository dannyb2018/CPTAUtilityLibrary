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
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import com.cloudpta.graphql.common.CPTAGraphQLInput;
import com.cloudpta.utilites.exceptions.CPTAException;
import graphql.ExecutionResult;
import graphql.GraphQLContext;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.observables.ConnectableObservable;
import jakarta.json.JsonObject;

// We have the publisher and subscriber as the same class because we want to have the option to do keep alives which 
// reactive streams bizarrely doesnt allow for
public abstract class CPTAGraphQLSubscription<ResultType,RequestType extends CPTAGraphQLInput> implements Subscriber<ExecutionResult>
{
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
    }

    protected void handleUnsubscribe()
    {
        // Tell result getter thread to stop getting results
        shouldRun.set(false);

        // Finally unsubscribe from source
        unsubscribeFromSource();

        // remove from link of publisher to feed
        if(null != publisher)
        {
            int hashcode = publisher.hashCode();
            CPTASubscriptionFeed.removePublisherToFeedLink(hashcode);
        }

        // wipe our memory of the emitter and thread
        currentEmitter = null;
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

            // if we are empty
            if(true == results.isEmpty())
            {
                // get the feed for this publisher
                if(null != publisher)
                {
                    int hashCode = publisher.hashCode();
                    CPTASubscriptionFeed feedForThisPublisher = CPTASubscriptionFeed.getFeedForPublisher(hashCode);

                    // do a keep alive
                    feedForThisPublisher.keepAlive();
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
    public abstract void parseArguments(RequestType input);

    @Override
    public void onSubscribe(Subscription s) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onSubscribe'");
    }

    @Override
    public void onNext(ExecutionResult t) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onNext'");
    }

    @Override
    public void onError(Throwable t) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onError'");
    }

    @Override
    public void onComplete() 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onComplete'");
    }

    protected String id;
    protected GraphQLContext context;
    protected RequestType request;
    protected Flowable<ResultType> publisher;
    protected long timeout = 500; 
    protected AtomicBoolean shouldRun = new AtomicBoolean();
    protected ObservableEmitter<ResultType> currentEmitter;
}

class ObservableOnSubscribeHandler<ResultType,RequestType extends CPTAGraphQLInput> implements ObservableOnSubscribe<ResultType>
{
    public ObservableOnSubscribeHandler(CPTAGraphQLSubscription<ResultType,RequestType> thePublisher)
    {
        publisher = thePublisher;
    }

    @Override
    public void subscribe(ObservableEmitter<ResultType> emitter) throws Throwable
    {
        publisher.handleSubscribe(emitter);
    }

    CPTAGraphQLSubscription<ResultType,RequestType> publisher;
}

class CancelSubscriptionHandler<ResultType, RequestType extends CPTAGraphQLInput> implements Action
{
    public CancelSubscriptionHandler(CPTAGraphQLSubscription<ResultType,RequestType> newPublisher)
    {
        publisher = newPublisher;
    }
    
    @Override
    public void run() throws Throwable
    {
        publisher.handleUnsubscribe();
    }

    CPTAGraphQLSubscription<ResultType,RequestType> publisher;
}