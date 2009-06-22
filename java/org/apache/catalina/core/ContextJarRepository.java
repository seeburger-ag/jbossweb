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
 * Context specific implementation for a JAR repository, which will manage
 * JARs from /WEB-INF/lib in an efficient way.
 *
 * @author Remy Maucherat
 */
public class ContextJarRepository
    implements JarRepository, Contained, Lifecycle {

    private static Logger log = Logger.getLogger(ContextJarRepository.class);

    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new ContextJarRepository instance with no associated Container.
     */
    public ContextJarRepository() {

        this(null);

    }


    /**
     * Construct a new ContextJarRepository instance that is associated with the
     * specified Container.
     *
     * @param container The container we should be associated with
     */
    public ContextJarRepository(Container container) {

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
    protected String info = "org.apache.catalina.core.ContextJarRepository/1.0";


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

    
    /**
     * Classes path, which is essentially an exploded Jar.
     */
    protected String classesPath = "/WEB-INF/classes";
    
    
    /**
     * Library path.
     */
    protected String libPath = "/WEB-INF/lib";
    
    
    /**
     * Map for the JarFile instances.
     */
    protected Map<String, JarFile> jarFiles = new HashMap<String, JarFile>();
    
    
    /**
     * Array of the JarFiles, as convenience.
     */
    protected JarFile[] jarFilesArray = new JarFile[0];
    

    /**
     * Map for the File instances.
     */
    protected Map<String, File> explodedJars = new HashMap<String, File>();
    
    
    /**
     * Array of the exploded Jars, as convenience.
     */
    protected File[] explodedJarsArray = new File[0];
    
    
    /**
     * Delete temp Jars.
     */
    protected boolean tempJars = false;
    

    /**
     * Delete temp exploded Jars.
     */
    protected boolean tempExplodedJars = false;
    

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

        if (!(container instanceof Context))
            throw new IllegalStateException("");
        DirContext resources = container.getResources();
        ServletContext servletContext = ((Context) container).getServletContext();
        if (servletContext == null)
            return;
        
        // Access work directory
        File workDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
        if (workDir == null) {
            // FIXME: this is actually an error
            log.info("No work dir for " + servletContext);
        }

        // Looking up directory /WEB-INF/lib in the context
        NamingEnumeration<Binding> libPathListing = null;
        try {
            libPathListing = resources.listBindings(libPath);
        } catch (NamingException e) {
            // Silent catch: it's valid that no /WEB-INF/lib collection exists
        }
        if (libPathListing != null) {
            String absoluteLibPath = servletContext.getRealPath(libPath);
            File destDir = null;
            if (absoluteLibPath != null) {
                destDir = new File(absoluteLibPath);
            } else {
                tempJars = true;
                destDir = new File(workDir, libPath);
                destDir.mkdirs();
            }
            while (libPathListing.hasMoreElements()) {
                Binding binding = libPathListing.nextElement();
                String logicalName = libPath + "/" + binding.getName();
                if (!logicalName.endsWith(".jar"))
                    continue;
                // Copy JAR in the work directory, always (the JAR file
                // would get locked otherwise, which would make it
                // impossible to update it or remove it at runtime)
                File destFile = new File(destDir, binding.getName());
                Object obj = binding.getObject();
                if (!(obj instanceof Resource))
                    continue;
                Resource jarResource = (Resource) obj;
                try {
                    if (tempJars) {
                        InputStream is = null;
                        OutputStream os = null;
                        try {
                            is = jarResource.streamContent();
                            os = new FileOutputStream(destFile);
                            IOTools.flow(is, os);
                            // Don't catch IOE - let the outer try/catch handle it
                        } finally {
                            try {
                                if (is != null) is.close();
                            } catch (IOException e){
                                // Ignore
                            }
                            try {
                                if (os != null) os.close();
                            } catch (IOException e){
                                // Ignore
                            }
                        }
                    }
                    JarFile jarFile = new JarFile(destFile);
                    jarFiles.put(logicalName, jarFile);
                } catch (IOException ex) {
                    // Catch the exception if there is an empty jar file,
                    // or if the JAR cannot be copied to temp
                    // FIXME: throw an error, as the webapp will not run
                }
            }

        }
        jarFilesArray = jarFiles.values().toArray(jarFilesArray);

        // Setting up the class repository (/WEB-INF/classes), if it exists
        DirContext classes = null;
        try {
            Object object = resources.lookup(classesPath);
            if (object instanceof DirContext) {
                classes = (DirContext) object;
            }
        } catch(NamingException e) {
            // Silent catch: it's valid that no /WEB-INF/classes collection
            // exists
        }
        if (classes != null) {
            File classRepository = null;
            String absoluteClassesPath = servletContext.getRealPath(classesPath);
            if (absoluteClassesPath != null) {
                classRepository = new File(absoluteClassesPath);
            } else {
                classRepository = new File(workDir, classesPath);
                classRepository.mkdirs();
                tempExplodedJars = true;
                try {
                    copyDirContext(classes, classRepository);
                } catch (NamingException ex) {
                    // Catch the exception if there is an empty jar file,
                    // or if the JAR cannot be copied to temp
                    // FIXME: throw an error, as the webapp will not run
                } catch (IOException ex) {
                    // Catch the exception if there is an empty jar file,
                    // or if the JAR cannot be copied to temp
                    // FIXME: throw an error, as the webapp will not run
                }
            }
            // Adding the repository to the class loader
            explodedJarsArray = new File[] { classRepository };
            explodedJars.put(classesPath + "/", classRepository);
        }

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

        // Close JARs and delete temporary copies if needed
        try {
            for (int i = 0; i < jarFilesArray.length; i++) {
                jarFilesArray[i].close();
                if (tempJars) {
                    (new File(jarFilesArray[i].getName())).delete();
                }
            }
        } catch (IOException ex) {
            // Catch the exception if there is an empty jar file,
            // or if the JAR cannot be copied to temp
            // FIXME: throw an error, as webapp will not run
        }
        jarFiles.clear();
        jarFilesArray = new JarFile[0];
        tempJars = false;
        explodedJars.clear();
        explodedJarsArray = new File[0];
        // FIXME: delete exploded copy
        tempExplodedJars = false;

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
        return jarFiles;
    }


    /**
     * Find the JarFile corresponding to the path.
     * 
     * @return the JarFile, or null if not found
     */
    public JarFile findJar(String path) {
        for (int i = 0; i < jarFilesArray.length; i++) {
            if (jarFilesArray[i].getName().equals(path)) {
                return jarFilesArray[i];
            }
        }
        return null;
    }


    /**
     * Find all JarFile managed by the JARRepository.
     * 
     * @return All JarFile
     */
    public JarFile[] findJars() {
        return jarFilesArray;
    }

    
    /**
     * Get the exploded Jar map.
     * 
     * @return the Jar map, associating logical name with File
     */
    public Map<String, File> getExplodedJars() {
        return explodedJars;
    }

    
    /**
     * Find all exploded Jars managed by the JARRepository.
     * 
     * @return All exploded File
     */
    public File[] findExplodedJars() {
        return explodedJarsArray;
    }


    /**
     * Copy directory.
     */
    protected void copyDirContext(DirContext srcDir, File destDir)
        throws IOException, NamingException {
        InputStream is = null;
        OutputStream os = null;
        NamingEnumeration<NameClassPair> enumeration = srcDir.list("");
        while (enumeration.hasMoreElements()) {
            NameClassPair ncPair = enumeration.nextElement();
            String name = ncPair.getName();
            Object object = srcDir.lookup(name);
            File currentFile = new File(destDir, name);
            if (object instanceof Resource) {
                try {
                    is = ((Resource) object).streamContent();
                    os = new FileOutputStream(currentFile);
                    IOTools.flow(is, os);
                    // Don't catch IOE - let the outer try/catch handle it
                } finally {
                    try {
                        if (is != null) is.close();
                    } catch (IOException e){
                        // Ignore
                    }
                    try {
                        if (os != null) os.close();
                    } catch (IOException e){
                        // Ignore
                    }
                }
            } else if (object instanceof InputStream) {
                try {
                    is = (InputStream) object;
                    os = new FileOutputStream(currentFile);
                    IOTools.flow(is, os);
                    // Don't catch IOE - let the outer try/catch handle it
                } finally {
                    try {
                        if (is != null) is.close();
                    } catch (IOException e){
                        // Ignore
                    }
                    try {
                        if (os != null) os.close();
                    } catch (IOException e){
                        // Ignore
                    }
                }
            } else if (object instanceof DirContext) {
                currentFile.mkdir();
                copyDirContext((DirContext) object, currentFile);
            }
        }
    }


}
