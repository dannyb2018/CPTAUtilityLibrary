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
package com.cloudpta.nifi.connections;

import com.cloudpta.nifi.CPTANifiEndpoint;
import com.cloudpta.nifi.utilities.CPTANifiConstants;
import com.cloudpta.utilites.exceptions.CPTAException;

/**
 *
 * @author Danny
 */
public class CPTAOutputPort extends CPTANifiEndpoint<CPTAPortComponent>
{
    public CPTAOutputPort() throws CPTAException
    {
        super();
        component = new CPTAPortComponent();
        component.setPortType("OUTPUT_PORT");
        endpointType = "OUTPUT_PORT";
    }

    @Override
    protected String getPersistSubURL()
    {
        return CPTANifiConstants.NIFI_API_PROCESS_GROUPS_SUBURL + "/" + component.getProcessGroupID() + CPTANifiConstants.NIFI_API_OUTPUT_PORTS_SUBURL;
    }

    @Override
    protected String getFindSubURL(String id)
    {
        return CPTANifiConstants.NIFI_API_OUTPUT_PORTS_SUBURL + "/" + id;
    }

    @Override
    protected String getDeleteSubURL()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected String getChangeStateSubURL()
    {
        return CPTANifiConstants.NIFI_API_OUTPUT_PORTS_SUBURL + "/" + component.getID()+ CPTANifiConstants.NIFI_API_RUN_STATUS_SUBURL;
    }

    @Override
    protected String getUpdateSubURL()
    {
        // TODO Auto-generated method stub
        return null;
    }    
}
