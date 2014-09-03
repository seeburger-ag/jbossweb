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

import static org.jboss.logging.Logger.Level.DEBUG;
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
 * Logging IDs 8800-9000
 * @author Remy Maucherat
 */
@MessageLogger(projectCode = "JBWEB")
public interface WebsocketsLogger extends BasicLogger {

    /**
     * A logger with the category of the package name.
     */
    WebsocketsLogger ROOT_LOGGER = Logger.getMessageLogger(WebsocketsLogger.class, "org.apache.tomcat.websocket");

    @LogMessage(level = INFO)
    @Message(id = 8800, value = "Failed to close channel cleanly")
    void errorClose();

    @LogMessage(level = ERROR)
    @Message(id = 8801, value = "A background process failed")
    void backgroundProcessFailed(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 8802, value = "Flushing batched messages before closing the session failed")
    void flushOnCloseFailed(@Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 8803, value = "Failed to send close message to remote endpoint")
    void closeMessageFail(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 8804, value = "Unable to parse HTTP header as no colon is present to delimit header name and header value in [%s]. The header has been skipped.")
    void invalidHttpHeader(String header);

    @LogMessage(level = DEBUG)
    @Message(id = 8805, value = "Session with ID [%s] did not close cleanly")
    void sessionCloseFailed(String id, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 8806, value = "Failed to call onOpen method of POJO end point for POJO of type [%s]")
    void onOpenFailed(String className, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 8807, value = "Failed to close WebSocket session during error handling")
    void closeSessionFailed(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 8808, value = "Failed to call onClose method of POJO end point for POJO of type [%s]")
    void onCloseFailed(String className, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 8809, value = "No error handling configured for [%s] and the following error occurred")
    void noOnError(String className, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 8810, value = "Failed to call onError method of POJO end point for POJO of type [%s]")
    void onErrorFailed(String className, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 8811, value = "Failed to close WebConnection while destroying the WebSocket HttpUpgradeHandler")
    void destroyFailed(@Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 8812, value = "Failed to close the ServletOutputStream connection cleanly")
    void closeFailed(@Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 8813, value = "WebSocket support is not available when running on Java 6")
    void noWebsocketsSupport();

    @LogMessage(level = WARN)
    @Message(id = 8814, value = "Thread group %s not destroyed, %s threads left")
    void threadGroupNotDestryed(String name, int threadCount);

}
