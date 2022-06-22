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

import ch.qos.logback.classic.Logger;

import com.cloudpta.nifi.CPTANifiEndpoint;
import com.cloudpta.nifi.controller_services.CPTAControllerService;
import com.cloudpta.nifi.processors.CPTAProcessor;
import com.cloudpta.nifi.utilities.CPTANifiConstants;
import com.cloudpta.utilites.exceptions.CPTAException;
import com.cloudpta.utilites.logging.CPTALogger;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Danny
 */
public class CPTAProcessGroup extends CPTANifiEndpoint<CPTAProcessGroupComponent>
{        
    public CPTAProcessGroup() throws CPTAException
    {
        super();
        component = new CPTAProcessGroupComponent();
        this.endpointType = "PROCESS_GROUP";
    }
    
    public CPTAProcessGroup(String serverName) throws CPTAException
    {
        super(serverName);
        component = new CPTAProcessGroupComponent();
        this.endpointType = "PROCESS_GROUP";
    }

    public boolean isRunning()
    {
        return component.isRunning();
    }
    public List<CPTAProcessGroup> getSubgroups(String processGroupNameStartString) throws CPTAException
    {
        // assume empty list
        List<CPTAProcessGroup> subgroups = new ArrayList<>();
        String getProcessorListUrl = "/process-groups/" + component.getID() + "/process-groups";
        WebTarget target = getApiConnection(getProcessorListUrl);
        Response processorListResponse = target.request(MediaType.APPLICATION_JSON).get();
        
        // If result was ok
        // if created or updated
        if(Response.Status.OK.getStatusCode() == processorListResponse.getStatus())
        {
            // get the string part
            String responseAsString = processorListResponse.readEntity(String.class);
            // Turn into json
            JsonReader reader = Json.createReader(new StringReader(responseAsString));
            JsonObject processorListAsJsonObject = reader.readObject();
            // The array is in field processors
            JsonArray processorListAsJsonArray = processorListAsJsonObject.getJsonArray("processGroups");
            // go through the list
            int numberOfProcessors = processorListAsJsonArray.size();
            for(int i = 0; i < numberOfProcessors; i++)
            {
                // Get the json rep of the current processor
                JsonObject currentProcessorAsJsonObject = processorListAsJsonArray.getJsonObject(i);
                // Get name
                String processGroupName = currentProcessorAsJsonObject.getJsonObject("component").getString("name");
                if(true == processGroupName.startsWith(processGroupNameStartString))
                {
                    // parse that json
                    CPTAProcessGroup currentProcessor = new CPTAProcessGroup();
                    currentProcessor.parse(currentProcessorAsJsonObject);

                    // add to list
                    subgroups.add(currentProcessor);
                }
            }
        }
        else
        {
            // error
        }
        
        return subgroups;
    }

    public List<CPTAProcessGroup> getSubgroups() throws CPTAException
    {
        // assume empty list
        List<CPTAProcessGroup> subgroups = new ArrayList<>();
        String getProcessorListUrl = "/process-groups/" + component.getID() + "/process-groups";
        WebTarget target = getApiConnection(getProcessorListUrl);
        Response processorListResponse = target.request(MediaType.APPLICATION_JSON).get();
        
        // If result was ok
        // if created or updated
        if(Response.Status.OK.getStatusCode() == processorListResponse.getStatus())
        {
            // get the string part
            String responseAsString = processorListResponse.readEntity(String.class);
            // Turn into json
            JsonReader reader = Json.createReader(new StringReader(responseAsString));
            JsonObject processorListAsJsonObject = reader.readObject();
            // The array is in field processors
            JsonArray processorListAsJsonArray = processorListAsJsonObject.getJsonArray("processGroups");
            // go through the list
            int numberOfProcessors = processorListAsJsonArray.size();
            for(int i = 0; i < numberOfProcessors; i++)
            {
                // Get the json rep of the current processor
                JsonObject currentProcessorAsJsonObject = processorListAsJsonArray.getJsonObject(i);
                // parse that json
                CPTAProcessGroup currentProcessor = new CPTAProcessGroup();
                currentProcessor.parse(currentProcessorAsJsonObject);
                
                // add to list
                subgroups.add(currentProcessor);
            }
        }
        else
        {
            // error
        }
        
        return subgroups;
    }
    
    public List<CPTAProcessor> getProcessors() throws CPTAException
    {
        // Assume empty list to start with
        List<CPTAProcessor> processors = new ArrayList<>();
        
        // create the processor request sub url
        // it is /process-groups/{id}/processors
        String getProcessorListUrl = "/process-groups/" + component.getID() + "/processors";
        WebTarget target = getApiConnection(getProcessorListUrl);
        Response processorListResponse = target.request(MediaType.APPLICATION_JSON).get();
        
        // If result was ok
        // if created or updated
        if(Response.Status.OK.getStatusCode() == processorListResponse.getStatus())
        {
            // get the string part
            String responseAsString = processorListResponse.readEntity(String.class);
            // Turn into json
            JsonReader reader = Json.createReader(new StringReader(responseAsString));
            JsonObject processorListAsJsonObject = reader.readObject();
            // The array is in field processors
            JsonArray processorListAsJsonArray = processorListAsJsonObject.getJsonArray("processors");
            // go through the list
            int numberOfProcessors = processorListAsJsonArray.size();
            for(int i = 0; i < numberOfProcessors; i++)
            {
                // Get the json rep of the current processor
                JsonObject currentProcessorAsJsonObject = processorListAsJsonArray.getJsonObject(i);
                // parse that json
                CPTAProcessor currentProcessor = new CPTAProcessor();
                currentProcessor.parse(currentProcessorAsJsonObject);
                
                // add to list
                processors.add(currentProcessor);
            }
        }
        else
        {
            // error
        }

        return processors;
    }
    
    public List<CPTAControllerService> getControllerServices() throws CPTAException
    {
        // Assume empty list to start with
        List<CPTAControllerService> controllerServices = new ArrayList<>();
        
        // create the controller service request sub url
        // it is /flow/process-groups/{id}/controller-services
        String getServicesListUrl = "/flow/process-groups/" + component.getID() + "/controller-services";
        WebTarget target = getApiConnection(getServicesListUrl);
        Response servicesListResponse = target.request(MediaType.APPLICATION_JSON).get();
        
        // If result was ok
        // if created or updated
        if(Response.Status.OK.getStatusCode() == servicesListResponse.getStatus())
        {
            // get the string part
            String responseAsString = servicesListResponse.readEntity(String.class);
            // Turn into json
            JsonReader reader = Json.createReader(new StringReader(responseAsString));
            JsonObject servicesListAsJsonObject = reader.readObject();
            // The array is in field controllerServices
            JsonArray servicesListAsJsonArray = servicesListAsJsonObject.getJsonArray("controllerServices");
            // go through the list
            int numberOfServices = servicesListAsJsonArray.size();
            for(int i = 0; i < numberOfServices; i++)
            {
                // Get the json rep of the current controller service
                JsonObject currentServiceAsJsonObject = servicesListAsJsonArray.getJsonObject(i);
                // parse that json
                CPTAControllerService currentService = new CPTAControllerService();
                currentService.parse(currentServiceAsJsonObject);
                
                // add to list
                controllerServices.add(currentService);
            }
        }
        else
        {
            // error
        }

        return controllerServices;        
    }
    
    public static CPTAProcessGroup getProcessGroupByName(String name) throws CPTAException
    {
        CPTAProcessGroup groupToGet = new CPTAProcessGroup();
        
        try
        {
            // Get the group id by name
            WebTarget target = groupToGet.getApiConnection("/flow/search-results?q=" + name);
            Response groupToGetResponse = target.request(MediaType.APPLICATION_JSON).get();
            // get the string part
            String responseAsString = groupToGetResponse.readEntity(String.class);
            
            // Need to look for the id        
            if(Response.Status.OK.getStatusCode() == groupToGetResponse.getStatus())
            {
                // Turn into json
                JsonReader reader = Json.createReader(new StringReader(responseAsString));
                JsonObject incomingObject = reader.readObject();

                // Its is searchResultsDTO.processGroupResults[0].id
                incomingObject = incomingObject.getJsonObject("searchResultsDTO");
                JsonArray processGroupResults = incomingObject.getJsonArray("processGroupResults");
                if( 0 != processGroupResults.size())
                {
                    JsonObject thisProcessGroup = processGroupResults.getJsonObject(0);
                    String id = thisProcessGroup.getString("id");

                    // get the process group
                    groupToGet.find(id);
                }
                else
                {
                    // The process group doesnt exist
                    CPTAException noSuchGroupException = new CPTAException("Process Group " + name + " not found");
                    componentLogger.error("cannot find process group " + name + ", " + noSuchGroupException.getErrors().toString() );
                    throw noSuchGroupException;
                }
            }
            else
            {
                CPTAException standardisedException = new CPTAException("Error trying to find process group " + name + ", reason, " + responseAsString);
                componentLogger.error("cannot find process group " + name + ", " + standardisedException.getErrors().toString() );
                throw standardisedException;
            }        
        }
        catch(Exception E)
        {
            CPTAException standardException = new CPTAException(E);
            componentLogger.error("error finding processor group " + standardException.getErrors().toString());
            throw standardException;
        }
        return groupToGet;
    }
        
    public static CPTAProcessGroup getTopLevelProcessGroup() throws CPTAException
    {
        // Top Level process group has id of root
        CPTAProcessGroup topLevelGroup = new CPTAProcessGroup();        
        WebTarget target = topLevelGroup.getApiConnection(CPTANifiConstants.NIFI_API_PROCESS_GROUPS_SUBURL + "/root");
        Response topLevelProcessGroupResponse = target.request(MediaType.APPLICATION_JSON).get();
        String topLevelProcessGroupAsString = topLevelProcessGroupResponse.readEntity(String.class);
        componentLogger.trace("top level processor group is " + topLevelProcessGroupAsString);        
        JsonReader responseReader = Json.createReader(new StringReader(topLevelProcessGroupAsString));

        JsonObject responseAsJson = responseReader.readObject();
        topLevelGroup.parse(responseAsJson);
        
        return topLevelGroup;
    }
    
    @Override
    public String getPersistSubURL()
    {
        return CPTANifiConstants.NIFI_API_PROCESS_GROUPS_SUBURL + "/" + component.getProcessGroupID() + "/process-groups";
    }

    @Override
    protected String getUpdateSubURL()
    {
        return CPTANifiConstants.NIFI_API_PROCESS_GROUPS_SUBURL + "/" + component.getID();
    }

    @Override
    protected String getFindSubURL(String id)
    {
        return CPTANifiConstants.NIFI_API_PROCESS_GROUPS_SUBURL + "/" + id;
    }

    @Override
    protected String getDeleteSubURL()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected JsonObject buildChangeStateRequest(String newState)
    {
        // Generate the json
        JsonObjectBuilder changeStateRequestAsJsonBuilder = Json.createObjectBuilder();
        // Add id
        changeStateRequestAsJsonBuilder.add("id", component.getID());
        // And add state
        changeStateRequestAsJsonBuilder.add("state", newState);
        
        JsonObject changeStateRequestAsJson = changeStateRequestAsJsonBuilder.build();
        componentLogger.trace("process group change request is " + changeStateRequestAsJson.toString());
        
        return changeStateRequestAsJson;
    }

    @Override
    protected String getChangeStateSubURL()
    {
        return CPTANifiConstants.NIFI_API_PROCESS_GROUP_CHANGE_STATE_SUBURL + component.getID();
    }
    
    protected void storeProcessors()
    {
        
    }

    protected List<CPTAProcessor> graph = new ArrayList<>();
    static Logger componentLogger = CPTALogger.getLogger();
}
