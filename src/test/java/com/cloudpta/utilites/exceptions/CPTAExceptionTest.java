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

import jakarta.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Danny
 */
public class CPTAExceptionTest
{
    /**
     * Test of getErrors method, of class CPTAException.
     */
    @Test
    public void testGetErrors()
    {
        try
        {
            String x = null;
            x.getBytes();
        }
        catch(NullPointerException e)
        {
            CPTAException instance = new CPTAException(e);
            JsonObject errors = instance.getErrors();
            System.out.println(errors.toString());
        }
        
        Exception e = new Exception("test");
        CPTAException instance = new CPTAException(e);
        JsonObject errors = instance.getErrors();
        System.out.println(errors.toString());
        
        List<String> testArray = new ArrayList<>();
        try
        {
        testArray.get(0);
        }
        catch(Exception e2)
        {
        instance = new CPTAException(e2);
        errors = instance.getErrors();
        System.out.println(errors.toString());
        
            
        }
    }
    
}
