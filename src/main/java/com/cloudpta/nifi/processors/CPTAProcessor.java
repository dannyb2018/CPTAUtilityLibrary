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

import ch.qos.logback.classic.Logger;
import com.cloudpta.nifi.CPTANifiEndpoint;
import com.cloudpta.nifi.process_groups.CPTAProcessGroup;
import com.cloudpta.nifi.utilities.CPTANifiConstants;
import com.cloudpta.utilites.exceptions.CPTAException;
import com.cloudpta.utilites.logging.CPTALogger;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Danny
 */
public class CPTAProcessor  extends CPTANifiEndpoint<CPTAProcessorComponent>
{
    public CPTAProcessor() throws CPTAException
    {
        super();
        component = new CPTAProcessorComponent();
        this.endpointType = "PROCESSOR";
    }

    public CPTAProcessor(String serverName) throws CPTAException
    {
        super(serverName);
        component = new CPTAProcessorComponent();
        this.endpointType = "PROCESSOR";
    }
    
    public void start() throws CPTAException
    {
        // Default is just start processor
        changeState(CPTANifiConstants.STATUS_RUNNING);
    }

    public void stop() throws CPTAException
    {
        // Default is just stop processor
        changeState(CPTANifiConstants.STATUS_STOPPED);        
    }    

    public void autoTerminateRelationship(String relationshipName)
    {
        List<String> autoterminatedRelationships = component.getAutoterminatedRelationships();
        // Check if the relationship is already in there
        if(false == autoterminatedRelationships.contains(relationshipName))
        {
            autoterminatedRelationships.add(relationshipName);
        }
    }
    
    public boolean isRunning()
    {
        // Assume it is not running
        boolean isRunning = false;
        
        // make a query to get the status of this processor
        String statusSubURL = "/flow/processors/" + component.getID() + "/status";
        WebTarget api = getApiConnection(statusSubURL);
        Response response = api.request(MediaType.APPLICATION_JSON).get();
        // If the query was a success
        if((Response.Status.OK.getStatusCode() == response.getStatus())||(Response.Status.CREATED.getStatusCode() == response.getStatus()))
        {
            // get the string part
            String responseAsString = response.readEntity(String.class);
            // Turn into json
            JsonReader reader = Json.createReader(new StringReader(responseAsString));
            // status is processorStatus->aggregateSnapshot->runStatus
            JsonObject statusObject = reader.readObject().getJsonObject("processorStatus");
            JsonObject snapshotObject = statusObject.getJsonObject("aggregateSnapshot");
            String status = snapshotObject.getString("runStatus");
            
            // Only true if the status is running
            isRunning = (0 == status.compareToIgnoreCase(CPTANifiConstants.STATUS_RUNNING));
        }
            
        
        return isRunning;
    }
    
    public boolean isValid()
    {
        // Assume it is not valid
        boolean isValid = false;

        // make a query to get the status of this processor
        String statusSubURL = "/flow/processors/" + component.getID() + "/status";
        WebTarget api = getApiConnection(statusSubURL);
        Response response = api.request(MediaType.APPLICATION_JSON).get();
        // If the query was a success
        if((Response.Status.OK.getStatusCode() == response.getStatus())||(Response.Status.CREATED.getStatusCode() == response.getStatus()))
        {
            // get the string part
            String responseAsString = response.readEntity(String.class);
            // Turn into json
            JsonReader reader = Json.createReader(new StringReader(responseAsString));
            // status is processorStatus->aggregateSnapshot->runStatus
            JsonObject statusObject = reader.readObject().getJsonObject("processorStatus");
            JsonObject snapshotObject = statusObject.getJsonObject("aggregateSnapshot");
            String status = snapshotObject.getString("runStatus");
            
            // Only true if the status isnot INVALID
            isValid = (0 != status.compareToIgnoreCase("INVALID"));
        }
        
        return isValid;
    }

    public void setProcessGroup( CPTAProcessGroup parentGroup )
    {
        component.setProcessGroupID(parentGroup.getComponent().getID());
    }
    
    public List<CPTAProcessorProperty> parseProperties(Properties properties)
    {        
        // Get the propoerties of the source
        List<CPTAProcessorProperty> processorProperties = new ArrayList<>();
        
        // firstly set defaults
        setDefaultProcessorProperties(processorProperties);
        
        // Go through each property and set the ones that are controller relevant
        for(String currentPropertyName: properties.stringPropertyNames() )
        {
            String currentPropertyValue = properties.getProperty(currentPropertyName);
            // If this is a processor property
            if(true == isProcessorProperty(currentPropertyName))
            {
                CPTAProcessorProperty currentProcessorProperty = parseProperty(currentPropertyName, currentPropertyValue);
                processorProperties.add(currentProcessorProperty);
            }
        }
        
        return processorProperties;
    }
    
    protected CPTAProcessorProperty parseProperty(String propertyToAddName, String propertyToAddValue)
    {
        // default is that is straight add as is
        CPTAProcessorProperty processorProperty = new CPTAProcessorProperty();
        processorProperty.name = propertyToAddName;
        processorProperty.value = propertyToAddValue;
        
        return processorProperty;
    }
    
    protected boolean isProcessorProperty(String property)
    {
        // default is no
        return false;
    }
    
    protected void setDefaultProcessorProperties(List<CPTAProcessorProperty> processorProperties)
    {
        // default is no default properties
        // so do nothing        
    }

    
    public List<CPTAProcessorProperty> getProperties()
    {
        return component.getProperties();
    }

    public void setProperties(List<CPTAProcessorProperty> newProperties)
    {
        component.setProperties(newProperties);
    }

    public String getSchedule()
    {
        return component.getSchedule();
    }
    public void setSchedule(String newSchedule)
    {
        component.setSchedule(newSchedule);
    }
    public int getSchedulingType()
    {
        return component.getSchedulingType();
    }
    public void setSchedulingType(int newSchedulingType)
    {
        component.setSchedulingType(newSchedulingType);
    }

    public String getName()
    {
        return component.getName();
    }
    
    public void setName(String newName)
    {
        component.setName(newName);
    }    

    public String getProcessorType()
    {
        return component.getProcessorType();
    }
    
    public void setProcessorType(String newType)
    {
        component.setProcessorType(newType);
    }    
    
    @Override
    protected String getPersistSubURL()
    {
        return CPTANifiConstants.NIFI_API_PROCESS_GROUPS_SUBURL + "/" + component.getProcessGroupID() + "/processors";
    }

    @Override
    protected String getFindSubURL(String id)
    {
        return CPTANifiConstants.NIFI_API_PROCESSORS_SUBURL + "/" + id;
    }

    @Override
    protected String getDeleteSubURL()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected String getChangeStateSubURL()
    {
        return CPTANifiConstants.NIFI_API_PROCESSORS_SUBURL + "/" + component.getID()+ CPTANifiConstants.NIFI_API_RUN_STATUS_SUBURL;
    }
    
    static Logger componentLogger = CPTALogger.getLogger();

    @Override
    protected String getUpdateSubURL()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
