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
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

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

        return data;
    }

    public abstract InputType newInput();
    public abstract ReturnType getData(InputType input) throws CPTAException;

    protected DataFetchingEnvironment environment;
}
