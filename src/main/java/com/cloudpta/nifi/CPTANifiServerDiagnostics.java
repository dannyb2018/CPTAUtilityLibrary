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
import jakarta.json.JsonObject;

/**
 *
 * @author Danny
 */
public class CPTANifiServerDiagnostics
{
    public void parseObject(JsonObject diagnosticsFromServer)
    {
        componentLogger.trace("diagnostics is " + diagnosticsFromServer.toString());
        // get diagnostics block
        JsonObject diagnosticsBlock = diagnosticsFromServer.getJsonObject("systemDiagnostics");
        
        // Get memory, processor block
        JsonObject memoryProcessorBlock = diagnosticsBlock.getJsonObject("aggregateSnapshot");
        usedMemory = memoryProcessorBlock.getString("usedHeap");
        freeMemory = memoryProcessorBlock.getString("freeHeap");
        double usedMemoryAsBytes = memoryProcessorBlock.getJsonNumber("usedHeapBytes").doubleValue();
        double totalMemoryAsBytes = memoryProcessorBlock.getJsonNumber("totalHeapBytes").doubleValue();
        usedMemoryPercentage = usedMemoryAsBytes/totalMemoryAsBytes;
        // Get processor utilization
        processorUsePercentage = memoryProcessorBlock.getJsonNumber("processorLoadAverage").doubleValue();
        
        // Get storage block
        JsonObject storageBlock = diagnosticsBlock.getJsonObject("aggregateSnapshot").getJsonArray("contentRepositoryStorageUsage").getJsonObject(0);
        freeStorage = storageBlock.getString("freeSpace");
        usedStorage = storageBlock.getString("usedSpace");
        double usedStorageAsBytes = storageBlock.getJsonNumber("usedSpaceBytes").doubleValue();
        double totalStorageAsBytes = storageBlock.getJsonNumber("totalSpaceBytes").doubleValue();
        usedStoragePercentage = usedStorageAsBytes/totalStorageAsBytes;                
    }
    public String freeStorage;
    public String usedStorage;
    public double usedStoragePercentage;
    
    public String freeMemory;
    public String usedMemory;
    public double usedMemoryPercentage;

    public double processorUsePercentage;
    static Logger componentLogger = CPTALogger.getLogger();                              
}
