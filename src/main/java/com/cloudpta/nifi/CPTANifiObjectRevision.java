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
public class CPTANifiObjectRevision
{
    public void setVersion(int newVersion)
    {
        version = Integer.toString(newVersion);
    }
    
    public int getVersion()
    {
        return Integer.parseInt(version);
    }
    
    public String getClientID()
    {
        return clientID;
    }
    
    public void setClientID(String newClientID)
    {
        clientID = newClientID;
    }
    
    public void parse(JsonObject nifiObject)
    {
        // get the revision subobject
        JsonObject revisionSubgroup = nifiObject.getJsonObject("revision");
        componentLogger.trace("nifi revision to parse " + revisionSubgroup.toString());
        
        // get client id, if it exists
        if( true == revisionSubgroup.containsKey("clientId"))
        {
            clientID = revisionSubgroup.getString("clientId");
        }
        // get the version which is a number
        int versionAsInt = revisionSubgroup.getInt("version");
        setVersion(versionAsInt);
    }
    
    public void addToObject(JsonObjectBuilder nifiObject)
    {
        JsonObjectBuilder revisionSubgroupAsJson = Json.createObjectBuilder();
        // if there is a client id
        if(null != clientID)
        {
           revisionSubgroupAsJson.add("clientId", clientID);
        }
        
        revisionSubgroupAsJson.add("version", version);
        nifiObject.add("revision", revisionSubgroupAsJson);
    }
    
    protected String version;
    protected String clientID;
    static Logger componentLogger = CPTALogger.getLogger();                              
}
