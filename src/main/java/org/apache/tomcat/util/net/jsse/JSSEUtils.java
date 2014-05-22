/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2011 Red Hat, Inc. and/or its affiliates, and individual
 * contributors as indicated by the @author tags.
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.tomcat.util.net.jsse.openssl.OpenSSLCipherConfigurationParser;

/**
 * Utility methods.
 *
 * @author <a href="mailto:ehugonne@redhat.com">Emmanuel Hugonnet</a> (c) 2014 Red Hat, inc.
 */
public final class JSSEUtils {

    public static String[] getEnabledCiphers(final String[] cipherSuites, final String[] supportedCiphers) {
        return resolveEnabledCipherSuite(cipherSuites, new HashSet<String>(Arrays.asList(supportedCiphers)));
    }

    static String[] resolveEnabledCipherSuite(final String[] cipherSuites, final Set<String> supportedCiphers) {
        Set<String> result = new LinkedHashSet<String>();
        if (cipherSuites.length == 1) {
            List<String> enabledCiphers = OpenSSLCipherConfigurationParser.parseExpression(cipherSuites[0]);
            for (String enabledCipher : enabledCiphers) {
                if (supportedCiphers.contains(enabledCipher)) {
                    result.add(enabledCipher);
                }
            }
        } else {
            for (String enabledCipher : cipherSuites) {
                if (supportedCiphers.contains(enabledCipher)) {
                    result.add(enabledCipher);
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

}
