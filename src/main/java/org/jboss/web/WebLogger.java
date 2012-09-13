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
 * Logging IDs 7500-8000
 * @author remm
 */
@MessageLogger(projectCode = "JBWEB")
public interface WebLogger extends BasicLogger {

    /**
     * A logger with the category of the package name.
     */
    WebLogger ROOT_LOGGER = Logger.getMessageLogger(WebLogger.class, "org.jboss.web");

    @LogMessage(level = ERROR)
    @Message(id = 7500, value = "Error initializing PHP library with library path: %s")
    void errorInitializingPhpLibrary(String libraryPath, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 7501, value = "Error terminating PHP library")
    void errorTerminatingPhpLibrary(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 7502, value = "Invalid PHP library %s.%s.%s, required version is %s.%s.%s")
    void invalidPhpLibrary(int major, int minor, int patch, int requiredMajor, int requiredMinor, int requiredPatch);

}
