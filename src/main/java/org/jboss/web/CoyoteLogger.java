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
import static org.jboss.logging.Logger.Level.DEBUG;

import java.net.InetAddress;
import java.security.cert.Certificate;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

/**
 * Logging IDs 3000-4000
 * @author Remy Maucherat
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
    CoyoteLogger NET_LOGGER = Logger.getMessageLogger(CoyoteLogger.class, "org.apache.tomcat.util.net");

    /**
     * A logger with the category of the package name.
     */
    CoyoteLogger HTTP_LOGGER = Logger.getMessageLogger(CoyoteLogger.class, "org.apache.coyote.http11");

    /**
     * A logger with the category of the class name.
     */
    CoyoteLogger HTTP_BIO_LOGGER = Logger.getMessageLogger(CoyoteLogger.class, "org.apache.coyote.http11.Http11Protocol");

    /**
     * A logger with the category of the class name.
     */
    CoyoteLogger HTTP_APR_LOGGER = Logger.getMessageLogger(CoyoteLogger.class, "org.apache.coyote.http11.Http11AprProtocol");

    /**
     * A logger with the category of the class name.
     */
    CoyoteLogger HTTP_NIO_LOGGER = Logger.getMessageLogger(CoyoteLogger.class, "org.apache.coyote.http11.Http11NioProtocol");

    /**
     * A logger with the category of the package name.
     */
    CoyoteLogger AJP_LOGGER = Logger.getMessageLogger(CoyoteLogger.class, "org.apache.coyote.ajp");
    /**
     * A logger with the category of the package name.
     */
    CoyoteLogger BAYEUX_LOGGER = Logger.getMessageLogger(CoyoteLogger.class, "org.apache.tomcat.bayeux");

    /**
     * A logger with the category of the package name.
     */
    CoyoteLogger MODELER_LOGGER = Logger.getMessageLogger(CoyoteLogger.class, "org.apache.tomcat.util.modeler");

    /**
     * A logger with the category of the package name.
     */
    CoyoteLogger FILEUPLOAD_LOGGER = Logger.getMessageLogger(CoyoteLogger.class, "org.apache.tomcat.util.http.fileupload");

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

    @LogMessage(level = DEBUG)
    @Message(id = 3011, value = "Error allocating socket processor")
    void errorProcessingSocket(@Cause Throwable exception);

    @LogMessage(level = DEBUG)
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

    @LogMessage(level = ERROR)
    @Message(id = 3026, value = "Error loading %s")
    void errorLoading(Object source);

    @LogMessage(level = WARN)
    @Message(id = 3027, value = "Failed loading HTTP messages strings")
    void errorLoadingMessages(@Cause Throwable exception);

    // 3028

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

    @LogMessage(level = WARN)
    @Message(id = 3081, value = "Check Bayeux exception")
    void errorInCheckBayeux(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3082, value = "Error processing Bayeux")
    void errorProcessingBayeux(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3083, value = "Message delivery error")
    void errorDeliveringBayeux(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 3084, value = "Failed setting property %s on object %s to %s")
    void errorSettingProperty(String propertyName, Object object, String propertyValue, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 3085, value = "Failed getting property %s on object %s")
    void errorGettingProperty(String propertyName, Object object, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 3086, value = "Bad maximum certificate length %s")
    void invalidMaxCertLength(String length);

    @LogMessage(level = INFO)
    @Message(id = 3087, value = "Error translating certificate %s")
    void errorTranslatingCertificate(Certificate certificate, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 3088, value = "SSL server initiated renegotiation is disabled, closing connection")
    void disabledSslRenegociation();

    @LogMessage(level = ERROR)
    @Message(id = 3089, value = "No descriptors found")
    void noDescriptorsFound();

    @LogMessage(level = ERROR)
    @Message(id = 3090, value = "No Mbeans found")
    void noMbeansFound();

    @LogMessage(level = ERROR)
    @Message(id = 3091, value = "Error reading descriptors")
    void errorReadingDescriptors(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3092, value = "Error creating MBean %s")
    void errorCreatingMbean(String objectName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3093, value = "Error invoking %s on %s")
    void errorInvoking(String operation, String name);

    @LogMessage(level = ERROR)
    @Message(id = 3094, value = "Node not found %s")
    void nodeNotFound(Object name);

    @LogMessage(level = ERROR)
    @Message(id = 3095, value = "Error writing MBeans")
    void errorWritingMbeans(@Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 3096, value = "Can't find attribute %s on %s")
    void attributeNotFound(String attributeName, String objectName);

    @LogMessage(level = ERROR)
    @Message(id = 3097, value = "Error processing attribute %s value %s on %s")
    void errorProcessingAttribute(String attributeName, String attributeValue, String objectName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3098, value = "Error sending notification")
    void errorSendingNotification(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3099, value = "Error creating object name")
    void errorCreatingObjectName(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3100, value = "Error invoking operation %s on %s")
    void errorInvokingOperation(String operation, Object objectName);

    @LogMessage(level = INFO)
    @Message(id = 3101, value = "No metadata for %s")
    void noMetadata(Object objectName);

    @LogMessage(level = ERROR)
    @Message(id = 3102, value = "Error unregistering MBean %s")
    void errorUnregisteringMbean(Object objectName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 3103, value = "Null %s component")
    void nullComponent(Object objectName);

    @LogMessage(level = ERROR)
    @Message(id = 3104, value = "Error registering MBean %s")
    void errorRegisteringMbean(Object objectName, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 3105, value = "Socket accept failed")
    void warnAcceptingSocket(@Cause Throwable exception);

    @LogMessage(level = WARN)
    @Message(id = 3106, value = "Unsupported protocol %s")
    void unsupportedProtocol(String unsupportedProtocol);

}
