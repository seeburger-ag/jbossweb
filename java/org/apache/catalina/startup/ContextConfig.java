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


package org.apache.catalina.startup;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.DispatcherType;
import javax.servlet.HttpMethodConstraintElement;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletSecurityElement;
import javax.servlet.annotation.HandlesTypes;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;

import org.apache.catalina.Authenticator;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.JarRepository;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.ContextJarRepository;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.deploy.WebAbsoluteOrdering;
import org.apache.catalina.deploy.WebOrdering;
import org.apache.catalina.deploy.jsp.TagLibraryInfo;
import org.apache.catalina.util.StringManager;
import org.apache.naming.resources.JARDirContext;
import org.apache.naming.resources.ProxyDirContext;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSet;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 * Startup event listener for a <b>Context</b> that configures the properties
 * of that Context, and the associated defined servlets.
 *
 * @author Craig R. McClanahan
 * @author Jean-Francois Arcand
 * @version $Revision$ $Date$
 */

public class ContextConfig
    implements LifecycleListener {

    protected static org.jboss.logging.Logger log=
        org.jboss.logging.Logger.getLogger( ContextConfig.class );

    // ----------------------------------------------------- Instance Variables


    /**
     * Custom mappings of login methods to authenticators
     */
    protected Map customAuthenticators;


    /**
     * The set of Authenticators that we know how to configure.  The key is
     * the name of the implemented authentication method, and the value is
     * the fully qualified Java class name of the corresponding Valve.
     */
    protected static Properties authenticators = null;


    /**
     * The Context we are associated with.
     */
    protected Context context = null;


    /**
     * The default web application's context file location.
     */
    protected String defaultContextXml = null;
    
    
    /**
     * The default web application's deployment descriptor location.
     */
    protected String defaultWebXml = null;
    
    
    /**
     * Track any fatal errors during startup configuration processing.
     */
    protected boolean ok = false;


    /**
     * Any parse error which occurred while parsing XML descriptors.
     */
    protected SAXParseException parseException = null;

    
    /**
     * Original docBase.
     */
    protected String originalDocBase = null;
    

    /**
     * The string resources for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * The <code>Digester</code> we will use to process web application
     * context files.
     */
    protected static Digester contextDigester = null;
    
    
    /**
     * The <code>Digester</code> we will use to process web application
     * deployment descriptor files.
     */
    protected static Digester webDigester = null;
    
    
    /**
     * The <code>Digester</code> we will use to process web application
     * fragment descriptor files.
     */
    protected static Digester webFragmentDigester = null;
    
    
    /**
     * The <code>Digester</code> we will use to process tag library
     * descriptor files.
     */
    protected static Digester tldDigester = null;
    
    
    /**
     * The <code>Digester</code> we will use to parse fragment ordering.
     */
    protected static Digester fragmentOrderingDigester = null;
    
    
    /**
     * The <code>Digester</code> we will use to parse absolute ordering in web.xml.
     */
    protected static Digester orderingDigester = null;
    
    
    /**
     * The <code>Rule</code> used to parse the web.xml.
     */
    protected static WebRuleSet webRuleSet = new WebRuleSet();


    /**
     * The <code>Rule</code> used to parse web-fragment.xml files.
     */
    protected static WebRuleSet webFragmentRuleSet = new WebRuleSet("", true);


    /**
     * Deployment count.
     */
    protected static long deploymentCount = 0L;
    
    
    protected static final LoginConfig DUMMY_LOGIN_CONFIG =
                                new LoginConfig("NONE", null, null, null);


    protected ArrayList<String> overlays = new ArrayList<String>();
    protected ArrayList<String> webFragments = new ArrayList<String>();
    protected Map<String, Set<String>> TLDs = new HashMap<String, Set<String>>();
    protected Map<String, ServletContainerInitializerInfo> servletContainerInitializerInfos = 
        new HashMap<String, ServletContainerInitializerInfo>();
    protected LinkedList<String> order = new LinkedList<String>();
    
    /**
     * Used to speed up scanning for the services interest classes.
     */
    protected Class<?>[] handlesTypesArray = null;
    protected Map<Class<?>, ServletContainerInitializerInfo> handlesTypes = 
        new HashMap<Class<?>, ServletContainerInitializerInfo>();


    // ------------------------------------------------------------- Properties


    /**
     * Return the location of the default deployment descriptor
     */
    public String getDefaultWebXml() {
        if( defaultWebXml == null ) {
            defaultWebXml=Constants.DefaultWebXml;
        }

        return (this.defaultWebXml);

    }


    /**
     * Set the location of the default deployment descriptor
     *
     * @param path Absolute/relative path to the default web.xml
     */
    public void setDefaultWebXml(String path) {

        this.defaultWebXml = path;

    }


    /**
     * Return the location of the default context file
     */
    public String getDefaultContextXml() {
        if( defaultContextXml == null ) {
            defaultContextXml=Constants.DefaultContextXml;
        }

        return (this.defaultContextXml);

    }


    /**
     * Set the location of the default context file
     *
     * @param path Absolute/relative path to the default context.xml
     */
    public void setDefaultContextXml(String path) {

        this.defaultContextXml = path;

    }


    /**
     * Sets custom mappings of login methods to authenticators.
     *
     * @param customAuthenticators Custom mappings of login methods to
     * authenticators
     */
    public void setCustomAuthenticators(Map customAuthenticators) {
        this.customAuthenticators = customAuthenticators;
    }


    // -------------------------------------------------- WarComponents Methods


    public Iterator<String> getOverlays() {
        return overlays.iterator();
    }


    public Iterator<String> getWebFragments() {
        return order.iterator();
    }


    public Map<String, Set<String>> getTLDs() {
        return TLDs;
    }
    
    
    public Map<String, ServletContainerInitializerInfo> getServletContainerInitializerInfo() {
        return servletContainerInitializerInfos;
    }
    
    
    // --------------------------------------------------------- Public Methods


    /**
     * Process events for an associated Context.
     *
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        // Identify the context we are associated with
        try {
            context = (Context) event.getLifecycle();
        } catch (ClassCastException e) {
            log.error(sm.getString("contextConfig.cce", event.getLifecycle()), e);
            return;
        }

        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT)) {
            start();
        } else if (event.getType().equals(Lifecycle.BEFORE_START_EVENT)) {
            beforeStart();
        } else if (event.getType().equals(Lifecycle.AFTER_START_EVENT)) {
            // Restore docBase for management tools
            if (originalDocBase != null) {
                String docBase = context.getDocBase();
                context.setDocBase(originalDocBase);
                originalDocBase = docBase;
            }
            // Invoke Servlet container initializer: instantiate and call onStartup
            if (ok) {
                Iterator<ServletContainerInitializerInfo> initializers = 
                    getServletContainerInitializerInfo().values().iterator();
                while (initializers.hasNext()) {
                    ServletContainerInitializerInfo service = initializers.next();
                    try {
                        ServletContainerInitializer servletContainerInitializer = 
                            (ServletContainerInitializer) service.getServletContainerInitializer().newInstance();
                        servletContainerInitializer.onStartup(service.getStartupNotifySet(), context.getServletContext());
                    } catch (Throwable t) {
                        log.error(sm.getString("contextConfig.servletContainerInitializer", 
                                service.getServletContainerInitializer().getName()), t);
                        ok = false;
                    }
                }
            }
        } else if (event.getType().equals(Context.COMPLETE_CONFIG_EVENT)) {
            completeConfig();
        } else if (event.getType().equals(Lifecycle.STOP_EVENT)) {
            if (originalDocBase != null) {
                String docBase = context.getDocBase();
                context.setDocBase(originalDocBase);
                originalDocBase = docBase;
            }
            stop();
        } else if (event.getType().equals(Lifecycle.INIT_EVENT)) {
            init();
        } else if (event.getType().equals(Lifecycle.DESTROY_EVENT)) {
            destroy();
        }

    }


    // -------------------------------------------------------- Protected Methods


    /**
     * Process the application classes annotations, if it exists.
     */
    protected void processConfigAnnotations(Class<?> clazz) {

        if (clazz.isAnnotationPresent(WebFilter.class)) {
            WebFilter annotation = clazz.getAnnotation(WebFilter.class);
            // Add servlet filter
            String filterName = annotation.filterName();
            FilterDef filterDef = new FilterDef();
            filterDef.setFilterName(annotation.filterName());
            filterDef.setFilterClass(clazz.getName());
            WebInitParam[] params = annotation.initParams();
            for (int i = 0; i < params.length; i++) {
                filterDef.addInitParameter(params[i].name(), params[i].value());
            }
            context.addFilterDef(filterDef);
            FilterMap filterMap = new FilterMap();
            filterMap.setFilterName(filterName);
            String[] urlPatterns = annotation.urlPatterns();
            if (urlPatterns != null) {
                for (int i = 0; i < urlPatterns.length; i++) {
                    filterMap.addURLPattern(urlPatterns[i]);
                }
            }
            String[] servletNames = annotation.servletNames();
            if (servletNames != null) {
                for (int i = 0; i < servletNames.length; i++) {
                    filterMap.addServletName(servletNames[i]);
                }
            }
            DispatcherType[] dispatcherTypes = annotation.dispatcherTypes();
            if (dispatcherTypes != null) {
                for (int i = 0; i < dispatcherTypes.length; i++) {
                    filterMap.setDispatcher(dispatcherTypes[i].toString());
                }
            }
            context.addFilterMap(filterMap);
        }
        if (clazz.isAnnotationPresent(WebServlet.class)) {
            WebServlet annotation = clazz.getAnnotation(WebServlet.class);
            // Add servlet
            Wrapper wrapper = context.createWrapper();
            wrapper.setName(annotation.name());
            wrapper.setServletClass(clazz.getName());
            wrapper.setLoadOnStartup(annotation.loadOnStartup());
            WebInitParam[] params = annotation.initParams();
            for (int i = 0; i < params.length; i++) {
                wrapper.addInitParameter(params[i].name(), params[i].value());
            }
            context.addChild(wrapper);
            String[] urlPatterns = annotation.urlPatterns();
            if (urlPatterns != null) {
                for (int i = 0; i < urlPatterns.length; i++) {
                    context.addServletMapping(urlPatterns[i], annotation.name());
                }
            }
        }
        if (clazz.isAnnotationPresent(WebListener.class)) {
            // Add listener
            context.addApplicationListener(clazz.getName());
        }

    }


    /**
     * Process the application configuration file, if it exists.
     */
    protected void applicationWebConfig() {

        String altDDName = null;

        // Open the application web.xml file, if it exists
        InputStream stream = null;
        ServletContext servletContext = context.getServletContext();
        if (servletContext != null) {
            altDDName = (String)servletContext.getAttribute(
                                                        Globals.ALT_DD_ATTR);
            if (altDDName != null) {
                try {
                    stream = new FileInputStream(altDDName);
                } catch (FileNotFoundException e) {
                    log.error(sm.getString("contextConfig.altDDNotFound",
                                           altDDName));
                }
            }
            else {
                stream = servletContext.getResourceAsStream
                    (Constants.ApplicationWebXml);
            }
        }
        if (stream == null) {
            if (log.isDebugEnabled()) {
                log.debug(sm.getString("contextConfig.applicationMissing") + " " + context);
            }
            return;
        }
        
        URL url=null;
        // Process the application web.xml file
        synchronized (webDigester) {
            try {
                if (altDDName != null) {
                    url = new File(altDDName).toURI().toURL();
                } else {
                    url = servletContext.getResource(
                                                Constants.ApplicationWebXml);
                }
                if( url!=null ) {
                    InputSource is = new InputSource(url.toExternalForm());
                    is.setByteStream(stream);
                    if (context instanceof StandardContext) {
                        ((StandardContext) context).setReplaceWelcomeFiles(true);
                    }
                    webDigester.push(context);
                    webDigester.setErrorHandler(new ContextErrorHandler());

                    if(log.isDebugEnabled()) {
                        log.debug("Parsing application web.xml file at " + url.toExternalForm());
                    }

                    webDigester.parse(is);

                    if (parseException != null) {
                        ok = false;
                    }
                } else {
                    log.info("No web.xml, using defaults " + context );
                }
            } catch (SAXParseException e) {
                log.error(sm.getString("contextConfig.applicationParse", url.toExternalForm()), e);
                log.error(sm.getString("contextConfig.applicationPosition",
                                 "" + e.getLineNumber(),
                                 "" + e.getColumnNumber()));
                ok = false;
            } catch (Exception e) {
                log.error(sm.getString("contextConfig.applicationParse", url.toExternalForm()), e);
                ok = false;
            } finally {
                webDigester.reset();
                webRuleSet.recycle();
                parseException = null;
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    log.error(sm.getString("contextConfig.applicationClose"), e);
                }
            }
        }

    }
    
    
    /**
     * Parse TLDs. This is separate, and is not subject to the order defined. Also,
     * all TLDs from all JARs are parsed.
     */
    protected void applicationTldConfig() {
        
        Map<String, Set<String>> TLDs = getTLDs();
        Set<String> warTLDs = TLDs.get("");
        ArrayList<TagLibraryInfo> tagLibraries = new ArrayList<TagLibraryInfo>();

        // Parse all TLDs from the WAR
        Iterator<String> warTLDsIterator = warTLDs.iterator();
        InputStream stream = null;
        while (warTLDsIterator.hasNext()) {
            String tldPath = warTLDsIterator.next();
            try {
                stream = context.getServletContext().getResourceAsStream(tldPath);
                if (stream == null) {
                    log.error(sm.getString("contextConfig.tldResourcePath", tldPath));
                    ok = false;
                } else {
                    synchronized (tldDigester) {
                        TagLibraryInfo tagLibraryInfo = new TagLibraryInfo();
                        try {
                            tldDigester.push(tagLibraryInfo);
                            tldDigester.parse(new InputSource(stream));
                        } finally {
                            tldDigester.reset();
                        }
                        tagLibraryInfo.setLocation("");
                        tagLibraryInfo.setPath(tldPath);
                        tagLibraries.add(tagLibraryInfo);
                        context.addJspTagLibrary(tagLibraryInfo);
                        context.addJspTagLibrary(tldPath, tagLibraryInfo);
                    }
                }
            } catch (Exception e) {
                log.error(sm.getString("contextConfig.tldFileException", tldPath,
                        context.getPath()), e);
                ok = false;
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Throwable t) {
                        // Ignore
                    }
                }
            }

        }

        // Parse all TLDs from JARs
        Iterator<String> jarPaths = TLDs.keySet().iterator();
        while (jarPaths.hasNext()) {
            String jarPath = jarPaths.next();
            if (jarPath.equals("")) {
                continue;
            }
            JarRepository jarRepository = context.getJarRepository();
            JarFile jarFile = jarRepository.findJar(jarPath);
            Iterator<String> jarTLDsIterator =  TLDs.get(jarPath).iterator();
            while (jarTLDsIterator.hasNext()) {
                try {
                    String tldPath = jarTLDsIterator.next();
                    stream = jarFile.getInputStream(jarFile.getEntry(tldPath));
                    synchronized (tldDigester) {
                        TagLibraryInfo tagLibraryInfo = new TagLibraryInfo();
                        try {
                            tldDigester.push(tagLibraryInfo);
                            tldDigester.parse(new InputSource(stream));
                        } finally {
                            tldDigester.reset();
                            if (stream != null) {
                                try {
                                    stream.close();
                                } catch (Throwable t) {
                                    // Ignore
                                }
                            }
                        }
                        tagLibraryInfo.setLocation(jarPath);
                        tagLibraryInfo.setPath(tldPath);
                        tagLibraries.add(tagLibraryInfo);
                        context.addJspTagLibrary(tagLibraryInfo);
                        if (tldPath.equals("META-INF/taglib.tld")) {
                            context.addJspTagLibrary(jarPath, tagLibraryInfo);
                        }
                    }
                } catch (Exception e) {
                    log.error(sm.getString("contextConfig.tldJarException",
                            jarPath, context.getPath()), e);
                    ok = false;
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (Throwable t) {
                            // Ignore
                        }
                    }
                }
            }
        }
        
        // Add additional TLDs URIs from explicit web config
        String taglibs[] = context.findTaglibs();
        for (int i = 0; i < taglibs.length; i++) {
            String uri = taglibs[i];
            String path = context.findTaglib(taglibs[i]);
            String location = "";
            if (path.indexOf(':') == -1 && !path.startsWith("/")) {
                path = "/WEB-INF/" + path;
            }
            if (path.endsWith(".jar")) {
                location = path;
                path = "META-INF/taglib.tld";
            }
            for (int j = 0; j < tagLibraries.size(); j++) {
                TagLibraryInfo tagLibraryInfo = tagLibraries.get(j);
                if (tagLibraryInfo.getLocation().equals(location) && tagLibraryInfo.getPath().equals(path)) {
                    context.addJspTagLibrary(uri, tagLibraryInfo);
                }
            }
        }

    }
    

    /**
     * Set up an Authenticator automatically if required, and one has not
     * already been configured.
     */
    protected void authenticatorConfig() {

        // Does this Context require an Authenticator?
        SecurityConstraint constraints[] = context.findConstraints();
        if ((constraints == null) || (constraints.length == 0))
            return;
        LoginConfig loginConfig = context.getLoginConfig();
        if (loginConfig == null) {
            loginConfig = DUMMY_LOGIN_CONFIG;
            context.setLoginConfig(loginConfig);
        }

        // Has an authenticator been configured already?
        if (context instanceof Authenticator)
            return;
        if (context instanceof ContainerBase) {
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            if (pipeline != null) {
                Valve basic = pipeline.getBasic();
                if ((basic != null) && (basic instanceof Authenticator))
                    return;
                Valve valves[] = pipeline.getValves();
                for (int i = 0; i < valves.length; i++) {
                    if (valves[i] instanceof Authenticator)
                        return;
                }
            }
        } else {
            return;     // Cannot install a Valve even if it would be needed
        }

        // Has a Realm been configured for us to authenticate against?
        if (context.getRealm() == null) {
            log.error(sm.getString("contextConfig.missingRealm"));
            ok = false;
            return;
        }

        /*
         * First check to see if there is a custom mapping for the login
         * method. If so, use it. Otherwise, check if there is a mapping in
         * org/apache/catalina/startup/Authenticators.properties.
         */
        Valve authenticator = null;
        if (customAuthenticators != null) {
            authenticator = (Valve)
                customAuthenticators.get(loginConfig.getAuthMethod());
        }
        if (authenticator == null) {
            // Load our mapping properties if necessary
            if (authenticators == null) {
                try {
                    InputStream is=this.getClass().getClassLoader().getResourceAsStream("org/apache/catalina/startup/Authenticators.properties");
                    if( is!=null ) {
                        authenticators = new Properties();
                        authenticators.load(is);
                    } else {
                        log.error(sm.getString(
                                "contextConfig.authenticatorResources"));
                        ok=false;
                        return;
                    }
                } catch (IOException e) {
                    log.error(sm.getString(
                                "contextConfig.authenticatorResources"), e);
                    ok = false;
                    return;
                }
            }

            // Identify the class name of the Valve we should configure
            String authenticatorName = null;
            authenticatorName =
                    authenticators.getProperty(loginConfig.getAuthMethod());
            if (authenticatorName == null) {
                log.error(sm.getString("contextConfig.authenticatorMissing",
                                 loginConfig.getAuthMethod()));
                ok = false;
                return;
            }

            // Instantiate and install an Authenticator of the requested class
            try {
                Class authenticatorClass = Class.forName(authenticatorName);
                authenticator = (Valve) authenticatorClass.newInstance();
            } catch (Throwable t) {
                log.error(sm.getString(
                                    "contextConfig.authenticatorInstantiate",
                                    authenticatorName),
                          t);
                ok = false;
            }
        }

        if (authenticator instanceof Authenticator) {
            context.setAuthenticator((Authenticator) authenticator);
        }
        if (authenticator != null && context instanceof ContainerBase) {
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            if (pipeline != null) {
                ((ContainerBase) context).addValve(authenticator);
                if (log.isDebugEnabled()) {
                    log.debug(sm.getString(
                                    "contextConfig.authenticatorConfigured",
                                    loginConfig.getAuthMethod()));
                }
            }
        }

    }


    /**
     * Create (if necessary) and return a Digester configured to process the
     * web application deployment descriptor (web.xml).
     */
    protected static Digester createWebDigester() {
        return DigesterFactory.newDigester(Globals.XML_NAMESPACE_AWARE, Globals.XML_VALIDATION, webRuleSet);
    }


    /**
     * Create (if necessary) and return a Digester configured to process the
     * web application fragment descriptors (web-fragment.xml).
     */
    protected static Digester createWebFragmentDigester() {
        return DigesterFactory.newDigester(Globals.XML_NAMESPACE_AWARE, Globals.XML_VALIDATION, webFragmentRuleSet);
    }


    /**
     * Create (if necessary) and return a Digester configured to process tag 
     * library descriptors.
     */
    protected static Digester createTldDigester() {
        return DigesterFactory.newDigester(Globals.XML_NAMESPACE_AWARE, Globals.XML_VALIDATION, new TldRuleSet());
    }


    /**
     * Create (if necessary) and return a Digester configured to process web fragments ordering.
     */
    protected static Digester createFragmentOrderingDigester() {
        return DigesterFactory.newDigester(Globals.XML_NAMESPACE_AWARE, 
                Globals.XML_VALIDATION, new WebOrderingRuleSet());
    }


    /**
     * Create (if necessary) and return a Digester configured to process the
     * context configuration descriptor for an application.
     */
    protected static Digester createContextDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);
        RuleSet contextRuleSet = new ContextRuleSet("", false);
        digester.addRuleSet(contextRuleSet);
        RuleSet namingRuleSet = new NamingRuleSet("Context/");
        digester.addRuleSet(namingRuleSet);
        return digester;
    }


    protected String getBaseDir() {
        Container engineC=context.getParent().getParent();
        if( engineC instanceof StandardEngine ) {
            return ((StandardEngine)engineC).getBaseDir();
        }
        return System.getProperty("catalina.base");
    }

    /**
     * Process the default configuration file, if it exists.
     * The default config must be read with the container loader - so
     * container servlets can be loaded
     */
    protected void defaultWebConfig() {
        long t1=System.currentTimeMillis();

        // Open the default web.xml file, if it exists
        if( defaultWebXml==null && context instanceof StandardContext ) {
            defaultWebXml=((StandardContext)context).getDefaultWebXml();
        }
        // set the default if we don't have any overrides
        if( defaultWebXml==null ) getDefaultWebXml();

        File file = new File(this.defaultWebXml);
        if (!file.isAbsolute()) {
            file = new File(getBaseDir(),
                            this.defaultWebXml);
        }

        InputStream stream = null;
        InputSource source = null;

        try {
            if ( ! file.exists() ) {
                // Use getResource and getResourceAsStream
                stream = getClass().getClassLoader()
                    .getResourceAsStream(defaultWebXml);
                if( stream != null ) {
                    source = new InputSource
                            (getClass().getClassLoader()
                            .getResource(defaultWebXml).toString());
                } 
                if( stream== null ) { 
                    // maybe embedded
                    stream = getClass().getClassLoader()
                        .getResourceAsStream("web-embed.xml");
                    if( stream != null ) {
                        source = new InputSource
                        (getClass().getClassLoader()
                                .getResource("web-embed.xml").toString());
                    }                                         
                }
                
                if( stream== null ) {
                    log.info("No default web.xml");
                }
            } else {
                source =
                    new InputSource("file://" + file.getAbsolutePath());
                stream = new FileInputStream(file);
                context.addWatchedResource(file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error(sm.getString("contextConfig.defaultMissing") 
                      + " " + defaultWebXml + " " + file , e);
        }

        if (stream != null) {
            processDefaultWebConfig(webDigester, stream, source);
            webRuleSet.recycle();
        }

        long t2=System.currentTimeMillis();
        if( (t2-t1) > 200 )
            log.debug("Processed default web.xml " + file + " "  + ( t2-t1));

        stream = null;
        source = null;

        String resourceName = getHostConfigPath(Constants.HostWebXml);
        file = new File(getConfigBase(), resourceName);
        
        try {
            if ( ! file.exists() ) {
                // Use getResource and getResourceAsStream
                stream = getClass().getClassLoader()
                    .getResourceAsStream(resourceName);
                if( stream != null ) {
                    source = new InputSource
                            (getClass().getClassLoader()
                            .getResource(resourceName).toString());
                }
            } else {
                source =
                    new InputSource("file://" + file.getAbsolutePath());
                stream = new FileInputStream(file);
            }
        } catch (Exception e) {
            log.error(sm.getString("contextConfig.defaultMissing") 
                      + " " + resourceName + " " + file , e);
        }

        if (stream != null) {
            processDefaultWebConfig(webDigester, stream, source);
            webRuleSet.recycle();
        }

    }


    /**
     * Process a default web.xml.
     */
    protected void processDefaultWebConfig(Digester digester, InputStream stream, 
            InputSource source) {

        if (log.isDebugEnabled())
            log.debug("Processing context [" + context.getName() 
                    + "] web configuration resource " + source.getSystemId());

        // Process the default web.xml file
        synchronized (digester) {
            try {
                source.setByteStream(stream);
                
                if (context instanceof StandardContext)
                    ((StandardContext) context).setReplaceWelcomeFiles(true);
                digester.setClassLoader(this.getClass().getClassLoader());
                digester.setUseContextClassLoader(false);
                digester.push(context);
                digester.setErrorHandler(new ContextErrorHandler());
                digester.parse(source);
                if (parseException != null) {
                    ok = false;
                }
            } catch (SAXParseException e) {
                log.error(sm.getString("contextConfig.defaultParse"), e);
                log.error(sm.getString("contextConfig.defaultPosition",
                                 "" + e.getLineNumber(),
                                 "" + e.getColumnNumber()));
                ok = false;
            } catch (Exception e) {
                log.error(sm.getString("contextConfig.defaultParse"), e);
                ok = false;
            } finally {
                digester.reset();
                parseException = null;
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    log.error(sm.getString("contextConfig.defaultClose"), e);
                }
            }
        }
    }

    
    /**
     * Parse fragments order.
     */
    protected void createFragmentsOrder() {
        
        WebAbsoluteOrdering absoluteOrdering = context.getWebAbsoluteOrdering();
        List<WebOrdering> orderings = new ArrayList<WebOrdering>();
        HashSet<String> jarsSet = new HashSet<String>();
        boolean fragmentFound = false;
        
        // Parse the ordering defined in web fragments
        JarRepository jarRepository = context.getJarRepository();
        JarFile[] jars = jarRepository.findJars();
        for (int i = 0; i < jars.length; i++) {
            // Find webapp descriptor fragments
            jarsSet.add(jars[i].getName());
            JarFile jarFile = jars[i];
            InputStream is = null;
            ZipEntry entry = jarFile.getEntry(Globals.WEB_FRAGMENT_PATH);
            if (entry != null) {
                fragmentFound = true;
                try {
                    webFragments.add(jars[i].getName());
                    is = jarFile.getInputStream(entry);
                    InputSource input = new InputSource((new File(jars[i].getName())).toURI().toURL().toExternalForm());
                    input.setByteStream(is);
                    synchronized (fragmentOrderingDigester) {
                        try {
                            fragmentOrderingDigester.parse(input);
                            WebOrdering ordering = (WebOrdering) fragmentOrderingDigester.peek();
                            if (ordering != null) {
                                ordering.setJar(jars[i].getName());
                                orderings.add(ordering);
                            }
                        } finally {
                            fragmentOrderingDigester.reset();
                        }
                    }
                } catch (Exception e) {
                    log.error(sm.getString("contextConfig.fragmentOrderingParse", jars[i].getName()), e);
                    ok = false;
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            } else {
                // If there is no fragment, still consider it for ordering as a
                // fragment specifying no name and no order
                WebOrdering ordering = new WebOrdering();
                ordering.setJar(jars[i].getName());
                orderings.add(ordering);
            }
        }
        if (!fragmentFound) {
            // Drop the order as there is no fragment in the webapp
            orderings.clear();
        }
        
        // Generate web fragments parsing order
        if (absoluteOrdering != null) {
            // Absolute ordering from web.xml, any relative fragment ordering is ignored
            List<String> fragmentNames = absoluteOrdering.getOrder();
            int otherPos = -1;
            for (int i = 0; i < fragmentNames.size(); i++) {
                String fragmentName = fragmentNames.get(i);
                if (fragmentName.equals("*")) {
                    if (otherPos >= 0) {
                        log.error(sm.getString("contextConfig.invalidAbsoluteOrder"));
                        ok = false;
                    }
                    otherPos = i;
                } else {
                    Iterator<WebOrdering> orderingsIterator = orderings.iterator();
                    while (orderingsIterator.hasNext()) {
                        WebOrdering ordering = orderingsIterator.next();
                        if (fragmentName.equals(ordering.getName())) {
                            order.add(ordering.getJar());
                            jarsSet.remove(ordering.getJar());
                            break;
                        }
                    }
                }
            }
            if (otherPos >= 0) {
                order.addAll(otherPos, jarsSet);
            }
        } else if (orderings.size() > 0) {
            // Resolve relative ordering
            try {
                OrderingResolver.resolveOrder(orderings, order);
            } catch (IllegalStateException e) {
                log.error(e.getMessage(), e);
                ok = false;
            }
        } else {
            // No order specified
            order.addAll(jarsSet);
        }
        
    }
    
    
    /**
     * Get the jar name corresponding to the ordering name.
     */
    protected String getJarName(List<WebOrdering> orderings, String name) {
        Iterator<WebOrdering> orderingsIterator = orderings.iterator();
        while (orderingsIterator.hasNext()) {
            WebOrdering ordering = orderingsIterator.next();
            if (name.equals(ordering.getName())) {
                return ordering.getJar();
            }
        }
        return null;
    }
    
    
    /**
     * Process additional descriptors: TLDs, web fragments, and map overlays.
     */
    protected void applicationExtraDescriptorsConfig() {
        
        JarRepository jarRepository = context.getJarRepository();

        HashSet<String> warTLDs = new HashSet<String>();

        // Find any TLD file in /WEB-INF
        DirContext resources = context.getResources();
        if (resources != null) {
            tldScanResourcePathsWebInf(resources, "/WEB-INF", warTLDs);
        }
        TLDs.put("", warTLDs);
        
        File[] explodedJars = jarRepository.findExplodedJars();
        for (int i = 0; i < explodedJars.length; i++) {
            scanClasses(explodedJars[i], "", !context.getIgnoreAnnotations());
        }
        
        // Parse web fragment according to order
        Iterator<String> orderIterator = order.iterator();
        while (orderIterator.hasNext()) {
            String jar = orderIterator.next();
            JarFile jarFile = jarRepository.findJar(jar);
            InputStream is = null;
            ZipEntry entry = jarFile.getEntry(Globals.WEB_FRAGMENT_PATH);
            if (entry != null) {
                try {
                    is = jarFile.getInputStream(entry);
                    InputSource input = new InputSource((new File(jar)).toURI().toURL().toExternalForm());
                    input.setByteStream(is);
                    synchronized (webFragmentDigester) {
                        try {
                            webFragmentDigester.push(context);
                            webFragmentDigester.setErrorHandler(new ContextErrorHandler());
                            webFragmentDigester.parse(input);
                            if (parseException != null) {
                                ok = false;
                            }
                        } finally {
                            webFragmentDigester.reset();
                            webFragmentRuleSet.recycle();
                            parseException = null;
                        }
                    }
                } catch (Exception e) {
                    log.error(sm.getString("contextConfig.applicationParse", jar), e);
                    ok = false;
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }
            // Scan the JAR for TLDs and annotations
            scanJar(jarFile, true);
        }
        
        // Process any Jar not in the order
        JarFile[] jarFiles = jarRepository.findJars();
        for (int i = 0; i < jarFiles.length; i++) {
            if (!order.contains(jarFiles[i].getName())) {
                // Scan the JAR for TLDs only
                scanJar(jarFiles[i], false);
            }
        }
        
    }
    
    
    protected void scanJar(JarFile jarFile, boolean annotations) {
        
        // Scan Jar for annotations and TLDs
        HashSet<String> jarTLDs = new HashSet<String>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.endsWith(".class")) {
                String className = getClassName(entry.getName());
                scanClass(className, annotations);
            } else if (name.startsWith("META-INF/") && name.endsWith(".tld")) {
                jarTLDs.add(name);
            }
        }
        if (jarTLDs.size() > 0) {
            TLDs.put(jarFile.getName(), jarTLDs);
        }

    }
    

    /**
     * Scans the web application's subdirectory identified by rootPath,
     * along with its subdirectories, for TLDs.
     *
     * Initially, rootPath equals /WEB-INF. The /WEB-INF/classes and
     * /WEB-INF/lib subdirectories are excluded from the search, as per the
     * JSP 2.0 spec.
     *
     * @param resources The web application's resources
     * @param rootPath The path whose subdirectories are to be searched for
     * TLDs
     * @param tldPaths The set of TLD resource paths to add to
     */
    protected void tldScanResourcePathsWebInf(DirContext resources,
                                            String rootPath,
                                            HashSet<String> tldPaths) {
        try {
            NamingEnumeration<NameClassPair> items = resources.list(rootPath);
            while (items.hasMoreElements()) {
                NameClassPair item = items.nextElement();
                String resourcePath = rootPath + "/" + item.getName();
                if (!resourcePath.endsWith(".tld")
                        && (resourcePath.startsWith("/WEB-INF/classes")
                            || resourcePath.startsWith("/WEB-INF/lib"))) {
                    continue;
                }
                if (resourcePath.endsWith(".tld")) {
                    tldPaths.add(resourcePath);
                } else {
                    tldScanResourcePathsWebInf(resources, resourcePath,
                                               tldPaths);
                }
            }
        } catch (NamingException e) {
            ; // Silent catch: it's valid that no /WEB-INF directory exists
        }
    }


    /**
     * Scan folder containing class files.
     */
    protected void scanClasses(File folder, String path, boolean annotations) {
        String[] files = folder.list();
        for (int i = 0; i < files.length; i++) {
            File file = new File(folder, files[i]);
            if (file.isDirectory()) {
                scanClasses(file, path + "/" + files[i], annotations);
            } else if (files[i].endsWith(".class")) {
                String className = getClassName(path + "/" + files[i]);
                scanClass(className, annotations);
            }
        }
    }
    
    
    protected void scanClass(String className, boolean annotations) {
        if (!annotations && (handlesTypesArray == null)) {
            return;
        }
        try {
            Class<?> clazz = context.getLoader().getClassLoader().loadClass(className);
            if (handlesTypesArray != null) {
                for (int i = 0; i < handlesTypesArray.length; i++) {
                    if (handlesTypesArray[i].isAssignableFrom(clazz)) {
                        ServletContainerInitializerInfo jarServletContainerInitializerService = 
                            handlesTypes.get(handlesTypesArray[i]);
                        jarServletContainerInitializerService.addStartupNotifyClass(clazz);
                    }
                }
            }
            if (annotations &&
                    (clazz.isAnnotationPresent(MultipartConfig.class)
                    || clazz.isAnnotationPresent(WebFilter.class)
                    || clazz.isAnnotationPresent(WebInitParam.class)
                    || clazz.isAnnotationPresent(WebListener.class)
                    || clazz.isAnnotationPresent(WebServlet.class))) {
                processConfigAnnotations(clazz);
            }
        } catch (Throwable t) {
            // Ignore classloading errors here
        }
    }
    
    
    /**
     * Get class name given a path to a classfile.
     * /my/class/MyClass.class -> my.class.MyClass
     */
    protected String getClassName(String filePath) {
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        if (filePath.endsWith(".class")) {
            filePath = filePath.substring(0, filePath.length() - ".class".length());
        }
        return filePath.replace('/', '.');
    }
    
    
    /**
     * Find and parse ServletContainerInitializer service in specified JAR.
     */
    public void applicationServletContainerInitializerConfig() {
        JarRepository jarRepository = context.getJarRepository();
        if (jarRepository != null) {
            JarFile[] jars = jarRepository.findJars();
            for (int i = 0; i < jars.length; i++) {
                scanJarForServletContainerInitializer(jars[i]);
            }
        }
        // Do the same for the context parent
        jarRepository = context.getParent().getJarRepository();
        if (jarRepository != null) {
            JarFile[] jars = jarRepository.findJars();
            for (int i = 0; i < jars.length; i++) {
                scanJarForServletContainerInitializer(jars[i]);
            }
        }
    }
    
    
    /**
     * Find and parse ServletContainerInitializer service in specified JAR.
     */
    public void scanJarForServletContainerInitializer(JarFile file) {
        // Find ServletContainerInitializer services
        JarEntry servletContainerInitializerEntry = file.getJarEntry(Globals.SERVLET_CONTAINER_INITIALIZER_SERVICE_PATH);
        String servletContainerInitializerClassName = null;
        if (servletContainerInitializerEntry != null) {
            // Read Servlet container initializer service file
            InputStream is = null;
            try {
                is = file.getInputStream(servletContainerInitializerEntry);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                servletContainerInitializerClassName = reader.readLine();
                int pos = servletContainerInitializerClassName.indexOf('#');
                if (pos > 0) {
                    servletContainerInitializerClassName = servletContainerInitializerClassName.substring(0, pos);
                }
                servletContainerInitializerClassName = servletContainerInitializerClassName.trim();
            } catch (Exception e) {
                log.warn(sm.getString("contextConfig.servletContainerInitializer", file.getName()), e);
                return;
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    // Ignore
                }
            }
            // Load Servlet container initializer class and read HandlesTypes annotation
            Class<?> servletContainerInitializerClass = null;
            Class<?>[] typesArray = null;
            if (servletContainerInitializerClassName != null) {
                try {
                    servletContainerInitializerClass = context.getLoader().getClassLoader()
                        .loadClass(servletContainerInitializerClassName);
                    if (servletContainerInitializerClass.isAnnotationPresent(HandlesTypes.class)) {
                        HandlesTypes handlesTypes = servletContainerInitializerClass.getAnnotation(HandlesTypes.class);
                        typesArray = handlesTypes.value();
                    }
                } catch (Throwable t) {
                    log.warn(sm.getString("contextConfig.servletContainerInitializer", file.getName()), t);
                    return;
                }
            }
            // Add in jarService map, and add in the local map used to speed up lookups
            ServletContainerInitializerInfo jarServletContainerInitializerService = 
                new ServletContainerInitializerInfo(servletContainerInitializerClass, handlesTypesArray);
            servletContainerInitializerInfos.put(file.getName(), jarServletContainerInitializerService);
            if (typesArray != null) {
                ArrayList<Class<?>> handlesTypesList = new ArrayList<Class<?>>();
                if (handlesTypesArray != null) {
                    for (int i = 0; i < handlesTypesArray.length; i++) {
                        handlesTypesList.add(handlesTypesArray[i]);
                    }
                }
                for (int i = 0; i < typesArray.length; i++) {
                    handlesTypesList.add(typesArray[i]);
                    handlesTypes.put(typesArray[i], jarServletContainerInitializerService);
                }
                handlesTypesArray = handlesTypesList.toArray(handlesTypesArray);
            }
        }
    }
    
    
    /**
     * Process the default configuration file, if it exists.
     */
    protected void contextConfig() {
        
        // Open the default web.xml file, if it exists
        if( defaultContextXml==null && context instanceof StandardContext ) {
            defaultContextXml = ((StandardContext)context).getDefaultContextXml();
        }
        // set the default if we don't have any overrides
        if( defaultContextXml==null ) getDefaultContextXml();

        if (!context.getOverride()) {
            processContextConfig(new File(getBaseDir()), defaultContextXml);
            processContextConfig(getConfigBase(), getHostConfigPath(Constants.HostContextXml));
        }
        if (context.getConfigFile() != null)
            processContextConfig(new File(context.getConfigFile()), null);
        
        if (context.getJarRepository() == null) {
            context.setJarRepository(new ContextJarRepository());
        }

    }

    
    /**
     * Process a context.xml.
     */
    protected void processContextConfig(File baseDir, String resourceName) {
        
        if (log.isDebugEnabled())
            log.debug("Processing context [" + context.getName() 
                    + "] configuration file " + baseDir + " " + resourceName);

        InputSource source = null;
        InputStream stream = null;

        File file = baseDir;
        if (resourceName != null) {
            file = new File(baseDir, resourceName);
        }
        
        try {
            if ( !file.exists() ) {
                if (resourceName != null) {
                    // Use getResource and getResourceAsStream
                    stream = getClass().getClassLoader()
                        .getResourceAsStream(resourceName);
                    if( stream != null ) {
                        source = new InputSource
                            (getClass().getClassLoader()
                            .getResource(resourceName).toString());
                    }
                }
            } else {
                source =
                    new InputSource("file://" + file.getAbsolutePath());
                stream = new FileInputStream(file);
                // Add as watched resource so that cascade reload occurs if a default
                // config file is modified/added/removed
                context.addWatchedResource(file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error(sm.getString("contextConfig.contextMissing",  
                      resourceName + " " + file) , e);
        }
        
        if (source == null)
            return;
        synchronized (contextDigester) {
            try {
                source.setByteStream(stream);
                contextDigester.setClassLoader(this.getClass().getClassLoader());
                contextDigester.setUseContextClassLoader(false);
                contextDigester.push(context.getParent());
                contextDigester.push(context);
                contextDigester.setErrorHandler(new ContextErrorHandler());
                contextDigester.parse(source);
                if (parseException != null) {
                    ok = false;
                }
                if (log.isDebugEnabled())
                    log.debug("Successfully processed context [" + context.getName() 
                            + "] configuration file " + baseDir + " " + resourceName);
            } catch (SAXParseException e) {
                log.error(sm.getString("contextConfig.contextParse",
                        context.getName()), e);
                log.error(sm.getString("contextConfig.defaultPosition",
                                 "" + e.getLineNumber(),
                                 "" + e.getColumnNumber()));
                ok = false;
            } catch (Exception e) {
                log.error(sm.getString("contextConfig.contextParse",
                        context.getName()), e);
                ok = false;
            } finally {
                contextDigester.reset();
                parseException = null;
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    log.error(sm.getString("contextConfig.contextClose"), e);
                }
            }
        }
    }

    
    /**
     * Adjust docBase.
     */
    protected void fixDocBase()
        throws IOException {
        
        Host host = (Host) context.getParent();
        String appBase = host.getAppBase();

        boolean unpackWARs = true;
        if (host instanceof StandardHost) {
            unpackWARs = ((StandardHost) host).isUnpackWARs() 
                && ((StandardContext) context).getUnpackWAR();
        }

        File canonicalAppBase = new File(appBase);
        if (canonicalAppBase.isAbsolute()) {
            canonicalAppBase = canonicalAppBase.getCanonicalFile();
        } else {
            canonicalAppBase = 
                new File(System.getProperty("catalina.base"), appBase)
                .getCanonicalFile();
        }

        String docBase = context.getDocBase();
        if (docBase == null) {
            // Trying to guess the docBase according to the path
            String path = context.getPath();
            if (path == null) {
                return;
            }
            if (path.equals("")) {
                docBase = "ROOT";
            } else {
                if (path.startsWith("/")) {
                    docBase = path.substring(1).replace('/', '#');
                } else {
                    docBase = path.replace('/', '#');
                }
            }
        }

        File file = new File(docBase);
        if (!file.isAbsolute()) {
            docBase = (new File(canonicalAppBase, docBase)).getPath();
        } else {
            docBase = file.getCanonicalPath();
        }
        file = new File(docBase);
        String origDocBase = docBase;
        
        String contextPath = context.getPath();
        if (contextPath.equals("")) {
            contextPath = "ROOT";
        } else {
            if (contextPath.lastIndexOf('/') > 0) {
                contextPath = "/" + contextPath.substring(1).replace('/','#');
            }
        }
        if (docBase.toLowerCase().endsWith(".war") && !file.isDirectory() && unpackWARs) {
            URL war = new URL("jar:" + (new File(docBase)).toURI().toURL() + "!/");
            docBase = ExpandWar.expand(host, war, contextPath);
            file = new File(docBase);
            docBase = file.getCanonicalPath();
            if (context instanceof StandardContext) {
                ((StandardContext) context).setOriginalDocBase(origDocBase);
            }
        } else {
            File docDir = new File(docBase);
            if (!docDir.exists()) {
                File warFile = new File(docBase + ".war");
                if (warFile.exists()) {
                    if (unpackWARs) {
                        URL war = new URL("jar:" + warFile.toURI().toURL() + "!/");
                        docBase = ExpandWar.expand(host, war, contextPath);
                        file = new File(docBase);
                        docBase = file.getCanonicalPath();
                    } else {
                        docBase = warFile.getCanonicalPath();
                    }
                }
                if (context instanceof StandardContext) {
                    ((StandardContext) context).setOriginalDocBase(origDocBase);
                }
            }
        }

        if (docBase.startsWith(canonicalAppBase.getPath() + File.separatorChar)) {
            docBase = docBase.substring(canonicalAppBase.getPath().length());
            docBase = docBase.replace(File.separatorChar, '/');
            if (docBase.startsWith("/")) {
                docBase = docBase.substring(1);
            }
        } else {
            docBase = docBase.replace(File.separatorChar, '/');
        }

        context.setDocBase(docBase);

    }
    
    
    protected void antiLocking() {

        if ((context instanceof StandardContext) 
            && ((StandardContext) context).getAntiResourceLocking()) {
            
            Host host = (Host) context.getParent();
            String appBase = host.getAppBase();
            String docBase = context.getDocBase();
            if (docBase == null)
                return;
            if (originalDocBase == null) {
                originalDocBase = docBase;
            } else {
                docBase = originalDocBase;
            }
            File docBaseFile = new File(docBase);
            if (!docBaseFile.isAbsolute()) {
                File file = new File(appBase);
                if (!file.isAbsolute()) {
                    file = new File(System.getProperty("catalina.base"), appBase);
                }
                docBaseFile = new File(file, docBase);
            }
            
            String path = context.getPath();
            if (path == null) {
                return;
            }
            if (path.equals("")) {
                docBase = "ROOT";
            } else {
                if (path.startsWith("/")) {
                    docBase = path.substring(1);
                } else {
                    docBase = path;
                }
            }

            File file = null;
            if (docBase.toLowerCase().endsWith(".war")) {
                file = new File(System.getProperty("java.io.tmpdir"),
                        deploymentCount++ + "-" + docBase + ".war");
            } else {
                file = new File(System.getProperty("java.io.tmpdir"), 
                        deploymentCount++ + "-" + docBase);
            }
            
            if (log.isDebugEnabled())
                log.debug("Anti locking context[" + context.getPath() 
                        + "] setting docBase to " + file);
            
            // Cleanup just in case an old deployment is lying around
            ExpandWar.delete(file);
            if (ExpandWar.copy(docBaseFile, file)) {
                context.setDocBase(file.getAbsolutePath());
            }
            
        }
        
    }
    

    /**
     * Process a "init" event for this Context.
     */
    protected void init() {
        // Called from StandardContext.init()

        if (webDigester == null){
            webDigester = createWebDigester();
            webDigester.getParser();
        }

        if (webFragmentDigester == null){
            webFragmentDigester = createWebFragmentDigester();
            webFragmentDigester.getParser();
        }

        if (tldDigester == null){
            tldDigester = createTldDigester();
            tldDigester.getParser();
        }

        if (fragmentOrderingDigester == null){
            fragmentOrderingDigester = createFragmentOrderingDigester();
            fragmentOrderingDigester.getParser();
        }

        if (contextDigester == null){
            contextDigester = createContextDigester();
            contextDigester.getParser();
        }

        if (log.isDebugEnabled())
            log.debug(sm.getString("contextConfig.init"));
        context.setConfigured(false);
        ok = true;
        
        contextConfig();
        
        try {
            fixDocBase();
        } catch (IOException e) {
            log.error(sm.getString("contextConfig.fixDocBase"), e);
        }
        
    }
    
    
    /**
     * Process a "before start" event for this Context.
     */
    protected void beforeStart() {
        antiLocking();
    }
    
    
    /**
     * Process a "start" event for this Context.
     */
    protected void start() {
        // Called from StandardContext.start()

        if (log.isDebugEnabled())
            log.debug(sm.getString("contextConfig.start"));

        // Process the default and application web.xml files
        if (ok) {
            defaultWebConfig();
        }
        // Scan the main descriptors
        if (ok) {
            applicationWebConfig();
        }
        // Parse any Servlet context initializer defined in a Jar
        if (ok) {
            applicationServletContainerInitializerConfig();
        }
        // Parse fragment order
        if (ok && !context.getIgnoreAnnotations()) {
            createFragmentsOrder();
        }
        // Scan fragments, TLDs and annotations
        if (ok) {
            applicationExtraDescriptorsConfig();
        }
        // Parse any TLDs found for listeners
        if (ok) {
            applicationTldConfig();
        }

        // Dump the contents of this pipeline if requested
        if ((log.isDebugEnabled()) && (context instanceof ContainerBase)) {
            log.debug("Pipeline Configuration:");
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            Valve valves[] = null;
            if (pipeline != null)
                valves = pipeline.getValves();
            if (valves != null) {
                for (int i = 0; i < valves.length; i++) {
                    log.debug("  " + valves[i].getInfo());
                }
            }
            log.debug("======================");
        }

        // Make our application available if no problems were encountered
        if (ok) {
            context.setConfigured(true);
        } else {
            log.error(sm.getString("contextConfig.unavailable"));
            context.setConfigured(false);
        }

    }

    /**
     * Process a "start" event for this Context.
     */
    protected void completeConfig() {
        // Called from StandardContext.start()

        // Scan all Servlet API related annotations
        if (ok && !context.getIgnoreAnnotations()) {
            WebAnnotationSet.loadApplicationAnnotations(context);
        }
        // Resolve security
        if (ok) {
            resolveServletSecurity();
        }
        if (ok) {
            validateSecurityRoles();
        }

        // Configure an authenticator if we need one
        if (ok) {
            authenticatorConfig();
        }

        // Find and configure overlays
        if (ok) {
            JarRepository jarRepository = context.getJarRepository();
            JarFile[] jars = jarRepository.findJars();
            for (int i = 0; i < jars.length; i++) {
                if (jars[i].getEntry(Globals.OVERLAY_PATH) != null) {
                    if (context.getResources() instanceof ProxyDirContext) {
                        ProxyDirContext resources = (ProxyDirContext) context.getResources();
                        JARDirContext overlay = new JARDirContext();
                        overlay.setJarFile(jars[i], Globals.OVERLAY_PATH);
                        resources.addOverlay(overlay);
                    } else {
                        // Error, overlays need a ProxyDirContext to compose results
                        log.error(sm.getString("contextConfig.noOverlay", jars[i].getName()));
                        ok = false;
                    }
                    overlays.add(jars[i].getName());
                }
            }
        }

        // Make our application unavailable if problems were encountered
        if (!ok) {
            log.error(sm.getString("contextConfig.unavailable"));
            context.setConfigured(false);
        }

    }

    /**
     * Process a "stop" event for this Context.
     */
    protected void stop() {

        if (log.isDebugEnabled())
            log.debug(sm.getString("contextConfig.stop"));

        int i;

        // Removing children
        Container[] children = context.findChildren();
        for (i = 0; i < children.length; i++) {
            context.removeChild(children[i]);
        }

        // Removing application parameters
        /*
        ApplicationParameter[] applicationParameters =
            context.findApplicationParameters();
        for (i = 0; i < applicationParameters.length; i++) {
            context.removeApplicationParameter
                (applicationParameters[i].getName());
        }
        */

        // Removing security constraints
        SecurityConstraint[] securityConstraints = context.findConstraints();
        for (i = 0; i < securityConstraints.length; i++) {
            context.removeConstraint(securityConstraints[i]);
        }

        // Removing Ejbs
        /*
        ContextEjb[] contextEjbs = context.findEjbs();
        for (i = 0; i < contextEjbs.length; i++) {
            context.removeEjb(contextEjbs[i].getName());
        }
        */

        // Removing environments
        /*
        ContextEnvironment[] contextEnvironments = context.findEnvironments();
        for (i = 0; i < contextEnvironments.length; i++) {
            context.removeEnvironment(contextEnvironments[i].getName());
        }
        */

        // Removing errors pages
        ErrorPage[] errorPages = context.findErrorPages();
        for (i = 0; i < errorPages.length; i++) {
            context.removeErrorPage(errorPages[i]);
        }

        // Removing filter defs
        FilterDef[] filterDefs = context.findFilterDefs();
        for (i = 0; i < filterDefs.length; i++) {
            context.removeFilterDef(filterDefs[i]);
        }

        // Removing filter maps
        FilterMap[] filterMaps = context.findFilterMaps();
        for (i = 0; i < filterMaps.length; i++) {
            context.removeFilterMap(filterMaps[i]);
        }

        // Removing local ejbs
        /*
        ContextLocalEjb[] contextLocalEjbs = context.findLocalEjbs();
        for (i = 0; i < contextLocalEjbs.length; i++) {
            context.removeLocalEjb(contextLocalEjbs[i].getName());
        }
        */

        // Removing Mime mappings
        String[] mimeMappings = context.findMimeMappings();
        for (i = 0; i < mimeMappings.length; i++) {
            context.removeMimeMapping(mimeMappings[i]);
        }

        // Removing parameters
        String[] parameters = context.findParameters();
        for (i = 0; i < parameters.length; i++) {
            context.removeParameter(parameters[i]);
        }

        // Removing resource env refs
        /*
        String[] resourceEnvRefs = context.findResourceEnvRefs();
        for (i = 0; i < resourceEnvRefs.length; i++) {
            context.removeResourceEnvRef(resourceEnvRefs[i]);
        }
        */

        // Removing resource links
        /*
        ContextResourceLink[] contextResourceLinks =
            context.findResourceLinks();
        for (i = 0; i < contextResourceLinks.length; i++) {
            context.removeResourceLink(contextResourceLinks[i].getName());
        }
        */

        // Removing resources
        /*
        ContextResource[] contextResources = context.findResources();
        for (i = 0; i < contextResources.length; i++) {
            context.removeResource(contextResources[i].getName());
        }
        */

        // Removing sercurity role
        String[] securityRoles = context.findSecurityRoles();
        for (i = 0; i < securityRoles.length; i++) {
            context.removeSecurityRole(securityRoles[i]);
        }

        // Removing servlet mappings
        String[] servletMappings = context.findServletMappings();
        for (i = 0; i < servletMappings.length; i++) {
            context.removeServletMapping(servletMappings[i]);
        }

        // FIXME : Removing status pages

        // Removing taglibs
        String[] taglibs = context.findTaglibs();
        for (i = 0; i < taglibs.length; i++) {
            context.removeTaglib(taglibs[i]);
        }

        // FIXME: remove JSP property groups
        
        // FIXME: remove JSP tag libraries
        
        // Removing welcome files
        String[] welcomeFiles = context.findWelcomeFiles();
        for (i = 0; i < welcomeFiles.length; i++) {
            context.removeWelcomeFile(welcomeFiles[i]);
        }

        // Removing wrapper lifecycles
        String[] wrapperLifecycles = context.findWrapperLifecycles();
        for (i = 0; i < wrapperLifecycles.length; i++) {
            context.removeWrapperLifecycle(wrapperLifecycles[i]);
        }

        // Removing wrapper listeners
        String[] wrapperListeners = context.findWrapperListeners();
        for (i = 0; i < wrapperListeners.length; i++) {
            context.removeWrapperListener(wrapperListeners[i]);
        }

        // Remove (partially) folders and files created by antiLocking
        Host host = (Host) context.getParent();
        String appBase = host.getAppBase();
        String docBase = context.getDocBase();
        if ((docBase != null) && (originalDocBase != null)) {
            File docBaseFile = new File(docBase);
            if (!docBaseFile.isAbsolute()) {
                docBaseFile = new File(appBase, docBase);
            }
            ExpandWar.delete(docBaseFile);
        }
        
        overlays.clear();
        webFragments.clear();
        TLDs.clear();
        servletContainerInitializerInfos.clear();
        order.clear();
        handlesTypesArray = null;
        handlesTypes.clear();
        
        ok = true;

    }
    
    
    /**
     * Process a "destroy" event for this Context.
     */
    protected void destroy() {
        // Called from StandardContext.destroy()
        if (log.isDebugEnabled())
            log.debug(sm.getString("contextConfig.destroy"));

        // Changed to getWorkPath per Bugzilla 35819.
        String workDir = ((StandardContext) context).getWorkPath();
        if (workDir != null)
            ExpandWar.delete(new File(workDir));
    }
    
    
    /**
     * Translate servlet security associated with Servlets to security constraints.
     */
    protected void resolveServletSecurity() {
        Container wrappers[] = context.findChildren();
        for (int i = 0; i < wrappers.length; i++) {
            Wrapper wrapper = (Wrapper) wrappers[i];
            ServletSecurityElement servletSecurity = wrapper.getServletSecurity();
            if (servletSecurity != null) {
                
                ArrayList<String> methodOmissions = new ArrayList<String>();
                boolean classPA = servletSecurity.getEmptyRoleSemantic().equals(EmptyRoleSemantic.PERMIT);
                boolean classDA = servletSecurity.getEmptyRoleSemantic().equals(EmptyRoleSemantic.DENY);
                boolean classTP = servletSecurity.getTransportGuarantee().equals(TransportGuarantee.CONFIDENTIAL);
                String[] classRA = servletSecurity.getRolesAllowed();
                Collection<HttpMethodConstraintElement> httpMethodConstraints = 
                    servletSecurity.getHttpMethodConstraints();

                // Process method constraints
                if (httpMethodConstraints != null && httpMethodConstraints.size() > 0)
                {
                   for (HttpMethodConstraintElement httpMethodConstraint : httpMethodConstraints)
                   {
                      boolean methodPA = httpMethodConstraint.getEmptyRoleSemantic().equals(EmptyRoleSemantic.PERMIT);
                      boolean methodDA = httpMethodConstraint.getEmptyRoleSemantic().equals(EmptyRoleSemantic.DENY);
                      boolean methodTP = httpMethodConstraint.getTransportGuarantee().equals(TransportGuarantee.CONFIDENTIAL);
                      String[] methodRA = httpMethodConstraint.getRolesAllowed();
                      if (methodPA || methodDA || methodTP || methodRA != null)
                      {
                         methodOmissions.add(httpMethodConstraint.getMethodName());
                         // Define a constraint specific for the method
                         SecurityConstraint constraint = new SecurityConstraint();
                         if (methodDA) {
                             constraint.setAuthConstraint(true);
                         }
                         if (methodPA) {
                             constraint.addAuthRole("*");
                         }
                         if (methodRA != null) {
                             for (String role : methodRA) {
                                 constraint.addAuthRole(role);
                             }
                         }
                         if (methodTP) {
                             constraint.setUserConstraint(org.apache.catalina.realm.Constants.CONFIDENTIAL_TRANSPORT);
                         }
                         SecurityCollection collection = new SecurityCollection();
                         collection.addMethod(httpMethodConstraint.getMethodName());
                         // Determine pattern set
                         String[] urlPatterns = wrapper.findMappings();
                         Set<String> servletSecurityPatterns = new HashSet<String>();
                         for (String urlPattern : urlPatterns) {
                             servletSecurityPatterns.add(urlPattern);
                         }
                         SecurityConstraint[] constraints = context.findConstraints();
                         for (SecurityConstraint constraint2 : constraints) {
                             for (SecurityCollection collection2 : constraint2.findCollections()) {
                                 for (String urlPattern : collection2.findPatterns()) {
                                     if (servletSecurityPatterns.contains(urlPattern)) {
                                         servletSecurityPatterns.remove(urlPattern);
                                     }
                                 }
                             }
                         }
                         for (String urlPattern : servletSecurityPatterns) {
                             collection.addPattern(urlPattern);
                         }
                         constraint.addCollection(collection);
                         context.addConstraint(constraint);
                      }

                   }

                }

                if (classPA || classDA || classTP || classRA != null)
                {
                    // Define a constraint for the class
                    SecurityConstraint constraint = new SecurityConstraint();
                    if (classPA) {
                        constraint.addAuthRole("*");
                    }
                    if (classDA) {
                        constraint.setAuthConstraint(true);
                    }
                    if (classRA != null) {
                        for (String role : classRA) {
                            constraint.addAuthRole(role);
                        }
                    }
                    if (classTP) {
                        constraint.setUserConstraint(org.apache.catalina.realm.Constants.CONFIDENTIAL_TRANSPORT);
                    }
                    SecurityCollection collection = new SecurityCollection();
                    // Determine pattern set
                    String[] urlPatterns = wrapper.findMappings();
                    Set<String> servletSecurityPatterns = new HashSet<String>();
                    for (String urlPattern : urlPatterns) {
                        servletSecurityPatterns.add(urlPattern);
                    }
                    SecurityConstraint[] constraints = context.findConstraints();
                    for (SecurityConstraint constraint2 : constraints) {
                        for (SecurityCollection collection2 : constraint2.findCollections()) {
                            for (String urlPattern : collection2.findPatterns()) {
                                if (servletSecurityPatterns.contains(urlPattern)) {
                                    servletSecurityPatterns.remove(urlPattern);
                                }
                            }
                        }
                    }
                    for (String urlPattern : servletSecurityPatterns) {
                        collection.addPattern(urlPattern);
                    }
                    for (String methodOmission : methodOmissions) {
                        collection.addMethodOmission(methodOmission);
                    }
                    constraint.addCollection(collection);
                    context.addConstraint(constraint);
                }
                
            }
        }
    }
    
    
    /**
     * Validate the usage of security role names in the web application
     * deployment descriptor.  If any problems are found, issue warning
     * messages (for backwards compatibility) and add the missing roles.
     * (To make these problems fatal instead, simply set the <code>ok</code>
     * instance variable to <code>false</code> as well).
     */
    protected void validateSecurityRoles() {

        // Check role names used in <security-constraint> elements
        SecurityConstraint constraints[] = context.findConstraints();
        for (int i = 0; i < constraints.length; i++) {
            String roles[] = constraints[i].findAuthRoles();
            for (int j = 0; j < roles.length; j++) {
                if (!"*".equals(roles[j]) &&
                    !context.findSecurityRole(roles[j])) {
                    log.info(sm.getString("contextConfig.role.auth", roles[j]));
                    context.addSecurityRole(roles[j]);
                }
            }
        }

        // Check role names used in <servlet> elements
        Container wrappers[] = context.findChildren();
        for (int i = 0; i < wrappers.length; i++) {
            Wrapper wrapper = (Wrapper) wrappers[i];
            String runAs = wrapper.getRunAs();
            if ((runAs != null) && !context.findSecurityRole(runAs)) {
                log.info(sm.getString("contextConfig.role.runas", runAs));
                context.addSecurityRole(runAs);
            }
            String names[] = wrapper.findSecurityReferences();
            for (int j = 0; j < names.length; j++) {
                String link = wrapper.findSecurityReference(names[j]);
                if ((link != null) && !context.findSecurityRole(link)) {
                    log.info(sm.getString("contextConfig.role.link", link));
                    context.addSecurityRole(link);
                }
            }
        }

    }


    /**
     * Get config base.
     */
    protected File getConfigBase() {
        File configBase = 
            new File(System.getProperty("catalina.base"), "conf");
        if (!configBase.exists()) {
            return null;
        } else {
            return configBase;
        }
    }  

    
    protected String getHostConfigPath(String resourceName) {
        StringBuilder result = new StringBuilder();
        Container container = context;
        Container host = null;
        Container engine = null;
        while (container != null) {
            if (container instanceof Host)
                host = container;
            if (container instanceof Engine)
                engine = container;
            container = container.getParent();
        }
        if (engine != null) {
            result.append(engine.getName()).append('/');
        }
        if (host != null) {
            result.append(host.getName()).append('/');
        }
        result.append(resourceName);
        return result.toString();
    }


    protected class ContextErrorHandler
        implements ErrorHandler {

        public void error(SAXParseException exception) {
            parseException = exception;
        }

        public void fatalError(SAXParseException exception) {
            parseException = exception;
        }

        public void warning(SAXParseException exception) {
            parseException = exception;
        }

    }


    protected class ServletContainerInitializerInfo {
        protected Class<?> servletContainerInitializer = null;
        protected Class<?>[] interestClasses = null;
        protected HashSet<Class<?>> startupNotifySet = new HashSet<Class<?>>();
        protected ServletContainerInitializerInfo(Class<?> servletContainerInitializer, Class<?>[] interestClasses) {
            this.servletContainerInitializer = servletContainerInitializer;
            this.interestClasses = interestClasses;
        }
        public Class<?> getServletContainerInitializer() {
            return servletContainerInitializer;
        }
        public Class<?>[] getInterestClasses() {
            return interestClasses;
        }
        protected void addStartupNotifyClass(Class<?> clazz) {
            startupNotifySet.add(clazz);
        }
        public Set<Class<?>> getStartupNotifySet() {
            return startupNotifySet;
        }
    }
    
    
}
