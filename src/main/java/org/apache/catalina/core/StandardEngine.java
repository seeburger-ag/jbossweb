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


import static org.jboss.web.CatalinaMessages.MESSAGES;

import java.util.Locale;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Service;
import org.apache.tomcat.util.modeler.Registry;
import org.jboss.web.CatalinaLogger;

/**
 * Standard implementation of the <b>Engine</b> interface.  Each
 * child container must be a Host implementation to process the specific
 * fully qualified host name of that virtual host. <br/>
 * You can set the jvmRoute direct or with the System.property <b>jvmRoute</b>.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1673 $ $Date: 2011-03-12 18:58:07 +0100 (Sat, 12 Mar 2011) $
 */

public class StandardEngine
    extends ContainerBase
    implements Engine {

    // ----------------------------------------------------------- Constructors


    /**
     * Create a new StandardEngine component with the default basic Valve.
     */
    public StandardEngine() {

        super();
        pipeline.setBasic(new StandardEngineValve());
        /* Set the jmvRoute using the system property jvmRoute */
        try {
            setJvmRoute(System.getProperty("jvmRoute"));
        } catch(Exception ex) {
        }
        // By default, the engine will hold the reloading thread
        backgroundProcessorDelay = 10;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Host name to use when no server host, or an unknown host,
     * is specified in the request.
     */
    private String defaultHost = null;


    /**
     * The descriptive information string for this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardEngine/1.0";


    /**
     * The <code>Service</code> that owns this Engine, if any.
     */
    private Service service = null;

    /** Allow the base dir to be specified explicitely for
     * each engine. In time we should stop using catalina.base property -
     * otherwise we loose some flexibility.
     */
    private String baseDir = null;

    /**
     * The JVM Route ID for this Tomcat instance. All Route ID's must be unique
     * across the cluster.
     */
    private String jvmRouteId;


    // ------------------------------------------------------------- Properties

    /**
     * Return the default host.
     */
    public String getDefaultHost() {

        return (defaultHost);

    }


    /**
     * Set the default host.
     *
     * @param host The new default host
     */
    public void setDefaultHost(String host) {

        String oldDefaultHost = this.defaultHost;
        if (host == null) {
            this.defaultHost = null;
        } else {
            this.defaultHost = host.toLowerCase(Locale.ENGLISH);
        }
        support.firePropertyChange("defaultHost", oldDefaultHost,
                                   this.defaultHost);

    }
    
    public void setName(String name ) {
        if( domain != null ) {
            // keep name==domain, ignore override
            // we are already registered
            super.setName( domain );
            return;
        }
        // The engine name is used as domain
        domain=name; // XXX should we set it in init() ? It shouldn't matter
        super.setName( name );
    }


    /**
     * Set the cluster-wide unique identifier for this Engine.
     * This value is only useful in a load-balancing scenario.
     * <p>
     * This property should not be changed once it is set.
     */
    public void setJvmRoute(String routeId) {
        jvmRouteId = routeId;
    }


    /**
     * Retrieve the cluster-wide unique identifier for this Engine.
     * This value is only useful in a load-balancing scenario.
     */
    public String getJvmRoute() {
        return jvmRouteId;
    }


    /**
     * Return the <code>Service</code> with which we are associated (if any).
     */
    public Service getService() {

        return (this.service);

    }


    /**
     * Set the <code>Service</code> with which we are associated (if any).
     *
     * @param service The service that owns this Engine
     */
    public void setService(Service service) {
        this.service = service;
    }

    public String getBaseDir() {
        if( baseDir==null ) {
            baseDir=System.getProperty("catalina.base");
        }
        if( baseDir==null ) {
            baseDir=System.getProperty("catalina.home");
        }
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    // --------------------------------------------------------- Public Methods


    /**
     * Add a child Container, only if the proposed child is an implementation
     * of Host.
     *
     * @param child Child container to be added
     */
    public void addChild(Container child) {

        if (!(child instanceof Host))
            throw MESSAGES.engineChildMustBeHost();
        super.addChild(child);

    }


    /**
     * Return descriptive information about this Container implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }

    /**
     * Disallow any attempt to set a parent for this Container, since an
     * Engine is supposed to be at the top of the Container hierarchy.
     *
     * @param container Proposed parent Container
     */
    public void setParent(Container container) {

        throw MESSAGES.engineHasNoParent();

    }


    private boolean initialized=false;
    
    public void init() {
        if( initialized ) return;
        initialized=true;

        if (org.apache.tomcat.util.Constants.ENABLE_MODELER) {
            if( oname==null ) {
                // not registered in JMX yet - standalone mode
                try {
                    if (domain==null) {
                        domain=getName();
                    }
                    oname=new ObjectName(domain + ":type=Engine");
                    controller=oname;
                    Registry.getRegistry(null, null)
                    .registerComponent(this, oname, null);
                } catch( Throwable t ) {
                    CatalinaLogger.CORE_LOGGER.failedEngineJmxRegistration(oname, t);
                }
            }
        }
        
        if( service==null ) {
            // for consistency...: we are probably in embeded mode
            try {
                service=new StandardService();
                service.setContainer( this );
                service.initialize();
            } catch( Throwable t ) {
                CatalinaLogger.CORE_LOGGER.failedServiceCreation(t);
            }
        }
        
    }
    
    public void destroy() throws LifecycleException {
        if( ! initialized ) return;
        initialized=false;
        
        // if we created it, make sure it's also destroyed
        // this call implizit this.stop()
        ((StandardService)service).destroy();

        if (org.apache.tomcat.util.Constants.ENABLE_MODELER) {
            if ( oname != null ) {
                try {
                    if( controller == oname ) {
                        Registry.getRegistry(null, null).unregisterComponent(oname);
                    }
                } catch( Throwable t ) {
                    CatalinaLogger.CORE_LOGGER.failedContainerJmxUnregistration(oname, t);
                }
            }
            // force all metadata to be reloaded.
            // That doesn't affect existing beans. We should make it per
            // registry - and stop using the static.
            Registry.getRegistry(null, null).resetMetadata();
        }        
    }
    
    /**
     * Start this Engine component.
     *
     * @exception LifecycleException if a startup error occurs
     */
    public void start() throws LifecycleException {
        if( started ) {
            return;
        }
        if( !initialized ) {
            init();
        }

        // Standard container startup
        super.start();

    }
    
    /**
     * Return a String representation of this component.
     */
    public String toString() {

        StringBuilder sb = new StringBuilder("StandardEngine[");
        sb.append(getName());
        sb.append("]");
        return (sb.toString());

    }


    // ------------------------------------------------------ Protected Methods


    // -------------------- JMX registration  --------------------

    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws Exception
    {
        super.preRegister(server,name);

        this.setName( name.getDomain());

        return name;
    }

    // FIXME Remove -- not used 
    public ObjectName getParentName() throws MalformedObjectNameException {
        if (getService()==null) {
            return null;
        }
        String name = getService().getName();
        ObjectName serviceName=new ObjectName(domain +
                        ":type=Service,serviceName="+name);
        return serviceName;                
    }
    
    public ObjectName createObjectName(String domain, ObjectName parent)
        throws Exception
    {
        return new ObjectName( domain + ":type=Engine");
    }

    public String getDomain() {
        if (domain!=null) {
            return domain;
        } else { 
            return getName();
        }
    }
    
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
}
