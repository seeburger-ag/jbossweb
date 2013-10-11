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

import static org.jboss.web.CoyoteMessages.MESSAGES;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.WritePendingException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.coyote.ActionCode;
import org.apache.coyote.Response;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.net.NioChannel;
import org.apache.tomcat.util.net.NioEndpoint;
import org.apache.tomcat.util.net.SocketStatus;
import org.jboss.web.CoyoteLogger;

/**
 * {@code InternalNioOutputBuffer}
 * 
 * Created on Dec 16, 2011 at 9:15:05 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class InternalNioOutputBuffer extends AbstractInternalOutputBuffer {

	/**
	 * Underlying channel.
	 */
	protected NioChannel channel;

	/**
	 * NIO endpoint.
	 */
	protected NioEndpoint endpoint;

    /**
     * NIO processor.
     */
    protected Http11NioProcessor processor;

	/**
	 * The completion handler used for asynchronous write operations
	 */
	private CompletionHandler<Integer, NioChannel> completionHandler;

    /**
     * Latch to wait for non blocking operations to run the completion handler.
     */
    private CountDownLatch latch = null;

	/**
	 * Create a new instance of {@code InternalNioOutputBuffer}
	 * 
	 * @param response
	 * @param headerBufferSize
	 * @param endpoint
	 */
	public InternalNioOutputBuffer(Http11NioProcessor processor, Response response, int headerBufferSize, NioEndpoint endpoint) {
		super(response, headerBufferSize);
		this.endpoint = endpoint;
		this.processor = processor;
		// Initialize the input buffer
		this.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.http11.AbstractInternalOutputBuffer#init()
	 */
	protected void init() {

		this.writeTimeout = (endpoint.getSoTimeout() > 0 ? endpoint.getSoTimeout()
				: Integer.MAX_VALUE);

		this.completionHandler = new CompletionHandler<Integer, NioChannel>() {

			@Override
			public void completed(Integer nBytes, NioChannel attachment) {
                try {
                    if (nBytes < 0) {
                        failed(new ClosedChannelException(), attachment);
                        return;
                    }

                    if (bbuf.hasRemaining()) {
                        try {
                            attachment.write(bbuf, writeTimeout, TimeUnit.MILLISECONDS, attachment, this);
                        } catch (WritePendingException e) {
                            response.setLastWrite(0);
                        }
                    } else {
                        // Clear the buffer when all bytes are written
                        clearBuffer();
                        response.setLastWrite(nBytes);
                        if (!processor.isProcessing() && processor.getWriteNotification()) {
                            endpoint.processChannel(attachment, SocketStatus.OPEN_WRITE);
                        }
                    }
                } finally {
                    latch.countDown();
                }
			}

			@Override
			public void failed(Throwable exc, NioChannel attachment) {
				endpoint.removeEventChannel(attachment);
                if (!processor.isProcessing()) {
                    endpoint.processChannel(attachment, SocketStatus.ERROR);
                }
			}
		};
	}

	/**
	 * Set the underlying socket.
	 * 
	 * @param channel
	 */
	public void setChannel(NioChannel channel) {
		this.channel = channel;
	}

	/**
	 * Get the underlying socket input stream.
	 * 
	 * @return the channel
	 */
	public NioChannel getChannel() {
		return channel;
	}

	/**
	 * Recycle the output buffer. This should be called when closing the
	 * connection.
	 */
	public void recycle() {
		super.recycle();
		channel = null;
	}

	/**
	 * Close the channel
	 * 
	 * @param channel
	 */
	private void close(NioChannel channel) {
		endpoint.closeChannel(channel);
	}

	/**
	 * Perform a blocking write operation
	 * 
	 * @param buffer
	 *            the buffer containing the data to write
	 * @param timeout
	 *            a timeout for the operation
	 * @param unit
	 *            The time unit
	 * 
	 * @return the number of bytes written, -1 in case of errors
	 */
	private int blockingWrite(long timeout, TimeUnit unit) {
		int nw = 0;
		try {
			nw = this.channel.writeBytes(this.bbuf, timeout, unit);
			if (nw < 0) {
				close(channel);
			}
		} catch (Throwable t) {
			if (CoyoteLogger.HTTP_LOGGER.isDebugEnabled()) {
	             CoyoteLogger.HTTP_LOGGER.errorWithBlockingWrite(t);
			}
		}

		return nw;
	}

	/**
	 * Perform a non-blocking write operation
	 * 
	 * @param buffer
	 *            the buffer containing the data to write
	 * @param timeout
	 *            a timeout for the operation
	 * @param unit
	 *            The time unit
	 */
	private void nonBlockingWrite(final long timeout, final TimeUnit unit) {
		try {
		    latch = new CountDownLatch(1);
			// Perform the write operation
			this.channel.write(this.bbuf, timeout, unit, this.channel, this.completionHandler);
			latch.await();
		} catch (Throwable t) {
			if (CoyoteLogger.HTTP_LOGGER.isDebugEnabled()) {
			    CoyoteLogger.HTTP_LOGGER.errorWithNonBlockingWrite(t);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.coyote.http11.AbstractInternalOutputBuffer#write(java.nio.
	 * ByteBuffer, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	protected int write(final long timeout, final TimeUnit unit) {
		if (nonBlocking) {
			nonBlockingWrite(timeout, unit);
			return 0;
		}

		return blockingWrite(timeout, unit);
	}

	/**
	 * Send an acknowledgment.
	 * 
	 * @throws Exception
	 */
	public void sendAck() throws Exception {

		if (!committed) {
			this.bbuf.clear();
			this.bbuf.put(Constants.ACK_BYTES).flip();
			if (this.write(writeTimeout, TimeUnit.MILLISECONDS) < 0) {
				throw new IOException(MESSAGES.failedWrite());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.coyote.http11.AbstractInternalOutputBuffer#doWrite(org.apache
	 * .tomcat.util.buf.ByteChunk, org.apache.coyote.Response)
	 */
	public int doWrite(ByteChunk chunk, Response res) throws IOException {

		if (!committed) {
			// Send the connector a request for commit. The connector should
			// then validate the headers, send them (using sendHeaders) and
			// set the filters accordingly.
			response.action(ActionCode.ACTION_COMMIT, null);
		}

		// If non blocking (event) and there are leftover bytes,
		// and lastWrite was 0 -> error
		if (leftover.getLength() > 0 && !(Http11NioProcessor.containerThread.get() == Boolean.TRUE)) {
			throw new IOException(MESSAGES.invalidBacklog());
		}

		if (lastActiveFilter == -1) {
			return outputBuffer.doWrite(chunk, res);
		} else {
			return activeFilters[lastActiveFilter].doWrite(chunk, res);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.http11.AbstractInternalOutputBuffer#flushBuffer()
	 */
	protected void flushBuffer() throws IOException {
		int res = 0;

		// If there are still leftover bytes here, this means the user did a
		// direct flush:
		// - If the call is asynchronous, throw an exception
		// - If the call is synchronous, make regular blocking writes to flush
		// the data
		if (leftover.getLength() > 0) {
			if (Http11NioProcessor.containerThread.get() == Boolean.TRUE) {
				// Send leftover bytes
				while (leftover.getLength() > 0) {
					// Calculate the maximum number of bytes that can fit in the
					// buffer
					int n = Math.min(bbuf.capacity() - bbuf.position(), leftover.getLength());
					int off = leftover.getOffset();
					// Put bytes into the buffer
					bbuf.put(leftover.getBuffer(), off, n).flip();
					// Update the offset of the leftover ByteChunck
					leftover.setOffset(off + n);
					while (bbuf.hasRemaining()) {
						res = blockingWrite(writeTimeout, TimeUnit.MILLISECONDS);
						if (res < 0) {
							break;
						}
					}
					bbuf.clear();
					if (res < 0) {
						throw new IOException(MESSAGES.failedWrite());
					}
				}
				leftover.recycle();
			} else {
				throw new IOException(MESSAGES.invalidBacklog());
			}
		}

		if (bbuf.position() > 0) {
			bbuf.flip();

			if (nonBlocking) {
				// Perform non blocking writes until all data is written, or the
				// result of the write is 0
				nonBlockingWrite(writeTimeout, TimeUnit.MILLISECONDS);
			} else {
				while (bbuf.hasRemaining()) {
					res = blockingWrite(writeTimeout, TimeUnit.MILLISECONDS);
					if (res <= 0) {
						break;
					}
				}
				response.setLastWrite(res);
				// bbuf.clear();
				clearBuffer();
			}

			if (res < 0) {
				throw new IOException(MESSAGES.failedWrite());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.coyote.http11.AbstractInternalOutputBuffer#flushLeftover()
	 */
	@Override
	public boolean flushLeftover() throws IOException {
	    if (leftover.getLength() == 0) {
	        return true;
	    }
		// Calculate the number of bytes that fit in the buffer
		int n = Math.min(leftover.getLength(), bbuf.capacity() - bbuf.position());
		// put bytes in the buffer
		bbuf.put(leftover.getBuffer(), leftover.getOffset(), n).flip();
		// Update the offset
		leftover.setOffset(leftover.getOffset() + n);
		final NioChannel ch = channel;

		ch.write(bbuf, writeTimeout, TimeUnit.MILLISECONDS, null,
				new CompletionHandler<Integer, Void>() {

					@Override
					public void completed(Integer result, Void attachment) {
						if (result < 0) {
							failed(new IOException(MESSAGES.failedWrite()), attachment);
							return;
						}
						response.setLastWrite(result);
						if (!bbuf.hasRemaining()) {
							bbuf.clear();
							if (leftover.getLength() > 0) {
								int n = Math.min(leftover.getLength(), bbuf.remaining());
								bbuf.put(leftover.getBuffer(), leftover.getOffset(), n).flip();
								leftover.setOffset(leftover.getOffset() + n);
							} else {
								leftover.recycle();
								return;
							}
						}
						// Write the remaining bytes
						ch.write(bbuf, writeTimeout, TimeUnit.MILLISECONDS, null, this);
					}

					@Override
					public void failed(Throwable exc, Void attachment) {
						close(ch);
					}
				});

		return true;
	}

}
