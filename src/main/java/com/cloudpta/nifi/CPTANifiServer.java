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
import jakarta.json.JsonReader;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import java.util.HashMap;

/**
 *
 * @author Danny
 */
public class CPTANifiServer
{
    public CPTANifiServer(String newName, String newUrl)
    {
        this.name = newName;
        this.url = newUrl;
    }
    
    public String getURL()
    {
        return url;
    }
    
    public String getName()
    {
        return name;
    }
    
    public CPTANifiServerDiagnostics getDiagnostics() throws CPTAException
    {
        CPTANifiServerDiagnostics diagnostics = null;
        
        // get diagnostics from backend        
        // Get the store url diagnostics
        String queryURL = getURL() + CPTANifiConstants.NIFI_API_BASE_URL + CPTANifiConstants.NIFI_API_SYSTEM_DIAGNOSTICS_SUBURL;
        Client apiClient = ClientBuilder.newClient(); 
        WebTarget apiTarget = apiClient.target(queryURL);
        Response response = apiTarget.request(MediaType.APPLICATION_JSON).get();
        
        // Get the response
        if(200 == response.getStatus())
        {
            String responseAsString = response.readEntity(String.class);
            JsonReader reader = Json.createReader(new StringReader(responseAsString));
            JsonObject incomingObject = reader.readObject();
            diagnostics = new CPTANifiServerDiagnostics();
            diagnostics.parseObject(incomingObject);
        }
        else
        {
            String newExceptionMessage = "Cannot get System Diagnostics";
            CPTAException exception = new CPTAException(newExceptionMessage);
            
            throw exception;
        }
        return diagnostics;
    }

    public static void storeInstance(String name, CPTANifiServer server)
    {
        
    }
    
    public static CPTANifiServer getDefaultInstance() throws CPTAException
    {
        return getInstance("quantpipeline_flow_server");
    }
    
    public static CPTANifiServer getInstance(String serverName) throws CPTAException
    {
        // if there is not a list of servers
        if(null == servers)
        {
            // throw exception
            CPTAException E = new CPTAException("Need to populate flow servers before access");
            throw E;
        }
        
        // Get the server by name
        CPTANifiServer server = servers.get(serverName);                
        
        return server;
    }
    
    public static void populateInstances(HashMap<String, CPTANifiServer> listOfNewServers) throws CPTAException
    {
        servers = listOfNewServers;
    }
    
    protected String name;
    protected String url;
    static HashMap<String, CPTANifiServer> servers = null;
    static Logger componentLogger = CPTALogger.getLogger();                              
}
