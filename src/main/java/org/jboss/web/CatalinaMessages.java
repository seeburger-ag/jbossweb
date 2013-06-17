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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;

/**
 * Logging IDs 1-1000
 * @author Remy Maucherat
 */
@MessageBundle(projectCode = "JBWEB")
public interface CatalinaMessages {

    /**
     * The messages
     */
    CatalinaMessages MESSAGES = Messages.getBundle(CatalinaMessages.class);

    @Message(id = 1, value = "Configuration error:  Must be attached to a Context")
    IllegalArgumentException authenticatorNeedsContext();

    @Message(id = 2, value = "Security Interceptor has already been started")
    String authenticatorAlreadyStarted();

    @Message(id = 3, value = "Security Interceptor has not yet been started")
    String authenticatorNotStarted();

    @Message(id = 4, value = "The request body was too large to be cached during the authentication process")
    String requestBodyTooLarge();

    @Message(id = 5, value = "The time allowed for the login process has been exceeded. If you wish to continue you must either click back twice and re-click the link you requested or close and re-open your browser")
    String sessionTimeoutDuringAuthentication();

    @Message(id = 6, value = "Invalid direct reference to form login page")
    String invalidFormLoginDirectReference();

    @Message(id = 7, value = "Unexpected error forwarding to error page")
    String errorForwardingToFormError();

    @Message(id = 8, value = "Unexpected error forwarding to login page")
    String errorForwardingToFormLogin();

    @Message(id = 9, value = "No client certificate chain in this request")
    String missingRequestCertificate();

    @Message(id = 10, value = "Cannot authenticate with the provided credentials")
    String certificateAuthenticationFailure();

    @Message(id = 11, value = "Valve has already been started")
    String valveAlreadyStarted();

    @Message(id = 12, value = "Valve has not yet been started")
    String valveNotStarted();

    @Message(id = 13, value = "Username [%s] NOT successfully authenticated")
    String userNotAuthenticated(String userName);

    @Message(id = 14, value = "Username [%s] successfully authenticated")
    String userAuthenticated(String userName);

    @Message(id = 15, value = "Access to the requested resource has been denied")
    String forbiddenAccess();

    @Message(id = 16, value = "User [%s] does not have role [%s]")
    String userDoesNotHaveRole(String user, String role);

    @Message(id = 17, value = "User [%s] has role [%s]")
    String userHasRole(String user, String role);

    @Message(id = 18, value = "Realm has already been started")
    String realmAlreadyStarted();

    @Message(id = 19, value = "Realm has not yet been started")
    String realmNotStarted();

    @Message(id = 20, value = "Invalid message digest algorithm %s specified")
    String invalidMessageDigest(String digest);

    @Message(id = 21, value = "Illegal digest encoding %s")
    IllegalArgumentException illegalDigestEncoding(String digest, @Cause UnsupportedEncodingException e);

    @Message(id = 22, value = "Missing MD5 digest")
    IllegalArgumentException noMD5Digest(@Cause NoSuchAlgorithmException e);

    @Message(id = 23, value = "Protocol handler initialization failed")
    String protocolHandlerInitFailed(@Cause Throwable t);

    @Message(id = 24, value = "Protocol handler start failed")
    String protocolHandlerStartFailed(@Cause Throwable t);

    @Message(id = 25, value = "Protocol handler destroy failed")
    String protocolHandlerDestroyFailed(@Cause Throwable t);

    @Message(id = 26, value = "Failed to instatiate protocol handler")
    IllegalArgumentException protocolHandlerInstantiationFailed(@Cause Throwable t);

    @Message(id = 27, value = "getWriter() has already been called for this response")
    IllegalStateException writerAlreadyUsed();

    @Message(id = 28, value = "getOutputStream() has already been called for this response")
    IllegalStateException outputStreamAlreadyUsed();

    @Message(id = 29, value = "Cannot reset buffer after response has been committed")
    IllegalStateException cannotResetBuffer();

    @Message(id = 30, value = "Cannot change buffer size after data has been written")
    IllegalStateException cannotChangeBufferSize();

    @Message(id = 31, value = "Cannot call sendError() after the response has been committed")
    IllegalStateException cannotSendError();

    @Message(id = 32, value = "Cannot call sendRedirect() after the response has been committed")
    IllegalStateException cannotSendRedirect();

    @Message(id = 33, value = "Cannot call sendUpgrade() after the response has been committed")
    IllegalStateException cannotSendUpgrade();

    @Message(id = 34, value = "Cannot upgrade from HTTP/1.1 without IO events")
    IllegalStateException cannotUpgradeWithoutEvents();

    @Message(id = 35, value = "Cannot upgrade from HTTP/1.1 is not using an HttpEventServlet")
    IllegalStateException cannotUpgradeWithoutEventServlet();

    @Message(id = 36, value = "Cannot call sendFile() after the response has been committed")
    IllegalStateException cannotSendFile();

    @Message(id = 37, value = "Sendfile is disabled")
    IllegalStateException noSendFile();

    @Message(id = 38, value = "Invalid path for sendfile %s")
    IllegalStateException invalidSendFilePath(String path);

    @Message(id = 39, value = "getReader() has already been called for this request")
    IllegalStateException readerAlreadyUsed();

    @Message(id = 40, value = "getInputStream() has already been called for this request")
    IllegalStateException inputStreamAlreadyUsed();

    @Message(id = 41, value = "Exception thrown by attributes event listener")
    String attributesEventListenerException();

    @Message(id = 42, value = "Cannot call setAttribute with a null name")
    IllegalStateException attributeNameNotSpecified();

    @Message(id = 43, value = "Cannot create a session after the response has been committed")
    IllegalStateException cannotCreateSession();

    @Message(id = 44, value = "Parameters were not parsed because the size of the posted data was too big. Use the maxPostSize attribute of the connector to resolve this if the application should accept large POSTs.")
    IllegalStateException postDataTooLarge();

    @Message(id = 45, value = "The request is not multipart content")
    String notMultipart();

    @Message(id = 46, value = "Exception thrown whilst processing multipart")
    IllegalStateException multipartProcessingFailed(@Cause Throwable t);

    @Message(id = 47, value = "Exception thrown whilst processing multipart")
    IOException multipartIoProcessingFailed(@Cause Throwable t);

    @Message(id = 48, value = "The servlet or filters that are being used by this request do not support async operation")
    IllegalStateException noAsync();

    @Message(id = 49, value = "Response has been closed already")
    IllegalStateException asyncClose();

    @Message(id = 50, value = "Cannot start async")
    IllegalStateException cannotStartAsync();

    @Message(id = 51, value = "Error invoking onStartAsync on listener of class %s")
    IllegalStateException errorStartingAsync(String listenerClassName, @Cause Throwable t);

    @Message(id = 52, value = "No authenticator available for programmatic login")
    String noAuthenticator();

    @Message(id = 53, value = "Failed to authenticate a principal")
    String authenticationFailure();

    @Message(id = 54, value = "Exception logging out user")
    String logoutFailure();

    @Message(id = 55, value = "Could not determine or access context for server absolute URI %s")
    IllegalStateException cannotFindDispatchContext(String uri);

    @Message(id = 56, value = "Failed to instantiate class %s")
    String listenerCreationFailed(String className);

    @Message(id = 57, value = "The request object has been recycled and is no longer associated with this facade")
    IllegalStateException nullRequestFacade();

    @Message(id = 58, value = "The response object has been recycled and is no longer associated with this facade")
    IllegalStateException nullResponseFacade();

    @Message(id = 59, value = "Stream closed")
    IOException streamClosed();

    @Message(id = 64, value = "Error report")
    String errorReport();

    @Message(id = 65, value = "HTTP Status %s - %s")
    String statusHeader(int statusCode, String message);

    @Message(id = 66, value = "Exception report")
    String exceptionReport();

    @Message(id = 67, value = "Status report")
    String statusReport();

    @Message(id = 68, value = "message")
    String statusMessage();

    @Message(id = 69, value = "description")
    String statusDescritpion();

    @Message(id = 70, value = "exception")
    String statusException();

    @Message(id = 71, value = "root cause")
    String statusRootCause();

    @Message(id = 72, value = "note")
    String statusNote();

    @Message(id = 73, value = "The full stack trace of the root cause is available in the %s logs.")
    String statusRootCauseInLogs(String log);

    @Message(id = 74, value = "Exception processing event.")
    String eventValveExceptionDuringEvent();

    @Message(id = 75, value = "Exception processing session listener event.")
    String eventValveSessionListenerException();

    @Message(id = 76, value = "Exception performing insert access entry.")
    String jdbcAccessLogValveInsertError();

    @Message(id = 77, value = "Exception closing database connection.")
    String jdbcAccessLogValveConnectionCloseError();

    @Message(id = 78, value = "Syntax error in request filter pattern %s")
    String requestFilterValvePatternError(String pattern);

    @Message(id = 79, value = "The property %s is not defined for filters of type %s")
    String propertyNotFound(String property, String className);

    @Message(id = 80, value = "Unable to create Random source using class %s")
    String cannotCreateRandom(String className);

    @Message(id = 81, value = "Unsupported startingPoint %s")
    IllegalStateException expiresUnsupportedStartingPoint(String startingPoint);

    @Message(id = 82, value = "Exception processing configuration parameter %s: %s")
    String expiresExceptionProcessingParameter(String name, String value);

    @Message(id = 83, value = "Starting point (access|now|modification|a<seconds>|m<seconds>) not found in directive %s")
    IllegalStateException expiresStartingPointNotFound(String line);

    @Message(id = 84, value = "Invalid starting point (access|now|modification|a<seconds>|m<seconds>) %s in directive %s")
    IllegalStateException expiresInvalidStartingPoint(String token, String line);

    @Message(id = 85, value = "Duration not found in directive %s")
    IllegalStateException expiresDurationNotFound(String line);

    @Message(id = 86, value = "Invalid duration (number) %s in directive %s")
    IllegalStateException expiresInvalidDuration(String token, String line);

    @Message(id = 87, value = "Duration unit not found after amount %s in directive %s")
    IllegalStateException expiresDurationUnitNotFound(int amount, String line);

    @Message(id = 88, value = "The requested resource (%s) is not available")
    String resourceNotAvailable(String resource);

    @Message(id = 89, value = "Directory Listing For %s")
    String listingDirectoryTitle(String directory);

    @Message(id = 90, value = "Up To %s")
    String listingDirectoryParent(String directory);

    @Message(id = 91, value = "Filename")
    String listingFilename();

    @Message(id = 92, value = "Size")
    String listingSize();

    @Message(id = 93, value = "Last Modified")
    String listingLastModified();

    @Message(id = 94, value = "JAXP initialization failed")
    String jaxpInitializationFailed();

    @Message(id = 95, value = "Ignored external entity %s %s")
    String ignoredExternalEntity(String publicId, String systemId);

    @Message(id = 96, value = "No modifications are allowed to a locked ParameterMap")
    IllegalStateException lockedParameterMap();

    @Message(id = 97, value = "No modifications are allowed to a locked ResourceSet")
    IllegalStateException lockedResourceSet();

    @Message(id = 98, value = "Bad hexadecimal digit")
    IllegalArgumentException badHexDigit();

    @Message(id = 99, value = "Odd number of hexadecimal digits")
    IllegalArgumentException oddNomberOfHexDigits();

    @Message(id = 100, value = "The client may continue.")
    String http100();

    @Message(id = 101, value = "The server is switching protocols according to the 'Upgrade' header.")
    String http101();

    @Message(id = 102, value = "The server has accepted the complete request, but has not yet completed it.")
    String http102();

    @Message(id = 103, value = "The request succeeded and a new resource has been created on the server.")
    String http201();

    @Message(id = 104, value = "This request was accepted for processing, but has not been completed.")
    String http202();

    @Message(id = 105, value = "The meta information presented by the client did not originate from the server.")
    String http203();

    @Message(id = 106, value = "The request succeeded but there is no information to return.")
    String http204();

    @Message(id = 107, value = "The client should reset the document view which caused this request to be sent.")
    String http205();

    @Message(id = 108, value = "The server has fulfilled a partial GET request for this resource.")
    String http206();

    @Message(id = 109, value = "Multiple status values have been returned.")
    String http207();

    @Message(id = 110, value = "This collection binding was already reported.")
    String http208();

    @Message(id = 111, value = "The response is a representation of the result of one or more instance-manipulations applied to the current instance.")
    String http226();

    @Message(id = 112, value = "The requested resource corresponds to any one of a set of representations, each with its own specific location.")
    String http300();

    @Message(id = 113, value = "The requested resource has moved permanently to a new location.")
    String http301();

    @Message(id = 114, value = "The requested resource has moved temporarily to a new location.")
    String http302();

    @Message(id = 115, value = "The response to this request can be found under a different URI.")
    String http303();

    @Message(id = 116, value = "The requested resource is available and has not been modified.")
    String http304();

    @Message(id = 117, value = "The requested resource must be accessed through the proxy given by the 'Location' header.")
    String http305();

    @Message(id = 118, value = "The requested resource resides temporarily under a different URI.")
    String http307();

    @Message(id = 119, value = "The target resource has been assigned a new permanent URI and any future references to this resource SHOULD use one of the returned URIs.")
    String http308();

    @Message(id = 120, value = "The request sent by the client was syntactically incorrect.")
    String http400();

    @Message(id = 121, value = "This request requires HTTP authentication.")
    String http401();

    @Message(id = 122, value = "Payment is required for access to this resource.")
    String http402();

    @Message(id = 123, value = "Access to the specified resource has been forbidden.")
    String http403();

    @Message(id = 124, value = "The requested resource is not available.")
    String http404();

    @Message(id = 125, value = "The specified HTTP method is not allowed for the requested resource.")
    String http405();

    @Message(id = 126, value = "The resource identified by this request is only capable of generating responses with characteristics not acceptable according to the request 'Accept' headers.")
    String http406();

    @Message(id = 127, value = "The client must first authenticate itself with the proxy.")
    String http407();

    @Message(id = 128, value = "The client did not produce a request within the time that the server was prepared to wait.")
    String http408();

    @Message(id = 129, value = "The request could not be completed due to a conflict with the current state of the resource.")
    String http409();

    @Message(id = 130, value = "The requested resource is no longer available, and no forwarding address is known.")
    String http410();

    @Message(id = 131, value = "This request cannot be handled without a defined content length.")
    String http411();

    @Message(id = 132, value = "A specified precondition has failed for this request.")
    String http412();

    @Message(id = 133, value = "The request entity is larger than the server is willing or able to process.")
    String http413();

    @Message(id = 134, value = "The server refused this request because the request URI was too long.")
    String http414();

    @Message(id = 135, value = "The server refused this request because the request entity is in a format not supported by the requested resource for the requested method.")
    String http415();

    @Message(id = 136, value = "The requested byte range cannot be satisfied.")
    String http416();

    @Message(id = 137, value = "The expectation given in the 'Expect' request header could not be fulfilled.")
    String http417();

    @Message(id = 138, value = "The server understood the content type and syntax of the request but was unable to process the contained instructions.")
    String http422();

    @Message(id = 139, value = "The source or destination resource of a method is locked.")
    String http423();

    @Message(id = 140, value = "The method could not be performed on the resource because the requested action depended on another action and that action failed.")
    String http424();

    @Message(id = 141, value = "The request can only be completed after a protocol upgrade.")
    String http426();

    @Message(id = 142, value = "The request is required to be conditional.")
    String http428();

    @Message(id = 143, value = "The user has sent too many requests in a given amount of time.")
    String http429();

    @Message(id = 144, value = "The server refused this request because the request header fields are too large.")
    String http431();

    @Message(id = 145, value = "The server encountered an internal error that prevented it from fulfilling this request.")
    String http500();

    @Message(id = 146, value = "The server does not support the functionality needed to fulfill this request.")
    String http501();

    @Message(id = 147, value = "This server received an invalid response from a server it consulted when acting as a proxy or gateway.")
    String http502();

    @Message(id = 148, value = "The requested service is not currently available.")
    String http503();

    @Message(id = 149, value = "The server received a timeout from an upstream server while acting as a gateway or proxy.")
    String http504();

    @Message(id = 150, value = "The server does not support the requested HTTP protocol version.")
    String http505();

    @Message(id = 151, value = "The chosen variant resource is configured to engage in transparent content negotiation itself, and is therefore not a proper end point in the negotiation process.")
    String http506();

    @Message(id = 152, value = "The resource does not have sufficient space to record the state of the resource after execution of this method.")
    String http507();

    @Message(id = 153, value = "The server terminated an operation because it encountered an infinite loop.")
    String http508();

    @Message(id = 154, value = "The policy for accessing the resource has not been met in the request.")
    String http510();

    @Message(id = 155, value = "The client needs to authenticate to gain network access.")
    String http511();

    @Message(id = 200, value = "Store has already been started")
    String storeAlreadyStarted();

    @Message(id = 201, value = "Store has not yet been started")
    String storeNotStarted();

    @Message(id = 202, value = "Loading Session %s from file %s")
    String fileStoreSessionLoad(String sessionId, String file);

    @Message(id = 203, value = "Saving Session %s to file %s")
    String fileStoreSessionSave(String sessionId, String file);

    @Message(id = 204, value = "Removing Session %s at file %s")
    String fileStoreSessionRemove(String sessionId, String file);

    @Message(id = 205, value = "No persisted data file found")
    String fileStoreFileNotFound();

    @Message(id = 206, value = "Parent Container is not a Context")
    IllegalArgumentException parentNotContext();

    @Message(id = 207, value = "JDBC Store SQL exception")
    String jdbcStoreDatabaseError();

    @Message(id = 208, value = "JDBC driver class not found %s")
    String jdbcStoreDriverFailure(String className);

    @Message(id = 209, value = "Session creation failed due to too many active sessions")
    IllegalStateException managerMaxActiveSessions();

    @Message(id = 210, value = "Error deserializing Session %s")
    IllegalStateException persistentManagerDeserializeError(String sessionId, @Cause Throwable t);

    @Message(id = 211, value = "Session event listener threw exception")
    String sessionEventListenerException();

    @Message(id = 212, value = "Session already invalidated")
    IllegalStateException invalidSession();

    @Message(id = 213, value = "Exception logging out user when expiring session")
    String sessionLogoutException();

    @Message(id = 214, value = "Session attribute event listener threw exception")
    String sessionAttributeEventListenerException();

    @Message(id = 215, value = "Session attribute name cannot be null")
    IllegalArgumentException sessionAttributeNameIsNull();

    @Message(id = 216, value = "Non-serializable attribute %s")
    IllegalArgumentException sessionAttributeIsNotSerializable(String name);

    @Message(id = 217, value = "Session binding event listener threw exception")
    String sessionBindingEventListenerException();

    @Message(id = 218, value = "Cannot serialize session attribute %s for session %s")
    String sessionAttributeSerializationException(Object attribute, String sessionId);

    @Message(id = 219, value = "Path %s does not start with a '/' character")
    IllegalArgumentException invalidDispatcherPath(String path);

    @Message(id = 220, value = "Dispatcher mapping error")
    String dispatcherMappingError();

    @Message(id = 221, value = "Path %s does not start with a '/' character")
    String invalidDispatcherPathString(String path);

    @Message(id = 222, value = "Exception thrown by attributes event listener")
    String servletContextAttributeListenerException();

    @Message(id = 223, value = "Attribute name cannot be null")
    String servletContextAttributeNameIsNull();

    @Message(id = 224, value = "The listener that attempted to call this method is restricted")
    UnsupportedOperationException restrictedListenerCannotCallMethod();

    @Message(id = 225, value = "Context %s is already initialized")
    IllegalStateException contextAlreadyInitialized(String context);

    @Message(id = 226, value = "Error creating instance")
    String contextObjectCreationError();

    @Message(id = 227, value = "Bad listener class %s for context %s")
    IllegalArgumentException invalidContextListener(String className, String path);

    @Message(id = 228, value = "Bad listener class %s for context %s")
    IllegalArgumentException invalidContextListenerWithException(String className, String path, @Cause Throwable t);

    @Message(id = 229, value = "The session tracking mode %s requested for context %s is not supported by that context")
    IllegalArgumentException unsupportedSessionTrackingMode(String sessionTracking, String path);

    @Message(id = 230, value = "The session tracking mode SSL requested for context %s cannot be combined with other tracking modes")
    IllegalArgumentException sslSessionTrackingModeIsExclusive(String path);

    @Message(id = 231, value = "Invalid empty role specified for context %s")
    IllegalArgumentException invalidEmptyRole(String path);

    @Message(id = 232, value = "Cannot forward after response has been committed")
    IllegalStateException cannotForwardAfterCommit();

    @Message(id = 233, value = "Exception sending request initialized lifecycle event to listener instance of class %s")
    String requestListenerInitException(String className);

    @Message(id = 234, value = "Servlet %s is currently unavailable")
    String servletIsUnavailable(String wrapperName);

    @Message(id = 235, value = "Allocate exception for servlet %s")
    String servletAllocateException(String wrapperName);

    @Message(id = 236, value = "Servlet.service() for servlet %s threw exception")
    String servletServiceException(String wrapperName);

    @Message(id = 237, value = "Deallocate exception for servlet %s")
    String servletDeallocateException(String wrapperName);

    @Message(id = 238, value = "Exception sending request destroyed lifecycle event to listener instance of class %s")
    String requestListenerDestroyException(String className);

    @Message(id = 239, value = "Original SevletRequest or wrapped original ServletRequest not passed to RequestDispatcher in violation of SRV.8.2 and SRV.14.2.5.1")
    String notOriginalRequestInDispatcher();

    @Message(id = 240, value = "Original SevletResponse or wrapped original ServletResponse not passed to RequestDispatcher in violation of SRV.8.2 and SRV.14.2.5.1")
    String notOriginalResponseInDispatcher();

    @Message(id = 241, value = "Context %s has already been initialized")
    IllegalStateException cannotAddFilterRegistrationAfterInit(String contextName);

    @Message(id = 242, value = "Illegal null or empty argument specified")
    IllegalArgumentException invalidFilterRegistrationArguments();

    @Message(id = 243, value = "Context %s has already been initialized")
    IllegalStateException cannotAddServletRegistrationAfterInit(String contextName);

    @Message(id = 244, value = "Illegal null or empty argument specified")
    IllegalArgumentException invalidServletRegistrationArguments();

    @Message(id = 245, value = "Error destroying filter %s")
    String errorDestroyingFilter(String filterName);

    @Message(id = 246, value = "Exception processing component pre destroy")
    String preDestroyException();

    @Message(id = 247, value = "Filter execution threw an exception")
    String filterException();

    @Message(id = 248, value = "Servlet execution threw an exception")
    String servletException();

    @Message(id = 249, value = "Child container name cannot be null")
    IllegalArgumentException containerChildWithNullName();

    @Message(id = 250, value = "Child container with name %s already exists")
    IllegalArgumentException containerChildNameNotUnique(String name);

    @Message(id = 251, value = "Failed to start child container %s")
    IllegalStateException containerChildStartFailed(String name, @Cause Throwable t);

    @Message(id = 252, value = "Child of an Engine must be a Host")
    IllegalArgumentException engineChildMustBeHost();

    @Message(id = 253, value = "Engine cannot have a parent Container")
    IllegalArgumentException engineHasNoParent();

    @Message(id = 254, value = "Host name is required")
    IllegalArgumentException hostNameIsNull();

    @Message(id = 255, value = "Child of a Host must be a Context")
    IllegalArgumentException hostChildMustBeContext();

    @Message(id = 256, value = "Parent of a Wrapper must be a Context")
    IllegalArgumentException wrapperParentMustBeContext();

    @Message(id = 257, value = "A Wrapper cannot have a child container")
    IllegalArgumentException wrapperHasNoChild();

    @Message(id = 258, value = "Cannot allocate servlet %s because it is being unloaded")
    String cannotAllocateServletWhileUnloading(String name);

    @Message(id = 259, value = "Error allocating a servlet instance")
    String cannotAllocateServletInstance();

    @Message(id = 260, value = "No servlet class has been specified for servlet %s")
    String noClassSpecifiedForServlet(String name);

    @Message(id = 261, value = "Class %s is not a Servlet")
    String specifiedClassIsNotServlet(String className);

    @Message(id = 262, value = "Error instantiating servlet class %s")
    String errorInstantiatingServletClass(String className);

    @Message(id = 263, value = "Servlet.init() for servlet %s threw exception")
    String errorInitializingServlet(String name);

    @Message(id = 264, value = "Marking servlet %s as unavailable")
    String markingServletUnavailable(String name);

    @Message(id = 265, value = "Servlet.destroy() for servlet %s threw exception")
    String errorDestroyingServlet(String name);

    @Message(id = 266, value = "Servlet %s threw unload exception")
    String errorUnloadingServlet(String name);

    @Message(id = 267, value = "LoginConfig cannot be null")
    IllegalArgumentException nullLoginConfig();

    @Message(id = 268, value = "Form login page %s must start with a ''/'")
    IllegalArgumentException loginPageMustStartWithSlash(String loginPage);

    @Message(id = 269, value = "Error page location %s must start with a ''/''")
    IllegalArgumentException errorPageMustStartWithSlash(String errorPage);

    @Message(id = 270, value = "%s is not a subclass of StandardWrapper")
    IllegalArgumentException invalidWrapperClass(String wrapperClass);

    @Message(id = 271, value = "Cannot set resources after Context startup")
    IllegalStateException cannotSetResourcesAfterStart();

    @Message(id = 272, value = "Child of a Context must be a Wrapper")
    IllegalArgumentException contextChildMustBeWrapper();

    @Message(id = 273, value = "JSP file %s must start with a ''/''")
    IllegalArgumentException jspFileMustStartWithSlash(String jspFile);

    @Message(id = 274, value = "Invalid <url-pattern> %s in security constraint")
    IllegalArgumentException invalidSecurityConstraintUrlPattern(String urlPattern);

    @Message(id = 275, value = "ErrorPage cannot be null")
    IllegalArgumentException nullErrorPage();

    @Message(id = 276, value = "Filter mapping specifies an unknown filter name %s")
    IllegalArgumentException unknownFilterNameInMapping(String filterName);

    @Message(id = 277, value = "Filter mapping must specify either a <url-pattern> or a <servlet-name>")
    IllegalArgumentException missingFilterMapping();

    @Message(id = 278, value = "Invalid <url-pattern> %s in filter mapping")
    IllegalArgumentException invalidFilterMappingUrlPattern(String urlPattern);

    @Message(id = 279, value = "Both parameter name and parameter value are required")
    IllegalArgumentException missingParameterNameOrValue();

    @Message(id = 280, value = "Duplicate context initialization parameter %s")
    IllegalArgumentException duplicateContextParameters(String name);

    @Message(id = 281, value = "Servlet mapping specifies an unknown Servlet name %s")
    IllegalArgumentException unknownServletNameInMapping(String servletName);

    @Message(id = 282, value = "Invalid <url-pattern> %s in Servlet mapping")
    IllegalArgumentException invalidServletMappingUrlPattern(String urlPattern);

    @Message(id = 283, value = "Create wrapper failed")
    IllegalStateException errorCreatingWrapper(@Cause Throwable t);

    @Message(id = 284, value = "Exception starting filter %s")
    String errorStartingFilter(String filterName);

    @Message(id = 285, value = "Error configuring application listener of class %s")
    String errorInstatiatingApplicationListener(String listenerClass);

    @Message(id = 286, value = "Skipped installing application listeners due to previous error(s)")
    String skippingApplicationListener();

    @Message(id = 287, value = "Exception sending context initialized event to listener instance of class %s")
    String errorSendingContextInitializedEvent(String listenerClass);

    @Message(id = 288, value = "Error destroying application listener of class %s")
    String errorDestroyingApplicationListener(String listenerClass);

    @Message(id = 289, value = "Servlet %s threw load() exception")
    String errorLoadingServlet(String servletName);

    @Message(id = 290, value = "Error initializing context")
    String errorInitializingContext();

    @Message(id = 291, value = "No Context was mapped to process this request")
    String noContext();

    @Message(id = 292, value = "Syntax error in IP filter pattern %s")
    IllegalArgumentException remoteIpValvePatternError(String pattern, @Cause Throwable t);

    @Message(id = 293, value = "No host [%s] mapped")
    String noHost(String host);

    @Message(id = 294, value = "Servlet %s does not have any instance support")
    IllegalStateException missingInstanceSupport(String servletName);

    @Message(id = 295, value = "This application is not currently available")
    String unavailable();

    @Message(id = 296, value = "Error acknowledging request for Servlet %s")
    String errorAcknowledgingRequest(String servletName);

    @Message(id = 297, value = "Async listener processing for servlet %s threw exception")
    String asyncListenerError(String servletName);

    @Message(id = 298, value = "Async runnable processing for servlet %s threw exception")
    String asyncRunnableError(String servletName);

    @Message(id = 299, value = "Async dispatch processing for servlet %s threw exception")
    String asyncDispatchError(String servletName);

    @Message(id = 300, value = "Loading Session %s from file %s")
    String jdbcStoreSessionLoad(String sessionId, String table);

    @Message(id = 301, value = "Saving Session %s to file %s")
    String jdbcStoreSessionSave(String sessionId, String table);

    @Message(id = 302, value = "Removing Session %s at file %s")
    String jdbcStoreSessionRemove(String sessionId, String table);

    @Message(id = 303, value = "No persisted data object found")
    String jdbcStoreIdNotFound();

    @Message(id = 304, value = "The database connection is null or was found to be closed. Trying to re-open it.")
    String jdbcStoreConnectionWasClosed();

    @Message(id = 305, value = "The re-open on the database failed. The database could be down.")
    String jdbcStoreConnectionReopenFailed();

    @Message(id = 306, value = "Exception sending context destroyed event to listener instance of class %s")
    String errorSendingContextDestroyedEvent(String listenerClass);

    @Message(id = 307, value = "Invalid duration unit (years|months|weeks|days|hours|minutes|seconds) %s in directive %s")
    IllegalStateException expiresInvalidDurationUnit(String token, String line);

    @Message(id = 308, value = "Request filter invalid pattern %s")
    IllegalArgumentException requestFilterInvalidPattern(String pattern, @Cause Throwable t);

    @Message(id = 309, value = "type")
    String statusType();

    @Message(id = 310, value = "Null attribute name")
    IllegalArgumentException invalidNullAttributeName();

    @Message(id = 311, value = "Null attribute")
    IllegalArgumentException invalidNullAttribute();

    @Message(id = 312, value = "Invalid negative read ahead %s specified")
    IllegalArgumentException invalidReadAhead(int readAhead);

    @Message(id = 313, value = "Exception processing error page %s")
    String errorProcessingErrorPage(String location);

    @Message(id = 314, value = "Exception parsing number %s (zero based) of comma delimited list %s")
    IllegalArgumentException invalidNumberInList(int number, String list);

    @Message(id = 315, value = "Invalid HTTP port number specified %s")
    IllegalArgumentException invalidHttpPortNumber(String port);

    @Message(id = 316, value = "Invalid HTTPS port number specified %s")
    IllegalArgumentException invalidHttpsPortNumber(String port);

    @Message(id = 317, value = "WebDAV client problem: XP-x64-SP2 clients only work with the root context")
    String webDavClientProblemXpRootOnly();

    @Message(id = 318, value = "WebDAV client problem: XP-x64-SP2 is known not to work with WebDAV Servlet")
    String webDavClientProblemXp();

    @Message(id = 319, value = "Unknown mode %s, must be one of: strict, authOnly, strictAuthOnly")
    IllegalArgumentException unknownAuthMode(String authMode);

    @Message(id = 320, value = "Running CGI: bad header line: %s")
    String cgiInvalidHeader(String header);

    @Message(id = 321, value = "Error running CGI: %s")
    String cgiException(String errorMessage);

    @Message(id = 322, value = "Error closing header reader: %s")
    String cgiExceptionClosingHeaderReader(String errorMessage);

    @Message(id = 323, value = "Error closing output stream: %s")
    String cgiExceptionClosingOutputStream(String errorMessage);

    @Message(id = 324, value = "Interrupted waiting for stderr reader thread")
    String cgiInterrupted();

    @Message(id = 325, value = "Invalid HTTP status line: %s")
    String cgiInvalidStatusLine(String line);

    @Message(id = 326, value = "Invalid status code: %s")
    String cgiInvalidStatusCode(String code);

    @Message(id = 327, value = "Invalid status value: %s")
    String cgiInvalidStatusValue(String value);

    @Message(id = 328, value = "CGI stderr: %s")
    String cgiErrorLogPrefix(String line);

    @Message(id = 329, value = "Error reading error reader")
    String cgiStderrErrror();

    @Message(id = 330, value = "XSL transformer error")
    String xslTransformerError();

    @Message(id = 331, value = "Session %s expires processing failed")
    String errorProcessingSessionExpires(String sessionId);

    @Message(id = 332, value = "Error removing key %s")
    String errorRemovingKey(String sessionId);

    @Message(id = 333, value = "No nodes created")
    String ssiParseNoNodes();

    @Message(id = 334, value = "Extra nodes created")
    String ssiParseExtraNodes();

    @Message(id = 335, value = "Unused opp nodes exist")
    String ssiParseUnusedNodes();

    @Message(id = 336, value = "Couldn't exec file: %s")
    String ssiExecFailed(String file);

    @Message(id = 337, value = "#%s--Invalid attribute: %s")
    String ssiInvalidAttribute(String directive, String attribute);

    @Message(id = 338, value = "#flastmod--Couldn't get last modified for file: %s")
    String ssiFlastmodFailed(String file);

    @Message(id = 339, value = "#fsize--Couldn't get size for file: %s")
    String ssiFsizeFailed(String file);

    @Message(id = 340, value = "Num chars can't be negative")
    IllegalArgumentException invalidNumChars();

    @Message(id = 341, value = "#include--Couldn't include file: %s")
    String ssiIncludeFailed(String file);

    @Message(id = 342, value = "#echo--Invalid encoding: %s")
    String ssiEchoInvlidEncoding(String encoding);

    @Message(id = 343, value = "[an error occurred while processing this directive]")
    String ssiDirectiveError();

    @Message(id = 344, value = "Unknown encoding: %s")
    String ssiUnknownEncoding(String encoding);

    @Message(id = 345, value = "Unknown command: %s")
    String ssiUnknownCommand(String command);

    @Message(id = 346, value = "Error parsing command %s parameters")
    String ssiParsingErrorNoParameters(String directive);

    @Message(id = 347, value = "Parameter names count does not match parameter values count on command %s")
    String ssiParsingErrorBadParameterCount(String directive);

    @Message(id = 348, value = "#set--no variable specified")
    String ssiSetFailed();

    @Message(id = 349, value = "Couldn't remove filename from path: %s")
    String ssiFailedRemovingFilename(String path);

    @Message(id = 350, value = "Normalization yielded null on path: %s")
    String ssiFailedNormalization(String path);

    @Message(id = 351, value = "A non-virtual path can't be absolute: %s")
    String ssiInvalidNonVirtualPath(String path);

    @Message(id = 352, value = "A non-virtual path can't contain '../': %s")
    String ssiInvalidNonVirtualPathWithTraversal(String path);

    @Message(id = 353, value = "Couldn't get context for path: %s")
    String ssiCannotGetContext(String path);

    @Message(id = 354, value = "Couldn't remove context from path: %s")
    String ssiCannotRemoveContext(String path);

    @Message(id = 355, value = "Couldn't get request dispatcher for path: %s")
    String ssiCannotGetRequestDispatcher(String path);

    @Message(id = 356, value = "Couldn't find file: %s")
    String ssiCannotFindFile(String path);

    @Message(id = 357, value = "Couldn't include file: %s")
    String ssiServletIncludeFailed(String path);

    @Message(id = 358, value = "Message digest non initialized")
    IllegalStateException uninitializedMessageDigest();

    @Message(id = 359, value = "Exception releasing filter %s")
    String errorStoppingFilter(String filterName);

    @Message(id = 360, value = "Unable to cast to the internal request class in order to complete HTTP upgrade")
    String errorUpgrading();

    @Message(id = 361, value = "The buffer is not big enough to contain the message currently being processed")
    String bufferTooSmall();

    @Message(id = 362, value = "The client frame was not masked but all client frames must be masked")
    String frameNotMasked();

    @Message(id = 363, value = "A sequence of bytes was received that did not represent valid UTF-8")
    String frameInvalidUtf8();

    @Message(id = 364, value = "The end of the stream was reached when trying to read the first byte of a new WebSocket frame")
    String frameEos();

    @Message(id = 365, value = "Failed to read the first byte of the next WebSocket frame. The return value from the read was [%s]")
    String frameFailedRead(int value);

    @Message(id = 366, value = "An attempt was made to access the request object passed to WebSocketServlet.createWebSocketInbound() outside of that method")
    IllegalStateException invalidWrapper();

    @Message(id = 367, value = "A frame with the unrecognized OpCode [%s] was received")
    String frameUnknownOpcode(byte opcode);

    @Message(id = 368, value = "A frame with the OpCode [%s] was received when a continuation frame was expected")
    String frameNotContinuation(byte opcode);

    @Message(id = 369, value = "The WebSocket connection has been closed")
    String outboundClosed();

}
