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
