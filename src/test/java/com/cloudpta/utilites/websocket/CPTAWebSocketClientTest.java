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
package com.cloudpta.utilites.websocket;

import com.neovisionaries.ws.client.WebSocketException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Danny
 */
public class CPTAWebSocketClientTest
{
    @Test
    public void testUnsecure() throws IOException, WebSocketException, InterruptedException
    {
        CPTAWebSocketClient unsecureSocket = new CPTAWebSocketClient();
        unsecureSocket.addEventListener(new CPTATestHandler(unsecureSocket));
        unsecureSocket.connect("ws://localhost:8080/QPAPIWSServer/api/dashboard/notifications");  
        Thread.sleep(400000);
        unsecureSocket.disconnect();
    }

    @Test
    public void testSecure() throws IOException, WebSocketException, InterruptedException, NoSuchAlgorithmException
    {
    }    

}
class CPTATestHandler implements CPTAWebSocketClientEventListener
{
    public CPTATestHandler(CPTAWebSocketClient newSocket)
    {
        socket = newSocket;
    }
    
    @Override
    public void handleConnect()
    {
        System.out.println("connected");
    }

    @Override
    public void handleDisconnect()
    {
        System.out.println("disconnected");
    }

    @Override
    public void handleError(String reason)
    {
        System.out.println("error " + reason);
    }

    @Override
    public void handleMessageReceived(String messageText)
    {
        System.out.println("received " + messageText);
    }
    
    protected CPTAWebSocketClient socket;
}