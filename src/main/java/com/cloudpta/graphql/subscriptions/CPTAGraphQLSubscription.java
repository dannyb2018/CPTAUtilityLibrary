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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import com.cloudpta.graphql.common.CPTAGraphQLAPIConstants;
import com.cloudpta.graphql.common.CPTAGraphQLInput;
import com.cloudpta.utilites.exceptions.CPTAException;
import graphql.ExecutionResult;
import graphql.GraphQLContext;
import graphql.execution.reactive.SubscriptionPublisher;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.observables.ConnectableObservable;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyNamingStrategy;

// We have the publisher and subscriber as the same class because we want to have the option to do keep alives which 
// reactive streams bizarrely doesnt allow for
public abstract class CPTAGraphQLSubscription<ResultType,RequestType extends CPTAGraphQLInput> implements Subscriber<ExecutionResult>, ObservableOnSubscribe<ResultType>, Action
{
    protected abstract void parseInput(RequestType input);

    public static CPTAGraphQLSubscription<?, ?> getSubscription(Publisher<ExecutionResult> publisher)
    {
        // get the data fetcher publisher 
        SubscriptionPublisher responseStreamAsPublisher = (SubscriptionPublisher)publisher;
        int fetcherPublisherHashCode = responseStreamAsPublisher.getUpstreamPublisher().hashCode();

        // look in map
        CPTAGraphQLSubscription<?, ?> desiredSubscription = mapPublishersToSubscriptions.get(fetcherPublisherHashCode);

        return desiredSubscription;
    }

    public void initialise(RequestType input)
    {
        // parse arguments
        parseInput(input);

        // Get the context from the request
        context = input.getInputContext();
        // Get the timeout
        String timeoutAsString = context.get(CPTAGraphQLAPIConstants.SUBSCRIPTION_TIMEOUT);
        // If there is one
        if(null != timeoutAsString)
        {
            timeout = Long.parseLong(timeoutAsString);
        }

        Observable<ResultType> resultObservable = Observable.create(this);

        ConnectableObservable<ResultType> connectableObservable = resultObservable.share().publish();
        connectableObservable.connect();

        // going to wait to be told when the subscription is cancelled
        publisher = connectableObservable.toFlowable(BackpressureStrategy.BUFFER).doOnCancel(this);

        // get publisher hash key
        int publisherID = publisher.hashCode();
        // store the subscriptions
        mapPublishersToSubscriptions.put(publisherID, this);
    }

    public void shutdown()
    {
        subscriptionRef.get().cancel();
    }

    public Flowable<ResultType> getPublisher() 
    {
        return publisher;
    }

    public void setListener(CPTAGraphQLSubscriptionListener newListener)
    {
        listener = newListener;
    }

    public void setID(String newID)
    {
        id = newID;
    }

    public String getID()
    {
        return id;
    }

    public long getMessagesAndPublishThem()
    {
        long numberOfMessages = getNewResults(currentEmitter);

        return numberOfMessages;
    }   

    protected void handleSubscribe(ObservableEmitter<ResultType> emitter) throws Throwable
    {
        // set up source
        setupSource();

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
            mapPublishersToSubscriptions.remove(hashcode);
        }

        // wipe our memory of the emitter and thread
        currentEmitter = null;
    }

    protected long getNewResults(ObservableEmitter<ResultType> emitter) 
    {

        long numberOfResults = 0;

        try
        {
            // Get results
            List<ResultType> results = getResults();

            // pass on to subscriber
            for (ResultType result : results) 
            {
                emitter.onNext(result);
            }    
            
            numberOfResults = results.size();
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

        return numberOfResults;
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

    @Override
    public void onSubscribe(Subscription s) 
    {
        subscriptionRef = new AtomicReference<Subscription>(s);
        // Request maximum amount
        subscriptionRef.get().request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(ExecutionResult executionResult) 
    {
        try 
        {
            // BUGBUGDB would be nice to do this inside the endpoint rather than having graphql stuff in more than one place
            Map<String, Object> spec = executionResult.toSpecification();

            // Convert that result into a json string
            JsonbConfig jsonbConfig = new JsonbConfig()
                                        .withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES)
                                        .withNullValues(true)
                                        .withFormatting(false);
            Jsonb converter = JsonbBuilder.create(jsonbConfig);
            String nextResult = converter.toJson(spec);
            JsonReader reader = Json.createReader(new StringReader(nextResult));
            JsonObject resultObject = reader.readObject(); 

            // handle new result
            listener.handleNextResultSend(this, resultObject);

            // get next ones
            subscriptionRef.get().request(Long.MAX_VALUE);
        } 
        catch (Exception e) 
        {
            // pass it to the error handler
            onError(e);
        }        
    }

    @Override
    public void onError(Throwable error) 
    {
        CPTAException wrappedException  = new CPTAException(error);

        listener.handleError(this, wrappedException);

        // remove from list of subscriptions
        // get publisher hash key
        int publisherID = publisher.hashCode();
        // store the subscriptions
        mapPublishersToSubscriptions.remove(publisherID);
    }

    @Override
    public void onComplete() 
    {
        listener.handleClose(this);

        // remove from list of subscriptions
        // get publisher hash key
        int publisherID = publisher.hashCode();
        // remove the subscriptions
        mapPublishersToSubscriptions.remove(publisherID);
        // no need for a listener
        listener = null;
    }

    @Override
    public void subscribe(ObservableEmitter<ResultType> emitter) throws Throwable
    {
        try
        {
            handleSubscribe(emitter);
        }
        catch(Throwable E)
        {

        }
    }

    // called by action when subscription is cancelled
    @Override
    public void run() throws Throwable
    {
        try
        {
            handleUnsubscribe();
        }
        catch(Throwable E)
        {

        }
    }

    protected String id;
    protected GraphQLContext context;
    protected RequestType request;
    protected Flowable<ResultType> publisher;
    protected long timeout = 500; 
    protected AtomicBoolean shouldRun = new AtomicBoolean();
    protected ObservableEmitter<ResultType> currentEmitter;
    protected AtomicReference<Subscription> subscriptionRef;
    protected CPTAGraphQLSubscriptionListener listener = null;
    protected static Map<Integer, CPTAGraphQLSubscription<?, ?>> mapPublishersToSubscriptions = new ConcurrentHashMap<>();
}