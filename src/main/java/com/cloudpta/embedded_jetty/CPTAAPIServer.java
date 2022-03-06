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
package com.cloudpta.embedded_jetty;

import com.cloudpta.utilites.CPTAUtilityConstants;
import com.cloudpta.utilites.logging.CPTALogger;
import com.cloudpta.utilites.persistence.CPTADatabaseConnectionManager;
import com.cloudpta.utilites.persistence.CPTADatabaseConstants;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import java.util.Properties;
import ch.qos.logback.classic.Logger;

public abstract class CPTAAPIServer 
{
    protected abstract CPTAAPIServerConfiguration getAPIServerConfiguration();

    protected CPTAAPIServer( String[] args) throws Exception
    {
        CPTAAPIServerCloseHandler shutdownHandler = new CPTAAPIServerCloseHandler(this);
        Runtime.getRuntime().addShutdownHook(shutdownHandler);        
    }

    protected void initialise() throws Exception
    {
        databasePassword = System.getenv(CPTADatabaseConstants.DB_PASSWORD_PROPERTY_NAME);                
        databaseJdbcUrl = System.getenv(CPTADatabaseConstants.DB_JDBC_URL_PROPERTY_NAME);                
        databaseUser = System.getenv(CPTADatabaseConstants.DB_USERNAME_PROPERTY_NAME);                
        databaseConnectionName = System.getenv(CPTADatabaseConstants.DB_CONNECTION_NAME);
        databaseConnectionType = System.getenv(CPTADatabaseConstants.DB_CONNECTION_TYPE);
        frontEndLocation = System.getenv(CPTAUtilityConstants.FRONTEND_LOCATION_PROPERTY_NAME);
        
        // if there is a port
        String serverPortAsString = System.getenv(CPTAUtilityConstants.SERVER_PORT_PROPERTY_NAME);
        if(null != serverPortAsString)
        {
            port = Integer.parseInt(serverPortAsString);
        }
        

        // Set up logger off env variables
        CPTALogger.initialise();
        serverLogger = CPTALogger.getLogger();
    
        // If there is a database
        if( null != databaseConnectionName)
        {
            // Set up the db details
            serverLogger.info("database connection name " + databaseConnectionName);
            serverLogger.info("database connection type " + databaseConnectionType);
            serverLogger.info("database connection jdbc " + databaseJdbcUrl);
            serverLogger.info("database connection user " + databaseUser);
            serverLogger.info("database connection password " + databasePassword);

            // Add database 
            Properties connectionProperties = new Properties();
            connectionProperties.put
                                (
                                    CPTADatabaseConstants.DB_JDBC_URL_PROPERTY_NAME, 
                                databaseJdbcUrl
                                );
            connectionProperties.put
                                (
                                    CPTADatabaseConstants.DB_PASSWORD_PROPERTY_NAME, 
                                databasePassword
                                );
            connectionProperties.put
                                (
                                    CPTADatabaseConstants.DB_USERNAME_PROPERTY_NAME, 
                                databaseUser
                                );
            // Add database
            CPTADatabaseConnectionManager.addConnectionManager
                                                            (
                                                            databaseConnectionName, 
                                                            databaseConnectionType, 
                                                            connectionProperties
                                                            );    
        }
    } 

    protected void addRestHandler(HandlerCollection contexts)
    {
        // Get instance of api server configuration
        CPTAAPIServerConfiguration RestAPIConfiguration = getAPIServerConfiguration(); 
        ServletHolder servletHolder = new ServletHolder(org.glassfish.jersey.servlet.ServletContainer.class);  
        servletHolder.setInitParameter("jakarta.ws.rs.Application", RestAPIConfiguration.getClass().getName() );
        ServletContextHandler restAPIHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        String apiContextPath = RestAPIConfiguration.getContextPath();  
        restAPIHandler.setContextPath(apiContextPath);
        String apiSubpath = RestAPIConfiguration.getAPISubPath();
        restAPIHandler.addServlet(servletHolder, apiSubpath);
        contexts.addHandler(restAPIHandler);
    }

    protected void addFrontendHandler(HandlerCollection contexts)
    {
        serverLogger.info("frontend files root director " + frontEndLocation);        
        ServletContextHandler frontend = new ServletContextHandler();
        frontend.setContextPath("/QPTraderConsole");            
        ServletHolder frontendServletHolder = new ServletHolder(DefaultServlet.class);
        System.out.println(frontEndLocation);
        frontend.setResourceBase(frontEndLocation);
        frontend.addServlet(frontendServletHolder, "/*");            
        contexts.addHandler(frontend);
    }

    protected void addWebsocketsHandler(HandlerCollection contexts)
    {
        ServletContextHandler webSocketContext = new ServletContextHandler();
        webSocketContext.setContextPath("/QPAPIWSServer/GraphQL/api");            
        contexts.addHandler(webSocketContext);            
        JettyWebSocketServletContainerInitializer.configure(webSocketContext, null);
        
    //    QPStructuredProductsAPIServerWebsocketConfiguration.AddWebSocketServlets(webSocketContext);

    }

    protected void run() throws Exception
    {        
        // create jetty server
        jettyServer = new Server(port);
        
        // going to have multiple handlers, one for frontend and one for api
        HandlerCollection contexts = new HandlerCollection();
        
        // add rest api
        addRestHandler(contexts);
        // Add frontend
        addFrontendHandler(contexts);
        // Add websockets
        addWebsocketsHandler(contexts);

        // Set up all handlers
        jettyServer.setHandler(contexts);            

        try 
        {
            // start the server
            jettyServer.start();
            jettyServer.join();
        } 
        finally 
        {
            jettyServer.destroy();            
            serverLogger.info("server stopped");
            CPTALogger.shutdown();
        }
    }
    
    public void shutdown() throws Exception
    {        
        try
        {
            jettyServer.stop();            
        }
        finally
        {
            serverLogger.info("server shutdown");
        }
    }

    protected Server jettyServer;

    protected String frontEndLocation;

    protected String databasePassword;
    protected String databaseUser;
    protected String databaseConnectionName;
    protected String databaseConnectionType;
    protected String databaseJdbcUrl;
    protected String jettyHome;
    protected Logger serverLogger = null;
    
    protected int port=8080;    
}
