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

import java.util.List;
import com.cloudpta.utilites.logging.CPTALogger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.websocket.server.JettyWebSocketCreator;
import ch.qos.logback.classic.Logger;

public abstract class CPTAWebSocket extends WebSocketAdapter implements JettyWebSocketCreator
{
    protected abstract Object returnNewInstance();
    protected abstract void handleIncomingMessage(String queryAsString);
    protected abstract void handleConnected(Session session);
    protected abstract void handleClose(int statusCode, String reason); 
    protected abstract void handleError(Throwable t);

    @Override
    public Object createWebSocket(JettyServerUpgradeRequest req, JettyServerUpgradeResponse resp) 
    {
        // get the requested protocals in the req
        List<String> requestedSubprotocols = req.getSubProtocols();
        for(String currentSubprotocol : requestedSubprotocols)
        {
            resp.setAcceptedSubProtocol(currentSubprotocol);
        }
        
        return returnNewInstance();
    }
    
    @Override
    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);
        socketSession = session;

        handleConnected(session);         
    }

    @Override
    public void onWebSocketText(String message)
    {
        super.onWebSocketText(message);

        handleIncomingMessage(message);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);

        handleClose(statusCode, reason);
    }

    @Override
    public void onWebSocketError(Throwable cause)
    {
        super.onWebSocketError(cause);

        handleError(cause); 
    }

    protected Logger socketLogger = CPTALogger.getLogger();    
    protected Session socketSession;   
}
