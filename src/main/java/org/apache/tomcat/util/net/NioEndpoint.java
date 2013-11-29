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

import java.io.File;
import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.WritePendingException;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;

import org.apache.tomcat.util.net.NioEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.jsse.NioJSSESocketChannelFactory;
import org.jboss.web.CoyoteLogger;

/**
 * {@code NioEndpoint} NIO2 endpoint, providing the following services:
 * <ul>
 * <li>Socket channel acceptor thread</li>
 * <li>Simple Worker thread pool, with possible use of executors</li>
 * </ul>
 * 
 * Created on Dec 13, 2011 at 9:41:53 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class NioEndpoint extends AbstractEndpoint {

	private AsynchronousServerSocketChannel listener;

	/**
	 * Handling of accepted sockets.
	 */
	protected Handler handler = null;

	/**
	 * The event poller
	 */
	private EventPoller eventPoller;

	/**
	 * 
	 */
	protected NioServerSocketChannelFactory serverSocketChannelFactory = null;

	/**
	 * SSL context.
	 */
	protected SSLContext sslContext;

	/**
	 * The static file sender.
	 */
	protected Sendfile sendfile;

	/**
	 * Create a new instance of {@code NioEndpoint}
	 */
	public NioEndpoint() {
		super();
	}

	/**
	 * @param handler
	 */
	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	/**
	 * @return the handler
	 */
	public Handler getHandler() {
		return handler;
	}

	/**
	 * Number of keep-alive channels.
	 * 
	 * @return the number of connection
	 */
	public int getKeepAliveCount() {
		return this.eventPoller.channelList.size();
	}

	/**
	 * Return the amount of threads that are managed by the pool.
	 * 
	 * @return the amount of threads that are managed by the pool
	 */
	public int getCurrentThreadCount() {
		return curThreads;
	}

	/**
	 * Return the amount of threads currently busy.
	 * 
	 * @return the amount of threads currently busy
	 */
	public int getCurrentThreadsBusy() {
		return curThreadsBusy;
	}

	/**
	 * Getter for sslContext
	 * 
	 * @return the sslContext
	 */
	public SSLContext getSslContext() {
		return this.sslContext;
	}

	/**
	 * Setter for the sslContext
	 * 
	 * @param sslContext
	 *            the sslContext to set
	 */
	public void setSslContext(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.AbstractEndpoint#init()
	 */
	@Override
	public void init() throws Exception {
		if (initialized) {
			return;
		}

		if (this.soTimeout < 0) {
			this.soTimeout = DEFAULT_SO_TIMEOUT;
		}

		if (this.keepAliveTimeout < 0) {
			this.keepAliveTimeout = this.soTimeout;
		}

		// Initialize thread count defaults for acceptor
		if (acceptorThreadCount <= 0) {
			acceptorThreadCount = 1;
		}

		// Create the thread factory
		if (this.threadFactory == null) {
			this.threadFactory = new DefaultThreadFactory(getName() + "-", threadPriority);
		}

		// If the executor is not set, create it with a fixed thread pool
		if (this.executor == null) {
			this.executor = Executors.newFixedThreadPool(this.maxThreads, this.threadFactory);
		}

		ExecutorService executorService = (ExecutorService) this.executor;
		AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup
				.withThreadPool(executorService);

		if (this.serverSocketChannelFactory == null) {
			this.serverSocketChannelFactory = NioServerSocketChannelFactory
					.createServerSocketChannelFactory(threadGroup, SSLEnabled);
		} else {
			this.serverSocketChannelFactory.threadGroup = threadGroup;
		}

		// Initialize the SSL context if the SSL mode is enabled
		if (SSLEnabled) {
			NioJSSESocketChannelFactory factory = (NioJSSESocketChannelFactory) this.serverSocketChannelFactory;
			sslContext = factory.getSslContext();
		}

		// Initialize the channel factory
		this.serverSocketChannelFactory.init();

		if (listener == null) {
		    listener = this.serverSocketChannelFactory.createServerChannel(port, backlog,
		            address, reuseAddress);
		}

		initialized = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.AbstractEndpoint#start()
	 */
	@Override
	public void start() throws Exception {
		// Initialize channel if not done before
		if (!initialized) {
			init();
		}
		if (!running) {
			running = true;
			paused = false;

            // Start acceptor threads
			for (int i = 0; i < acceptorThreadCount; i++) {
				Thread acceptorThread = newThread(new Acceptor(), "Acceptor", daemon);
				acceptorThread.start();
			}

			// Start sendfile thread
			if (useSendfile) {
				this.sendfile = new Sendfile();
				this.sendfile.init();
				Thread sendfileThread = newThread(this.sendfile, "SendFile", true);
				sendfileThread.start();
			}

			// Starting the event poller
			this.eventPoller = new EventPoller(this.maxConnections);
			this.eventPoller.init();
			Thread eventPollerThread = newThread(this.eventPoller, "EventPoller", true);
			eventPollerThread.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.AbstractEndpoint#stop()
	 */
	@Override
	public void stop() {
		if (running) {
			running = false;
			unlockAccept();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.AbstractEndpoint#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		if (running) {
			stop();
		}
		if (listener != null) {
			try {
				listener.close();
			} catch (IOException e) {
			    CoyoteLogger.UTIL_LOGGER.errorClosingSocket(e);
			} finally {
				listener = null;
			}
		}

		// Destroy the send file thread
		if (this.sendfile != null) {
			sendfile.destroy();
		}

		// destroy the send file thread
		if (this.eventPoller != null) {
			this.eventPoller.destroy();
		}

		// Destroy the server socket channel factory
		this.serverSocketChannelFactory.destroy();
		this.serverSocketChannelFactory = null;

		// Shut down the executor
		((ExecutorService) this.executor).shutdown();

		initialized = false;
	}

	/**
	 * Configure the channel options before being processed
	 */
	protected boolean setChannelOptions(NioChannel channel) {
		// Process the connection
		try {
			// Set channel options: timeout, linger, etc
			if (keepAliveTimeout > 0) {
				channel.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
			}
			if (soLinger >= 0) {
				channel.setOption(StandardSocketOptions.SO_LINGER, soLinger);
			}
			if (tcpNoDelay) {
				channel.setOption(StandardSocketOptions.TCP_NODELAY, tcpNoDelay);
			}
            if (soReceiveBuffer > 0) {
                channel.setOption(StandardSocketOptions.SO_RCVBUF, soReceiveBuffer);
            }
            if (soSendBuffer > 0) {
                channel.setOption(StandardSocketOptions.SO_SNDBUF, soSendBuffer);
            }

			// Initialize the channel
			serverSocketChannelFactory.initChannel(channel);
			return true;
		} catch (Throwable t) {
		    if (t instanceof SSLHandshakeException) {
		        CoyoteLogger.UTIL_LOGGER.handshakeFailed(t);
		    } else {
		        CoyoteLogger.UTIL_LOGGER.unexpectedError(t);
		    }
			return false;
		}
	}

	/**
	 * Add specified channel and associated pool to the poller. The added will
	 * be added to a temporary array, and polled first after a maximum amount of
	 * time equal to pollTime (in most cases, latency will be much lower,
	 * however). Note: If both read and write are false, the socket will only be
	 * checked for timeout; if the socket was already present in the poller, a
	 * callback event will be generated and the socket will be removed from the
	 * poller.
	 * 
	 * @param channel
	 *            the channel to add to the poller
	 * @param timeout
	 *            to use for this connection
	 * @param read
	 *            to do read polling
	 * @param write
	 *            to do write polling
	 * @param resume
	 *            to send a callback event
	 * @param wakeup
	 * 
	 * @see #addEventChannel(NioChannel, long, int)
	 */
	public void addEventChannel(NioChannel channel, long timeout, boolean read, boolean write,
			boolean resume, boolean wakeup) {

		int flags = (read ? ChannelInfo.READ : 0) | (write ? ChannelInfo.WRITE : 0)
				| (resume ? ChannelInfo.RESUME : 0) | (wakeup ? ChannelInfo.WAKEUP : 0);

		addEventChannel(channel, timeout, flags);
	}

	/**
	 * Same as
	 * {@link #addEventChannel(NioChannel, long, boolean, boolean, boolean, boolean)}
	 * 
	 * @param channel
	 *            the channel to add to the poller
	 * @param timeout
	 *            the channel timeout
	 * @param flags
	 *            a merge of read, write, resume and wake up event types
	 * 
	 * @see #addEventChannel(NioChannel, long, boolean, boolean, boolean,
	 *      boolean)
	 */
	public void addEventChannel(NioChannel channel, long timeout, int flags) {

		long eventTimeout = timeout <= 0 ? keepAliveTimeout : timeout;

		if (eventTimeout <= 0) {
			// Always put a timeout in
			eventTimeout = soTimeout > 0 ? soTimeout : Integer.MAX_VALUE;
		}

		if (!this.eventPoller.add(channel, eventTimeout, flags)) {
			closeChannel(channel);
		}
	}

	/**
	 * Remove the channel from the list of venet channels
	 * 
	 * @param channel
	 */
	public void removeEventChannel(NioChannel channel) {
		this.eventPoller.remove(channel);
	}

	/**
	 * Add a send file data to the queue of static files
	 * 
	 * @param data
	 * @return <tt>TRUE</tt> if the object is added successfully to the list of
	 *         {@code SendfileData}, else <tt>FALSE</tt>
	 */
	public boolean addSendfileData(SendfileData data) {
		if (this.sendfile != null) {
			return this.sendfile.add(data);
		}

		return false;
	}

	/**
	 * Process given channel.
	 */
	protected boolean processChannelWithOptions(NioChannel channel) {
		try {
			executor.execute(new ChannelWithOptionsProcessor(channel));
		} catch (Throwable t) {
			// This means we got an OOM or similar creating a thread, or that
			// the pool and its queue are full
            CoyoteLogger.NET_LOGGER.errorProcessingSocket(t);
			return false;
		}
		return true;
	}

	/**
	 * Process given channel for an event.
	 * 
	 * @param channel
	 * @param status
	 * @return <tt>true</tt> if the processing of the channel finish
	 *         successfully else <tt>false</tt>
	 */
	public boolean processChannel(NioChannel channel, SocketStatus status) {
		if (channel.isClosed()) {
			return false;
		}
		try {
			this.executor.execute(new ChannelProcessor(channel, status));
			return true;
		} catch (Throwable t) {
			// This means we got an OOM or similar creating a thread, or that
			// the pool and its queue are full
            CoyoteLogger.NET_LOGGER.errorProcessingSocket(t);
			return false;
		}
	}

	/**
	 * @param channel
	 * @return
	 */
	private boolean handshake(NioChannel channel) {
		try {
			this.executor.execute(new HandshakeHandler(channel));
			return true;
		} catch (Throwable t) {
			// This means we got an OOM or similar creating a thread, or that
			// the pool and its queue are full
            CoyoteLogger.NET_LOGGER.errorProcessingSocket(t);
			return false;
		}
	}

	/**
	 * Getter for serverSocketChannelFactory
	 * 
	 * @return the serverSocketChannelFactory
	 */
	public NioServerSocketChannelFactory getServerSocketChannelFactory() {
		return this.serverSocketChannelFactory;
	}

	/**
	 * Setter for the serverSocketChannelFactory
	 * 
	 * @param serverSocketChannelFactory
	 *            the serverSocketChannelFactory to set
	 */
	public void setServerSocketChannelFactory(
			NioServerSocketChannelFactory serverSocketChannelFactory) {
		this.serverSocketChannelFactory = serverSocketChannelFactory;
	}

	/**
	 * Close the specified channel and remove it from the list of open
	 * connections
	 * 
	 * @param channel
	 *            the channel to be closed
	 */
	public void closeChannel(NioChannel channel) {
		if (channel != null) {
			try {
				channel.close();
			} catch (IOException e) {
	            CoyoteLogger.UTIL_LOGGER.errorClosingSocket(e);
			}
		}
	}

	/**
	 * @return if the send file is supported, peek up a {@link SendfileData}
	 *         from the pool, else <tt>null</tt>
	 */
	public SendfileData getSendfileData() {
		return new SendfileData();
	}

	/**
	 * {@code Acceptor}
	 * 
	 * <p>
	 * Server socket acceptor thread.
	 * </p>
	 * Created on Mar 6, 2012 at 9:13:34 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	protected class Acceptor implements Runnable {

		/**
		 * The background thread that listens for incoming TCP/IP connections
		 * and hands them off to an appropriate processor.
		 */
		public void run() {

			// Loop until we receive a shutdown command
			while (running) {
				// Loop if end point is paused
				while (running && paused) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// Ignore
					}
				}
                if (!running) {
                    break;
                }

				try {
					// Accept the next incoming connection from the server
					// channel
					final NioChannel channel = serverSocketChannelFactory.acceptChannel(listener);
					boolean ok = false;
					if (setChannelOptions(channel) && channel.isOpen()) {
						if (channel.isSecure()) {
							handshake(channel);
							ok = true;
						} else {
							ok = processChannel(channel, null);
						}
					}
					// If a problem occurs, close the channel right away
					if (!ok) {
		                CoyoteLogger.UTIL_LOGGER.errorProcessingChannel();
						closeChannel(channel);
					}
				} catch (Exception exp) {
					if (running) {
					    CoyoteLogger.UTIL_LOGGER.errorAcceptingSocket(exp);
					}
				} catch (Throwable t) {
                    CoyoteLogger.UTIL_LOGGER.errorAcceptingSocket(t);
				}
			}
		}
	}

	/**
	 * {@code HandshakeHandler}
	 * <p>
	 * Asynchronous handler for the secure channel handshake. Since the
	 * handshake for the secure channels may take awhile, if several new
	 * connections are received at the same time, the non-blocking handshake
	 * aims to avoid connections to be timed out. Note that this does not
	 * guarantee that no connection will be timed out, this depends to the
	 * socket SO_TIMEOUT in the client side.
	 * </p>
	 * 
	 * Created on May 23, 2012 at 11:48:45 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	protected class HandshakeHandler implements Runnable {

		private NioChannel channel;

		/**
		 * Create a new instance of {@code HandshakeProcessor}
		 * 
		 * @param channel
		 */
		public HandshakeHandler(NioChannel channel) {
			this.channel = channel;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				serverSocketChannelFactory.handshake(channel);

				if (!processChannel(channel, null)) {
                    CoyoteLogger.UTIL_LOGGER.errorProcessingChannel();
					closeChannel(channel);
				}
			} catch (Exception exp) {
                CoyoteLogger.UTIL_LOGGER.errorProcessingChannelDebug(exp);
				closeChannel(channel);
			}
		}

	}

	/**
	 * {@code ChannelInfo}
	 * <p>
	 * Channel list class, used to avoid using a possibly large amount of
	 * objects with very little actual use.
	 * </p>
	 * Created on Apr 13, 2012 at 11:13:13 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	public static class ChannelInfo {
		/**
		 * 
		 */
		public static final int READ = 1;
		/**
		 * 
		 */
		public static final int WRITE = 2;
		/**
		 * 
		 */
		public static final int RESUME = 4;
		/**
		 * 
		 */
		public static final int WAKEUP = 8;

		protected NioChannel channel;
		protected long timeout;
		protected int flags;

		/**
		 * Create a new instance of {@code ChannelInfo}
		 */
		public ChannelInfo() {
			this(null, 0, 0);
		}

		/**
		 * Create a new instance of {@code ChannelInfo}
		 * 
		 * @param channel
		 *            the channel
		 * @param timeout
		 *            the channel timeout. The default time unit is
		 *            {@code java.util.concurrent.TimeUnit.MILLISECONDS}
		 * @param flags
		 */
		public ChannelInfo(NioChannel channel, long timeout, int flags) {
			this.channel = channel;
			this.timeout = timeout;
			this.flags = flags;
		}

		/**
		 * Create a new instance of {@code ChannelInfo}
		 * 
		 * @param channel
		 * @param timeout
		 * @param unit
		 * @param flags
		 */
		public ChannelInfo(NioChannel channel, long timeout, TimeUnit unit, int flags) {
			this(channel, TimeUnit.MILLISECONDS.convert(timeout, unit), flags);
		}

		/**
		 * @return the read flag
		 */
		public boolean read() {
			return (flags & READ) == READ;
		}

		/**
		 * Set the <code>read</code> flag. If the parameter is true, the read
		 * flag will have the value 1 else 0.
		 * 
		 * @param read
		 */
		public void read(boolean read) {
			this.flags = (read ? (this.flags | READ) : (this.flags & 0xE));
		}

		/**
		 * @return the write flag
		 */
		public boolean write() {
			return (flags & WRITE) == WRITE;
		}

		/**
		 * Set the <code>write</code> flag. If the parameter is true, the write
		 * flag will have the value 1 else 0.
		 * 
		 * @param write
		 */
		public void write(boolean write) {
			this.flags = (write ? (this.flags | WRITE) : (this.flags & 0xD));
		}

		/**
		 * @return the resume flag
		 */
		public boolean resume() {
			return (flags & RESUME) == RESUME;
		}

		/**
		 * Set the <code>resume</code> flag. If the parameter is true, the
		 * resume flag will have the value 1 else 0.
		 * 
		 * @param resume
		 */
		public void resume(boolean resume) {
			this.flags = (resume ? (this.flags | RESUME) : (this.flags & 0xB));
		}

		/**
		 * @return the wake up flag
		 */
		public boolean wakeup() {
			return (flags & WAKEUP) == WAKEUP;
		}

		/**
		 * Set the <code>wakeup</code> flag. If the parameter is true, the
		 * wakeup flag will have the value 1 else 0.
		 * 
		 * @param wakeup
		 */
		public void wakeup(boolean wakeup) {
			this.flags = (wakeup ? (this.flags | WAKEUP) : (this.flags & 0x7));
		}

		/**
		 * Merge the tow flags
		 * 
		 * @param flag1
		 * @param flag2
		 * @return the result of merging the tow flags
		 */
		public static int merge(int flag1, int flag2) {
			return ((flag1 & READ) | (flag2 & READ)) | ((flag1 & WRITE) | (flag2 & WRITE))
					| ((flag1 & RESUME) | (flag2 & RESUME)) | ((flag1 & WAKEUP) & (flag2 & WAKEUP));
		}
	}

	/**
	 * {@code Handler}
	 * 
	 * <p>
	 * Bare bones interface used for socket processing. Per thread data is to be
	 * stored in the ThreadWithAttributes extra folders, or alternately in
	 * thread local fields.
	 * </p>
	 * 
	 * Created on Mar 6, 2012 at 9:13:07 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	public interface Handler {
		/**
		 * {@code ChannelState}
		 * 
		 * Created on Dec 12, 2011 at 9:41:06 AM
		 * 
		 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
		 */
		public enum SocketState {
			/**
			 * 
			 */
			OPEN,
			/**
			 * 
			 */
			CLOSED,
			/**
			 * 
			 */
			LONG
		}

		/**
		 * Process the specified {@code org.apache.tomcat.util.net.NioChannel}
		 * 
		 * @param channel
		 *            the {@code org.apache.tomcat.util.net.NioChannel}
		 * @return a channel state
		 */
		public SocketState process(NioChannel channel);

		/**
		 * Process the specified {@code org.apache.tomcat.util.net.NioChannel}
		 * 
		 * @param channel
		 * @param status
		 * @return a channel state
		 */
		public SocketState event(NioChannel channel, SocketStatus status);

	}

	/**
	 * {@code ChannelWithOptionsProcessor}
	 * <p>
	 * This class is the equivalent of the Worker, but will simply use in an
	 * external Executor thread pool. This will also set the channel options and
	 * do the handshake.
	 * </p>
	 * Created on Mar 6, 2012 at 9:09:43 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	protected class ChannelWithOptionsProcessor extends ChannelProcessor {

		/**
		 * Create a new instance of {@code ChannelWithOptionsProcessor}
		 * 
		 * @param channel
		 */
		public ChannelWithOptionsProcessor(NioChannel channel) {
			super(channel);
		}

		@Override
		public void run() {
			boolean ok = true;

			if (!deferAccept) {
				ok = setChannelOptions(channel);
			} else {
				// Process the request from this channel
				ok = setChannelOptions(channel)
						&& handler.process(channel) != Handler.SocketState.CLOSED;
			}

			if (!ok) {
				// Close the channel
				closeChannel(channel);
			}

			channel = null;
		}
	}

	/**
	 * {@code ChannelProcessor}
	 * <p>
	 * This class is the equivalent of the Worker, but will simply use in an
	 * external Executor thread pool.
	 * </p>
	 * Created on Mar 6, 2012 at 9:10:06 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	protected class ChannelProcessor implements Runnable {

		protected NioChannel channel;
		protected SocketStatus status = null;

		/**
		 * Create a new instance of {@code ChannelProcessor}
		 * 
		 * @param channel
		 */
		public ChannelProcessor(NioChannel channel) {
			this.channel = channel;
		}

		/**
		 * Create a new instance of {@code ChannelProcessor}
		 * 
		 * @param channel
		 * @param status
		 */
		public ChannelProcessor(NioChannel channel, SocketStatus status) {
			this(channel);
			this.status = status;
		}

		@Override
		public void run() {
			try {
				Handler.SocketState state = ((status == null) ? handler.process(channel) : handler
						.event(channel, status));

				if (state == SocketState.CLOSED) {
					closeChannel(channel);
				}
			} catch (Throwable th) {
                CoyoteLogger.UTIL_LOGGER.errorProcessingChannelWithException(th);
			}
		}

	}

	/**
	 * {@code EventPoller}
	 * 
	 * Created on Mar 26, 2012 at 12:51:53 PM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	public class EventPoller implements Runnable {

		/**
		 * Last run of maintain. Maintain will run usually every 5s.
		 */
		protected long lastMaintain = System.currentTimeMillis();

		protected ConcurrentHashMap<Long, ChannelInfo> channelList;
		private Object mutex;
		private int size;

		/**
		 * Create a new instance of {@code EventPoller}
		 * 
		 * @param size
		 */
		public EventPoller(int size) {
			this.size = size;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (running) {
				// Loop if endpoint is paused
				while (running && paused) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// Ignore
					}
				}

				while (running && this.channelList.size() < 1 && running) {
					synchronized (this.mutex) {
						try {
							this.mutex.wait(10000);
						} catch (InterruptedException e) {
							// NOPE
						}
					}
				}

				while (running && this.channelList.size() > 0 && running) {
					maintain();
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// NOPE
					}
				}

			}
		}

		/**
		 * Check timeouts and raise timeout event
		 */
		public void maintain() {
			long date = System.currentTimeMillis();
			// Maintain runs at most once every 5s, although it will likely get
			// called more
			if ((date - lastMaintain) < 5000L) {
				return;
			}

			// Update the last maintain time
			lastMaintain = date;

			for (ChannelInfo info : this.channelList.values()) {
				if (date >= info.timeout) {
					NioChannel ch = info.channel;
					remove(info);
					if (!processChannel(ch, SocketStatus.TIMEOUT)) {
						closeChannel(ch);
					}
				}
			}
		}

		/**
		 * Remove the channel having the specified id
		 * 
		 * @param id
		 */
		protected boolean remove(long id) {
			return this.channelList.remove(id) != null;
		}

		/**
		 * @param channel
		 * @return true if the channel is removed successfully else false
		 */
		public boolean remove(NioChannel channel) {
			return channel != null ? remove(channel.getId()) : false;
		}

		/**
		 * @param info
		 * @return true if the channel-info is removed successfully else false
		 */
		public boolean remove(ChannelInfo info) {
			return info != null ? remove(info.channel) : false;
		}

		/**
		 * Initialize the event poller
		 */
		public void init() {
			this.mutex = new Object();
			this.channelList = new ConcurrentHashMap<Long, ChannelInfo>(this.size);
		}

		/**
		 * Destroy the event poller
		 */
		public void destroy() {
			synchronized (this.mutex) {
				this.channelList.clear();
				this.mutex.notifyAll();
			}
		}

		/**
		 * Create a completion handler for read.
		 * 
		 * @return a reference of {@link java.nio.CompletionHandler}
		 */
		private CompletionHandler<Integer, NioChannel> getCompletionHandler() {
		    CompletionHandler<Integer, NioChannel> handler = new CompletionHandler<Integer, NioChannel>() {
		        @Override
		        public void completed(Integer nBytes, NioChannel attach) {
		            if (nBytes < 0) {
		                failed(new ClosedChannelException(), attach);
		            } else {
		                remove(attach);
		                if (!processChannel(attach, SocketStatus.OPEN_READ)) {
		                    closeChannel(attach);
		                }
		            }
		        }

		        @Override
		        public void failed(Throwable exc, NioChannel attach) {
		            remove(attach);
		            if (!processChannel(attach, SocketStatus.ERROR)) {
		                closeChannel(attach);
		            }
		        }
		    };

			return handler;
		}

		/**
		 * Add the channel to the list of channels
		 * 
		 * @param channel
		 * @param timeout
		 * @param flag
		 * @return <tt>true</tt> if the channel is added successfully else
		 *         <tt>false</tt>
		 */
		public boolean add(final NioChannel channel, long timeout, int flag) {
			if (this.channelList.size() > this.size) {
				return false;
			}

			long date = timeout + System.currentTimeMillis();
			ChannelInfo info = this.channelList.get(channel.getId());

			if (info == null) {
				info = new ChannelInfo();
				info.channel = channel;
				info.flags = flag;
				this.channelList.put(channel.getId(), info);
			} else {
				info.flags = ChannelInfo.merge(info.flags, flag);
			}

			// Setting the channel timeout
			info.timeout = date;
			final NioChannel ch = channel;
			if (info.resume()) {
				remove(info);
				if (!processChannel(ch, SocketStatus.OPEN_CALLBACK)) {
					closeChannel(ch);
				}
			} else if (info.write()) {
				remove(info);
				if (!processChannel(ch, SocketStatus.OPEN_WRITE)) {
					closeChannel(ch);
				}
            } else if (info.read()) {
                try {
                    // Trying awaiting for read event
                    ch.awaitRead(ch, getCompletionHandler());
                } catch (Exception e) {
                    // Ignore
                    CoyoteLogger.UTIL_LOGGER.errorAwaitingRead(e);
                }
			} else if (info.wakeup()) {
				remove(info);
				// TODO
			}

			// Wake up all waiting threads
			synchronized (this.mutex) {
				this.mutex.notifyAll();
			}
			return true;
		}
	}

	/**
	 * {@code DefaultThreadFactory}
	 * 
	 * The default thread factory
	 * 
	 * Created on Mar 6, 2012 at 9:11:20 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	protected static class DefaultThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private final int threadPriority;

		/**
		 * Create a new instance of {@code DefaultThreadFactory}
		 * 
		 * @param namePrefix
		 * @param threadPriority
		 */
		public DefaultThreadFactory(String namePrefix, int threadPriority) {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			this.namePrefix = namePrefix;
			this.threadPriority = threadPriority;
		}

		/**
		 * 
		 * Create a new instance of {@code DefaultThreadFactory}
		 * 
		 * @param threadPriority
		 */
		public DefaultThreadFactory(int threadPriority) {
			this("pool-" + poolNumber.getAndIncrement() + "-thread-", threadPriority);
		}

		/**
		 * Create and return a new thread
		 */
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (thread.isDaemon())
				thread.setDaemon(false);

			if (thread.getPriority() != this.threadPriority)
				thread.setPriority(this.threadPriority);
			return thread;
		}
	}

	/**
	 * SendfileData class.
	 */
	public static class SendfileData {
		// File
		protected String fileName;
		// Range information
		protected long start;
		protected long end;
		// The channel
		protected NioChannel channel;
		// The file channel
		protected java.nio.channels.FileChannel fileChannel;
		// Position
		protected long pos;
		// KeepAlive flag
		protected boolean keepAlive;

		/**
		 * Prepare the {@code SendfileData}
		 * 
		 * @throws IOException
		 * 
		 * @throws Exception
		 */
		protected void setup() throws IOException {
			this.pos = this.start;
			if (this.fileChannel == null || !this.fileChannel.isOpen()) {
				java.nio.file.Path path = new File(this.fileName).toPath();
				this.fileChannel = java.nio.channels.FileChannel
						.open(path, StandardOpenOption.READ).position(this.pos);
			}
		}

		/**
		 * Getter for fileName
		 * 
		 * @return the fileName
		 */
		public String getFileName() {
			return this.fileName;
		}

		/**
		 * Setter for the fileName
		 * 
		 * @param fileName
		 *            the fileName to set
		 */
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		/**
		 * Getter for start
		 * 
		 * @return the start
		 */
		public long getStart() {
			return this.start;
		}

		/**
		 * Setter for the start
		 * 
		 * @param start
		 *            the start to set
		 */
		public void setStart(long start) {
			this.start = start;
		}

		/**
		 * Getter for end
		 * 
		 * @return the end
		 */
		public long getEnd() {
			return this.end;
		}

		/**
		 * Setter for the end
		 * 
		 * @param end
		 *            the end to set
		 */
		public void setEnd(long end) {
			this.end = end;
		}

		/**
		 * Getter for channel
		 * 
		 * @return the channel
		 */
		public NioChannel getChannel() {
			return this.channel;
		}

		/**
		 * Setter for the channel
		 * 
		 * @param channel
		 *            the channel to set
		 */
		public void setChannel(NioChannel channel) {
			this.channel = channel;
		}

		/**
		 * Getter for pos
		 * 
		 * @return the pos
		 */
		public long getPos() {
			return this.pos;
		}

		/**
		 * Setter for the pos
		 * 
		 * @param pos
		 *            the pos to set
		 */
		public void setPos(long pos) {
			this.pos = pos;
		}

		/**
		 * Getter for keepAlive
		 * 
		 * @return the keepAlive
		 */
		public boolean isKeepAlive() {
			return this.keepAlive;
		}

		/**
		 * Setter for the keepAlive
		 * 
		 * @param keepAlive
		 *            the keepAlive to set
		 */
		public void setKeepAlive(boolean keepAlive) {
			this.keepAlive = keepAlive;
		}
	}

	/**
	 * {@code Sendfile}
	 * 
	 * Created on Mar 7, 2012 at 4:04:59 PM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	public class Sendfile implements Runnable {

		protected int size;
		protected ConcurrentLinkedQueue<SendfileData> fileDatas;
		protected AtomicInteger counter;
		private Object mutex;

		/**
		 * @return the number of send file
		 */
		public int getSendfileCount() {
			return this.counter.get();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {

			while (running) {
				// Loop if endpoint is paused
				while (running && paused) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// Ignore
					}
				}
				// Loop while poller is empty
				while (this.counter.get() < 1 && running && !paused) {
					try {
						synchronized (this.mutex) {
							this.mutex.wait();
						}
					} catch (InterruptedException e) {
						// Ignore
					}
				}

				if (running && !paused) {
					try {
						SendfileData data = this.poll();
						if (data != null) {
							sendFile(data);
						}
					} catch (Throwable th) {
						// Ignore
					}
				}
			}
		}

		/**
		 * Initialize the {@code Sendfile}
		 */
		protected void init() {
		    this.size = sendfileSize;
			this.mutex = new Object();
			this.counter = new AtomicInteger(0);
			this.fileDatas = new ConcurrentLinkedQueue<SendfileData>();
		}

		/**
		 * Destroy the SendFile
		 */
		protected void destroy() {
			synchronized (this.mutex) {
				// To unlock the
				this.counter.incrementAndGet();
				this.fileDatas.clear();
				// Unlock threads waiting for this monitor
				this.mutex.notifyAll();
			}
		}

		/**
		 * 
		 * @param data
		 * @throws Exception
		 */
		private void sendFile(final SendfileData data) throws Exception {

			// Configure the send file data
			data.setup();

			final NioChannel channel = data.channel;
			final int BUFFER_SIZE = channel.getOption(StandardSocketOptions.SO_SNDBUF);
			final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
			int nr = data.fileChannel.read(buffer);

			if (nr >= 0) {
				buffer.flip();
				try {
					channel.write(buffer, data, new CompletionHandler<Integer, SendfileData>() {

						@Override
						public void completed(Integer nw, SendfileData attachment) {
							if (nw < 0) { // Reach the end of stream
								closeChannel(channel);
								closeFile(attachment.fileChannel);
								return;
							}

							attachment.pos += nw;

							if (attachment.pos >= attachment.end) {
								return;
							}

							boolean ok = true;

							if (!buffer.hasRemaining()) {
								// This means that all data in the buffer has
								// been
								// written => Empty the buffer and read again
								buffer.clear();
								try {
									if (attachment.fileChannel.read(buffer) >= 0) {
										buffer.flip();
									} else {
										// Reach the EOF
										ok = false;
									}
								} catch (Throwable th) {
									ok = false;
								}
							}

							if (ok) {
								channel.write(buffer, attachment, this);
							} else {
								closeFile(attachment.fileChannel);
							}
						}

						@Override
						public void failed(Throwable exc, SendfileData attachment) {
							// Closing channels
							closeChannel(channel);
							closeFile(data.fileChannel);
						}

						/**
						 * 
						 * @param closeable
						 */
						private void closeFile(java.io.Closeable closeable) {
							try {
								closeable.close();
							} catch (IOException e) {
								// NOPE
							}
						}
					});
				} catch (WritePendingException exp) {
					data.fileChannel.close();
					add(data);
				}
			}
		}

		/**
		 * Add the sendfile data to the sendfile poller. Note that in most
		 * cases, the initial non blocking calls to sendfile will return right
		 * away, and will be handled asynchronously inside the kernel. As a
		 * result, the poller will never be used.
		 * 
		 * @param data
		 *            containing the reference to the data which should be sent
		 * @return true if all the data has been sent right away, and false
		 *         otherwise
		 */
		public boolean add(SendfileData data) {
			if (data != null && this.counter.get() < this.size) {
				synchronized (this.mutex) {
					if (this.fileDatas.offer(data)) {
						this.counter.incrementAndGet();
						this.mutex.notifyAll();
						return true;
					}
				}
			}

			return false;
		}

		/**
		 * Retrieves and removes the head of this queue, or returns
		 * <tt>null</tt> if this queue is empty.
		 * 
		 * @return the head of this queue, or <tt>null</tt> if this queue is
		 *         empty
		 */
		protected SendfileData poll() {
			SendfileData data = this.fileDatas.poll();
			if (data != null) {
				this.counter.decrementAndGet();
			}
			return data;
		}

		/**
		 * Remove socket from the poller.
		 * 
		 * @param data
		 *            the sendfile data which should be removed
		 */
		protected void remove(SendfileData data) {
			if (this.fileDatas.remove(data)) {
				this.counter.decrementAndGet();
			}
		}
	}
}
