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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class QPQueryVariablesParser 
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
