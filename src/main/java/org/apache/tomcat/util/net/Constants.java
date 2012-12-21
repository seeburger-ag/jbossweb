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

package org.apache.tomcat.util.net;

/**
 * Constants.
 *
 * @author Remy Maucherat
 */
public final class Constants {

    public static final int MAX_THREADS = 
            Integer.valueOf(System.getProperty("org.apache.tomcat.util.net.MAX_THREADS", "-1")).intValue();

    public static final boolean WAIT_FOR_THREAD = 
        Boolean.valueOf(System.getProperty("org.apache.tomcat.util.net.WAIT_FOR_THREAD", "false")).booleanValue();

    public static final boolean REUSE_ADDRESS = 
            Boolean.valueOf(System.getProperty("org.apache.tomcat.util.net.REUSE_ADDRESS", "true")).booleanValue();

    public static final int SO_RCV_BUFFER = 
            Integer.valueOf(System.getProperty("org.apache.tomcat.util.net.SO_RCV_BUFFER", "-1")).intValue();

    public static final int SO_SND_BUFFER = 
            Integer.valueOf(System.getProperty("org.apache.tomcat.util.net.SO_SND_BUFFER", "-1")).intValue();

    /**
     * The Request attribute key for the cipher suite.
     */
    public static final String CIPHER_SUITE_KEY = "javax.servlet.request.cipher_suite";

    /**
     * The Request attribute key for the key size.
     */
    public static final String KEY_SIZE_KEY = "javax.servlet.request.key_size";

    /**
     * The Request attribute key for the client certificate chain.
     */
    public static final String CERTIFICATE_KEY = "javax.servlet.request.X509Certificate";

    /**
     * The Request attribute key for the session id.
     * This one is a Tomcat extension to the Servlet spec.
     */
    public static final String SESSION_ID_KEY = "javax.servlet.request.ssl_session_id";


}
