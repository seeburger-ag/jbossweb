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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;

import org.apache.tomcat.util.net.Constants;
import org.apache.tomcat.util.net.SSLSupport;
import org.jboss.web.CoyoteLogger;

/**
 * {@code NioJSSESupport}
 * 
 * Created on Jan 5, 2012 at 1:28:34 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
class NioJSSESupport implements SSLSupport {

	protected SecureNioChannel channel;
	protected SSLSession session;

	/**
	 * Create a new instance of {@code NioJSSESupport}
	 * 
	 * @param channel
	 */
	NioJSSESupport(SecureNioChannel channel) {
		this.channel = channel;
		this.session = channel.getSSLSession();
	}

	/**
	 * Create a new instance of {@code NioJSSESupport}
	 * 
	 * @param session
	 */
	NioJSSESupport(SSLSession session) {
		this.session = session;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.SSLSupport#getCipherSuite()
	 */
	@Override
	public String getCipherSuite() throws IOException {
		// Look up the current SSLSession
		return this.session == null ? null : this.session.getCipherSuite();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.SSLSupport#getPeerCertificateChain()
	 */
	@Override
	public Object[] getPeerCertificateChain() throws IOException {
		return getPeerCertificateChain(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.tomcat.util.net.SSLSupport#getPeerCertificateChain(boolean)
	 */
	@Override
	public Object[] getPeerCertificateChain(boolean force) throws IOException {
		// Look up the current SSLSession
		if (session == null) {
			return null;
		}

		// Convert JSSE's certificate format to the ones we need
		X509Certificate[] jsseCerts = null;
		try {
			jsseCerts = session.getPeerCertificateChain();
		} catch (Exception bex) {
			// ignore.
		}
		if (jsseCerts == null)
			jsseCerts = new X509Certificate[0];
		if (jsseCerts.length <= 0 && force) {
			session.invalidate();
			handShake();
			session = channel.getSSLSession();
		}
		return getX509Certificates(session);
	}

	/**
	 * 
	 * @param session
	 * @return
	 * @throws IOException
	 */
	protected java.security.cert.X509Certificate[] getX509Certificates(SSLSession session)
			throws IOException {
		Certificate[] certs = null;
		try {
			certs = session.getPeerCertificates();
		} catch (Throwable t) {
		    CoyoteLogger.UTIL_LOGGER.debug("Error getting client certs", t);
			return null;
		}
		if (certs == null)
			return null;

		java.security.cert.X509Certificate[] x509Certs = new java.security.cert.X509Certificate[certs.length];
		for (int i = 0; i < certs.length; i++) {
			if (certs[i] instanceof java.security.cert.X509Certificate) {
				// always currently true with the JSSE 1.1.x
				x509Certs[i] = (java.security.cert.X509Certificate) certs[i];
			} else {
				try {
					byte[] buffer = certs[i].getEncoded();
					CertificateFactory cf = CertificateFactory.getInstance("X.509");
					ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
					x509Certs[i] = (java.security.cert.X509Certificate) cf
							.generateCertificate(stream);
				} catch (Exception ex) {
                    CoyoteLogger.UTIL_LOGGER.errorTranslatingCertificate(certs[i], ex);
					return null;
				}
			}
			if (CoyoteLogger.UTIL_LOGGER.isTraceEnabled())
			    CoyoteLogger.UTIL_LOGGER.trace("Cert #" + i + " = " + x509Certs[i]);
		}
		if (x509Certs.length < 1)
			return null;
		return x509Certs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.SSLSupport#getKeySize()
	 */
	@Override
	public Integer getKeySize() throws IOException {
		// Look up the current SSLSession
		SSLSupport.CipherData c_aux[] = ciphers;
		if (session == null)
			return null;
		Integer keySize = (Integer) session.getValue(Constants.KEY_SIZE_KEY);
		if (keySize == null) {
			int size = 0;
			String cipherSuite = session.getCipherSuite();
			for (int i = 0; i < c_aux.length; i++) {
				if (cipherSuite.indexOf(c_aux[i].phrase) >= 0) {
					size = c_aux[i].keySize;
					break;
				}
			}
			keySize = new Integer(size);
			session.putValue(Constants.KEY_SIZE_KEY, keySize);
		}
		return keySize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.SSLSupport#getSessionId()
	 */
	@Override
	public String getSessionId() throws IOException {
		// Look up the current SSLSession
		if (session == null) {
			return null;
		}
		// Expose ssl_session (getId)
		byte[] ssl_session = session.getId();
		if (ssl_session == null) {
			return null;
		}
		StringBuilder buf = new StringBuilder("");
		for (int x = 0; x < ssl_session.length; x++) {
			String digit = Integer.toHexString((int) ssl_session[x]);
			if (digit.length() < 2) {
				buf.append('0');
			}
			if (digit.length() > 2) {
				digit = digit.substring(digit.length() - 2);
			}
			buf.append(digit);
		}
		return buf.toString();
	}

	/**
	 * 
	 * @throws IOException
	 */
	protected void handShake() throws IOException {
		if (channel != null && channel.handshakeComplete) {
			return;
		}

		if (channel != null) {
			channel.handshake();
		}
	}
}
