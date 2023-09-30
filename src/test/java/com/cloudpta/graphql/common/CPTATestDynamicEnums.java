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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.cloudpta.utilites.logging.CPTALogger;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class CPTATestDynamicEnums 
{
    @Test
    public void testDynamicEnumFactory() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        CPTALogger.initialise();

        List<Colour> values = new ArrayList<>();
        values.add(new Colour(0, "RED"));
        values.add(new Colour(1, "BLUE"));
        values.add(new Colour(2, "GREEN"));
        Map<String, String> valueDescriptions = new HashMap<>();
        valueDescriptions.put("BLUE", "Blue colour");
        ColourFactory cf = new ColourFactory(values, valueDescriptions);
        ColourFactory cf2 = (ColourFactory)CPTAGraphQLDynamicEnumFactory.getInstanceByClass(Colour.class);
        Colour[] colours = Colour.values(Colour.class);
        assertEquals(3, Array.getLength(colours));
        Colour red = Colour.valueOf(Colour.class,"RED");
        assertEquals("RED", red.name());

        values.add(new Colour(3, "BROWN"));
        cf = new ColourFactory(values, valueDescriptions);
        colours = Colour.values(Colour.class);
        assertEquals(4, Array.getLength(colours));
        red = Colour.valueOf(Colour.class,"RED");
        assertEquals("RED", red.name());
        Colour brown = Colour.valueOf(Colour.class,"BROWN");
        assertEquals("BROWN", brown.name());
    }

    @Test
    public void testBuildModifyEnum() throws Exception
    {
        CPTALogger.initialise();

        // set up initial enum
        List<String> colourNames = new ArrayList<>();
        List<Colour> values = new ArrayList<>();
        values.add(new Colour(0, "RED"));
        colourNames.add("RED");
        values.add(new Colour(1, "BLUE"));
        colourNames.add("BLUE");
        values.add(new Colour(2, "GREEN"));
        colourNames.add("GREEN");
        Map<String, String> valueDescriptions = new HashMap<>();
        valueDescriptions.put("BLUE", "Blue colour");
        ColourFactory cf = new ColourFactory(values, valueDescriptions);
        ColourFactory cf2 = (ColourFactory)CPTAGraphQLDynamicEnumFactory.getInstanceByClass(Colour.class);
        Colour[] colours = Colour.values(Colour.class);
        assertEquals(3, Array.getLength(colours));
        Colour red = Colour.valueOf(Colour.class,"RED");
        assertEquals("RED", red.name());

        // build simple schema
        SchemaParser schemaParser = new SchemaParser();
        // We are going to need a holder schema to say what types of queries are there
        String holderSchema = "type Query{hello: String}";
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(holderSchema);
        // add enum
        cf.addToTypeRegistry(typeDefinitionRegistry);
        // build rest of schema         
        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring().build();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        
        // check we have colour
        GraphQLEnumType colourType = (GraphQLEnumType)graphQLSchema.getType("Colour");
        // should exist
        assertNotNull(colourType);
        List<GraphQLEnumValueDefinition> colourValues = colourType.getValues();
        // should have 3
        assertEquals(3, colourValues.size());
        for(GraphQLEnumValueDefinition currentColour: colourValues)
        {
            String currentColourName = currentColour.getName();
            assertTrue(colourNames.contains(currentColourName));
        }

        // modify enum
        values.add(new Colour(3, "BROWN"));
        cf = new ColourFactory(values, valueDescriptions);
        colours = Colour.values(Colour.class);
        assertEquals(4, Array.getLength(colours));
        red = Colour.valueOf(Colour.class,"RED");
        assertEquals("RED", red.name());
        Colour brown = Colour.valueOf(Colour.class,"BROWN");
        assertEquals("BROWN", brown.name());
        colourNames.add("BROWN");

        // modify schema
        GraphQLSchema newGraphQLSchema = cf.changeSchema(graphQLSchema);
        // check we have colour
        colourType = (GraphQLEnumType)newGraphQLSchema.getType("Colour");
        // should exist
        assertNotNull(colourType);
        colourValues = colourType.getValues();
        // should have 4
        assertEquals(4, colourValues.size());
        for(GraphQLEnumValueDefinition currentColour: colourValues)
        {
            String currentColourName = currentColour.getName();
            assertTrue(colourNames.contains(currentColourName));
        }

    }
}

class Colour extends CPTAGraphQLDynamicEnum<Colour>
{
    protected Colour(int ordinal, String name)
    {
        super(ordinal, name);
    }
}

class ColourFactory extends CPTAGraphQLDynamicEnumFactory<Colour>
{

    public ColourFactory(List<Colour> values,
            Map<String, String> valueDescriptions) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        super(Colour.class, "Supported Colours", values, valueDescriptions);
    }
    
}
