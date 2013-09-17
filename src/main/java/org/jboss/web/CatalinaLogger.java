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

import java.io.File;
import java.util.Date;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

/**
 * Logging IDs 1000-2000
 * @author Remy Maucherat
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

    /**
     * A logger with the category of the package name.
     */
    CatalinaLogger CONNECTOR_LOGGER = Logger.getMessageLogger(CatalinaLogger.class, "org.apache.catalina.connector");

    /**
     * A logger with the category of the package name.
     */
    CatalinaLogger FILTERS_LOGGER = Logger.getMessageLogger(CatalinaLogger.class, "org.apache.catalina.filters");

    /**
     * A logger with the category of the package name.
     */
    CatalinaLogger STARTUP_LOGGER = Logger.getMessageLogger(CatalinaLogger.class, "org.apache.catalina.startup");

    /**
     * A logger with the category of the package name.
     */
    CatalinaLogger SESSION_LOGGER = Logger.getMessageLogger(CatalinaLogger.class, "org.apache.catalina.session");

    /**
     * A logger with the category of the package name.
     */
    CatalinaLogger CORE_LOGGER = Logger.getMessageLogger(CatalinaLogger.class, "org.apache.catalina.core");

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
    @Message(id = 1003, value = "Error digesting user credentials")
    void errorDigestingCredentials(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1004, value = "Failed realm [%s] JMX registration")
    void failedRealmJmxRegistration(Object objectName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1005, value = "Failed realm [%s] JMX unregistration")
    void failedRealmJmxUnregistration(Object objectName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1006, value = "Missing parent [%s]")
    void missingParentJmxRegistration(Object objectName, @Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 1007, value = "The connector has already been initialized")
    void connectorAlreadyInitialized();

    @LogMessage(level = ERROR)
    @Message(id = 1008, value = "Failed connector [%s] JMX registration")
    void failedConnectorJmxRegistration(Object objectName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1009, value = "Failed connector [%s] JMX unregistration")
    void failedConnectorJmxUnregistration(Object objectName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1010, value = "Protocol handler pause failed")
    void protocolHandlerPauseFailed(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1011, value = "Protocol handler resume failed")
    void protocolHandlerResumeFailed(@Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 1012, value = "The connector has already been started")
    void connectorAlreadyStarted();

    @LogMessage(level = INFO)
    @Message(id = 1013, value = "Cannot proceed with protocol handler JMX registration")
    void failedProtocolJmxRegistration();

    @LogMessage(level = INFO)
    @Message(id = 1014, value = "The connector has not been started")
    void connectorNotStarted();

    @LogMessage(level = ERROR)
    @Message(id = 1015, value = "Failed protocol handler [%s] JMX unregistration.")
    void failedProtocolJmxUnregistration(Object objectName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1016, value = "Connector stop failure")
    void connectorStopFailed(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1017, value = "The Servlet did not read all available bytes during the processing of the read event")
    void servletDidNotReadAvailableData();

    @LogMessage(level = ERROR)
    @Message(id = 1018, value = "An exception or error occurred in the container during the request processing")
    void exceptionDuringService(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1019, value = "The AsyncLisnener %s onComplete threw an exception, which will be ignored")
    void exceptionDuringComplete(String className, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1020, value = "Invalid URI encoding, will use HTTP default")
    void invalidEncodingUseHttpDefault(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1021, value = "Invalid URI encoding, will use straight conversion")
    void invalidEncoding(@Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 1022, value = "Exception thrown whilst processing multipart")
    void exceptionProcessingMultipart(@Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 1023, value = "Parameters were not parsed because the size of the posted data was too big. Use the maxPostSize attribute of the connector to resolve this if the application should accept large POSTs.")
    void postDataTooLarge();

    @LogMessage(level = DEBUG)
    @Message(id = 1024, value = "Exception thrown whilst processing POSTed parameters")
    void exceptionProcessingParameters(@Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 1025, value = "Request [%s], can not apply ExpiresFilter on already committed response")
    void expiresResponseAlreadyCommitted(String uri);

    @LogMessage(level = WARN)
    @Message(id = 1026, value = "Unknown parameter %s with value %s is ignored")
    void expiresUnknownParameter(String name, String value);

    @LogMessage(level = DEBUG)
    @Message(id = 1027, value = "Request [%s] with response status %s content-type %s, expiration header already defined")
    void expiresHeaderAlreadyDefined(String uri, int statusCode, String contentType);

    @LogMessage(level = DEBUG)
    @Message(id = 1028, value = "Request [%s] with response status %s content-type %s, skip expiration header generation for given status")
    void expiresSkipStatusCode(String uri, int statusCode, String contentType);

    @LogMessage(level = ERROR)
    @Message(id = 1029, value = "Error copying %s to %s")
    void fileCopyError(File src, File dest, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1030, value = "%s could not be completely deleted. The presence of the remaining files may cause problems")
    void fileDeleteError(String delete);

    @LogMessage(level = ERROR)
    @Message(id = 1031, value = "No Realm has been configured to authenticate against")
    void noRealmFound();

    @LogMessage(level = ERROR)
    @Message(id = 1032, value = "Cannot load authenticators mapping list")
    void cannotFindAuthenticatoMappings();

    @LogMessage(level = ERROR)
    @Message(id = 1033, value = "Cannot load authenticators mapping list")
    void failedLoadingAuthenticatoMappings(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1034, value = "Cannot configure an authenticator for method %s")
    void noAuthenticatorForAuthMethod(String authMethod);

    @LogMessage(level = ERROR)
    @Message(id = 1035, value = "Cannot instantiate an authenticator of class %s")
    void failedLoadingAuthenticator(String className, @Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 1036, value = "Configured an authenticator for method %s")
    void authenticatorConfigured(String authMethod);

    @LogMessage(level = ERROR)
    @Message(id = 1037, value = "Marking this application unavailable due to previous error(s)")
    void contextUnavailable();

    @LogMessage(level = INFO)
    @Message(id = 1038, value = "Security role name %s used in an <auth-constraint> without being defined in a <security-role>")
    void roleValidationAuth(String roleName);

    @LogMessage(level = INFO)
    @Message(id = 1039, value = "Security role name %s used in a <role-link> without being defined in a <security-role>")
    void roleValidationRunAs(String roleName);

    @LogMessage(level = INFO)
    @Message(id = 1040, value = "Security role name %s used in a <run-as> without being defined in a <security-role>")
    void roleValidationLink(String roleName);

    @LogMessage(level = ERROR)
    @Message(id = 1041, value = "Failed session manager [%s] JMX registration.")
    void failedSessionManagerJmxRegistration(Object objectName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1042, value = "Exception loading persisted sessions.")
    void managerLoadFailed(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1043, value = "Exception unloading persisted sessions.")
    void managerUnloadFailed(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1044, value = "Invalid session timeout setting %s")
    void managerInvalidSessionTimeout(String timeoutValue);

    @LogMessage(level = ERROR)
    @Message(id = 1045, value = "Exception checking load state for session %s")
    void persistentManagerIsLoadedException(String sessionId, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1046, value = "Exception clearing session store")
    void persistentManagerStoreClearException(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1047, value = "Error loading persisted sessions")
    void persistentManagerLoadFailed(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1048, value = "Error removing session %s")
    void persistentManagerSessionRemoveFailed(String sessionId, @Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 1049, value = "Unloading %s sessions")
    void persistentManagerSessionUnloadCount(int count);

    @LogMessage(level = ERROR)
    @Message(id = 1050, value = "Error swapping in session %s")
    void persistentManagerSwapInFailed(String sessionId, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1051, value = "Error swapping out session %s")
    void persistentManagerSwapOutFailed(String sessionId, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1052, value = "Swapped in invalid session %s")
    void persistentManagerSwapInInvalid(String sessionId);

    @LogMessage(level = DEBUG)
    @Message(id = 1053, value = "Swapped in session %s")
    void sessionSwapIn(String sessionId);

    @LogMessage(level = ERROR)
    @Message(id = 1054, value = "Error saving session %s to store")
    void persistentManagerStoreSaveError(String sessionId, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1055, value = "No store is configured, persistence disabled")
    void noStoreConfigured();

    @LogMessage(level = DEBUG)
    @Message(id = 1056, value = "Swapping session %s to Store, idle for %s seconds")
    void sessionSwapOut(String sessionId, int idle);

    @LogMessage(level = DEBUG)
    @Message(id = 1057, value = "Too many active sessions [%s] looking for idle sessions to swap out")
    void persistentManagerCheckIdle(int activeCount);

    @LogMessage(level = DEBUG)
    @Message(id = 1058, value = "Swapping out session %s, idle for %s seconds too many sessions active")
    void persistentManagerSwapIdleSession(String sessionId, int idle);

    @LogMessage(level = DEBUG)
    @Message(id = 1059, value = "Backing up session %s to Store, idle for %s seconds")
    void persistentManagerBackupSession(String sessionId, int idle);

    @LogMessage(level = ERROR)
    @Message(id = 1060, value = "JMX registration failed for filter of type [%s] and name [%s]")
    void filterJmxRegistrationFailed(String filterClass, String filterName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1061, value = "JMX unregistration failed for filter of type [%s] and name [%s]")
    void filterJmxUnregistrationFailed(String filterClass, String filterName, @Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 1062, value = "Failed to initialize the SSLEngine")
    void aprSslEngineInitFailedWithThrowable(@Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 1063, value = "Failed to initialize the SSLEngine")
    void aprSslEngineInitFailed();

    @LogMessage(level = DEBUG)
    @Message(id = 1064, value = "The native library which allows optimal performance in production environments was not found on the java.library.path: %s")
    void aprInitFailedWithThrowable(String libraryPath, @Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 1065, value = "The native library which allows optimal performance in production environments was not found on the java.library.path: %s")
    void aprInitFailed(String libraryPath);

    @LogMessage(level = ERROR)
    @Message(id = 1066, value = "An incompatible version %s.%s.%s of the native library is installed, while JBoss Web requires version %s.%s.%s")
    void aprInvalidVersion(int major, int minor, int patch, int requiredMajor, int requiredMinor, int requiredPatch);

    @LogMessage(level = INFO)
    @Message(id = 1067, value = "An older version %s.%s.%s of the native library is installed, while JBoss Web recommends version greater than %s.%s.%s")
    void aprRecommendedVersion(int major, int minor, int patch, int requiredMajor, int requiredMinor, int requiredPatch);

    @LogMessage(level = DEBUG)
    @Message(id = 1068, value = "Loaded native library %s.%s.%s with APR capabilities: IPv6 [%s], sendfile [%s], random [%s]")
    void aprInit(int major, int minor, int patch, boolean hasIp6, boolean hasSendfile, boolean hasRandom);

    @LogMessage(level = ERROR)
    @Message(id = 1069, value = "Error stopping loader")
    void errorStoppingLoader(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1070, value = "Error starting loader")
    void errorStartingLoader(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1071, value = "Error stopping manager")
    void errorStoppingManager(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1072, value = "Error starting manager")
    void errorStartingManager(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1073, value = "Error stopping cluster")
    void errorStoppingCluster(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1074, value = "Error starting cluster")
    void errorStartingCluster(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1075, value = "Error stopping realm")
    void errorStoppingRealm(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1076, value = "Error starting realm")
    void errorStartingRealm(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1077, value = "Error starting realm")
    void errorStoppingChild(@Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 1078, value = "Container %s has already been started")
    void containerAlreadyStarted(String name);

    @LogMessage(level = INFO)
    @Message(id = 1079, value = "Container %s has not been started")
    void containerNotStarted(String name);

    @LogMessage(level = ERROR)
    @Message(id = 1080, value = "Failed container [%s] JMX unregistration")
    void failedContainerJmxUnregistration(Object objectName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1081, value = "Error invoking periodic operation")
    void errorInPeriodicOperation(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1082, value = "Background processing error in [%s]")
    void backgroundProcessingError(Object component, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1083, value = "Failed engine [%s] JMX registration")
    void failedEngineJmxRegistration(Object objectName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1084, value = "Error setting up service")
    void failedServiceCreation(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1085, value = "Failed loding specified error report valve class: %s")
    void invalidErrorReportValveClass(String className, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1086, value = "Failed host [%s] JMX registration")
    void failedHostJmxRegistration(Object objectName, @Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 1087, value = "Error instantiating servlet class %s")
    void errorInstantiatingServletClass(String className, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1088, value = "Failed Servlet [%s] JMX registration")
    void failedServletJmxRegistration(Object object, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1089, value = "Failed Servlet [%s] JSP monitoring JMX registration")
    void failedServletJspMonitorJmxRegistration(Object object, @Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 1090, value = "Form login page %s must start with a ''/'' in Servlet 2.4")
    void loginPageStartsWithSlash(String loginPage);

    @LogMessage(level = DEBUG)
    @Message(id = 1091, value = "Error page location %s must start with a ''/'' in Servlet 2.4")
    void errorPageStartsWithSlash(String errorPage);

    @LogMessage(level = ERROR)
    @Message(id = 1092, value = "Failed access to work directory for Context %s")
    void failedObtainingWorkDirectory(String name, @Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 1093, value = "The listener %s is already configured for this context, the duplicate definition has been ignored")
    void duplicateListener(String listenerClass);

    @LogMessage(level = DEBUG)
    @Message(id = 1094, value = "JSP file %s must start with a ''/'' in Servlet 2.4")
    void jspFileStartsWithSlash(String jspFile);

    @LogMessage(level = INFO)
    @Message(id = 1095, value = "Cannot find JSP Servlet, so ignoring jsp-property-group mappings")
    void missingJspServlet();

    @LogMessage(level = ERROR)
    @Message(id = 1096, value = "Error stopping context %s")
    void errorStoppingContext(String name, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1097, value = "Error starting context %s")
    void errorStartingContext(String name, @Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 1098, value = "Starting filter %s")
    void startingFilter(String filterName);

    @LogMessage(level = DEBUG)
    @Message(id = 1099, value = "Stopping filter %s")
    void stoppingFilter(String filterName);

    @LogMessage(level = ERROR)
    @Message(id = 1100, value = "Error starting context")
    void errorStartingResources(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1101, value = "Error stopping context")
    void errorStoppingResources(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1102, value = "Error initializing resources")
    void errorInitializingResources(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1103, value = "Error detected during context %s start, will stop it")
    void errorStartingContextWillStop(String name);

    @LogMessage(level = ERROR)
    @Message(id = 1104, value = "Error performing failed context %s start cleanup")
    void errorStartingContextCleanup(String name, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1105, value = "Error resetting context %s")
    void errorResettingContext(String name, @Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 1106, value = "URL pattern %s must start with a ''/'' in Servlet 2.4")
    void urlPatternStartsWithSlash(String urlPattern);

    @LogMessage(level = INFO)
    @Message(id = 1107, value = "Suspicious url pattern: %s in context %s - see section SRV.11.2 of the Servlet specification")
    void suspiciousUrlPattern(String contextName, String urlPattern);

    @LogMessage(level = INFO)
    @Message(id = 1108, value = "Context %s object name creation failed")
    void contextObjectNameCreationFailed(String contextName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1109, value = "Cannot find context %s parent Host JMX name")
    void cannotFindContextJmxParentName(String name);

    @LogMessage(level = WARN)
    @Message(id = 1110, value = "JSP container initialization failed")
    void jspContainerInitializationFailed(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 1111, value = "Thread %s (id=%s) has been active for %s milliseconds (since %s) to serve the same request for %s and may be stuck (configured threshold for this StuckThreadDetectionValve is %s seconds). There is/are %s thread(s) in total that are monitored by this Valve and may be stuck.")
    void stuckThreadDetected(String threadName, long threadId, long active, Date start, String requestUri, int threshold, int stuckCount, @Cause Throwable stackTrace);

    @LogMessage(level = WARN)
    @Message(id = 1112, value = "Thread %s (id=%s) was previously reported to be stuck but has completed. It was active for approximately %s milliseconds. There is/are still %s thread(s) that are monitored by this Valve and may be stuck.")
    void stuckThreadCompleted(String threadName, long threadId, long active, int stuckCount);

    @LogMessage(level = WARN)
    @Message(id = 1113, value = "Failed to trigger creation of the GC Daemon thread during start to prevent possible memory leaks. This is expected on non-Sun JVMs.")
    void errorCreatingGcDaemon(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 1114, value = "Error whilst attempting to prevent memory leak in javax.security.auth.Policy class")
    void errorLoadingPolicy(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 1115, value = "Failed to disable Jar URL connection caching by default")
    void errorDisablingUrlConnectionCaching(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 1116, value = "Error whilst attempting to prevent memory leaks during XML parsing")
    void errorLoadingJaxp(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 1117, value = "Failed to trigger creation of the com.sun.jndi.ldap.LdapPoolManager class during Tomcat start to prevent possible memory leaks. This is expected on non-Sun JVMs.")
    void errorLoadingLdapPoolManager(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 1118, value = "Failed to load class %s during Tomcat start to prevent possible memory leaks.")
    void errorLoadingLeakClass(String className, @Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 1119, value = "Client abort exception: %s")
    void clientAbortException(String message);

    @LogMessage(level = DEBUG)
    @Message(id = 1120, value = "Pipeline already started")
    void pipelineAlreadyStarted();

    @LogMessage(level = DEBUG)
    @Message(id = 1121, value = "Pipeline has not been started")
    void pipelineNotStarted();

    @LogMessage(level = ERROR)
    @Message(id = 1122, value = "Failed valve [%s] JMX registration")
    void failedValveJmxRegistration(Object valve, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1123, value = "Failed valve [%s] JMX unregistration")
    void failedValveJmxUnregistration(Object valve, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1124, value = "Error stopping valve")
    void errorStoppingValve(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1125, value = "Error starting valve")
    void errorStartingValve(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1126, value = "Error starting service")
    void errorStartingService(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1127, value = "Error initializing service")
    void errorInitializingService(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1128, value = "Failed service [%s] JMX registration")
    void failedServiceJmxRegistration(Object objectName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1129, value = "Error starting connector")
    void errorStartingConnector(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1130, value = "Error initializing connector")
    void errorInitializingConnector(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1131, value = "Error stopping connector")
    void errorStoppingConnector(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1132, value = "Error starting executor")
    void errorStartingExecutor(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1133, value = "Error stopping executor")
    void errorStoppingExecutor(@Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 1134, value = "This service has already been started")
    void serviceAlreadyStarted();

    @LogMessage(level = DEBUG)
    @Message(id = 1135, value = "This service has not been started")
    void serviceNotStarted();

    @LogMessage(level = DEBUG)
    @Message(id = 1136, value = "Starting service %s")
    void startingService(String serviceName);

    @LogMessage(level = DEBUG)
    @Message(id = 1137, value = "Stopping service %s")
    void stoppingService(String serviceName);

    @LogMessage(level = ERROR)
    @Message(id = 1138, value = "Failed server [%s] JMX registration")
    void failedServerJmxRegistration(Object objectName, @Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 1139, value = "Context %s JMX registration failed")
    void contextJmxRegistrationFailed(String contextName, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1140, value = "Failed protocol handler [%s] JMX registration")
    void failedProtocolJmxRegistration(Object objectName, @Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 1141, value = "NIO 2 is not available, the java.io connector will be used instead")
    void usingJavaIoConnector();

    @LogMessage(level = ERROR)
    @Message(id = 1142, value = "Access log rotation failed")
    void errorRotatingAccessLog(@Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 1143, value = "Error closing old log file")
    void errorClosingOldAccessLog(@Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 1144, value = "Pattern was just empty or whitespace")
    void extendedAccessLogEmptyPattern();

    @LogMessage(level = ERROR)
    @Message(id = 1145, value = "Pattern parse error")
    void extendedAccessLogPatternParseError(@Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 1146, value = "Unable to decode with rest of chars starting: %s")
    void extendedAccessLogUnknownToken(String token);

    @LogMessage(level = ERROR)
    @Message(id = 1147, value = "No closing ) found for in decode")
    void extendedAccessLogMissingClosing();

    @LogMessage(level = ERROR)
    @Message(id = 1148, value = "The next characters couldn't be decoded: %s")
    void extendedAccessLogCannotDecode(String chars);

    @LogMessage(level = ERROR)
    @Message(id = 1149, value = "X param for servlet request, couldn't decode value: %s")
    void extendedAccessLogCannotDecodeXParamValue(String value);

    @LogMessage(level = ERROR)
    @Message(id = 1150, value = "X param in wrong format. Needs to be 'x-#(...)'")
    void extendedAccessLogBadXParam();

}
