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

import com.cloudpta.nifi.CPTANifiServer;
import com.cloudpta.nifi.connections.CPTAConnection;
import com.cloudpta.nifi.connections.CPTAOutputPort;
import com.cloudpta.nifi.controller_services.CPTAControllerService;
import com.cloudpta.nifi.processors.CPTAProcessor;
import com.cloudpta.nifi.processors.CPTAProcessorProperty;
import com.cloudpta.nifi.utilities.CPTANifiConstants;
import com.cloudpta.utilites.exceptions.CPTAException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 *
 * @author Danny
 */
public class CPTAProcessGroupTest
{    
    /**
     * Test of getTopLevelProcessGroup method, of class QPProcessGroup.
     */
/*    @org.junit.jupiter.api.Test
    public void testGetTopLevelProcessGroup() throws CPTAException, SQLException
    {
        Properties connectionProperties = new Properties();
        connectionProperties.put(QPPersistenceConstants.DB_JDBC_URL_PROPERTY_NAME, jdbcURL);
        connectionProperties.put(QPPersistenceConstants.DB_PASSWORD_PROPERTY_NAME, password);
        connectionProperties.put(QPPersistenceConstants.DB_USERNAME_PROPERTY_NAME, username);
        // Add postgres test connection
        QPDatabaseConnectionManager.addConnectionManager("test_quantpipeline", QPPersistenceConstants.DB_POSTGRES_CONNECTION_TYPE, connectionProperties);  
        
        QPAdministrationStore store = QPAdministrationStore.getNamedInstance("test_quantpipeline");
        QPNifiServer.setStore(store);
        System.out.println("getTopLevelProcessGroup");
        QPProcessGroup expResult = new QPProcessGroup();
        expResult.getComponent().setID("root");
        QPProcessGroup subgroup = new QPProcessGroup();
        subgroup.getRevision().setVersion(0);
        subgroup.getComponent().setProcessGroupID("root");
        subgroup.getComponent().setName("testpg");
        subgroup.persist();
        
        QPControllerService cs = new QPControllerService();
        cs.getRevision().setClientID(UUID.randomUUID().toString());
        cs.getRevision().setVersion(0);
        cs.getComponent().setName("my writer");
        cs.getComponent().setProcessGroupID(subgroup.getComponent().getID());
        cs.getComponent().setControllerServiceType(QPNifiConstants.CSV_RECORD_WRITER_CONTROLLER_SERVICE_TYPE);
        cs.persist();
        cs.changeState(QPNifiConstants.STATUS_ENABLED);
        QPControllerService cs1 = new QPControllerService();
        cs1.getRevision().setClientID(UUID.randomUUID().toString());
        cs1.getRevision().setVersion(0);
        cs1.getComponent().setName("my reader");
        cs1.getComponent().setProcessGroupID(subgroup.getComponent().getID());
        cs1.getComponent().setControllerServiceType(QPNifiConstants.CSV_READER_CONTROLLER_SERVICE_TYPE);
        cs1.persist();
        cs1.changeState(QPNifiConstants.STATUS_ENABLED);
        QPProcessor q = new QPProcessor();
        q.getComponent().setName("get file");
        q.getRevision().setClientID(UUID.randomUUID().toString());
        q.getComponent().setProcessGroupID(subgroup.getComponent().getID());
        q.getComponent().setSchedule("30 sec");
        q.getComponent().setSchedulingType(QPNifiConstants.SCHEDULER_TYPE_TIMER_DRIVEN);
        q.getRevision().setVersion(0);
        q.getComponent().setProcessorType(QPNifiConstants.GET_FILE_PROCESSOR_TYPE);
        List<QPProcessorProperty> properties =new ArrayList<>();
        QPProcessorProperty prop = new QPProcessorProperty();
        prop.name = QPNifiConstants.GET_FILE_PROCESSOR_INPUT_DIRECTORY;
        prop.value = "c:\\unittest\\t";
        properties.add(prop);
        q.getComponent().setProperties(properties);
        q.persist();

        QPProcessor q1 = new QPProcessor();
        q1.getComponent().setName("put on kafka");
        q1.getRevision().setClientID(UUID.randomUUID().toString());
        q1.getComponent().setProcessGroupID(subgroup.getComponent().getID());
        q1.getComponent().setSchedule("1 sec");
        q1.getComponent().setSchedulingType(QPNifiConstants.SCHEDULER_TYPE_TIMER_DRIVEN);
        q1.getRevision().setVersion(0);
        q1.getComponent().setProcessorType(QPNifiConstants.PUBLISH_KAFKA_PROCESSOR_TYPE);
        q1.autoTerminateRelationship("failure");
        List<QPProcessorProperty> properties1 =new ArrayList<>();
        QPProcessorProperty prop1 = new QPProcessorProperty();
        prop1.name = "record-writer";
        prop1.value = cs.getComponent().getID();
        properties1.add(prop1);
        QPProcessorProperty prop12 = new QPProcessorProperty();
        prop12.name = "record-reader";
        prop12.value = cs1.getComponent().getID();
        properties1.add(prop12);
        //KAFKA_PROCESSOR_PROPERTIES_DELIVERY_GUARANTY
        QPProcessorProperty prop13 = new QPProcessorProperty();
        prop13.name = QPNifiConstants.KAFKA_PROCESSOR_PROPERTY_DELIVERY_GUARANTY;
        prop13.value = QPNifiConstants.KAFKA_PROCESSOR_DELIVERY_GUARANTY_MULTIPLE_NODE_GUARANTEE;
        properties1.add(prop13);
        //KAFKA_PROCESSOR_PROPERTY_TOPIC
        QPProcessorProperty prop14 = new QPProcessorProperty();
        prop14.name = QPNifiConstants.KAFKA_PROCESSOR_PROPERTY_TOPIC;
        prop14.value = "test topic";
        properties1.add(prop14);
        q1.getComponent().setProperties(properties1);
        q1.persist();
        
        QPConnection connection  = new QPConnection();
        connection.getComponent().setProcessGroupID(subgroup.getComponent().getID());
        List<String> relationships = new ArrayList<>();
        relationships.add("success");
        connection.getComponent().setRelationships(relationships);
        connection.getComponent().setSource(q);
        connection.getComponent().setDestination(q1);
        connection.getRevision().setClientID(UUID.randomUUID().toString());
        connection.getRevision().setVersion(0);
        connection.getComponent().setName("test connection");
        connection.persist();
        QPOutputPort port = new QPOutputPort();
        port.getRevision().setClientID(UUID.randomUUID().toString());
        port.getRevision().setVersion(0);
        port.getComponent().setProcessGroupID(subgroup.getComponent().getID());
        port.getComponent().setName("test output");
        port.persist();
        QPConnection connection1  = new QPConnection();
        connection1.getComponent().setProcessGroupID(subgroup.getComponent().getID());
        List<String> relationships1 = new ArrayList<>();
        relationships1.add("success");
        connection1.getComponent().setRelationships(relationships);
        connection1.getComponent().setSource(q1);
        connection1.getComponent().setDestination(port);
        connection1.getRevision().setClientID(UUID.randomUUID().toString());
        connection1.getRevision().setVersion(0);
        connection1.getComponent().setName("test connection");
        connection1.persist();
//        port.changeState(QPNifiConstants.STATUS_RUNNING);
  //      port.changeState(QPNifiConstants.STATUS_STOPPED);
    //    port.changeState(QPNifiConstants.STATUS_ENABLED);
        // expResult.setID(UUID.randomUUID().toString());
      //  expResult.setName("Input/test1");
      //  expResult.store();
      //  QPProcessGroup result = QPProcessGroup.getTopLevelProcessGroup();
    }
    static String jdbcURL = "jdbc:postgresql://localhost:5432/test";
    static String username = "postgres";
    static String password = "w0bble";    
  */  
}
