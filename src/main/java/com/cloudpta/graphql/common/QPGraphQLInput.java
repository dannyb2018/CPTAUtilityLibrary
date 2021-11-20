package com.cloudpta.graphql.common;

import java.util.Map;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;

public abstract class QPGraphQLInput 
{
    public void parseInput(DataFetchingEnvironment input)
    {
        // Get the context for input
        getContext(input);

        // get the arguments themselves
        Map<String, Object> argumentsAsMap = input.getArguments();
        parseArguments(argumentsAsMap);
    }    

    public GraphQLContext getInputContext()
    {
        return inputContext;
    }

    protected void getContext(DataFetchingEnvironment input)
    {
        inputContext = input.getContext();
    }

    protected abstract void parseArguments(Map<String, Object> argumentsAsMap);

    protected GraphQLContext inputContext = null;
}
