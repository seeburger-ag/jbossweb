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

    @Message(id = 21, value = "Missing MD5 digest")
    IllegalArgumentException noMD5Digest(@Cause NoSuchAlgorithmException e);

    @Message(id = 22, value = "The client may continue (%s).")
    String http100(String resource);

    @Message(id = 23, value = "The server is switching protocols according to the 'Upgrade' header (%s).")
    String http101(String resource);

    @Message(id = 24, value = "The request succeeded and a new resource (%s) has been created on the server.")
    String http201(String resource);

    @Message(id = 25, value = "This request was accepted for processing, but has not been completed (%s).")
    String http202(String resource);

    @Message(id = 26, value = "The meta information presented by the client did not originate from the server (%s).")
    String http203(String resource);

    @Message(id = 27, value = "The request succeeded but there is no information to return (%s).")
    String http204(String resource);

    @Message(id = 28, value = "The client should reset the document view which caused this request to be sent (%s).")
    String http205(String resource);

    @Message(id = 29, value = "The server has fulfilled a partial GET request for this resource (%s).")
    String http206(String resource);

    @Message(id = 30, value = "Multiple status values have been returned (%s).")
    String http207(String resource);

    @Message(id = 31, value = "The requested resource (%s) corresponds to any one of a set of representations, each with its own specific location.")
    String http300(String resource);

    @Message(id = 32, value = "The requested resource (%s) has moved permanently to a new location.")
    String http301(String resource);

    @Message(id = 33, value = "The requested resource (%s) has moved temporarily to a new location.")
    String http302(String resource);

    @Message(id = 34, value = "The response to this request can be found under a different URI (%s).")
    String http303(String resource);

    @Message(id = 35, value = "The requested resource (%s) is available and has not been modified.")
    String http304(String resource);

    @Message(id = 36, value = "The requested resource (%s) must be accessed through the proxy given by the 'Location' header.")
    String http305(String resource);

    @Message(id = 37, value = "The request sent by the client was syntactically incorrect (%s).")
    String http400(String resource);

    @Message(id = 38, value = "This request requires HTTP authentication (%s).")
    String http401(String resource);

    @Message(id = 39, value = "Payment is required for access to this resource (%s).")
    String http402(String resource);

    @Message(id = 40, value = "Access to the specified resource (%s) has been forbidden.")
    String http403(String resource);

    @Message(id = 41, value = "The requested resource (%s) is not available.")
    String http404(String resource);

    @Message(id = 42, value = "The specified HTTP method is not allowed for the requested resource (%s).")
    String http405(String resource);

    @Message(id = 43, value = "The resource identified by this request is only capable of generating responses with characteristics not acceptable according to the request 'Accept' headers (%s).")
    String http406(String resource);

    @Message(id = 44, value = "The client must first authenticate itself with the proxy (%s).")
    String http407(String resource);

    @Message(id = 45, value = "The client did not produce a request within the time that the server was prepared to wait (%s).")
    String http408(String resource);

    @Message(id = 46, value = "The request could not be completed due to a conflict with the current state of the resource (%s).")
    String http409(String resource);

    @Message(id = 47, value = "The requested resource (%s) is no longer available, and no forwarding address is known.")
    String http410(String resource);

    @Message(id = 48, value = "This request cannot be handled without a defined content length (%s).")
    String http411(String resource);

    @Message(id = 49, value = "A specified precondition has failed for this request (%s).")
    String http412(String resource);

    @Message(id = 50, value = "The request entity is larger than the server is willing or able to process (%s).")
    String http413(String resource);

    @Message(id = 51, value = "The server refused this request because the request URI was too long (%s).")
    String http414(String resource);

    @Message(id = 52, value = "The server refused this request because the request entity is in a format not supported by the requested resource for the requested method (%s).")
    String http415(String resource);

    @Message(id = 53, value = "The requested byte range cannot be satisfied (%s).")
    String http416(String resource);

    @Message(id = 54, value = "The expectation given in the 'Expect' request header (%s) could not be fulfilled.")
    String http417(String resource);

    @Message(id = 55, value = "The server understood the content type and syntax of the request but was unable to process the contained instructions (%s).")
    String http422(String resource);

    @Message(id = 56, value = "The source or destination resource of a method is locked (%s).")
    String http423(String resource);

    @Message(id = 57, value = "The server encountered an internal error (%s) that prevented it from fulfilling this request.")
    String http500(String resource);

    @Message(id = 58, value = "The server does not support the functionality needed to fulfill this request (%s).")
    String http501(String resource);

    @Message(id = 59, value = "This server received an invalid response from a server it consulted when acting as a proxy or gateway (%s).")
    String http502(String resource);

    @Message(id = 60, value = "The requested service (%s) is not currently available.")
    String http503(String resource);

    @Message(id = 61, value = "The server received a timeout from an upstream server while acting as a gateway or proxy (%s).")
    String http504(String resource);

    @Message(id = 62, value = "The server does not support the requested HTTP protocol version (%s).")
    String http505(String resource);

    @Message(id = 63, value = "The resource does not have sufficient space to record the state of the resource after execution of this method (%s).")
    String http507(String resource);

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

}
