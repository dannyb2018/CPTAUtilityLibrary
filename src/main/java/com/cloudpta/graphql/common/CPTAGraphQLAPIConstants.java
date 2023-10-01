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
package com.cloudpta.graphql.common;

public interface CPTAGraphQLAPIConstants 
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

    // fields for graphql queries
    // For incoming operations
    public final static String OPERATION_NAME = "operationName";    
    public final static String OPERATION_TEXT = "query";    
    public final static String OPERATION_VARIABLES = "variables";  

    // What is stored by default in context
    public final static String CONTEXT_OPERATION_NAME = "operation_name";    
    public final static String CONTEXT_REMOTE_HOST = "remote_host";    
    public final static String CONTEXT_OPERATION_REQUEST = "operation";    
    public final static String CONTEXT_OPERATION_VARIABLES = "operation_variables";

    // fields in building the graphQL
    public final static String OPERATION_MUTATION_TYPE = "mutation";    
    public final static String OPERATION_QUERY_TYPE = "query";    
    public final static String OPERATION_SUBSCRIPTION_TYPE = "subscription";    
    public final static String WIRING_MUTATION_TYPE = "Mutation";    
    public final static String WIRING_QUERY_TYPE = "Query";    
    public final static String WIRING_SUBSCRIPTION_TYPE = "Subscription";    
    public final static String SUBSCRIPTION_HOLDER_SCHEMA = "schema {\n" + "query:Query\n" + "subscription:Subscription \n" + "}";  
    public final static String SUBSCRIPTION_DUMMY_QUERY_SCHEMA = "type Query{ \n" + "dummyQuery:String \n" + "}";  
    
    // Get the names of standard qp fields in context
    public final static String SUBSCRIPTION_TIMEOUT = "QP_SUBSCRIPTION_TIMEOUT";

    // Get names of standard kafka subscription properties
    public final static String KAFKA_BOOTSTRAP_BROKERS_URL = "QP_KAFKA_BOOTSTRAP_BROKERS_URL";
    public final static String KAFKA_GROUP_ID = "QP_KAFKA_GROUP_ID";
    public final static String KAFKA_OFFSET_RESET = "QP_KAFKA_OFFSET_RESET";
    public final static String KAFKA_TOPIC_TO_BROWSE = "QP_KAFKA_TOPIC_TO_BROWSE";
    public final static String KAFKA_SCHEMA_TO_USE = "QP_KAFKA_SCHEMA_TO_USE";
    public final static String KAFKA_OFFSET_RESET_SKIP_MISSED_MESSAGES = "latest";
    public final static String KAFKA_OFFSET_RESET_NOT_SKIP_MISSED_MESSAGES = "earliest";

    // for query modifications
    public final static String MODIFIED_QUERY_SCHEMA = "MODIFIED_QUERY_SCHEMA";
    public final static String MODIFIED_MUTATION_SCHEMA = "MODIFIED_MUTATION_SCHEMA";
    public final static String MODIFIED_SUBSCRIPTION_SCHEMA = "MODIFIED_SUBSCRIPTION_SCHEMA";
}
