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
package com.cloudpta.utilites.exceptions;

import com.cloudpta.utilites.CPTAUtilityConstants;
import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Danny
 */
public class CPTAException extends RuntimeException implements GraphQLError
{

    @Override
    public Map<String, Object> getExtensions() 
    {
        Map<String, Object> customAttributes = new LinkedHashMap<>();

        customAttributes.put("errorMessage", this.getMessage());

        return customAttributes;
    }

    @Override
    public ErrorClassification getErrorType()
    {
        return ErrorType.DataFetchingException;
    }

    @Override
    public List<SourceLocation> getLocations()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CPTAException(Throwable originalException)
    {
        super(originalException);
        
        // Get the message
        exceptionMessage = originalException.getMessage();
        if(null == exceptionMessage)
        {
            // get exception type instead
            exceptionMessage = originalException.toString();
        }        
        
        
        parseStackTrace(originalException.getStackTrace());
    }
    
    public CPTAException(String newExceptionMessage)
    {
        super(newExceptionMessage);

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        parseStackTrace(elements);
        exceptionMessage = newExceptionMessage;
    }
    
    public CPTAException(JsonObject errors)
    {
        exceptionMessage = errors.getString(CPTAUtilityConstants.EXCEPTION_MESSAGE_FIELD);
        firstRelevantLineError = errors.getString(CPTAUtilityConstants.ERROR_LINE_MESSAGE_FIELD);
        stackTrace = errors.getString(CPTAUtilityConstants.STACK_TRACE_FIELD);
    }
    
    @Override
    public String getMessage()
    {
        return exceptionMessage;
    }
    
    public JsonObject getErrors()
    {
        JsonObjectBuilder errorBuilder = Json.createObjectBuilder();
        
        errorBuilder.add(CPTAUtilityConstants.EXCEPTION_MESSAGE_FIELD, exceptionMessage);
        errorBuilder.add(CPTAUtilityConstants.ERROR_LINE_MESSAGE_FIELD, firstRelevantLineError);
        errorBuilder.add(CPTAUtilityConstants.STACK_TRACE_FIELD, stackTrace);
        
        return errorBuilder.build();
    }
    
    protected void parseStackTrace(StackTraceElement[] elements)
    {
        // Get first place in CPTA or QP code the error happens on        
        int numberOfStackTraceElements = Array.getLength(elements);
        int i = 1;
        for(i = 1; i < numberOfStackTraceElements; i++ )
        {
            stackTrace = stackTrace + "\tat " + elements[i].toString() + "\r\n";
            String fileErrorHappenedIn = elements[i].getFileName();
            
            // If the file starts with QP or CPTA
            if((true == fileErrorHappenedIn.startsWith("QP"))||(true == fileErrorHappenedIn.startsWith("CPTA")))
            {
                // Store details here
                firstRelevantLineError = elements[i].toString();
                break;
            }
        }
        for(; i < numberOfStackTraceElements; i++ )
        {
            // Keep doing the rest of the stack
            stackTrace = stackTrace + "\tat " + elements[i].toString() + "\r\n";
        }
    }
    
    protected String exceptionMessage = "";
    protected String firstRelevantLineError = "";
    protected String stackTrace = "";
}
