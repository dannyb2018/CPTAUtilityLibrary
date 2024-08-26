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

import java.util.Map;
import java.util.Set;

import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.RuntimeWiring.Builder;
import com.cloudpta.utilites.exceptions.CPTAException;
import graphql.GraphQLContext;
import graphql.schema.DataFetcher;
import graphql.schema.TypeResolver;

public abstract class CPTAGraphQLHandler 
{
    protected abstract Map<String, TypeResolver> getSubscriptionTypeResolvers(GraphQLContext context) throws CPTAException;
    protected abstract Map<String, TypeResolver> getMutationTypeResolvers(GraphQLContext context) throws CPTAException;
    protected abstract Map<String, TypeResolver> getQueryTypeResolvers(GraphQLContext context) throws CPTAException;
    protected abstract Map<String, DataFetcher<?>> getSubscriptionDataFetchers(GraphQLContext context) throws CPTAException;
    protected abstract Map<String, DataFetcher<?>> getMutationDataFetchers(GraphQLContext context) throws CPTAException;
    protected abstract Map<String, DataFetcher<?>> getQueryDataFetchers(GraphQLContext context) throws CPTAException;

    public void addTypeResolversForQueryType(CPTAGraphQLQueryType queryType, Builder wiringBuilder, GraphQLContext context) throws CPTAException
    {
        // Get types need resolving and their names
        Map<String, TypeResolver> typeResolversForQueryType = getResolvers(queryType, context);

        // Get list of type names
        Set<String> typeNames = typeResolversForQueryType.keySet();
        
        // Add the type resolvers for each name
        for(String currentTypeName : typeNames)
        {
            // Get the type resolver for this name
            TypeResolver resolverForThisType = typeResolversForQueryType.get(currentTypeName);
            // add it to the wiring
            wiringBuilder.type(currentTypeName, typeWriting -> typeWriting.typeResolver(resolverForThisType)); 
        }

    }

    public Map<String, DataFetcher<?>> getDataFetchersForQueryType(CPTAGraphQLQueryType queryType, GraphQLContext context) throws CPTAException
    {
        // If it is subscription
        if(CPTAGraphQLQueryType.SUBSCRIPTION == queryType)
        {
            return getSubscriptionDataFetchers(context);
        }
        else if(CPTAGraphQLQueryType.MUTATION == queryType)
        {
            return getMutationDataFetchers(context);
        }
        else
        {
            return getQueryDataFetchers(context);
        }
    }

    public void addCustomTypeDefinitionsToRegistry(CPTAGraphQLQueryType queryType, GraphQLContext context, TypeDefinitionRegistry mergedTypeDefinitionRegistry) throws CPTAException
    {
        // override this to add custom types
    }

    protected Map<String, TypeResolver> getResolvers(CPTAGraphQLQueryType queryType, GraphQLContext context) throws CPTAException
    {
        // If it is subscription
        if(CPTAGraphQLQueryType.SUBSCRIPTION == queryType)
        {
            return getSubscriptionTypeResolvers(context);
        }
        else if(CPTAGraphQLQueryType.MUTATION == queryType)
        {
            return getMutationTypeResolvers(context);
        }
        else
        {
            return getQueryTypeResolvers(context);
        }
    }
}
