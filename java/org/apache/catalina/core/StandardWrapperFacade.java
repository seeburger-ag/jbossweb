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


import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

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

public final class StandardWrapperFacade
    implements ServletRegistration, ServletConfig {


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


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


    public void addMapping(String... urlPatterns) {
        if (((Context) wrapper.getParent()).isInitialized()) {
            throw new IllegalStateException(sm.getString
                    ("servletRegistration.addServletMapping.ise", ((Context) wrapper.getParent()).getPath()));
        }
        if (urlPatterns == null) {
            return;
        }
        for (int i = 0; i < urlPatterns.length; i++) {
            ((Context) wrapper.getParent()).addServletMapping(urlPatterns[i], wrapper.getName());
        }
    }


    public void setAsyncSupported(boolean asyncSupported) {
        wrapper.setAsyncSupported(asyncSupported);
    }


    public void setDescription(String description) {
        wrapper.setDescription(description);
    }


    public boolean setInitParameter(String name, String value) {
        wrapper.addInitParameter(name, value);
        // FIXME: return value
        return true;
    }


    public void setInitParameters(Map<String, String> initParameters) {
        Iterator<String> parameterNames = initParameters.keySet().iterator();
        while (parameterNames.hasNext()) {
            String parameterName = parameterNames.next();
            wrapper.addInitParameter(parameterName, initParameters.get(parameterName));
        }
    }


    public void setLoadOnStartup(int loadOnStartup) {
        wrapper.setLoadOnStartup(loadOnStartup);
    }


}
