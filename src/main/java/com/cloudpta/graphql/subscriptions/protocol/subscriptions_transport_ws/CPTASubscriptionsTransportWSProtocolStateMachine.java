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
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLoggedOnEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLogoffRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolSendMessageEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolStateMachineEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolSubscribeRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolSubscribedEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolUnsubscribeRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolUnsubscribedEvent;
import com.cloudpta.utilites.exceptions.CPTAException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
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
            String subscriptionID = subscriptionRequestAsJsonObject.getString(CPTASubscriptionsTransportWSProtocolConstants.PAYLOAD_ID);
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
            JsonObject subscriptionRequestAsJsonObject = messageAsJsonObject.getJsonObject(CPTAGraphQLAPIConstants.PAYLOAD);  
            String subscriptionID = subscriptionRequestAsJsonObject.getString(CPTASubscriptionsTransportWSProtocolConstants.PAYLOAD_ID);

            eventFromMessage = new CPTAWebsocketProtocolUnsubscribeRequestEvent(this, subscriptionID);
        }            

        return eventFromMessage;
    }

    @Override
    protected String getSubscriptionSucceededMessage(CPTAGraphQLSubscription<?, ?> subscription) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSubscriptionSucceededMessage'");
    }

    @Override
    protected String getMesageFromResult(CPTAGraphQLSubscription<?, ?> subscription, String resultAsString) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMesageFromResult'");
    }

    @Override
    protected String getMessageFromError(CPTAGraphQLSubscription<?, ?> subscription, Throwable error) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMessageFromError'");
    }

    @Override
    protected String getKeepAliveMessage() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getKeepAliveMessage'");
    }

    @Override
    protected void handleLoggedOn(CPTAWebsocketProtocolLoggedOnEvent request) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleLoggedOn'");
    }

    @Override
    protected void handleLogoffRequest(CPTAWebsocketProtocolLogoffRequestEvent request) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleLogoffRequest'");
    }

    @Override
    protected void handleLoggedOff(CPTAWebsocketProtocolLoggedOffEvent request) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleLoggedOff'");
    }

    @Override
    protected void handleKeepAlive(CPTAWebsocketProtocolKeepAliveEvent anyAdditionalInformation) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleKeepAlive'");
    }

    @Override
    protected void handleError(Throwable error) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleError'");
    }

    @Override
    protected void handleSubscribeRequest(CPTAWebsocketProtocolSubscribeRequestEvent request) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleSubscribeRequest'");
    }

    @Override
    protected void handleSubscribed(CPTAWebsocketProtocolSubscribedEvent request) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleSubscribed'");
    }

    @Override
    protected void handleUnsubscribeRequest(CPTAWebsocketProtocolUnsubscribeRequestEvent request) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleUnsubscribeRequest'");
    }

    @Override
    protected void handleUnsubscribed(CPTAWebsocketProtocolUnsubscribedEvent request) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleUnsubscribed'");
    }

    @Override
    protected void handleLogonAccepted() 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleLogonAccepted'");
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
    
}
