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
        Colour[] colours = Colour.values(Colour.class);
        assertEquals(3, Array.getLength(colours));
        Colour red = Colour.valueOf(Colour.class,"RED");
        assertEquals("RED", red.name());
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

    public ColourFactory(List<String> values,
            Map<String, String> valueDescriptions) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        super(Colour.class, "Supported Colours", values, valueDescriptions);
    }
    
}
