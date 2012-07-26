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

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;

/**
 * Logging IDs 2000-3000
 * @author remm
 */
@MessageBundle(projectCode = "JBWEB")
public interface CoyoteMessages {

    /**
     * The messages
     */
    CoyoteMessages MESSAGES = Messages.getBundle(CoyoteMessages.class);

    @Message(id = 2000, value = "Alias name %s does not identify a key entry")
    String noKeyAlias(String alias);

    @Message(id = 2001, value = "SSL configuration is invalid due to %s")
    String invalidSSLConfiguration(String message);

    @Message(id = 2002, value = "Socket bind failed: [%s] %s")
    Exception socketBindFailed(int code, String message);

    @Message(id = 2003, value = "Socket listen failed: [%s] %s")
    Exception socketListenFailed(int code, String message);

    @Message(id = 2004, value = "More than the maximum number of request parameters (GET plus POST) for a single request (%s) were detected. Any parameters beyond this limit have been ignored. To change this limit, set the maxParameterCount attribute on the Connector.")
    IllegalStateException maxParametersFail(int limit);

    @Message(id = 2005, value = "Header count exceeded allowed maximum [%s]")
    IllegalStateException maxHeadersFail(int limit);

    @Message(id = 2006, value = "Odd number of hexadecimal digits")
    IllegalStateException hexaOdd();

    @Message(id = 2007, value = "Bad hexadecimal digit")
    IllegalStateException hexaBad();

    @Message(id = 2008, value = "EOF while decoding UTF-8")
    String utf8DecodingEof();

    @Message(id = 2009, value = "UTF-8 decoding failure, byte sequence [%s, %s, %s, %s]")
    String utf8DecodingFailure(int b0, int b1, int b2, int b3);

    @Message(id = 2010, value = "UTF-8 decoding failure, byte sequence [%s, %s, %s]")
    String utf8DecodingFailure(int b0, int b1, int b2);

    @Message(id = 2011, value = "Socket read failed")
    String failedRead();

    @Message(id = 2012, value = "Socket write failed")
    String failedWrite();

    @Message(id = 2013, value = "Invalid message received")
    String invalidAjpMessage();

    @Message(id = 2014, value = "Unexpected EOF read on the socket")
    String eofError();

    @Message(id = 2015, value = "Request header is too large")
    IllegalArgumentException requestHeaderTooLarge();

    @Message(id = 2016, value = "Backlog is present")
    String invalidBacklog();

}
