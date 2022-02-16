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
package com.cloudpta.graphql.query_mutation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.cloudpta.utilites.exceptions.CPTAException;
import com.cloudpta.utilites.logging.CPTALogger;
import com.cloudpta.graphql.common.QPGraphQLAPIConstants;
import com.cloudpta.graphql.common.QPQueryVariablesParser;
import ch.qos.logback.classic.Logger;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLContext;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyNamingStrategy;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

public abstract class QPGraphQLQueryMutationEndpoint extends HttpServlet  
{
    public Response handleGraphQLQuery
                                     (
                                     @Context HttpServletRequest httpRequest,
                                     String queryAsString
                                     )
    {
        Response response = null;
        try
        {
            // Parse the query
            JsonReader reader = Json.createReader(new StringReader(queryAsString));
            JsonObject queryObject = reader.readObject();             
            // Get operation name
            String operationName = null;
            if(false == queryObject.isNull(QPGraphQLAPIConstants.OPERATION_NAME))
            {
                operationName = queryObject.getString(QPGraphQLAPIConstants.OPERATION_NAME);
            }
            // get the query field
            String graphQLQuery = queryObject.getString(QPGraphQLAPIConstants.OPERATION_TEXT);
            // Need to turn variables into a map of keys and values
            JsonObject variablesAsJsonObject = queryObject.getJsonObject(QPGraphQLAPIConstants.OPERATION_VARIABLES);
            Map<String, Object> variables = QPQueryVariablesParser.parseVariables(variablesAsJsonObject);


            // Build the context for the query
            GraphQLContext contextForQuery = GraphQLContext.newContext().build();
            Map<String, String> socketRequestDetails = convertRequestToMap(httpRequest);
            initialiseContext(contextForQuery, socketRequestDetails, operationName, graphQLQuery, variablesAsJsonObject);
            
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
            Map<String, Object> spec = executionResult.toSpecification();

            // Convert that result into a json string
            JsonbConfig jsonbConfig = new JsonbConfig()
                                        .withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES)
                                        .withNullValues(true)
                                        .withFormatting(false);
            Jsonb converter = JsonbBuilder.create(jsonbConfig);
            String resultAsString = converter.toJson(spec);
            
            // Send it back
            response = Response.ok().entity(resultAsString).build();
        }
        catch(Exception E)
        {
            CPTAException wrappedException = new CPTAException(E);
            servletLogger.trace("Exception whilst handling graphQL request + " + queryAsString);
            JsonObject result = wrappedException.getErrors();
            String json = result.toString();
            servletLogger.error("Couldnt execute graphql, reason " + json);
            response = Response.serverError().entity(json).build();            
        }

        // Response is right
        return response;
    } 
    
    protected Map<String, String> convertRequestToMap(HttpServletRequest httpRequest)
    {
        Map<String, String> socketRequestDetails = new HashMap<>();

        // Add remote host
        String remoteHost = httpRequest.getRemoteHost();
        socketRequestDetails.put(QPGraphQLAPIConstants.CONTEXT_REMOTE_HOST, remoteHost);

        // Add all the headers
        Iterator<String> headerNames = httpRequest.getHeaderNames().asIterator();
        while(true == headerNames.hasNext())
        {
            String currentHeaderName = headerNames.next();
            String currentHeaderValue = httpRequest.getHeader(currentHeaderName);
            socketRequestDetails.put(currentHeaderName, currentHeaderValue);
        }

        // Add all the cookies
        Cookie[] cookies = httpRequest.getCookies();
        int numberOfCookies = Array.getLength(cookies);
        for(int i = 0; i < numberOfCookies; i++)
        {
            String currentCookieName = cookies[i].getName();
            String currentCookieValue = cookies[i].getValue();
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
            // Get operation type
            String typeOfOperation = ((String)context.get(QPGraphQLAPIConstants.CONTEXT_OPERATION_REQUEST)).trim();
            // If it is a mutation
            if(true == typeOfOperation.startsWith(QPGraphQLAPIConstants.OPERATION_MUTATION_TYPE))
            {
                // set up mutation build if needed
                operationBuild = getMutationBuild(mergedTypeRegistry, context);
            }
            // otherwise is a query 
            else
            {
                // set up a query if needed
                operationBuild = getQueryBuild(mergedTypeRegistry, context);
            }

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
                InputStream mutationsSchemaStream = getMutationSchemaStream(context);
                InputStream subscriptionsSchemaStream = getSubscriptionSchemaStream(context);
                InputStream queriesSchemaStream = getQueriesSchemaStream(context);

                // Start with the types schema
                BufferedReader schemaStream = new BufferedReader(new InputStreamReader(typesSchemaStream, StandardCharsets.UTF_8));
                String apiTypesSchema = schemaStream.lines().collect(Collectors.joining("\n"));
                // Then with queries schema
                schemaStream = new BufferedReader(new InputStreamReader(queriesSchemaStream, StandardCharsets.UTF_8));
                String queriesSchema = schemaStream.lines().collect(Collectors.joining("\n"));
                // Then with mutations schema
                schemaStream = new BufferedReader(new InputStreamReader(mutationsSchemaStream, StandardCharsets.UTF_8));
                String mutationsSchema = schemaStream.lines().collect(Collectors.joining("\n"));
                // Then with subscriptions schema
                schemaStream = new BufferedReader(new InputStreamReader(subscriptionsSchemaStream, StandardCharsets.UTF_8));
                String subscriptionsSchema = schemaStream.lines().collect(Collectors.joining("\n"));
                // Finally schema to bring it all together
                String holderSchema = QPGraphQLAPIConstants.MUTATION_QUERY_HOLDER_SCHEMA;

                // Parse these schemas
                SchemaParser schemaParser = new SchemaParser();
                TypeDefinitionRegistry apiTypeDefinitionRegistry = schemaParser.parse(apiTypesSchema);
                TypeDefinitionRegistry mutationTypeDefinitionRegistry = schemaParser.parse(mutationsSchema);
                TypeDefinitionRegistry queryTypeDefinitionRegistry = schemaParser.parse(queriesSchema);
                TypeDefinitionRegistry subscriptionTypeDefinitionRegistry = schemaParser.parse(subscriptionsSchema);
                TypeDefinitionRegistry schemaTypeDefinitionRegistry = schemaParser.parse(holderSchema);

                // Merge them
                mergedTypeDefinitionRegistry = new TypeDefinitionRegistry();
                mergedTypeDefinitionRegistry.merge(apiTypeDefinitionRegistry);
                mergedTypeDefinitionRegistry.merge(mutationTypeDefinitionRegistry);
                mergedTypeDefinitionRegistry.merge(queryTypeDefinitionRegistry);
                mergedTypeDefinitionRegistry.merge(subscriptionTypeDefinitionRegistry);
                mergedTypeDefinitionRegistry.merge(schemaTypeDefinitionRegistry);              
        }

        return mergedTypeDefinitionRegistry;
    }

    protected GraphQL getMutationBuild(TypeDefinitionRegistry typeRegistry, GraphQLContext context) throws CPTAException
    {
        // Get the mutation build
        GraphQL mutationBuild = getExistingMutationBuild(typeRegistry,context);
        // If we havent already set up graphQL
        if(null == mutationBuild)
        {
            try
            {
                // Build the executable schema for mutations
                // Add data fetchers
                Map<String, DataFetcher> fetcherForThisBuild = getDataFetchersForMutation(context);
                graphql.schema.idl.RuntimeWiring.Builder runtimeWiringBuilder= 
                RuntimeWiring.newRuntimeWiring()
                .type
                (
                    TypeRuntimeWiring.newTypeWiring(QPGraphQLAPIConstants.WIRING_MUTATION_TYPE)
                    .dataFetchers(fetcherForThisBuild)
                );
                // Add type resolvers
                addTypeResolversForMutation(runtimeWiringBuilder, context);

                // Generate schema to be made executable           
                RuntimeWiring runtimeWiring = runtimeWiringBuilder.build();
                SchemaGenerator schemaGenerator = new SchemaGenerator();
                GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);

                mutationBuild = GraphQL.newGraphQL(graphQLSchema).build();   
            }
            catch(Exception E)
            {
                CPTAException wrappedException = new CPTAException(E);
                throw wrappedException;
            }
        }  
        
        return mutationBuild;
    }

    protected GraphQL getQueryBuild(TypeDefinitionRegistry typeRegistry, GraphQLContext context) throws CPTAException
    {
        // Get the query build
        GraphQL queryBuild = getExistingQueryBuild(typeRegistry,context);
        // If we havent already set up graphQL
        if(null == queryBuild)
        {
            try
            {
                // Build the executable schema for queries
                // Add data fetchers
                Map<String, DataFetcher> fetcherForThisBuild = getDataFetchersForQuery(context);
                graphql.schema.idl.RuntimeWiring.Builder runtimeWiringBuilder= 
                RuntimeWiring.newRuntimeWiring()
                .type
                (
                    TypeRuntimeWiring.newTypeWiring(QPGraphQLAPIConstants.WIRING_QUERY_TYPE)
                    .dataFetchers(fetcherForThisBuild)
                );
                // Add type resolvers
                addTypeResolversForQuery(runtimeWiringBuilder, context);

                // Generate schema to be made executable           
                RuntimeWiring runtimeWiring = runtimeWiringBuilder.build();
                SchemaGenerator schemaGenerator = new SchemaGenerator();
                GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);

                queryBuild = GraphQL.newGraphQL(graphQLSchema).build();   
            }
            catch(Exception E)
            {
                CPTAException wrappedException = new CPTAException(E);
                throw wrappedException;
            }
        }  
        
        return queryBuild;
    }

    protected abstract List<QPQueryMutationHandler> getHandlers();
    
    // Get type resolvers and data fetchers
    protected void addTypeResolversForMutation(graphql.schema.idl.RuntimeWiring.Builder mutationRuntimeWiringBuilder, GraphQLContext context) throws CPTAException
    {
        // go through the handlers
        List<QPQueryMutationHandler> handlers = getHandlers();

        for(QPQueryMutationHandler currentHandler:handlers)
        {
            currentHandler.addTypeResolversForMutations(mutationRuntimeWiringBuilder, context);
        }
    }

    protected Map<String, DataFetcher> getDataFetchersForMutation(GraphQLContext context) throws CPTAException
    {
        Map<String, DataFetcher> allDataFetchersForMutation = new ConcurrentHashMap<>();

        // go through the handlers
        List<QPQueryMutationHandler> handlers = getHandlers();

        for(QPQueryMutationHandler currentHandler:handlers)
        {
            Map<String, DataFetcher> dataFetchersForThisHandler = currentHandler.getMutationsDataFetchers(context);
            allDataFetchersForMutation.putAll(dataFetchersForThisHandler);
        }

        return allDataFetchersForMutation;
    }

    protected void addTypeResolversForQuery(graphql.schema.idl.RuntimeWiring.Builder queryRuntimeWiringBuilder, GraphQLContext context) throws CPTAException
    {
        // go through the handlers
        List<QPQueryMutationHandler> handlers = getHandlers();

        for(QPQueryMutationHandler currentHandler:handlers)
        {
            currentHandler.addTypeResolversForQueries(queryRuntimeWiringBuilder, context);
        }
    }

    protected Map<String, DataFetcher> getDataFetchersForQuery(GraphQLContext context) throws CPTAException
    {
        Map<String, DataFetcher> allDataFetchersForQuery = new ConcurrentHashMap<>();

        // go through the handlers
        List<QPQueryMutationHandler> handlers = getHandlers();

        for(QPQueryMutationHandler currentHandler:handlers)
        {
            Map<String, DataFetcher> dataFetchersForThisHandler = currentHandler.getQueriesDataFetchers(context);
            allDataFetchersForQuery.putAll(dataFetchersForThisHandler);
        }

        return allDataFetchersForQuery;

    }

    // This should return any existing graph ql build or null if there is not one
    protected abstract GraphQL getExistingGraphQL(GraphQLContext context) throws CPTAException;
    protected abstract TypeDefinitionRegistry getExistingTypeDefinitionRegistry(GraphQLContext context) throws CPTAException;
    protected abstract GraphQL getExistingMutationBuild(TypeDefinitionRegistry typeRegistry, GraphQLContext context) throws CPTAException;
    protected abstract GraphQL getExistingQueryBuild(TypeDefinitionRegistry typeRegistry, GraphQLContext context) throws CPTAException;

    // These are the inputs to the various schemas
    protected abstract InputStream getTypesSchemaStream(GraphQLContext context) throws CPTAException;
    protected abstract InputStream getMutationSchemaStream(GraphQLContext context) throws CPTAException;
    protected abstract InputStream getSubscriptionSchemaStream(GraphQLContext context) throws CPTAException;
    protected abstract InputStream getQueriesSchemaStream(GraphQLContext context) throws CPTAException;

    Logger servletLogger = CPTALogger.getLogger();   
}