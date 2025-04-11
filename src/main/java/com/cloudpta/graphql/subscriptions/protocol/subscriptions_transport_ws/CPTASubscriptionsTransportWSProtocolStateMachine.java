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
package com.cloudpta.graphql.subscriptions.protocol.subscriptions_transport_ws;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.cloudpta.graphql.common.CPTAGraphQLAPIConstants;
import com.cloudpta.graphql.common.CPTAQueryVariablesParser;
import com.cloudpta.graphql.subscriptions.CPTAGraphQLSubscription;
import com.cloudpta.graphql.subscriptions.protocol.CPTAWebsocketProtocolStateMachine;
import com.cloudpta.graphql.subscriptions.protocol.CPTAWebsocketProtocolStateMachineListener;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocoLogonRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolKeepAliveEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLoggedOffEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLogoffRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolSendMessageEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolStateMachineEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolSubscribeRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolUnsubscribeRequestEvent;
import com.cloudpta.utilites.exceptions.CPTAException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;

public class CPTASubscriptionsTransportWSProtocolStateMachine extends CPTAWebsocketProtocolStateMachine
{
    @Override
    protected CPTAWebsocketProtocolStateMachineEvent getEventFromMessage(String message) 
    {
        CPTAWebsocketProtocolStateMachineEvent eventFromMessage = null;
        // convert to json
        JsonReader reader = Json.createReader(new StringReader(message));
        JsonObject messageAsJsonObject = reader.readObject(); 
        String type = messageAsJsonObject.getString(CPTASubscriptionsTransportWSProtocolConstants.PAYLOAD_TYPE);  
        // If we are an init
        if( 0 == type.compareTo(CPTASubscriptionsTransportWSProtocolConstants.PAYLOAD_TYPE_CONNECTION_INIT))
        {
            // get connection inititalisation parameters
            // Get the payload
            JsonObject requestParamsForSubscription = messageAsJsonObject.getJsonObject(CPTAGraphQLAPIConstants.PAYLOAD); 
            // turn into map
            Map<String, String> connectionInitParameters = new ConcurrentHashMap<>();
            
            // Add all the fields
            Iterator<String> fieldNames = requestParamsForSubscription.keySet().iterator();
            while(true == fieldNames.hasNext())
            {
                String currentFieldName = fieldNames.next();
                String currentFieldValue = requestParamsForSubscription.getString(currentFieldName);
                connectionInitParameters.put(currentFieldName.toLowerCase(), currentFieldValue);
            }

            eventFromMessage = new CPTAWebsocketProtocoLogonRequestEvent(this, connectionInitParameters);
        }
        // If we are a start
        else if(0 == type.compareTo(CPTASubscriptionsTransportWSProtocolConstants.PAYLOAD_TYPE_START))
        {
            // handle query
            JsonObject subscriptionRequestAsJsonObject = messageAsJsonObject.getJsonObject(CPTAGraphQLAPIConstants.PAYLOAD);  
            String subscriptionID = messageAsJsonObject.getString(CPTASubscriptionsTransportWSProtocolConstants.PAYLOAD_ID);
            // Get operation name
            String operationName = null;
            if(false == subscriptionRequestAsJsonObject.isNull(CPTAGraphQLAPIConstants.OPERATION_NAME))
            {
                operationName = subscriptionRequestAsJsonObject.getString(CPTAGraphQLAPIConstants.OPERATION_NAME);
            }
            // get the query field
            String graphQLQueryName = subscriptionRequestAsJsonObject.getString(CPTAGraphQLAPIConstants.OPERATION_TEXT);
            // Need to turn variables into a map of keys and values
            JsonObject variablesAsJsonObject = subscriptionRequestAsJsonObject.getJsonObject(CPTAGraphQLAPIConstants.OPERATION_VARIABLES);
            Map<String, Object> variables = CPTAQueryVariablesParser.parseVariables(variablesAsJsonObject);

            eventFromMessage = new CPTAWebsocketProtocolSubscribeRequestEvent
                                                                            (
                                                                            this,
                                                                            subscriptionID, 
                                                                            operationName,
                                                                            variables,
                                                                            graphQLQueryName
                                                                            );
        }
        // If we are a stop
        else if(0 == type.compareTo(CPTASubscriptionsTransportWSProtocolConstants.PAYLOAD_TYPE_STOP))
        {
            // handle query
            String subscriptionID = messageAsJsonObject.getString(CPTASubscriptionsTransportWSProtocolConstants.PAYLOAD_ID);

            eventFromMessage = new CPTAWebsocketProtocolUnsubscribeRequestEvent(this, subscriptionID);
        }            

        return eventFromMessage;
    }

    @Override
    protected String getSubscriptionSucceededMessage(CPTAGraphQLSubscription<?, ?> subscription) 
    {
        // no message needed so return null
        return null;
    }

    @Override
    protected String getMesageFromResult(CPTAGraphQLSubscription<?, ?> subscription, JsonObject resultAsJsonObject) 
    {
        // get subscription ID
        String subscriptionID = subscription.getID();

        // generate a data message
        JsonObjectBuilder dataMessageAsObjectBuilder = Json.createObjectBuilder();
        dataMessageAsObjectBuilder.add(CPTAGraphQLAPIConstants.PAYLOAD_TYPE, CPTAGraphQLAPIConstants.PAYLOAD_TYPE_DATA);
        dataMessageAsObjectBuilder.add(CPTAGraphQLAPIConstants.PAYLOAD_ID, subscriptionID);
        dataMessageAsObjectBuilder.add(CPTAGraphQLAPIConstants.PAYLOAD, resultAsJsonObject);
        JsonObject dataMessageAsObject = dataMessageAsObjectBuilder.build();
        String dataMessageAsString = dataMessageAsObject.toString();

        return dataMessageAsString;
    }

    @Override
    protected String getMessageFromError(CPTAGraphQLSubscription<?, ?> subscription, Throwable error) 
    {
        // wrap the exception 
        CPTAException wrappedError = new CPTAException(error);
        // get the error data as a string
        String errorAsString = wrappedError.getErrors().toString();

        // get subscription id
        String subscriptionID = subscription.getID();

        // form the error message
        JsonObjectBuilder errorMessageAsObjectBuilder = Json.createObjectBuilder();
        errorMessageAsObjectBuilder.add(CPTAGraphQLAPIConstants.PAYLOAD_TYPE, CPTAGraphQLAPIConstants.PAYLOAD_TYPE_CONNECTION_ERROR);
        errorMessageAsObjectBuilder.add(CPTAGraphQLAPIConstants.PAYLOAD_ID, subscriptionID);
        errorMessageAsObjectBuilder.add(CPTAGraphQLAPIConstants.PAYLOAD, errorAsString);
        JsonObject errorMessageAsObject = errorMessageAsObjectBuilder.build();
        String errorMessage = errorMessageAsObject.toString();

        return errorMessage;
    }

    @Override
    protected String getKeepAliveMessage() 
    {
        // had a time out with no data so send a keep alive
        JsonObjectBuilder keepAliveMessageAsObjectBuilder = Json.createObjectBuilder();
        keepAliveMessageAsObjectBuilder.add(CPTAGraphQLAPIConstants.PAYLOAD_TYPE, CPTAGraphQLAPIConstants.PAYLOAD_TYPE_CONNECTION_KEEP_ALIVE);
        JsonObject keepAliveMessageAsObject = keepAliveMessageAsObjectBuilder.build();
        String keepAliveMessage = keepAliveMessageAsObject.toString();
    
        return keepAliveMessage;
    }


    @Override
    protected void validateLogonRequest(CPTAWebsocketProtocoLogonRequestEvent request) throws CPTAException 
    {
        // Do nothing for now
    }

    @Override
    protected void sendLogonResponse(CPTAWebsocketProtocoLogonRequestEvent request) throws CPTAException 
    {
        // generate response
        String logonResponse = CPTASubscriptionsTransportWSProtocolConstants.CONNECTION_INIT_RESPONSE;
        CPTAWebsocketProtocolSendMessageEvent sendEvent = new CPTAWebsocketProtocolSendMessageEvent(this, logonResponse);

        // say send it
        for(CPTAWebsocketProtocolStateMachineListener currentListener : listeners)
        {
            currentListener.onSendMessage(sendEvent);
        }
    }

    @Override
    protected void handleLogonAccepted() 
    {
        // do nothing for now
    }

    @Override
    protected void handleLogoffRequest(CPTAWebsocketProtocolLogoffRequestEvent request) 
    {
        // do nothing for now
    }

    @Override
    protected void handleLoggedOff(CPTAWebsocketProtocolLoggedOffEvent request) 
    {
        // do nothing for now
    }

    @Override
    protected void handleKeepAlive(CPTAWebsocketProtocolKeepAliveEvent anyAdditionalInformation) 
    {
        // do nothing for now
    }

    @Override
    protected void handleError(Throwable error) 
    {
        // do nothing for now
    }    
}
