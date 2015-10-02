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

package org.apache.tomcat.util.net.jsse;

import static org.jboss.web.CoyoteMessages.MESSAGES;

import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;

import org.apache.tomcat.util.net.NioChannel;
import org.jboss.web.CoyoteLogger;

/**
 * {@code SecureNioChannel}
 * <p>
 * This class is an extension of the class {@link NioChannel} to allow using
 * secure communication channels.
 * </p>
 * Created on Jan 3, 2012 at 3:43:44 PM
 * 
 * @see javax.net.ssl.SSLEngine
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class SecureNioChannel extends NioChannel {

	public static final int MIN_BUFFER_SIZE = 16921;
	public static final int MIN_APP_BUFFER_SIZE = 16916;

	protected SSLEngine sslEngine;
	private ByteBuffer netInBuffer;
	private ByteBuffer netOutBuffer;
	protected boolean handshakeComplete = false;
	// To save the handshake status for each operation
	protected HandshakeStatus handshakeStatus;

	/**
	 * Create a new instance of {@code SecureNioChannel}
	 * 
	 * @param channel
	 *            the {@link java.nio.channels.AsynchronousSocketChannel}
	 * @param engine
	 *            The {@link javax.net.ssl.SSLEngine} linked to this channel
	 * @throws NullPointerException
	 *             if the one at least one of the parameters is null
	 */
	protected SecureNioChannel(AsynchronousSocketChannel channel, SSLEngine engine) {
		super(channel);
		if (engine == null) {
			throw MESSAGES.nullSslEngine();
		}

		this.sslEngine = engine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioChannel#isSecure()
	 */
	public boolean isSecure() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioChannel#read(java.nio.ByteBuffer)
	 * 
	 * @deprecated (use readBytes(...) instead)
	 */
	@Deprecated
	@Override
	public Future<Integer> read(ByteBuffer dst) {
		throw MESSAGES.operationNotSupported();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioChannel#readBytes(java.nio.ByteBuffer)
	 */
	public int readBytes(ByteBuffer dst) throws Exception {
		return readBytes(dst, Integer.MAX_VALUE, TimeUnit.SECONDS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioChannel#readBytes(java.nio.ByteBuffer,
	 * long, java.util.concurrent.TimeUnit)
	 */
	public int readBytes(ByteBuffer dst, long timeout, TimeUnit unit) throws Exception {

		// The handshake is completed
		checkHandshake();

		if (netInBuffer.position() == 0) {
			reset(netInBuffer);
			int x = channel.read(this.netInBuffer).get(timeout, unit);
			if (x < 0) {
				throw new ClosedChannelException();
			}
		}
		// Unwrap the data read, and return the number of unwrapped bytes
		int result = unwrap(this.netInBuffer, dst);
		if (result == 0) {
		    // Try reading since it means an underflow
            int x = channel.read(this.netInBuffer).get(timeout, unit);
            if (x < 0) {
                throw new ClosedChannelException();
            }
            result = unwrap(this.netInBuffer, dst);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioChannel#read(java.nio.ByteBuffer,
	 * java.lang.Object, java.nio.channels.CompletionHandler)
	 */
	@Override
	public <A> void read(final ByteBuffer dst, A attachment,
			CompletionHandler<Integer, ? super A> handler) {
		this.read(dst, Integer.MAX_VALUE, TimeUnit.MILLISECONDS, attachment, handler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioChannel#read(java.nio.ByteBuffer,
	 * long, java.util.concurrent.TimeUnit, java.lang.Object,
	 * java.nio.channels.CompletionHandler)
	 */
	@Override
	public <A> void read(final ByteBuffer dst, long timeout, TimeUnit unit, A attachment,
			final CompletionHandler<Integer, ? super A> handler) {

		// The handshake is completed
		checkHandshake();
		// Retrieve bytes in the internal buffer
		this.reset(this.netInBuffer);
		// perform read operation
		this.channel.read(this.netInBuffer, timeout, unit, attachment,
				new CompletionHandler<Integer, A>() {

					@Override
					public void completed(Integer nBytes, A attach) {
						if (nBytes < 0) {
							handler.failed(new ClosedChannelException(), attach);
							return;
						}

						try {
							// Unwrap the data
							int read = unwrap(netInBuffer, dst);
							// If everything is OK, so complete
							handler.completed(read, attach);
						} catch (Exception e) {
							// The operation must fails
							handler.failed(e, attach);
						}
					}

					@Override
					public void failed(Throwable exc, A attach) {
						handler.failed(exc, attach);
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioChannel#read(java.nio.ByteBuffer[],
	 * int, int, long, java.util.concurrent.TimeUnit, java.lang.Object,
	 * java.nio.channels.CompletionHandler)
	 */
	@Override
	public <A> void read(final ByteBuffer[] dsts, final int offset, final int length, long timeout,
			TimeUnit unit, A attachment, final CompletionHandler<Long, ? super A> handler) {

		// The handshake is completed
		checkHandshake();

		if (handler == null) {
			throw MESSAGES.nullHandler();
		}
		if ((offset < 0) || (length < 0) || (offset > dsts.length - length)) {
			throw new IndexOutOfBoundsException();
		}

		final ByteBuffer netInBuffers[] = new ByteBuffer[length];
		for (int i = 0; i < length; i++) {
			netInBuffers[i] = ByteBuffer.allocate(getSSLSession().getPacketBufferSize());
		}

		this.reset(netInBuffers[0]);

		this.channel.read(netInBuffers, 0, length, timeout, unit, attachment,
				new CompletionHandler<Long, A>() {

					@Override
					public void completed(Long nBytes, A attach) {
						if (nBytes < 0) {
							failed(new ClosedChannelException(), attach);
							return;
						}

						long read = 0;
						for (int i = 0; i < length; i++) {
							try {
								read += unwrap(netInBuffers[i], dsts[offset + i]);
							} catch (Exception e) {
								handler.failed(e, attach);
								return;
							}
						}

						handler.completed(read, attach);
					}

					@Override
					public void failed(Throwable exc, A attach) {
						handler.failed(exc, attach);
					}
				});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioChannel#write(java.nio.ByteBuffer)
	 * 
	 * @deprecated (use writeBytes(...) instead)
	 */
	@Deprecated
	@Override
	public Future<Integer> write(ByteBuffer src) {
		throw MESSAGES.operationNotSupported();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.tomcat.util.net.NioChannel#writeBytes(java.nio.ByteBuffer)
	 */
	public int writeBytes(ByteBuffer src) throws Exception {
		return writeBytes(src, Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.tomcat.util.net.NioChannel#writeBytes(java.nio.ByteBuffer,
	 * long, java.util.concurrent.TimeUnit)
	 */
	public int writeBytes(ByteBuffer src, long timeout, TimeUnit unit) throws Exception {

		// The handshake is completed
		checkHandshake();

		// Clear the output buffer
		this.netOutBuffer.compact();
		// the number of bytes written
		int written = wrap(src, this.netOutBuffer);
		this.netOutBuffer.flip();

		// write bytes to the channel
		while (this.netOutBuffer.hasRemaining()) {
			int x = this.channel.write(netOutBuffer).get(timeout, unit);
			if (x < 0) {
				throw new ClosedChannelException();
			}
		}

		return written;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioChannel#write(java.nio.ByteBuffer,
	 * java.lang.Object, java.nio.channels.CompletionHandler)
	 */
	@Override
	public <A> void write(ByteBuffer src, A attachment,
			CompletionHandler<Integer, ? super A> handler) {
		this.write(src, Integer.MAX_VALUE, TimeUnit.MILLISECONDS, attachment, handler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioChannel#write(java.nio.ByteBuffer,
	 * long, java.util.concurrent.TimeUnit, java.lang.Object,
	 * java.nio.channels.CompletionHandler)
	 */
	@Override
	public <A> void write(final ByteBuffer src, final long timeout, final TimeUnit unit, final A attachment,
			final CompletionHandler<Integer, ? super A> handler) {

		// The handshake is completed
		checkHandshake();

		try {
			// Prepare the output buffer
			this.netOutBuffer.clear();
			// Wrap the source data into the internal buffer
			final int written = wrap(src, this.netOutBuffer);
			this.netOutBuffer.flip();

			// Write data to the channel
			this.channel.write(this.netOutBuffer, timeout, unit, attachment,
					new CompletionHandler<Integer, A>() {

						@Override
						public void completed(Integer nBytes, A attach) {
	                        if (nBytes.intValue() < 0) {
	                            failed(new EOFException(), attach);
	                        } else if (netOutBuffer.hasRemaining()) {
	                            channel.write(netOutBuffer, timeout, unit, attachment, this);
	                        } else if (written == 0) {
	                            // Special case, start over to avoid code duplication
	                            write(src, timeout, unit, attachment, handler);
	                        } else {
	                            // Call the handler completed method with the
	                            // consumed bytes number
	                            handler.completed(Integer.valueOf(written), attach);
	                        }
						}

						@Override
						public void failed(Throwable exc, A attach) {
							handler.failed(exc, attach);
						}
					});

		} catch (Throwable exp) {
			handler.failed(exp, attachment);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioChannel#write(java.nio.ByteBuffer[],
	 * int, int, long, java.util.concurrent.TimeUnit, java.lang.Object,
	 * java.nio.channels.CompletionHandler)
	 */
	@Override
	public <A> void write(final ByteBuffer[] srcs, int offset, int length, long timeout,
			TimeUnit unit, A attachment, final CompletionHandler<Long, ? super A> handler) {

		// The handshake is completed
		checkHandshake();

		if (handler == null) {
			throw MESSAGES.nullHandler();
		}
		if ((offset < 0) || (length < 0) || (offset > srcs.length - length)) {
			throw new IndexOutOfBoundsException();
		}

		ByteBuffer[] netOutBuffers = new ByteBuffer[length];
		int size = getSSLSession().getPacketBufferSize();
		long written = 0;
		for (int i = 0; i < length; i++) {
			try {
				// Prepare the output buffer
				netOutBuffers[i] = ByteBuffer.allocate(size);
				// Wrap the source data into the internal buffer
				written += wrap(srcs[offset + i], netOutBuffers[i]);
				netOutBuffers[i].flip();
			} catch (Throwable exp) {
				handler.failed(exp, attachment);
				return;
			}
		}

		final long res = written;

		this.channel.write(netOutBuffers, 0, length, timeout, unit, attachment,
				new CompletionHandler<Long, A>() {

					@Override
					public void completed(Long nBytes, A attach) {
						if (nBytes < 0) {
							handler.failed(new ClosedChannelException(), attach);
						} else {
							// If everything is OK, so complete
							handler.completed(res, attach);
						}
					}

					@Override
					public void failed(Throwable exc, A attach) {
						handler.failed(exc, attach);
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.NioChannel#close()
	 */
	@Override
	public synchronized void close() throws IOException {
		if (isClosed()) {
			return;
		}
		try {
			// Handle closing the SSL Engine
			handleClose();
			this.channel.close();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * Close the {@link SSLEngine} attached to this channel
	 * 
	 * @throws Exception
	 */
	private void handleClose() throws Exception {
		if (sslEngine.isOutboundDone()) {
			return;
		}
		sslEngine.closeOutbound();

		SSLSession session = getSSLSession();
		int packetBufferSize = Math.max(session.getPacketBufferSize(), MIN_BUFFER_SIZE);

		this.netOutBuffer = (this.netOutBuffer == null) ? ByteBuffer
				.allocate(packetBufferSize) : this.netOutBuffer.compact();
		this.netInBuffer = (this.netInBuffer == null) ? ByteBuffer.allocate(packetBufferSize)
				: this.netInBuffer.compact();

		while (!sslEngine.isOutboundDone()) {
			// Get close message
			SSLEngineResult res = sslEngine.wrap(this.netInBuffer, this.netOutBuffer);

			switch (res.getStatus()) {
			case OK:
				// Execute tasks if we need to
				tryTasks();
				while (this.netOutBuffer.hasRemaining()) {
					if (this.channel.write(this.netOutBuffer).get() < 0) {
						break;
					}
					this.netOutBuffer.compact();
				}
				break;
			case BUFFER_OVERFLOW:
				ByteBuffer tmp = ByteBuffer.allocate(packetBufferSize
						+ this.netOutBuffer.capacity());
				this.netOutBuffer.flip();
				tmp.put(this.netOutBuffer);
				this.netOutBuffer = tmp;

				break;
			case BUFFER_UNDERFLOW:
				// Cannot happens in case of wrap
			case CLOSED:
				// Already closed, so return
				break;
			}
		}
	}

	/**
	 * Getter for sslEngine
	 * 
	 * @return the sslEngine
	 */
	public SSLEngine getSslEngine() {
		return this.sslEngine;
	}

	/**
	 * Setter for the sslEngine
	 * 
	 * @param sslEngine
	 *            the sslEngine to set
	 */
	protected void setSslEngine(SSLEngine sslEngine) {
		this.sslEngine = sslEngine;
	}

	/**
	 * Attempts to encode a buffer of plaintext application data into SSL/TLS
	 * network data.
	 * 
	 * @param src
	 *            a ByteBuffer containing outbound application data
	 * @param dst
	 *            a ByteBuffer to hold outbound network data
	 * @return the number of bytes consumed
	 * @throws Exception
	 *             if the wrap status is not <tt>OK</tt>
	 */
	private int wrap(ByteBuffer src, ByteBuffer dst) throws Exception {
		// Wrap the source data into the destination buffer
		SSLEngineResult result = sslEngine.wrap(src, dst);
		// the number of bytes written
		int written = result.bytesConsumed();
		this.handshakeStatus = result.getHandshakeStatus();

		switch (result.getStatus()) {
		case OK:
			// Execute tasks if we need to
			tryTasks();
			break;
		case CLOSED:
			// We can't do encryption any more
			written = -1;
		case BUFFER_OVERFLOW:
			throw new BufferOverflowException();
		case BUFFER_UNDERFLOW:
			// This case should not happen for a wrap method
			break;
		}

		return written;
	}

	/**
	 * Attempts to decode SSL/TLS network data into a plaintext application data
	 * buffer.
	 * 
	 * @param src
	 *            a ByteBuffer containing inbound network data.
	 * @param dst
	 *            a ByteBuffer to hold inbound application data.
	 * @return the number of bytes produced
	 * @throws Exception
	 */
	private int unwrap(ByteBuffer src, ByteBuffer dst) throws Exception {
		SSLEngineResult result;
		int read = 0;
		do {
			// prepare the input buffer
			src.flip();
			// unwrap the data
			result = sslEngine.unwrap(src, dst);
			// compact the buffer
			src.compact();

			if (result.getStatus() == Status.OK || result.getStatus() == Status.BUFFER_UNDERFLOW) {
				// we did receive some data, add it to our total
				read += result.bytesProduced();

				handshakeStatus = result.getHandshakeStatus();
				// perform any tasks if needed
				tryTasks();
				// if we need more network data, then bail out for now.
				if (result.getStatus() == Status.BUFFER_UNDERFLOW) {
                    break;
				}
			} else if (result.getStatus() == Status.BUFFER_OVERFLOW && read > 0) {
				// buffer overflow can happen, if we have read data, then
				// empty out the destination buffer before we do another read
				break;
			} else if (result.getStatus() == Status.CLOSED) {
				return -1;
			} else {
				// here we should trap BUFFER_OVERFLOW and call expand on the
				// buffer for now, throw an exception, as we initialized the
				// buffers in the constructor
				throw new IOException(MESSAGES.errorUnwrappingData(result.getStatus().toString()));
			}
			// continue to unwrapping as long as the input buffer has stuff
		} while (src.position() != 0);

		return read;
	}

	/**
	 * Initiates handshaking (initial or renegotiation) on this SSLEngine.
	 * <P>
	 * This method is not needed for the initial handshake, as the
	 * <code>wrap()</code> and <code>unwrap()</code> methods will implicitly
	 * call this method if handshaking has not already begun.
	 * <P>
	 * Note that the client may also request a session renegotiation with this
	 * <code>SSLEngine</code> by sending the appropriate session renegotiate
	 * handshake message.
	 * <P>
	 * Unlike the {@link SSLSocket#startHandshake() SSLSocket#startHandshake()}
	 * method, this method does not block until handshaking is completed.
	 * <P>
	 * To force a complete SSL/TLS session renegotiation, the current session
	 * should be invalidated prior to calling this method.
	 * <P>
	 * Some protocols may not support multiple handshakes on an existing engine
	 * and may throw an <code>SSLException</code>.
	 * 
	 * @throws SSLException
	 *             if a problem was encountered while signaling the
	 *             <code>SSLEngine</code> to begin a new handshake. See the
	 *             class description for more information on engine closure.
	 * @throws IllegalStateException
	 *             if the client/server mode has not yet been set.
	 * @see javax.net.ssl.SSLEngine#beginHandshake()
	 * @see javax.net.ssl.SSLSession#invalidate()
	 */
	protected void handshake() throws SSLException {
		if (handshakeComplete) {
			return;
		}

		try {
			doHandshake();
		} catch (Exception e) {
			throw new SSLException(e);
		}
	}

	/**
	 * Check whether the handshake is already complete or not
	 */
	private void checkHandshake() {
		if (!handshakeComplete) {
			throw MESSAGES.incompleteHandshake();
		}
	}

	/**
	 * Start a new handshake operation for this channel.
	 * 
	 * @see #handshake()
	 * @throws SSLException
	 */
	protected void reHandshake() throws SSLException {
        if (sslEngine.getWantClientAuth()) {
            CoyoteLogger.UTIL_LOGGER.debug("No client cert sent for want");
        } else {
            if (!sslEngine.getNeedClientAuth()) {
                sslEngine.setNeedClientAuth(true);
            } else {
                CoyoteLogger.UTIL_LOGGER.debug("Already need client cert");
            }
        }
		handshakeComplete = false;
		handshakeStatus = sslEngine.getHandshakeStatus();
		try {
			doHandshake();
		} catch (Exception e) {
			throw new SSLException(e);
		}
	}

	/**
	 * Execute a handshake with the client socket channel
	 * 
	 * @throws Exception
	 */
	private void doHandshake() throws Exception {

		SSLSession session = getSSLSession();
		int packetBufferSize = Math.max(session.getPacketBufferSize(), MIN_BUFFER_SIZE);
		// Create byte buffers to use for holding application data
		initBuffers(packetBufferSize);

		ByteBuffer clientNetData = ByteBuffer.allocate(packetBufferSize);
		ByteBuffer clientAppData = ByteBuffer.allocate(packetBufferSize);

		// Begin handshake
		sslEngine.beginHandshake();
		handshakeStatus = sslEngine.getHandshakeStatus();
		int i = 1;
		boolean read = true;
		// Process handshaking message
		while (!handshakeComplete) {

			switch (handshakeStatus) {
			case NEED_UNWRAP:
				int nBytes = 0;
				if (read) {
					clientAppData.clear();
					nBytes = this.channel.read(this.netInBuffer).get();
				}
				if (nBytes < 0) {
					throw new IOException(MESSAGES.errorUnwrappingHandshake());
				} else {
					boolean cont = false;
					// Loop while we can perform pure SSLEngine data
					do {
						// Prepare the buffer with the incoming data
						this.netInBuffer.flip();
						// Call unwrap
						SSLEngineResult res = sslEngine.unwrap(this.netInBuffer, clientAppData);
						// Compact the buffer, this is an optional method,
						// wonder what would happen if we didn't
						this.netInBuffer.compact();
						// Read in the status
						handshakeStatus = res.getHandshakeStatus();
						if (res.getStatus() == SSLEngineResult.Status.OK) {
							// Execute tasks if we need to
							tryTasks();
						}
						// Perform another unwrap?
						cont = res.getStatus() == SSLEngineResult.Status.OK
								&& handshakeStatus == HandshakeStatus.NEED_UNWRAP;
					} while (cont);
				}

				break;
			case NEED_WRAP:
				this.netOutBuffer.clear();
				SSLEngineResult res = sslEngine.wrap(clientNetData, this.netOutBuffer);
				handshakeStatus = res.getHandshakeStatus();
				this.netOutBuffer.flip();

				if (res.getStatus() == Status.OK) {
					// Execute tasks if we need to
					tryTasks();
					// Send the handshaking data to client
					while (this.netOutBuffer.hasRemaining()) {
						if (this.channel.write(this.netOutBuffer).get() < 0) {
							// Handle closed channel
							throw new IOException(MESSAGES.errorWrappingHandshake());
						}
					}
				} else {
					// Wrap should always work with our buffers
					throw new IOException(MESSAGES.errorWrappingHandshakeStatus(res.getStatus().toString()));
				}

				break;
			case NEED_TASK:
				handshakeStatus = tasks();

				break;
			case NOT_HANDSHAKING:
				throw new SSLHandshakeException(MESSAGES.notHandshaking());
			case FINISHED:
				handshakeComplete = true;
				break;
			}
		}

		this.handshakeComplete = (handshakeStatus == HandshakeStatus.FINISHED);
	}

	/**
	 * Perform tasks, if any, during the handshake phase
	 * 
	 * @return The handshake status (
	 *         {@link javax.net.ssl.SSLEngineResult.HandshakeStatus})
	 */
	private SSLEngineResult.HandshakeStatus tasks() {
		Runnable task = null;
		while ((task = sslEngine.getDelegatedTask()) != null) {
			// Run the task in blocking mode
			task.run();
		}

		return sslEngine.getHandshakeStatus();
	}

	/**
	 * Try to run tasks if any.
	 */
	private void tryTasks() {
		if (handshakeStatus == HandshakeStatus.NEED_TASK) {
			handshakeStatus = tasks();
		}
	}

	/**
	 * 
	 * @param capacity
	 */
	private void initBuffers(int capacity) {
		if (this.netInBuffer == null) {
			this.netInBuffer = ByteBuffer.allocate(capacity);
		} else {
			this.netInBuffer.clear();
		}
		if (this.netOutBuffer == null) {
			this.netOutBuffer = ByteBuffer.allocate(capacity);
		} else {
			this.netOutBuffer.clear();
		}
	}

	/**
	 * Check if the handshake was done or not yet
	 * 
	 * @return <tt>true</tt> if the handshake was already done, else
	 *         <tt>false</tt>
	 */
	protected boolean handshakeComplete() {
		return this.handshakeComplete;
	}

	/**
	 * @return The SSL Session of the channel
	 */
	public SSLSession getSSLSession() {
		return this.sslEngine.getSession();
	}
}
