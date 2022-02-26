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
package com.cloudpta.utilites;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.Properties;

import com.cloudpta.utilites.exceptions.CPTAException;

public class CPTAIniFileParser 
{
    public static Properties getPropertiesFromIniFile(String nameOfFile) throws CPTAException
    {
        // This is the properties from the ini file
        Properties propertiesFromIniFile = new Properties();
        
        try
        {
            // get the ini file location
            File iniFile = new File(nameOfFile);
            FileReader fr = new FileReader(iniFile);   
            //reads the file  
            BufferedReader iniFileReader =new BufferedReader(fr); 

            // Read line after line
            String nextLine = iniFileReader.readLine();
            while(null != nextLine)
            {
                // Split with first equals sign
                String[] tokens = nextLine.split("=", 2);
                if(2 == Array.getLength(tokens))
                {
                    String propertyName = tokens[0].trim();
                    String propertyValue = tokens[1].trim();
                    propertiesFromIniFile.setProperty(propertyName, propertyValue);
                
                }
                // next line
                nextLine = iniFileReader.readLine();
            }
        }
        catch(Exception E)
        {
            CPTAException wrappedException = new CPTAException(E);
            throw wrappedException;
        }
        
        return propertiesFromIniFile;
    }      
}
