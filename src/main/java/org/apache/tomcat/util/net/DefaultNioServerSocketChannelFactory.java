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

package org.apache.tomcat.util.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * {@code DefaultNioServerSocketChannelFactory}
 * 
 * Created on Jan 3, 2012 at 12:12:02 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class DefaultNioServerSocketChannelFactory extends NioServerSocketChannelFactory {

	/**
	 * Create a new instance of {@code DefaultNioServerSocketChannelFactory}
	 */
	public DefaultNioServerSocketChannelFactory() {
		super();
	}

	/**
	 * Create a new instance of {@code DefaultNioServerSocketChannelFactory}
	 * 
	 * @param threadGroup
	 */
	public DefaultNioServerSocketChannelFactory(AsynchronousChannelGroup threadGroup) {
		super(threadGroup);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioServerSocketChannelFactory#init()
	 */
	@Override
	public void init() throws IOException {
		// NOOP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioServerSocketChannelFactory#destroy()
	 */
	public void destroy() throws IOException {
	    try {
	        if (threadGroup != null && internalExecutor) {
	            threadGroup.shutdownNow();
	            long timeout = Constants.SHUTDOWN_TIMEOUT;
	            if (timeout > 0) {
	                try {
	                    threadGroup.awaitTermination(timeout, TimeUnit.MILLISECONDS);
	                } catch (InterruptedException e) {
	                    // Ignore
	                }
	            }
	        }
	    } finally {
	        this.threadGroup = null;
	        this.attributes.clear();
	        this.attributes = null;
	    }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.tomcat.util.net.NioServerSocketChannelFactory#initChannel(
	 * org.apache.tomcat.util.net.NioChannel)
	 */
	public void initChannel(NioChannel channel) throws Exception {
		// NOOP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.tomcat.util.net.NioServerSocketChannelFactory#handshake(org
	 * .apache.tomcat.util.net.NioChannel)
	 */
	@Override
	public void handshake(NioChannel channel) throws IOException {
		// NOOP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.tomcat.util.net.NioServerSocketChannelFactory#createServerChannel
	 * (int, int, java.net.InetAddress)
	 */
	@Override
	public AsynchronousServerSocketChannel createServerChannel(int port, int backlog,
			InetAddress ifAddress, boolean reuseAddress, boolean internalExecutor) throws IOException {
	    this.internalExecutor = internalExecutor;
		return open().setOption(StandardSocketOptions.SO_REUSEADDR, reuseAddress).bind(
				new InetSocketAddress(ifAddress, port), backlog);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.tomcat.util.net.NioServerSocketChannelFactory#acceptChannel
	 * (java.nio.channels.AsynchronousServerSocketChannel)
	 */
	@Override
	public NioChannel acceptChannel(AsynchronousServerSocketChannel listener) throws IOException {
		try {
			return new NioChannel(listener.accept().get());
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
