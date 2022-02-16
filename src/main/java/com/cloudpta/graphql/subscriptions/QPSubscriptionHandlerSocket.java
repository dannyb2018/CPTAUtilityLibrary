////////////////////////////////////////////////////////////////////////////////
//
//                                 NOTICE:
//  THIS PROGRAM CONSISTS OF TRADE SECRECTS THAT ARE THE PROPERTY OF
//  Advanced Products Ltd. THE CONTENTS MAY NOT BE USED OR DISCLOSED
//  WITHOUT THE EXPRESS WRITTEN PERMISSION OF THE OWNER.
//
//               COPYRIGHT Advanced Products Ltd 2016-2019
//
////////////////////////////////////////////////////////////////////////////////
package com.cloudpta.graphql.subscriptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import com.cloudpta.utilites.exceptions.CPTAException;
import com.cloudpta.utilites.logging.CPTALogger;
import com.cloudpta.graphql.common.QPGraphQLAPIConstants;
import com.cloudpta.graphql.common.QPQueryVariablesParser;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import ch.qos.logback.classic.Logger;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLContext;
import graphql.GraphQLError;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;

public abstract class QPSubscriptionHandlerSocket extends WebSocketAdapter
{
    @Override
    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);
        handleConnected(session);         
    }

    @Override
    public void onWebSocketText(String message)
    {
        super.onWebSocketText(message);
        handleIncomingMessage(message);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);
        handleClose(statusCode, reason);
    }

    @Override
    public void onWebSocketError(Throwable cause)
    {
        super.onWebSocketError(cause);
        handleError(cause); 
    }

    protected void handleInitialiseSubscriptionRequest(JsonObject queryAsJson) throws IOException
    {
        // Get id
        String id = queryAsJson.getString(QPGraphQLAPIConstants.PAYLOAD_ID);
        // Get the payload
        JsonObject requestParamsForThisSubscription = queryAsJson.getJsonObject(QPGraphQLAPIConstants.PAYLOAD); 
        requestParamsForSubscriptions.put(id, requestParamsForThisSubscription);
        // send ack back
        socketSession.getRemote().sendString(QPGraphQLAPIConstants.CONNECTION_INIT_RESPONSE);
    }

    protected void handleStartSubscriptionRequest(JsonObject queryAsJson) throws CPTAException, IOException
    {
        // Get id
        String id = queryAsJson.getString(QPGraphQLAPIConstants.PAYLOAD_ID);
        // handle query
        queryAsJson = queryAsJson.getJsonObject(QPGraphQLAPIConstants.PAYLOAD);  
        // Get operation name
        String operationName = null;
        if(false == queryAsJson.isNull(QPGraphQLAPIConstants.OPERATION_NAME))
        {
            operationName = queryAsJson.getString(QPGraphQLAPIConstants.OPERATION_NAME);
        }
        // get the query field
        String graphQLQuery = queryAsJson.getString(QPGraphQLAPIConstants.OPERATION_TEXT);
        // Need to turn variables into a map of keys and values
        JsonObject variablesAsJsonObject = queryAsJson.getJsonObject(QPGraphQLAPIConstants.OPERATION_VARIABLES);
        Map<String, Object> variables = QPQueryVariablesParser.parseVariables(variablesAsJsonObject);

        // Build the context for the query
        GraphQLContext contextForQuery = GraphQLContext.newContext().build();
        // Get the request params
        JsonObject requestParams = this.requestParamsForSubscriptions.get(id);
        Map<String, String> socketRequestDetails = convertRequestToMap(requestParams, socketSession);
        initialiseContext(contextForQuery, socketRequestDetails, graphQLQuery, operationName, variablesAsJsonObject);

        // Set up all the graphql if need to
        GraphQL build = getGraphQL(contextForQuery);

        // Build the input for the query
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                                                    .query(graphQLQuery)
                                                    .operationName(operationName)
                                                    .context(contextForQuery)
                                                    .variables(variables)
                                                    .build();        
        // Get result
        ExecutionResult executionResult = build.execute(executionInput); 
        // Check if there are any errors
        List<GraphQLError> errors = executionResult.getErrors();
        // Get publisher of that result        
        Publisher<ExecutionResult> responseStream = executionResult.getData();

        // If there are errors
        if((0 < errors.size()) || (null == responseStream))
        {
            JsonObjectBuilder response = Json.createObjectBuilder();
            // Add type
            response.add(QPGraphQLAPIConstants.PAYLOAD_TYPE, QPGraphQLAPIConstants.PAYLOAD_TYPE_DATA);
            // Add id
            response.add(QPGraphQLAPIConstants.PAYLOAD_ID, id);

            // Add payload
            // This is the execution result as json
            Map<String, Object> resultAsJson = executionResult.toSpecification();
            JsonObjectBuilder payloadAsJson = Json.createObjectBuilder(resultAsJson);
            response.add(QPGraphQLAPIConstants.PAYLOAD, payloadAsJson);

            // Convert that result into a json string
            String resultAsString = response.build().toString();
            socketSession.getRemote().sendString(resultAsString);
        }   
        // If there are no errors
        else
        {
            // Create a subscription
            AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
            QPSubscriptionFeed resultStreamSubscriber = new QPSubscriptionFeed(socketSession, subscriptionRef, id);

            // Subscribe to this stream
            responseStream.subscribe(resultStreamSubscriber);
        
            // save it
            subscription = resultStreamSubscriber;
        }
    }

    protected void handleStopSubscriptionRequest(JsonObject queryAsJson)
    {
        // Get id
        String id = queryAsJson.getString(QPGraphQLAPIConstants.PAYLOAD_ID);
        // Stop the subscription
        subscription.stop();
        subscription = null;

        // remove request params
        requestParamsForSubscriptions.remove(id);        
    }

    protected void handleIncomingMessage(String queryAsString) 
    {
        try
        {
            // Parse the query
            JsonReader reader = Json.createReader(new StringReader(queryAsString));
            JsonObject queryObject = reader.readObject(); 
            String type = queryObject.getString(QPGraphQLAPIConstants.PAYLOAD_TYPE);  
            // If we are an init
            if( 0 == type.compareTo(QPGraphQLAPIConstants.PAYLOAD_TYPE_CONNECTION_INIT))
            {
                handleInitialiseSubscriptionRequest(queryObject);
            }
            // If we are a start
            else if(0 == type.compareTo(QPGraphQLAPIConstants.PAYLOAD_TYPE_START))
            {
                handleStartSubscriptionRequest(queryObject);
            }
            // If we are a stop
            else if(0 == type.compareTo(QPGraphQLAPIConstants.PAYLOAD_TYPE_STOP))
            {
                handleStopSubscriptionRequest(queryObject);
            }            
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

    protected void handleConnected(Session session)
    {
        socketSession = session;
    }


    protected void handleClose(int statusCode, String reason) 
    {
        System.out.println(reason);
    }
    
    protected void handleError(Throwable t) 
    {
        t.printStackTrace();
        // Not used
    }    

    protected Map<String, String> convertRequestToMap(JsonObject httpRequest, Session socketSession)
    {
        Map<String, String> socketRequestDetails = new HashMap<>();

        // Add remote host
        String remoteHost = socketSession.getRemoteAddress().toString();
        socketRequestDetails.put(QPGraphQLAPIConstants.CONTEXT_REMOTE_HOST, remoteHost);

        // Add all the fields
        Iterator<String> headerNames = httpRequest.keySet().iterator();
        while(true == headerNames.hasNext())
        {
            String currentHeaderName = headerNames.next();
            String currentHeaderValue = httpRequest.getString(currentHeaderName);
            socketRequestDetails.put(currentHeaderName, currentHeaderValue);
        }

        // Add all the cookies
        List<HttpCookie> cookies = socketSession.getUpgradeRequest().getCookies();
        for(HttpCookie currentCookie : cookies)
        {
            String currentCookieName = currentCookie.getName();
            String currentCookieValue = currentCookie.getValue();
            socketRequestDetails.put(currentCookieName, currentCookieValue);
        }

        return socketRequestDetails;
    }
    protected void initialiseContext(GraphQLContext contextToInitialise, Map<String, String> socketRequestDetails, String operationName, String operationAsString, JsonObject variablesAsJsonObject)
    {
        // Default just saves the operation name, operation as string, request and variables
        if(null != operationName)
        {
            contextToInitialise.put(QPGraphQLAPIConstants.CONTEXT_OPERATION_NAME, operationName);
        }
        contextToInitialise.put(QPGraphQLAPIConstants.CONTEXT_OPERATION_REQUEST, operationAsString);
        contextToInitialise.put(QPGraphQLAPIConstants.CONTEXT_OPERATION_VARIABLES, variablesAsJsonObject);

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
                String subscriptionsSchema = schemaStream.lines().collect(Collectors.joining("\n"));
                // Finally schema to bring it all together
                String holderSchema = QPGraphQLAPIConstants.SUBSCRIPTION_HOLDER_SCHEMA;

                // Parse these schemas
                SchemaParser schemaParser = new SchemaParser();
                TypeDefinitionRegistry apiTypeDefinitionRegistry = schemaParser.parse(apiTypesSchema);
                TypeDefinitionRegistry subscriptionTypeDefinitionRegistry = schemaParser.parse(subscriptionsSchema);
                TypeDefinitionRegistry schemaTypeDefinitionRegistry = schemaParser.parse(holderSchema);

                // Merge them
                mergedTypeDefinitionRegistry = new TypeDefinitionRegistry();
                mergedTypeDefinitionRegistry.merge(apiTypeDefinitionRegistry);
                mergedTypeDefinitionRegistry.merge(subscriptionTypeDefinitionRegistry);
                mergedTypeDefinitionRegistry.merge(schemaTypeDefinitionRegistry);              
        }

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
                Map<String, DataFetcher> fetcherForThisBuild = getDataFetchersForSubscription(context);
                graphql.schema.idl.RuntimeWiring.Builder runtimeWiringBuilder= 
                RuntimeWiring.newRuntimeWiring()
                .type
                (
                    TypeRuntimeWiring.newTypeWiring(QPGraphQLAPIConstants.WIRING_SUBSCRIPTION_TYPE)
                    .dataFetchers(fetcherForThisBuild)
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

    // This should return any existing graph ql build or null if there is not one
    protected abstract GraphQL getExistingGraphQL(GraphQLContext context) throws CPTAException;
    protected abstract TypeDefinitionRegistry getExistingTypeDefinitionRegistry(GraphQLContext context) throws CPTAException;
    protected abstract GraphQL getExistingSubscriptionBuild(TypeDefinitionRegistry typeRegistry, GraphQLContext context) throws CPTAException;

    // These are the inputs to the various schemas
    protected abstract InputStream getTypesSchemaStream(GraphQLContext context) throws CPTAException;
    protected abstract InputStream getSubscriptionSchemaStream(GraphQLContext context) throws CPTAException;

    // get handlers
    protected abstract List<QPSubscriptionHandler> getHandlers();
    
    protected void addTypeResolversForSubscription(graphql.schema.idl.RuntimeWiring.Builder subscriptionRuntimeWiringBuilder, GraphQLContext context) throws CPTAException
    {
        // get the handlers
        List<QPSubscriptionHandler> handlers = getHandlers();

        // for each handler add type resolvers
        for(QPSubscriptionHandler currentHandler : handlers)
        {
            currentHandler.addTypeResolversForSubscriptions(subscriptionRuntimeWiringBuilder, context);
        }
    }

    protected Map<String, DataFetcher> getDataFetchersForSubscription(GraphQLContext context) throws CPTAException
    {
        Map<String, DataFetcher> allDataFetchers = new ConcurrentHashMap<>();

        // Get all the handlers
        List<QPSubscriptionHandler> handlers = getHandlers();
        
        // for each handler add data handlers
        for(QPSubscriptionHandler currentHandler: handlers)
        {
            currentHandler.getSubcriptionsDataFetchers(context);
        }

        return allDataFetchers;
    }

    protected QPSubscriptionFeed subscription = null;
    //protected Map<String, QPSubscriptionFeed> subscriptions = new ConcurrentHashMap<>();
    protected Map<String, JsonObject> requestParamsForSubscriptions = new HashMap<>();
    Logger socketLogger = CPTALogger.getLogger();    
    protected Session socketSession;    
}