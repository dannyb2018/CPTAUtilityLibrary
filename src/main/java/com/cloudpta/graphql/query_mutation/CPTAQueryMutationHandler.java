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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.cloudpta.graphql.common.CPTAGraphQLDynamicEnumFactory;
import com.cloudpta.utilites.exceptions.CPTAException;
import graphql.GraphQLContext;
import graphql.schema.DataFetcher;
import graphql.schema.TypeResolver;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.RuntimeWiring.Builder;


public abstract class CPTAQueryMutationHandler
{
    protected abstract List<CPTAGraphQLDynamicEnumFactory<?>> getDynamicEnumTypeFactories(GraphQLContext context) throws CPTAException;
    protected abstract List<String> getNamesOfQueryTypesNeedingResolving(GraphQLContext context) throws CPTAException;
    protected abstract List<String> getNamesOfMutationTypesNeedingResolving(GraphQLContext context) throws CPTAException;
    protected abstract TypeResolver getResolverForThisType(String typeName, GraphQLContext context) throws CPTAException;
    protected abstract void addDataFetcherForMutation(String mutationName, Map<String, DataFetcher> mutationDataFetcher, GraphQLContext context) throws CPTAException;
    protected abstract void addDataFetcherForQuery(String queryName, Map<String, DataFetcher> queryDataFetcher, GraphQLContext context) throws CPTAException;
    protected abstract List<String> getNamesOfMutationsHandled(GraphQLContext context) throws CPTAException;
    protected abstract List<String> getNamesOfQueriesHandled(GraphQLContext context) throws CPTAException;

    
    public void addDynamicTypes(GraphQLContext context, TypeDefinitionRegistry typeRegistry) throws CPTAException
    {
        // get the dynamic enums
        List<CPTAGraphQLDynamicEnumFactory<?>> enumFactories = getDynamicEnumTypeFactories(context);
        // Add the enums
        for(CPTAGraphQLDynamicEnumFactory<?> currentEnumFactory : enumFactories)
        {
            currentEnumFactory.addToTypeRegistry(typeRegistry);
        }
    }
    
    public void addTypeResolversForQueries(Builder wiringBuilder, GraphQLContext context) throws CPTAException
    {
        // Get list of type names
        List<String> typeNames = getNamesOfQueryTypesNeedingResolving(context);
        
        // Add the type resolvers for each name
        for(String currentTypeName : typeNames)
        {
            // Get the type resolver for this name
            TypeResolver resolverForThisType = getResolverForThisType(currentTypeName, context);
            // add it to the wiring
            wiringBuilder.type(currentTypeName, typeWriting -> typeWriting.typeResolver(resolverForThisType)); 
        }
    }


    public void addTypeResolversForMutations(Builder wiringBuilder, GraphQLContext context) throws CPTAException
    {
        // Get list of type names
        List<String> typeNames = getNamesOfMutationTypesNeedingResolving(context);
        
        // Add the type resolvers for each name
        for(String currentTypeName : typeNames)
        {
            // Get the type resolver for this name
            TypeResolver resolverForThisType = getResolverForThisType(currentTypeName, context);
            // add it to the wiring
            wiringBuilder.type(currentTypeName, typeWriting -> typeWriting.typeResolver(resolverForThisType)); 
        }
    }

    public Map<String, DataFetcher> getQueriesDataFetchers(GraphQLContext context) throws CPTAException
    {
        Map<String, DataFetcher> allDataFetchersForQueries = new ConcurrentHashMap<>();

        // Get list of queries
        List<String> queryNames = getNamesOfQueriesHandled(context);
        // Add the data fetcher for each query
        for(String currentQueryName: queryNames)
        {
            addDataFetcherForQuery(currentQueryName, allDataFetchersForQueries, context);
        }

        return allDataFetchersForQueries;
    }

    public Map<String, DataFetcher> getMutationsDataFetchers(GraphQLContext context) throws CPTAException
    {
        Map<String, DataFetcher> allDataFetchersForMutations = new ConcurrentHashMap<>();

        // Get list of mutations
        List<String> mutationNames = getNamesOfMutationsHandled(context);
        // Add the data fetcher for each mutation
        for(String currentMutationName: mutationNames)
        {
            addDataFetcherForMutation(currentMutationName, allDataFetchersForMutations, context);
        }

        return allDataFetchersForMutations;
    }

}