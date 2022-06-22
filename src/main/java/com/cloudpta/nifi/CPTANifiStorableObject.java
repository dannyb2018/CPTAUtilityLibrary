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
import com.cloudpta.nifi.utilities.CPTANifiConstants;
import com.cloudpta.utilites.exceptions.CPTAException;
import com.cloudpta.utilites.logging.CPTALogger;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;

/**
 *
 * @author Danny
 */
public abstract class CPTANifiStorableObject<T extends CPTANifiComponent> extends CPTANifiObject
{
    public CPTANifiStorableObject() throws CPTAException
    {
        // use default server
        server = CPTANifiServer.getDefaultInstance();
    }
    
    public CPTANifiStorableObject(String serverName) throws CPTAException
    {
        server = CPTANifiServer.getInstance(serverName);
    }
    
    public void persist() throws CPTAException
    {
        // Generate the json
        JsonObjectBuilder objectAsJsonBuilder = Json.createObjectBuilder();
        
        addToObject(objectAsJsonBuilder);

        JsonObject objectAsJson = objectAsJsonBuilder.build();
        String objectAsJsonString = objectAsJson.toString();
        componentLogger.trace("object to be stored body is " + objectAsJsonString);
        
        // Get the store url
        String storeSubURL = getPersistSubURL();
        componentLogger.trace("get store url is " + storeSubURL);
        
        // call the store
        WebTarget api = getApiConnection(storeSubURL);
        Response response = api.request(MediaType.APPLICATION_JSON).post(Entity.json(objectAsJsonString));
        
        // parse the response
        handlePersistResponse(response);
    }
    
    public void update() throws CPTAException
    {
        // Generate the json
        JsonObjectBuilder objectAsJsonBuilder = Json.createObjectBuilder();
        
        addToObject(objectAsJsonBuilder);

        JsonObject objectAsJson = objectAsJsonBuilder.build();
        String objectAsJsonString = objectAsJson.toString();
        componentLogger.trace("object to be stored body is " + objectAsJsonString);
        
        // Get the update url
        String storeSubURL = getUpdateSubURL();
        componentLogger.trace("get store url is " + storeSubURL);
        
        // call the store
        WebTarget api = getApiConnection(storeSubURL);
        Response response = api.request(MediaType.APPLICATION_JSON).put(Entity.json(objectAsJsonString));
        
        // parse the response
        handlePersistResponse(response);
    }

    public void delete() throws CPTAException
    {
        // Generate the json
        // Get the store url
        // call the store
        // parse the response
    }
    
    public CPTANifiStorableObject<T> find(String id) throws CPTAException
    {
        // Get the find url
        String findSubURL = getFindSubURL(id);
        // call it
        WebTarget api = getApiConnection(findSubURL);
        Response response = api.request(MediaType.APPLICATION_JSON).get();

        // handle find response
        handlePersistResponse(response);
        
        // This object is now the object we want
        return this;
    }

    public T getComponent()
    {
        return component;
    }
    
    public void setComponent(T newComponent)
    {
        component = newComponent;
    }

    @Override
    public void addToObject(JsonObjectBuilder nifiObject)
    {
        super.addToObject(nifiObject);
        // add component
        component.addToObject(nifiObject);
    }
        
    @Override
    public void parse(JsonObject nifiObject)
    {
        // let super do its thing
        super.parse(nifiObject);
        // get the component to parse
        component.parse(nifiObject);
    }

    protected void handlePersistResponse(Response response) throws CPTAException
    {
        // get the string part
        String responseAsString = response.readEntity(String.class);
        componentLogger.trace("response to persist response " + responseAsString);
        
        int statusCode = response.getStatus();
        
        // if created or updated
        if((Response.Status.OK.getStatusCode() == statusCode)||(Response.Status.CREATED.getStatusCode() == statusCode))
        {
            // Turn into json
            JsonReader reader = Json.createReader(new StringReader(responseAsString));
            JsonObject incomingObject = reader.readObject();
            // parse it
            parse(incomingObject);
        }
        else
        {
            // error
            String errorMessage = Integer.toString(statusCode) + ":" + responseAsString;
            componentLogger.error("error getting nifi persist response " + errorMessage);
            CPTAException standardException = new CPTAException(errorMessage);
            throw standardException;
        }
    }
    
    
    protected WebTarget getApiConnection(String suburl)
    {
        String serverURL = server.getURL();
        Client apiClient = ClientBuilder.newClient(); 
        String queryURL = serverURL + CPTANifiConstants.NIFI_API_BASE_URL + suburl;
        WebTarget apiTarget = apiClient.target(queryURL);

        return apiTarget;
    }

    protected void handleFindResponse(Response response) throws CPTAException
    {
        // get the string part
        String responseAsString = response.readEntity(String.class);
        componentLogger.trace("response to find response " + responseAsString);
        int statusCode = response.getStatus();
        
        // if created or updated
        if((Response.Status.OK.getStatusCode() == statusCode)||(Response.Status.CREATED.getStatusCode() == statusCode))
        {
            // Turn into json
            JsonReader reader = Json.createReader(new StringReader(responseAsString));
            JsonObject incomingObject = reader.readObject();
            // parse it
            parse(incomingObject);
        }
        else
        {
            // error
            String errorMessage = Integer.toString(statusCode) + ":" + responseAsString;
            componentLogger.error("error getting nifi find response " + errorMessage);
            CPTAException standardException = new CPTAException(errorMessage);
            throw standardException;
        }        
    }
    
    protected abstract String getPersistSubURL();
    protected abstract String getFindSubURL(String id);
    protected abstract String getDeleteSubURL();
    protected abstract String getUpdateSubURL();

    protected T component;
    protected CPTANifiServer server;
    static Logger componentLogger = CPTALogger.getLogger();
}
