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


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

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

    
    protected enum State { OK, ERROR };

    
    // ---------------------------------------------- Properties


    protected int proxyPort = 8000;
    public int getProxyPort() { return proxyPort; }
    public void setProxyPort(int proxyPort) { this.proxyPort = proxyPort; }


    protected InetAddress proxyAddress = null;
    public InetAddress getProxyAddress() { return proxyAddress; }
    public void setAddress(InetAddress proxyAddress) { this.proxyAddress = proxyAddress; }

    
    protected String proxyURL = "/";
    public String getProxyURL() { return proxyURL; }
    public void setProxyURL(String proxyURL) { this.proxyURL = proxyURL; }


    protected int socketTimeout = 5000;
    public int getSocketTimeout() { return socketTimeout; }
    public void setSocketTimeout(int socketTimeout) { this.socketTimeout = socketTimeout; }


    protected UEncoder encoder = new UEncoder();
    protected State state = State.OK;
    

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
    
    
    public String getProxyConfiguration() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        // Send DUMP * request
        return sendRequest("DUMP", true, parameters);
    }
    
    
    protected void startServer(Server server) {
        Service[] services = server.findServices();
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
    }
    
    
    protected void stopServer(Server server) {
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
    
    
    protected void config(Engine engine) {
        System.out.println("Config: " + engine.getName());
        // Collect configuration from the connectors and service and call CONFIG
        Connector connector = findProxyConnector(engine.getService().findConnectors());
        HashMap<String, String> parameters = new HashMap<String, String>();
        if (engine.getJvmRoute() == null) {
            // FIXME: automagical JVM route (some hash of address + port + engineName ?)
            throw new IllegalStateException("JVMRoute must be set");
        } else {
            parameters.put("JVMRoute", engine.getJvmRoute());
        }
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


    protected void removeAll(Engine engine) {
        System.out.println("Stop: " + engine.getName());

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("JVMRoute", engine.getJvmRoute());

        // Send REMOVE-APP * request
        sendRequest("REMOVE-APP", true, parameters);
    }


    protected void status(Engine engine) {
        if (state != State.OK) {
            state = State.OK;
            // Something went wrong in a status at some point, so fully restore the configuration
            stopServer(ServerFactory.getServer());
            startServer(ServerFactory.getServer());
        } else {
            System.out.println("Status: " + engine.getName());

            Connector connector = findProxyConnector(engine.getService().findConnectors());
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("JVMRoute", engine.getJvmRoute());
            parameters.put("Load", "1");

            // Send STATUS request
            sendRequest("STATUS", false, parameters);
        }
    }

    
    protected void addContext(Context context) {
        System.out.println("Deploy context: " + context.getPath() + " to Host: " + context.getParent().getName() + " State: " + ((StandardContext) context).getState());
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


    protected void removeContext(Context context) {
        System.out.println("Undeploy context: " + context.getPath() + " to Host: " + context.getParent().getName() + " State: " + ((StandardContext) context).getState());
        ((Lifecycle) context).removeLifecycleListener(this);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("JVMRoute", getJvmRoute(context));
        parameters.put("Context", ("".equals(context.getPath())) ? "/" : context.getPath());
        parameters.put("Alias", getHost(context));

        // Send REMOVE-APP
        sendRequest("REMOVE-APP", false, parameters);
    }


    protected void startContext(Context context) {
        Container parent = context.getParent();
        System.out.println("Start context: " + context.getPath() + " to Host: " + parent.getName());

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("JVMRoute", getJvmRoute(context));
        parameters.put("Context", ("".equals(context.getPath())) ? "/" : context.getPath());
        parameters.put("Alias", getHost(context));

        // Send ENABLE-APP
        sendRequest("ENABLE-APP", false, parameters);
    }


    protected void stopContext(Context context) {
        Container parent = context.getParent();
        System.out.println("Stop context: " + context.getPath() + " to Host: " + parent.getName());

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("JVMRoute", getJvmRoute(context));
        parameters.put("Context", ("".equals(context.getPath())) ? "/" : context.getPath());
        parameters.put("Alias", getHost(context));

        // Send STOP-APP
        sendRequest("STOP-APP", false, parameters);
    }

    
    protected String getJvmRoute(Context context) {
        return ((Engine) context.getParent().getParent()).getJvmRoute();
    }
    
    
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
            return "127.0.0.1";
        } else {
            return inetAddress.toString();
        }
    }
    
    protected String sendRequest(String command, boolean wildcard, HashMap<String, String> parameters) {
        Reader reader = null;
        Writer writer = null;
        CharChunk keyCC = null;
        Socket connection = null;
        boolean ok = false;
        try {
            // First, encode the POST body
            Iterator<String> keys = parameters.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = parameters.get(key);
                if (value == null) {
                    throw new IllegalStateException("Value for attribute " + key + " cannot be null");
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
            if (proxyAddress == null) {
                connection = new Socket("127.0.0.1", proxyPort);
            } else {
                connection = new Socket(proxyAddress, proxyPort);
            }
            connection.setSoTimeout(socketTimeout);
            
            String requestLine = command + " " + ((wildcard) ? "*" : proxyURL) + " HTTP/1.0";
            int contentLength = keyCC.getLength();
            /*
            URL url = new URL("http", (proxyAddress == null) ? "127.0.0.1" : proxyAddress.toString(), proxyPort, (wildcard) ? "*" : proxyURL);
            System.out.println(url.toString() + " Request body: " + new String(keyCC.getBuffer(), keyCC.getStart(), keyCC.getLength()));
             connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(command);
            //connection.addRequestProperty("Content-type", "application/x-www-form-urlencoded");
            connection.setFixedLengthStreamingMode(keyCC.getLength());
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.connect();*/
            
            /*
response: HTTP/1.1 200 OK
response:
node: [1:1] JVMRoute: node1 Domain: [øcx97;x97;¿À°] Host: 127.0.0.1 Port: 8009 Type: ajp
host: 1 [] vhost: 1 node: 1
context: 1 [/] vhost: 1 node: 1 status: 1
context: 2 [/myapp] vhost: 1 node: 1 status: 1
context: 3 [/host-manager] vhost: 1 node: 1 status: 1
context: 4 [/docs] vhost: 1 node: 1 status: 1
context: 5 [/manager] vhost: 1 node: 1 status: 1
             */

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
            //System.out.println("Response body: " + result.toString());
            // FIXME: probably parse away the request header
            // FIXME: generate an IOE or similar if not 200, or simply mark as error ?
            ok = true;
            return result.toString();
            
        } catch (IOException e) {
            log.error("Error sending: " + command, e);
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
            if (!ok) {
                state = State.ERROR;
            }
        }
        return null;
    }

    
}
