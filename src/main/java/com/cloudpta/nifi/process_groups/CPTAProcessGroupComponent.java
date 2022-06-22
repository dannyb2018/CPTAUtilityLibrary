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
package com.cloudpta.nifi.process_groups;

import com.cloudpta.nifi.CPTANifiNamedComponent;
import com.cloudpta.nifi.parameters.CPTAParameterContext;
import com.cloudpta.utilites.exceptions.CPTAException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

/**
 *
 * @author Danny
 */
public class CPTAProcessGroupComponent extends CPTANifiNamedComponent
{
    @Override
    public void parse(JsonObject nifiObject)
    {
        super.parse(nifiObject);
        
        // Get the stopped count
        stoppedCount = nifiObject.getInt("stoppedCount");
        // get the invalid count
        invalidCount = nifiObject.getInt("invalidCount");
        // get the disabled count
        disabledCount = nifiObject.getInt("disabledCount");

        // Get the component
        JsonObject componentAsJson = nifiObject.getJsonObject("component");
        // get the parameter context
        JsonObject parameterContextAsJson = componentAsJson.getJsonObject("parameterContext");
        // If there is one
        if(null != parameterContextAsJson)
        {
            JsonObject parameterContextComponentAsJson = parameterContextAsJson.getJsonObject("component");
            parameterContextName = parameterContextComponentAsJson.getString("name");
        }
    }
    
    @Override
    protected JsonObjectBuilder getComponentAsJson()
    {
        JsonObjectBuilder componentAsJson = super.getComponentAsJson();

        // If there is a parameter context name
        if(null != parameterContextName)
        {
            JsonObjectBuilder parameterContextAsJson = Json.createObjectBuilder();
            // find the parameter context by name
            try
            {
                String parameterContextID = CPTAParameterContext.getParameterContextIDByName(parameterContextName);
                // Add id
                parameterContextAsJson.add("id", parameterContextID);
                JsonObjectBuilder permissions = Json.createObjectBuilder();
                permissions.add("canWrite", true);
                parameterContextAsJson.add("permissions", permissions);
                JsonObjectBuilder parameterContextComponent = Json.createObjectBuilder();
                parameterContextComponent.add("name", parameterContextName);
                parameterContextComponent.add("id", parameterContextID);
                parameterContextAsJson.add("component", parameterContextComponent);

                componentAsJson.add("parameterContext", parameterContextAsJson);
            }
            catch(CPTAException E)
            {
                
            }
        }

        return componentAsJson;
    }

    protected boolean isRunning()
    {
        // Only if no processors/controllers are stopped, invalid or disabled
        return (0 == stoppedCount) && (0 == disabledCount) && (0 == invalidCount);
    }


    public String getParameterContext()
    {
        return parameterContextName;
    }

    public void setParameterContext(String newParameterContextName)
    {
        parameterContextName = newParameterContextName;
    }

    protected int stoppedCount = -1;
    protected int disabledCount = -1;
    protected int invalidCount = -1;
    protected String parameterContextName = null;
}
