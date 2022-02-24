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
package com.cloudpta.graphql.common;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import graphql.language.Description;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;

public class CPTAGraphQLDynamicEnumFactory <A extends CPTAGraphQLDynamicEnum<A>>
{
    public CPTAGraphQLDynamicEnumFactory
                                       (
                                       Class<A> type,
                                       String description, 
                                       List<String> values, 
                                       Map<String, String> valueDescriptions
                                       ) 
                                       throws 
                                       NoSuchMethodException, 
                                       SecurityException, 
                                       InstantiationException, 
                                       IllegalAccessException, 
                                       IllegalArgumentException, 
                                       InvocationTargetException
    {
        ConcurrentHashMap<Class<?>, CPTAGraphQLDynamicEnumFactory<?>> allFactories = CPTAGraphQLDynamicEnum.allFactories;
 
        // Check if it is already defined
        if (null != allFactories.get(type))
        {
            String errorMessage = "you must instantiate only one factory per dynamic enum type. Duplicate factory instantiated for " + type;
            IllegalStateException duplicatedEnumException = new IllegalStateException(errorMessage);
            throw duplicatedEnumException;
        }

        // Initialise array of values
        int numberOfValues = values.size();
        enumValues = (A[])Array.newInstance(type, values.size());

        // create the enum type definition for this enum
        EnumTypeDefinition.Builder dynamicEnumTypeDefinitionBuilder = EnumTypeDefinition.newEnumTypeDefinition();
        // start by creating list of enum definitions
        List<EnumValueDefinition> dynamicEnumTypeValueDefinitions = new ArrayList<>();
        for(int i = 0; i < numberOfValues;  i++)
        {
            // Get current value
            String currentValue = values.get(i);

            // build the current enum value
            EnumValueDefinition.Builder currentValueBuilder = EnumValueDefinition.newEnumValueDefinition().name(currentValue);
            // if there is a description add it
            String currentValueDescriptionAsString = valueDescriptions.get(currentValue);
            if(null != currentValueDescriptionAsString)
            {
                Description currentValueDescription = new Description(currentValueDescriptionAsString, null, false);
                currentValueBuilder = currentValueBuilder.description(currentValueDescription);
            }

            // Add to list
            EnumValueDefinition currentValueDefinition = currentValueBuilder.build();
            dynamicEnumTypeValueDefinitions.add(currentValueDefinition);

            // Add to enum values too
            Class<?>[] parameterList = new Class<?>[]{Integer.TYPE, String.class};
            Constructor<A> constructor = type.getDeclaredConstructor(parameterList);
            enumValues[i] = constructor.newInstance(new Object[]{i, currentValue});
        }

        // Create the the enum type definition description if there is one
        if(null != description)
        {
            Description dynamicEnumTypeDescription = new Description(description, null, false);
            dynamicEnumTypeDefinitionBuilder = dynamicEnumTypeDefinitionBuilder.description(dynamicEnumTypeDescription);
        }

        // Add values
        dynamicEnumTypeDefinitionBuilder.enumValueDefinitions(dynamicEnumTypeValueDefinitions);
        
        // Build the definition
        enumTypeDefinition = dynamicEnumTypeDefinitionBuilder.build();
        allFactories.put(type, this);

        typeName = type.getName();
    }

    public A valueOf(String enumAsString)
    {
        A desiredValue = null;

        // Search the list of values looking for one that matches
        int numberOfValues = Array.getLength(enumValues);
        for(int i = 0; i < numberOfValues; i++)
        {
            // Look at this value
            A valueToCheck = enumValues[i];
            // If it is the right one
            if(0 == enumAsString.compareTo(valueToCheck.name()))
            {
                // set it and no need to look further
                desiredValue = valueToCheck;

                break;
            }
        }

        return desiredValue;
    }

    public A[] values()
    {
        // hand over list
        return enumValues;
    }

    public static <B extends CPTAGraphQLDynamicEnum<B>> CPTAGraphQLDynamicEnum<B> valueOf(Class<B> enumType, String enumAsString)
    {
        // Get the factory
        CPTAGraphQLDynamicEnumFactory<B> factory = getInstanceByClass(enumType);
        // Get the value
        B value = factory.valueOf(enumAsString);

        return value;
    }

    public static <B extends CPTAGraphQLDynamicEnum<B>> CPTAGraphQLDynamicEnumFactory<B> getInstanceByClass(Class<B> enumType)
    {
        CPTAGraphQLDynamicEnumFactory<B> f = (CPTAGraphQLDynamicEnumFactory<B>) CPTAGraphQLDynamicEnum.allFactories.get(enumType);

        if (f == null)
        {
            try
            {
                Class.forName(enumType.getName());
            }
            catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }

            throw new IllegalStateException("no factory registered for " + enumType.getClass());
        }

        return f;

    }

    public void addToTypeRegistry(TypeDefinitionRegistry apiTypeDefinitionRegistry)
    {
        apiTypeDefinitionRegistry.add(enumTypeDefinition); 
    }

    protected final EnumTypeDefinition enumTypeDefinition;
    protected final String typeName;
    protected final A[] enumValues;

   // protected static final ConcurrentHashMap<String, CPTAGraphQLDynamicEnumFactory<?>> allFactories = new ConcurrentHashMap<>();
}