/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.core;


import static org.jboss.web.CatalinaMessages.MESSAGES;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;

import org.apache.catalina.Context;
import org.apache.catalina.deploy.Multipart;
import org.apache.catalina.Wrapper;


/**
 * Facade for the <b>StandardWrapper</b> object.
 *
 * @author Remy Maucharat
 * @version $Revision: 1250 $ $Date: 2009-11-06 18:08:04 +0100 (Fri, 06 Nov 2009) $
 */

public class StandardWrapperFacade
    implements ServletRegistration, ServletConfig {


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
        if (!((Context) wrapper.getParent()).isStarting()) {
            throw MESSAGES.cannotAddServletRegistrationAfterInit(((Context) wrapper.getParent()).getPath());
        }
        if (urlPatterns == null || urlPatterns.length == 0) {
            throw MESSAGES.invalidServletRegistrationArguments();
        }
        for (String urlPattern : urlPatterns) {
			Context context = ((Context) wrapper.getParent());
            String wrapperName = context.findServletMapping(urlPattern);
            if (wrapperName != null) {
                Wrapper servletWrapper = (Wrapper) context.findChild(wrapperName);
                if (servletWrapper.isOverridable()) {
                    // Some Wrappers (from global and host web.xml) may be
                    // overridden rather than generating a conflict
                    context.removeServletMapping(urlPattern);
                } else {
                    conflicts.add(urlPattern);
                }
			}
        }
        if (conflicts.isEmpty()) {
            for (String urlPattern : urlPatterns) {
                ((Context) wrapper.getParent()).addServletMapping(urlPattern, wrapper.getName());
            }
        }
        return conflicts;
    }


    public void setAsyncSupported(boolean asyncSupported) {
        if (!((Context) wrapper.getParent()).isStarting()) {
            throw MESSAGES.cannotAddServletRegistrationAfterInit(((Context) wrapper.getParent()).getPath());
        }
        wrapper.setAsyncSupported(asyncSupported);
    }


    public void setDescription(String description) {
        wrapper.setDescription(description);
    }


    public boolean setInitParameter(String name, String value) {
        if (!((Context) wrapper.getParent()).isStarting()) {
            throw MESSAGES.cannotAddServletRegistrationAfterInit(((Context) wrapper.getParent()).getPath());
        }
        if (name == null || value == null) {
            throw MESSAGES.invalidServletRegistrationArguments();
        }
        if (wrapper.findInitParameter(name) == null) {
            wrapper.addInitParameter(name, value);
            return true;
        } else {
            return false;
        }
    }


    public Set<String> setInitParameters(Map<String, String> initParameters) {
        if (!((Context) wrapper.getParent()).isStarting()) {
            throw MESSAGES.cannotAddServletRegistrationAfterInit(((Context) wrapper.getParent()).getPath());
        }
        if (initParameters == null) {
            throw MESSAGES.invalidServletRegistrationArguments();
        }
        Set<String> conflicts = new HashSet<String>();
        Iterator<String> parameterNames = initParameters.keySet().iterator();
        while (parameterNames.hasNext()) {
            String parameterName = parameterNames.next();
            if (wrapper.findInitParameter(parameterName) != null) {
                conflicts.add(parameterName);
            } else {
                String value = initParameters.get(parameterName);
                if (value == null) {
                    throw MESSAGES.invalidServletRegistrationArguments();
                }
                wrapper.addInitParameter(parameterName, value);
            }
        }
        return conflicts;
    }


    public void setLoadOnStartup(int loadOnStartup) {
        if (!((Context) wrapper.getParent()).isStarting()) {
            throw MESSAGES.cannotAddServletRegistrationAfterInit(((Context) wrapper.getParent()).getPath());
        }
        wrapper.setLoadOnStartup(loadOnStartup);
    }


    public Collection<String> getMappings() {
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
    
    
    public String getRunAsRole() {
        return wrapper.getRunAs();
    }

    public void setRunAsRole(String roleName) {
        if (!((Context) wrapper.getParent()).isStarting()) {
            throw MESSAGES.cannotAddServletRegistrationAfterInit(((Context) wrapper.getParent()).getPath());
        }
        if (roleName == null) {
            throw MESSAGES.invalidServletRegistrationArguments();
        }
        wrapper.setRunAs(roleName);
    }
    
    public Set<String> setServletSecurity(ServletSecurityElement servletSecurity) {
        if (!((Context) wrapper.getParent()).isStarting()) {
            throw MESSAGES.cannotAddServletRegistrationAfterInit(((Context) wrapper.getParent()).getPath());
        }
        if (servletSecurity == null) {
            throw MESSAGES.invalidServletRegistrationArguments();
        }
        return wrapper.setServletSecurity(servletSecurity);
    }
    
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {
        if (!((Context) wrapper.getParent()).isStarting()) {
            throw MESSAGES.cannotAddServletRegistrationAfterInit(((Context) wrapper.getParent()).getPath());
        }
        if (multipartConfig == null) {
            throw MESSAGES.invalidServletRegistrationArguments();
        }
        Multipart multipart = new Multipart();
        multipart.setLocation(multipartConfig.getLocation());
        multipart.setMaxFileSize(multipartConfig.getMaxFileSize());
        multipart.setMaxRequestSize(multipartConfig.getMaxRequestSize());
        multipart.setFileSizeThreshold(multipartConfig.getFileSizeThreshold());
        wrapper.setMultipartConfig(multipart);
    }
    
}
