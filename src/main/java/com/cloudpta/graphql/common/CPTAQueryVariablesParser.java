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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class CPTAQueryVariablesParser 
{
    public static Map<String, Object> parseVariables(JsonObject variables)
    {
        Map<String, Object> parsedVariables = toMap(variables);

        return parsedVariables;
    }

    protected static Map<String, Object> toMap(JsonObject object) 
    {
        Map<String, Object> map = new HashMap<String, Object>();
    
        // If there were any variables passed in
        if(null != object)
        {
            Set<String> keys = object.keySet();
            // Loop through the keys
            for(String currentKey: keys)
            {
                JsonValue valueForKey = object.get(currentKey);
                Object value = null;
                // If we are an object
                if(JsonValue.ValueType.OBJECT == valueForKey.getValueType())
                {
                    // get the object
                    value = toMap(object.getJsonObject(currentKey));
                }
                // If we are array
                else if(JsonValue.ValueType.ARRAY == valueForKey.getValueType())
                {
                    value = toList(valueForKey.asJsonArray());
                }
                else if(JsonValue.ValueType.NULL == valueForKey.getValueType())
                {
                    value = null;
                }
                else if(JsonValue.ValueType.NUMBER == valueForKey.getValueType())
                {
                    JsonNumber number = object.getJsonNumber(currentKey);
                    if(true == number.isIntegral())
                    {
                        value = number.intValue();
                    }
                    else
                    {
                        // Otherwise it is a float
                        value = number.doubleValue();
                    }
                }            
                else if(JsonValue.ValueType.STRING == valueForKey.getValueType())
                {
                    value = object.getString(currentKey);
                }            
                else if(JsonValue.ValueType.TRUE == valueForKey.getValueType())
                {
                    value = object.getBoolean(currentKey);
                }            
                else if(JsonValue.ValueType.FALSE == valueForKey.getValueType())
                {
                    value = object.getBoolean(currentKey);
                }  
                
                map.put(currentKey, value);
            }    
        }

        return map;
    }
    
    protected static List<Object> toList(JsonArray array)
    {
        List<Object> list = new ArrayList<Object>();
        
        for(int i = 0; i < array.size(); i++) 
        {
            JsonValue valueAtThisOffset = array.get(i);
            Object value = null;
            // If we are an object
            if(JsonValue.ValueType.OBJECT == valueAtThisOffset.getValueType())
            {
                // get the object
                value = toMap(valueAtThisOffset.asJsonObject());
            }
            // If we are array
            else if(JsonValue.ValueType.ARRAY == valueAtThisOffset.getValueType())
            {
                value = toList(valueAtThisOffset.asJsonArray());
            }
            else if(JsonValue.ValueType.NULL == valueAtThisOffset.getValueType())
            {
                value = null;
            }
            else if(JsonValue.ValueType.STRING == valueAtThisOffset.getValueType())
            {
                value = array.getJsonString(i).getString();
            }
            else
            {
                value = valueAtThisOffset;
            }            

            list.add(value);
        }

        return list;
    }    
}
