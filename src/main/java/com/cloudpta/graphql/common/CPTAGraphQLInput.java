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
import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;

public abstract class CPTAGraphQLInput 
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
