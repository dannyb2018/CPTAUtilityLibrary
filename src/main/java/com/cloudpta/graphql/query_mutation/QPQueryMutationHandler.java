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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.cloudpta.utilites.exceptions.CPTAException;
import graphql.GraphQLContext;
import graphql.schema.DataFetcher;
import graphql.schema.TypeResolver;
import graphql.schema.idl.RuntimeWiring.Builder;


public abstract class QPQueryMutationHandler
{
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

    protected abstract List<String> getNamesOfQueryTypesNeedingResolving(GraphQLContext context) throws CPTAException;

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

    protected abstract List<String> getNamesOfMutationTypesNeedingResolving(GraphQLContext context) throws CPTAException;

    protected abstract TypeResolver getResolverForThisType(String typeName, GraphQLContext context) throws CPTAException;


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

    protected abstract void addDataFetcherForQuery(String queryName, Map<String, DataFetcher> queryDataFetcher, GraphQLContext context) throws CPTAException;
    protected abstract List<String> getNamesOfQueriesHandled(GraphQLContext context) throws CPTAException;


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

    protected abstract void addDataFetcherForMutation(String mutationName, Map<String, DataFetcher> mutationDataFetcher, GraphQLContext context) throws CPTAException;
    protected abstract List<String> getNamesOfMutationsHandled(GraphQLContext context) throws CPTAException;
}