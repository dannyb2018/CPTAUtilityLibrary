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
import com.cloudpta.graphql.common.CPTAGraphQLAPIConstants;
import com.cloudpta.graphql.common.CPTAGraphQLHandler;
import com.cloudpta.graphql.common.CPTAGraphQLQueryType;
import com.cloudpta.graphql.common.CPTAQueryVariablesParser;
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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public abstract class CPTAGraphQLQueryMutationEndpoint extends HttpServlet  
{
    protected void addCustomTypeDefinitionsToRegistry(GraphQLContext context, TypeDefinitionRegistry mergedTypeDefinitionRegistry)
    {
        // Default is that there are no custom type definitions so do nothing
    }

    // This should return any existing graph ql build or null if there is not one
    protected abstract GraphQL getExistingGraphQL(GraphQLContext context) throws CPTAException;
    protected abstract TypeDefinitionRegistry getExistingTypeDefinitionRegistry(GraphQLContext context) throws CPTAException;
    protected abstract GraphQL getExistingMutationBuild(GraphQLContext context, TypeDefinitionRegistry typeRegistry) throws CPTAException;
    protected abstract GraphQL getExistingQueryBuild(GraphQLContext context, TypeDefinitionRegistry typeRegistry) throws CPTAException;
    // Store the existing builds after made
    protected abstract void setExistingTypeDefinitionRegistry(GraphQLContext context, TypeDefinitionRegistry typeRegistry) throws CPTAException;
    protected abstract void setExistingMutationBuild(GraphQLContext context, GraphQL mutationBuild) throws CPTAException;
    protected abstract void setExistingQueryBuild(GraphQLContext context, GraphQL queryBuild) throws CPTAException;

    // These are the inputs to the various schemas
    protected abstract InputStream getTypesSchemaStream(GraphQLContext context) throws CPTAException;
    protected abstract InputStream getMutationSchemaStream(GraphQLContext context) throws CPTAException;
    protected abstract InputStream getQueriesSchemaStream(GraphQLContext context) throws CPTAException;
    protected abstract InputStream getSubscriptionsSchemaStream(GraphQLContext context) throws CPTAException;

    @Path("/")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleGraphQLQuery
                                     (
                                     @Context HttpServletRequest httpRequest,
                                     String queryAsString
                                     )
    {
        Response response = null;
        try
        {
            // Log query
            servletLogger.info("received query: " + queryAsString);

            // Parse the query
            JsonReader reader = Json.createReader(new StringReader(queryAsString));
            JsonObject queryObject = reader.readObject();             
            // Get operation name
            String operationName = null;
            if(null != queryObject.get(CPTAGraphQLAPIConstants.OPERATION_NAME))
            {
                operationName = queryObject.getString(CPTAGraphQLAPIConstants.OPERATION_NAME);
            }
            // get the query field
            String graphQLQuery = queryObject.getString(CPTAGraphQLAPIConstants.OPERATION_TEXT);
            // Need to turn variables into a map of keys and values
            JsonObject variablesAsJsonObject = queryObject.getJsonObject(CPTAGraphQLAPIConstants.OPERATION_VARIABLES);
            Map<String, Object> variables = CPTAQueryVariablesParser.parseVariables(variablesAsJsonObject);


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
            // Log query
            servletLogger.info("sent response : " + resultAsString + " to request: " + queryAsString);
            
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
        socketRequestDetails.put(CPTAGraphQLAPIConstants.CONTEXT_REMOTE_HOST, remoteHost);

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
        if(null != cookies)
        {
            int numberOfCookies = Array.getLength(cookies);
            for(int i = 0; i < numberOfCookies; i++)
            {
                String currentCookieName = cookies[i].getName();
                String currentCookieValue = cookies[i].getValue();
                socketRequestDetails.put(currentCookieName, currentCookieValue);
            }
        }   

        return socketRequestDetails;
    }

    protected void initialiseContext(GraphQLContext contextToInitialise, Map<String, String> socketRequestDetails, String operationName, String operationAsString, JsonObject variablesAsJsonObject)
    {
        // Default just saves the operation name, operation as string, request and variables
        if(null != operationName)
        {
            contextToInitialise.put(CPTAGraphQLAPIConstants.CONTEXT_OPERATION_NAME, operationName);
        }
        contextToInitialise.put(CPTAGraphQLAPIConstants.CONTEXT_OPERATION_REQUEST, operationAsString);
        contextToInitialise.put(CPTAGraphQLAPIConstants.CONTEXT_OPERATION_VARIABLES, variablesAsJsonObject);

        // Add all the socket request details
        Set<String> socketRequestPropertyNames = socketRequestDetails.keySet();
        for(String currentSocketRequestPropertyName : socketRequestPropertyNames)
        {
            String currentSocketRequestPropertyValue = socketRequestDetails.get(currentSocketRequestPropertyName);
            contextToInitialise.put(currentSocketRequestPropertyName, currentSocketRequestPropertyValue);
        }
        
        // But this is where you set up the context for the query like any DB or topics 
    }

    protected synchronized GraphQL getGraphQL(GraphQLContext context) throws CPTAException
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
            String typeOfOperation = ((String)context.get(CPTAGraphQLAPIConstants.CONTEXT_OPERATION_REQUEST)).trim();
            // If it is a mutation
            if(true == typeOfOperation.startsWith(CPTAGraphQLAPIConstants.OPERATION_MUTATION_TYPE))
            {
                // set up mutation build if needed
                operationBuild = getMutationBuild(mergedTypeRegistry, context);
                // Save it
                setExistingMutationBuild(context, operationBuild);            
            }
            // otherwise is a query 
            else
            {
                // set up a query if needed
                operationBuild = getQueryBuild(mergedTypeRegistry, context);
                // Save it
                setExistingQueryBuild(context, operationBuild);
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
                InputStream queriesSchemaStream = getQueriesSchemaStream(context);
                InputStream subscriptionsSchemaStream = getSubscriptionsSchemaStream(context);

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
                String holderSchema = CPTAGraphQLAPIConstants.MUTATION_QUERY_HOLDER_SCHEMA;

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

                // Add custom type definitions
                addCustomTypeDefinitionsToRegistry(context, mergedTypeDefinitionRegistry);

                // Save the type registry
                setExistingTypeDefinitionRegistry(context, mergedTypeDefinitionRegistry); 
        }

        return mergedTypeDefinitionRegistry;
    }

    protected GraphQL getMutationBuild(TypeDefinitionRegistry typeRegistry, GraphQLContext context) throws CPTAException
    {
        // Get the mutation build
        GraphQL mutationBuild = getExistingMutationBuild(context, typeRegistry);
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
                    TypeRuntimeWiring.newTypeWiring(CPTAGraphQLAPIConstants.WIRING_MUTATION_TYPE)
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
        GraphQL queryBuild = getExistingQueryBuild(context, typeRegistry);
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
                    TypeRuntimeWiring.newTypeWiring(CPTAGraphQLAPIConstants.WIRING_QUERY_TYPE)
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

    protected abstract List<CPTAGraphQLHandler> getHandlers(GraphQLContext context);
    
    // Get type resolvers and data fetchers
    protected void addTypeResolversForMutation(graphql.schema.idl.RuntimeWiring.Builder mutationRuntimeWiringBuilder, GraphQLContext context) throws CPTAException
    {
        // go through the handlers
        List<CPTAGraphQLHandler> handlers = getHandlers(context);

        for(CPTAGraphQLHandler currentHandler:handlers)
        {
            currentHandler.addTypeResolversForQueryType(CPTAGraphQLQueryType.MUTATION, mutationRuntimeWiringBuilder, context);
        }
    }

    protected Map<String, DataFetcher> getDataFetchersForMutation(GraphQLContext context) throws CPTAException
    {
        Map<String, DataFetcher> allDataFetchersForMutation = new ConcurrentHashMap<>();

        // go through the handlers
        List<CPTAGraphQLHandler> handlers = getHandlers(context);

        for(CPTAGraphQLHandler currentHandler:handlers)
        {
            Map<String, DataFetcher> dataFetchersForThisHandler = currentHandler.getDataFetchersForQueryType(CPTAGraphQLQueryType.MUTATION, context);
            allDataFetchersForMutation.putAll(dataFetchersForThisHandler);
        }

        return allDataFetchersForMutation;
    }

    protected void addTypeResolversForQuery(graphql.schema.idl.RuntimeWiring.Builder queryRuntimeWiringBuilder, GraphQLContext context) throws CPTAException
    {
        // go through the handlers
        List<CPTAGraphQLHandler> handlers = getHandlers(context);

        for(CPTAGraphQLHandler currentHandler:handlers)
        {
            currentHandler.addTypeResolversForQueryType(CPTAGraphQLQueryType.QUERY, queryRuntimeWiringBuilder, context);
        }
    }

    protected Map<String, DataFetcher> getDataFetchersForQuery(GraphQLContext context) throws CPTAException
    {
        Map<String, DataFetcher> allDataFetchersForQuery = new ConcurrentHashMap<>();

        // go through the handlers
        List<CPTAGraphQLHandler> handlers = getHandlers(context);

        for(CPTAGraphQLHandler currentHandler:handlers)
        {
            Map<String, DataFetcher> dataFetchersForThisHandler = currentHandler.getDataFetchersForQueryType(CPTAGraphQLQueryType.QUERY, context);
            allDataFetchersForQuery.putAll(dataFetchersForThisHandler);
        }

        return allDataFetchersForQuery;

    }

    Logger servletLogger = CPTALogger.getLogger();   
}
