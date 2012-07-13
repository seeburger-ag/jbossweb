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
 * Logging IDs 3000-4000
 * @author remm
 */
@MessageLogger(projectCode = "JBWEB")
public interface CoyoteLogger extends BasicLogger {

    /**
     * A logger with the category of the package name.
     */
    CoyoteLogger ROOT_LOGGER = Logger.getMessageLogger(CoyoteLogger.class, "org.apache.coyote");

    @LogMessage(level = INFO)
    @Message(id = 3000, value = "Coyote HTTP/1.1 starting on: %s")
    void startConnector(String name);

    @LogMessage(level = INFO)
    @Message(id = 3001, value = "Coyote HTTP/1.1 initializing on : %s")
    void initConnector(String name);

}
