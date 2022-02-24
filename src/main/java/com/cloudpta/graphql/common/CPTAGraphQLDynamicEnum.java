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

import java.util.concurrent.ConcurrentHashMap;
import graphql.schema.idl.TypeDefinitionRegistry;

public class CPTAGraphQLDynamicEnum<A extends CPTAGraphQLDynamicEnum<A>> implements Comparable<A>
{
    protected CPTAGraphQLDynamicEnum(int ordinal, String name)
    {
        this.ordinal = ordinal;
        this.name = name;
    }

    @Override
    public final boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        CPTAGraphQLDynamicEnum<?> that = (CPTAGraphQLDynamicEnum<?>) o;

        return ordinal == that.ordinal;

    }

    @Override
    public final int hashCode()
    {
        return ordinal;
    }

    @Override
    public final String toString()
    {
        //compatibility with Java enum
        return name;
    }

    @Override
    public final int compareTo(A o)
    {
        return this.ordinal - o.ordinal;
    }

    public int ordinal()
    {
        return ordinal;
    }

    public String name()
    {
        return name;
    }

    public static <B extends CPTAGraphQLDynamicEnum<B>> B valueOf(Class<B> enumType, String enumAsString)
    { 
        // Get the factory
        CPTAGraphQLDynamicEnumFactory<B> factory = (CPTAGraphQLDynamicEnumFactory<B>)allFactories.get(enumType); 

        return factory.valueOf(enumAsString);
    }

    public static <B extends CPTAGraphQLDynamicEnum<B>> B[] values(Class<B> enumType)
    {
        // Get the factory
        CPTAGraphQLDynamicEnumFactory<B> factory = (CPTAGraphQLDynamicEnumFactory<B>)allFactories.get(enumType); 
        
        return factory.values();
    }

    public static <B extends CPTAGraphQLDynamicEnum<B>> void addToTypeRegistry(Class<B>enumType, TypeDefinitionRegistry apiTypeDefinitionRegistry)
    {
        // Get the factory
        CPTAGraphQLDynamicEnumFactory<B> factory = (CPTAGraphQLDynamicEnumFactory<B>)allFactories.get(enumType); 
        factory.addToTypeRegistry(apiTypeDefinitionRegistry);
    }

    protected static final ConcurrentHashMap<Class<?>, CPTAGraphQLDynamicEnumFactory<?>> allFactories = new ConcurrentHashMap<>();
    protected final int ordinal;
    protected final String name;
}