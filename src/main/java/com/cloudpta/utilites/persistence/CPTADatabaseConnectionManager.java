/*

Copyright 2017-2019 Advanced Products Limited, 
dannyb@cloudpta.com
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
package com.cloudpta.utilites.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import com.cloudpta.utilites.exceptions.CPTAException;
import com.cloudpta.utilites.persistence.postgres.CPTAPostgresDatabaseConnectionManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class CPTADatabaseConnectionManager  implements AutoCloseable
{
    public static CPTADatabaseConnectionManager getDatabaseConnectionManager(String name) throws CPTAException
    {
        // Get the connection by name
        CPTADatabaseConnectionManager connectionManager = instances.get(name);        
                        
        // Hand it over
        return connectionManager;
    }
    
    public static Properties getDatabaseProperties() throws CPTAException
    {
        Properties serverDatabaseSettings = null;
        
        try
        {
            // Look up database settings
            serverDatabaseSettings = new Properties();
            String databaseConnectionName = System.getenv(CPTADatabaseConstants.DB_CONNECTION_NAME);
            serverDatabaseSettings.put(CPTADatabaseConstants.DB_CONNECTION_NAME, databaseConnectionName);
            String databaseConnectionType = System.getenv( CPTADatabaseConstants.DB_CONNECTION_TYPE);
            serverDatabaseSettings.put(CPTADatabaseConstants.DB_CONNECTION_TYPE, databaseConnectionType);
            String databaseJdbcUrl = System.getenv( CPTADatabaseConstants.DB_JDBC_URL_PROPERTY_NAME);
            serverDatabaseSettings.put(CPTADatabaseConstants.DB_JDBC_URL_PROPERTY_NAME, databaseJdbcUrl);
            String databaseUser = System.getenv( CPTADatabaseConstants.DB_USERNAME_PROPERTY_NAME);
            serverDatabaseSettings.put(CPTADatabaseConstants.DB_USERNAME_PROPERTY_NAME, databaseUser);
            String databasePassword = System.getenv( CPTADatabaseConstants.DB_PASSWORD_PROPERTY_NAME);            
            serverDatabaseSettings.put(CPTADatabaseConstants.DB_PASSWORD_PROPERTY_NAME, databasePassword);
        }
        catch(Exception E)
        {
            CPTAException standardisedException = new CPTAException(E);
            throw standardisedException;
        }
        
        return serverDatabaseSettings;
    }

    public CPTADatabaseConnection getConnection() throws SQLException
    {
        Connection dbConnection = datasource.getConnection(); 
        
        CPTADatabaseConnection connection = new CPTADatabaseConnection(dbConnection, preparedStatements);
        
        return connection;
    }
    
    public final void addPreparedStatementDefinition(String statementName, String statementSQL)
    {
        // If not already stored
        String existingSQL = preparedStatements.get(statementName);
        if(null == existingSQL)
        {
            // add to list
            preparedStatements.put(statementName, statementSQL);
        }
    }

    public static void addConnectionManager(String name, String databaseType, Properties connectionProperties) throws SQLException
    {
        // Create a connection manager of this type
        CPTADatabaseConnectionManager connectionManager = null;
        // If it is postgres
        if( 0 == databaseType.compareTo(CPTADatabaseConstants.DB_POSTGRES_CONNECTION_TYPE) )
        {
            connectionManager = new CPTAPostgresDatabaseConnectionManager();
        }
        
        // intitialise it 
        connectionManager.initialise(connectionProperties);
        
        // Put it into the list
        CPTADatabaseConnectionManager.instances.put(name, connectionManager);
    }

    public static void addConnectionManager(String name, CPTADatabaseConnectionManager connectionManagerToAdd, Properties connectionProperties) throws SQLException
    {        
        // intitialise it 
        connectionManagerToAdd.initialise(connectionProperties);
        
        // Put it into the list
        CPTADatabaseConnectionManager.instances.put(name, connectionManagerToAdd);
    }
    
    protected void initialise(Properties connectionProperties) throws SQLException
    {
        // setting up the connection manager is two steps
        
        // 1) set the properties and set up the configuration
        createConfigurationFile(connectionProperties);

        // 2) create the data source
        datasource = new HikariDataSource(connectionPoolConfiguration);
    }
    
    protected void createConfigurationFile(Properties connectionProperties)
    {
        // Get connection settings
        String jdbcUrlPropertyValue = connectionProperties.getProperty(CPTADatabaseConstants.DB_JDBC_URL_PROPERTY_NAME);
        connectionPoolConfiguration.setJdbcUrl(jdbcUrlPropertyValue);
        String usernamePropertyValue = connectionProperties.getProperty(CPTADatabaseConstants.DB_USERNAME_PROPERTY_NAME);
        connectionPoolConfiguration.setUsername(usernamePropertyValue);
        String passwordPropertyValue = connectionProperties.getProperty(CPTADatabaseConstants.DB_PASSWORD_PROPERTY_NAME);
        connectionPoolConfiguration.setPassword(passwordPropertyValue);
        
        // Cache prepared statements properties
        String cachePrepStmtPropertyValue = connectionProperties.getProperty
                                                                           (
                                                                           CPTADatabaseConstants.DB_CACHE_PREP_STMTS_PROPERTY_NAME, 
                                                                           CPTADatabaseConstants.DB_CACHE_PREP_STMTS_PROPERTY_DEFAULT_VALUE
                                                                           );
        connectionPoolConfiguration.addDataSourceProperty
                                                        ( 
                                                        CPTADatabaseConstants.DB_CACHE_PREP_STMTS_PROPERTY_NAME, 
                                                        cachePrepStmtPropertyValue 
                                                        );
        String prepStmtCacheSizePropertyValue = connectionProperties.getProperty
                                                                           (
                                                                            CPTADatabaseConstants.DB_PREP_STMT_CACHE_SIZE_PROPERTY_NAME, 
                                                                            CPTADatabaseConstants.DB_PREP_STMT_CACHE_SIZE_PROPERTY_DEFAULT_VALUE
                                                                           );
        connectionPoolConfiguration.addDataSourceProperty
                                                        ( 
                                                        CPTADatabaseConstants.DB_PREP_STMT_CACHE_SIZE_PROPERTY_NAME, 
                                                        prepStmtCacheSizePropertyValue 
                                                        );
        String prepStmtCacheSQLLimitPropertyValue = connectionProperties.getProperty
                                                                           (
                                                                            CPTADatabaseConstants.DB_PREP_STMT_CACHE_SQL_LIMIT_PROPERTY_NAME, 
                                                                            CPTADatabaseConstants.DB_PREP_STMT_CACHE_SQL_LIMIT_PROPERTY_DEFAULT_VALUE
                                                                           );
        connectionPoolConfiguration.addDataSourceProperty
                                                        ( 
                                                        CPTADatabaseConstants.DB_PREP_STMT_CACHE_SQL_LIMIT_PROPERTY_NAME, 
                                                        prepStmtCacheSQLLimitPropertyValue 
                                                        );
    }

    @Override
    public void close() throws Exception
    {
        // shutdown datasource
        datasource.close();
    }
    
    protected HikariConfig connectionPoolConfiguration = new HikariConfig();
    protected HikariDataSource datasource = null;
    protected HashMap<String, String> preparedStatements = new HashMap<>();
    
    // List of instances
    static HashMap<String, CPTADatabaseConnectionManager> instances = new HashMap<>();
}
