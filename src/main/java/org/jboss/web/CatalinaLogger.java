/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.web;

import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

/**
 * Logging IDs 1000-2000
 * @author remm
 */
@MessageLogger(projectCode = "JBWEB")
public interface CatalinaLogger extends BasicLogger {

    /**
     * A logger with the category of the package name.
     */
    CatalinaLogger ROOT_LOGGER = Logger.getMessageLogger(CatalinaLogger.class, "org.apache.catalina");

    /**
     * A logger with the category of the package name.
     */
    CatalinaLogger AUTH_LOGGER = Logger.getMessageLogger(CatalinaLogger.class, "org.apache.catalina.authenticator");

    /**
     * A logger with the category of the package name.
     */
    CatalinaLogger VALVES_LOGGER = Logger.getMessageLogger(CatalinaLogger.class, "org.apache.catalina.valves");

    /**
     * A logger with the category of the package name.
     */
    CatalinaLogger REALM_LOGGER = Logger.getMessageLogger(CatalinaLogger.class, "org.apache.catalina.realm");

    @LogMessage(level = WARN)
    @Message(id = 1000, value = "A valid entry has been removed from client nonce cache to make room for new entries. A replay attack is now possible. To prevent the possibility of replay attacks, reduce nonceValidity or increase cnonceCacheSize. Further warnings of this type will be suppressed for 5 minutes.")
    void digestCacheRemove();

    @LogMessage(level = WARN)
    @Message(id = 1001, value = "Failed to process certificate string [%s] to create a java.security.cert.X509Certificate object")
    void certificateProcessingFailed(String certificate, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1002, value = "The SSL provider specified on the connector associated with this request of [%s] is invalid. The certificate data could not be processed.")
    void missingSecurityProvider(String provider, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1003, value = "Error digesting user credentials.")
    void errorDigestingCredentials(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1004, value = "Failed realm [%s] JMX registration.")
    void failedRealmJmxRegistration(Object objectName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1005, value = "Failed realm [%s] JMX unregistration.")
    void failedRealmJmxUnregistration(Object objectName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1006, value = "Missing parent [%s].")
    void missingParentJmxRegistration(Object objectName, @Cause Throwable t);

}
