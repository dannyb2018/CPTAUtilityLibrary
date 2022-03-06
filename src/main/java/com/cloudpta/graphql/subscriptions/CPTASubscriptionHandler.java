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


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.cloudpta.utilites.exceptions.CPTAException;
import graphql.GraphQLContext;
import graphql.schema.DataFetcher;
import graphql.schema.TypeResolver;
import graphql.schema.idl.RuntimeWiring.Builder;

public abstract class CPTASubscriptionHandler 
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
