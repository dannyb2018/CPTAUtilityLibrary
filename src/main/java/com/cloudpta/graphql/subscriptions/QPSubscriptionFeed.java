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

import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import com.cloudpta.utilites.exceptions.CPTAException;
import com.cloudpta.utilites.logging.CPTALogger;
import com.cloudpta.graphql.common.QPGraphQLAPIConstants;
import org.eclipse.jetty.websocket.api.Session;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import ch.qos.logback.classic.Logger;
import graphql.ExecutionResult;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyNamingStrategy;

public class QPSubscriptionFeed implements Subscriber<ExecutionResult>  
{
    Session socketSession;
    AtomicReference<Subscription> subscriptionRef;
    String id;
    
    public QPSubscriptionFeed(Session websocketSession, AtomicReference<Subscription> subscriptionRef, String subscriptionID)
    {
        socketSession = websocketSession;
        this.subscriptionRef = subscriptionRef;
        id = subscriptionID;
    }
    
    @Override
    public void onSubscribe(Subscription s)
    {
        subscriptionRef.set( s);
        // Request maximum amount
        subscriptionRef.get().request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(ExecutionResult executionResult)
    {
        try 
        {
            Map<String, Object> spec = executionResult.toSpecification();

            // Convert that result into a json string
            JsonbConfig jsonbConfig = new JsonbConfig()
                                        .withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES)
                                        .withNullValues(true)
                                        .withFormatting(false);
            Jsonb converter = JsonbBuilder.create(jsonbConfig);
            String resultAsString = converter.toJson(spec);
            JsonReader reader = Json.createReader(new StringReader(resultAsString));
            JsonObject resultObject = reader.readObject(); 

            JsonObjectBuilder responseObjectBuilder = Json.createObjectBuilder();
            responseObjectBuilder.add(QPGraphQLAPIConstants.PAYLOAD_TYPE, QPGraphQLAPIConstants.PAYLOAD_TYPE_DATA);
            responseObjectBuilder.add(QPGraphQLAPIConstants.PAYLOAD_ID, id);
            responseObjectBuilder.add(QPGraphQLAPIConstants.PAYLOAD, resultObject);
            JsonObject responseObject = responseObjectBuilder.build();
            String responseAsString = responseObject.toString();
            socketSession.getRemote().sendString(responseAsString);

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
    public void onError(Throwable thrwbl)
    {
        Exception E = new Exception(thrwbl);
        CPTAException wrappedException  = new CPTAException(E);

        // If the socket is still open
        if( true == socketSession.isOpen())
        {
            // write back an error somehow
        }

        // Get the text of exception and log it
        
        socketSession.close();
    }

    @Override
    public void onComplete()
    {
        // Need to shut down properly, so send back a stop message
        JsonObjectBuilder responseObjectBuilder = Json.createObjectBuilder();
        responseObjectBuilder.add(QPGraphQLAPIConstants.PAYLOAD_TYPE, QPGraphQLAPIConstants.PAYLOAD_TYPE_STOP);
        responseObjectBuilder.add(QPGraphQLAPIConstants.PAYLOAD_ID, id);
        responseObjectBuilder.add(QPGraphQLAPIConstants.PAYLOAD, "");
        JsonObject responseObject = responseObjectBuilder.build();
        String responseAsString = responseObject.toString();

        try
        {
            socketSession.getRemote().sendString(responseAsString); 
        } 
        catch(Exception E)
        {
            // shutting down anyway so just log this
            CPTAException wrappedException  = new CPTAException(E);
        }  
    }
       
    public void stop()
    {
        // Cancel the subscription
        subscriptionRef.get().cancel();

        // Force close, shouldnt need to do this
        onComplete();
    } 
    
    Logger logger = CPTALogger.getLogger();
}
