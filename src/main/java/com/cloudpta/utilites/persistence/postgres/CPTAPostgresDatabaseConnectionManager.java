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
package com.cloudpta.utilites.persistence.postgres;

import java.util.Properties;

import com.cloudpta.utilites.persistence.CPTADatabaseConnectionManager;
import com.cloudpta.utilites.persistence.CPTADatabaseConstants;

public class CPTAPostgresDatabaseConnectionManager  extends CPTADatabaseConnectionManager
{
    public CPTAPostgresDatabaseConnectionManager()
    {
        super();
        
        // add the prepared statements to the list
      
        // Add offset query fragment
        addPreparedStatementDefinition
                                     (
                                     CPTADatabaseConstants.OFFSET_QUERY_FRAGMENT_NAME, 
                                     CPTAPostgresPreparedStatementSQL.OFFSET_QUERY_FRAGMENT
                                     );
        addPreparedStatementDefinition
                                     (
                                     CPTADatabaseConstants.LIMIT_QUERY_FRAGMENT_NAME, 
                                     CPTAPostgresPreparedStatementSQL.LIMIT_QUERY_FRAGMENT
                                     );
        
    }
    
    @Override
    protected void createConfigurationFile(Properties connectionProperties)
    {        
        // Set the database driver
        connectionPoolConfiguration.setDriverClassName(CPTADatabaseConstants.DB_POSTGRES_DRIVER_PROPERTY_VALUE);
        
        // Let super class do the rest
        super.createConfigurationFile(connectionProperties);
    }    
}
