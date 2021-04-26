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
