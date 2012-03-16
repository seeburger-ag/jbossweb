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

/**
 * Application can provide implementation of the ProtocolHandler interface to support
 * protocol upgrades from HTTP/1.1. This allow access to the raw streams of the underlying
 * connection, and full asynchronous IO for maximized scalability.
 * 
 * @author remm
 */
public interface ProtocolHandler {

    /**
     * Init will be called at the beginning of the processing of the upgraded connection. 
     * It can be used to initialize any relevant components that will be used for processing.
     * Between the end of the processing of this event, and the beginning of the processing 
     * of the end or error events, it is possible to use the WebConnection object instance 
     * to write data on the open connection. Note that the WebConnection object instance 
     * methods are not synchronized, so when they are accessed by multiple threads adequate
     * synchronization is needed. The WebConnection object will remain associated to the
     * same connection for its entire lifecycle until it is destroyed.
     * 
     * @param wc The WebConnection object that will be associated with this connection
     */
    public void init(WebConnection wc);

    /**
     * Destroy may be called to end the processing of the request. Components that have
     * been initialized in the init method should be reset. After this event has
     * been processed, the WebConnection object will be recycled and used to process 
     * other connections. In particular, this method will be called when the connection 
     * is closed asynchronously.
     * 
     * @param wc The WebConnection object that is associated with this connection
     */
    public void destroy(WebConnection wc);

    /**
     * This indicates that input data is available, and that at least one 
     * read can be made without blocking using the given WebConnection object. 
     * The isReadReady method of the WebConnection may be used to determine 
     * if there is a risk of blocking: the Servlet must continue reading while 
     * data is reported available. When encountering a read error, 
     * the Servlet should report it by propagating the exception properly. Throwing 
     * an exception will cause the error method to be invoked, and the connection 
     * will be closed.
     * Alternately, it is also possible to catch any exception, perform clean up
     * on any data structure the Servlet may be using, and using the close method.
     * It is not allowed to attempt reading data from the request 
     * object outside of the processing of this event, unless the suspend() method
     * has been used.
     * 
     * @param wc The WebConnection object that is associated with this connection
     */
    public void inputAvailable(WebConnection wc);

    /**
     * Write is called if the Servlet is using the isWriteReady method of the WebConnection.
     * This means that the connection is ready to receive data to be written out. This method
     * will never be called if the Servlet is not using the isWriteReady() method, or if the
     * isWriteReady() method always returns true.
     * 
     * @param wc The WebConnection object that is associated with this connection
     */
    public void outputReady(WebConnection wc);

    /**
     * Resume will be called by the container after the resume() method of the WebConnection 
     * is called, during which any operations can be performed, including closing the connection
     * using the close() method.
     * 
     * @param wc The WebConnection object that is associated with this connection
     */
    public void resume(WebConnection wc);

    /**
     * The connection timed out, but the connection will not be closed unless 
     * the Servlet uses the close method of the WebConnection.
     * 
     * @param wc The WebConnection object that is associated with this connection
     */
    public void timeout(WebConnection wc);

    /**
     * Error will be called by the container in the case where an IO exception
     * or a similar unrecoverable error occurs on the connection. Components that have
     * been initialized in the init method should be reset. After this method has
     * been called, the WebConnection object will be recycled and used to process 
     * other requests.
     * 
     * @param wc The WebConnection object that is associated with this connection
     */
    public void error(WebConnection wc);

    /**
     * The end of file of the input has been reached, and no further data is
     * available. This event is sent because it can be difficult to detect otherwise.
     * Following the processing of this method and the processing of any subsequent
     * method, the connection will be suspended.
     * 
     * @param wc The WebConnection object that is associated with this connection
     */
    public void eof(WebConnection wc);

}
