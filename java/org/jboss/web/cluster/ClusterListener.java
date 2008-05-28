/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and individual contributors as indicated
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


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

import javax.management.ObjectName;

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
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.util.StringManager;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.UEncoder;
import org.apache.tomcat.util.modeler.Registry;
import org.jboss.logging.Logger;



/**
 * This listener communicates with a front end mod_cluster enabled proxy to
 * automatically maintain the node configuration according to what is 
 * deployed.
 */
public class ClusterListener
    implements LifecycleListener, ContainerListener {

    protected static Logger log = Logger.getLogger(ClusterListener.class);

    /**
     * The string manager for this package.
     */
    protected StringManager sm =
        StringManager.getManager(Constants.Package);


    // -------------------------------------------------------------- Constants

    
    protected enum State { OK, ERROR, DOWN };

    
    // ----------------------------------------------------------------- Fields


    /**
     * URL encoder used to generate requests bodies.
     */
    protected UEncoder encoder = new UEncoder();
    
    
    /**
     * State of the node. If a communication error occurs with the
     * frontend proxy, the configuration will be refreshed.
     */
    protected State state = State.OK;
    

    /**
     * JMX registration information.
     */
    protected ObjectName oname;
    
    
    // ------------------------------------------------------------- Properties


    /**
     * Port on which the proxy is listening for balancer control commands.
     */
    protected int proxyPort = 8000;
    public int getProxyPort() { return proxyPort; }
    public void setProxyPort(int proxyPort) { this.proxyPort = proxyPort; }


    /**
     * Address on which the proxy is listening for balancer control commands.
     * Default is localhost.
     */
    protected InetAddress proxyAddress = null;
    public InetAddress getProxyAddress() { return proxyAddress; }
    public void setAddress(InetAddress proxyAddress) { this.proxyAddress = proxyAddress; }

    
    /**
     * Most likely only useful for testing.
     */
    protected String proxyURL = "/";
    public String getProxyURL() { return proxyURL; }
    public void setProxyURL(String proxyURL) { this.proxyURL = proxyURL; }


    /**
     * Connection timeout for communication with the proxy.
     */
    protected int socketTimeout = 5000;
    public int getSocketTimeout() { return socketTimeout; }
    public void setSocketTimeout(int socketTimeout) { this.socketTimeout = socketTimeout; }


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
            } else {
                return;
            }
        } else if (Lifecycle.AFTER_START_EVENT.equals(event.getType())) {
            if (source instanceof Server) {
                startServer((Server) source);
            } else {
                return;
            }
        } else if (Lifecycle.STOP_EVENT.equals(event.getType())) {
            if (source instanceof Context) {
                // Stop a webapp
                stopContext((Context) source);
            } else if (source instanceof Server) {
                stopServer((Server) source);
            } else {
                return;
            }
        } else if (Lifecycle.PERIODIC_EVENT.equals(event.getType())) {
            if (source instanceof Engine) {
                status((Engine) source);
            }
        }

    }
    
    
    /**
     * Retrieves the full proxy configuration. To be used through JMX or similar.
     * 
     *         response: HTTP/1.1 200 OK
     *   response:
     *   node: [1:1] JVMRoute: node1 Domain: [bla] Host: 127.0.0.1 Port: 8009 Type: ajp
     *   host: 1 [] vhost: 1 node: 1
     *   context: 1 [/] vhost: 1 node: 1 status: 1
     *   context: 2 [/myapp] vhost: 1 node: 1 status: 1
     *   context: 3 [/host-manager] vhost: 1 node: 1 status: 1
     *   context: 4 [/docs] vhost: 1 node: 1 status: 1
     *   context: 5 [/manager] vhost: 1 node: 1 status: 1
     *
     * @return the proxy confguration
     */
    public String getProxyConfiguration() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        // Send DUMP * request
        return sendRequest("DUMP", true, parameters);
    }
    
    
    /**
     * Reset a DOWN connection to the proxy up to ERROR, where the configuration will
     * be refreshed. To be used through JMX or similar.
     */
    public void reset() {
        if (state == State.DOWN) {
            state = State.ERROR;
        }
    }
    
    
    /**
     * Refresh configuration. To be used through JMX or similar.
     */
    public void refresh() {
        // Set as error, and the periodic event will refresh the configuration
        if (state == State.OK) {
            state = State.ERROR;
        }
    }
    
    
    /**
     * Disable all webapps for all engines. To be used through JMX or similar.
     */
    public boolean disable() {
    	Service[] services = ServerFactory.getServer().findServices();
        for (int i = 0; i < services.length; i++) {
            Engine engine = (Engine) services[i].getContainer();
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("JVMRoute", engine.getJvmRoute());
            // Send DISABLE-APP * request
            sendRequest("DISABLE-APP", true, parameters);
        }
        return (state == State.OK);
    }
    
    
    /**
     * Enable all webapps for all engines. To be used through JMX or similar.
     */
    public boolean enable() {
    	Service[] services = ServerFactory.getServer().findServices();
        for (int i = 0; i < services.length; i++) {
            Engine engine = (Engine) services[i].getContainer();
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("JVMRoute", engine.getJvmRoute());
            // Send ENABLE-APP * request
            sendRequest("ENABLE-APP", true, parameters);
        }
        return (state == State.OK);
    }
    
    
    /**
     * Send commands to the front end server assocaited with the startup of the
     * node.
     */
    protected void startServer(Server server) {

        // JMX registration
        if (oname==null) {
            try {
                oname = new ObjectName(((StandardServer) server).getDomain() + ":type=ClusterListener");
                Registry.getRegistry(null, null).registerComponent(this, oname, null);
            } catch (Exception e) {
                log.error(sm.getString("clusterListener.error.jmxRegister"), e);
            }
        }
                
        Service[] services = server.findServices();
        for (int i = 0; i < services.length; i++) {
            services[i].getContainer().addContainerListener(this);

            Engine engine = (Engine) services[i].getContainer();
            ((Lifecycle) engine).addLifecycleListener(this);
            Connector connector = findProxyConnector(engine.getService().findConnectors());
            InetAddress localAddress = 
                (InetAddress) IntrospectionUtils.getProperty(connector.getProtocolHandler(), "address");
            if (engine.getJvmRoute() == null || localAddress == null) {
                // Automagical JVM route (address + port + engineName)
                try {
                    if (localAddress == null) {
                        Socket connection = getConnection();
                        localAddress = connection.getLocalAddress();
                        if (localAddress != null) {
                            IntrospectionUtils.setProperty(connector.getProtocolHandler(), "address", localAddress.getHostAddress());
                        } else {
                            // Should not happen
                            IntrospectionUtils.setProperty(connector.getProtocolHandler(), "address", "127.0.0.1");
                        }
                        connection.close();
                        log.info(sm.getString("clusterListener.address", localAddress.getHostAddress()));
                    }
                    if (engine.getJvmRoute() == null) {
                        String hostName = null;
                        if (localAddress != null) {
                            hostName = localAddress.getHostName();
                        } else {
                            // Fallback
                            hostName = "127.0.0.1";
                        }
                        String jvmRoute = hostName + ":" + connector.getPort() + ":" + engine.getName();
                        engine.setJvmRoute(jvmRoute);
                        log.info(sm.getString("clusterListener.jvmRoute", engine.getName(), jvmRoute));
                    }
                } catch (Exception e) {
                    state = State.ERROR;
                    log.info(sm.getString("clusterListener.error.addressJvmRoute"), e);
                    return;
                }
            }
            
            config(engine);
            Container[] children = engine.findChildren();
            for (int j = 0; j < children.length; j++) {
                children[j].addContainerListener(this);
                Container[] children2 = children[j].findChildren();
                for (int k = 0; k < children2.length; k++) {
                    addContext((Context) children2[k]);
                }
            }
        }
    }
    
    
    /**
     * Send commands to the front end server associated with the shutdown of the
     * node.
     */
    protected void stopServer(Server server) {
        
        // JMX unregistration
        if (oname==null) {
            try {
                Registry.getRegistry(null, null).unregisterComponent(oname);
            } catch (Exception e) {
                log.error(sm.getString("clusterListener.error.jmxUnregister"), e);
            }
        }

        Service[] services = server.findServices();
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
    }
    

    /**
     * Send the configuration for the specified engine to the proxy. 
     * 
     * @param engine
     */
    protected void config(Engine engine) {
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("clusterListener.config", engine.getName()));
        }
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
        sendRequest("CONFIG", false, parameters);
    }


    /**
     * Remove all contexts from the specified engine.
     * 
     * @param engine
     */
    protected void removeAll(Engine engine) {
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("clusterListener.stop", engine.getName()));
        }

        // JVMRoute can be null here if nothing was ever initialized
        if (engine.getJvmRoute() != null) {
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("JVMRoute", engine.getJvmRoute());

            // Send REMOVE-APP * request
            sendRequest("REMOVE-APP", true, parameters);
        }
    }


    /**
     * Send a periodic status request. If in error state, the listener will attempt to refresh
     * the configuration on the front end server.
     * 
     * @param engine
     */
    protected void status(Engine engine) {
        if (state == State.ERROR) {
            state = State.OK;
            // Something went wrong in a status at some point, so fully restore the configuration
            stopServer(ServerFactory.getServer());
            startServer(ServerFactory.getServer());
        } else if (state == State.OK) {
            if (log.isDebugEnabled()) {
                log.debug(sm.getString("clusterListener.status", engine.getName()));
            }

            Connector connector = findProxyConnector(engine.getService().findConnectors());
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("JVMRoute", engine.getJvmRoute());
            parameters.put("Load", "1");

            // Send STATUS request
            sendRequest("STATUS", false, parameters);
        }
    }

    
    /**
     * Add a new context.
     * 
     * @param context
     */
    protected void addContext(Context context) {
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("clusterListener.context.enable", context.getPath(), context.getParent().getName(), ((StandardContext) context).getState()));
        }

        ((Lifecycle) context).addLifecycleListener(this);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("JVMRoute", getJvmRoute(context));
        parameters.put("Context", ("".equals(context.getPath())) ? "/" : context.getPath());
        parameters.put("Alias", getHost(context));

        // Send ENABLE-APP if state is started
        if (context.isStarted()) {
            sendRequest("ENABLE-APP", false, parameters);
        }
    }


    /**
     * Remove a context.
     * 
     * @param context
     */
    protected void removeContext(Context context) {
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("clusterListener.context.disable", context.getPath(), context.getParent().getName(), ((StandardContext) context).getState()));
        }

        ((Lifecycle) context).removeLifecycleListener(this);

        HashMap<String, String> parameters = new HashMap<String, String>();
        String jvmRoute = getJvmRoute(context);
        // JVMRoute can be null here if nothing was ever initialized
        if (jvmRoute != null) {
            parameters.put("JVMRoute", jvmRoute);
            parameters.put("Context", ("".equals(context.getPath())) ? "/" : context.getPath());
            parameters.put("Alias", getHost(context));

            // Send REMOVE-APP
            sendRequest("REMOVE-APP", false, parameters);
        }
    }


    /**
     * Start a context.
     * 
     * @param context
     */
    protected void startContext(Context context) {
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("clusterListener.context.start", context.getPath(), context.getParent().getName()));
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("JVMRoute", getJvmRoute(context));
        parameters.put("Context", ("".equals(context.getPath())) ? "/" : context.getPath());
        parameters.put("Alias", getHost(context));

        // Send ENABLE-APP
        sendRequest("ENABLE-APP", false, parameters);
    }


    /**
     * Stop a context.
     * 
     * @param context
     */
    protected void stopContext(Context context) {
        if (log.isDebugEnabled()) {
            log.debug(sm.getString("clusterListener.context.stop", context.getPath(), context.getParent().getName()));
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("JVMRoute", getJvmRoute(context));
        parameters.put("Context", ("".equals(context.getPath())) ? "/" : context.getPath());
        parameters.put("Alias", getHost(context));

        // Send STOP-APP
        sendRequest("STOP-APP", false, parameters);
    }

    
    /**
     * Return the JvmRoute for the specified context.
     * 
     * @param context
     * @return
     */
    protected String getJvmRoute(Context context) {
        return ((Engine) context.getParent().getParent()).getJvmRoute();
    }
    

    /**
     * Return the host and its alias list with which the context is associated.
     * 
     * @param context
     * @return
     */
    protected String getHost(Context context) {
        StringBuffer result = new StringBuffer();
        Host host = (Host) context.getParent();
        result.append(host.getName());
        String[] aliases = host.findAliases();
        for (int i = 0; i < aliases.length; i++) {
            result.append(',');
            result.append(aliases[i]);
        }
        return result.toString();
    }
    
    
    /**
     * Find the most likely connector the proxy server should connect to, or
     * accept connections from.
     * 
     * @param connectors
     * @return
     */
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

    
    /**
     * Return the address on which the connector is bound.
     * 
     * @param connector
     * @return
     */
    protected String getAddress(Connector connector) {
        InetAddress inetAddress = 
            (InetAddress) IntrospectionUtils.getProperty(connector.getProtocolHandler(), "address");
        if (inetAddress == null) {
            // Should not happen
            return "127.0.0.1";
        } else {
            return inetAddress.getHostAddress();
        }
    }
    
    
    /**
     * Send HTTP request, with the specified list of parameters. If an IO error occurs, the error state will
     * be set. If the front end server reports an error, will mark as error state. Other unexpected exceptions 
     * will be thrown and the error state will be set.
     * 
     * @param command
     * @param wildcard
     * @param parameters
     * @return the response body as a String; null if in error state or a normal error occurs
     */
    protected String sendRequest(String command, boolean wildcard, HashMap<String, String> parameters) {

        // If there was an error, do nothing until the next periodic event, where the whole configuration
        // will be refreshed
        if (state != State.OK) {
            return null;
        }
        
        BufferedReader reader = null;
        BufferedWriter writer = null;
        CharChunk keyCC = null;
        Socket connection = null;
        try {
            // First, encode the POST body
            Iterator<String> keys = parameters.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = parameters.get(key);
                if (value == null) {
                    state = State.DOWN;
                    throw new IllegalStateException(sm.getString("clusterListener.error.nullAttribute", key));
                }
                keyCC = encoder.encodeURL(key, 0, key.length());
                keyCC.append('=');
                if (value != null) {
                    keyCC = encoder.encodeURL(value, 0, value.length());
                }
                if (keys.hasNext()) {
                    keyCC.append('&');
                }
            }
            
            // Then, connect to the proxy
            connection = getConnection();
            connection.setSoTimeout(socketTimeout);
            
            String requestLine = command + " " + ((wildcard) ? "*" : proxyURL) + " HTTP/1.0";
            int contentLength = keyCC.getLength();
            writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            writer.write(requestLine);
            writer.write("\r\n");
            writer.write("Content-Length: " + contentLength + "\r\n");
            writer.write("User-Agent: ClusterListener/1.0\r\n");
            writer.write("\r\n");
            writer.write(keyCC.getBuffer(), keyCC.getStart(), keyCC.getLength());
            writer.write("\r\n");
            writer.flush();
            
            // Read the response to a string
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            // Read the first response line and skip the rest of the HTTP header
            String responseStatus = reader.readLine();
            // Parse the line, which is formed like HTTP/1.x YYY Message
            int status = 500;
            String version = "0";
            String message = null;
            String errorType = null;
            try {
                responseStatus = responseStatus.substring(responseStatus.indexOf(' ') + 1, responseStatus.indexOf(' ', responseStatus.indexOf(' ') + 1));
                status = Integer.parseInt(responseStatus);
                String header = reader.readLine();
                while (!"".equals(header)) {
                    int colon = header.indexOf(':');
                    String headerName = header.substring(0, colon).trim();
                    String headerValue = header.substring(colon + 1).trim();
                    if ("version".equalsIgnoreCase(headerName)) {
                        version = headerValue;
                    } else if ("type".equalsIgnoreCase(headerName)) {
                        errorType = headerValue;
                    } else if ("mess".equalsIgnoreCase(headerName)) {
                        message = headerValue;
                    }
                    header = reader.readLine();
                }
            } catch (Exception e) {
                log.info(sm.getString("clusterListener.error.parse", command), e);
            }
            
            // Mark as error if the front end server did not return 200; the configuration will
            // be refreshed during the next periodic event 
            if (status == 200) {
                // Read the request body
                StringBuffer result = new StringBuffer();
                char[] buf = new char[512];
                while (true) {
                    int n = reader.read(buf);
                    if (n <= 0) {
                        break;
                    } else {
                        result.append(buf, 0, n);
                    }
                }
                return result.toString();
            } else {
                if ("SYNTAX".equals(errorType)) {
                    // Syntax error means the protocol is incorrect, which cannot be automatically fixed
                    state = State.DOWN;
                    log.error(sm.getString("clusterListener.error.syntax", command, version, errorType, message));
                } else {
                    state = State.ERROR;
                    log.error(sm.getString("clusterListener.error.other", command, version, errorType, message));
                }
            }

        } catch (IOException e) {
            // Most likely this is a connection error with the proxy
            state = State.ERROR;
            log.info(sm.getString("clusterListener.error.io", command), e);
        } finally {
            if (keyCC != null) {
                keyCC.recycle();
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
        
        return null;
        
    }

    
    /**
     * Return a connection to the proxy.
     */
    protected Socket getConnection()
        throws IOException {
        if (proxyPort == -1) {
            // FIXME: Determine connection port and address automagically
        }
        if (proxyAddress == null) {
            return new Socket(InetAddress.getLocalHost(), proxyPort);
        } else {
            return new Socket(proxyAddress, proxyPort);
        }
    }
    
    
}
