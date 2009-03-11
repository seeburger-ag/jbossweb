/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina.core;


import java.util.EnumSet;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.apache.catalina.Context;
import org.apache.catalina.deploy.FilterDef;


/**
 * Facade for the <b>FilterDef</b> object, with a hook to the Context to
 * store them automatically.
 *
 * @author Remy Maucharat
 * @version $Revision: 947 $ $Date: 2009-03-10 05:02:22 +0100 (Tue, 10 Mar 2009) $
 */

public final class StandardFilterFacade
    implements FilterRegistration {


    // ----------------------------------------------------------- Constructors


    /**
     * Create a new facede around a StandardWrapper.
     */
    public StandardFilterFacade(Context context, FilterDef filterDef) {

        super();
        this.context = context;
        this.filterDef = filterDef;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Associated context.
     */
    private Context context = null;


    /**
     * Wrapped filter def (facade).
     */
    private FilterDef filterDef = null;


   // --------------------------------------------- FilterRegistration Methods


    public void addMappingForServletNames(
            EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
            String... servletNames) {
        // TODO Auto-generated method stub
        
    }


    public void addMappingForUrlPatterns(
            EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
            String... urlPatterns) {
        // TODO Auto-generated method stub
        
    }


    public void setAsyncSupported(boolean asyncSupported) {
        // TODO Auto-generated method stub
        
    }


    public boolean setDescription(String description) {
        // TODO Auto-generated method stub
        return false;
    }


    public boolean setInitParameter(String name, String value) {
        // TODO Auto-generated method stub
        return false;
    }


    public void setInitParameters(Map<String, String> initParameters) {
        // TODO Auto-generated method stub
        
    }


}
