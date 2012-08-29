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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;

/**
 * Logging IDs 1-1000
 * @author remm
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

    @Message(id = 51, value = "Error invoking onStartAsync on listener of class {0}")
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

    @Message(id = 60, value = "Stream closed")
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

    @Message(id = 115, value = "The response to this request can be found under a different URI (%s).")
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

}
