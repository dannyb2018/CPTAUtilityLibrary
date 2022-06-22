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
package com.cloudpta.nifi.parameters;

import java.util.Properties;
import java.util.Set;
import com.cloudpta.nifi.CPTANifiNamedComponent;
import com.cloudpta.utilites.logging.CPTALogger;
import ch.qos.logback.classic.Logger;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class CPTAParameterContextComponent extends CPTANifiNamedComponent
{
    public CPTAParameterContextComponent()
    {
        // do the standard stuff
        super();

        setDefaults();
    }
 
    public Properties getParameters()
    {
        return parameters;
    }

    public void setParameters(Properties newParameters)
    {
        parameters = newParameters;
    }

    @Override
    public void parse(JsonObject contextAsJson)
    {
        // do the default 
        super.parse(contextAsJson);

        setDefaults();

        // get parameters
        JsonObject contextComponentAsJson = contextAsJson.getJsonObject("component");
        JsonArray parametersAsJsonArray = contextComponentAsJson.getJsonArray("parameters");
        int numberOfParameters = parametersAsJsonArray.size();
        for(int i = 0; i < numberOfParameters; i++)
        {
            JsonObject parameterSettings = parametersAsJsonArray.getJsonObject(i);
            JsonObject parameterAsJson = parameterSettings.getJsonObject("parameter");
            String propertyName = parameterAsJson.getString("name");
            String propertyValue = parameterAsJson.getString("value");
            parameters.put(propertyName, propertyValue);
        }
    }
    
    @Override
    protected JsonObjectBuilder getComponentAsJson()
    {
        JsonObjectBuilder componentAsJson = super.getComponentAsJson();
        // get parameters
        componentAsJson.add("parameters", getParametersAsJson());

        // bind to every process group
        return componentAsJson;
    }

    protected void setDefaults()
    {
        parameters = new Properties();

        // add defaults to the properties
        parameters.setProperty("database_max_idle_connections", "8");
        parameters.setProperty("database_max_total_connections", "8");
        parameters.setProperty("database_min_idle_connections", "0");
        
    }
    protected JsonArrayBuilder getParametersAsJson()
    {
        JsonArrayBuilder parametersAsJson = Json.createArrayBuilder();

        // get the names of the properties
        Set<Object>  propertyNames = parameters.keySet();
        for(Object propertyNameAsObject : propertyNames)
        {
            // create the parameter setting
            String parameterName = (String)propertyNameAsObject;
            JsonObjectBuilder parameterSetting = Json.createObjectBuilder();
            parameterSetting.add("canWrite", true);
            JsonObjectBuilder parameter = Json.createObjectBuilder();
            parameter.add("name", parameterName);
            String parameterValue = parameters.getProperty(parameterName); 
            parameter.add("value", parameterValue);
            // make sensitive if includes word password
            if(true == parameterName.contains("password"))
            {
                parameter.add("sensitive", true);
            }
            parameterSetting.add("parameter", parameter);

            // add it to list
            parametersAsJson.add(parameterSetting);
        }

        return parametersAsJson;
    }


    protected Properties parameters = new Properties();
    static Logger componentLogger = CPTALogger.getLogger();
    
}
