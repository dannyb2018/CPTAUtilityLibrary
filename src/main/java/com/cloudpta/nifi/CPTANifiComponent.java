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
package com.cloudpta.nifi;

import ch.qos.logback.classic.Logger;
import com.cloudpta.utilites.logging.CPTALogger;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

/**
 *
 * @author Danny
 */
public class CPTANifiComponent
{    
    public String getID()
    {
        return id;
    }
    
    public void setID(String newID)
    {
        id = newID;
    }

    public String getProcessGroupID()
    {
        return processGroupID;
    }
    
    public void setProcessGroupID(String newProcessGroupID)
    {
        processGroupID = newProcessGroupID;
    }

    public void parse(JsonObject nifiObject)
    {
        // get the component subobject
        JsonObject componentAsJsonObject = nifiObject.getJsonObject("component");
        // parse the component bit, this will be overloaded in descedent classes
        parseComponent(componentAsJsonObject);
    }
    
    public void addToObject(JsonObjectBuilder nifiObject)
    {
        JsonObjectBuilder componentAsJson = getComponentAsJson();
        nifiObject.add("component", componentAsJson);
    }
    
    protected JsonObjectBuilder getComponentAsJson()
    {
        JsonObjectBuilder componentAsJson = Json.createObjectBuilder();
        // if there is a process group id
        if(null != processGroupID)
        {
            componentAsJson.add("parentGroupId", processGroupID);
        }
        // If there is an id, ie it is not new
        if( null != id)
        {
            componentAsJson.add("id", id);
        }
        
        return componentAsJson;
    }
    
    protected void parseComponent(JsonObject componentAsJsonObject)
    {
        componentLogger.trace("parsing component " + componentAsJsonObject.toString());
        
        // Get the id
        id = componentAsJsonObject.getString("id");
        // get the processor group id
        processGroupID = componentAsJsonObject.getString("parentGroupId", null);        
    }
    
    protected String id;    
    protected String processGroupID;
    static Logger componentLogger = CPTALogger.getLogger();                              
}
