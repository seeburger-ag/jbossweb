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
import static org.jboss.logging.Logger.Level.DEBUG;

import java.net.InetAddress;

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

    /**
     * A logger with the category of the package name.
     */
    CoyoteLogger UTIL_LOGGER = Logger.getMessageLogger(CoyoteLogger.class, "org.apache.tomcat.util");

    /**
     * A logger with the category of the package name.
     */
    CoyoteLogger HTTP_LOGGER = Logger.getMessageLogger(CoyoteLogger.class, "org.apache.coyote.http11");

    /**
     * A logger with the category of the package name.
     */
    CoyoteLogger AJP_LOGGER = Logger.getMessageLogger(CoyoteLogger.class, "org.apache.coyote.ajp");

    @LogMessage(level = INFO)
    @Message(id = 3000, value = "Coyote HTTP/1.1 starting on: %s")
    void startHttpConnector(String name);

    @LogMessage(level = INFO)
    @Message(id = 3001, value = "Coyote HTTP/1.1 initializing on : %s")
    void initHttpConnector(String name);

    @LogMessage(level = ERROR)
    @Message(id = 3002, value = "Failed to load keystore type %s with path %s due to %s")
    void errorLoadingKeystore(String type, String path, String message);

    @LogMessage(level = ERROR)
    @Message(id = 3003, value = "Failed to load keystore type %s with path %s due to %s")
    void errorLoadingKeystoreWithException(String type, String path, String message, @Cause Throwable exception);

    @LogMessage(level = WARN)
    @Message(id = 3004, value = "Secure renegotiation is not supported by the SSL library %s")
    void noInsecureRengotiation(String version);

    @LogMessage(level = DEBUG)
    @Message(id = 3005, value = "Handshake failed: %s")
    void handshakeFailed(String cause);

    @LogMessage(level = DEBUG)
    @Message(id = 3006, value = "Handshake failed")
    void handshakeFailed(@Cause Throwable exception);

    @LogMessage(level = DEBUG)
    @Message(id = 3007, value = "Unexpected error processing socket")
    void unexpectedError(@Cause Throwable exception);

    @LogMessage(level = INFO)
    @Message(id = 3008, value = "Maximum number of threads (%s) created for connector with address %s and port %s")
    void maxThreadsReached(int maxThreads, InetAddress address, int port);

    @LogMessage(level = INFO)
    @Message(id = 3009, value = "Failed to create poller with specified size of %s")
    void limitedPollerSize(int size);

    @LogMessage(level = ERROR)
    @Message(id = 3010, value = "Poller creation failed")
    void errorCreatingPoller(@Cause Throwable exception);

    @LogMessage(level = ERROR)
    @Message(id = 3011, value = "Error allocating socket processor")
    void errorProcessingSocket(@Cause Throwable exception);

    @LogMessage(level = ERROR)
    @Message(id = 3012, value = "Socket accept failed")
    void errorAcceptingSocket(@Cause Throwable exception);

    @LogMessage(level = ERROR)
    @Message(id = 3013, value = "Error processing timeouts")
    void errorProcessingSocketTimeout(@Cause Throwable exception);

    @LogMessage(level = ERROR)
    @Message(id = 3014, value = "Unexpected poller error")
    void errorPollingSocket();

    @LogMessage(level = WARN)
    @Message(id = 3015, value = "Unfiltered poll flag %s, sending error")
    void errorPollingSocketCode(long code);

    @LogMessage(level = ERROR)
    @Message(id = 3016, value = "Critical poller failure (restarting poller): [%s] %s")
    void pollerFailure(int code, String message);

    @LogMessage(level = ERROR)
    @Message(id = 3017, value = "Unexpected poller error")
    void errorPollingSocketWithException(@Cause Throwable exception);

    @LogMessage(level = ERROR)
    @Message(id = 3018, value = "Unexpected sendfile error")
    void errorSendingFile(@Cause Throwable exception);

    @LogMessage(level = WARN)
    @Message(id = 3019, value = "Sendfile failure: [%s] %s")
    void errorSendingFile(int code, String message);

    @LogMessage(level = ERROR)
    @Message(id = 3020, value = "Error closing clannel")
    void errorClosingChannel(@Cause Throwable exception);

    @LogMessage(level = DEBUG)
    @Message(id = 3021, value = "Error closing socket")
    void errorClosingSocket(@Cause Throwable exception);

    @LogMessage(level = INFO)
    @Message(id = 3022, value = "Channel processing failed")
    void errorProcessingChannel();

    @LogMessage(level = DEBUG)
    @Message(id = 3023, value = "Channel processing failed")
    void errorProcessingChannelDebug(@Cause Throwable exception);

    @LogMessage(level = ERROR)
    @Message(id = 3024, value = "Channel processing failed")
    void errorProcessingChannelWithException(@Cause Throwable exception);

    @LogMessage(level = DEBUG)
    @Message(id = 3025, value = "Error awaiting read")
    void errorAwaitingRead(@Cause Throwable exception);

    @LogMessage(level = DEBUG)
    @Message(id = 3026, value = "Unknown event")
    void unknownEvent();

    @LogMessage(level = WARN)
    @Message(id = 3027, value = "Failed loading HTTP messages strings")
    void errorLoadingMessages(@Cause Throwable exception);

    @LogMessage(level = DEBUG)
    @Message(id = 3028, value = "Start processing with input [%s]")
    void startProcessingParameter(String parameter);

    @LogMessage(level = DEBUG)
    @Message(id = 3029, value = "Parameter starting at position [%s] and ending at position [%s] with a value of [%s] was not followed by an '=' character")
    void parameterMissingEqual(int start, int end, String value);

    @LogMessage(level = DEBUG)
    @Message(id = 3030, value = "Empty parameter chunk ignored")
    void emptyParamterChunk();

    @LogMessage(level = DEBUG)
    @Message(id = 3031, value = "Invalid chunk starting at byte [%s] and ending at byte [%s] with a value of [%s] ignored")
    void parameterInvalid(int start, int end, String value);

    @LogMessage(level = ERROR)
    @Message(id = 3032, value = "Failed to create copy of original parameter values for debug logging purposes")
    void parametersCopyFailed();

    @LogMessage(level = DEBUG)
    @Message(id = 3033, value = "Character decoding failed. Parameter [%s] with value [%s] has been ignored.")
    void parameterDecodingFailed(String name, String value);

    @LogMessage(level = DEBUG)
    @Message(id = 3034, value = "Character decoding failed. A total of [%s] failures were detected. Enable debug level logging for this logger to log all failures.")
    void parametersDecodingFailures(int count);

    @LogMessage(level = WARN)
    @Message(id = 3035, value = "Parameters processing failed.")
    void parametersProcessingFailed();

    @LogMessage(level = DEBUG)
    @Message(id = 3036, value = "Invalid cookie header [%s].")
    void invalidCookieHeader(String header);

    @LogMessage(level = DEBUG)
    @Message(id = 3037, value = "Invalid special cookie [%s].")
    void invalidSpecialCookie(String cookie);

    @LogMessage(level = ERROR)
    @Message(id = 3038, value = "Error processing request")
    void errorProcessingRequest(@Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 3039, value = "Unexpected AJP message with type [%s].")
    void unexpectedAjpMessage(int type);

    @LogMessage(level = DEBUG)
    @Message(id = 3040, value = "Header message parsing failed.")
    void errorParsingAjpHeaderMessage(@Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 3041, value = "Error preparing AJP request.")
    void errorPreparingAjpRequest(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3042, value = "Certificate conversion failed")
    void errorProcessingCertificates(@Cause java.security.cert.CertificateException e);

    @LogMessage(level = ERROR)
    @Message(id = 3043, value = "Error initializing endpoint")
    void errorInitializingEndpoint(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3044, value = "Threadpool JMX registration failed")
    void errorRegisteringPool(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3045, value = "Error starting endpoint")
    void errorStartingEndpoint(@Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 3046, value = "Starting Coyote AJP/1.3 on %s")
    void startingAjpProtocol(String name);

    @LogMessage(level = ERROR)
    @Message(id = 3047, value = "Error pausing endpoint")
    void errorPausingEndpoint(@Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 3048, value = "Pausing Coyote AJP/1.3 on %s")
    void pausingAjpProtocol(String name);

    @LogMessage(level = ERROR)
    @Message(id = 3049, value = "Error resuming endpoint")
    void errorResumingEndpoint(@Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 3050, value = "Resuming Coyote AJP/1.3 on %s")
    void resumingAjpProtocol(String name);

    @LogMessage(level = INFO)
    @Message(id = 3051, value = "Stopping Coyote AJP/1.3 on %s")
    void stoppingAjpProtocol(String name);

    @LogMessage(level = WARN)
    @Message(id = 3052, value = "Skip destroy for Coyote AJP/1.3 on %s due to active request processors")
    void cannotDestroyAjpProtocol(String name);

    @LogMessage(level = ERROR)
    @Message(id = 3053, value = "Skip destroy for Coyote AJP/1.3 on %s due to active request processors")
    void cannotDestroyAjpProtocolWithException(String name, @Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 3054, value = "Socket exception processing event.")
    void socketException(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3055, value = "Error reading request, ignored.")
    void socketError(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 3056, value = "Error registering request")
    void errorRegisteringRequest(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 3057, value = "Error unregistering request")
    void errorUnregisteringRequest(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3058, value = "Cannot append null value to AJP message")
    void cannotAppendNull();

    @LogMessage(level = ERROR)
    @Message(id = 3059, value = "Overflow error for buffer adding %s bytes at position %s")
    void ajpMessageOverflow(int count, int pos);

    @LogMessage(level = ERROR)
    @Message(id = 3060, value = "Requested %s bytes exceeds message available data")
    void ajpMessageUnderflow(int count);

    @LogMessage(level = ERROR)
    @Message(id = 3061, value = "Invalid message received with signature %s")
    void invalidAjpMessage(int signature);

    @LogMessage(level = ERROR)
    @Message(id = 3062, value = "Error parsing regular expression %s")
    void errorParsingRegexp(String expression, @Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 3063, value = "Error during non blocking read")
    void errorWithNonBlockingRead(@Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 3064, value = "Error during blocking read")
    void errorWithBlockingRead(@Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 3065, value = "Error during non blocking write")
    void errorWithNonBlockingWrite(@Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 3066, value = "Error during blocking write")
    void errorWithBlockingWrite(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3067, value = "Exception getting socket information")
    void errorGettingSocketInformation(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 3068, value = "Unknown filter %s")
    void unknownFilter(String filter);

    @LogMessage(level = ERROR)
    @Message(id = 3069, value = "Error intializing filter %s")
    void errorInitializingFilter(String filter, @Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 3070, value = "Error parsing HTTP request header")
    void errorParsingHeader(@Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 3071, value = "Error preparing request")
    void errorPreparingRequest(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3072, value = "Error finishing request")
    void errorFinishingRequest(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3073, value = "Error finishing response")
    void errorFinishingResponse(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 3074, value = "Exception getting SSL attributes")
    void errorGettingSslAttributes(@Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 3075, value = "Coyote HTTP/1.1 pausing on: %s")
    void pauseHttpConnector(String name);

    @LogMessage(level = INFO)
    @Message(id = 3076, value = "Coyote HTTP/1.1 resuming on : %s")
    void resumeHttpConnector(String name);

    @LogMessage(level = INFO)
    @Message(id = 3077, value = "Coyote HTTP/1.1 stopping on : %s")
    void stopHttpConnector(String name);

    @LogMessage(level = WARN)
    @Message(id = 3078, value = "Skip destroy for Coyote HTTP/1.1 on %s due to active request processors")
    void cannotDestroyHttpProtocol(String name);

    @LogMessage(level = ERROR)
    @Message(id = 3079, value = "Skip destroy for Coyote HTTP/1.1 on %s due to active request processors")
    void cannotDestroyHttpProtocolWithException(String name, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3080, value = "Error initializing socket factory")
    void errorInitializingSocketFactory(@Cause Throwable t);

}
