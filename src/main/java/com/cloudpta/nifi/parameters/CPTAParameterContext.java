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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import com.cloudpta.nifi.CPTANifiStorableObject;
import com.cloudpta.nifi.utilities.CPTANifiConstants;
import com.cloudpta.utilites.exceptions.CPTAException;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class CPTAParameterContext extends CPTANifiStorableObject<CPTAParameterContextComponent> 
{
    public CPTAParameterContext() throws CPTAException
    {
        super();
        component = new CPTAParameterContextComponent();
    }

    public static List<CPTAParameterContext> getParameterContexts() throws CPTAException
    {
        List<CPTAParameterContext> contexts = new ArrayList<>();

        // request the list
        CPTAParameterContext dummyContext = new CPTAParameterContext();
        WebTarget target = dummyContext.getApiConnection(CPTANifiConstants.NIFI_API_GET_ALL_PARAMETER_CONTEXTS_SUBURL);
        Response parameterContextListAsResponse = target.request(MediaType.APPLICATION_JSON).get();
        String parameterContextListAsString = parameterContextListAsResponse.readEntity(String.class);
        JsonReader responseReader = Json.createReader(new StringReader(parameterContextListAsString));

        JsonObject responseAsJson = responseReader.readObject();

        // Get list of contexts
        JsonArray contextsAsJsonArray = responseAsJson.getJsonArray("parameterContexts");
        int numberOfContexts = contextsAsJsonArray.size();
        for(int i = 0; i < numberOfContexts; i++)
        {
            JsonObject contextAsJsonObject = contextsAsJsonArray.getJsonObject(i);
            CPTAParameterContext context = new CPTAParameterContext();
            // parse the context
            context.parse(contextAsJsonObject);
            // Add to list
            contexts.add(context);
        }
        
        return contexts;
    }

    public static String getParameterContextIDByName(String parameterContextName) throws CPTAException
    {
        CPTAParameterContext contextWithThisName = new CPTAParameterContext();
        String id = null;

        // Get the parameter context by name
        WebTarget target = contextWithThisName.getApiConnection("/flow/search-results?q=" + parameterContextName);
        Response groupToGetResponse = target.request(MediaType.APPLICATION_JSON).get();
        // get the string part
        String responseAsString = groupToGetResponse.readEntity(String.class);
        
        // Need to look for the id        
        if(Response.Status.OK.getStatusCode() == groupToGetResponse.getStatus())
        {
            // Turn into json
            JsonReader reader = Json.createReader(new StringReader(responseAsString));
            JsonObject incomingObject = reader.readObject();

            // Its is searchResultsDTO.parameterContextResults[0].id
            incomingObject = incomingObject.getJsonObject("searchResultsDTO");
            JsonArray parameterContextResults = incomingObject.getJsonArray("parameterContextResults");
            int numberOfResults = parameterContextResults.size();
            for(int i = 0; i < numberOfResults; i++)
            {
                JsonObject thisParameterContext = parameterContextResults.getJsonObject(i);
                String thisParameterContextName = thisParameterContext.getString("name");
                if( 0 == thisParameterContextName.compareTo(parameterContextName))
                {
                    id = thisParameterContext.getString("id");
                    break;
                }
                
            }
        }

        return id;
    }
    
    @Override
    protected String getPersistSubURL()
    {
        return CPTANifiConstants.NIFI_API_PARAMETER_CONTEXTS_SUBURL;
    }

    @Override
    protected String getFindSubURL(String id)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected String getDeleteSubURL()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected String getUpdateSubURL()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
}
