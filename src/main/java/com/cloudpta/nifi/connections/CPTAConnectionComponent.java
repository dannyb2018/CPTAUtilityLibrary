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
package com.cloudpta.nifi.connections;

import ch.qos.logback.classic.Logger;
import com.cloudpta.nifi.CPTANifiEndpoint;
import com.cloudpta.nifi.CPTANifiNamedComponent;
import com.cloudpta.utilites.logging.CPTALogger;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Danny
 */
public class CPTAConnectionComponent extends CPTANifiNamedComponent
{
    public CPTANifiEndpoint getSource()
    {
        return source;
    }

    public void setSource(CPTANifiEndpoint newSource)
    {
        source = newSource;
    }
    public CPTANifiEndpoint getDestination()
    {
        return destination;
    }

    public void setDestination(CPTANifiEndpoint newDestination)
    {
        destination = newDestination;
    }
    public List<String> getRelationships()
    {
        return selectedRelationships;
    }

    public void setRelationships(List<String> newRelationships)
    {
        selectedRelationships = newRelationships;
    }

    @Override
    protected JsonObjectBuilder getComponentAsJson()
    {
        JsonObjectBuilder componentAsJson = super.getComponentAsJson();
        componentAsJson.add("source", getSourceAsJson());
        // Add destination processor
        componentAsJson.add("destination", getDestinationAsJson());
        // Add relationships the connection represent
        JsonArrayBuilder relationshipsAsJson = Json.createArrayBuilder();
        for(String relationship: this.selectedRelationships)
        {
            relationshipsAsJson.add(relationship);
        }
        componentAsJson.add("selectedRelationships", relationshipsAsJson);
        
        return componentAsJson;
    }
    
    protected JsonObjectBuilder getSourceAsJson()
    {
        JsonObjectBuilder sourceAsJson = Json.createObjectBuilder();
        // Its a processor
        sourceAsJson.add("type", source.getEndpointType());
        sourceAsJson.add("id", source.getComponent().getID());
        sourceAsJson.add("groupId", source.getComponent().getProcessGroupID());
        
        return sourceAsJson;
    }
    
    protected JsonObjectBuilder getDestinationAsJson()
    {
        JsonObjectBuilder destinationAsJson = Json.createObjectBuilder();
        // Its a processor
        destinationAsJson.add("type", destination.getEndpointType());
        destinationAsJson.add("id", destination.getComponent().getID());
        destinationAsJson.add("groupId", destination.getComponent().getProcessGroupID());
        
        return destinationAsJson;
    }

    protected CPTANifiEndpoint source;
    protected CPTANifiEndpoint destination;
    protected List<String> selectedRelationships = new ArrayList<>();
    static Logger componentLogger = CPTALogger.getLogger();
}
