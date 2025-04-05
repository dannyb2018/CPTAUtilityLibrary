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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.cloudpta.graphql.subscriptions.CPTAGraphQLSubscription;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocoLogonRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolKeepAliveEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLoggedOffEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLoggedOnEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLogoffRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolMachineEventType;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolStateMachineEvent;

public abstract class CPTAWebsocketProtocolStateMachine 
{
    public void addWebsocketProtocolStateMachineListener(CPTAWebsocketProtocolStateMachineListener listenerToAdd)
    {
        listeners.add(listenerToAdd);
    }

    public void removeWebsocketProtocolStateMachineListener(CPTAWebsocketProtocolStateMachineListener listenerToRemove)
    {
        listeners.remove(listenerToRemove);
    }

    public abstract void sendData(String data);

    public void handleIncomingData(String message)
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
        }
        catch(Throwable E)
        {
            // handle any exception
            handleError(E);
        }
    }

    protected abstract CPTAWebsocketProtocolStateMachineEvent getEventFromMessage(String message);

    protected abstract void handleLogonRequest(CPTAWebsocketProtocoLogonRequestEvent request);
    protected abstract void handleLoggedOn(CPTAWebsocketProtocolLoggedOnEvent request);
    protected abstract void handleLogoffRequest(CPTAWebsocketProtocolLogoffRequestEvent request);
    protected abstract void handleLoggedOff(CPTAWebsocketProtocolLoggedOffEvent request);
    protected abstract void handleKeepAlive(CPTAWebsocketProtocolKeepAliveEvent anyAdditionalInformation);
    protected abstract void handleError(Throwable error);

    protected Set<CPTAWebsocketProtocolStateMachineListener> listeners = new HashSet<>();
    protected Map<String, CPTAGraphQLSubscription<?, ?>> mapOfIdsToSubscriptions = new ConcurrentHashMap<>();
}
