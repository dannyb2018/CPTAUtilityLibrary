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
import com.cloudpta.nifi.CPTANifiStorableObject;
import com.cloudpta.nifi.utilities.CPTANifiConstants;
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
public class CPTAControllerService extends CPTANifiStorableObject<CPTAControllerServiceComponent>
{
    public CPTAControllerService() throws CPTAException
    {
        super();
        component = new CPTAControllerServiceComponent();
    }

    public CPTAControllerService(String serverName) throws CPTAException
    {
        super(serverName);
        component = new CPTAControllerServiceComponent();
    }

    public void enable() throws CPTAException
    {
        changeState(CPTANifiConstants.STATUS_ENABLED);
    }

    public void disable() throws CPTAException
    {
        changeState(CPTANifiConstants.STATUS_DISABLED);
    }

    public void changeState( String newState) throws CPTAException
    {
        // Get latest version
        String findSubURL = getFindSubURL(component.getID());
        componentLogger.trace("make find request to " + findSubURL);
        
        WebTarget api = getApiConnection(findSubURL);
        Response response = api.request(MediaType.APPLICATION_JSON).get();
        handleFindResponse(response);

        // Generate the json
        JsonObjectBuilder changeStateRequestAsJsonBuilder = Json.createObjectBuilder();
        // Need to just get revision
        revision.addToObject(changeStateRequestAsJsonBuilder);
        // And add state
        changeStateRequestAsJsonBuilder.add("state", newState);
        
        JsonObject changeStateRequestAsJson = changeStateRequestAsJsonBuilder.build();
        String changeStateRequestAsJsonString = changeStateRequestAsJson.toString();
        componentLogger.trace("make change request body is " + changeStateRequestAsJsonString);
        
        // Get the store url
        String changeStateURL = getChangeStateSubURL();
        
        // call the store
        componentLogger.trace("make change request to " + changeStateURL);
        api = getApiConnection(changeStateURL);
        response = api.request(MediaType.APPLICATION_JSON).put(Entity.json(changeStateRequestAsJsonString));
        
        // parse the response
        handleChangeStateResponse(response);
    }
                    
    protected void handleChangeStateResponse(Response response) throws CPTAException
    {
        // get the string part
        String responseAsString = response.readEntity(String.class);
        componentLogger.trace("response to change state " + responseAsString);
        
        // if created or updated
        if((Response.Status.OK.getStatusCode() == response.getStatus())||(Response.Status.CREATED.getStatusCode() == response.getStatus()))
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
            String errorString = "cannot change state of controller " + this.component.getName() + " on flow server " + this.server.getName() + " reason " + responseAsString;
            CPTAException standardException = new CPTAException(errorString);
            componentLogger.error(errorString);
            throw standardException;
        }
    }
    
    protected void handleFindResponse(Response response) throws CPTAException
    {
        // get the string part
        String responseAsString = response.readEntity(String.class);
        componentLogger.trace("response to find controller " + responseAsString);
        
        // if created or updated
        if((Response.Status.OK.getStatusCode() == response.getStatus())||(Response.Status.CREATED.getStatusCode() == response.getStatus()))
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
            String errorString = "cannot find controller " + this.component.getName() + " on flow server " + this.server.getName() + " reason " + responseAsString;
            CPTAException standardException = new CPTAException(errorString);
            componentLogger.error(errorString);
            throw standardException;
        }        
    }
    
    protected String getChangeStateSubURL()
    {
        return CPTANifiConstants.NIFI_API_CONTROLLER_SERVICES_SUBURL + "/" + component.getID()+ CPTANifiConstants.NIFI_API_RUN_STATUS_SUBURL;
    }

    @Override
    protected String getPersistSubURL()
    {
        return CPTANifiConstants.NIFI_API_PROCESS_GROUPS_SUBURL + "/" + component.getProcessGroupID() + CPTANifiConstants.NIFI_API_CONTROLLER_SERVICES_SUBURL;
    }

    @Override
    protected String getFindSubURL(String id)
    {
        return CPTANifiConstants.NIFI_API_CONTROLLER_SERVICES_SUBURL + "/" + id;
    }

    @Override
    protected String getDeleteSubURL()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    protected String endpointType;    
    static Logger componentLogger = CPTALogger.getLogger();
    @Override
    protected String getUpdateSubURL()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
