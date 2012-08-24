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

}
