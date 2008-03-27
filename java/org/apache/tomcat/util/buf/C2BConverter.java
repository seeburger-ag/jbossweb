/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.util.buf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * NIO based character encoder.
 */
public final class C2BConverter {
    
    protected static org.jboss.logging.Logger log =
        org.jboss.logging.Logger.getLogger(C2BConverter.class);

    protected CharsetEncoder encoder = null;
    protected ByteBuffer bb = null;
    protected CharBuffer cb = null;

    /**
     * Create an encoder for the specified charset.
     */
    public C2BConverter(String charset) {
        encoder = Charset.forName(charset).newEncoder();
    }

    /** 
     * The encoding remain in effect, the encoder remains allocated.
     */
    public void recycle() {
        encoder.reset();
    }

    /**
     * Convert the given charaters to bytes. 
     */
    public void convert(CharChunk cc, ByteChunk bc) 
    throws IOException {
        if ((bb == null) || (bb.array() != bc.getBuffer())) {
            // Create a new byte buffer if anything changed
            bb = ByteBuffer.wrap(bc.getBuffer(), bc.getEnd(), 
                    bc.getBuffer().length - bc.getEnd());
        } else {
            // Initialize the byte buffer
            bb.position(bc.getEnd());
            bb.limit(bc.getBuffer().length);
        }
        if ((cb == null) || (cb.array() != cc.getBuffer())) {
            // Create a new char buffer if anything changed
            cb = CharBuffer.wrap(cc.getBuffer(), cc.getStart(), 
                    cc.getLength());
        } else {
            // Initialize the char buffer
            cb.position(cc.getStart());
            cb.limit(cc.getEnd());
        }
        // Parse leftover if any are present
        CoderResult result = null;
        // Do the decoding and get the results into the byte chunk and the char chunk
        result = encoder.encode(cb, bb, false);
        if (result.isError() || result.isMalformed()) {
            result.throwException();
        } else if (result.isOverflow()) {
            // Propagate current positions to the byte chunk and char chunk
            bc.setEnd(bb.position());
            cc.setOffset(cb.position());
        } else if (result.isUnderflow()) {
            // Propagate current positions to the byte chunk and char chunk
            bc.setEnd(bb.position());
            cc.setOffset(cb.position());
        }
    }
    
}
