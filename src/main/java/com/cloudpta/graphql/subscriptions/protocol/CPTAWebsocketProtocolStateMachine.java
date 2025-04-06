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
package com.cloudpta.graphql.subscriptions.protocol;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import com.cloudpta.graphql.subscriptions.CPTAGraphQLSubscription;
import com.cloudpta.graphql.subscriptions.CPTAGraphQLSubscriptionListener;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocoLogonRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolErrorEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolKeepAliveEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLoggedOffEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLoggedOnEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLogoffRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolMachineEventType;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolSendMessageEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolStateMachineEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolSubscribeRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolSubscribedEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolUnsubscribeRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolUnsubscribedEvent;
import com.cloudpta.utilites.exceptions.CPTAException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public abstract class CPTAWebsocketProtocolStateMachine implements CPTAGraphQLSubscriptionListener
{
    public void logonAccepted(Map<String, String> newConnectionParameters)
    {
        connectionParameters = newConnectionParameters;

        // handle logon accepted
        handleLogonAccepted();

        // start thread to pump the subscriptions
    }

    public void subscriptionFailed(CPTAGraphQLSubscription<?, ?> failedSubscription, Map<String, Object> errorAsMap)
    {
        // turn result into error
        JsonObjectBuilder errorAsJsonObjectBuilder = Json.createObjectBuilder(errorAsMap);
        JsonObject errorAsJson = errorAsJsonObjectBuilder.build();
        CPTAException errorAsException = new CPTAException(errorAsJson);

        // turn exception into message
        String errorMessage = getMessageFromError(failedSubscription, errorAsException);
        // convert to event
        CPTAWebsocketProtocolSendMessageEvent sendMessageEvent = new CPTAWebsocketProtocolSendMessageEvent(this, errorMessage);

        // go through listeners and fire message send
        for(CPTAWebsocketProtocolStateMachineListener currentListener : listeners)
        {
            currentListener.onSendMessage(sendMessageEvent);
        }
    }

    public void saveSubscription(CPTAGraphQLSubscription<?, ?> subscriptionToSave)
    {
        String id = subscriptionToSave.getID();

        mapOfIdsToSubscriptions.put(id, subscriptionToSave);
    }

    public void subscriptionSucceeded(CPTAGraphQLSubscription<?, ?> successfulSubscription)
    {
        String subscriptionSucceededMessage = getSubscriptionSucceededMessage(successfulSubscription);
        // convert to event
        CPTAWebsocketProtocolSendMessageEvent sendMessageEvent = new CPTAWebsocketProtocolSendMessageEvent(this, subscriptionSucceededMessage);

        // go through listeners and fire message send
        for(CPTAWebsocketProtocolStateMachineListener currentListener : listeners)
        {
            currentListener.onSendMessage(sendMessageEvent);
        }
    }

    public void handleConnectionClosed()
    {
        // if there is a thread
        if(null != pumpThread)
        {
            // stop it
        }
    }

    public Map<String, String> getConnectionParameters()
    {
        return connectionParameters;
    }

    @Override
    public void handleNextResultSend(CPTAGraphQLSubscription<?, ?> subscription, String nextResultAsString) 
    {
        String messageToSend = getMesageFromResult(subscription, nextResultAsString);
        // convert to event
        CPTAWebsocketProtocolSendMessageEvent sendMessageEvent = new CPTAWebsocketProtocolSendMessageEvent(this, messageToSend);

        // go through listeners and fire message send
        for(CPTAWebsocketProtocolStateMachineListener currentListener : listeners)
        {
            currentListener.onSendMessage(sendMessageEvent);
        }
    }

    @Override
    public void handleError(CPTAGraphQLSubscription<?, ?> subscription, CPTAException error) 
    {
        String errorMessage = getMessageFromError(subscription, error);

        // convert to event
        CPTAWebsocketProtocolSendMessageEvent sendMessageEvent = new CPTAWebsocketProtocolSendMessageEvent(this, errorMessage);

        // go through listeners and fire message send
        for(CPTAWebsocketProtocolStateMachineListener currentListener : listeners)
        {
            currentListener.onSendMessage(sendMessageEvent);
        }

        // then fire error events
        CPTAWebsocketProtocolErrorEvent errorEvent = new CPTAWebsocketProtocolErrorEvent(this, subscription, error);
        // go through listeners and fire error
        for(CPTAWebsocketProtocolStateMachineListener currentListener : listeners)
        {
            currentListener.onError(errorEvent);
        }

        // then fire unsubscribe events
        CPTAWebsocketProtocolUnsubscribedEvent unsubscribeEvent = new CPTAWebsocketProtocolUnsubscribedEvent(this, subscription);
        // go through listeners and fire unsubcribed
        for(CPTAWebsocketProtocolStateMachineListener currentListener : listeners)
        {
            currentListener.onUnsubscribed(unsubscribeEvent);
        }
        // remove the subscription
        String subscriptionID = subscription.getID();
        mapOfIdsToSubscriptions.remove(subscriptionID);
    }

    @Override
    public void handleClose(CPTAGraphQLSubscription<?, ?> subscription) 
    {
        // remove the subscription
        String subscriptionID = subscription.getID();
        mapOfIdsToSubscriptions.remove(subscriptionID);
    }

    public void handleIncomingMessage(String message)
    {
        try
        {
            // get the event
            CPTAWebsocketProtocolStateMachineEvent eventGeneratedByMessage = getEventFromMessage(message);
            // get the type
            CPTAWebsocketProtocolMachineEventType generatedEventType = eventGeneratedByMessage.getType();
            // handle depending on the type
            if(CPTAWebsocketProtocolMachineEventType.LOGGED_OFF == generatedEventType)
            {
                handleLoggedOff((CPTAWebsocketProtocolLoggedOffEvent)eventGeneratedByMessage);
            }
            else if(CPTAWebsocketProtocolMachineEventType.LOG_OFF_REQUESTED == generatedEventType)
            {
                handleLogoffRequest((CPTAWebsocketProtocolLogoffRequestEvent)eventGeneratedByMessage);
            }
            else if(CPTAWebsocketProtocolMachineEventType.LOG_ON_REQUESTED == generatedEventType)
            {
                handleLogonRequest((CPTAWebsocketProtocoLogonRequestEvent)eventGeneratedByMessage);
            }
            else if(CPTAWebsocketProtocolMachineEventType.LOGGED_ON == generatedEventType)
            {
                handleLoggedOn((CPTAWebsocketProtocolLoggedOnEvent)eventGeneratedByMessage);
            }
            else if(CPTAWebsocketProtocolMachineEventType.KEEP_ALIVE == generatedEventType)
            {
                handleKeepAlive((CPTAWebsocketProtocolKeepAliveEvent)eventGeneratedByMessage);
            }
            else if(CPTAWebsocketProtocolMachineEventType.SUBSCRIBE_REQUEST == generatedEventType)
            {
                handleSubscribeRequest((CPTAWebsocketProtocolSubscribeRequestEvent)eventGeneratedByMessage);
            }
            else if(CPTAWebsocketProtocolMachineEventType.SUBSCRIBED == generatedEventType)
            {
                handleSubscribed((CPTAWebsocketProtocolSubscribedEvent)eventGeneratedByMessage);
            }
            else if(CPTAWebsocketProtocolMachineEventType.UNSUBSCRIBE_REQUEST == generatedEventType)
            {
                handleUnsubscribeRequest((CPTAWebsocketProtocolUnsubscribeRequestEvent)eventGeneratedByMessage);
            }
            else if(CPTAWebsocketProtocolMachineEventType.UNSUBSCRIBED == generatedEventType)
            {
                handleUnsubscribed((CPTAWebsocketProtocolUnsubscribedEvent)eventGeneratedByMessage);
            }
        }
        catch(Throwable E)
        {
            // handle any exception
            handleError(E);
        }
    }

    protected void pumpSubscriptions()
    {
        // number of messages sent
        long numberOfMessagesSent = 0;
        // get the subscriptions
        Collection<CPTAGraphQLSubscription<?, ?>> subscriptions = mapOfIdsToSubscriptions.values();
        // loop over each subscription
        for(CPTAGraphQLSubscription<?, ?> currentSubscription : subscriptions)
        {
            long numberOfessagesSentByCurrentSubscription = currentSubscription.getMessagesAndPublishThem();
            // add it to count
            numberOfMessagesSent = numberOfMessagesSent + numberOfessagesSentByCurrentSubscription;
        }

        // if we sent nothing
        if(0 == numberOfMessagesSent)
        {
            // keep alive
        }
    }

    public void addWebsocketProtocolStateMachineListener(CPTAWebsocketProtocolStateMachineListener listenerToAdd)
    {
        listeners.add(listenerToAdd);
    }

    public void removeWebsocketProtocolStateMachineListener(CPTAWebsocketProtocolStateMachineListener listenerToRemove)
    {
        listeners.remove(listenerToRemove);
    }

    protected abstract CPTAWebsocketProtocolStateMachineEvent getEventFromMessage(String message);
    protected abstract String getSubscriptionSucceededMessage(CPTAGraphQLSubscription<?, ?> subscription);
    protected abstract String getMesageFromResult(CPTAGraphQLSubscription<?, ?> subscription, String resultAsString);
    protected abstract String getMessageFromError(CPTAGraphQLSubscription<?, ?> subscription, Throwable error);
    protected abstract String getKeepAliveMessage();

    protected abstract void handleLogonRequest(CPTAWebsocketProtocoLogonRequestEvent request);
    protected abstract void handleLoggedOn(CPTAWebsocketProtocolLoggedOnEvent request);
    protected abstract void handleLogoffRequest(CPTAWebsocketProtocolLogoffRequestEvent request);
    protected abstract void handleLoggedOff(CPTAWebsocketProtocolLoggedOffEvent request);
    protected abstract void handleKeepAlive(CPTAWebsocketProtocolKeepAliveEvent anyAdditionalInformation);
    protected abstract void handleError(Throwable error);
    protected abstract void handleSubscribeRequest(CPTAWebsocketProtocolSubscribeRequestEvent request);
    protected abstract void handleSubscribed(CPTAWebsocketProtocolSubscribedEvent request);
    protected abstract void handleUnsubscribeRequest(CPTAWebsocketProtocolUnsubscribeRequestEvent request);
    protected abstract void handleUnsubscribed(CPTAWebsocketProtocolUnsubscribedEvent request);
    protected abstract void handleLogonAccepted();

    protected Set<CPTAWebsocketProtocolStateMachineListener> listeners = new HashSet<>();
    protected Map<String, String> connectionParameters = new ConcurrentHashMap<>();
    protected Map<String, CPTAGraphQLSubscription<?, ?>> mapOfIdsToSubscriptions = new ConcurrentHashMap<>();
    protected CPTASubscriptionsPumpThread pumpThread = null;
    protected long lastKeepAliveMessageSent = 0;
}

class CPTASubscriptionsPumpThread extends Thread
{
    public CPTASubscriptionsPumpThread(CPTAWebsocketProtocolStateMachine newProtocolStackMachine)
    {
        protocolStackMachine = newProtocolStackMachine;
    }

    @Override
    public void run()
    {
        // 
    }

    CPTAWebsocketProtocolStateMachine protocolStackMachine;
    AtomicBoolean shouldRun = new AtomicBoolean(true);
}