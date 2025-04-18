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

public class CPTAWebsocketProtocoLogonRequestEvent extends CPTAWebsocketProtocolStateMachineEvent
{

    public CPTAWebsocketProtocoLogonRequestEvent
                                               (
                                               CPTAWebsocketProtocolStateMachine newMachine,
                                               Map<String, String> newLogonRequestParameters
                                               ) 
    {
        super(newMachine);

        // set event type to error
        eventType = CPTAWebsocketProtocolMachineEventType.LOG_ON_REQUESTED;

        // save the logon request parameters
        logonRequestParameters = newLogonRequestParameters;
    }

    public Map<String, String> getLogonRequestParameters()
    {
        return logonRequestParameters;
    }
    
    protected Map<String, String> logonRequestParameters;
}
