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
package org.apache.catalina.websocket;

import static org.jboss.web.CatalinaMessages.MESSAGES;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CoderResult;

import org.apache.tomcat.util.buf.Utf8Decoder;
import org.jboss.servlet.http.HttpEvent;

/**
 * Represents a complete WebSocket frame with the exception of the payload for
 * non-control frames.
 */
public class WsFrame {

    private final boolean fin;
    private final int rsv;
    private final byte opCode;
    private final byte[] mask = new byte[4];
    private long payloadLength;
    private final ByteBuffer payload;

    /**
     * Create the new WebSocket frame, reading data from the processor as
     * necessary.
     *
     * @param first     First byte of data for this frame
     * @param processor Processor associated with the WebSocket connection on
     *                  which the frame has been sent
     *
     * @throws IOException  If a problem occurs processing the frame. Any
     *                      exception will trigger the closing of the WebSocket
     *                      connection.
     */
    private WsFrame(byte first, HttpEvent event) throws IOException {

        int b = first & 0xFF;
        fin = (b & 0x80) > 0;
        rsv = (b & 0x70) >>> 4;
        opCode = (byte) (b & 0x0F);

        b = blockingRead(event);
        // Client data must be masked
        if ((b & 0x80) == 0) {
            throw new IOException(MESSAGES.frameNotMasked());
        }

        payloadLength = b & 0x7F;
        if (payloadLength == 126) {
            byte[] extended = new byte[2];
            blockingRead(event, extended);
            payloadLength = byteArrayToLong(extended);
        } else if (payloadLength == 127) {
            byte[] extended = new byte[8];
            blockingRead(event, extended);
            payloadLength = byteArrayToLong(extended);
        }

        if (isControl()) {
            if (payloadLength > 125) {
                throw new IOException();
            }
            if (!fin) {
                throw new IOException();
            }
        }

        blockingRead(event, mask);

        if (isControl()) {
            // Note: Payload limited to <= 125 bytes by test above
            payload = ByteBuffer.allocate((int) payloadLength);
            blockingRead(event, payload);

            if (opCode == Constants.OPCODE_CLOSE && payloadLength > 2) {
                // Check close payload - if present - is valid UTF-8
                CharBuffer cb = CharBuffer.allocate((int) payloadLength);
                Utf8Decoder decoder = new Utf8Decoder();
                payload.position(2);
                CoderResult cr = decoder.decode(payload, cb, true);
                payload.position(0);
                if (cr.isError()) {
                    throw new IOException(MESSAGES.frameInvalidUtf8());
                }
            }
        } else {
            payload = null;
        }
    }

    public static long byteArrayToLong(byte[] input) throws IOException {
        if (input.length > 8) {
            // TODO: Better message
            throw new IOException();
        }

        int shift = 0;
        long result = 0;
        for (int i = input.length - 1; i >= 0; i--) {
            result = result + ((input[i] & 0xFF) << shift);
            shift += 8;
        }

        return result;
    }

    public boolean getFin() {
        return fin;
    }

    public int getRsv() {
        return rsv;
    }

    public byte getOpCode() {
        return opCode;
    }

    public boolean isControl() {
        return (opCode & 0x08) > 0;
    }

    public byte[] getMask() {
        return mask;
    }

    public long getPayLoadLength() {
        return payloadLength;
    }

    public ByteBuffer getPayLoad() {
        return payload;
    }


    /*
     * Blocks until a aingle byte has been read
     */
    private int blockingRead(HttpEvent event)
            throws IOException {
        int result = event.getHttpServletRequest().getInputStream().read();
        if (result == -1) {
            throw new IOException(MESSAGES.frameEos());
        }
        return result;
    }


    /*
     * Blocks until the byte array has been filled.
     */
    private void blockingRead(HttpEvent event, byte[] bytes)
            throws IOException {
        int read = 0;
        int last = 0;
        while (read < bytes.length) {
            last = event.getHttpServletRequest().getInputStream().read(bytes, read, bytes.length - read);
            if (last == -1) {
                throw new IOException(MESSAGES.frameEos());
            }
            read += last;
        }
    }


    /*
     * Intended to read whole payload and blocks until it has. Therefore able to
     * unmask the payload data.
     */
    private void blockingRead(HttpEvent event, ByteBuffer bb)
            throws IOException {
        int last = 0;
        while (bb.hasRemaining()) {
            last = event.getHttpServletRequest().getInputStream().read();
            if (last == -1) {
                throw new IOException(MESSAGES.frameEos());
            }
            bb.put((byte) (last ^ mask[bb.position() % 4]));
        }
        bb.flip();
    }


    /**
     * Read the next WebSocket frame, reading data from the processor as
     * necessary.
     *
     * @param processor Processor associated with the WebSocket connection on
     *                  which the frame has been sent
     *
     * @param block Should this method block until a frame is presented if no
     *              data is currently available to process. Note that is a
     *              single byte is available, this method will block until the
     *              complete frame (excluding payload for non-control frames) is
     *              available.
     *
     * @throws IOException  If a problem occurs processing the frame. Any
     *                      exception will trigger the closing of the WebSocket
     *                      connection.
     */
    public static WsFrame nextFrame(HttpEvent event,
            boolean block) throws IOException {

        byte[] first = new byte[1];
        int read = 0;
        if (block) {
            read = event.getHttpServletRequest().getInputStream().read(first, 0, 1);
        } else {
            if (event.isReadReady()) {
                read = event.getHttpServletRequest().getInputStream().read(first, 0, 1);
            }
        }
        if (read == 1) {
            return new WsFrame(first[0], event);
        } else if (read == 0) {
            return null;
        } else if (read == -1) {
            throw new EOFException(MESSAGES.frameEos());
        } else {
            throw new IOException(MESSAGES.frameFailedRead(Integer.valueOf(read)));
        }
    }
}
