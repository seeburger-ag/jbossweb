/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @author Remy Maucherat
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
