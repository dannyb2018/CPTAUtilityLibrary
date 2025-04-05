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
package com.cloudpta.graphql.subscriptions.protocol.event;

import java.util.Map;
import com.cloudpta.graphql.subscriptions.protocol.CPTAWebsocketProtocolStateMachine;

public class CPTAWebsocketProtocolSubscribeRequestEvent extends CPTAWebsocketProtocolStateMachineEvent
{
    protected CPTAWebsocketProtocolSubscribeRequestEvent
                                                       (
                                                       CPTAWebsocketProtocolStateMachine newMachine,
                                                       String newSubscriptionID,
                                                       String newOperationName,
                                                       Map<String, Object> newVariables
                                                       ) 
    {
        super(newMachine);

        subscriptionID = newSubscriptionID;
        operationName = newOperationName;
        variables = newVariables;
    }

    public String getSubscriptionID()
    {
        return subscriptionID;
    }

    public String getOperationName()
    {
        return operationName;
    }

    public Map<String, Object> getVariables()
    {
        return variables;
    }

    protected String subscriptionID = null;
    protected String operationName = null;
    protected Map<String, Object> variables = null;
}
