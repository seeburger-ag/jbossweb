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


package org.apache.catalina.loader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;

import org.apache.catalina.JarRepository;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.util.StringManager;
import org.apache.naming.JndiPermission;
import org.apache.tomcat.util.IntrospectionUtils;

/**
 * Specialized web application class loader.
 * <p>
 * This class loader is a full reimplementation of the 
 * <code>URLClassLoader</code> from the JDK. It is desinged to be fully
 * compatible with a normal <code>URLClassLoader</code>, although its internal
 * behavior may be completely different.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - This class loader faithfully follows 
 * the delegation model recommended in the specification. The system class 
 * loader will be queried first, then the local repositories, and only then 
 * delegation to the parent class loader will occur. This allows the web 
 * application to override any shared class except the classes from J2SE.
 * Special handling is provided from the JAXP XML parser interfaces, the JNDI
 * interfaces, and the classes from the servlet API, which are never loaded 
 * from the webapp repository.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - Due to limitations in Jasper 
 * compilation technology, any repository which contains classes from 
 * the servlet API will be ignored by the class loader.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - The class loader generates source
 * URLs which include the full JAR URL when a class is loaded from a JAR file,
 * which allows setting security permission at the class level, even when a
 * class is contained inside a JAR.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - Local repositories are searched in
 * the order they are specified in the JarRepository.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - No check for sealing violations or
 * security is made unless a security manager is present.
 *
 * @author Remy Maucherat
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */
public class WebappClassLoader
    extends URLClassLoader
    implements Lifecycle
 {

    protected static org.jboss.logging.Logger log=
        org.jboss.logging.Logger.getLogger( WebappClassLoader.class );

    public static final boolean ENABLE_CLEAR_REFERENCES = 
        Boolean.valueOf(System.getProperty("org.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES", "true")).booleanValue();
    
    public static final boolean SYSTEM_CL_DELEGATION = 
        Boolean.valueOf(System.getProperty("org.apache.catalina.loader.WebappClassLoader.SYSTEM_CL_DELEGATION", "true")).booleanValue();
    
    protected class PrivilegedFindResource
        implements PrivilegedAction {

        protected File file;
        protected String path;

        PrivilegedFindResource(File file, String path) {
            this.file = file;
            this.path = path;
        }

        public Object run() {
            return findResourceInternal(file, path);
        }

    }


    // ------------------------------------------------------- Static Variables


    /**
     * The set of trigger classes that will cause a proposed repository not
     * to be added if this class is visible to the class loader that loaded
     * this factory class.  Typically, trigger classes will be listed for
     * components that have been integrated into the JDK for later versions,
     * but where the corresponding JAR files are required to run on
     * earlier versions.
     */
    protected static final String[] triggers = {
        "javax.servlet.Servlet"                     // Servlet API
    };


    /**
     * Set of package names which are not allowed to be loaded from a webapp
     * class loader without delegating first.
     */
    protected static final String[] packageTriggers = {
        "javax.servlet."
    };


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);

    
    /**
     * Use anti JAR locking code, which does URL rerouting when accessing
     * resources.
     */
    boolean antiJARLocking = false; 
    

    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new ClassLoader with no defined repositories and no
     * parent ClassLoader.
     */
    public WebappClassLoader() {

        super(new URL[0]);
        this.parent = getParent();
        system = getSystemClassLoader();
        securityManager = System.getSecurityManager();

        if (securityManager != null) {
            refreshPolicy();
        }

    }


    /**
     * Construct a new ClassLoader with no defined repositories and no
     * parent ClassLoader.
     */
    public WebappClassLoader(ClassLoader parent) {

        super(new URL[0], parent);
                
        this.parent = getParent();
        
        system = getSystemClassLoader();
        securityManager = System.getSecurityManager();

        if (securityManager != null) {
            refreshPolicy();
        }
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Associated Jar repository.
     */
    protected JarRepository repository = null;


    /**
     * The cache of ResourceEntry for classes and resources we have loaded,
     * keyed by resource name.
     */
    protected Map<String, ResourceEntry> resourceEntries = new ConcurrentHashMap<String, ResourceEntry>();


    /**
     * The list of not found resources.
     */
    protected Map<String, Object> notFoundResources = new ConcurrentHashMap<String, Object>();
    protected static final Object VALUE = new Object();


    /**
     * Should this class loader delegate to the parent class loader
     * <strong>before</strong> searching its own repositories (i.e. the
     * usual Java2 delegation model)?  If set to <code>false</code>,
     * this class loader will search its own repositories first, and
     * delegate to the parent only if the class or resource is not
     * found locally.
     */
    protected boolean delegate = false;


    /**
     * Last time a JAR was accessed.
     */
    //protected long lastJarAccessed = 0L;


    /**
     * The list of local repositories, in the order they should be searched
     * for locally loaded classes or resources.
     */
    //protected String[] repositories = new String[0];


     /**
      * Repositories URLs, used to cache the result of getURLs.
      */
     protected URL[] repositoryURLs = null;


    /**
     * Repositories translated as path in the work directory (for Jasper
     * originally), but which is used to generate fake URLs should getURLs be
     * called.
     */
    //protected File[] files = new File[0];


    /**
     * The list of JARs, in the order they should be searched
     * for locally loaded classes or resources.
     */
    //protected JarFile[] jarFiles = new JarFile[0];


    /**
     * The list of JARs, in the order they should be searched
     * for locally loaded classes or resources.
     */
    //protected File[] jarRealFiles = new File[0];


    /**
     * The path which will be monitored for added Jar files.
     */
    //protected String jarPath = null;


    /**
     * The list of JARs, in the order they should be searched
     * for locally loaded classes or resources.
     */
    //protected String[] jarNames = new String[0];


    /**
     * The list of JARs last modified dates, in the order they should be
     * searched for locally loaded classes or resources.
     */
    //protected long[] lastModifiedDates = new long[0];


    /**
     * The list of resources which should be checked when checking for
     * modifications.
     */
    //protected String[] paths = new String[0];


    /**
     * A list of read File and Jndi Permission's required if this loader
     * is for a web application context.
     */
    protected ArrayList permissionList = new ArrayList();


    /**
     * Path where resources loaded from JARs will be extracted.
     */
    protected File loaderDir = null;


    /**
     * The PermissionCollection for each CodeSource for a web
     * application context.
     */
    protected HashMap loaderPC = new HashMap();


    /**
     * Instance of the SecurityManager installed.
     */
    protected SecurityManager securityManager = null;


    /**
     * The parent class loader.
     */
    protected ClassLoader parent = null;


    /**
     * The system class loader.
     */
    protected ClassLoader system = null;


    /**
     * Has this component been started?
     */
    protected boolean started = false;


    /**
     * Has external repositories.
     */
    //protected boolean hasExternalRepositories = false;

    /**
     * need conversion for properties files
     */
    protected boolean needConvert = false;


    /**
     * All permission.
     */
    protected Permission allPermission = new java.security.AllPermission();


    // ------------------------------------------------------------- Properties


    public JarRepository getRepository() {
        return repository;
    }


    public void setRepository(JarRepository repository) {
        this.repository = repository;
    }


    /**
     * Return the "delegate first" flag for this class loader.
     */
    public boolean getDelegate() {

        return (this.delegate);

    }


    /**
     * Set the "delegate first" flag for this class loader.
     *
     * @param delegate The new "delegate first" flag
     */
    public void setDelegate(boolean delegate) {

        this.delegate = delegate;

    }


    /**
     * @return Returns the antiJARLocking.
     */
    public boolean getAntiJARLocking() {
        return antiJARLocking;
    }
    
    
    /**
     * @param antiJARLocking The antiJARLocking to set.
     */
    public void setAntiJARLocking(boolean antiJARLocking) {
        this.antiJARLocking = antiJARLocking;
    }

    
    /**
     * If there is a Java SecurityManager create a read FilePermission
     * or JndiPermission for the file directory path.
     *
     * @param path file directory path
     */
    public void addPermission(String path) {
        if (path == null) {
            return;
        }

        if (securityManager != null) {
            Permission permission = null;
            if( path.startsWith("jndi:") || path.startsWith("jar:jndi:") ) {
                if (!path.endsWith("/")) {
                    path = path + "/";
                }
                permission = new JndiPermission(path + "*");
                addPermission(permission);
            } else {
                if (!path.endsWith(File.separator)) {
                    permission = new FilePermission(path, "read");
                    addPermission(permission);
                    path = path + File.separator;
                }
                permission = new FilePermission(path + "-", "read");
                addPermission(permission);
            }
        }
    }


    /**
     * If there is a Java SecurityManager create a read FilePermission
     * or JndiPermission for URL.
     *
     * @param url URL for a file or directory on local system
     */
    public void addPermission(URL url) {
        if (url != null) {
            addPermission(url.toString());
        }
    }


    /**
     * If there is a Java SecurityManager create a Permission.
     *
     * @param permission The permission
     */
    public void addPermission(Permission permission) {
        if ((securityManager != null) && (permission != null)) {
            permissionList.add(permission);
        }
    }


    /**
     * Change the work directory.
     */
    public void setWorkDir(File workDir) {
        this.loaderDir = new File(workDir, "loader");
    }

    /**
     * Utility method for use in subclasses.
     * Must be called before Lifecycle methods to have any effect.
     */
    protected void setParentClassLoader(ClassLoader pcl) {
        parent = pcl;
    }


    /**
     * Render a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("WebappClassLoader\r\n");
        sb.append("  delegate: ");
        sb.append(delegate);
        sb.append("\r\n");
        if (this.parent != null) {
            sb.append("----------> Parent Classloader:\r\n");
            sb.append(this.parent.toString());
            sb.append("\r\n");
        }
        return (sb.toString());

    }


    // ---------------------------------------------------- ClassLoader Methods


    /**
     * Find the specified class in our local repositories, if possible.  If
     * not found, throw <code>ClassNotFoundException</code>.
     *
     * @param name Name of the class to be loaded
     *
     * @exception ClassNotFoundException if the class was not found
     */
    public Class findClass(String name) throws ClassNotFoundException {

        if (log.isDebugEnabled())
            log.debug("    findClass(" + name + ")");

        // Cannot load anything from local repositories if class loader is stopped
        if (!started) {
            throw new ClassNotFoundException(name);
        }

        // (1) Permission to define this class when using a SecurityManager
        if (securityManager != null) {
            int i = name.lastIndexOf('.');
            if (i >= 0) {
                try {
                    if (log.isTraceEnabled())
                        log.trace("      securityManager.checkPackageDefinition");
                    securityManager.checkPackageDefinition(name.substring(0,i));
                } catch (Exception se) {
                    if (log.isTraceEnabled())
                        log.trace("      -->Exception-->ClassNotFoundException", se);
                    throw new ClassNotFoundException(name, se);
                }
            }
        }

        // Ask our superclass to locate this class, if possible
        // (throws ClassNotFoundException if it is not found)
        Class clazz = null;
        if (log.isTraceEnabled())
            log.trace("      findClassInternal(" + name + ")");
        try {
            clazz = findClassInternal(name);
        } catch(ClassNotFoundException cnfe) {
            throw cnfe;
        } catch(AccessControlException ace) {
            throw new ClassNotFoundException(name, ace);
        } catch (RuntimeException e) {
            if (log.isTraceEnabled())
                log.trace("      -->RuntimeException Rethrown", e);
            throw e;
        }

        // Return the class we have located
        if (log.isTraceEnabled())
            log.debug("      Returning class " + clazz);
        if ((log.isTraceEnabled()) && (clazz != null))
            log.debug("      Loaded by " + clazz.getClassLoader());
        return (clazz);

    }


    /**
     * Find the specified resource in our local repository, and return a
     * <code>URL</code> refering to it, or <code>null</code> if this resource
     * cannot be found.
     *
     * @param name Name of the resource to be found
     */
    public URL findResource(final String name) {

        if (log.isDebugEnabled())
            log.debug("    findResource(" + name + ")");

        URL url = null;

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry == null) {
            entry = findResourceInternal(name, name);
        }
        if (entry != null) {
            url = entry.source;
        }

        if (log.isDebugEnabled()) {
            if (url != null)
                log.debug("    --> Returning '" + url.toString() + "'");
            else
                log.debug("    --> Resource not found, returning null");
        }
        return (url);

    }


    /**
     * Return an enumeration of <code>URLs</code> representing all of the
     * resources with the given name.  If no resources with this name are
     * found, return an empty enumeration.
     *
     * @param name Name of the resources to be found
     *
     * @exception IOException if an input/output error occurs
     */
    public Enumeration findResources(String name) throws IOException {

        if (log.isDebugEnabled())
            log.debug("    findResources(" + name + ")");

        Vector result = new Vector();

        File[] repositories = repository.findExplodedJars();
        JarFile[] jarFiles = repository.findJars();

        // Looking at the repositories
        for (int i = 0; i < repositories.length; i++) {
            File resource = new File(repositories[i], name);
            if (resource.exists()) {
                result.addElement(getURI(resource));
            }
        }

        // Looking at the JAR files
        for (int i = 0; i < jarFiles.length; i++) {
            JarEntry jarEntry = jarFiles[i].getJarEntry(name);
            if (jarEntry != null) {
                File jarFile = new File(jarFiles[i].getName());
                try {
                    String jarFakeUrl = getURI(jarFile).toString();
                    jarFakeUrl = "jar:" + jarFakeUrl + "!/" + name;
                    result.addElement(new URL(jarFakeUrl));
                } catch (MalformedURLException e) {
                    // Ignore
                }
            }
        }

        return result.elements();

    }


    /**
     * Find the resource with the given name.  A resource is some data
     * (images, audio, text, etc.) that can be accessed by class code in a
     * way that is independent of the location of the code.  The name of a
     * resource is a "/"-separated path name that identifies the resource.
     * If the resource cannot be found, return <code>null</code>.
     * <p>
     * This method searches according to the following algorithm, returning
     * as soon as it finds the appropriate URL.  If the resource cannot be
     * found, returns <code>null</code>.
     * <ul>
     * <li>If the <code>delegate</code> property is set to <code>true</code>,
     *     call the <code>getResource()</code> method of the parent class
     *     loader, if any.</li>
     * <li>Call <code>findResource()</code> to find this resource in our
     *     locally defined repositories.</li>
     * <li>Call the <code>getResource()</code> method of the parent class
     *     loader, if any.</li>
     * </ul>
     *
     * @param name Name of the resource to return a URL for
     */
    public URL getResource(String name) {

        if (log.isDebugEnabled())
            log.debug("getResource(" + name + ")");
        URL url = null;

        // (1) Delegate to parent if requested
        if (delegate) {
            if (log.isDebugEnabled())
                log.debug("  Delegating to parent classloader " + parent);
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            url = loader.getResource(name);
            if (url != null) {
                if (log.isDebugEnabled())
                    log.debug("  --> Returning '" + url.toString() + "'");
                return (url);
            }
        }

        // (2) Search local repositories
        url = findResource(name);
        if (url != null) {
            // Locating the repository for special handling in the case 
            // of a JAR
            if (antiJARLocking) {
                ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
                try {
                    String repository = entry.codeBase.toString();
                    if ((repository.endsWith(".jar")) 
                            && (!(name.endsWith(".class")))) {
                        // Copy binary content to the work directory if not present
                        File resourceFile = new File(loaderDir, name);
                        url = getURI(resourceFile);
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            if (log.isDebugEnabled())
                log.debug("  --> Returning '" + url.toString() + "'");
            return (url);
        }

        // (3) Delegate to parent unconditionally if not already attempted
        if( !delegate ) {
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            url = loader.getResource(name);
            if (url != null) {
                if (log.isDebugEnabled())
                    log.debug("  --> Returning '" + url.toString() + "'");
                return (url);
            }
        }

        // (4) Resource was not found
        if (log.isDebugEnabled())
            log.debug("  --> Resource not found, returning null");
        return (null);

    }


    /**
     * Find the resource with the given name, and return an input stream
     * that can be used for reading it.  The search order is as described
     * for <code>getResource()</code>, after checking to see if the resource
     * data has been previously cached.  If the resource cannot be found,
     * return <code>null</code>.
     *
     * @param name Name of the resource to return an input stream for
     */
    public InputStream getResourceAsStream(String name) {

        if (log.isDebugEnabled())
            log.debug("getResourceAsStream(" + name + ")");
        InputStream stream = null;

        // (0) Check for a cached copy of this resource
        stream = findLoadedResource(name);
        if (stream != null) {
            if (log.isDebugEnabled())
                log.debug("  --> Returning stream from cache");
            return (stream);
        }

        // (1) Delegate to parent if requested
        if (delegate) {
            if (log.isDebugEnabled())
                log.debug("  Delegating to parent classloader " + parent);
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            stream = loader.getResourceAsStream(name);
            if (stream != null) {
                // FIXME - cache???
                if (log.isDebugEnabled())
                    log.debug("  --> Returning stream from parent");
                return (stream);
            }
        }

        // (2) Search local repositories
        if (log.isDebugEnabled())
            log.debug("  Searching local repositories");
        URL url = findResource(name);
        if (url != null) {
            // FIXME - cache???
            if (log.isDebugEnabled())
                log.debug("  --> Returning stream from local");
            stream = findLoadedResource(name);
            if (stream != null)
                return (stream);
        }

        // (3) Delegate to parent unconditionally
        if (!delegate) {
            if (log.isDebugEnabled())
                log.debug("  Delegating to parent classloader unconditionally " + parent);
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            stream = loader.getResourceAsStream(name);
            if (stream != null) {
                // FIXME - cache???
                if (log.isDebugEnabled())
                    log.debug("  --> Returning stream from parent");
                return (stream);
            }
        }

        // (4) Resource was not found
        if (log.isDebugEnabled())
            log.debug("  --> Resource not found, returning null");
        return (null);

    }


    /**
     * Load the class with the specified name, searching using the following
     * algorithm until it finds and returns the class.  If the class cannot
     * be found, returns <code>ClassNotFoundException</code>.
     * <ul>
     * <li>Call <code>findLoadedClass(String)</code> to check if the
     *     class has already been loaded.  If it has, the same
     *     <code>Class</code> object is returned.</li>
     * <li>If the <code>delegate</code> property is set to <code>true</code>,
     *     call the <code>loadClass()</code> method of the parent class
     *     loader, if any.</li>
     * <li>Call <code>findClass()</code> to find this class in our locally
     *     defined repositories.</li>
     * <li>Call the <code>loadClass()</code> method of our parent
     *     class loader, if any.</li>
     * </ul>
     * If the class was found using the above steps, and the
     * <code>resolve</code> flag is <code>true</code>, this method will then
     * call <code>resolveClass(Class)</code> on the resulting Class object.
     *
     * @param name Name of the class to be loaded
     * @param resolve If <code>true</code> then resolve the class
     *
     * @exception ClassNotFoundException if the class was not found
     */
    public Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException {

        if (log.isDebugEnabled())
            log.debug("loadClass(" + name + ", " + resolve + ")");
        Class clazz = null;

        // Log access to stopped classloader
        if (!started) {
            try {
                throw new IllegalStateException();
            } catch (IllegalStateException e) {
                log.info(sm.getString("webappClassLoader.stopped", name), e);
            }
        }

        // (0) Check our previously loaded local class cache
        clazz = findLoadedClass0(name);
        if (clazz != null) {
            if (log.isDebugEnabled())
                log.debug("  Returning class from cache");
            if (resolve)
                resolveClass(clazz);
            return (clazz);
        }

        // (0.1) Check our previously loaded class cache
        clazz = findLoadedClass(name);
        if (clazz != null) {
            if (log.isDebugEnabled())
                log.debug("  Returning class from cache");
            if (resolve)
                resolveClass(clazz);
            return (clazz);
        }

        // (0.2) Try loading the class with the system class loader, to prevent
        //       the webapp from overriding J2SE classes
        if (SYSTEM_CL_DELEGATION) {
            try {
                clazz = Class.forName(name, false, system);
                if (clazz != null) {
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }

        // (0.5) Permission to access this class when using a SecurityManager
        if (securityManager != null) {
            int i = name.lastIndexOf('.');
            if (i >= 0) {
                try {
                    securityManager.checkPackageAccess(name.substring(0,i));
                } catch (SecurityException se) {
                    String error = "Security Violation, attempt to use " +
                        "Restricted Class: " + name;
                    log.info(error, se);
                    throw new ClassNotFoundException(error, se);
                }
            }
        }

        boolean delegateLoad = delegate || filter(name);

        // (1) Delegate to our parent if requested
        if (delegateLoad) {
            if (log.isDebugEnabled())
                log.debug("  Delegating to parent classloader1 " + parent);
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            try {
                clazz = Class.forName(name, false, loader);
                if (clazz != null) {
                    if (log.isDebugEnabled())
                        log.debug("  Loading class from parent");
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                ;
            }
        }

        // (2) Search local repositories
        if (log.isDebugEnabled())
            log.debug("  Searching local repositories");
        try {
            clazz = findClass(name);
            if (clazz != null) {
                if (log.isDebugEnabled())
                    log.debug("  Loading class from local repository");
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
            ;
        }

        // (3) Delegate to parent unconditionally
        if (!delegateLoad) {
            if (log.isDebugEnabled())
                log.debug("  Delegating to parent classloader at end: " + parent);
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            try {
                clazz = Class.forName(name, false, loader);
                if (clazz != null) {
                    if (log.isDebugEnabled())
                        log.debug("  Loading class from parent");
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                ;
            }
        }

        throw new ClassNotFoundException(name);
    }


    /**
     * Get the Permissions for a CodeSource.  If this instance
     * of WebappClassLoader is for a web application context,
     * add read FilePermission or JndiPermissions for the base
     * directory (if unpacked),
     * the context URL, and jar file resources.
     *
     * @param codeSource where the code was loaded from
     * @return PermissionCollection for CodeSource
     */
    protected PermissionCollection getPermissions(CodeSource codeSource) {

        String codeUrl = codeSource.getLocation().toString();
        PermissionCollection pc;
        if ((pc = (PermissionCollection)loaderPC.get(codeUrl)) == null) {
            pc = super.getPermissions(codeSource);
            if (pc != null) {
                Iterator perms = permissionList.iterator();
                while (perms.hasNext()) {
                    Permission p = (Permission)perms.next();
                    pc.add(p);
                }
                loaderPC.put(codeUrl,pc);
            }
        }
        return (pc);

    }


    /**
     * Returns the search path of URLs for loading classes and resources.
     * This includes the original list of URLs specified to the constructor,
     * along with any URLs subsequently appended by the addURL() method.
     * @return the search path of URLs for loading classes and resources.
     */
    public URL[] getURLs() {

        if (repositoryURLs != null) {
            return repositoryURLs;
        }

        URL[] external = super.getURLs();

        File[] repositories = repository.findExplodedJars();
        JarFile[] jarFiles = repository.findJars();

        int length = repositories.length + jarFiles.length;

        try {

            URL[] urls = new URL[length];
            for (int i = 0; i < length; i++) {
                if (i < repositories.length) {
                    urls[i] = getURL(repositories[i], true);
                } else {
                    urls[i] = getURL(new File(jarFiles[i - repositories.length].getName()), true);
                }
            }

            repositoryURLs = urls;

        } catch (MalformedURLException e) {
            repositoryURLs = new URL[0];
        }

        return repositoryURLs;

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {
    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this 
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {
        return new LifecycleListener[0];
    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {
    }


    /**
     * Start the class loader.
     *
     * @exception LifecycleException if a lifecycle error occurs
     */
    public void start() throws LifecycleException {

        started = true;
        String encoding = null;
        try {
            encoding = System.getProperty("file.encoding");
        } catch (Exception e) {
            return;
        }
        if (encoding.indexOf("EBCDIC")!=-1) {
            needConvert = true;
        }

    }


    /**
     * Stop the class loader.
     *
     * @exception LifecycleException if a lifecycle error occurs
     */
    public void stop() throws LifecycleException {

        // Clearing references should be done before setting started to
        // false, due to possible side effects
        clearReferences();

        started = false;

        notFoundResources.clear();
        resourceEntries.clear();
        repositoryURLs = null;
        parent = null;
        repository = null;

        permissionList.clear();
        loaderPC.clear();

        if (loaderDir != null) {
            deleteDir(loaderDir);
        }

    }


    // ------------------------------------------------------ Protected Methods

    
    /**
     * Clear references.
     */
    protected void clearReferences() {

        /*
         * Deregister any JDBC drivers registered by the webapp that the webapp
         * forgot. This is made unnecessary complex because a) DriverManager
         * checks the class loader of the calling class (it would be much easier
         * if it checked the context class loader) b) using reflection would
         * create a dependency on the DriverManager implementation which can,
         * and has, changed.
         * 
         * We can't just create an instance of JdbcLeakPrevention as it will be
         * loaded by the common class loader (since it's .class file is in the
         * $CATALINA_HOME/lib directory). This would fail DriverManager's check
         * on the class loader of the calling class. So, we load the bytes via
         * our parent class loader but define the class with this class loader
         * so the JdbcLeakPrevention looks like a webapp class to the
         * DriverManager.
         * 
         * If only apps cleaned up after themselves...
         */
        InputStream is = getResourceAsStream(
                "org/apache/catalina/loader/JdbcLeakPrevention.class");
        // Cheat - we know roughly how big the class will be (~1K) but allow
        // plenty room to grow
        byte[] classBytes = new byte[4096];
        int offset = 0;
        try {
            int read = is.read(classBytes, offset, 4096-offset);
            while (read > -1) {
                offset += read;
                read = is.read(classBytes, offset, 4096-offset);
            }
            Class<?> lpClass =
                defineClass("org.apache.catalina.loader.JdbcLeakPrevention",
                    classBytes, 0, offset);
            Object obj = lpClass.newInstance();
            obj.getClass().getMethod(
                    "clearJdbcDriverRegistrations").invoke(obj);
        } catch (Exception e) {
            // So many things to go wrong above...
            log.warn(sm.getString("webappClassLoader.jdbcRemoveFailed"), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    log.warn(sm.getString(
                            "webappClassLoader.jdbcRemoveStreamError"), ioe);
                }
            }
        }
        
        // Null out any static or final fields from loaded classes,
        // as a workaround for apparent garbage collection bugs
        if (ENABLE_CLEAR_REFERENCES) {
            Iterator loadedClasses = resourceEntries.values().iterator();
            while (loadedClasses.hasNext()) {
                ResourceEntry entry = (ResourceEntry) loadedClasses.next();
                if (entry.loadedClass != null) {
                    Class clazz = entry.loadedClass;
                    try {
                        Field[] fields = clazz.getDeclaredFields();
                        for (int i = 0; i < fields.length; i++) {
                            Field field = fields[i];
                            int mods = field.getModifiers();
                            if (field.getType().isPrimitive() 
                                    || (field.getName().indexOf("$") != -1)) {
                                continue;
                            }
                            if (Modifier.isStatic(mods)) {
                                try {
                                    field.setAccessible(true);
                                    if (Modifier.isFinal(mods)) {
                                        if (!((field.getType().getName().startsWith("java."))
                                                || (field.getType().getName().startsWith("javax.")))) {
                                            nullInstance(field.get(null));
                                        }
                                    } else {
                                        field.set(null, null);
                                        if (log.isDebugEnabled()) {
                                            log.debug("Set field " + field.getName() 
                                                    + " to null in class " + clazz.getName());
                                        }
                                    }
                                } catch (Throwable t) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Could not set field " + field.getName() 
                                                + " to null in class " + clazz.getName(), t);
                                    }
                                }
                            }
                        }
                    } catch (Throwable t) {
                        if (log.isDebugEnabled()) {
                            log.debug("Could not clean fields for class " + clazz.getName(), t);
                        }
                    }
                }
            }
        }
        
         // Clear the IntrospectionUtils cache.
        IntrospectionUtils.clear();
        
        // Clear the classloader reference in the VM's bean introspector
        java.beans.Introspector.flushCaches();

    }


    protected void nullInstance(Object instance) {
        if (instance == null) {
            return;
        }
        Field[] fields = instance.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            int mods = field.getModifiers();
            if (field.getType().isPrimitive() 
                    || (field.getName().indexOf("$") != -1)) {
                continue;
            }
            try {
                field.setAccessible(true);
                if (Modifier.isStatic(mods) && Modifier.isFinal(mods)) {
                    // Doing something recursively is too risky
                    continue;
                } else {
                    Object value = field.get(instance);
                    if (null != value) {
                        Class valueClass = value.getClass();
                        if (!loadedByThisOrChild(valueClass)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Not setting field " + field.getName() +
                                        " to null in object of class " + 
                                        instance.getClass().getName() +
                                        " because the referenced object was of type " +
                                        valueClass.getName() + 
                                        " which was not loaded by this WebappClassLoader.");
                            }
                        } else {
                            field.set(instance, null);
                            if (log.isDebugEnabled()) {
                                log.debug("Set field " + field.getName() 
                                        + " to null in class " + instance.getClass().getName());
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not set field " + field.getName() 
                            + " to null in object instance of class " 
                            + instance.getClass().getName(), t);
                }
            }
        }
    }


    /**
     * Determine whether a class was loaded by this class loader or one of
     * its child class loaders.
     */
    protected boolean loadedByThisOrChild(Class clazz)
    {
        boolean result = false;
        for (ClassLoader classLoader = clazz.getClassLoader();
                null != classLoader; classLoader = classLoader.getParent()) {
            if (classLoader.equals(this)) {
                result = true;
                break;
            }
        }
        return result;
    }    


    /**
     * Find specified class in local repositories.
     *
     * @return the loaded class, or null if the class isn't found
     */
    protected Class findClassInternal(String name)
        throws ClassNotFoundException {

        if (!validate(name))
            throw new ClassNotFoundException(name);

        String tempPath = name.replace('.', '/');
        String classPath = tempPath + ".class";

        ResourceEntry entry = null;

        entry = findResourceInternal(name, classPath);

        if (entry == null)
            throw new ClassNotFoundException(name);

        Class clazz = entry.loadedClass;
        if (clazz != null)
            return clazz;

        synchronized (this) {
            clazz = entry.loadedClass;
            if (clazz != null)
                return clazz;

            if (entry.binaryContent == null)
                throw new ClassNotFoundException(name);

            // Looking up the package
            String packageName = null;
            int pos = name.lastIndexOf('.');
            if (pos != -1)
                packageName = name.substring(0, pos);
        
            Package pkg = null;
        
            if (packageName != null) {
                pkg = getPackage(packageName);
                // Define the package (if null)
                if (pkg == null) {
                    try {
                        if (entry.manifest == null) {
                            definePackage(packageName, null, null, null, null,
                                    null, null, null);
                        } else {
                            definePackage(packageName, entry.manifest,
                                    entry.codeBase);
                        }
                    } catch (IllegalArgumentException e) {
                        // Ignore: normal error due to dual definition of package
                    }
                    pkg = getPackage(packageName);
                }
            }
    
            if (securityManager != null) {

                // Checking sealing
                if (pkg != null) {
                    boolean sealCheck = true;
                    if (pkg.isSealed()) {
                        sealCheck = pkg.isSealed(entry.codeBase);
                    } else {
                        sealCheck = (entry.manifest == null)
                            || !isPackageSealed(packageName, entry.manifest);
                    }
                    if (!sealCheck)
                        throw new SecurityException
                            ("Sealing violation loading " + name + " : Package "
                             + packageName + " is sealed.");
                }
    
            }

            clazz = defineClass(name, entry.binaryContent, 0,
                    entry.binaryContent.length, 
                    new CodeSource(entry.codeBase, entry.certificates));
            entry.loadedClass = clazz;
            entry.binaryContent = null;
            entry.source = null;
            entry.codeBase = null;
            entry.manifest = null;
            entry.certificates = null;
        }
        
        return clazz;

    }

    /**
     * Find specified resource in local repositories. This block
     * will execute under an AccessControl.doPrivilege block.
     *
     * @return the loaded resource, or null if the resource isn't found
     */
    protected ResourceEntry findResourceInternal(File file, String path){
        ResourceEntry entry = new ResourceEntry();
        try {
            entry.source = getURI(new File(file, path));
            entry.codeBase = getURL(new File(file, path), false);
        } catch (MalformedURLException e) {
            return null;
        }   
        return entry;
    }
    

    /**
     * Find specified resource in local repositories.
     *
     * @return the loaded resource, or null if the resource isn't found
     */
    protected ResourceEntry findResourceInternal(String name, String path) {

        if (!started) {
            log.info(sm.getString("webappClassLoader.stopped", name));
            return null;
        }

        if ((name == null) || (path == null))
            return null;

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry != null)
            return entry;

        int contentLength = -1;
        InputStream binaryStream = null;

        File[] repositories = repository.findExplodedJars();
        JarFile[] jarFiles = repository.findJars();

        int i;

        //Resource resource = null;

        boolean fileNeedConvert = false;

        for (i = 0; (entry == null) && (i < repositories.length); i++) {

            File resource = new File(repositories[i], path);
            if (!resource.exists()) {
                continue;
            }

            // Note : Not getting an exception here means the resource was
            // found
            if (securityManager != null) {
                PrivilegedAction dp =
                    new PrivilegedFindResource(repositories[i], path);
                entry = (ResourceEntry)AccessController.doPrivileged(dp);
            } else {
                entry = findResourceInternal(repositories[i], path);
            }

            contentLength = (int) resource.length();
            entry.lastModified = resource.lastModified();

            try {
                binaryStream = new FileInputStream(resource);
            } catch (IOException e) {
                return null;
            }

            if (needConvert) {
                if (path.endsWith(".properties")) {
                    fileNeedConvert = true;
                }
            }

        }

        if ((entry == null) && (notFoundResources.containsKey(name)))
            return null;

        JarEntry jarEntry = null;

        for (i = 0; (entry == null) && (i < jarFiles.length); i++) {

            jarEntry = jarFiles[i].getJarEntry(path);

            if (jarEntry != null) {

                entry = new ResourceEntry();
                try {
                    File jarFile = new File(jarFiles[i].getName());
                    entry.codeBase = getURL(jarFile, false);
                    String jarFakeUrl = getURI(jarFile).toString();
                    jarFakeUrl = "jar:" + jarFakeUrl + "!/" + path;
                    entry.source = new URL(jarFakeUrl);
                    entry.lastModified = jarFile.lastModified();
                } catch (MalformedURLException e) {
                    return null;
                }
                contentLength = (int) jarEntry.getSize();
                try {
                    entry.manifest = jarFiles[i].getManifest();
                    binaryStream = jarFiles[i].getInputStream(jarEntry);
                } catch (IOException e) {
                    return null;
                }

                // Extract resources contained in JAR to the workdir
                if (antiJARLocking && !(path.endsWith(".class"))) {
                    byte[] buf = new byte[1024];
                    File resourceFile = new File
                    (loaderDir, jarEntry.getName());
                    if (!resourceFile.exists()) {
                        Enumeration entries = jarFiles[i].entries();
                        while (entries.hasMoreElements()) {
                            JarEntry jarEntry2 = 
                                (JarEntry) entries.nextElement();
                            if (!(jarEntry2.isDirectory()) 
                                    && (!jarEntry2.getName().endsWith(".class"))) {
                                resourceFile = new File
                                (loaderDir, jarEntry2.getName());
                                resourceFile.getParentFile().mkdirs();
                                FileOutputStream os = null;
                                InputStream is = null;
                                try {
                                    is = jarFiles[i].getInputStream(jarEntry2);
                                    os = new FileOutputStream(resourceFile);
                                    while (true) {
                                        int n = is.read(buf);
                                        if (n <= 0) {
                                            break;
                                        }
                                        os.write(buf, 0, n);
                                    }
                                } catch (IOException e) {
                                    // Ignore
                                } finally {
                                    try {
                                        if (is != null) {
                                            is.close();
                                        }
                                    } catch (IOException e) {
                                    }
                                    try {
                                        if (os != null) {
                                            os.close();
                                        }
                                    } catch (IOException e) {
                                    }
                                }
                            }
                        }
                    }
                }

            }
            
        }

        if (entry == null) {
            notFoundResources.put(name, VALUE);
            return null;
        }

        if (binaryStream != null) {

            byte[] binaryContent = new byte[contentLength];

            int pos = 0;
            try {

                while (true) {
                    int n = binaryStream.read(binaryContent, pos,
                            binaryContent.length - pos);
                    if (n <= 0)
                        break;
                    pos += n;
                }
            } catch (IOException e) {
                log.error(sm.getString("webappClassLoader.readError", name), e);
                return null;
            } finally {
                try {
                    binaryStream.close();
                } catch (IOException e) {}
            }

            if (fileNeedConvert) {
                String str = new String(binaryContent,0,pos);
                try {
                    binaryContent = str.getBytes("UTF-8");
                } catch (Exception e) {
                    return null;
                }
            }
            entry.binaryContent = binaryContent;

            // The certificates are only available after the JarEntry 
            // associated input stream has been fully read
            if (jarEntry != null) {
                entry.certificates = jarEntry.getCertificates();
            }

        }

        // Add the entry in the local resource repository
        synchronized (resourceEntries) {
            // Ensures that all the threads which may be in a race to load
            // a particular class all end up with the same ResourceEntry
            // instance
            ResourceEntry entry2 = (ResourceEntry) resourceEntries.get(name);
            if (entry2 == null) {
                resourceEntries.put(name, entry);
            } else {
                entry = entry2;
            }
        }

        return entry;

    }


    /**
     * Returns true if the specified package name is sealed according to the
     * given manifest.
     */
    protected boolean isPackageSealed(String name, Manifest man) {

        String path = name.replace('.', '/') + '/';
        Attributes attr = man.getAttributes(path); 
        String sealed = null;
        if (attr != null) {
            sealed = attr.getValue(Name.SEALED);
        }
        if (sealed == null) {
            if ((attr = man.getMainAttributes()) != null) {
                sealed = attr.getValue(Name.SEALED);
            }
        }
        return "true".equalsIgnoreCase(sealed);

    }


    /**
     * Finds the resource with the given name if it has previously been
     * loaded and cached by this class loader, and return an input stream
     * to the resource data.  If this resource has not been cached, return
     * <code>null</code>.
     *
     * @param name Name of the resource to return
     */
    protected InputStream findLoadedResource(String name) {

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry != null) {
            if (entry.binaryContent != null)
                return new ByteArrayInputStream(entry.binaryContent);
        }
        return (null);

    }


    /**
     * Finds the class with the given name if it has previously been
     * loaded and cached by this class loader, and return the Class object.
     * If this class has not been cached, return <code>null</code>.
     *
     * @param name Name of the resource to return
     */
    protected Class findLoadedClass0(String name) {

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry != null) {
            return entry.loadedClass;
        }
        return (null);  // FIXME - findLoadedResource()

    }


    /**
     * Refresh the system policy file, to pick up eventual changes.
     */
    protected void refreshPolicy() {

        try {
            // The policy file may have been modified to adjust 
            // permissions, so we're reloading it when loading or 
            // reloading a Context
            Policy policy = Policy.getPolicy();
            policy.refresh();
        } catch (AccessControlException e) {
            // Some policy files may restrict this, even for the core,
            // so this exception is ignored
        }

    }


    /**
     * Filter classes.
     * 
     * @param name class name
     * @return true if the class should be filtered
     */
    protected boolean filter(String name) {

        if (name == null)
            return false;

        // Looking up the package
        String packageName = null;
        int pos = name.lastIndexOf('.');
        if (pos != -1)
            packageName = name.substring(0, pos);
        else
            return false;

        for (int i = 0; i < packageTriggers.length; i++) {
            if (packageName.startsWith(packageTriggers[i]))
                return true;
        }

        return false;

    }


    /**
     * Validate a classname. As per SRV.9.7.2, we must restict loading of 
     * classes from J2SE (java.*) and classes of the servlet API 
     * (javax.servlet.*). That should enhance robustness and prevent a number
     * of user error (where an older version of servlet.jar would be present
     * in /WEB-INF/lib).
     * 
     * @param name class name
     * @return true if the name is valid
     */
    protected boolean validate(String name) {

        if (name == null)
            return false;
        if (name.startsWith("java."))
            return false;

        return true;

    }


    /**
     * Check the specified JAR file, and return <code>true</code> if it does
     * not contain any of the trigger classes.
     *
     * @param jarfile The JAR file to be checked
     *
     * @exception IOException if an input/output error occurs
     */
    protected boolean validateJarFile(File jarfile)
        throws IOException {

        if (triggers == null)
            return (true);
        JarFile jarFile = new JarFile(jarfile);
        for (int i = 0; i < triggers.length; i++) {
            Class clazz = null;
            try {
                if (parent != null) {
                    clazz = parent.loadClass(triggers[i]);
                } else {
                    clazz = Class.forName(triggers[i]);
                }
            } catch (Throwable t) {
                clazz = null;
            }
            if (clazz == null)
                continue;
            String name = triggers[i].replace('.', '/') + ".class";
            if (log.isDebugEnabled())
                log.debug(" Checking for " + name);
            JarEntry jarEntry = jarFile.getJarEntry(name);
            if (jarEntry != null) {
                log.info("validateJarFile(" + jarfile + 
                    ") - jar not loaded. See Servlet Spec 2.3, "
                    + "section 9.7.2. Offending class: " + name);
                jarFile.close();
                return (false);
            }
        }
        jarFile.close();
        return (true);

    }


    /**
     * Get URL.
     */
    protected URL getURL(File file, boolean encoded)
        throws MalformedURLException {

        File realFile = file;
        try {
            realFile = realFile.getCanonicalFile();
        } catch (IOException e) {
            // Ignore
        }
        if(encoded) {
            return getURI(realFile);
        } else {
            return realFile.toURL();
        }

    }


    /**
     * Get URL.
     */
    protected URL getURI(File file)
        throws MalformedURLException {


        File realFile = file;
        try {
            realFile = realFile.getCanonicalFile();
        } catch (IOException e) {
            // Ignore
        }
        return realFile.toURI().toURL();

    }


    /**
     * Delete the specified directory, including all of its contents and
     * subdirectories recursively.
     *
     * @param dir File object representing the directory to be deleted
     */
    protected static void deleteDir(File dir) {

        String files[] = dir.list();
        if (files == null) {
            files = new String[0];
        }
        for (int i = 0; i < files.length; i++) {
            File file = new File(dir, files[i]);
            if (file.isDirectory()) {
                deleteDir(file);
            } else {
                file.delete();
            }
        }
        dir.delete();

    }


}

