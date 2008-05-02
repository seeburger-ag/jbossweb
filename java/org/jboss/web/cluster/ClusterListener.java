/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
 */


package org.jboss.web.cluster;


import java.net.InetAddress;
import java.util.HashMap;

import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.util.StringManager;
import org.apache.tomcat.util.IntrospectionUtils;
import org.jboss.logging.Logger;



/**
 * 
 */

public class ClusterListener
    implements LifecycleListener, ContainerListener {

    private static Logger log = Logger.getLogger(ClusterListener.class);

    /**
     * The string manager for this package.
     */
    protected StringManager sm =
        StringManager.getManager(Constants.Package);


    // ---------------------------------------------- Constants


    // ---------------------------------------------- Properties


    protected int proxyPort = 8000;
    public int getProxyPort() { return proxyPort; }
    public void setProxyPort(int proxyPort) { this.proxyPort = proxyPort; }


    protected InetAddress proxyAddress = null;
    public InetAddress getProxyAddress() { return proxyAddress; }
    public void setAddress(InetAddress proxyAddress) { this.proxyAddress = proxyAddress; }


    // ---------------------------------------------- LifecycleListener Methods

    /**
     * Acknowledge the occurrence of the specified event.
     * Note: Will never be called when the listener is associated to a Server,
     * since it is not a Container.
     *
     * @param event ContainerEvent that has occurred
     */
    public void containerEvent(ContainerEvent event) {

        Container container = event.getContainer();
        Object child = event.getData();
        String type = event.getType();

        if (type.equals(Container.ADD_CHILD_EVENT)) {
            if (container instanceof Host) {
                // Deploying a webapp
                addContext((Context) child);
            } else if (container instanceof Engine) {
                // Deploying a host
                container.addContainerListener(this);
            }
        } else if (type.equals(Container.REMOVE_CHILD_EVENT)) {
            if (container instanceof Host) {
                // Undeploying a webapp
                removeContext((Context) child);
            } else if (container instanceof Engine) {
                // Undeploying a host
                container.removeContainerListener(this);
            }
        }

    }

    /**
     * Primary entry point for startup and shutdown events.
     *
     * @param event The event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        Object source = event.getLifecycle();

        if (Lifecycle.START_EVENT.equals(event.getType())) {
            if (source instanceof Context) {
                // Start a webapp
                startContext((Context) source);
            } else if (source instanceof Server) {
                Service[] services = ((Server) source).findServices();
                for (int i = 0; i < services.length; i++) {
                    services[i].getContainer().addContainerListener(this);
                    ((Lifecycle) services[i].getContainer()).addLifecycleListener(this);
                    config((Engine) services[i].getContainer());
                    Container[] children = services[i].getContainer().findChildren();
                    for (int j = 0; j < children.length; j++) {
                        children[j].addContainerListener(this);
                        Container[] children2 = children[j].findChildren();
                        for (int k = 0; k < children2.length; k++) {
                            addContext((Context) children2[k]);
                        }
                    }
                }
            } else {
                return;
            }
        } else if (Lifecycle.STOP_EVENT.equals(event.getType())) {
            if (source instanceof Context) {
                // Stop a webapp
                stopContext((Context) source);
            } else if (source instanceof Server) {
                Service[] services = ((Server) source).findServices();
                for (int i = 0; i < services.length; i++) {
                    services[i].getContainer().removeContainerListener(this);
                    ((Lifecycle) services[i].getContainer()).removeLifecycleListener(this);
                    removeAll((Engine) services[i].getContainer());
                    Container[] children = services[i].getContainer().findChildren();
                    for (int j = 0; j < children.length; j++) {
                        children[j].removeContainerListener(this);
                        Container[] children2 = children[j].findChildren();
                        for (int k = 0; k < children2.length; k++) {
                            removeContext((Context) children2[k]);
                        }
                    }
                }
            } else {
                return;
            }
        } else if (Lifecycle.PERIODIC_EVENT.equals(event.getType())) {
            if (source instanceof Engine) {
                status((Engine) source);
            }
        }

    }
    
    
    protected void config(Engine engine) {
        System.out.println("Config: " + engine.getName());
        // Collect configuration from the connectors and service and call CONFIG
        Connector connector = findProxyConnector(engine.getService().findConnectors());
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("JVMRoute", engine.getJvmRoute());
        boolean reverseConnection = 
            Boolean.TRUE.equals(IntrospectionUtils.getProperty(connector.getProtocolHandler(), "reverseConnection"));
        boolean ssl = 
            Boolean.TRUE.equals(IntrospectionUtils.getProperty(connector.getProtocolHandler(), "SSLEnabled"));
        boolean ajp = ((String) IntrospectionUtils.getProperty(connector.getProtocolHandler(), "name")).startsWith("ajp-");

        if (reverseConnection) {
            parameters.put("Reversed", "true");
        }
        parameters.put("Host", getAddress(connector));
        parameters.put("Port", "" + connector.getPort());
        if (ajp) {
            parameters.put("Type", "ajp");
        } else if (ssl) {
            parameters.put("Type", "https");
        } else {
            parameters.put("Type", "http");
        }
        
        // Send CONFIG request
        // FIXME: By default, connect on localhost on some predefined port ?
    }

    
    protected void removeAll(Engine engine) {
        System.out.println("Stop: " + engine.getName());
        // FIXME: send STATUS
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("JVMRoute", engine.getJvmRoute());

        // FIXME: Send REMOVE-APP * request
    }


    protected void status(Engine engine) {
        System.out.println("Status: " + engine.getName());
        // FIXME: send STATUS
        Connector connector = findProxyConnector(engine.getService().findConnectors());
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("JVMRoute", engine.getJvmRoute());

        // FIXME: Send STATUS request
    }

    
    protected void addContext(Context context) {
        System.out.println("Deploy context: " + context.getPath() + " to Host: " + context.getParent().getName() + " State: " + ((StandardContext) context).getState());
        ((Lifecycle) context).addLifecycleListener(this);

        boolean started = (((StandardContext) context).getState() == 1);
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("JVMRoute", getJvmRoute(context));
        parameters.put("context", ("".equals(context.getPath())) ? "/" : context.getPath());

        // FIXME: send ENABLE-APP if state is started
    }


    protected void removeContext(Context context) {
        System.out.println("Undeploy context: " + context.getPath() + " to Host: " + context.getParent().getName() + " State: " + ((StandardContext) context).getState());
        ((Lifecycle) context).removeLifecycleListener(this);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("JVMRoute", getJvmRoute(context));
        parameters.put("context", ("".equals(context.getPath())) ? "/" : context.getPath());

        // FIXME: send REMOVE-APP
    }


    protected void startContext(Context context) {
        Container parent = context.getParent();
        System.out.println("Start context: " + context.getPath() + " to Host: " + parent.getName());

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("JVMRoute", getJvmRoute(context));
        parameters.put("context", ("".equals(context.getPath())) ? "/" : context.getPath());

        // FIXME: send ENABLE-APP
    }


    protected void stopContext(Context context) {
        Container parent = context.getParent();
        System.out.println("Stop context: " + context.getPath() + " to Host: " + parent.getName());

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("JVMRoute", getJvmRoute(context));
        parameters.put("context", ("".equals(context.getPath())) ? "/" : context.getPath());

        // FIXME: send STOP-APP
    }

    
    protected String getJvmRoute(Context context) {
        return ((Engine) context.getParent().getParent()).getJvmRoute();
    }
    
    
    protected Connector findProxyConnector(Connector[] connectors) {
        int pos = 0;
        int maxThreads = 0;
        for (int i = 0; i < connectors.length; i++) {
            if (connectors[i].getProtocol().startsWith("AJP")) {
                // Return any AJP connector found
                return connectors[i];
            }
            if (Boolean.TRUE.equals(IntrospectionUtils.getProperty(connectors[i].getProtocolHandler(), "reverseConnection"))) {
                return connectors[i];
            }
            Integer mt = (Integer) IntrospectionUtils.getProperty(connectors[i].getProtocolHandler(), "maxThreads");
            if (mt.intValue() > maxThreads) {
                maxThreads = mt.intValue();
                pos = i;
            }
        }
        // If no AJP connector and no reverse, return the connector with the most threads
        return connectors[pos];
    }

    protected String getAddress(Connector connector) {
        InetAddress inetAddress = 
            (InetAddress) IntrospectionUtils.getProperty(connector.getProtocolHandler(), "address");
        if (inetAddress == null) {
            // FIXME: Return local address ? This is hard ...
            return null;
        } else {
            return inetAddress.toString();
        }
    }

}
