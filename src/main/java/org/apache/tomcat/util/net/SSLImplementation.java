/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.tomcat.util.net;

import java.net.Socket;
import javax.net.ssl.SSLSession;

import org.apache.tomcat.util.net.jsse.NioJSSESocketChannelFactory;

/**
 * {@code SSLImplementation}
 * <p>
 * Abstract factory and base class for all SSL implementations.
 * </p>
 * 
 * 
 * Created on Feb 22, 2012 at 12:55:17 PM
 * 
 * @author EKR & <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
abstract public class SSLImplementation {
    private static org.jboss.logging.Logger logger = org.jboss.logging.Logger
            .getLogger(SSLImplementation.class);

    private static final String[] implementations = { "org.apache.tomcat.util.net.jsse.NioJSSEImplementation" };

    /**
     * @return the default implementation of {@code SSLImplementation}
     * @throws ClassNotFoundException
     */
    public static SSLImplementation getInstance() throws ClassNotFoundException {
        for (int i = 0; i < implementations.length; i++) {
            try {
                SSLImplementation impl = getInstance(implementations[i]);
                return impl;
            } catch (Exception e) {
                if (logger.isTraceEnabled())
                    logger.trace("Error creating " + implementations[i], e);
            }
        }

        // If we can't instantiate any of these
        throw new ClassNotFoundException("Can't find any SSL implementation");
    }

    /**
     * Returns the {@code SSLImplementation} specified by the name of it's class
     * 
     * @param className
     * @return a new instance of the {@code SSLImplementation} given by it's name
     * @throws ClassNotFoundException
     */
    public static SSLImplementation getInstance(String className) throws ClassNotFoundException {
        if (className == null)
            return getInstance();

        try {
            Class<?> clazz = Class.forName(className);
            return (SSLImplementation) clazz.newInstance();
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug("Error loading SSL Implementation " + className, e);
            throw new ClassNotFoundException("Error loading SSL Implementation " + className + " :"
                    + e.toString());
        }
    }

    abstract public String getImplementationName();
    abstract public SSLSupport getSSLSupport(SSLSession session);
}