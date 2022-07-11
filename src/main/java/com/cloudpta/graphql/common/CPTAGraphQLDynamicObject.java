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

import java.util.List;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.idl.TypeDefinitionRegistry;

public abstract class CPTAGraphQLDynamicObject 
{
    public void addToTypeRegistry(TypeDefinitionRegistry apiTypeDefinitionRegistry)
    {
        // create the object type definition for this object
        // Get the name of the object
        String objectName = getClass().getSimpleName();
        GraphQLObjectType.Builder dynamicObjectTypeDefinitionBuilder = GraphQLObjectType.newObject().name(objectName);
        // add description
        String description = getDescription();
        dynamicObjectTypeDefinitionBuilder.description(description);
        // If there is a non-null interface
        GraphQLInterfaceType possibleInterface = getInterface(apiTypeDefinitionRegistry);
        if(null != possibleInterface)
        {
            dynamicObjectTypeDefinitionBuilder.withInterface(possibleInterface);
        }

        // Add fields
        List<GraphQLFieldDefinition> fieldDefinitions = getFieldDefinitions();
        dynamicObjectTypeDefinitionBuilder.fields(fieldDefinitions);

        // add to api type registry
        GraphQLObjectType typeDefinitionOfThisObject = dynamicObjectTypeDefinitionBuilder.build();
        apiTypeDefinitionRegistry.add(typeDefinitionOfThisObject.getDefinition()); 
    }
    
    protected abstract List<GraphQLFieldDefinition> getFieldDefinitions();
    protected abstract String getDescription();
    protected abstract GraphQLInterfaceType getInterface(TypeDefinitionRegistry apiTypeDefinitionRegistry);
}
