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

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;

/**
 * Logging IDs 2000-3000
 * @author Remy Maucherat
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

    @Message(id = 2017, value = "Invalid CRLF, no CR character encountered")
    IOException invalidCrlfNoCr();

    @Message(id = 2018, value = "Invalid CRLF, two CR characters encountered")
    IOException invalidCrlfTwoCr();

    @Message(id = 2019, value = "Invalid CRLF")
    IOException invalidCrlf();

    @Message(id = 2020, value = "Invalid chunk header")
    IOException invalidChunkHeader();

    @Message(id = 2021, value = "Channel pattern must not be null")
    NullPointerException invalidNullChannelPattern();

    @Message(id = 2022, value = "Invalid message class, you can only publish messages created through the Bayeux.newMessage() method")
    IllegalArgumentException invalidMessagePublish();

    @Message(id = 2023, value = "Misconfigured server, must be configured to support Comet operations")
    String invalidBayeuxConfiguration();

    @Message(id = 2024, value = "No Bayeux message to send")
    String noBayeuxMessage();

    @Message(id = 2025, value = "Client doesn't support any appropriate connection type")
    String noBayeuxConnectionType();

    @Message(id = 2026, value = "Unable to fit %s bytes into the array. length:%s required length: %s")
    ArrayIndexOutOfBoundsException errorGeneratingUuid(int uuidLength, int destLength, int reqLength);

    @Message(id = 2027, value = "Invalid client id")
    String invalidBayeuxClientId();

    @Message(id = 2028, value = "Invalid handshake")
    String invalidBayeuxHandshake();

    @Message(id = 2029, value = "No Bayeux subscription")
    String noBayeuxSubscription();

    @Message(id = 2030, value = "Message data missing")
    String noBayeuxMessageData();

    @Message(id = 2031, value = "Invalid JSON object in data attribute")
    String invalidBayeuxMessageData();

    @Message(id = 2032, value = "Unsupported APR Version %s")
    UnsatisfiedLinkError unsupportedAprVersion(String version);

    @Message(id = 2033, value = "Missing APR threads support")
    UnsatisfiedLinkError missingAprThreadsSupport();

    @Message(id = 2034, value = "(Error on: ")
    String aprError();

    @Message(id = 2035, value = "Buffer length %s overflow with limit %s and no sink")
    String bufferOverflow(int length, int limit);

    @Message(id = 2036, value = "Unexpected EOF")
    String unexpectedEof();

    @Message(id = 2037, value = "Invalid HEX")
    String invalidHex();

    @Message(id = 2038, value = "Invalid slash")
    String invalidSlash();

    @Message(id = 2039, value = "Control character in cookie value or attribute")
    IllegalArgumentException invalidControlCharacter();

    @Message(id = 2040, value = "Invalid escape character in cookie value")
    IllegalArgumentException invalidEscapeCharacter();

    @Message(id = 2041, value = "SSL handshake error")
    String sslHandshakeError();

    @Message(id = 2042, value = "SSL handshake failed, cipher suite in SSL Session is SSL_NULL_WITH_NULL_NULL")
    String invalidSslCipherSuite();

    @Message(id = 2043, value = "Certificate revocation list is not supported for algorithm %s")
    String unsupportedCrl(String algorithm);

    @Message(id = 2044, value = "SSL handshake timeout")
    String sslHandshakeTimeout();

    @Message(id = 2045, value = "Null SSL engine")
    IllegalArgumentException nullSslEngine();

    @Message(id = 2046, value = "Operation not supported")
    RuntimeException operationNotSupported();

    @Message(id = 2047, value = "Null handler")
    IllegalArgumentException nullHandler();

    @Message(id = 2048, value = "Unable to unwrap data, invalid status %s")
    String errorUnwrappingData(String status);

    @Message(id = 2049, value = "Handshake incomplete, you must complete handshake before read/write data")
    IllegalStateException incompleteHandshake();

    @Message(id = 2050, value = "Error encountered during handshake unwrap")
    String errorUnwrappingHandshake();

    @Message(id = 2051, value = "Error encountered during handshake wrap")
    String errorWrappingHandshake();

    @Message(id = 2052, value = "Unexpected status %s during handshake wrap")
    String errorWrappingHandshakeStatus(String status);

    @Message(id = 2053, value = "NOT_HANDSHAKING during handshake")
    String notHandshaking();

    @Message(id = 2054, value = "Error loading SSL implementation %s")
    String errorLoadingSslImplementation(String className);

    @Message(id = 2055, value = "No SSL implementation")
    String noSslImplementation();

    @Message(id = 2056, value = "URL with no protocol: %s")
    String urlWithNoProtocol(String url);

    @Message(id = 2057, value = "Error processing URL: %s")
    String errorProcessingUrl(String cause);

    @Message(id = 2058, value = "Invalid relative URL reference")
    String invalidRelativeUrlReference();

    @Message(id = 2059, value = "Closing ']' not found in IPV6 address %s")
    String invalidIp6Address(String authority);

    @Message(id = 2060, value = "Invalid IP address %s: %s")
    String invalidIpAddress(String authority, String cause);

    @Message(id = 2061, value = "Base path does not start with '/'")
    String invalidBasePath();

    @Message(id = 2062, value = "Cannot process source: %s")
    IllegalStateException invalidSource(Object source);

    @Message(id = 2063, value = "Attribute name is null")
    String nullAttributeName();

    @Message(id = 2064, value = "Error getting attribute %s")
    String errorGettingAttribute(String name);

    @Message(id = 2065, value = "Attribute name list is null")
    String nullAttributeNameList();

    @Message(id = 2066, value = "Method name is null")
    String nullMethodName();

    @Message(id = 2067, value = "Error invoking method %s")
    String errorInvokingMethod(String name);

    @Message(id = 2068, value = "Attribute is null")
    String nullAttribute();

    @Message(id = 2069, value = "Error setting attribute %s")
    String errorSettingAttribute(String name);

    @Message(id = 2070, value = "Null managed resource")
    String nullManagedResource();

    @Message(id = 2071, value = "Null listener")
    String nullListener();

    @Message(id = 2072, value = "Null notification")
    String nullNotification();

    @Message(id = 2073, value = "Null message")
    String nullMessage();

    @Message(id = 2074, value = "Too many hooks registered %s")
    IllegalStateException tooManyHooks(int count);

    @Message(id = 2075, value = "Cannot load model MBean %s")
    String errorLoadingModelMbean(String className);

    @Message(id = 2076, value = "Cannot instantiate model MBean %s")
    String errorInstantiatingModelMbean(String className);

    @Message(id = 2077, value = "No host found: %s")
    IllegalStateException mapperHostNotFound(String hostName);

    @Message(id = 2078, value = "No context found: %s")
    IllegalStateException mapperContextNotFound(String contextPath);

}
