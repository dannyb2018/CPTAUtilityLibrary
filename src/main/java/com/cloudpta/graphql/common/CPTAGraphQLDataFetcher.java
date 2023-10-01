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

import com.cloudpta.utilites.exceptions.CPTAException;
import graphql.GraphQLContext;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;

public abstract class CPTAGraphQLDataFetcher<ReturnType, InputType extends CPTAGraphQLInput> implements DataFetcher<ReturnType> 
{
    @Override
    public ReturnType get(DataFetchingEnvironment env) throws Exception 
    {
        // store environment in case it is needed
        environment = env;
        
        // get a new input
        InputType input = newInput();
        // parse the arguments
        input.parseInput(env);
        // get data
        ReturnType data = getData(input);

        // set modified schemas
        if(null != modifiedMutationSchema)
        {
            GraphQLContext contextForFetching = env.getLocalContext();
            contextForFetching.put(CPTAGraphQLAPIConstants.MODIFIED_MUTATION_SCHEMA, modifiedMutationSchema);
        }
        if(null != modifiedQuerySchema)
        {
            GraphQLContext contextForFetching = env.getLocalContext();
            contextForFetching.put(CPTAGraphQLAPIConstants.MODIFIED_QUERY_SCHEMA, modifiedQuerySchema);
        }

        return data;
    }

    public abstract InputType newInput();
    public abstract ReturnType getData(InputType input) throws CPTAException;

    protected void modifyMutationSchema(GraphQLSchema newMutationSchema)
    {
        modifiedMutationSchema = newMutationSchema;
    }

    protected GraphQLSchema getCurrentMutationSchema()
    {
        GraphQLContext contextForFetching = environment.getLocalContext();
        return (GraphQLSchema)contextForFetching.get(CPTAGraphQLAPIConstants.CURRENT_MUTATION_SCHEMA);
    }

    protected GraphQLSchema getCurrentQuerySchema()
    {
        GraphQLContext contextForFetching = environment.getLocalContext();
        return (GraphQLSchema)contextForFetching.get(CPTAGraphQLAPIConstants.CURRENT_QUERY_SCHEMA);
    }

    protected void modifyQuerySchema(GraphQLSchema newQuerySchema)
    {
        modifiedQuerySchema = newQuerySchema;
    }

    protected GraphQLSchema modifiedMutationSchema = null;
    protected GraphQLSchema modifiedQuerySchema = null;
    protected DataFetchingEnvironment environment;
}
