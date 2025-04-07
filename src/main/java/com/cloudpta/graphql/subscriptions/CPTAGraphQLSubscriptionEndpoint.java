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
package com.cloudpta.graphql.subscriptions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.reactivestreams.Publisher;
import com.cloudpta.embedded_jetty.CPTAWebSocket;
import com.cloudpta.graphql.common.CPTAGraphQLAPIConstants;
import com.cloudpta.graphql.common.CPTAGraphQLAuditor;
import com.cloudpta.graphql.common.CPTAGraphQLHandler;
import com.cloudpta.graphql.common.CPTAGraphQLQueryType;
import com.cloudpta.graphql.subscriptions.protocol.CPTAWebsocketProtocolStateMachine;
import com.cloudpta.graphql.subscriptions.protocol.CPTAWebsocketProtocolStateMachineListener;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocoLogonRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolErrorEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolKeepAliveEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLoggedOffEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLoggedOnEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolLogoffRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolSendMessageEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolSubscribeRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolSubscribedEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolUnsubscribeRequestEvent;
import com.cloudpta.graphql.subscriptions.protocol.event.CPTAWebsocketProtocolUnsubscribedEvent;
import com.cloudpta.utilites.exceptions.CPTAException;
import com.cloudpta.utilites.logging.CPTALogger;
import ch.qos.logback.classic.Logger;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLContext;
import graphql.GraphQLError;
import graphql.execution.reactive.SubscriptionPublisher;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import jakarta.json.JsonObject;

public class CPTAGraphQLSubscriptionEndpoint<ProtocolStateMachine extends CPTAWebsocketProtocolStateMachine> extends CPTAWebSocket implements CPTAWebsocketProtocolStateMachineListener
{
    // BUGBUGDB make abstract later

    // called during returnNewInstance to make a new protocol state machine
    protected void returnNewProtocolStateMachine()
    {

    }

    // called during returnNewInstance, returns the concrete implementation of this endpoint
    protected CPTAGraphQLSubscriptionEndpoint<ProtocolStateMachine> returnNewEndpoint()
    {
        return this;
    }

    // This should return any existing graph ql build or null if there is not one
    protected GraphQL getExistingGraphQL(GraphQLContext context) throws CPTAException
    {
        return null;
    }

    protected TypeDefinitionRegistry getExistingTypeDefinitionRegistry(GraphQLContext context) throws CPTAException
    {
        return null;
    }
    
    protected GraphQL getExistingSubscriptionBuild(TypeDefinitionRegistry typeRegistry, GraphQLContext context) throws CPTAException
    {
        return null;
    }

    // These are the inputs to the various schemas
    protected InputStream getTypesSchemaStream(GraphQLContext context) throws CPTAException
    {
        return null;
    }
    protected InputStream getSubscriptionSchemaStream(GraphQLContext context) throws CPTAException
    {
        return null;
    }

    // get handlers
    protected List<CPTAGraphQLHandler> getHandlers(GraphQLContext context)
    {
        return null;
    }
    
    // BUGBUGDB no longer abstract


    protected void setupAuditor(GraphQLContext context)
    {
        // default do nothing
        // child classes can override to use different auditor
    }

    protected void addCustomTypeDefinitionsToRegistry(GraphQLContext context, TypeDefinitionRegistry mergedTypeDefinitionRegistry)
    {
        // ask handlers to add custom types
        // go through the handlers
        List<CPTAGraphQLHandler> handlers = getHandlers(context);

        for(CPTAGraphQLHandler currentHandler:handlers)
        {
            currentHandler.addCustomTypeDefinitionsToRegistry(CPTAGraphQLQueryType.SUBSCRIPTION, context, mergedTypeDefinitionRegistry);
        }
    }

    @Override
    public void onSendMessage(CPTAWebsocketProtocolSendMessageEvent sendEvent) throws CPTAException 
    {
        try
        {
            // if there is a open socket session
            if(true == socketSession.isOpen())
            {
                // get the text to send
                String messageToSend = sendEvent.getMessageToSend();
                socketSession.getRemote().sendString(messageToSend);
            }
        }
        catch(Throwable E)
        {
            // create a wrapped exception and throw it
            CPTAException wrappedException = new CPTAException(E);
            throw wrappedException;
        }
    }

    @Override
    public void onLogonRequest(CPTAWebsocketProtocoLogonRequestEvent logonRequestEvent) 
    {
        // get the connection parameters map
        Map<String, String> logonRequestParameters = logonRequestEvent.getLogonRequestParameters();
        Map<String, String> connectionInitialisationParameters = getConnectionInitialisationParameters(logonRequestParameters, socketSession);

        // BUGBUGDB have option to validate initialisation parameters
        
        // set the connection parameters in the protocol machine
        protocolStateMachine.logonAccepted(connectionInitialisationParameters);
    }

    @Override
    public void onSubscribeRequest(CPTAWebsocketProtocolSubscribeRequestEvent subscribeRequestedEvent) 
    {
        // Get id
        String id = subscribeRequestedEvent.getSubscriptionID();  
        // Get operation name
        String operationName = subscribeRequestedEvent.getOperationName();
        // get graphQL query name
        String graphQLQueryName = subscribeRequestedEvent.getGraphQLQueryName();
        // get variables
        Map<String, Object> variables = subscribeRequestedEvent.getVariables();

        // Build the context for the query
        GraphQLContext contextForQuery = GraphQLContext.newContext().build();
        // get the connection parameters
        Map<String, String> connectionParameters = protocolStateMachine.getConnectionParameters();
        initialiseContext(contextForQuery, connectionParameters, graphQLQueryName, operationName, variables);

        // Set up all the graphql if need to
        GraphQL build = getGraphQL(contextForQuery);

        // run an audit
        CPTAGraphQLAuditor auditor = CPTAGraphQLAuditor.getAuditor(contextForQuery);
        auditor.audit(contextForQuery);


        // Build the input for the query
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                                                    .query(graphQLQueryName)
                                                    .operationName(operationName)
                                                    .localContext(contextForQuery)
                                                    .variables(variables)
                                                    .build();        
        // Get result
        ExecutionResult executionResult = build.execute(executionInput); 
        // Check if there are any errors
        List<GraphQLError> errors = executionResult.getErrors();
        // Get publisher of that result        
        Publisher<ExecutionResult> responseStream = executionResult.getData();

        // get the subscription publisher so we can get the associated subscription 
        SubscriptionPublisher responseStreamAsPublisher = (SubscriptionPublisher)responseStream;
        // get the subscription for the publisher
        CPTAGraphQLSubscription<?,?> subscription = CPTAGraphQLSubscription.getSubscription(responseStreamAsPublisher);
        // set the id
        subscription.setID(id);
        // save the subscription with the protocol state machine
        protocolStateMachine.saveSubscription(subscription);
        // set the subscription listener to be the protocol state machine
        subscription.setListener(protocolStateMachine);
        
        // If there are errors
        if((0 < errors.size()) || (null == responseStream))
        {
            // This is the execution result as json
            Map<String, Object> resultAsJson = executionResult.toSpecification();
            protocolStateMachine.subscriptionFailed(subscription, resultAsJson);
        }   
        // If there are no errors
        else
        {
            // Subscribe to this stream
            responseStream.subscribe(subscription);
            // tell protocol machine that we are subscribed
            protocolStateMachine.subscriptionSucceeded(subscription);
        }
    }

    @Override
    public void onUnsubscribeRequest(CPTAWebsocketProtocolUnsubscribeRequestEvent subscribeRequestedEvent) 
    {
        // for now do nothing
    }

    @Override
    public void onUnsubscribed(CPTAWebsocketProtocolUnsubscribedEvent subscribedEvent) 
    {
        // remove the subscription
    }


    @Override
    public void onLoggedOn(CPTAWebsocketProtocolLoggedOnEvent loggedOnEvent) 
    {
        // for now do nothing
    }

    @Override
    public void onLogoffRequest(CPTAWebsocketProtocolLogoffRequestEvent logoffRequestEvent) 
    {
        // for now do nothing
    }

    @Override
    public void onLoggedoff(CPTAWebsocketProtocolLoggedOffEvent loggedOffEvent) 
    {
        // for now do nothing
    }

    @Override
    public void onKeepAlive(CPTAWebsocketProtocolKeepAliveEvent keepAliveEvent) 
    {
        // for now do nothing
    }

    @Override
    public void onError(CPTAWebsocketProtocolErrorEvent errorEvent) 
    {
        // for now do nothing
    }

    @Override
    public void onSubscribed(CPTAWebsocketProtocolSubscribedEvent subscribedEvent) 
    {
        // for now do nothing
    }

    protected Map<String, String> getConnectionInitialisationParameters(Map<String, String> logonRequestParameters, Session socketSession)
    {
        Map<String, String> initialisationParameters = new HashMap<>();

        // Add remote host
        String remoteHost = socketSession.getRemoteAddress().toString();
        initialisationParameters.put(CPTAGraphQLAPIConstants.CONTEXT_REMOTE_HOST, remoteHost);

        // add the logon parameters
        initialisationParameters.putAll(logonRequestParameters);

        // get the headers for the socket
        UpgradeRequest originalHttpRequest = socketSession.getUpgradeRequest();
        Map<String, List<String>> headers = socketSession.getUpgradeRequest().getHeaders();
        Iterator<String> headerNames = headers.keySet().iterator();
        while(true == headerNames.hasNext())
        {
            // gets current header name and value
            String currentHeaderName = headerNames.next();
            String currentHeaderValue = originalHttpRequest.getHeader(currentHeaderName);
            // adds to list of initialisation parameters
            initialisationParameters.put(currentHeaderName.toLowerCase(), currentHeaderValue);
        }

        // Add all the cookies
        List<HttpCookie> cookies = socketSession.getUpgradeRequest().getCookies();
        for(HttpCookie currentCookie : cookies)
        {
            String currentCookieName = currentCookie.getName();
            String currentCookieValue = currentCookie.getValue();
            initialisationParameters.put(currentCookieName, currentCookieValue);
        }

        return initialisationParameters;
    }

    protected void initialiseContext(GraphQLContext contextToInitialise, Map<String, String> socketRequestDetails, String operationName, String operationAsString, Map<String, Object> variables)
    {
        // Default just saves the operation name, operation as string, request and variables
        if(null != operationName)
        {
            contextToInitialise.put(CPTAGraphQLAPIConstants.CONTEXT_OPERATION_NAME, operationName);
        }
        contextToInitialise.put(CPTAGraphQLAPIConstants.CONTEXT_OPERATION_REQUEST, operationAsString);
        contextToInitialise.put(CPTAGraphQLAPIConstants.CONTEXT_OPERATION_VARIABLES, variables);

        // Add all the socket request details
        Set<String> socketRequestPropertyNames = socketRequestDetails.keySet();
        for(String currentSocketRequestPropertyName : socketRequestPropertyNames)
        {
            String currentSocketRequestPropertyValue = socketRequestDetails.get(currentSocketRequestPropertyName);
            contextToInitialise.put(currentSocketRequestPropertyName, currentSocketRequestPropertyValue);
        }

        // But this is where you set up the context for the query like any DB or topics 
    }

    protected GraphQL getGraphQL(GraphQLContext context) throws CPTAException
    {
        // Check if we havent already set up graphQL
        GraphQL operationBuild = getExistingGraphQL(context);
        // If we have not already set up graphQL
        if(null == operationBuild)
        {
            // set auditor if needed
            setupAuditor(context);

            // Get merged registry
            TypeDefinitionRegistry mergedTypeRegistry = getTypeDefinitionRegistry(context);
            
            // set up the graphql instance for the this
            operationBuild = getSubscriptionBuild(mergedTypeRegistry, context);
        }

        return operationBuild;

    }

    protected TypeDefinitionRegistry getTypeDefinitionRegistry(GraphQLContext context) throws CPTAException
    {
        // See if it is already setup
        TypeDefinitionRegistry mergedTypeDefinitionRegistry = getExistingTypeDefinitionRegistry(context);
        // If not
        if(null == mergedTypeDefinitionRegistry)
        {
                // get path to schemas
                InputStream typesSchemaStream = getTypesSchemaStream(context);
                InputStream subscriptionsSchemaStream = getSubscriptionSchemaStream(context);

                // Start with the types schema
                BufferedReader schemaStream = new BufferedReader(new InputStreamReader(typesSchemaStream, StandardCharsets.UTF_8));
                String apiTypesSchema = schemaStream.lines().collect(Collectors.joining("\n"));
                // Then with subscriptions schema
                schemaStream = new BufferedReader(new InputStreamReader(subscriptionsSchemaStream, StandardCharsets.UTF_8));
                String subscriptionsSchema = " type Subscription { " + schemaStream.lines().collect(Collectors.joining("\n")) + " } ";
                // Need dummy query type
                String dummyQuerySchema = "type Query{ \n" +
                    "dummyQuery:String \n" +
                "}";
                // Finally schema to bring it all together
                String holderSchema = CPTAGraphQLAPIConstants.SUBSCRIPTION_HOLDER_SCHEMA;

                // Parse these schemas
                SchemaParser schemaParser = new SchemaParser();
                TypeDefinitionRegistry apiTypeDefinitionRegistry = schemaParser.parse(apiTypesSchema);
                TypeDefinitionRegistry subscriptionTypeDefinitionRegistry = schemaParser.parse(subscriptionsSchema);
                TypeDefinitionRegistry dummyQueryDefinitionRegistry = schemaParser.parse(dummyQuerySchema);
                TypeDefinitionRegistry schemaTypeDefinitionRegistry = schemaParser.parse(holderSchema);

                // Merge them
                mergedTypeDefinitionRegistry = new TypeDefinitionRegistry();
                mergedTypeDefinitionRegistry.merge(apiTypeDefinitionRegistry);
                mergedTypeDefinitionRegistry.merge(subscriptionTypeDefinitionRegistry);
                mergedTypeDefinitionRegistry.merge(dummyQueryDefinitionRegistry);              
                mergedTypeDefinitionRegistry.merge(schemaTypeDefinitionRegistry);              
        }

        // Add custom type definitions
        addCustomTypeDefinitionsToRegistry(context, mergedTypeDefinitionRegistry);

        return mergedTypeDefinitionRegistry;
    }

    protected GraphQL getSubscriptionBuild(TypeDefinitionRegistry typeRegistry, GraphQLContext context) throws CPTAException
    {
        // Get the subscription build
        GraphQL subscriptionBuild = getExistingSubscriptionBuild(typeRegistry,context);
        // If we havent already set up graphQL
        if(null == subscriptionBuild)
        {
            try
            {
                // Build the executable schema for subscriptions
                // Add data fetchers
                Map<String, DataFetcher<?>> fetcherForThisBuild = getDataFetchersForSubscription(context);
                @SuppressWarnings("rawtypes")
                Map<String, DataFetcher> dataFetchers = new HashMap<>(fetcherForThisBuild);
                graphql.schema.idl.RuntimeWiring.Builder runtimeWiringBuilder= 
                RuntimeWiring.newRuntimeWiring()
                .type
                (
                    TypeRuntimeWiring.newTypeWiring(CPTAGraphQLAPIConstants.WIRING_SUBSCRIPTION_TYPE)
                    .dataFetchers(dataFetchers)
                );
                // Add type resolvers
                addTypeResolversForSubscription(runtimeWiringBuilder, context);

                // Generate schema to be made executable           
                RuntimeWiring runtimeWiring = runtimeWiringBuilder.build();
                SchemaGenerator schemaGenerator = new SchemaGenerator();
                GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);

                subscriptionBuild = GraphQL.newGraphQL(graphQLSchema).build();   
            }
            catch(Exception E)
            {
                CPTAException wrappedException = new CPTAException(E);
                throw wrappedException;
            }
        }  
        
        return subscriptionBuild;
    }
    
    protected void addTypeResolversForSubscription(graphql.schema.idl.RuntimeWiring.Builder subscriptionRuntimeWiringBuilder, GraphQLContext context) throws CPTAException
    {
        // get the handlers
        List<CPTAGraphQLHandler> handlers = getHandlers(context);

        // for each handler add type resolvers
        for(CPTAGraphQLHandler currentHandler : handlers)
        {
            currentHandler.addTypeResolversForQueryType(CPTAGraphQLQueryType.SUBSCRIPTION, subscriptionRuntimeWiringBuilder, context);
        }
    }

    protected Map<String, DataFetcher<?>> getDataFetchersForSubscription(GraphQLContext context) throws CPTAException
    {
        Map<String, DataFetcher<?>> allDataFetchers = new ConcurrentHashMap<>();

        // Get all the handlers
        List<CPTAGraphQLHandler> handlers = getHandlers(context);
        
        // for each handler add data handlers
        for(CPTAGraphQLHandler currentHandler: handlers)
        {
            Map<String, DataFetcher<?>> dataFetchersForHandler = currentHandler.getDataFetchersForQueryType(CPTAGraphQLQueryType.SUBSCRIPTION, context);
            allDataFetchers.putAll(dataFetchersForHandler);
        }

        return allDataFetchers;
    }

    @Override
    protected Object returnNewInstance() 
    {
        // get an instance of this endpoint
        CPTAGraphQLSubscriptionEndpoint<ProtocolStateMachine> newInstance = returnNewEndpoint();
        // initialise protocol engine
        newInstance.returnNewProtocolStateMachine();
        // add listener for protocol events
        protocolStateMachine.addWebsocketProtocolStateMachineListener(newInstance);

        // return endpoint
        return newInstance;
    }

    @Override
    protected void handleIncomingMessage(String queryAsString) 
    {
        try
        {
            protocolStateMachine.handleIncomingMessage(queryAsString);          
        }
        catch(Exception E)
        {
            CPTAException wrappedException = new CPTAException(E);
            socketLogger.trace("Exception whilst handling graphQL request + " + queryAsString);
            JsonObject result = wrappedException.getErrors();
            String json = result.toString();
            socketLogger.error("Couldnt execute graphql, reason " + json);           
        }
    }

    @Override
    protected void handleConnected(Session session) 
    {
        socketSession = session;
    }

    @Override
    protected void handleClose(int statusCode, String reason) 
    {
        protocolStateMachine.handleConnectionClosed();
    }

    @Override
    protected void handleError(Throwable t) 
    {
        protocolStateMachine.handleConnectionClosed();
    }
    
    protected Logger socketLogger = CPTALogger.getLogger();    
    protected Session socketSession;
    protected ProtocolStateMachine protocolStateMachine = null;
}
