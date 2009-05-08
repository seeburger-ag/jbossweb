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


import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.apache.catalina.Context;
import org.apache.catalina.util.StringManager;


/**
 * Facade for the <b>StandardWrapper</b> object.
 *
 * @author Remy Maucharat
 * @version $Revision$ $Date$
 */

public class StandardWrapperFacade
    implements ServletRegistration, ServletConfig {


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    public static class Dynamic extends StandardWrapperFacade
        implements ServletRegistration.Dynamic {

        public Dynamic(StandardWrapper wrapper) {
            super(wrapper);
        }
        
    }
    
    
    // ----------------------------------------------------------- Constructors


    /**
     * Create a new facede around a StandardWrapper.
     */
    public StandardWrapperFacade(StandardWrapper wrapper) {

        super();
        this.wrapper = wrapper;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Wrapped config.
     */
    private StandardWrapper wrapper = null;


    /**
     * Wrapped context (facade).
     */
    private ServletContext context = null;


    // -------------------------------------------------- ServletConfig Methods


    public String getServletName() {
        return wrapper.getServletName();
    }


    public ServletContext getServletContext() {
        if (context == null) {
            context = wrapper.getServletContext();
            if ((context != null) && (context instanceof ApplicationContext))
                context = ((ApplicationContext) context).getFacade();
        }
        return (context);
    }


    public String getInitParameter(String name) {
        return wrapper.getInitParameter(name);
    }


    public Enumeration getInitParameterNames() {
        return wrapper.getInitParameterNames();
    }


    public Set<String> addMapping(String... urlPatterns) {
        Set<String> conflicts = new HashSet<String>();
        if (((Context) wrapper.getParent()).isInitialized()) {
            throw new IllegalStateException(sm.getString
                    ("servletRegistration.addServletMapping.ise", ((Context) wrapper.getParent()).getPath()));
        }
        if (urlPatterns != null) {
            for (int i = 0; i < urlPatterns.length; i++) {
                if (((Context) wrapper.getParent()).findServletMapping(urlPatterns[i]) != null) {
                    conflicts.add(urlPatterns[i]);
                } else {
                    ((Context) wrapper.getParent()).addServletMapping(urlPatterns[i], wrapper.getName());
                }
            }
        }
        return conflicts;
    }


    public void setAsyncSupported(boolean asyncSupported) {
        wrapper.setAsyncSupported(asyncSupported);
    }


    public void setDescription(String description) {
        wrapper.setDescription(description);
    }


    public boolean setInitParameter(String name, String value) {
        wrapper.addInitParameter(name, value);
        return true;
    }


    public Set<String> setInitParameters(Map<String, String> initParameters) {
        Set<String> conflicts = new HashSet<String>();
        Iterator<String> parameterNames = initParameters.keySet().iterator();
        while (parameterNames.hasNext()) {
            String parameterName = parameterNames.next();
            if (wrapper.findInitParameter(parameterName) != null) {
                conflicts.add(parameterName);
            } else {
                wrapper.addInitParameter(parameterName, initParameters.get(parameterName));
            }
        }
        return conflicts;
    }


    public void setLoadOnStartup(int loadOnStartup) {
        wrapper.setLoadOnStartup(loadOnStartup);
    }


    public Iterable<String> getMappings() {
        HashSet<String> result = new HashSet<String>();
        String[] mappings = wrapper.findMappings();
        for (int i = 0; i < mappings.length; i++) {
            result.add(mappings[i]);
        }
        return Collections.unmodifiableSet(result);
    }


    public String getClassName() {
        return wrapper.getServletClass();
    }


    public Map<String, String> getInitParameters() {
        HashMap<String, String> result = new HashMap<String, String>();
        String[] names = wrapper.findInitParameters();
        for (int i = 0; i < names.length; i++) {
            result.put(names[i], wrapper.getInitParameter(names[i]));
        }
        return Collections.unmodifiableMap(result);
    }


    public String getName() {
        return wrapper.getName();
    }


}
