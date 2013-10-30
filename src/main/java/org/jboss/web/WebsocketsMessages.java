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

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;

/**
 * Logging IDs 8500-8850
 * @author Remy Maucherat
 */
@MessageBundle(projectCode = "JBWEB")
public interface WebsocketsMessages {

    /**
     * The messages
     */
    WebsocketsMessages MESSAGES = Messages.getBundle(WebsocketsMessages.class);

    @Message(id = 8500, value = "Concurrent read operations are not permitted")
    IllegalStateException invalidConcurrentRead();

    @Message(id = 8501, value = "Concurrent write operations are not permitted")
    IllegalStateException invalidConcurrentWrite();

    @Message(id = 8502, value = "Unexpected Status of SSLEngineResult after a wrap() operation")
    IllegalStateException unexpectedStatusAfterWrap();

    @Message(id = 8503, value = "Flag that indicates a write is in progress was found to be false (it should have been true) when trying to complete a write operation")
    IllegalStateException invalidWriteState();

    @Message(id = 8504, value = "Unexpected end of stream")
    String unexpectedEndOfStream();

    @Message(id = 8505, value = "Flag that indicates a read is in progress was found to be false (it should have been true) when trying to complete a read operation")
    IllegalStateException invalidReadState();

    @Message(id = 8506, value = "Unexpected Status of SSLEngineResult after an unwrap() operation")
    IllegalStateException unexpectedStatusAfterUnwrap();

    @Message(id = 8507, value = "The result [%s] is too big to be expressed as an Integer")
    String notAnInteger(long value);

    @Message(id = 8508, value = "Unable to coerce value [%s] to type [%s]. That type is not supported.")
    IllegalArgumentException invalidType(String value, String type);

    @Message(id = 8509, value = "The specified decoder of type [%s] could not be instantiated")
    String cannotInstatiateDecoder(String className);

    @Message(id = 8510, value = "Unable to add the message handler [%s] as it was for the unrecognised type [%s]")
    IllegalArgumentException unknownHandler(Object listener, Object target);

    @Message(id = 8511, value = "The message handler provided does not have an onMessage(Object) method")
    IllegalArgumentException invalidMessageHandler(@Cause Throwable t);

    @Message(id = 8512, value = "The Decoder type [%s] is not recognized")
    IllegalArgumentException unknownDecoderType(String className);

    @Message(id = 8513, value = "New frame received after a close control frame")
    String receivedFrameAfterClose();

    @Message(id = 8514, value = "The client frame set the reserved bits to [%s] which was not supported by this endpoint")
    String unsupportedReservedBitsSet(int bit);

    @Message(id = 8515, value = "A fragmented control frame was received but control frames may not be fragmented")
    String invalidFragmentedControlFrame();

    @Message(id = 8516, value = "A WebSocket frame was sent with an unrecognised opCode of [%s]")
    String invalidFrameOpcode(int code);

    @Message(id = 8517, value = "A new message was started when a continuation frame was expected")
    String noContinuationFrame();

    @Message(id = 8518, value = "The client data can not be processed because the session has already been closed")
    String sessionClosed();

    @Message(id = 8519, value = "The client frame was not masked but all client frames must be masked")
    String frameWithoutMask();

    @Message(id = 8520, value = "A control frame was sent with a payload of size [%s] which is larger than the maximum permitted of 125 bytes")
    String controlFramePayloadTooLarge(long size);

    @Message(id = 8521, value = "A control frame was sent that did not have the fin bit set. Control frames are not permitted to use continuation frames.")
    String controlFrameWithoutFin();

    @Message(id = 8522, value = "The client sent a close frame with a single byte payload which is not valid")
    String invalidOneByteClose();

    @Message(id = 8523, value = "A WebSocket close frame was received with a close reason that contained invalid UTF-8 byte sequences")
    String invalidUtf8Close();

    @Message(id = 8524, value = "The message was [%s] bytes long but the MessageHandler has a limit of [%s] bytes")
    String messageTooLarge(long size, long limit);

    @Message(id = 8525, value = "A WebSocket text frame was received that could not be decoded to UTF-8 because it contained invalid byte sequences")
    String invalidUtf8();

    @Message(id = 8526, value = "The decoded text message was too big for the output buffer and the endpoint does not support partial messages")
    String textMessageTooLarge();

    @Message(id = 8527, value = "No async message support and buffer too small. Buffer size: [%s], Message size: [%s]")
    String bufferTooSmall(int capacity, long payload);

    @Message(id = 8528, value = "Too many bytes ([%s]) were provided to be converted into a long")
    String invalidLong(long length);

    @Message(id = 8529, value = "Message will not be sent because the WebSocket session is currently sending another message")
    IllegalStateException messageInProgress();

    @Message(id = 8530, value = "Message will not be sent because the WebSocket session has been closed")
    IllegalStateException messageSessionClosed();

    @Message(id = 8531, value = "When sending a fragmented message, all fragments bust be of the same type")
    IllegalStateException messageFragmentTypeChange();

    @Message(id = 8532, value = "No encoder specified for object of class [%s]")
    String noEncoderForClass(String className);

    @Message(id = 8533, value = "The specified encoder of type [%s] could not be instantiated")
    String cannotInstatiateEncoder(String className);

    @Message(id = 8534, value = "This method may not be called as the OutputStream has been closed")
    IllegalStateException closedOutputStream();

    @Message(id = 8535, value = "This method may not be called as the Writer has been closed")
    IllegalStateException closedWriter();

    @Message(id = 8536, value = "A text message handler has already been configured")
    IllegalStateException duplicateHandlerText();

    @Message(id = 8537, value = "A binary message handler has already been configured")
    IllegalStateException duplicateHandlerBinary();

    @Message(id = 8538, value = "A pong message handler has already been configured")
    IllegalStateException duplicateHandlerPong();

    @Message(id = 8539, value = "A pong message handler must implement MessageHandler.Basic")
    IllegalStateException invalidHandlerPong();

    @Message(id = 8540, value = "Unable to add the message handler [%s] as it was wrapped as the unrecognised type [%s]")
    IllegalArgumentException invalidMessageHandler(Object listener, Object type);

    @Message(id = 8541, value = "Unable to remove the handler [%s] as it was not registered with this session")
    IllegalStateException cannotRemoveHandler(Object listener);

    @Message(id = 8542, value = "Unable to write the complete message as the WebSocket connection has been closed")
    String messageFailed();

    @Message(id = 8543, value = "The WebSocket session timeout expired")
    String sessionTimeout();

    @Message(id = 8544, value = "The WebSocket session has been closed and no method (apart from close()) may be called on a closed session")
    IllegalStateException sessionAlreadyClosed();

    @Message(id = 8545, value = "Unable to create dedicated AsynchronousChannelGroup for WebSocket clients which is required to prevent memory leaks in complex class loader environments like J2EE containers")
    IllegalStateException asyncGroupFail();

    @Message(id = 8546, value = "Cannot use POJO class [%s] as it is not annotated with @ClientEndpoint")
    String missingClientEndpointAnnotation(String className);

    @Message(id = 8547, value = "Failed to create the default configurator")
    String defaultConfiguratorFailed();

    @Message(id = 8548, value = "Failed to create a local endpoint of type [%s]")
    String endpointCreateFailed(String className);

    @Message(id = 8549, value = "The scheme [%s] is not supported")
    String pathWrongScheme(String scheme);

    @Message(id = 8550, value = "No host was specified in URI")
    String pathNoHost();

    @Message(id = 8551, value = "The requested scheme, [%s], is not supported. The supported schemes are ws and wss")
    String invalidScheme(String scheme);

    @Message(id = 8552, value = "Unable to open a connection to the server")
    String connectionFailed();

    @Message(id = 8553, value = "The HTTP request to initiate the WebSocket connection failed")
    String httpRequestFailed();

    @Message(id = 8554, value = "Invalid websockets protocol header")
    String invalidProtocolHeader();

    @Message(id = 8555, value = "The HTTP response from the server [%s] did not permit the HTTP upgrade to WebSocket")
    String invalidHttpStatus(String line);

    @Message(id = 8556, value = "Unable to create SSLEngine to support SSL/TLS connections")
    String sslEngineFail();

    @Message(id = 8557, value = "The web application is stopping")
    String webappStopping();

    @Message(id = 8558, value = "Failed to create instance of POJO of type [%s]")
    IllegalArgumentException pojoInstanceFailed(String className, @Cause Throwable t);

    @Message(id = 8559, value = "IO error while decoding message")
    String errorDecodingMessage();

    @Message(id = 8560, value = "Duplicate annotations [%s] present on class [%s]")
    String duplicateAnnotations(Class<?> annotation, Class<?> clazz);

    @Message(id = 8561, value = "The annotated method [%s] is not public")
    String methodNotPublic(String method);

    @Message(id = 8562, value = "Parameters annotated with @PathParam may only be Strings, Java primitives or a boxed version thereof")
    String invalidPathParamType();

    @Message(id = 8563, value = "A parameter of type [%s] was found on method[%s] of class [%s] that did not have a @PathParam annotation")
    String pathParamWithoutAnnotation(Class<?> clazz, String method, String className);

    @Message(id = 8564, value = "No Throwable parameter was present on the method [%s] of class [%s] that was annotated with OnError")
    String onErrorWithoutThrowable(String method, String className);

    @Message(id = 8565, value = "Failed to decode path parameter value [%s] to expected type [%s]")
    String errorDecodingPathParam(String value, Class<?> clazz);

    @Message(id = 8566, value = "Multiple message parameters present on the method [%s] of class [%s] that was annotated with OnMessage")
    IllegalArgumentException duplicateMessageParameter(String method, String className);

    @Message(id = 8567, value = "Multiple boolean (last) parameters present on the method [%s] of class [%s] that was annotated with OnMessage")
    IllegalArgumentException duplicateLastMessageParameter(String method, String className);

    @Message(id = 8568, value = "Multiple session parameters present on the method [%s] of class [%s] that was annotated with OnMessage")
    IllegalArgumentException duplicateSessionParameter(String method, String className);

    @Message(id = 8569, value = "Multiple PongMessage parameters present on the method [%s] of class [%s] that was annotated with OnMessage")
    IllegalArgumentException duplicatePongMessageParameter(String method, String className);

    @Message(id = 8570, value = "Invalid PongMessgae and Message parameters present on the method [%s] of class [%s] that was annotated with OnMessage")
    IllegalArgumentException invalidPongWithPayload(String method, String className);

    @Message(id = 8571, value = "No payload parameter present on the method [%s] of class [%s] that was annotated with OnMessage")
    IllegalArgumentException missingPayload(String method, String className);

    @Message(id = 8572, value = "Invalid PongMesssge and boolean parameters present on the method [%s] of class [%s] that was annotated with OnMessage")
    IllegalArgumentException partialPong(String method, String className);

    @Message(id = 8573, value = "Invalid Reader and boolean parameters present on the method [%s] of class [%s] that was annotated with OnMessage")
    IllegalArgumentException partialReader(String method, String className);

    @Message(id = 8574, value = "Invalid InputStream and boolean parameters present on the method [%s] of class [%s] that was annotated with OnMessage")
    IllegalArgumentException partialInputStream(String method, String className);

    @Message(id = 8575, value = "Invalid Object and boolean parameters present on the method [%s] of class [%s] that was annotated with OnMessage")
    IllegalArgumentException partialObject(String method, String className);

    @Message(id = 8576, value = "The path [%s] is not valid.")
    String invalidPath(String path);

    @Message(id = 8577, value = "The path [%s] contains one or more empty segments which are is not permitted")
    IllegalArgumentException invalidEmptySegment(String path);

    @Message(id = 8578, value = "The parameter [%s] appears more than once in the path which is not permitted")
    IllegalArgumentException duplicateParameter(String path);

    @Message(id = 8579, value = "The segment [%s] is not valid in the provided path [%s]")
    IllegalArgumentException invalidPathSegment(String segment, String path);

    @Message(id = 8580, value = "The preInit() method must be called to configure the WebSocket HttpUpgradeHandler before the container calls init(). Usually, this means the Servlet that created the WsHttpUpgradeHandler instance should also call preInit()")
    IllegalStateException noPreInit();

    @Message(id = 8581, value = "No further Endpoints may be registered once an attempt has been made to use one of the previously registered endpoints")
    String addNotAllowed();

    @Message(id = 8582, value = "No ServletContext was specified")
    String missingServletContext();

    @Message(id = 8583, value = "Multiple Endpoints may not be deployed to using the same path [%s]")
    String duplicatePaths(String path);

    @Message(id = 8584, value = "Cannot deploy POJO class [%s] as it is not annotated with @ServerEndpoint")
    String cannotDeployPojo(String className);

    @Message(id = 8585, value = "Failed to create configurator of type [%s] for POJO of type [%s]")
    String configuratorFailed(String configurator, String className);

    @Message(id = 8586, value = "Upgrade failed")
    String upgradeFailed();

    @Message(id = 8587, value = "This connection was established under an authenticated HTTP session that has ended")
    String expiredHttpSession();

}
