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

import com.cloudpta.nifi.CPTANifiServer;
import com.cloudpta.nifi.process_groups.CPTAProcessGroup;
import com.cloudpta.nifi.utilities.CPTANifiConstants;
import com.cloudpta.utilites.exceptions.CPTAException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Danny
 */
public class CPTAProcessorTest
{

/*    @Test
    public void testCreateStartStop() throws CPTAException, SQLException
    {
        Properties connectionProperties = new Properties();
        connectionProperties.put(QPPersistenceConstants.DB_JDBC_URL_PROPERTY_NAME, jdbcURL);
        connectionProperties.put(QPPersistenceConstants.DB_PASSWORD_PROPERTY_NAME, password);
        connectionProperties.put(QPPersistenceConstants.DB_USERNAME_PROPERTY_NAME, username);
        // Add postgres test connection
        QPDatabaseConnectionManager.addConnectionManager("test_quantpipeline", QPPersistenceConstants.DB_POSTGRES_CONNECTION_TYPE, connectionProperties);  
        
        QPAdministrationStore store = QPAdministrationStore.getNamedInstance("test_quantpipeline");
        QPNifiServer.setStore(store);
        
        QPProcessGroup parentGroup = QPProcessGroup.getTopLevelProcessGroup();
        // Create the processor
        QPProcessor generateRequestProcessor = new QPProcessor();
        generateRequestProcessor.setProcessorType("org.apache.nifi.processors.standard.GenerateFlowFile");
        // Name is qp-trigger-source_category-source_name
        String sourceProcessorName = "qp-trigger-test-test";   
        generateRequestProcessor.setName(sourceProcessorName);
        // set the process group
        generateRequestProcessor.setProcessGroup(parentGroup);        
        // Finally set all the static properties that dont change
        // Really we want this to be a one time shot
        generateRequestProcessor.getComponent().setSchedule("1000000 sec");
        generateRequestProcessor.getComponent().setSchedulingType(QPNifiConstants.SCHEDULER_TYPE_TIMER_DRIVEN);
        // Set revision, version 0 and client id
        generateRequestProcessor.getRevision().setVersion(0);
        generateRequestProcessor.getRevision().setClientID("test");
        
        // Only property is custom text
        List<QPProcessorProperty> triggerProperties = new ArrayList<>();
        QPProcessorProperty customTextProperty = new QPProcessorProperty();        
        customTextProperty.name = "generate-ff-custom-text";
        customTextProperty.value = "{test:\"test\"}";
        triggerProperties.add(customTextProperty);
        generateRequestProcessor.setProperties(triggerProperties);
        
        generateRequestProcessor.persist();
    }

    static String jdbcURL = "jdbc:postgresql://localhost:5432/test";
    static String username = "postgres";
    static String password = "w0bble";
    */
}
