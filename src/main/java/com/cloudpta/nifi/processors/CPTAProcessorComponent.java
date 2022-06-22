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
package com.cloudpta.nifi.processors;

import com.cloudpta.nifi.CPTANifiNamedComponent;
import com.cloudpta.nifi.utilities.CPTANifiConstants;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Danny
 */
public class CPTAProcessorComponent extends CPTANifiNamedComponent
{
    public List<CPTAProcessorProperty> getProperties()
    {
        return properties;
    }
    public void setProperties(List<CPTAProcessorProperty> newProperties)
    {
        properties = newProperties;
    }

    public int getSchedulingType()
    {
        int schedulingType = 0;
        int numberOfOptions = Array.getLength(CPTANifiConstants.SCHEDULER_TYPE_TEXT);
        for(int i = 1; i < numberOfOptions; i++)
        {            
            if(0 == schedulingStrategy.compareTo(CPTANifiConstants.SCHEDULER_TYPE_TEXT[i]))
            {
                schedulingType = i;
                break;
            }
        }
        
        return schedulingType;
    }
    public void setSchedulingType(int newSchedulingStrategy)
    {
        schedulingStrategy = CPTANifiConstants.SCHEDULER_TYPE_TEXT[newSchedulingStrategy];
    }
    public String getSchedule()
    {
        return schedulingPeriod;
    }

    public String getYieldPeriod()
    {
        return yieldPeriod;
    }

    public void setYieldPeriod(String newYieldPeriod)
    {
        yieldPeriod = newYieldPeriod;
    }

    public void setSchedule(String newSchedulingPeriod)
    {
        schedulingPeriod = newSchedulingPeriod;
    }

    public String getProcessorType()
    {
        return type;
    }
    
    public void setProcessorType(String newType)
    {
        type = newType;
    }
    
    public void setAutoterminatedRelationships(List<String> relationshipsToAutoterminate)
    {
        autoterminatedRelationships = relationshipsToAutoterminate;
    }
    
    public List<String> getAutoterminatedRelationships()
    {
        return autoterminatedRelationships;
    }
    
    @Override
    protected void parseComponent(JsonObject componentAsJsonObject)
    {
        // Let super class do its thing
        super.parseComponent(componentAsJsonObject);
        // get type
        type = componentAsJsonObject.getString("type");
        // get configuration
        getConfiguration(componentAsJsonObject);
    }
    
    @Override
    protected JsonObjectBuilder getComponentAsJson()
    {
        JsonObjectBuilder componentAsJson = super.getComponentAsJson();
        
        // Add the type of processor this is
        componentAsJson.add("type", type);
        
        // Add the configuration subcomponent
        addConfiguration(componentAsJson);
        
        return componentAsJson;
    }

    protected void addConfiguration(JsonObjectBuilder componentAsJson)
    {
        JsonObjectBuilder configurationSubgroupAsJson = Json.createObjectBuilder();
        // Add scheduling
        configurationSubgroupAsJson.add("schedulingPeriod", schedulingPeriod);
        configurationSubgroupAsJson.add("schedulingStrategy", schedulingStrategy);
        configurationSubgroupAsJson.add("yieldDuration", yieldPeriod);
        
        // Add properties
        JsonObjectBuilder processorPropertiesAsJson = Json.createObjectBuilder();   
        for(CPTAProcessorProperty currentProperty : properties)
        {
            processorPropertiesAsJson.add(currentProperty.name, currentProperty.value);
        }
        configurationSubgroupAsJson.add("properties", processorPropertiesAsJson);        

        // If there are any relationships to autoterminate
        if( 0 != autoterminatedRelationships.size())
        {
            JsonArrayBuilder autoterminatedRelationshipsList = Json.createArrayBuilder();
            // Go through the relationships to be autoterminated
            // This is just a list of relationship names
            for( String autoterminatedRelationshipName : autoterminatedRelationships)
            {
                // Add to list
                autoterminatedRelationshipsList.add(autoterminatedRelationshipName);
            }
            
            // Add it to the configuration json object
            configurationSubgroupAsJson.add("autoTerminatedRelationships", autoterminatedRelationshipsList);
        }
        componentAsJson.add("config", configurationSubgroupAsJson);        
    }
    
    protected void getConfiguration(JsonObject componentAsJson)
    {
        // get config block
        JsonObject configAsJsonObject = componentAsJson.getJsonObject("config");
        // Get scheduling period
        schedulingPeriod = configAsJsonObject.getString("schedulingPeriod");
        // Get strategy
        schedulingStrategy = configAsJsonObject.getString("schedulingStrategy");
        
        // Clear old properties
        properties.clear();
        // get new properties
        // Get the properties object
        JsonObject propertiesAsJsonObject = configAsJsonObject.getJsonObject("properties");
        // the properties are name of the field and value
        // So get the list of all the fields by name
        Set<String> propertyNames = propertiesAsJsonObject.keySet();
        for( String currentPropertyName : propertyNames)
        {
            // Get the value for this property
            // May not be string so need to go via json value
            JsonValue currentPropertyValueAsJsonValue = propertiesAsJsonObject.get(currentPropertyName);
            String currentPropertyValue = currentPropertyValueAsJsonValue.toString();
            
            // get property as processor property
            CPTAProcessorProperty currentProperty = new CPTAProcessorProperty();
            currentProperty.name = currentPropertyName;
            currentProperty.value = currentPropertyValue;
            
            // Add to list of properties
            properties.add(currentProperty);
        }
    }
    
    protected List<String> autoterminatedRelationships = new ArrayList<>();
    protected List<CPTAProcessorProperty> properties = new ArrayList<>();    
    protected String type;
    protected String schedulingStrategy;
    protected String schedulingPeriod;
    protected String yieldPeriod = "1 sec";
}
