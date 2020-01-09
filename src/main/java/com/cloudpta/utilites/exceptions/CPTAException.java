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

import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;

/**
 *
 * @author Danny
 */
public class CPTAException extends Exception
{
    public CPTAException(Exception originalException)
    {
        // Get the message
        String exceptionMessage = originalException.getMessage();
        // Add it to errors
        errors = new ArrayList<>();
        errors.add(exceptionMessage);
    }
    
    public CPTAException(List<String> errorsForThisException)
    {
        errors = errorsForThisException;
    }
    
    public List<String> getErrors()
    {
        return errors;
    }
    
    public String getErrorsAsString()
    {
        JsonArrayBuilder errorsBuilder = Json.createArrayBuilder();
        for(String currentError : errors)
        {
            errorsBuilder.add(currentError);
        }
        
        String errorsAsString = errorsBuilder.build().toString();
        return errorsAsString;
    }
    protected List<String> errors;
}
