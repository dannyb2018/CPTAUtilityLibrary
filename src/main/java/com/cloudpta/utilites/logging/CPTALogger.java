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
package com.cloudpta.utilites.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.cloudpta.utilites.CPTAUtilityConstants;
import java.util.Properties;


/**
 *
 * @author Danny
 */
public class CPTALogger
{
    public static void initialise()
    {
        if( null == context)
        {
            // Set root logger to off
            LoggerContext loggerContext = new LoggerContext();
            ContextInitializer contextInitializer = new ContextInitializer(loggerContext);
            Logger rootLogger = (ch.qos.logback.classic.Logger)loggerContext.getLogger("ROOT");
            rootLogger.setLevel(Level.OFF);
            
            // get rid of any old appenders
            apiServerLogger.detachAndStopAllAppenders();
            
            context = apiServerLogger.getLoggerContext();
            // get pattern is common to both appenders
            String logPattern = CPTALogger.getPropertyValue
                                                          (
                                                          CPTAUtilityConstants.LOG_PATTERN_PROPERTY, 
                                                          CPTAUtilityConstants.LOG_PATTERN_PROPERTY_DEFAULT
                                                          );
            PatternLayoutEncoder layout = new PatternLayoutEncoder();
            layout.setPattern(logPattern);
            layout.setContext(context);
            layout.setImmediateFlush(true);
            layout.start();
            // get threshold
            String threshold = CPTALogger.getPropertyValue
                                                         (
                                                         CPTAUtilityConstants.LOG_THRESHOLD_PROPERTY, 
                                                         CPTAUtilityConstants.LOG_THRESHOLD_PROPERTY_DEFAULT
                                                         );
            loggerLevel = property2Level(threshold);
            apiServerLogger.setLevel(loggerLevel);
            
            // If we need a console logging
            String logToConsole = CPTALogger.getPropertyValue
                                                            (
                                                            CPTAUtilityConstants.LOG_SHOULD_USE_CONSOLE_PROPERTY, 
                                                            CPTAUtilityConstants.LOG_SHOULD_USE_CONSOLE_PROPERTY_DEFAULT
                                                            );
            
            if(0 == logToConsole.compareTo("Y"))
            {
                consoleAppender = new ConsoleAppender<>();
                consoleAppender.setEncoder(layout);
                consoleAppender.setImmediateFlush(true);
                consoleAppender.setContext(context);
                apiServerLogger.addAppender(consoleAppender);
                consoleAppender.start();
            }

            // If we need a file logger
            String logToFile = CPTALogger.getPropertyValue
                                                         (
                                                         CPTAUtilityConstants.LOG_SHOULD_USE_FILE_PROPERTY, 
                                                         CPTAUtilityConstants.LOG_SHOULD_USE_FILE_PROPERTY_DEFAULT
                                                         );
            
            if(0 == logToFile.compareTo("Y"))
            {
                fileAppender = new RollingFileAppender<>();
                fileAppender.setEncoder(layout); 

                // Set up the rolling log policy
                String maxFileSize = CPTALogger.getPropertyValue
                                                               (
                                                               CPTAUtilityConstants.LOG_MAX_FILE_SIZE_PROPERTY, 
                                                               CPTAUtilityConstants.LOG_MAX_FILE_SIZE_PROPERTY_DEFAULT
                                                               );
                String filePattern = CPTALogger.getPropertyValue
                                                               (
                                                               CPTAUtilityConstants.LOG_FILE_NAME_PATTERN_PROPERTY, 
                                                               CPTAUtilityConstants.LOG_FILE_NAME_PATTERN_PROPERTY_DEFAULT
                                                               );
                filePattern = filePattern + "qpapiserver-%d{yyyy-MM-dd}.%i.log.gz";
                String maxHistory = CPTALogger.getPropertyValue
                                                              (
                                                              CPTAUtilityConstants.LOG_FILE_MAX_HISTORY_PROPERTY, 
                                                              CPTAUtilityConstants.LOG_FILE_MAX_HISTORY_PROPERTY_DEFAULT
                                                              );
                String totalMaxSize = CPTALogger.getPropertyValue
                                                                (
                                                                CPTAUtilityConstants.LOG_FILE_TOTAL_MAX_SIZE_PROPERTY, 
                                                                CPTAUtilityConstants.LOG_FILE_TOTAL_MAX_SIZE_PROPERTY_DEFAULT
                                                                );
                SizeAndTimeBasedRollingPolicy newLogFilePolicy = new SizeAndTimeBasedRollingPolicy();
                newLogFilePolicy.setMaxFileSize(FileSize.valueOf(maxFileSize));
                newLogFilePolicy.setFileNamePattern(filePattern);
                newLogFilePolicy.setMaxHistory(Integer.parseInt(maxHistory));
                newLogFilePolicy.setTotalSizeCap(FileSize.valueOf(totalMaxSize));      
                fileAppender.setRollingPolicy(newLogFilePolicy);
                fileAppender.setName("file_appender");

                fileAppender.setImmediateFlush(true);
                fileAppender.setAppend(true);
                fileAppender.setContext(context);
                
                newLogFilePolicy.setParent(fileAppender);
                newLogFilePolicy.setContext(context);
                apiServerLogger.addAppender(fileAppender);
                
                newLogFilePolicy.start();
                fileAppender.start();
                
            }
        }
    }

    public static void initialise( Properties loggerProperties )
    {
        if( null == context)
        {
            // Set root logger to off
            LoggerContext loggerContext = new LoggerContext();
            ContextInitializer contextInitializer = new ContextInitializer(loggerContext);
            Logger rootLogger = (ch.qos.logback.classic.Logger)loggerContext.getLogger("ROOT");
            rootLogger.setLevel(Level.OFF);
            
            // get rid of any old appenders
            apiServerLogger.detachAndStopAllAppenders();
            
            context = apiServerLogger.getLoggerContext();
            // get pattern is common to both appenders
            String logPattern = loggerProperties.getProperty
                                                        (
                                                        CPTAUtilityConstants.LOG_PATTERN_PROPERTY, 
                                                        CPTAUtilityConstants.LOG_PATTERN_PROPERTY_DEFAULT
                                                        );
            PatternLayoutEncoder layout = new PatternLayoutEncoder();
            layout.setPattern(logPattern);
            layout.setContext(context);
            layout.setImmediateFlush(true);
            layout.start();
            // get threshold
            String threshold = loggerProperties.getProperty
                                                        (
                                                        CPTAUtilityConstants.LOG_THRESHOLD_PROPERTY, 
                                                        CPTAUtilityConstants.LOG_THRESHOLD_PROPERTY_DEFAULT
                                                        );
            loggerLevel = property2Level(threshold);
            apiServerLogger.setLevel(loggerLevel);
            
            // If we need a console logging
            String logToConsole = loggerProperties.getProperty
                                                        (
                                                        CPTAUtilityConstants.LOG_SHOULD_USE_CONSOLE_PROPERTY, 
                                                        CPTAUtilityConstants.LOG_SHOULD_USE_CONSOLE_PROPERTY_DEFAULT
                                                        );
            
            if(0 == logToConsole.compareTo("Y"))
            {
                consoleAppender = new ConsoleAppender<>();
                consoleAppender.setEncoder(layout);
                consoleAppender.setImmediateFlush(true);
                consoleAppender.setContext(context);
                apiServerLogger.addAppender(consoleAppender);
                consoleAppender.start();
            }

            // If we need a file logger
            String logToFile = loggerProperties.getProperty
                                                        (
                                                        CPTAUtilityConstants.LOG_SHOULD_USE_FILE_PROPERTY, 
                                                        CPTAUtilityConstants.LOG_SHOULD_USE_FILE_PROPERTY_DEFAULT
                                                        );
            
            if(0 == logToFile.compareTo("Y"))
            {
                fileAppender = new RollingFileAppender<>();
                fileAppender.setEncoder(layout); 

                // Set up the rolling log policy
                String maxFileSize = loggerProperties.getProperty
                                                            (
                                                            CPTAUtilityConstants.LOG_MAX_FILE_SIZE_PROPERTY, 
                                                            CPTAUtilityConstants.LOG_MAX_FILE_SIZE_PROPERTY_DEFAULT
                                                            );
                String filePattern = loggerProperties.getProperty
                                                            (
                                                            CPTAUtilityConstants.LOG_FILE_NAME_PATTERN_PROPERTY, 
                                                            CPTAUtilityConstants.LOG_FILE_NAME_PATTERN_PROPERTY_DEFAULT
                                                            );
                filePattern = filePattern + "qpapiserver-%d{yyyy-MM-dd}.%i.log.gz";
                String maxHistory = loggerProperties.getProperty
                                                            (
                                                            CPTAUtilityConstants.LOG_FILE_MAX_HISTORY_PROPERTY, 
                                                            CPTAUtilityConstants.LOG_FILE_MAX_HISTORY_PROPERTY_DEFAULT
                                                            );
                String totalMaxSize = loggerProperties.getProperty
                                                            (
                                                            CPTAUtilityConstants.LOG_FILE_TOTAL_MAX_SIZE_PROPERTY, 
                                                            CPTAUtilityConstants.LOG_FILE_TOTAL_MAX_SIZE_PROPERTY_DEFAULT
                                                            );
                SizeAndTimeBasedRollingPolicy newLogFilePolicy = new SizeAndTimeBasedRollingPolicy();
                newLogFilePolicy.setMaxFileSize(FileSize.valueOf(maxFileSize));
                newLogFilePolicy.setFileNamePattern(filePattern);
                newLogFilePolicy.setMaxHistory(Integer.parseInt(maxHistory));
                newLogFilePolicy.setTotalSizeCap(FileSize.valueOf(totalMaxSize));      
                fileAppender.setRollingPolicy(newLogFilePolicy);
                fileAppender.setName("file_appender");

                fileAppender.setImmediateFlush(true);
                fileAppender.setAppend(true);
                fileAppender.setContext(context);
                
                newLogFilePolicy.setParent(fileAppender);
                newLogFilePolicy.setContext(context);
                apiServerLogger.addAppender(fileAppender);
                
                newLogFilePolicy.start();
                fileAppender.start();
                
            }
        }           
    }
    
    public static void shutdown()
    {
        // If there is a file appender
        if( null != fileAppender)
        {
            fileAppender.getRollingPolicy().stop();
            fileAppender.stop();
        }
        // If there is a console appender
        if( null != consoleAppender)
        {
            consoleAppender.stop();
        }
    }
    
    public static Logger getLogger() 
    {
        return (ch.qos.logback.classic.Logger)apiServerLogger;
    }
    
    private static Level property2Level(String levelAsString)
    {
        if( 0 == levelAsString.compareTo(CPTAUtilityConstants.LOG_THRESHOLD_PROPERTY_DEBUG))
        {
            return Level.DEBUG;
        }
        else if( 0 == levelAsString.compareTo(CPTAUtilityConstants.LOG_THRESHOLD_PROPERTY_INFO))
        {
            return Level.INFO;
        }
        else if( 0 == levelAsString.compareTo(CPTAUtilityConstants.LOG_THRESHOLD_PROPERTY_ERROR))
        {
            return Level.ERROR;
        }
        else if( 0 == levelAsString.compareTo(CPTAUtilityConstants.LOG_THRESHOLD_PROPERTY_WARN))
        {
            return Level.WARN;
        }
        else if( 0 == levelAsString.compareTo(CPTAUtilityConstants.LOG_THRESHOLD_PROPERTY_TRACE))
        {
            return Level.TRACE;
        }
        else if( 0 == levelAsString.compareTo(CPTAUtilityConstants.LOG_THRESHOLD_PROPERTY_OFF))
        {
            return Level.OFF;
        }
        else
        {
            return Level.ALL;
        }
    }
    
    protected static String getPropertyValue(String propertyName, String defaultValue)
    {
        String propertyValue = System.getenv(propertyName);
        // if there is no property set, use default
        if(null == propertyValue)
        {
            propertyValue = defaultValue;
        }

        return propertyValue;
    }
    static LoggerContext context = null;
    static RollingFileAppender<ILoggingEvent> fileAppender = null;
    static ConsoleAppender<ILoggingEvent> consoleAppender = null;
    static Level loggerLevel = Level.OFF;
    static Logger apiServerLogger = (ch.qos.logback.classic.Logger)((new LoggerContext()).getLogger("qpapiserver"));
}
