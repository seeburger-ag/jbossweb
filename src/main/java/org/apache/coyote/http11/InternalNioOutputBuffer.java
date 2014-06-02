/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
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
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.coyote.ActionCode;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Response;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.HttpMessages;
import org.apache.tomcat.util.http.MimeHeaders;
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
public class InternalNioOutputBuffer implements OutputBuffer {

    /**
     * Associated Coyote response.
     */
    protected Response response;

    /**
     * Headers of the associated request.
     */
    protected MimeHeaders headers;

    /**
     * Committed flag.
     */
    protected boolean committed;

    /**
     * Finished flag.
     */
    protected boolean finished;

    /**
     * Pointer to the current write buffer.
     */
    protected byte[] buf;

    /**
     * Position in the buffer.
     */
    protected int pos;

    /**
     * Underlying output buffer.
     */
    protected OutputBuffer outputBuffer;

    /**
     * Filter library. Note: Filter[0] is always the "chunked" filter.
     */
    protected OutputFilter[] filterLibrary;

    /**
     * Active filter (which is actually the top of the pipeline).
     */
    protected OutputFilter[] activeFilters;

    /**
     * Index of the last active filter.
     */
    protected int lastActiveFilter;

    /**
     * Direct byte buffer used for writing.
     */
    protected ByteBuffer bbuf = null;

    /**
     * Leftover bytes which could not be written during a non blocking write.
     */
    protected ByteChunk leftover = null;

    /**
     * Non blocking mode.
     */
    protected boolean nonBlocking = false;

    /**
     * Write timeout
     */
    protected int writeTimeout = -1;

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
     * Semaphore used for waiting for completion handler.
     */
    private Semaphore semaphore = new Semaphore(1);
	
	/**
	 * Create a new instance of {@code InternalNioOutputBuffer}
	 * 
	 * @param response
	 * @param headerBufferSize
	 * @param endpoint
	 */
	public InternalNioOutputBuffer(Http11NioProcessor processor, Response response, int headerBufferSize, NioEndpoint endpoint) {

        this.response = response;
        this.headers = response.getMimeHeaders();
        buf = new byte[headerBufferSize];
        bbuf = ByteBuffer.allocateDirect(headerBufferSize);

        outputBuffer = new OutputBufferImpl();
        filterLibrary = new OutputFilter[0];
        activeFilters = new OutputFilter[0];
        lastActiveFilter = -1;

        committed = false;
        finished = false;

        leftover = new ByteChunk();
        nonBlocking = false;

        this.endpoint = endpoint;
        this.processor = processor;
        // Initialize the input buffer
        this.init();

        // Cause loading of HttpMessages
        HttpMessages.getMessage(200);

        // Cause loading of constants
        boolean res = org.apache.coyote.Constants.USE_CUSTOM_STATUS_MSG_IN_HEADER;

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
			public synchronized void completed(Integer nBytes, NioChannel attachment) {
                if (nBytes < 0) {
                    failed(new IOException(MESSAGES.failedWrite()), attachment);
                    return;
                }
                if (!bbuf.hasRemaining()) {
                    bbuf.clear();
                    if (leftover.getLength() > 0) {
                        int n = Math.min(leftover.getLength(), bbuf.remaining());
                        bbuf.put(leftover.getBuffer(), leftover.getOffset(), n).flip();
                        leftover.setOffset(leftover.getOffset() + n);
                    } else {
                        response.setLastWrite(nBytes);
                        leftover.recycle();
                        semaphore.release();
                        if (/*!processor.isProcessing() && */processor.getWriteNotification()) {
                            if (!endpoint.processChannel(attachment, SocketStatus.OPEN_WRITE)) {
                                endpoint.closeChannel(attachment);
                            }
                        }
                        return;
                    }
                }
                // Write the remaining bytes
                attachment.write(bbuf, writeTimeout, TimeUnit.MILLISECONDS, attachment, this);
			}

			@Override
			public void failed(Throwable exc, NioChannel attachment) {
                processor.getResponse().setErrorException(exc);
				endpoint.removeEventChannel(attachment);
                semaphore.release();
				if (!endpoint.processChannel(attachment, SocketStatus.ERROR)) {
				    endpoint.closeChannel(attachment);
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
     * Perform a write operation. The operation may be blocking or non-blocking
     * depending on the value of {@code nonBlocking} flag.
     * 
     * @param timeout
     *            a timeout for the operation
     * @param unit
     *            The time unit of the timeout
     * @return
     */
	protected int write(final long timeout, final TimeUnit unit) {
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

    /**
     * Write the contents of a byte chunk.
     * 
     * @param chunk
     *            byte chunk
     * @return number of bytes written
     * @throws IOException
     *             an undelying I/O error occured
     */
	public int doWrite(ByteChunk chunk, Response res) throws IOException {

		if (!committed) {
			// Send the connector a request for commit. The connector should
			// then validate the headers, send them (using sendHeaders) and
			// set the filters accordingly.
			response.action(ActionCode.ACTION_COMMIT, null);
		}

		if (lastActiveFilter == -1) {
			return outputBuffer.doWrite(chunk, res);
		} else {
			return activeFilters[lastActiveFilter].doWrite(chunk, res);
		}
	}

    /**
     * Callback to write data from the buffer.
     */
	protected void flushBuffer() throws IOException {
		int res = 0;

		if (!nonBlocking && bbuf.position() > 0) {
			bbuf.flip();

			while (bbuf.hasRemaining()) {
			    res = blockingWrite(writeTimeout, TimeUnit.MILLISECONDS);
			    if (res <= 0) {
			        break;
			    }
			}
			response.setLastWrite(res);
			bbuf.clear();

			if (res < 0) {
				throw new IOException(MESSAGES.failedWrite());
			}
		}
	}

   /**
     * Set the non blocking flag.
     * 
     * @param nonBlocking
     */
    public void setNonBlocking(boolean nonBlocking) {
        this.nonBlocking = nonBlocking;
    }

    /**
     * Get the non blocking flag value.
     * 
     * @return non blocking
     */
    public boolean getNonBlocking() {
        return nonBlocking;
    }

    /**
     * Add an output filter to the filter library.
     * 
     * @param filter
     */
    public void addFilter(OutputFilter filter) {

        OutputFilter[] newFilterLibrary = new OutputFilter[filterLibrary.length + 1];
        for (int i = 0; i < filterLibrary.length; i++) {
            newFilterLibrary[i] = filterLibrary[i];
        }
        newFilterLibrary[filterLibrary.length] = filter;
        filterLibrary = newFilterLibrary;
        activeFilters = new OutputFilter[filterLibrary.length];
    }

    /**
     * Get filters.
     * 
     * @return the list of filters
     */
    public OutputFilter[] getFilters() {
        return filterLibrary;
    }

    /**
     * Clear filters.
     */
    public void clearFilters() {
        filterLibrary = new OutputFilter[0];
        lastActiveFilter = -1;
    }

    /**
     * Add an output filter to the filter library.
     * 
     * @param filter
     */
    public void addActiveFilter(OutputFilter filter) {

        if (lastActiveFilter == -1) {
            filter.setBuffer(outputBuffer);
        } else {
            for (int i = 0; i <= lastActiveFilter; i++) {
                if (activeFilters[i] == filter)
                    return;
            }
            filter.setBuffer(activeFilters[lastActiveFilter]);
        }

        activeFilters[++lastActiveFilter] = filter;
        filter.setResponse(response);
    }

    public void removeActiveFilters() {
        // Recycle filters
        for (int i = 0; i <= lastActiveFilter; i++) {
            activeFilters[i].recycle();
        }
        lastActiveFilter = -1;
    }

    /**
     * Flush the response.
     * 
     * @throws IOException
     *             an undelying I/O error occured
     */
    public void flush() throws IOException {
        if (!committed) {

            // Send the connector a request for commit. The connector should
            // then validate the headers, send them (using sendHeader) and
            // set the filters accordingly.
            response.action(ActionCode.ACTION_COMMIT, null);
        }

        // Flush the current buffer
        flushBuffer();
    }


    /**
     * Recycle this object
     */
    public void recycle() {
        channel = null;
        // Recycle Request object
        response.recycle();
        pos = 0;
        lastActiveFilter = -1;
        committed = false;
        finished = false;
    }

    /**
     * End processing of current HTTP request. Note: All bytes of the current
     * request should have been already consumed. This method only resets all
     * the pointers so that we are ready to parse the next HTTP request.
     */
    public void nextRequest() {
        // Recycle Request object
        response.recycle();

        // Recycle filters
        for (int i = 0; i <= lastActiveFilter; i++) {
            activeFilters[i].recycle();
        }

        // Reset pointers
        byte[] leftoverBuf = leftover.getBuffer();
        if (leftoverBuf != null && leftoverBuf.length > Constants.ASYNC_BUFFER_SIZE) {
            leftover = new ByteChunk();
        } else {
            leftover.recycle();
        }
        pos = 0;
        lastActiveFilter = -1;
        committed = false;
        finished = false;
        if (nonBlocking) {
            semaphore.release();
        }
        nonBlocking = false;
    }

    /**
     * End request.
     * 
     * @throws IOException
     *             an undelying I/O error occured
     */
    public void endRequest() throws IOException {

        if (!committed) {
            // Send the connector a request for commit. The connector should
            // then validate the headers, send them (using sendHeader) and
            // set the filters accordingly.
            response.action(ActionCode.ACTION_COMMIT, null);
        }

        if (finished) {
            return;
        }

        if (lastActiveFilter != -1) {
            activeFilters[lastActiveFilter].end();
        }

        flushBuffer();
        finished = true;
    }

    // ------------------------------------------------ HTTP/1.1 Output Methods

    /**
     * Send the response status line.
     */
    public void sendStatus() {

        // Write protocol name
        write(Constants.HTTP_11_BYTES);
        buf[pos++] = Constants.SP;

        // Write status code
        int status = response.getStatus();
        switch (status) {
        case 200:
            write(Constants._200_BYTES);
            break;
        case 400:
            write(Constants._400_BYTES);
            break;
        case 404:
            write(Constants._404_BYTES);
            break;
        default:
            write(status);
        }

        buf[pos++] = Constants.SP;

        // Write message
        String message = null;
        if (org.apache.coyote.Constants.USE_CUSTOM_STATUS_MSG_IN_HEADER) {
            message = response.getMessage();
        }
        if (message == null) {
            write(HttpMessages.getMessage(status));
        } else {
            write(message.replace('\n', ' ').replace('\r', ' '));
        }

        // End the response status line
        buf[pos++] = Constants.CR;
        buf[pos++] = Constants.LF;
    }

    /**
     * Send a header.
     * 
     * @param name
     *            Header name
     * @param value
     *            Header value
     */
    public void sendHeader(MessageBytes name, MessageBytes value) {
        if (name.getLength() > 0 && !value.isNull()) {
            write(name);
            buf[pos++] = Constants.COLON;
            buf[pos++] = Constants.SP;
            write(value);
            buf[pos++] = Constants.CR;
            buf[pos++] = Constants.LF;
        }
    }

    /**
     * Send a header.
     * 
     * @param name
     *            Header name
     * @param value
     *            Header value
     */
    public void sendHeader(ByteChunk name, ByteChunk value) {
        write(name);
        buf[pos++] = Constants.COLON;
        buf[pos++] = Constants.SP;
        write(value);
        buf[pos++] = Constants.CR;
        buf[pos++] = Constants.LF;
    }

    /**
     * Send a header.
     * 
     * @param name
     *            Header name
     * @param value
     *            Header value
     */
    public void sendHeader(String name, String value) {
        write(name);
        buf[pos++] = Constants.COLON;
        buf[pos++] = Constants.SP;
        write(value);
        buf[pos++] = Constants.CR;
        buf[pos++] = Constants.LF;
    }

    /**
     * End the header block.
     */
    public void endHeaders() {
        buf[pos++] = Constants.CR;
        buf[pos++] = Constants.LF;
    }

    /**
     * Commit the response.
     * 
     * @throws IOException
     *             an undelying I/O error occured
     */
    protected void commit() throws IOException {

        // The response is now committed
        committed = true;
        response.setCommitted(true);

        if (pos > 0) {
            // Sending the response header buffer
            bbuf.clear();
            bbuf.put(buf, 0, pos);
        }
    }

    /**
     * This method will write the contents of the specyfied message bytes buffer
     * to the output stream, without filtering. This method is meant to be used
     * to write the response header.
     * 
     * @param mb
     *            data to be written
     */
    protected void write(MessageBytes mb) {
        if (mb == null) {
            return;
        }

        switch (mb.getType()) {
        case MessageBytes.T_BYTES:
            write(mb.getByteChunk());
            break;
        case MessageBytes.T_CHARS:
            write(mb.getCharChunk());
            break;
        default:
            write(mb.toString());
            break;
        }
    }

    /**
     * This method will write the contents of the specyfied message bytes buffer
     * to the output stream, without filtering. This method is meant to be used
     * to write the response header.
     * 
     * @param bc
     *            data to be written
     */
    protected void write(ByteChunk bc) {
        // Writing the byte chunk to the output buffer
        int length = bc.getLength();
        System.arraycopy(bc.getBytes(), bc.getStart(), buf, pos, length);
        pos = pos + length;
    }

    /**
     * This method will write the contents of the specyfied char buffer to the
     * output stream, without filtering. This method is meant to be used to
     * write the response header.
     * 
     * @param cc
     *            data to be written
     */
    protected void write(CharChunk cc) {
        int start = cc.getStart();
        int end = cc.getEnd();
        char[] cbuf = cc.getBuffer();
        for (int i = start; i < end; i++) {
            char c = cbuf[i];
            // Note: This is clearly incorrect for many strings,
            // but is the only consistent approach within the current
            // servlet framework. It must suffice until servlet output
            // streams properly encode their output.
            if (((c <= 31) && (c != 9)) || c == 127 || c > 255) {
                c = ' ';
            }
            buf[pos++] = (byte) c;
        }

    }

    /**
     * This method will write the contents of the specyfied byte buffer to the
     * output stream, without filtering. This method is meant to be used to
     * write the response header.
     * 
     * @param b
     *            data to be written
     */
    public void write(byte[] b) {
        // Writing the byte chunk to the output buffer
        System.arraycopy(b, 0, buf, pos, b.length);
        pos = pos + b.length;
    }

    /**
     * This method will write the contents of the specyfied String to the output
     * stream, without filtering. This method is meant to be used to write the
     * response header.
     * 
     * @param s
     *            data to be written
     */
    protected void write(String s) {
        if (s == null) {
            return;
        }

        // From the Tomcat 3.3 HTTP/1.0 connector
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            // Note: This is clearly incorrect for many strings,
            // but is the only consistent approach within the current
            // servlet framework. It must suffice until servlet output
            // streams properly encode their output.
            if (((c <= 31) && (c != 9)) || c == 127 || c > 255) {
                c = ' ';
            }

            buf[pos++] = (byte) c;
        }
    }

    /**
     * This method will print the specified integer to the output stream,
     * without filtering. This method is meant to be used to write the response
     * header.
     * 
     * @param i
     *            data to be written
     */
    protected void write(int i) {
        write(String.valueOf(i));
    }

    // ----------------------------------- OutputBufferImpl Inner Class

    /**
     * {@code OutputBufferImpl} This class is an output buffer which will write
     * data to an output stream/channel.
     * 
     * Created on Jan 10, 2012 at 12:20:15 PM
     * 
     * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
     */
    class OutputBufferImpl implements OutputBuffer {

        /**
         * Write chunk.
         */
        public int doWrite(ByteChunk chunk, Response res) throws IOException {
            if (nonBlocking) {
                // If the buffer is growing and flow control is not used, autoblock if in a container thread 
                if (leftover.getLength() > Constants.ASYNC_BUFFER_SIZE && response.getFlushLeftovers()
                        && Http11AbstractProcessor.containerThread.get() == Boolean.TRUE) {
                    try {
                        if (semaphore.tryAcquire(writeTimeout, TimeUnit.MILLISECONDS))
                            semaphore.release();
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
                synchronized (completionHandler) {
                    leftover.append(chunk);
                    if (leftover.getLength() > Constants.ASYNC_BUFFER_SIZE) {
                        response.setLastWrite(0);
                    }
                    if (semaphore.tryAcquire()) {
                        // Calculate the number of bytes that fit in the buffer
                        int n = Math.min(leftover.getLength(), bbuf.capacity() - bbuf.position());
                        bbuf.put(leftover.getBuffer(), leftover.getOffset(), n).flip();
                        leftover.setOffset(leftover.getOffset() + n);
                        boolean writeNotification = processor.getWriteNotification();
                        processor.setWriteNotification(false);
                        try {
                            channel.write(bbuf, writeTimeout, TimeUnit.MILLISECONDS, channel, completionHandler);
                        } catch (Exception e) {
                            processor.getResponse().setErrorException(e);
                            if (CoyoteLogger.HTTP_LOGGER.isDebugEnabled()) {
                                CoyoteLogger.HTTP_LOGGER.errorWithNonBlockingWrite(e);
                            }
                        }
                        if (writeNotification && bbuf.hasRemaining()) {
                            // Write did not complete inline, possible write notification
                            processor.setWriteNotification(writeNotification);
                        }
                    }
                }
            } else {
                int len = chunk.getLength();
                int start = chunk.getStart();
                byte[] b = chunk.getBuffer();
                while (len > 0) {
                    int thisTime = len;
                    if (!bbuf.hasRemaining()) {
                        flushBuffer();
                    }
                    if (thisTime > bbuf.remaining()) {
                        thisTime = bbuf.remaining();
                    }

                    bbuf.put(b, start, thisTime);
                    len = len - thisTime;
                    start = start + thisTime;
                }
            }
            return chunk.getLength();
        }
    }

}
