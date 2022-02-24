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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class CPTATestDynamicEnums 
{
    @Test
    public void testDynamicEnumFactory() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        List<String> values = new ArrayList<>();
        values.add("RED");
        values.add("BLUE");
        values.add("GREEN");
        Map<String, String> valueDescriptions = new HashMap<>();
        valueDescriptions.put("BLUE", "Blue colour");
        ColourFactory cf = new ColourFactory(values, valueDescriptions);
        ColourFactory cf2 = (ColourFactory)CPTAGraphQLDynamicEnumFactory.getInstanceByClass(Colour.class);
        Colour[] colours = cf2.values();
        assertEquals(3, Array.getLength(colours));
        Colour red = cf2.valueOf("RED");
        assertEquals("RED", red.name());
    }
}

class Colour extends CPTAGraphQLDynamicEnum<Colour>
{
    public Colour(int ordinal, String name)
    {
        super(ordinal, name);
    }
}

class ColourFactory extends CPTAGraphQLDynamicEnumFactory<Colour>
{

    public ColourFactory(List<String> values,
            Map<String, String> valueDescriptions) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        super(Colour.class, "Supported Colours", values, valueDescriptions);
    }
    
}
