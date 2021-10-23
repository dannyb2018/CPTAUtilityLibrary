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

public class CPTADatabaseConstants 
{
    // Offset limit sql
    public static final String OFFSET_QUERY_FRAGMENT_NAME = "OFFSET_QUERY_FRAGMENT_NAME";
    public static final String LIMIT_QUERY_FRAGMENT_NAME = "LIMIT_QUERY_FRAGMENT_NAME";  

    // Data connection manager constants
    public final static String DB_POSTGRES_DRIVER_PROPERTY_VALUE = "org.postgresql.Driver";
    public final static String DB_JDBC_URL_PROPERTY_NAME = "JDBC_URL";
    public final static String DB_USERNAME_PROPERTY_NAME = "DB_USERNAME";
    public final static String DB_PASSWORD_PROPERTY_NAME = "DB_PASSWORD";
    public final static String DB_CACHE_PREP_STMTS_PROPERTY_NAME = "cachePrepStmts";
    public final static String DB_CACHE_PREP_STMTS_PROPERTY_DEFAULT_VALUE = "true";
    public final static String DB_PREP_STMT_CACHE_SIZE_PROPERTY_NAME = "prepStmtCacheSize";
    public final static String DB_PREP_STMT_CACHE_SIZE_PROPERTY_DEFAULT_VALUE = "250";
    public final static String DB_PREP_STMT_CACHE_SQL_LIMIT_PROPERTY_NAME = "prepStmtCacheSqlLimit";
    public final static String DB_PREP_STMT_CACHE_SQL_LIMIT_PROPERTY_DEFAULT_VALUE = "2048";
    public final static String DB_POSTGRES_CONNECTION_TYPE = "postgres";
    public final static String DB_CONNECTION_TYPE = "DB_CONNECTION_TYPE";
    public final static String DB_CONNECTION_NAME = "DB_CONNECTION_NAME";    
}
