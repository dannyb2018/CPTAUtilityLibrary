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
import com.cloudpta.utilites.exceptions.CPTAException;
import com.cloudpta.utilites.logging.CPTALogger;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;

/**
 *
 * @author Danny
 */
public abstract class CPTANifiEndpoint<T extends CPTANifiComponent> extends CPTANifiStorableObject<T>
{
    public CPTANifiEndpoint() throws CPTAException
    {
        super();
    }
    
    public CPTANifiEndpoint(String serverName) throws CPTAException
    {
        super(serverName);
    }
    
    public String getEndpointType()
    {
        return endpointType;
    }
    
    public void setEndpointType(String newEndpointType)
    {
        endpointType = newEndpointType;
    }

    public void changeState( String newState) throws CPTAException
    {
        // Get latest version
        String findSubURL = getFindSubURL(component.getID());
        componentLogger.trace("find endpoint url is " + findSubURL);
        WebTarget api = getApiConnection(findSubURL);
        Response response = api.request(MediaType.APPLICATION_JSON).get();
        handleFindResponse(response);

        JsonObject changeStateRequestAsJson = buildChangeStateRequest(newState);
        String changeStateRequestAsJsonString = changeStateRequestAsJson.toString();
        
        // Get the store url
        String changeStateURL = getChangeStateSubURL();
        componentLogger.trace("change state endpoint url is " + changeStateURL);
        
        // call the store
        api = getApiConnection(changeStateURL);
        response = api.request(MediaType.APPLICATION_JSON).put(Entity.json(changeStateRequestAsJsonString));
        
        // parse the response
        handleChangeStateResponse(newState, response);
    }
          
    protected JsonObject buildChangeStateRequest(String newState)
    {
        // Generate the json
        JsonObjectBuilder changeStateRequestAsJsonBuilder = Json.createObjectBuilder();
        // Need to just get revision
        revision.addToObject(changeStateRequestAsJsonBuilder);
        // And add state
        changeStateRequestAsJsonBuilder.add("state", newState);
        
        JsonObject changeStateRequestAsJson = changeStateRequestAsJsonBuilder.build();
        componentLogger.trace("change request is " + changeStateRequestAsJson.toString());
        
        return changeStateRequestAsJson;
    }
    
    protected void handleChangeStateResponse(String newState, Response response) throws CPTAException
    {
        // get the string part
        String responseAsString = response.readEntity(String.class);
        int statusCode = response.getStatus();
        
        // if created or updated
        if((Response.Status.OK.getStatusCode() == statusCode)||(Response.Status.CREATED.getStatusCode() == statusCode))
        {
            // Turn into json
            JsonReader reader = Json.createReader(new StringReader(responseAsString));
            JsonObject incomingObject = reader.readObject();
            // parse it
            parse(incomingObject);
            
            // Get the state
            // if it is not the same as the desired state
            // throw an exception
        }
        else
        {
            // error 
            String errorString = Integer.toString(statusCode) + " : Cannot change status to " + newState + ", reason " + responseAsString;
            CPTAException exception = new CPTAException(errorString);
            throw exception;
        }
    }
    
    abstract protected String getChangeStateSubURL();

    protected String endpointType;
    static Logger componentLogger = CPTALogger.getLogger();
}
