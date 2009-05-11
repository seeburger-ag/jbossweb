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


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.jar.JarFile;

import javax.naming.Binding;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.ServletContext;

import org.apache.catalina.Contained;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.JarRepository;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.util.IOTools;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;
import org.apache.naming.resources.Resource;
import org.jboss.logging.Logger;


/**
 * Server specific implementation for a JAR repository, which will manage
 * shared JARs which may contain descriptors.
 *
 * FIXME: Stub impl
 * @author Remy Maucherat
 */
public class ServerJarRepository
    implements JarRepository, Contained, Lifecycle {

    private static Logger log = Logger.getLogger(ServerJarRepository.class);

    /**
     * Names of JARs that are known not to contain any descriptors or annotations.
     */
    protected static HashSet<String> skipJars;

    /**
     * Initializes the set of JARs that are known not to contain any descriptors.
     */
    static {
        skipJars = new HashSet<String>();
        // Bootstrap JARs
        skipJars.add("bootstrap.jar");
        skipJars.add("commons-daemon.jar");
        skipJars.add("tomcat-juli.jar");
        // Main JARs
        skipJars.add("annotations-api.jar");
        skipJars.add("catalina.jar");
        skipJars.add("catalina-ant.jar");
        skipJars.add("el-api.jar");
        skipJars.add("jasper.jar");
        skipJars.add("jasper-el.jar");
        skipJars.add("jasper-jdt.jar");
        skipJars.add("jsp-api.jar");
        skipJars.add("servlet-api.jar");
        skipJars.add("tomcat-coyote.jar");
        skipJars.add("tomcat-dbcp.jar");
        // i18n JARs
        skipJars.add("tomcat-i18n-en.jar");
        skipJars.add("tomcat-i18n-es.jar");
        skipJars.add("tomcat-i18n-fr.jar");
        skipJars.add("tomcat-i18n-ja.jar");
        // Misc JARs not included with Tomcat
        skipJars.add("ant.jar");
        skipJars.add("commons-dbcp.jar");
        skipJars.add("commons-beanutils.jar");
        skipJars.add("commons-fileupload-1.0.jar");
        skipJars.add("commons-pool.jar");
        skipJars.add("commons-digester.jar");
        skipJars.add("commons-logging.jar");
        skipJars.add("commons-collections.jar");
        skipJars.add("jmx.jar");
        skipJars.add("jmx-tools.jar");
        skipJars.add("xercesImpl.jar");
        skipJars.add("xmlParserAPIs.jar");
        skipJars.add("xml-apis.jar");
        // JARs from J2SE runtime
        skipJars.add("sunjce_provider.jar");
        skipJars.add("ldapsec.jar");
        skipJars.add("localedata.jar");
        skipJars.add("dnsns.jar");
        skipJars.add("tools.jar");
        skipJars.add("sunpkcs11.jar");
    }

    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new ContextJarRepository instance with no associated Container.
     */
    public ServerJarRepository() {

        this(null);

    }


    /**
     * Construct a new ContextJarRepository instance that is associated with the
     * specified Container.
     *
     * @param container The container we should be associated with
     */
    public ServerJarRepository(Container container) {

        super();
        setContainer(container);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The Container with which this Pipeline is associated.
     */
    protected Container container = null;


    /**
     * Descriptive information about this implementation.
     */
    protected String info = "org.apache.catalina.core.ServerJarRepository/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * Has this component been started yet?
     */
    protected boolean started = false;

    
    // --------------------------------------------------------- Public Methods


    /**
     * Return descriptive information about this implementation class.
     */
    public String getInfo() {

        return (this.info);

    }


    // ------------------------------------------------------ Contained Methods


    /**
     * Return the Container with which this Pipeline is associated.
     */
    public Container getContainer() {

        return (this.container);

    }


    /**
     * Set the Container with which this Pipeline is associated.
     *
     * @param container The new associated container
     */
    public void setContainer(Container container) {

        this.container = container;

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this 
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }

    /**
     * Prepare for active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public synchronized void start() throws LifecycleException {

        // Validate and update our current component state
        if (started) {
            if(log.isDebugEnabled())
                log.debug(sm.getString("contextJarRepository.alreadyStarted"));
            return;
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);

        started = true;

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(START_EVENT, null);

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);

    }


    /**
     * Gracefully shut down active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public synchronized void stop() throws LifecycleException {

        // Validate and update our current component state
        if (!started) {
            if(log.isDebugEnabled())
                log.debug(sm.getString("contextJarRepository.notStarted"));
            return;
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, null);

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, null);
    }


    // -------------------------------------------------- JarRepository Methods


    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    public void backgroundProcess() {
        
    }


    /**
     * Get the JarFile map.
     * 
     * @return the JarFile map, associating logical name with JarFile
     */
    public Map<String, JarFile> getJars() {
        return null;
    }


    /**
     * Find the JarFile corresponding to the path.
     * 
     * @return the JarFile, or null if not found
     */
    public JarFile findJar(String path) {
        return null;
    }


    /**
     * Find all JarFile managed by the JARRepository.
     * 
     * @return All JarFile
     */
    public JarFile[] findJars() {
        return null;
    }

    
    /**
     * Get the exploded Jar map.
     * 
     * @return the Jar map, associating logical name with File
     */
    public Map<String, File> getExplodedJars() {
        return null;
    }

    
    /**
     * Find all exploded Jars managed by the JARRepository.
     * 
     * @return All exploded File
     */
    public File[] findExplodedJars() {
        return null;
    }


}
