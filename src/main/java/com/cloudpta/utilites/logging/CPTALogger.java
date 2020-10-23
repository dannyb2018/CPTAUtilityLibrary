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
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.cloudpta.utilites.CPTAUtilityConstants;
import java.util.Properties;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Danny
 */
/**
 *
 * @author Danny
 */
public class CPTALogger
{
    public static void initialise( Properties loggerProperties )
    {
        if( null == context)
        {
            context = (LoggerContext) LoggerFactory.getILoggerFactory();
            // get pattern is common to both appenders
            String logPattern = loggerProperties.getProperty
                                                        (
                                                        CPTAUtilityConstants.LOG_PATTERN_PROPERTY, 
                                                        CPTAUtilityConstants.LOG_PATTERN_PROPERTY_DEFAULT
                                                        );
            PatternLayoutEncoder layout = new PatternLayoutEncoder();
            layout.setPattern(logPattern);

            // If we need a console logging
            String threshold = loggerProperties.getProperty
                                                        (
                                                        CPTAUtilityConstants.LOG_CONSOLE_THRESHOLD_PROPERTY, 
                                                        CPTAUtilityConstants.LOG_CONSOLE_THRESHOLD_PROPERTY_DEFAULT
                                                        );
            consoleLoggerLevel = property2Level(threshold);
            if(false == consoleLoggerLevel.equals(Level.OFF))
            {
                consoleAppender.setEncoder(layout);
                consoleAppender.setImmediateFlush(true);
                consoleAppender.setContext(context);
                consoleAppender.start();
            }

            // If we need a file logging
            threshold = loggerProperties.getProperty
                                                        (
                                                        CPTAUtilityConstants.LOG_FILE_THRESHOLD_PROPERTY, 
                                                        CPTAUtilityConstants.LOG_FILE_THRESHOLD_PROPERTY_DEFAULT
                                                        );
            fileLoggerLevel = property2Level(threshold);
            // If we need a file logger
            if( false == Level.OFF.equals(fileLoggerLevel))
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

                fileAppender.setImmediateFlush(true);
                fileAppender.setAppend(true);
                fileAppender.setContext(context);
                fileAppender.start();
            }
        }       
    }
    
    public static void shutdown()
    {
        // If there is a file appender
        if( null != fileAppender)
        {
            fileAppender.stop();
        }
        // If there is a console appender
        if( null != consoleAppender)
        {
            consoleAppender.stop();
        }
    }
    
    public static Logger getLogger(Class className) 
    {
        Logger loggerForThisClass = context.getLogger(className);
        // If we need a console logger
        if(false == consoleLoggerLevel.equals(Level.OFF))
        {
            loggerForThisClass.addAppender(consoleAppender);
        }
        // If we need a file logger
        if(false == fileLoggerLevel.equals(Level.OFF))
        {
            loggerForThisClass.addAppender(fileAppender);
        }
        return loggerForThisClass;
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
    
    static LoggerContext context = null;
    static RollingFileAppender<ILoggingEvent> fileAppender = null;
    static ConsoleAppender<ILoggingEvent> consoleAppender = null;
    static Level consoleLoggerLevel = Level.OFF;
    static Level fileLoggerLevel = Level.OFF;
    static Logger rootLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger("com.quantpipeline");
}
