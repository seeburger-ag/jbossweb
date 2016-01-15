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

import javax.net.ssl.SSLSession;

import org.apache.tomcat.util.net.NioChannel;
import org.apache.tomcat.util.net.SSLImplementation;
import org.apache.tomcat.util.net.SSLSupport;

/**
 * {@code NioJSSEImplementation}
 * 
 * Created on Feb 22, 2012 at 12:41:08 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class NioJSSEImplementation extends SSLImplementation {

	static final String SSLClass = "javax.net.ssl.SSLEngine";

	private NioJSSEFactory factory = null;

	/**
	 * Create a new instance of {@code NioJSSEImplementation}
	 * 
	 * @throws ClassNotFoundException
	 */
	public NioJSSEImplementation() throws ClassNotFoundException {
		// Check to see if JSSE is floating around somewhere
		Class.forName(SSLClass);
		factory = new NioJSSEFactory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.SSLImplementation#getImplementationName()
	 */
	public String getImplementationName() {
		return "JSSE";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.tomcat.util.net.SSLImplementation#getServerSocketChannelFactory
	 * ()
	 */
	public NioJSSESocketChannelFactory getServerSocketChannelFactory() {
		NioJSSESocketChannelFactory ssf = factory.getSocketChannelFactory();
		return ssf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.tomcat.util.net.SSLImplementation#getSSLSupport(org.apache
	 * .tomcat.util.net.NioChannel)
	 */
	public SSLSupport getSSLSupport(NioChannel channel) {
		return factory.getSSLSupport(channel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.tomcat.util.net.SSLImplementation#getSSLSupport(javax.net.
	 * ssl.SSLSession)
	 */
	public SSLSupport getSSLSupport(SSLSession session) {
		return factory.getSSLSupport(session);
	}

}
