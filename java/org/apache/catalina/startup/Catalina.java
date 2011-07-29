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


package org.apache.catalina.startup;


import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;


/**
 * Wrapper impl of Embedded.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision$ $Date$
 */

public class Catalina extends Embedded {


    // ----------------------------------------------------- Instance Variables


    /**
     * The server component we are starting or stopping
     */
    protected Server server = null;


    /**
     * Are we starting a new server?
     */
    protected boolean starting = false;


    /**
     * Are we stopping an existing server?
     */
    protected boolean stopping = false;


    // ------------------------------------------------------------- Properties


    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void create() {
    }

    public void destroy() {
    }

    /**
     * Start a new server instance.
     */
    public void start() {
        // Start the new server
        if (server instanceof Lifecycle) {
            try {
                ((Lifecycle) server).start();
            } catch (LifecycleException e) {
                log.error("Catalina.start: ", e);
            }
        }
    }


    /**
     * Stop an existing server instance.
     */
    public void stop() {

        // Shut down the server
        if (server instanceof Lifecycle) {
            try {
                ((Lifecycle) server).stop();
            } catch (LifecycleException e) {
                log.error("Catalina.stop", e);
            }
        }

    }


    private static org.jboss.logging.Logger log=
        org.jboss.logging.Logger.getLogger( Catalina.class );

}
