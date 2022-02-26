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
package com.cloudpta.utilites.websocket;

import com.cloudpta.utilites.exceptions.CPTAException;
import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;

/**
 *
 * @author Danny
 */
public class CPTAWebSocketClient
{
    public CPTAWebSocketClient()
    {
    }
    
    public void setHeaders(List<CPTAWebSocketHeader> newHeaders)
    {
        headers = newHeaders;
    }
    
    public void connect(String address) throws CPTAException
    {
        try
        {
            underlyingSocket = factory.createSocket(address);
            underlyingSocket.addListener(new CPTAInternalWebSocketEventListener(this));
            for( CPTAWebSocketHeader header: headers)
            {
                underlyingSocket.addHeader(header.getName(), header.getValue());
            }
            
            underlyingSocket.connect();    
        }
        catch(Exception E)
        {
            CPTAException wrappedException = new CPTAException(E);
            throw wrappedException;
        }
    }
    
    public void secureConnect(String address, SSLContext secureContext) throws CPTAException
    {
        // Create a custom SSL context.
        // SSLContext context = NaiveSSLContext.getInstance("TLS");

        try
        {
            // Set the custom SSL context.
            factory.setSSLContext(secureContext);        
            underlyingSocket = factory.createSocket(address);
            for( CPTAWebSocketHeader header: headers)
            {
                underlyingSocket.addHeader(header.getName(), header.getValue());
            }
            
            underlyingSocket.addListener(new CPTAInternalWebSocketEventListener(this));
            underlyingSocket.connect();
        }
        catch(Exception E)
        {
            CPTAException wrappedException = new CPTAException(E);
            throw wrappedException;
        }       
    }
    
    public void disconnect()
    {
        underlyingSocket.disconnect();
        underlyingSocket = null;
    }
    
    public void sendMessage(String text)
    {
        underlyingSocket.sendText(text);
    }
    
    public boolean isConnected()
    {
        return whetherConnected.get();
    }

    public void setPingInterval(long pingInterval)
    {
        underlyingSocket.setPingInterval(pingInterval);
    }
    
    public void addEventListener(CPTAWebSocketClientEventListener newListener)
    {
        listeners.add(newListener);
    }
    
    public void removeEventListener(CPTAWebSocketClientEventListener oldListener)
    {
        listeners.remove(oldListener);        
    }
    
    protected void fireConnected()
    {
        for( CPTAWebSocketClientEventListener currentListener: listeners)
        {
            currentListener.handleConnect();
        }
    }
    
    protected void fireDisconnected(boolean didCloseCleanly, String reason)
    {
        for( CPTAWebSocketClientEventListener currentListener: listeners)
        {
            if(true == didCloseCleanly)
            {
                currentListener.handleDisconnect();
            }
            else
            {
                currentListener.handleError(reason);
            }
        }        
    }

    protected void fireMessageReceived(String messageText)
    {
        for( CPTAWebSocketClientEventListener currentListener: listeners)
        {
            currentListener.handleMessageReceived(messageText);
        }        
    }

    protected void fireConnectionStatusChanged(boolean newConnectionStatus)
    {
        whetherConnected.set(newConnectionStatus);
    }
    
    protected AtomicReference<Boolean> whetherConnected = new AtomicReference<>(false);
    protected List<CPTAWebSocketHeader> headers = new ArrayList<>();
    protected WebSocketFactory factory = new WebSocketFactory();
    protected WebSocket underlyingSocket;   
    protected List<CPTAWebSocketClientEventListener> listeners = new ArrayList<>();
}

class CPTAInternalWebSocketEventListener implements WebSocketListener
{
    public CPTAInternalWebSocketEventListener(CPTAWebSocketClient newClient)
    {
        client = newClient;
    }
    
    @Override
    public void onError(WebSocket ws, WebSocketException wse) throws Exception
    {
        System.out.println(wse.toString());
    }

    @Override
    public void onConnected(WebSocket ws, Map<String, List<String>> map) throws Exception
    {
        client.fireConnected();
    }

    @Override
    public void onConnectError(WebSocket ws, WebSocketException wse) throws Exception
    {
        client.fireDisconnected(false, wse.getMessage());
    }

    @Override
    public void onDisconnected(WebSocket ws, WebSocketFrame wsf, WebSocketFrame wsf1, boolean bln) throws Exception
    {
        client.fireDisconnected(true, "");
    }

    @Override
    public void onTextMessage(WebSocket ws, String string) throws Exception
    {
        client.fireMessageReceived(string);
    }

    @Override
    public void onStateChanged(WebSocket ws, WebSocketState wss) throws Exception
    {
        boolean isConnected = ( 0 == wss.compareTo(WebSocketState.OPEN) );
        client.fireConnectionStatusChanged(isConnected);
    }
    protected CPTAWebSocketClient client;
    
    // Not used
    
    @Override
    public void onFrame(WebSocket ws, WebSocketFrame wsf) throws Exception
    {
    }
    @Override
    public void onContinuationFrame(WebSocket ws, WebSocketFrame wsf) throws Exception
    {
    }
    @Override
    public void onTextFrame(WebSocket ws, WebSocketFrame wsf) throws Exception
    {
    }
    @Override
    public void onBinaryFrame(WebSocket ws, WebSocketFrame wsf) throws Exception
    {
    }
    @Override
    public void onCloseFrame(WebSocket ws, WebSocketFrame wsf) throws Exception
    {
    }
    @Override
    public void onPingFrame(WebSocket ws, WebSocketFrame wsf) throws Exception
    {
    }
    @Override
    public void onPongFrame(WebSocket ws, WebSocketFrame wsf) throws Exception
    {
    }
    @Override
    public void onTextMessage(WebSocket ws, byte[] bytes) throws Exception
    {
    }
    @Override
    public void onBinaryMessage(WebSocket ws, byte[] bytes) throws Exception
    {
    }
    @Override
    public void onSendingFrame(WebSocket ws, WebSocketFrame wsf) throws Exception
    {
    }
    @Override
    public void onFrameSent(WebSocket ws, WebSocketFrame wsf) throws Exception
    {
    }
    @Override
    public void onFrameUnsent(WebSocket ws, WebSocketFrame wsf) throws Exception
    {
    }
    @Override
    public void onThreadCreated(WebSocket ws, ThreadType tt, Thread thread) throws Exception
    {
    }
    @Override
    public void onThreadStarted(WebSocket ws, ThreadType tt, Thread thread) throws Exception
    {
    }
    @Override
    public void onThreadStopping(WebSocket ws, ThreadType tt, Thread thread) throws Exception
    {
    }

    @Override
    public void onFrameError(WebSocket ws, WebSocketException wse, WebSocketFrame wsf) throws Exception
    {
    }

    @Override
    public void onMessageError(WebSocket ws, WebSocketException wse, List<WebSocketFrame> list) throws Exception
    {
    }

    @Override
    public void onMessageDecompressionError(WebSocket ws, WebSocketException wse, byte[] bytes) throws Exception
    {
    }

    @Override
    public void onTextMessageError(WebSocket ws, WebSocketException wse, byte[] bytes) throws Exception
    {
    }

    @Override
    public void onSendError(WebSocket ws, WebSocketException wse, WebSocketFrame wsf) throws Exception
    {
    }

    @Override
    public void onUnexpectedError(WebSocket ws, WebSocketException wse) throws Exception
    {
    }

    @Override
    public void handleCallbackError(WebSocket ws, Throwable thrwbl) throws Exception
    {
    }

    @Override
    public void onSendingHandshake(WebSocket ws, String string, List<String[]> lists) throws Exception
    {
    }
}

