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
import java.util.Iterator;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.apache.catalina.Context;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.util.StringManager;


/**
 * Facade for the <b>FilterDef</b> object, with a hook to the Context to
 * store them automatically. The name of the class is not accurate, since
 * there is no StandardFilter, but for consistency with StandardWrapper.
 *
 * @author Remy Maucharat
 * @version $Revision: 947 $ $Date: 2009-03-10 05:02:22 +0100 (Tue, 10 Mar 2009) $
 */

public final class StandardFilterFacade
    implements FilterRegistration {


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ----------------------------------------------------------- Constructors


    /**
     * Create a new facade around a FilterDef.
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


    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, 
            boolean isMatchAfter, String... servletNames) {
        if (context.isInitialized()) {
            throw new IllegalStateException(sm.getString("filterRegistration.addFilterMapping.ise", context.getPath()));
        }
        FilterMap filterMap = new FilterMap(); 
        for (String servletName : servletNames) {
            filterMap.addServletName(servletName);
        }
        filterMap.setFilterName(filterDef.getFilterName());
        for (DispatcherType dispatcherType: dispatcherTypes) {
            filterMap.setDispatcher(dispatcherType.name());
        }
        if (isMatchAfter) {
            context.addFilterMap(filterMap);
        } else {
            context.addFilterMapBefore(filterMap);
        }
    }


    public void addMappingForUrlPatterns(
            EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
            String... urlPatterns) {
        if (context.isInitialized()) {
            throw new IllegalStateException(sm.getString("filterRegistration.addFilterMapping.ise", context.getPath()));
        }
        FilterMap filterMap = new FilterMap(); 
        for (String urlPattern : urlPatterns) {
            filterMap.addURLPattern(urlPattern);
        }
        filterMap.setFilterName(filterDef.getFilterName());
        for (DispatcherType dispatcherType: dispatcherTypes) {
            filterMap.setDispatcher(dispatcherType.name());
        }
        if (isMatchAfter) {
            context.addFilterMap(filterMap);
        } else {
            context.addFilterMapBefore(filterMap);
        }
    }


    public void setAsyncSupported(boolean asyncSupported) {
        filterDef.setAsyncSupported(asyncSupported);
        context.addFilterDef(filterDef);
    }


    public boolean setDescription(String description) {
        filterDef.setDescription(description);
        context.addFilterDef(filterDef);
        // FIXME: return value ???
        return true;
    }


    public boolean setInitParameter(String name, String value) {
        filterDef.addInitParameter(name, value);
        context.addFilterDef(filterDef);
        // FIXME: return value ???
        return true;
    }


    public void setInitParameters(Map<String, String> initParameters) {
        Iterator<String> parameterNames = initParameters.keySet().iterator();
        while (parameterNames.hasNext()) {
            String parameterName = parameterNames.next();
            filterDef.addInitParameter(parameterName, initParameters.get(parameterName));
        }
    }


}
