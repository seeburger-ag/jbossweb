/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 * 
 * 
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 1999-2009 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.catalina.core;


import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.security.SecurityUtil;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.StringManager;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.util.log.SystemLogHandler;


/**
 * Implementation of a <code>javax.servlet.FilterConfig</code> useful in
 * managing the filter instances instantiated when a web application
 * is first started.
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public final class ApplicationFilterConfig implements FilterConfig, Serializable {


    protected static StringManager sm =
        StringManager.getManager(Constants.Package);

    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new ApplicationFilterConfig for the specified filter
     * definition.
     *
     * @param context The context with which we are associated
     * @param filterDef Filter definition for which a FilterConfig is to be
     *  constructed
     */
    public ApplicationFilterConfig(Context context, FilterDef filterDef) {
        this.context = context;
        this.filterDef = filterDef;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The facade associated with this wrapper.
     */
    protected ApplicationFilterConfigFacade facade =
        new ApplicationFilterConfigFacade(this);


    /**
     * The Context with which we are associated.
     */
    private Context context = null;


    /**
     * Dynamic flag.
     */
    protected boolean dynamic = false;
    
    
    /**
     * The application Filter we are configured for.
     */
    private transient Filter filter = null;


    /**
     * The application Filter we are configured for.
     */
    private transient Filter filterInstance = null;


    /**
     * The <code>FilterDef</code> that defines our associated Filter.
     */
    private FilterDef filterDef = null;

    /**
     * the InstanceManager used to create and destroy filter instances.
     */
    private transient InstanceManager instanceManager;


    // --------------------------------------------------- FilterConfig Methods


    /**
     * Return the name of the filter we are configuring.
     */
    public String getFilterName() {

        return (filterDef.getFilterName());

    }


    /**
     * Return a <code>String</code> containing the value of the named
     * initialization parameter, or <code>null</code> if the parameter
     * does not exist.
     *
     * @param name Name of the requested initialization parameter
     */
    public String getInitParameter(String name) {

        Map map = filterDef.getParameterMap();
        if (map == null)
            return (null);
        else
            return ((String) map.get(name));

    }


    /**
     * Return an <code>Enumeration</code> of the names of the initialization
     * parameters for this Filter.
     */
    public Enumeration getInitParameterNames() {

        Map map = filterDef.getParameterMap();
        if (map == null)
            return (new Enumerator(new ArrayList()));
        else
            return (new Enumerator(map.keySet()));

    }


    /**
     * Return the ServletContext of our associated web application.
     */
    public ServletContext getServletContext() {

        return (this.context.getServletContext());

    }


    /**
     * Get the facade FilterRegistration.
     */
    public FilterRegistration getFacade() {
        return facade;
    }
    

    public boolean isDynamic() {
        return dynamic;
    }


    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
        if (dynamic) {
            // Change the facade (normally, this happens when the Wrapper is created)
            facade = new ApplicationFilterConfigFacade.Dynamic(this);
        }
    }


    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("ApplicationFilterConfig[");
        sb.append("name=");
        sb.append(filterDef.getFilterName());
        sb.append(", filterClass=");
        sb.append(filterDef.getFilterClass());
        sb.append("]");
        return (sb.toString());

    }


    public boolean addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, 
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
        return true;
    }


    public boolean addMappingForUrlPatterns(
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
        return true;
    }


    public void setAsyncSupported(boolean asyncSupported) {
        filterDef.setAsyncSupported(asyncSupported);
        context.addFilterDef(filterDef);
    }


    public void setDescription(String description) {
        filterDef.setDescription(description);
        context.addFilterDef(filterDef);
    }


    public boolean setInitParameter(String name, String value) {
        filterDef.addInitParameter(name, value);
        context.addFilterDef(filterDef);
        return true;
    }


    public boolean setInitParameters(Map<String, String> initParameters) {
        Iterator<String> parameterNames = initParameters.keySet().iterator();
        while (parameterNames.hasNext()) {
            String parameterName = parameterNames.next();
            filterDef.addInitParameter(parameterName, initParameters.get(parameterName));
        }
        return true;
    }


    // -------------------------------------------------------- Package Methods


    /**
     * Return the application Filter we are configured for.
     *
     * @exception ClassCastException if the specified class does not implement
     *  the <code>javax.servlet.Filter</code> interface
     * @exception ClassNotFoundException if the filter class cannot be found
     * @exception IllegalAccessException if the filter class cannot be
     *  publicly instantiated
     * @exception InstantiationException if an exception occurs while
     *  instantiating the filter object
     * @exception ServletException if thrown by the filter's init() method
     * @throws NamingException
     * @throws InvocationTargetException
     */
    Filter getFilter() throws ClassCastException, ClassNotFoundException,
        IllegalAccessException, InstantiationException, ServletException,
        InvocationTargetException, NamingException {

        // Return the existing filter instance, if any
        if (this.filter != null)
            return (this.filter);

        // Identify the class loader we will be using
        if (filterInstance == null) {
            String filterClass = filterDef.getFilterClass();
            this.filter = (Filter) getInstanceManager().newInstance(filterClass);
        } else {
            this.filter = filterInstance;
            filterInstance = null;
        }

        if (context instanceof StandardContext &&
                context.getSwallowOutput()) {
            try {
                SystemLogHandler.startCapture();
                filter.init(this);
            } finally {
                String log = SystemLogHandler.stopCapture();
                if (log != null && log.length() > 0) {
                    getServletContext().log(log);
                }
            }
        } else {
            filter.init(this);
        }
        return (this.filter);


    }

    
    /**
     * Set the filter instance programmatically.
     */
    public void setFilter(Filter filter) {
        filterInstance = filter;
    }
    

    /**
     * Return the filter definition we are configured for.
     */
    public FilterDef getFilterDef() {

        return (this.filterDef);

    }

    /**
     * Release the Filter instance associated with this FilterConfig,
     * if there is one.
     */
    void release() {

        if (this.filter != null)
        {
            if (Globals.IS_SECURITY_ENABLED) {
                try {
                    SecurityUtil.doAsPrivilege("destroy", filter);
                } catch(java.lang.Exception ex){
                    context.getLogger().error("ApplicationFilterConfig.doAsPrivilege", ex);
                }
                SecurityUtil.remove(filter);
            } else {
                filter.destroy();
            }
            if (!context.getIgnoreAnnotations()) {
                try {
                    ((StandardContext) context).getInstanceManager().destroyInstance(this.filter);
                } catch (Exception e) {
                    context.getLogger().error("ApplicationFilterConfig.preDestroy", e);
                }
            }
        }
        this.filter = null;

     }


    // -------------------------------------------------------- Private Methods


    private InstanceManager getInstanceManager() {
        if (instanceManager == null) {
            if (context instanceof StandardContext) {
                instanceManager = ((StandardContext)context).getInstanceManager();
            } else {
                instanceManager = new DefaultInstanceManager(null,
                        new HashMap<String, Map<String, String>>(),
                        context,
                        getClass().getClassLoader()); 
            }
        }
        return instanceManager;
    }

}
