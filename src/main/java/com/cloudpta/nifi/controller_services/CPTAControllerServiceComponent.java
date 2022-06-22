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
package com.cloudpta.nifi.controller_services;

import ch.qos.logback.classic.Logger;
import com.cloudpta.nifi.CPTANifiNamedComponent;
import com.cloudpta.utilites.logging.CPTALogger;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Danny
 */
public class CPTAControllerServiceComponent extends CPTANifiNamedComponent
{
    public List<CPTAControllerServiceProperty> getProperties()
    {
        return properties;
    }
    public void setProperties(List<CPTAControllerServiceProperty> newProperties)
    {
        properties = newProperties;
    }

    public String getControllerServiceType()
    {
        return type;
    }
    
    public void setControllerServiceType(String newType)
    {
        type = newType;
    }
    
    @Override
    protected void parseComponent(JsonObject componentAsJsonObject)
    {
        componentLogger.trace("nifi component to be parsed " + componentAsJsonObject.toString());
        // Let super class do its thing
        super.parseComponent(componentAsJsonObject);
        // get type
        type = componentAsJsonObject.getString("type");
        // get properties
        // Clear old properties
        properties.clear();
        // get new properties
        // Get the properties object
        JsonObject propertiesAsJsonObject = componentAsJsonObject.getJsonObject("properties");
        // the properties are name of the field and value
        // So get the list of all the fields by name
        Set<String> propertyNames = propertiesAsJsonObject.keySet();
        for( String currentPropertyName : propertyNames)
        {
            // Get the value for this property
            // May not be string so need to go via json value
            JsonValue currentPropertyValueAsJsonValue = propertiesAsJsonObject.get(currentPropertyName);
            String currentPropertyValue = currentPropertyValueAsJsonValue.toString();
            
            // get property as controller service property
            CPTAControllerServiceProperty currentProperty = new CPTAControllerServiceProperty();
            currentProperty.name = currentPropertyName;
            currentProperty.value = currentPropertyValue;
            
            // Add to list of properties
            properties.add(currentProperty);
        }
    }
    
    @Override
    protected JsonObjectBuilder getComponentAsJson()
    {
        JsonObjectBuilder componentAsJson = super.getComponentAsJson();
        componentAsJson.add("type", type);
        // Add properties
        JsonObjectBuilder processorPropertiesAsJson = Json.createObjectBuilder();   
        for(CPTAControllerServiceProperty currentProperty : properties)
        {
            processorPropertiesAsJson.add(currentProperty.name, currentProperty.value);
        }
        componentAsJson.add("properties", processorPropertiesAsJson);        
        return componentAsJson;
    }    
    
    protected List<CPTAControllerServiceProperty> properties = new ArrayList<>();    
    protected String type;
    protected String schedulingStrategy;
    protected String schedulingPeriod;
    static Logger componentLogger = CPTALogger.getLogger();
}
