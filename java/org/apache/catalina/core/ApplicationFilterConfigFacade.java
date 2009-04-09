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


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
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
import org.apache.catalina.security.SecurityUtil;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.StringManager;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.util.log.SystemLogHandler;


/**
 * Facade for AppalicationFilterConfig.
 *
 * @author Remy Maucherat
 * @version $Revision: 992 $ $Date: 2009-04-08 01:09:34 +0200 (Wed, 08 Apr 2009) $
 */

public class ApplicationFilterConfigFacade implements FilterConfig, FilterRegistration {


    protected static StringManager sm =
        StringManager.getManager(Constants.Package);

    public static class Dynamic extends ApplicationFilterConfigFacade
    implements FilterRegistration.Dynamic {

        public Dynamic(ApplicationFilterConfig config) {
            super(config);
        }

    }


    // ----------------------------------------------------------- Constructors


    public ApplicationFilterConfigFacade(ApplicationFilterConfig config) {
        this.config = config;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The Context with which we are associated.
     */
    private ApplicationFilterConfig config = null;


    // --------------------------------------------------- FilterConfig Methods


    /**
     * Return the name of the filter we are configuring.
     */
    public String getFilterName() {

        return config.getFilterName();

    }


    /**
     * Return a <code>String</code> containing the value of the named
     * initialization parameter, or <code>null</code> if the parameter
     * does not exist.
     *
     * @param name Name of the requested initialization parameter
     */
    public String getInitParameter(String name) {

        return config.getInitParameter(name);

    }


    /**
     * Return an <code>Enumeration</code> of the names of the initialization
     * parameters for this Filter.
     */
    public Enumeration getInitParameterNames() {

        return config.getInitParameterNames();

    }


    /**
     * Return the ServletContext of our associated web application.
     */
    public ServletContext getServletContext() {

        return config.getServletContext();

    }


    public boolean addMappingForServletNames(
            EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
            String... servletNames) {
        return config.addMappingForServletNames(dispatcherTypes, isMatchAfter, servletNames);
    }


    public boolean addMappingForUrlPatterns(
            EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
            String... urlPatterns) {
        return config.addMappingForUrlPatterns(dispatcherTypes, isMatchAfter, urlPatterns);
    }


    public boolean setInitParameter(String name, String value) {
        return config.setInitParameter(name, value);
    }


    public boolean setInitParameters(Map<String, String> initParameters) {
        return config.setInitParameters(initParameters);
    }


    public void setAsyncSupported(boolean isAsyncSupported) {
        config.setAsyncSupported(isAsyncSupported);
    }

    public void setDescription(String description) {
        config.setDescription(description);
    }

}
