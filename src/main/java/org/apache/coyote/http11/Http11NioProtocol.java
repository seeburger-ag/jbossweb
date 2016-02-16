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

package org.apache.coyote.http11;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.ObjectName;

import org.apache.coyote.RequestGroupInfo;
import org.apache.coyote.RequestInfo;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.net.NioChannel;
import org.apache.tomcat.util.net.NioEndpoint;
import org.apache.tomcat.util.net.SocketStatus;
import org.apache.tomcat.util.net.jsse.NioJSSEImplementation;
import org.apache.tomcat.util.net.jsse.NioJSSESocketChannelFactory;
import org.jboss.web.CoyoteLogger;
import org.jboss.web.NetworkUtils;

/**
 * {@code Http11NioProtocol}
 * 
 * Created on Jan 10, 2012 at 3:14:49 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Http11NioProtocol extends Http11AbstractProtocol {

	protected NioEndpoint endpoint = new NioEndpoint();
	private Http11ConnectionHandler cHandler = new Http11ConnectionHandler(this);
	protected NioJSSESocketChannelFactory socketFactory = null;

	/**
	 * Create a new instance of {@code Http11NioProtocol}
	 */
	public Http11NioProtocol() {
		setSoLinger(Constants.DEFAULT_CONNECTION_LINGER);
		setSoTimeout(Constants.DEFAULT_CONNECTION_TIMEOUT);
		setTcpNoDelay(Constants.DEFAULT_TCP_NO_DELAY);
        setKeepAliveTimeout(Constants.DEFAULT_KEEP_ALIVE_TIMEOUT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.ProtocolHandler#getRequestGroupInfo()
	 */
	@Override
	public RequestGroupInfo getRequestGroupInfo() {
		return cHandler.global;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.ProtocolHandler#init()
	 */
	@Override
	public void init() throws Exception {
		endpoint.setName(getName());
		endpoint.setHandler(cHandler);

		// Verify the validity of the configured socket factory
		try {
			if (isSSLEnabled()) {
				sslImplementation = new NioJSSEImplementation();
				//Possible pluggability ? SSLImplementation.getInstance(NioJSSEImplementation.class.getName());
				socketFactory = ((NioJSSEImplementation) sslImplementation).getServerSocketChannelFactory();
				endpoint.setServerSocketChannelFactory(socketFactory);
			}
		} catch (Exception ex) {
		    CoyoteLogger.HTTP_NIO_LOGGER.errorInitializingSocketFactory(ex);
			throw ex;
		}

		if (socketFactory != null) {
			Iterator<String> attE = attributes.keySet().iterator();
			while (attE.hasNext()) {
				String key = attE.next();
				Object v = attributes.get(key);
				socketFactory.setAttribute(key, v);
			}
		}

		try {
			// endpoint.setKeepAliveTimeout(this.timeout);
			endpoint.init();
		} catch (Exception ex) {
		    CoyoteLogger.HTTP_NIO_LOGGER.errorInitializingEndpoint(ex);
			throw ex;
		}

        CoyoteLogger.HTTP_NIO_LOGGER.initHttpConnector(getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.ProtocolHandler#start()
	 */
	@Override
	public void start() throws Exception {
		if (org.apache.tomcat.util.Constants.ENABLE_MODELER) {
			if (this.domain != null) {
				try {
					tpOname = new ObjectName(domain + ":" + "type=ThreadPool,name=" + getJmxName());
					Registry.getRegistry(null, null).registerComponent(endpoint, tpOname, null);
				} catch (Exception e) {
				    CoyoteLogger.HTTP_NIO_LOGGER.errorRegisteringPool(e);
				}
				rgOname = new ObjectName(domain + ":type=GlobalRequestProcessor,name=" + getJmxName());
				Registry.getRegistry(null, null).registerComponent(cHandler.global, rgOname, null);
			}
		}
		try {
			endpoint.start();
		} catch (Exception ex) {
		    CoyoteLogger.HTTP_NIO_LOGGER.errorStartingEndpoint(ex);
			throw ex;
		}
		CoyoteLogger.HTTP_NIO_LOGGER.startHttpConnector(getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.ProtocolHandler#pause()
	 */
	@Override
	public void pause() throws Exception {
		try {
			endpoint.pause();
		} catch (Exception ex) {
		    CoyoteLogger.HTTP_NIO_LOGGER.errorPausingEndpoint(ex);
			throw ex;
		}
		canDestroy = false;
		// Wait for a while until all the processors are idle
		RequestInfo[] states = cHandler.global.getRequestProcessors();
		int retry = 0;
		boolean done = false;
		while (!done && retry < org.apache.coyote.Constants.MAX_PAUSE_WAIT) {
			retry++;
			done = true;
			for (int i = 0; i < states.length; i++) {
				if (states[i].getStage() == org.apache.coyote.Constants.STAGE_SERVICE) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// NOTHING TO DO
					}
					done = false;
					break;
				}
			}
			if (done) {
				canDestroy = true;
			}
		}
		CoyoteLogger.HTTP_NIO_LOGGER.pauseHttpConnector(getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.ProtocolHandler#resume()
	 */
	@Override
	public void resume() throws Exception {
		try {
			endpoint.resume();
		} catch (Exception ex) {
		    CoyoteLogger.HTTP_NIO_LOGGER.errorResumingEndpoint(ex);
			throw ex;
		}
		CoyoteLogger.HTTP_NIO_LOGGER.resumeHttpConnector(getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.ProtocolHandler#destroy()
	 */
	@Override
	public void destroy() throws Exception {
	    CoyoteLogger.HTTP_NIO_LOGGER.stopHttpConnector(getName());
		if (canDestroy) {
			endpoint.destroy();
		} else {
		    CoyoteLogger.HTTP_NIO_LOGGER.cannotDestroyHttpProtocol(getName());
			try {
				RequestInfo[] states = cHandler.global.getRequestProcessors();
				for (int i = 0; i < states.length; i++) {
					if (states[i].getStage() == org.apache.coyote.Constants.STAGE_SERVICE) {
						// FIXME: Log RequestInfo content
					}
				}
			} catch (Exception ex) {
			    CoyoteLogger.HTTP_NIO_LOGGER.cannotDestroyHttpProtocolWithException(getName(), ex);
				throw ex;
			}
		}
		if (org.apache.tomcat.util.Constants.ENABLE_MODELER) {
			if (tpOname != null)
				Registry.getRegistry(null, null).unregisterComponent(tpOname);
			if (rgOname != null)
				Registry.getRegistry(null, null).unregisterComponent(rgOname);
		}
	}

    public String getJmxName() {
        String encodedAddr = "";
        if (getAddress() != null) {
            encodedAddr = "" + getAddress();
            encodedAddr = URLEncoder.encode(encodedAddr.replace('/', '-').replace(':', '_').replace('%', '-')) + "-";
        }
        return ("http-" + encodedAddr + endpoint.getPort());
    }

    public String getName() {
        String encodedAddr = "";
        if (getAddress() != null) {
            encodedAddr = NetworkUtils.formatIPAddressForURI(getAddress()) + ":";
        }
        return ("http-" + encodedAddr + endpoint.getPort());
    }

	/**
	 * @return the executor
	 */
	public Executor getExecutor() {
		return endpoint.getExecutor();
	}

	/**
	 * Setter for the executor
	 * 
	 * @param executor
	 *            the executor to set
	 */
	public void setExecutor(Executor executor) {
		endpoint.setExecutor(executor);
	}

	/**
	 * @return the maximum number of threads
	 */
	public int getMaxThreads() {
		return endpoint.getMaxThreads();
	}

	/**
	 * Setter for the maximum number of threads
	 * 
	 * @param maxThreads
	 *            the maximum number of threads to set
	 */
	public void setMaxThreads(int maxThreads) {
		endpoint.setMaxThreads(maxThreads);
	}

	/**
	 * @param size
	 */
	public void setPollerSize(int size) {
		this.endpoint.setMaxConnections(size);
	}

	/**
	 * @return The poller size
	 */
	public int getPollerSize() {
		return this.endpoint.getMaxConnections();
	}

	/**
	 * @return the thread priority
	 */
	public int getThreadPriority() {
		return endpoint.getThreadPriority();
	}

	/**
	 * Setter for the thread priority
	 * 
	 * @param threadPriority
	 *            the thread priority to set
	 */
	public void setThreadPriority(int threadPriority) {
		endpoint.setThreadPriority(threadPriority);
	}

	/**
	 * @return the backlog
	 */
	public int getBacklog() {
		return endpoint.getBacklog();
	}

	/**
	 * Setter for the backlog
	 * 
	 * @param backlog
	 *            the backlog to set
	 */
	public void setBacklog(int backlog) {
		endpoint.setBacklog(backlog);
	}

	/**
	 * @return the port number
	 */
	public int getPort() {
		return endpoint.getPort();
	}

	/**
	 * Setter for the port number
	 * 
	 * @param port
	 *            the port number to set
	 */
	public void setPort(int port) {
		endpoint.setPort(port);
	}

	/**
	 * @return the IP address
	 */
	public InetAddress getAddress() {
		return endpoint.getAddress();
	}

	/**
	 * Setter for the IP address
	 * 
	 * @param ia
	 *            the IP address to set
	 */
	public void setAddress(InetAddress ia) {
		endpoint.setAddress(ia);
	}

	/**
	 * @return TCP NO DELAY
	 */
	public boolean getTcpNoDelay() {
		return endpoint.getTcpNoDelay();
	}

	/**
	 * @param tcpNoDelay
	 */
	public void setTcpNoDelay(boolean tcpNoDelay) {
		endpoint.setTcpNoDelay(tcpNoDelay);
	}

	/**
	 * @return the soLinger
	 */
	public int getSoLinger() {
		return endpoint.getSoLinger();
	}

	/**
	 * @param soLinger
	 *            the soLinger to set
	 */
	public void setSoLinger(int soLinger) {
		endpoint.setSoLinger(soLinger);
	}

	/**
	 * @return the socket timeout
	 */
	public int getSoTimeout() {
		return endpoint.getSoTimeout();
	}

	/**
	 * Setter for the socket timeout
	 * 
	 * @param soTimeout
	 */
	public void setSoTimeout(int soTimeout) {
		endpoint.setSoTimeout(soTimeout);
	}

	/**
	 * @return <tt>TRUE</tt> if the reverse connection is enabled, else
	 *         <tt>FALSE</tt>
	 */
	public boolean getReverseConnection() {
		return endpoint.isReverseConnection();
	}

	/**
	 * Set the reverse connection
	 * 
	 * @param reverseConnection
	 */
	public void setReverseConnection(boolean reverseConnection) {
		endpoint.setReverseConnection(reverseConnection);
	}

	/**
	 * @return <tt>TRUE</tt> if the defer accept is enabled, else <tt>FALSE</tt>
	 */
	public boolean getDeferAccept() {
		return endpoint.getDeferAccept();
	}

	/**
	 * Set the defer accept
	 * 
	 * @param deferAccept
	 */
	public void setDeferAccept(boolean deferAccept) {
		endpoint.setDeferAccept(deferAccept);
	}

	/**
	 * The number of seconds Tomcat will wait for a subsequent request before
	 * closing the connection.
	 * 
	 * @return the keep alive timeout value
	 */
	public int getKeepAliveTimeout() {
		return endpoint.getKeepAliveTimeout();
	}

	/**
	 * Set the keep alive timeout value
	 * 
	 * @param timeout
	 */
	public void setKeepAliveTimeout(int timeout) {
		endpoint.setKeepAliveTimeout(timeout);
	}

	/**
	 * @return the user send file boolean value
	 */
	public boolean getUseSendfile() {
		return endpoint.getUseSendfile();
	}

	/**
	 * Set the user send file
	 * 
	 * @param useSendfile
	 */
	public void setUseSendfile(boolean useSendfile) {
		endpoint.setUseSendfile(useSendfile);
	}

	/**
	 * @return the send file size
	 */
	public int getSendfileSize() {
		return endpoint.getSendfileSize();
	}

	/**
	 * @param sendfileSize
	 */
	public void setSendfileSize(int sendfileSize) {
		endpoint.setSendfileSize(sendfileSize);
	}

	/**
	 * Return the Keep-Alive policy for the connection.
	 * 
	 * @return keep-alive
	 */
	public boolean getKeepAlive() {
		return ((maxKeepAliveRequests != 0) && (maxKeepAliveRequests != 1));
	}

	/**
	 * Set the keep-alive policy for this connection.
	 * 
	 * @param keepAlive
	 */
	public void setKeepAlive(boolean keepAlive) {
		if (!keepAlive) {
			setMaxKeepAliveRequests(1);
		}
	}

	// -------------------- Various implementation classes --------------------

	// -------------------- SSL related properties --------------------

	/**
	 * SSL engine.
	 * 
	 * @return <tt>true</tt> if the SSL is enabled, else <tt>false</tt>
	 */
	public boolean isSSLEnabled() {
		return endpoint.getSSLEnabled();
	}

	/**
	 * @param SSLEnabled
	 */
	public void setSSLEnabled(boolean SSLEnabled) {
		endpoint.setSSLEnabled(SSLEnabled);
	}

	/**
	 * SSL protocol.
	 * 
	 * @return the SSL protocol
	 */
	public String getSSLProtocol() {
		return endpoint.getSSLProtocol();
	}

	/**
	 * @param SSLProtocol
	 */
	public void setSSLProtocol(String SSLProtocol) {
		endpoint.setSSLProtocol(SSLProtocol);
	}

	/**
	 * SSL password (if a cert is encrypted, and no password has been provided,
	 * a callback will ask for a password).
	 * 
	 * @return the SSL password
	 */
	public String getSSLPassword() {
		return endpoint.getSSLPassword();
	}

	/**
	 * @param SSLPassword
	 */
	public void setSSLPassword(String SSLPassword) {
		endpoint.setSSLPassword(SSLPassword);
	}

	/**
	 * SSL cipher suite.
	 * 
	 * @return the SSL cipher suite
	 */
	public String getSSLCipherSuite() {
		return endpoint.getSSLCipherSuite();
	}

	/**
	 * @param SSLCipherSuite
	 */
	public void setSSLCipherSuite(String SSLCipherSuite) {
		endpoint.setSSLCipherSuite(SSLCipherSuite);
	}

	/**
	 * SSL certificate file.
	 * 
	 * @return SSL certificate file
	 */
	public String getSSLCertificateFile() {
		return endpoint.getSSLCertificateFile();
	}

	/**
	 * @param SSLCertificateFile
	 */
	public void setSSLCertificateFile(String SSLCertificateFile) {
		endpoint.setSSLCertificateFile(SSLCertificateFile);
	}

	/**
	 * SSL certificate key file.
	 * 
	 * @return SSL certificate key file
	 */
	public String getSSLCertificateKeyFile() {
		return endpoint.getSSLCertificateKeyFile();
	}

	/**
	 * @param SSLCertificateKeyFile
	 */
	public void setSSLCertificateKeyFile(String SSLCertificateKeyFile) {
		endpoint.setSSLCertificateKeyFile(SSLCertificateKeyFile);
	}

	/**
	 * SSL certificate chain file.
	 * 
	 * @return SSL certificate chain file
	 */
	public String getSSLCertificateChainFile() {
		return endpoint.getSSLCertificateChainFile();
	}

	/**
	 * @param SSLCertificateChainFile
	 */
	public void setSSLCertificateChainFile(String SSLCertificateChainFile) {
		endpoint.setSSLCertificateChainFile(SSLCertificateChainFile);
	}

	/**
	 * SSL CA certificate path.
	 * 
	 * @return SSL CA certificate path
	 */
	public String getSSLCACertificatePath() {
		return endpoint.getSSLCACertificatePath();
	}

	/**
	 * @param SSLCACertificatePath
	 */
	public void setSSLCACertificatePath(String SSLCACertificatePath) {
		endpoint.setSSLCACertificatePath(SSLCACertificatePath);
	}

	/**
	 * SSL CA certificate file.
	 * 
	 * @return SSL CA certificate file
	 */
	public String getSSLCACertificateFile() {
		return endpoint.getSSLCACertificateFile();
	}

	/**
	 * @param SSLCACertificateFile
	 */
	public void setSSLCACertificateFile(String SSLCACertificateFile) {
		endpoint.setSSLCACertificateFile(SSLCACertificateFile);
	}

	/**
	 * SSL CA revocation path.
	 * 
	 * @return SSL CA revocation path
	 */
	public String getSSLCARevocationPath() {
		return endpoint.getSSLCARevocationPath();
	}

	/**
	 * @param SSLCARevocationPath
	 */
	public void setSSLCARevocationPath(String SSLCARevocationPath) {
		endpoint.setSSLCARevocationPath(SSLCARevocationPath);
	}

	/**
	 * SSL CA revocation file.
	 * 
	 * @return the SSL CA revocation file
	 */
	public String getSSLCARevocationFile() {
		return endpoint.getSSLCARevocationFile();
	}

	/**
	 * @param SSLCARevocationFile
	 */
	public void setSSLCARevocationFile(String SSLCARevocationFile) {
		endpoint.setSSLCARevocationFile(SSLCARevocationFile);
	}

	/**
	 * SSL verify client.
	 * 
	 * @return SSLVerifyClient
	 */
	public String getSSLVerifyClient() {
		return endpoint.getSSLVerifyClient();
	}

	/**
	 * @param SSLVerifyClient
	 */
	public void setSSLVerifyClient(String SSLVerifyClient) {
		endpoint.setSSLVerifyClient(SSLVerifyClient);
	}

	/**
	 * SSL verify depth.
	 * 
	 * @return the SSL verify depth
	 */
	public int getSSLVerifyDepth() {
		return endpoint.getSSLVerifyDepth();
	}

	/**
	 * @param SSLVerifyDepth
	 *            the SSL verify depth
	 */
	public void setSSLVerifyDepth(int SSLVerifyDepth) {
		endpoint.setSSLVerifyDepth(SSLVerifyDepth);
	}

	// -------------------- Connection handler --------------------

	/**
	 * {@code Http11ConnectionHandler}
	 * 
	 * Created on Jan 13, 2012 at 10:45:44 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	static class Http11ConnectionHandler implements NioEndpoint.Handler {

		protected Http11NioProtocol proto;
		protected AtomicLong registerCount = new AtomicLong(0);
		protected RequestGroupInfo global = new RequestGroupInfo();

		protected ConcurrentHashMap<Long, Http11NioProcessor> connections = new ConcurrentHashMap<Long, Http11NioProcessor>();
		protected ConcurrentLinkedQueue<Http11NioProcessor> recycledProcessors = new ConcurrentLinkedQueue<Http11NioProcessor>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			protected AtomicInteger size = new AtomicInteger(0);

			@Override
			public boolean offer(Http11NioProcessor processor) {
				boolean offer = (proto.processorCache == -1) ? true
						: (size.get() < proto.processorCache);
				// avoid over growing our cache or add after we have stopped
				boolean result = false;
				if (offer) {
					result = super.offer(processor);
					if (result) {
						size.incrementAndGet();
					}
				}
				if (!result)
					unregister(processor);
				return result;
			}

			@Override
			public Http11NioProcessor poll() {
				Http11NioProcessor result = super.poll();
				if (result != null) {
					size.decrementAndGet();
				}
				return result;
			}

			@Override
			public void clear() {
				Http11NioProcessor next = poll();
				while (next != null) {
					unregister(next);
					next = poll();
				}
				super.clear();
				size.set(0);
			}
		};

		/**
		 * Create a new instance of {@code Http11ConnectionHandler}
		 * 
		 * @param proto
		 */
		Http11ConnectionHandler(Http11NioProtocol proto) {
			this.proto = proto;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.tomcat.util.net.NioEndpoint.Handler#event(java.nio.channels
		 * .AsynchronousSocketChannel, org.apache.tomcat.util.net.ChannelStatus)
		 */
		@Override
		// FIXME: probably needs sync due to concurrent read/write possibilities
		public SocketState event(NioChannel channel, SocketStatus status) {

			Http11NioProcessor processor = connections.get(channel.getId());
			SocketState state = SocketState.CLOSED;
			if (processor != null) {
				processor.startProcessing();
				// Call the appropriate event
				try {
					state = processor.event(status);
				} catch (java.net.SocketException e) {
					// SocketExceptions are normal
				    CoyoteLogger.HTTP_NIO_LOGGER.socketException(e);
				} catch (java.io.IOException e) {
					// IOExceptions are normal
                    CoyoteLogger.HTTP_NIO_LOGGER.socketException(e);
				}
				// Future developers: if you discover any other
				// rare-but-nonfatal exceptions, catch them here, and log as
				// above.
				catch (Throwable e) {
					// any other exception or error is odd. Here we log it
					// with "ERROR" level, so it will show up even on
					// less-than-verbose logs.
                    CoyoteLogger.HTTP_NIO_LOGGER.socketError(e);
				} finally {
					if (state != SocketState.LONG) {
						connections.remove(channel.getId());
						recycledProcessors.offer(processor);
						if (proto.endpoint.isRunning() && state == SocketState.OPEN) {
							final NioChannel ch = channel;
							proto.endpoint.removeEventChannel(ch);
							try {
								ch.awaitRead(proto.getKeepAliveTimeout(), TimeUnit.MILLISECONDS,
										proto.endpoint,
										new CompletionHandler<Integer, NioEndpoint>() {

											@Override
											public void completed(Integer nBytes,
													NioEndpoint endpoint) {
												if (nBytes < 0) {
													failed(new ClosedChannelException(), endpoint);
												} else {
													if (!endpoint.processChannel(ch, null)) {
													    endpoint.closeChannel(ch);
													}
												}
											}

											@Override
											public void failed(Throwable exc, NioEndpoint endpoint) {
												endpoint.closeChannel(ch);
											}
										});
							} catch (Exception exp) {
								// NOPE
							}
						}
	                    processor.endProcessing();
					} else {
					    if (processor.isAvailable() && processor.getReadNotifications()) {
					        // Call a read event right away
					        state = event(channel, SocketStatus.OPEN_READ);
		                    processor.endProcessing();
					    } else if (proto.endpoint.isRunning()) {
					        synchronized (processor) {
					            proto.endpoint.addEventChannel(channel, processor.getTimeout(),
					                    false,
					                    processor.getWriteNotification(),
					                    processor.getResumeNotification(), false);
					            processor.endProcessing();
					        }
					    }
					}
				}
			}

			return state;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.tomcat.util.net.NioEndpoint.Handler#process(java.nio.channels
		 * .AsynchronousSocketChannel)
		 */
		@Override
		public SocketState process(NioChannel channel) {
			Http11NioProcessor processor = recycledProcessors.poll();
			try {
				if (processor == null) {
					processor = createProcessor();
				}
                processor.startProcessing();

                if (proto.secure && (proto.sslImplementation != null)) {
					processor.setSSLSupport(((NioJSSEImplementation) proto.sslImplementation).getSSLSupport(channel));
				} else {
					processor.setSSLSupport(null);
				}

				SocketState state = processor.process(channel);

				if (state == SocketState.LONG) {
					// Associate the connection with the processor. The next
					// request processed by this thread will use either a new or
					// a recycled processor.
					connections.put(channel.getId(), processor);

					if (processor.isAvailable() && processor.getReadNotifications()) {
						// Call a read event right away
					    state = event(channel, SocketStatus.OPEN_READ);
		                processor.endProcessing();
					} else {
					    synchronized (processor) {
					        proto.endpoint.addEventChannel(channel, processor.getTimeout(),
					                processor.getReadNotifications(), false,
					                processor.getResumeNotification(), false);
					        processor.endProcessing();
					    }
					}
				} else {
					recycledProcessors.offer(processor);
	                processor.endProcessing();
				}
				return state;

			} catch (IOException e) {
				if (e instanceof java.net.SocketException) {
					// SocketExceptions are normal
                    CoyoteLogger.HTTP_NIO_LOGGER.socketException(e);
				} else {
					// IOExceptions are normal
                    CoyoteLogger.HTTP_NIO_LOGGER.socketException(e);
				}
			}
			// Future developers: if you discover any other
			// rare-but-non-fatal exceptions, catch them here, and log as
			// above.
			catch (Throwable e) {
				// any other exception or error is odd. Here we log it
				// with "ERROR" level, so it will show up even on
				// less-than-verbose logs.
                CoyoteLogger.HTTP_NIO_LOGGER.socketError(e);
			}
            processor.endProcessing();
			recycledProcessors.offer(processor);
			return SocketState.CLOSED;
		}

		/**
		 * @return
		 */
		protected Http11NioProcessor createProcessor() {
			Http11NioProcessor processor = new Http11NioProcessor(proto.maxHttpHeaderSize,
					proto.endpoint);
			processor.setAdapter(proto.adapter);
			processor.setMaxKeepAliveRequests(proto.maxKeepAliveRequests);
			processor.setTimeout(proto.timeout);
			processor.setDisableUploadTimeout(proto.disableUploadTimeout);
			processor.setCompressionMinSize(proto.compressionMinSize);
			processor.setCompression(proto.compression);
			processor.setNoCompressionUserAgents(proto.noCompressionUserAgents);
			processor.setCompressableMimeTypes(proto.compressableMimeTypes);
			processor.setRestrictedUserAgents(proto.restrictedUserAgents);
			processor.setMaxSavePostSize(proto.maxSavePostSize);
			processor.setServer(proto.server);
			register(processor);
			return processor;
		}

		/**
		 * @param processor
		 */
		protected void register(Http11NioProcessor processor) {
			RequestInfo rp = processor.getRequest().getRequestProcessor();
			rp.setGlobalProcessor(global);
			if (org.apache.tomcat.util.Constants.ENABLE_MODELER && proto.getDomain() != null) {
				synchronized (this) {
					try {
						long count = registerCount.incrementAndGet();
						ObjectName rpName = new ObjectName(proto.getDomain()
								+ ":type=RequestProcessor,worker=" + proto.getJmxName()
								+ ",name=HttpRequest" + count);
						Registry.getRegistry(null, null).registerComponent(rp, rpName, null);
						rp.setRpName(rpName);
					} catch (Exception e) {
					    CoyoteLogger.HTTP_NIO_LOGGER.errorRegisteringRequest(e);
					}
				}
			}
		}

		/**
		 * @param processor
		 */
		protected void unregister(Http11NioProcessor processor) {
			RequestInfo rp = processor.getRequest().getRequestProcessor();
			rp.setGlobalProcessor(null);
			if (org.apache.tomcat.util.Constants.ENABLE_MODELER && proto.getDomain() != null) {
				synchronized (this) {
					try {
						ObjectName rpName = rp.getRpName();
						Registry.getRegistry(null, null).unregisterComponent(rpName);
						rp.setRpName(null);
					} catch (Exception e) {
					    CoyoteLogger.HTTP_NIO_LOGGER.errorUnregisteringRequest(e);
					}
				}
			}
		}
	}

}
