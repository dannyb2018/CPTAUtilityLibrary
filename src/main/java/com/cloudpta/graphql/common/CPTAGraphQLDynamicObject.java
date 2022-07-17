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

import java.util.ArrayList;
import java.util.List;
import graphql.language.Description;
import graphql.language.FieldDefinition;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeName;
import graphql.language.ObjectTypeDefinition.Builder;
import graphql.schema.idl.TypeDefinitionRegistry;

public abstract class CPTAGraphQLDynamicObject 
{
    public void addToTypeRegistry(TypeDefinitionRegistry apiTypeDefinitionRegistry)
    {
        // create the object type definition for this object
        // Get the name of the object
        String objectName = getGraphQLName();
        Builder objectDefinitionBuilder = ObjectTypeDefinition.newObjectTypeDefinition();
        objectDefinitionBuilder.name(objectName);
        // add description
        Description description = new Description(descriptionContent, null, false);
        objectDefinitionBuilder.description(description);
        // If there is a non-null interface
        String possibleInterfaceName = getInterfaceName();
        if(null != possibleInterfaceName)
        {
            // Get the interface type
            TypeName.Builder interfaceTypeBuilder = TypeName.newTypeName(possibleInterfaceName);
            TypeName possibleInterface = interfaceTypeBuilder.build();            
            // This object will implement this interface
            objectDefinitionBuilder.implementz(possibleInterface);
            
            // now need to get interface fields that need to be implemented
            InterfaceTypeDefinition instrumentInterfaceTypeDefinition = (InterfaceTypeDefinition)apiTypeDefinitionRegistry.getType(possibleInterfaceName).get();
            // Get the fields implemented by this instrument
            List<FieldDefinition> fieldsForThisInterface = instrumentInterfaceTypeDefinition.getFieldDefinitions();
            // If these fields are different from what we already have
            if(fieldsForThisInterface.size() != interfaceFields.size())
            {
                // replace them
                interfaceFields = fieldsForThisInterface;
            }
        }

        // Add fields
        List<FieldDefinition> fieldDefinitions = getFieldDefinitions();
        objectDefinitionBuilder.fieldDefinitions(fieldDefinitions);

        // add to api type registry
        ObjectTypeDefinition typeDefinitionOfThisObject = objectDefinitionBuilder.build();
        apiTypeDefinitionRegistry.add(typeDefinitionOfThisObject); 
    }
    
    protected List<FieldDefinition> getFieldDefinitions()
    {
        List<FieldDefinition> fields = new ArrayList<>();

        // Add interface fields
        fields.addAll(interfaceFields);

        // Let inherited class add extra fields
        addNonInterfaceFields(fields);
 
        return fields;
    }   

    protected abstract String getInterfaceName();
    protected abstract void addNonInterfaceFields(List<FieldDefinition> fields);
    public abstract String getGraphQLName();

    protected String descriptionContent;
    protected List<FieldDefinition> interfaceFields = new ArrayList<>();
}
