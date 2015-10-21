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

/**
 * {@code NioJSSEFactory}
 * 
 * Created on Feb 22, 2012 at 12:10:48 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class NioJSSEFactory {

	/**
	 * Returns the NioSocketChannelFactory to use.
	 * 
	 * @return the NioSocketChannelFactory to use.
	 */
	public NioJSSESocketChannelFactory getSocketChannelFactory() {
		return new NioJSSESocketChannelFactory();
	}

	/**
	 * Returns the SSLSupport attached to this channel.
	 * 
	 * @param channel
	 * @return the SSLSupport attached to this channel
	 */
	public NioJSSESupport getSSLSupport(NioChannel channel) {
		return new NioJSSESupport((SecureNioChannel) channel);
	}

	/**
	 * Return the SSLSupport attached to this session
	 * 
	 * @param session
	 * @return the SSLSupport attached to this session
	 */
	public NioJSSESupport getSSLSupport(SSLSession session) {
		return new NioJSSESupport(session);
	}

}
