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

/**
 *
 * @author Danny
 */
public interface CPTAUtilityConstants
{
    public static final String VERSION = "1.5.6";       
    
    public static final String FRONTEND_LOCATION_PROPERTY_NAME = "FRONTEND_LOCATION";
    public static final String SERVER_PORT_PROPERTY_NAME = "SERVER_PORT";

    public static final String EXCEPTION_MESSAGE_FIELD = "message";
    public static final String ERROR_LINE_MESSAGE_FIELD = "error_line";
    public static final String STACK_TRACE_FIELD = "stack_trace";
    
    public static final String CPTA_DATE_FORMAT = "yyyy-MM-dd";
    public static final String CPTA_DATE_TIME_FORMAT = "yyyy-MM-dd";

    public static final String CPTA_OFFSET_PROPERTY_DAILY = "D";
    public static final String CPTA_OFFSET_PROPERTY_WEEKLY = "W";
    public static final String CPTA_OFFSET_PROPERTY_MONTHLY = "M";        
    public static final String CPTA_OFFSET_PROPERTY_YEARLY = "Y";        

    public static final String DATE_FIELD_NAME = "date";    
    
    public final static String LOG_PATTERN_PROPERTY = "LOG_PATTERN";    
    public final static String LOG_PATTERN_PROPERTY_DEFAULT = "%p %d{yyyy:MM:dd-HH:mm:ss:SSS} [%C{1}:%M:%L] %m%n";    
    public final static String LOG_MAX_FILE_SIZE_PROPERTY = "MAX_FILE_SIZE";    
    public final static String LOG_MAX_FILE_SIZE_PROPERTY_DEFAULT = "50MB";    
    public final static String LOG_SHOULD_USE_CONSOLE_PROPERTY = "LOG_SHOULD_USE_CONSOLE";    
    public final static String LOG_SHOULD_USE_CONSOLE_PROPERTY_DEFAULT = "Y";    
    public final static String LOG_SHOULD_USE_FILE_PROPERTY = "LOG_SHOULD_USE_FILE";    
    public final static String LOG_SHOULD_USE_FILE_PROPERTY_DEFAULT = "Y";    
    public final static String LOG_THRESHOLD_PROPERTY = "LOG_THRESHOLD";    
    public final static String LOG_THRESHOLD_PROPERTY_OFF = "OFF";
    public final static String LOG_THRESHOLD_PROPERTY_DEFAULT = "OFF";    
    public final static String LOG_THRESHOLD_PROPERTY_ALL = "ALL";    
    public final static String LOG_THRESHOLD_PROPERTY_TRACE = "TRACE";    
    public final static String LOG_THRESHOLD_PROPERTY_WARN = "WARN";    
    public final static String LOG_THRESHOLD_PROPERTY_DEBUG = "DEBUG";    
    public final static String LOG_THRESHOLD_PROPERTY_ERROR = "ERROR";    
    public final static String LOG_THRESHOLD_PROPERTY_INFO = "INFO";    
    public final static String LOG_FILE_NAME_PATTERN_PROPERTY = "FILE_NAME_PATTERN";
    public final static String LOG_FILE_NAME_PATTERN_PROPERTY_DEFAULT = "/opt/api-server/logs/";
    public final static String LOG_FILE_MAX_HISTORY_PROPERTY = "MAX_HISTORY";
    public final static String LOG_FILE_MAX_HISTORY_PROPERTY_DEFAULT = "30";
    public final static String LOG_FILE_TOTAL_MAX_SIZE_PROPERTY = "TOTAL_MAX_LOG_SIZE";
    public final static String LOG_FILE_TOTAL_MAX_SIZE_PROPERTY_DEFAULT = "3GB";
    
    
}
