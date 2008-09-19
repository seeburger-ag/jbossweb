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
package org.apache.tomcat.bayeux;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.cometd.bayeux.Bayeux;
import org.apache.tomcat.util.json.JSONArray;
import org.apache.tomcat.util.json.JSONException;
import org.apache.tomcat.util.json.JSONObject;
import org.jboss.servlet.http.HttpEvent;
import org.jboss.servlet.http.HttpEventServlet;

/**
 * 
 * @author Filip Hanik
 * @author Guy Molinari
 * @version 1.0
 */
public class BayeuxServlet implements HttpEventServlet {


    /**
     * The debugging detail level for this servlet.
     */
    protected int debug = 0;


    /**
     * Attribute to hold the TomcatBayeux object in the servlet context
     */
    public static final String TOMCAT_BAYEUX_ATTR = Bayeux.DOJOX_COMETD_BAYEUX;
    
    /**
     * Servlet config - for future use
     */
    protected ServletConfig servletConfig;
    
    /**
     * Reference to the global TomcatBayeux object
     */
    protected TomcatBayeux tb;
    
    /**
     * Upon servlet destruction, the servlet will clean up the 
     * TomcatBayeux object and terminate any outstanding events.
     */
    public void destroy() {
        servletConfig = null;
        //to do, close all outstanding comet events
        //tb.destroy();
        tb = null;//TO DO, close everything down
        
    }
    
    /**
     * Returns the preconfigured connection timeout.
     * If no timeout has been configured as a servlet init parameter named <code>timeout</code>
     * then the default of 2min will be used.
     * @return int - the timeout for a connection in milliseconds
     */
    protected int getTimeout() {
        String timeoutS = servletConfig.getInitParameter("timeout");
        int timeout = 120*1000; //2 min
        try {
            timeout = Integer.parseInt(timeoutS);
        }catch (NumberFormatException nfe) {
            //ignore, we have a default value
        }
        return timeout;
    }
    
    protected int getReconnectInterval() {
        String rs = servletConfig.getInitParameter("reconnectInterval");
        int rct = 5000; //5 seconds
        try {
            rct = Integer.parseInt(rs);
        }catch (NumberFormatException nfe) {
            //ignore, we have a default value
        }
        return rct;
    }


    public void event(HttpEvent cometEvent) throws IOException, ServletException {
        HttpEvent.EventType type = cometEvent.getType();
        if (debug > 0) {
            getServletConfig().getServletContext().log("["+Thread.currentThread().getName()+"] Received Comet Event type="+type);
        }
        synchronized (cometEvent) {
            switch (type) {
            case BEGIN:
                cometEvent.setTimeout(getTimeout());
                break;
            case READ:
                checkBayeux(cometEvent);
                break;
            case EOF:
            case EVENT:
            case WRITE:
                break;
            case ERROR:
            case END:
            case TIMEOUT:
                tb.remove(cometEvent);
                cometEvent.close();
                break;
            }
        }//synchronized
    }//event

    /**
     * 
     * @param cometEvent CometEvent
     * @return boolean - true if we comet event stays open
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    protected void checkBayeux(HttpEvent cometEvent) throws IOException, UnsupportedOperationException {
        //we actually have data.
        //data can be text/json or 
        if (Bayeux.JSON_CONTENT_TYPE.equals(cometEvent.getHttpServletRequest().getContentType())) {
            //read and decode the bytes according to content length
            getServletConfig().getServletContext().log("["+Thread.currentThread().getName()+"] JSON encoding not supported, will throw an exception and abort the request.");
            int contentlength = cometEvent.getHttpServletRequest().getContentLength();
            throw new UnsupportedOperationException("Decoding "+Bayeux.JSON_CONTENT_TYPE+" not yet implemented.");
        } else { //GET method or application/x-www-form-urlencoded
            String message = cometEvent.getHttpServletRequest().getParameter(Bayeux.MESSAGE_PARAMETER);
            if (debug > 0) {
                getServletConfig().getServletContext().log("["+Thread.currentThread().getName()+"] Received JSON message:"+message);
            }
            try {
                int action = handleBayeux(message, cometEvent);
                if (debug > 0) {
                    getServletConfig().getServletContext().log("["+Thread.currentThread().getName()+"] Bayeux handling complete, action result="+action);
                }
                if (action<=0) {
                    cometEvent.close();
                }
            }catch (Exception x) {
                tb.remove(cometEvent);
                getServletConfig().getServletContext().log(x, "Exception in check");
                cometEvent.close();
            }
        }
    }
    
    protected int handleBayeux(String message, HttpEvent event) throws IOException, ServletException {
        int result = 0;
        if (message==null || message.length()==0) return result;
        try {
            BayeuxRequest request = null;
            //a message can be an array of messages
            JSONArray jsArray = new JSONArray(message);
            for (int i = 0; i < jsArray.length(); i++) {
                JSONObject msg = jsArray.getJSONObject(i);
                
                if (debug > 0) {
                    getServletConfig().getServletContext().log("["+Thread.currentThread().getName()+"] Processing bayeux message:"+msg);
                }
                request = RequestFactory.getRequest(tb,event,msg);
                if (debug > 0) {
                    getServletConfig().getServletContext().log("["+Thread.currentThread().getName()+"] Processing bayeux message using request:"+request);
                }
                result = request.process(result);
                if (debug > 0) {
                    getServletConfig().getServletContext().log("["+Thread.currentThread().getName()+"] Processing bayeux message result:"+result);
                }
            }
            if (result>0 && request!=null) {
                event.getHttpServletRequest().setAttribute(BayeuxRequest.LAST_REQ_ATTR, request);
                ClientImpl ci = (ClientImpl)tb.getClient(((RequestBase)request).getClientId());
                ci.addCometEvent(event);
                if (debug > 0) {
                    getServletConfig().getServletContext().log("["+Thread.currentThread().getName()+"] Done bayeux message added to request attribute");
                }
            } else if (result == 0 && request!=null) {
                RequestBase.deliver(event,(ClientImpl)tb.getClient(((RequestBase)request).getClientId()));
                if (debug > 0) {
                    getServletConfig().getServletContext().log("["+Thread.currentThread().getName()+"] Done bayeux message, delivered to client");
                }
            }
            
        }catch (JSONException x) {
            getServletConfig().getServletContext().log(x, "Error");//to do impl error handling
            result = -1;
        }catch (BayeuxException x) {
            getServletConfig().getServletContext().log(x, "Error"); //to do impl error handling
            result = -1;
        }
        return result;
    }

    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    public String getServletInfo() {
        return "Tomcat/BayeuxServlet/1.0";
    }

    public void init(ServletConfig servletConfig) throws ServletException {
        
        this.servletConfig = servletConfig;
        ServletContext ctx = servletConfig.getServletContext();
        if (ctx.getAttribute(TOMCAT_BAYEUX_ATTR)==null)
            ctx.setAttribute(TOMCAT_BAYEUX_ATTR,new TomcatBayeux());
        this.tb = (TomcatBayeux)ctx.getAttribute(TOMCAT_BAYEUX_ATTR);
        tb.setReconnectInterval(getReconnectInterval());
    }

    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        if (servletResponse instanceof HttpServletResponse) {
            ( (HttpServletResponse) servletResponse).sendError(500, "Misconfigured Tomcat server, must be configured to support Comet operations.");
        } else {
            throw new ServletException("Misconfigured Tomcat server, must be configured to support Comet operations for the Bayeux protocol.");
        }
    }
}
