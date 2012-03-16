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

package org.jboss.web.upgrade;

import java.io.IOException;

/**
 * The WebConnection object is provided by the web container and is used to allow
 * access to the raw streams of the socket following an upgrade from HTTP/1.1.
 * 
 * @author remm
 */
public interface WebConnection {

    /**
     * Returns true when data may be read from the connection (the flag becomes false if no data
     * is available to read). When the flag becomes false, the Servlet can attempt to read additional
     * data, but it will block until data is available. If calling this method returns false, it will also 
     * request notification when the connection has data available for reading again, and the  
     * Servlet will be called back using the inputAvailable method of the ProtocolHandler.
     * 
     * @return boolean true if data can be read without blocking
     */
    public boolean isReadReady();

    /**
     * Returns true when data may be written to the connection (the flag becomes false 
     * when the client is unable to accept data fast enough). When the flag becomes false, 
     * the Servlet must stop writing data. If there's an attempt to flush additional data 
     * to the client and data still cannot be written immediately, an IOException will be 
     * thrown. If calling this method returns false, it will also 
     * request notification when the connection becomes available for writing again, and the  
     * Servlet will be called back using the outputReady method of the ProtocolHandler.
     * <br>
     * Note: If the Servlet is not using isWriteReady, and is writing its output inside the
     * container threads (inside the ProtocolHandler.resume() method processing, for example), 
     * using this method is not mandatory, and writes will block until all bytes are written.
     * 
     * @return boolean true if data can be written without blocking
     */
    public boolean isWriteReady();

    /**
     * This method sets the timeout in milliseconds of idle time on the connection.
     * The timeout is reset every time data is received from the connection. If a timeout occurs, the 
     * Servlet will be called back using the ProtocolHandler.timeout method which will not result in 
     * automatically closing the connection (the connection may be closed using the close() method).
     * 
     * @param timeout The timeout in milliseconds for this connection, must be a positive value, larger than 0
     */
    public void setTimeout(int timeout);

    /**
     * Suspend processing of the connection until the configured timeout occurs, 
     * or resume() is called. In practice, this means the servlet will no longer 
     * receive read method callbacks. Reading should always be performed synchronously in 
     * the web container threads unless the connection has been suspended.
     */
    public void suspend();

    /**
     * Resume will cause the Servlet container to use the ProtocolHandler.resume()
     * method to call back the Servlet, where the request can be processed synchronously 
     * (for example, it is possible to use this to complete the request after 
     * some asynchronous processing is done). This also resumes read events 
     * if they have been disabled using suspend. It is then possible to call suspend 
     * again later. It is also possible to call resume without calling suspend before.
     * This method must be called asynchronously.
     */
    public void resume();

    /**
     * Close the connection. This will send back to the client a notice that the server 
     * has no more data to send as part of this request. The ProtocolHandler.close method will 
     * also be called by the container. This method will actually never perform any operation
     * synchronously, it will merely mark the connection for closing.
     */
    public void close();

    /**
     * Read data in the given byte array.
     */
    public int read(byte[] b, int off, int len)
            throws IOException;

    /**
     * Write the given bytes.
     */
    public void write(byte[] b, int off, int len)
            throws IOException;

}
