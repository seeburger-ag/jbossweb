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

package org.jboss.servlet.http;

import java.io.IOException;

/**
 * Upgradable HTTP Servlet response.
 * 
 * @author Remy Maucherat
 */
public interface UpgradableHttpServletResponse {

    /**
     * Start the connection upgrade process. After calling this method,
     * data will be available raw from the connection. Calling this method
     * is optional if no read/write are needed during the upgrade process.
     */
    public void startUpgrade();

    /**
     * Send the switching protocol HTTP status and commit the response by
     * flushing the buffer.
     */
    public void sendUpgrade()
            throws IOException;

}
