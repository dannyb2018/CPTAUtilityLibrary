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

import org.eclipse.jetty.websocket.server.JettyWebSocketServerContainer;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer.Configurator;
import jakarta.servlet.ServletContext;
import java.util.Set;
import com.cloudpta.graphql.subscriptions.CPTASubscriptionHandlerSocket;
import java.lang.reflect.Constructor;
import java.util.Map;

public class CPTAAPIServerWebSocketConfiguration  implements Configurator
{
    public CPTAAPIServerWebSocketConfiguration(Map<String, Class<? extends CPTASubscriptionHandlerSocket>> websocketHandlers)
    {
        this.websocketHandlers = websocketHandlers;
    }

    @Override
    public void accept(ServletContext servletContext, JettyWebSocketServerContainer container) 
    {
        Class<?>[] parameterList = new Class<?>[]{};
        Set<String> paths = websocketHandlers.keySet();
     
        for(String currentPath : paths)
        {
            try
            {
                Class<? extends CPTASubscriptionHandlerSocket> handlerClass = websocketHandlers.get(currentPath);
                Constructor<? extends CPTASubscriptionHandlerSocket> handlerClassConstructor = handlerClass.getConstructor(parameterList);
                CPTASubscriptionHandlerSocket handler = handlerClassConstructor.newInstance();
                container.addMapping(currentPath, handler);
            }
            catch(Exception E)
            {
                E.printStackTrace();
            }
        }
           
    }

    Map<String, Class<? extends CPTASubscriptionHandlerSocket>> websocketHandlers;
}