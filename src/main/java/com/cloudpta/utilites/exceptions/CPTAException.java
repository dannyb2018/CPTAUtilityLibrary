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
package com.cloudpta.utilites.exceptions;

import com.cloudpta.utilites.CPTAUtilityConstants;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author Danny
 */
public class CPTAException extends Exception
{
    public CPTAException(Exception originalException)
    {
        // Get the message
        exceptionMessage = originalException.getMessage();
        if(null == exceptionMessage)
        {
            // get exception type instead
            exceptionMessage = originalException.toString();
        }
        
        // Get first place in CPTA or QP code the error happens on        
        StackTraceElement[] elements = originalException.getStackTrace();
        int numberOfStackTraceElements = Array.getLength(elements);
        for(int i = 0; i < numberOfStackTraceElements; i++ )
        {
            String fileErrorHappenedIn = elements[i].getFileName();
            // If the file starts with QP or CPTA
            if((true == fileErrorHappenedIn.startsWith("QP"))||(true == fileErrorHappenedIn.startsWith("CPTA")))
            {
                // Store details here
                firstRelevantLineError = elements[i].toString();
                break;
            }
        }
        // Get the stack trace
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        originalException.printStackTrace(pw);
        stackTrace = sw.toString();
    }
    
    public CPTAException(String newExceptionMessage, String newFirstRelevantLineError, String newStackTrace)
    {
        exceptionMessage = newExceptionMessage;
        firstRelevantLineError = newFirstRelevantLineError;
        stackTrace = newStackTrace;        
    }
    
    public CPTAException(JsonObject errors)
    {
        exceptionMessage = errors.getString(CPTAUtilityConstants.EXCEPTION_MESSAGE_FIELD);
        firstRelevantLineError = errors.getString(CPTAUtilityConstants.ERROR_LINE_MESSAGE_FIELD);
        stackTrace = errors.getString(CPTAUtilityConstants.STACK_TRACE_FIELD);
    }
    
    
    public JsonObject getErrors()
    {
        JsonObjectBuilder errorBuilder = Json.createObjectBuilder();
        
        errorBuilder.add(CPTAUtilityConstants.EXCEPTION_MESSAGE_FIELD, exceptionMessage);
        errorBuilder.add(CPTAUtilityConstants.ERROR_LINE_MESSAGE_FIELD, firstRelevantLineError);
        errorBuilder.add(CPTAUtilityConstants.STACK_TRACE_FIELD, stackTrace);
        
        return errorBuilder.build();
    }
    
    protected String exceptionMessage = "";
    protected String firstRelevantLineError = "";
    protected String stackTrace = "";
}
