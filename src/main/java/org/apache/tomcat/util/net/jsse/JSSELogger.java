/*
 * Copyright (C) 2014 Red Hat, inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.apache.tomcat.util.net.jsse;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

import static org.jboss.logging.Logger.Level.DEBUG;
import static org.jboss.logging.Logger.Level.WARN;

/**
 * Logging IDs 9000-9500
 * @author <a href="mailto:ehugonne@redhat.com">Emmanuel Hugonnet</a> (c) 2014 Red Hat, inc.
 */
@MessageLogger(projectCode = "JBWEB")
public interface JSSELogger extends BasicLogger {
    /**
     * A logger with the category of the package name.
     */
    JSSELogger ROOT_LOGGER = Logger.getMessageLogger(JSSELogger.class, "org.apache.tomcat.util.net.jsse");
    
    
    @LogMessage(level = DEBUG)
    @Message(id = 9000, value = "List of enabled ciphers: %s")
    void logEnabledCiphers(final String ciphers);
    
    @LogMessage(level = DEBUG)
    @Message(id = 9001, value = "List of cipher suites that my be used: %s")
    void logUseableCiphers(final String ciphers);

    @LogMessage(level = WARN)
    @Message(id = 9002, value = "Unknown element: %s")
    void warnUnknowElement(final String alias);
}
