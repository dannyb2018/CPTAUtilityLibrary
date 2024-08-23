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
        // If input is null then return false
        if(null == o)
        {
            return false;
        }

        // if we are both same class
        if (getClass() == o.getClass())
        {
            CPTAGraphQLDynamicEnum<?> that = (CPTAGraphQLDynamicEnum<?>) o;

            return ordinal == that.ordinal;
        }
        // Else if input is String
        else if( o instanceof String)
        {
            return (0 == ((String)o).compareTo(name));
        }
        // if input is enum
        else if(o instanceof Enum)
        {
            return (0 == ((Enum<?>)o).name().compareTo(name));
        }
        // otherwise wrong type
        else
        {
            return false;
        }


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