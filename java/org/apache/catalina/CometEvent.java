/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The CometEvent interface.
 * 
 * @author Filip Hanik
 * @author Remy Maucherat
 */
public interface CometEvent {

    /**
     * Enumeration describing the major events that the container can invoke 
     * the CometProcessor event() method with:
     * <ul>
     * <li>BEGIN - will be called at the beginning 
     *  of the processing of the connection. It can be used to initialize any relevant 
     *  fields using the request and response objects. Between the end of the processing 
     *  of this event, and the beginning of the processing of the end or error events,
     *  it is possible to use the response object to write data on the open connection.
     *  Note that the response object and depedent OutputStream and Writer are still 
     *  not synchronized, so when they are accessed by multiple threads, 
     *  synchronization is mandatory. After processing the initial event, the request 
     *  is considered to be committed.</li>
     * <li>READ - This indicates that input data is available, and that one read can be made
     *  without blocking. The available and ready methods of the InputStream or
     *  Reader may be used to determine if there is a risk of blocking: the servlet
     *  should read while data is reported available. When encountering a read error, 
     *  the servlet should report it by propagating the exception properly. Throwing 
     *  an exception will cause the error event to be invoked, and the connection 
     *  will be closed. 
     *  Alternately, it is also possible to catch any exception, perform clean up
     *  on any data structure the servlet may be using, and using the close method
     *  of the event. It is not allowed to attempt reading data from the request 
     *  object outside of the execution of this method.</li>
     * <li>END - End may be called to end the processing of the request. Fields that have
     *  been initialized in the begin method should be reset. After this event has
     *  been processed, the request and response objects, as well as all their dependent
     *  objects will be recycled and used to process other requests.</li>
     * <li>ERROR - Error will be called by the container in the case where an IO exception
     *  or a similar unrecoverable error occurs on the connection. Fields that have
     *  been initialized in the begin method should be reset. After this event has
     *  been processed, the request and response objects, as well as all their dependent
     *  objects will be recycled and used to process other requests.</li>
     * <li>EVENT - Event will be called by the container after the resume() method is called.
     *  This allows you get an event instantly, and you can perform IO actions
     *  or close the Comet connection.</li>
     * <li>WRITE - Write is sent if the servlet is using the ready method. This means that 
     *  the connection is ready to receive data to be written out.</li>
     * </ul>
     */
    public enum EventType {BEGIN, READ, END, ERROR, WRITE, EVENT}
    
    
    /**
     * Event details:
     * <ul>
     * <li>TIMEOUT - the connection timed out (sub type of ERROR); note that this ERROR type is not fatal, and
     *   the connection will not be closed unless the servlet uses the close method of the event</li>
     * <li>CLIENT_DISCONNECT - the client connection was closed (sub type of ERROR)</li>
     * <li>IOEXCEPTION - an IO exception occurred, such as invalid content, for example, an invalid chunk block (sub type of ERROR)</li>
     * <li>WEBAPP_RELOAD - the webapplication is being reloaded (sub type of END)</li>
     * <li>SERVER_SHUTDOWN - the server is shutting down (sub type of END)</li>
     * <li>SESSION_END - the servlet ended the session (sub type of END)</li>
     * </ul>
     */
    public enum EventSubType { TIMEOUT, CLIENT_DISCONNECT, IOEXCEPTION, WEBAPP_RELOAD, SERVER_SHUTDOWN, SESSION_END }
    
    
    /**
     * Returns the HttpServletRequest.
     * 
     * @return HttpServletRequest
     */
    public HttpServletRequest getHttpServletRequest();
    
    /**
     * Returns the HttpServletResponse.
     * 
     * @return HttpServletResponse
     */
    public HttpServletResponse getHttpServletResponse();
    
    /**
     * Returns the event type.
     * 
     * @return EventType
     * @see #EventType
     */
    public EventType getEventType();
    
    /**
     * Returns the sub type of this event.
     * 
     * @return EventSubType
     * @see #EventSubType
     */
    public EventSubType getEventSubType();

    /**
     * Ends the Comet session. This signals to the container that 
     * the container wants to end the comet session. This will send back to the
     * client a notice that the server has no more data to send as part of this
     * request. The servlet should perform any needed cleanup as if it had recieved
     * an END or ERROR event. 
     * 
     * @throws IOException if an IO exception occurs
     */
    public void close() throws IOException;

    /**
     * This method sets the timeout in milliseconds of idle time on the connection.
     * The timeout is reset every time data is received from the connection or data is flushed
     * using <code>response.flushBuffer()</code>. If a timeout occurs, the 
     * servlet will receive an ERROR/TIMEOUT event which will not result in automatically closing
     * the event (the event may be closed using the close() method).
     * 
     * @param timeout The timeout in milliseconds for this connection, must be a positive value, larger than 0
     */
    public void setTimeout(int timeout);

    /**
     * Returns true when data may be written to the connection (the flag becomes false 
     * when the client is unable to accept data fast enough). When the flag becomes false, 
     * the servlet must stop writing data. If there's an attempt to flush additional data 
     * to the client and data still cannot be written immediately, an IOException will be 
     * thrown. If calling this method returns false, it will also 
     * request notification when the connection becomes available for writing again, and the  
     * servlet will recieve a write event.<br/>
     * 
     * Note: If the servlet is not using ready, and is writing its output inside the
     * container threads, using this method is not mandatory, but any incomplete writes will be
     * performed again in blocking mode.
     * 
     * @return boolean true if you can write to the response 
     */
    public boolean ready();

    /**
     * Suspend processing of the connection until the configured timeout occurs, or resume() is called. In
     * parctice, this means the servlet will no longer recieve read events. Reading should always be
     * performed synchronously in the Tomcat threads unless the connection has been suspended.
     */
    public void suspend();

    /**
     * Will ask the servlet container to send a generic event to the servlet, where the request can be processed
     * synchronously (for example, it is possible to use this to complete the request after some asynchronous
     * processing is done). This also resumes read events if they had been disabled using suspend (it is possible
     * to call suspend again). It is possible to call resume without calling suspend before.
     */
    public void resume();

}

/**
 * Returns true if data is available to be read. If attempting to read and this flag is false, 
 * an IO exception will occur.
 * 
 * <!--Note: If the servlet is not using isReadable, and is reading data inside the
 * container threads, it is not needed to call this method. Any such read will be
 * performed again in blocking mode.-->
 * 
 * @see javax.servlet.ServletRequest#getInputStream()#available()>0
 * @return boolean
 */
//public boolean isReadable();
