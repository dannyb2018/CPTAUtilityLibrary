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


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cloudpta.utilites.exceptions.CPTAException;

import graphql.GraphQLContext;
import graphql.schema.DataFetcher;
import graphql.schema.TypeResolver;
import graphql.schema.idl.RuntimeWiring.Builder;

public abstract class QPSubscriptionHandler 
{
    public void addTypeResolversForSubscriptions(Builder wiringBuilder, GraphQLContext context) throws CPTAException
    {
        // Get list of type names
        List<String> typeNames = getNamesOfSubscriptionTypesNeedingResolving(context);
        
        // Add the type resolvers for each name
        for(String currentTypeName : typeNames)
        {
            // Get the type resolver for this name
            TypeResolver resolverForThisType = getResolverForThisType(currentTypeName, context);
            // add it to the wiring
            wiringBuilder.type(currentTypeName, typeWriting -> typeWriting.typeResolver(resolverForThisType)); 
        }
    }

    protected abstract List<String> getNamesOfSubscriptionTypesNeedingResolving(GraphQLContext context) throws CPTAException;
    protected abstract TypeResolver getResolverForThisType(String typeName, GraphQLContext context) throws CPTAException;


    public Map<String, DataFetcher> getSubcriptionsDataFetchers(GraphQLContext context) throws CPTAException
    {
        Map<String, DataFetcher> allDataFetchersForSubscriptions = new ConcurrentHashMap<>();

        // Get list of subscriptions
        List<String> subscriptionNames = getNamesOfSubscriptionsHandled(context);
        // Add the data fetcher for each subscription
        for(String currentSubscriptionName: subscriptionNames)
        {
            addDataFetcherForSubscription(currentSubscriptionName, allDataFetchersForSubscriptions, context);
        }

        return allDataFetchersForSubscriptions;
    }

    protected abstract void addDataFetcherForSubscription(String queryName, Map<String, DataFetcher> queryDataFetcher, GraphQLContext context) throws CPTAException;
    protected abstract List<String> getNamesOfSubscriptionsHandled(GraphQLContext context) throws CPTAException;
}
