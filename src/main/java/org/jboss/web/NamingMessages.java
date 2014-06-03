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

package org.jboss.web;

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;

/**
 * Logging IDs 6700-7000
 * @author Remy Maucherat
 */
@MessageBundle(projectCode = "JBWEB")
public interface NamingMessages {

    /**
     * The messages
     */
    NamingMessages MESSAGES = Messages.getBundle(NamingMessages.class);

    @Message(id = 6700, value = "Alias path %s is invalid")
    IllegalArgumentException invalidAliasPath(String path);

    @Message(id = 6701, value = "Invalid alias mapping %s")
    IllegalArgumentException invalidAliasMapping(String mapping);

    @Message(id = 6702, value = "Aliased path %s does not exist")
    IllegalArgumentException aliasNotFound(String path);

    @Message(id = 6703, value = "Aliased path %s is not a folder")
    IllegalArgumentException aliasNotFolder(String path);

    @Message(id = 6704, value = "Resource %s not found")
    String resourceNotFound(String path);

    @Message(id = 6705, value = "Document base cannot be null")
    IllegalArgumentException invalidNullDocumentBase();

    @Message(id = 6706, value = "Document base %s does not exist or is not a readable directory")
    IllegalArgumentException invalidBaseFolder(String docBase);

    @Message(id = 6707, value = "Unbind failed: %s")
    String resourceUnbindFailed(String path);

    @Message(id = 6708, value = "Name %s is already bound in this Context")
    String resourceAlreadyBound(String path);

    @Message(id = 6709, value = "Bind failed: %s")
    String resourceBindFailed(String path);

    @Message(id = 6710, value = "Could not get directory listing for %s")
    String failedListingFolder(String path);

    @Message(id = 6711, value = "Doc base %s must point to a WAR file")
    IllegalArgumentException docBaseNotWar(String docBase);

    @Message(id = 6712, value = "Invalid or unreadable WAR file : %s")
    IllegalArgumentException failedOpeningWar(String docBase);

}
