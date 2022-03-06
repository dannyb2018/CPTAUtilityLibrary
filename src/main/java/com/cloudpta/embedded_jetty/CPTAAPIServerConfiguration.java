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

import org.glassfish.jersey.server.ServerProperties;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import jakarta.ws.rs.core.Application;

public abstract class CPTAAPIServerConfiguration extends Application 
{       
    protected abstract String getContextPath();
    protected abstract String getAPISubPath();
    protected abstract Set<Class<?>> getAPIHandlers();

    @Override
    public Map<String, Object> getProperties() 
    {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put
                     (
                     "jersey.config.server.provider.classnames", 
                     "org.glassfish.jersey.media.multipart.MultiPartFeature"
                     );       
        properties.put(ServerProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, 1);
        return properties;
    }

    @Override
    public Set<Class<?>> getClasses()
    {
        final Set<Class<?>> classes = getAPIHandlers();
        return classes;
    }     
}
