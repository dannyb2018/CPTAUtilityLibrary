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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ch.qos.logback.classic.Logger;
import com.cloudpta.utilites.logging.CPTALogger;
import graphql.language.Description;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.schema.SchemaTransformer;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;

public class CPTAGraphQLDynamicEnumFactory <A extends CPTAGraphQLDynamicEnum<A>>
{
    public CPTAGraphQLDynamicEnumFactory
                                       (
                                       Class<A> type,
                                       String description, 
                                       List<A> values, 
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
            // remove old one
            allFactories.remove(type);
            
            logger.warn("updating enum " + type);
        }

        // Initialise array of values
        int numberOfValues = values.size();
        enumValues = (A[])Array.newInstance(type, values.size());

        // create the enum type definition for this enum
        // Get the name of the enum
        String enumName = type.getSimpleName();
        EnumTypeDefinition.Builder dynamicEnumTypeDefinitionBuilder = EnumTypeDefinition.newEnumTypeDefinition().name(enumName);
        // start by creating list of enum definitions
        List<EnumValueDefinition> dynamicEnumTypeValueDefinitions = new ArrayList<>();
        for(int i = 0; i < numberOfValues;  i++)
        {
            // Get current value
            A currentValue = values.get(i);

            // build the current enum value
            EnumValueDefinition.Builder currentValueBuilder = EnumValueDefinition.newEnumValueDefinition().name(currentValue.name());
            // if there is a description add it
            String currentValueDescriptionAsString = valueDescriptions.get(currentValue.name());
            if(null != currentValueDescriptionAsString)
            {
                Description currentValueDescription = new Description(currentValueDescriptionAsString, null, false);
                currentValueBuilder = currentValueBuilder.description(currentValueDescription);
            }

            // Add to list
            EnumValueDefinition currentValueDefinition = currentValueBuilder.build();
            dynamicEnumTypeValueDefinitions.add(currentValueDefinition);

            enumValues[i] = currentValue;
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

    public GraphQLSchema changeSchema(GraphQLSchema oldSchema)
    {
        CPTADynamicEnumSchemaTransformer<A> transformerForThisEnum = new CPTADynamicEnumSchemaTransformer<A>(this);

        GraphQLSchema newSchema = SchemaTransformer.transformSchema(oldSchema, transformerForThisEnum);

        return newSchema;
    }

    protected final EnumTypeDefinition enumTypeDefinition;
    protected final String typeName;
    protected final A[] enumValues;
    protected static Logger logger = CPTALogger.getLogger();
}

class CPTADynamicEnumSchemaTransformer<A extends CPTAGraphQLDynamicEnum<A>> extends GraphQLTypeVisitorStub
{
    CPTADynamicEnumSchemaTransformer(CPTAGraphQLDynamicEnumFactory<A> theModifiedEumFactory)
    {
        modifiedEumFactory = theModifiedEumFactory;
    }

    @Override
    public TraversalControl visitGraphQLEnumType(GraphQLEnumType enumType, TraverserContext<GraphQLSchemaElement> context) 
    {
        // if we the enum
        String typeName = modifiedEumFactory.enumTypeDefinition.getName();
        if(0 == typeName.compareTo(enumType.getName()))
        {
            // build a new enum type from old one
            GraphQLEnumType.Builder newEnumTypeBuilder = GraphQLEnumType.newEnum();
            // get the description
            Description enumDescription = modifiedEumFactory.enumTypeDefinition.getDescription();
            if(null != enumDescription)
            {
                newEnumTypeBuilder.description(enumDescription.getContent());
            }
            // add the name
            newEnumTypeBuilder.name(typeName);
            // add values
            List<EnumValueDefinition> values = modifiedEumFactory.enumTypeDefinition.getEnumValueDefinitions();
            for(EnumValueDefinition currentValue : values)
            {
                // get name
                String currentValueName = currentValue.getName();
                GraphQLEnumValueDefinition.Builder newValue = GraphQLEnumValueDefinition.newEnumValueDefinition().name(currentValueName);
                // add a description if exists
                Description currentValueDescription = currentValue.getDescription();
                if(null != currentValueDescription)
                {
                    newValue.description(currentValueDescription.getContent());
                }

                newEnumTypeBuilder.value(newValue.build());
            }

            GraphQLEnumType newEnumType = newEnumTypeBuilder.build();

            // modify the enum
            return changeNode(context, newEnumType);
        }

        return TraversalControl.CONTINUE;
    }

    CPTAGraphQLDynamicEnumFactory<A> modifiedEumFactory;
}