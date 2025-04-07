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

public interface CPTASubscriptionsTransportWSProtocolConstants 
{
    // fields for subscription requests
    public final static String PAYLOAD_ID = "id";    
    public final static String PAYLOAD_TYPE = "type";  
    public final static String PAYLOAD = "payload";
    public final static String PAYLOAD_TYPE_DATA = "data";
    public final static String PAYLOAD_TYPE_STOP = "stop";  
    public final static String PAYLOAD_TYPE_START = "start"; 
    public final static String PAYLOAD_TYPE_ERROR = "error"; // Server -> Client
    public final static String PAYLOAD_TYPE_COMPLETE = "complete"; 
    public final static String PAYLOAD_TYPE_CONNECTION_TERMINATE = "connection_terminate"; // Client -> Server
    public final static String PAYLOAD_TYPE_CONNECTION_KEEP_ALIVE = "ka";
    public final static String PAYLOAD_TYPE_CONNECTION_ERROR = "connection_error";
    public final static String PAYLOAD_TYPE_CONNECTION_INIT = "connection_init";  
    public final static String CONNECTION_INIT_RESPONSE = "{ \"type\": \"connection_ack\", \"payload\": {} }";    
}
