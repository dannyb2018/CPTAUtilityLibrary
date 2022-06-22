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

import com.cloudpta.nifi.CPTANifiServer;
import com.cloudpta.utilites.exceptions.CPTAException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Danny
 */
public class CPTAPortComponentTest
{
    /**
     * Test of getPortType method, of class QPPortComponent.
     */
/*    @Test
    public void testGetSetPortType() throws CPTAException, SQLException
    {
        System.out.println("getPortType");
        Properties connectionProperties = new Properties();
        connectionProperties.put(QPPersistenceConstants.DB_JDBC_URL_PROPERTY_NAME, jdbcURL);
        connectionProperties.put(QPPersistenceConstants.DB_PASSWORD_PROPERTY_NAME, password);
        connectionProperties.put(QPPersistenceConstants.DB_USERNAME_PROPERTY_NAME, username);
        // Add postgres test connection
        QPDatabaseConnectionManager.addConnectionManager("test_quantpipeline", QPPersistenceConstants.DB_POSTGRES_CONNECTION_TYPE, connectionProperties);  
        
        QPAdministrationStore store = QPAdministrationStore.getNamedInstance("test_quantpipeline");
        QPNifiServer.setStore(store);

        QPInputPort inputPort = new QPInputPort();        
        QPPortComponent instance = inputPort.getComponent();
        String expResult = "INPUT_PORT";
        String result = instance.getPortType();
        assertEquals(expResult, result);
        QPOutputPort outputPort = new QPOutputPort();        
        instance = outputPort.getComponent();
        expResult = "OUTPUT_PORT";
        result = instance.getPortType();
        assertEquals(expResult, result);
    }

    /**
     * Test of getComponentAsJson method, of class QPPortComponent.
     */
/*    @Test
    public void testGetComponentAsJson() throws CPTAException, SQLException
    {
        System.out.println("getComponentAsJson");
        Properties connectionProperties = new Properties();
        connectionProperties.put(QPPersistenceConstants.DB_JDBC_URL_PROPERTY_NAME, jdbcURL);
        connectionProperties.put(QPPersistenceConstants.DB_PASSWORD_PROPERTY_NAME, password);
        connectionProperties.put(QPPersistenceConstants.DB_USERNAME_PROPERTY_NAME, username);
        // Add postgres test connection
        QPDatabaseConnectionManager.addConnectionManager("test_quantpipeline", QPPersistenceConstants.DB_POSTGRES_CONNECTION_TYPE, connectionProperties);  
        
        QPAdministrationStore store = QPAdministrationStore.getNamedInstance("test_quantpipeline");
        QPNifiServer.setStore(store);

        QPInputPort port = new QPInputPort();
        port.getRevision().setVersion(1);
        QPPortComponent instance = port.getComponent();
        // Set name id and process group
        instance.setID(UUID.randomUUID().toString());
        instance.setName(UUID.randomUUID().toString());
        instance.setProcessGroupID(UUID.randomUUID().toString());
        JsonObjectBuilder result = instance.getComponentAsJson();
        System.out.println(result.build().toString());
        JsonObjectBuilder objectAsJsonBuilder = Json.createObjectBuilder();
        port.addToObject(objectAsJsonBuilder);
        JsonObject objectAsJson = objectAsJsonBuilder.build();
        String objectAsJsonString = objectAsJson.toString();
        System.out.println(objectAsJsonString);
    }
    
    static String jdbcURL = "jdbc:postgresql://localhost:5432/test";
    static String username = "postgres";
    static String password = "w0bble";    
*/
}
