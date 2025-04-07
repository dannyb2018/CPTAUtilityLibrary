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

import com.cloudpta.graphql.subscriptions.CPTAGraphQLSubscription;
import com.cloudpta.graphql.subscriptions.protocol.CPTAWebsocketProtocolStateMachine;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocoLogonRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolKeepAliveEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLoggedOffEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLoggedOnEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLogoffRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolStateMachineEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolSubscribeRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolSubscribedEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolUnsubscribeRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolUnsubscribedEvent;

public class CPTASubscriptionsTransportWSProtocolStateMachine extends CPTAWebsocketProtocolStateMachine
{
    @Override
    protected CPTAWebsocketProtocolStateMachineEvent getEventFromMessage(String message) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEventFromMessage'");
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
    protected void handleLogonRequest(CPTAWebsocketProtocoLogonRequestEvent request) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleLogonRequest'");
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
    
}
