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

import static org.jboss.web.CoyoteMessages.MESSAGES;

import javax.net.ssl.SSLSession;

import org.jboss.web.CoyoteLogger;

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

    private static final String[] implementations = { "org.apache.tomcat.util.net.jsse.JSSEImplementation" };

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
                if (CoyoteLogger.UTIL_LOGGER.isTraceEnabled())
                    CoyoteLogger.UTIL_LOGGER.trace("Error creating " + implementations[i], e);
            }
        }

        // If we can't instantiate any of these
        throw new ClassNotFoundException(MESSAGES.noSslImplementation());
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
            if (CoyoteLogger.UTIL_LOGGER.isDebugEnabled())
                CoyoteLogger.UTIL_LOGGER.debug("Error loading SSL Implementation " + className, e);
            throw new ClassNotFoundException(MESSAGES.errorLoadingSslImplementation(className), e);
        }
    }

    abstract public String getImplementationName();
    abstract public SSLSupport getSSLSupport(SSLSession session);
}