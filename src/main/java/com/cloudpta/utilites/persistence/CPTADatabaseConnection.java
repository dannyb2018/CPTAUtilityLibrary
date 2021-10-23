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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.cloudpta.utilites.exceptions.CPTAException;
import com.cloudpta.utilites.logging.CPTALogger;

import ch.qos.logback.classic.Logger;

public class CPTADatabaseConnection implements AutoCloseable
{
    public CPTADatabaseConnection(Connection theDBConnection, HashMap<String, String> originalPreparedStatementMap) throws SQLException
    {        
        dbConnection = theDBConnection;
        // start transaction 
        dbConnection.setAutoCommit(false);
        copyOfPreparedStatementMap = originalPreparedStatementMap;
    }
    
    public PreparedStatement getStatementFromSQL(String statementSQL) throws SQLException
    {
        // Assume something goes wrong
        PreparedStatement desiredStatement = null;
        
        // Prepare it
        dbConnectionLogger.trace("getting statement from sql " + statementSQL);
        desiredStatement = dbConnection.prepareStatement(statementSQL);
        dbConnectionLogger.trace("got statement from sql " + desiredStatement.toString());
        // Add to list
        statements.add(desiredStatement);
        
        // return the prepared statement
        return desiredStatement;                
    }
    
    public PreparedStatement getStatement(String statementName) throws SQLException, CPTAException
    {
        // Assume something goes wrong
        PreparedStatement desiredStatement = null;
        
        // get the statement sql
        dbConnectionLogger.trace("getting statement by name " + statementName);
        String preparedStatementSQL = copyOfPreparedStatementMap.get(statementName);
        dbConnectionLogger.trace("got statement sql by name " + preparedStatementSQL);
        // If it is null
        if( null == preparedStatementSQL )
        {
            // throw no such statment exception
            String error = "statement " + statementName + " is not defined";
            CPTAException standardisedException = new CPTAException(error);
            dbConnectionLogger.error("could not get statement " + standardisedException.getErrors().toString());
            throw standardisedException;
        }
        
        // Prepare it
        desiredStatement = dbConnection.prepareStatement(preparedStatementSQL);
        dbConnectionLogger.trace("got statement by name " + desiredStatement.toString());
        
        // Add to list
        statements.add(desiredStatement);
        
        // return the prepared statement
        return desiredStatement;        
    }
    
    public CallableStatement getCallableStatement(String statementName) throws SQLException, CPTAException
    {
        // Assume something goes wrong
        CallableStatement desiredStatement = null;
        
        // get the statement sql
        dbConnectionLogger.trace("getting callable statement by name " + statementName);
        String callableStatementSQL = copyOfPreparedStatementMap.get(statementName);
        dbConnectionLogger.trace("got callable statement sql by name " + callableStatementSQL);
        // If it is null
        if( null == callableStatementSQL )
        {
            // throw no such statment exception
            String error = "callable statement " + statementName + " is not defined";
            CPTAException standardisedException = new CPTAException(error);
            dbConnectionLogger.error("could not get callable statement " + standardisedException.getErrors().toString());
            throw standardisedException;
        }
        
        // Prepare it
        desiredStatement = dbConnection.prepareCall(callableStatementSQL);
        dbConnectionLogger.trace("got callable statement by name " + desiredStatement.toString());
        
        // Add to list
        statements.add(desiredStatement);
        
        // return the prepared statement
        return desiredStatement;                
    }
    
    public String getStatementSQL(String statementName) throws SQLException, CPTAException
    {        
        // get the statement sql
        dbConnectionLogger.trace("getting statement sql by name " + statementName);
        String preparedStatementSQL = copyOfPreparedStatementMap.get(statementName);
        // If it is null
        if( null == preparedStatementSQL )
        {
            // throw no such statment exception
            String error = "statement " + statementName + " is no defined";
            CPTAException standardisedException = new CPTAException(error);
            dbConnectionLogger.error("could not get statement sql " + standardisedException.getErrors().toString());
            throw standardisedException;
        }
        
        
        // return the prepared statementSQL
        return preparedStatementSQL;        
    }

    public String getOffsetSQL() throws SQLException, CPTAException
    {
        String offsetSQL = copyOfPreparedStatementMap.get(CPTADatabaseConstants.OFFSET_QUERY_FRAGMENT_NAME);
        // If it is null
        if( null == offsetSQL )
        {
            // throw no such statment exception
            String error = "offset sql statement is not defined";
            CPTAException standardisedException = new CPTAException(error);
            throw standardisedException;
        }
        
        
        // return the offset  sql
        return offsetSQL;        
    }
    
    public String getLimitSQL() throws SQLException, CPTAException
    {
        String limitSQL = copyOfPreparedStatementMap.get(CPTADatabaseConstants.LIMIT_QUERY_FRAGMENT_NAME);
        // If it is null
        if( null == limitSQL )
        {
            // throw no such statment exception
            String error = "limit sql statement is not defined";
            CPTAException standardisedException = new CPTAException(error);
            throw standardisedException;
        }
        
        
        // return the  limit sql
        return limitSQL;        
    }

    public ResultSet getResultSet(PreparedStatement statement) throws SQLException
    {
        ResultSet result = statement.executeQuery();
        // add to list 
        resultSets.add(result);
        
        return result;
    }
    
    @Override
    public void close() throws Exception
    {
        // If we didnt complete properly
        if(true == shouldRollback)
        {
            try
            {
                dbConnection.rollback();
            }
            catch(SQLException E)
            {
                CPTAException standardException = new CPTAException(E);
                throw standardException;
            }
        }
        
        // Loop through the result sets
        for(ResultSet currentResultSet: resultSets)
        {
            try
            {
                currentResultSet.close();
            }
            catch(Exception E)
            {
                CPTAException standardException = new CPTAException(E);
                throw standardException;
            }
        }
        // Loop through the preparedStatements
        for(PreparedStatement currentStatement: statements)
        {
            try
            {
                currentStatement.close();
            }
            catch(Exception E)
            {
                CPTAException standardException = new CPTAException(E);
                throw standardException;
            }
        }
        // close the connection
        try
        {
            dbConnection.close();
        }
        catch(Exception E)
        {
            CPTAException standardException = new CPTAException(E);
            throw standardException;
        }
    }
    
    public void commit() throws SQLException
    {
        dbConnection.commit();
        // Once commited ok
        // shouldnt rollback
        shouldRollback = false;
    }
    
    public Connection getRawConnection()
    {
        return dbConnection;
    }
    
    static Logger dbConnectionLogger = CPTALogger.getLogger();
    
    boolean shouldRollback = true;
    List<ResultSet> resultSets = new ArrayList<>();
    List<PreparedStatement> statements = new ArrayList<>();
    Connection dbConnection = null;
    protected HashMap<String, String> copyOfPreparedStatementMap = new HashMap<>();
}
