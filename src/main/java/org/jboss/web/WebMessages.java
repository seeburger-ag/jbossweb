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
 * Logging IDs 7000-7500
 * @author Remy Maucherat
 */
@MessageBundle(projectCode = "JBWEB")
public interface WebMessages {

    /**
     * The messages
     */
    WebMessages MESSAGES = Messages.getBundle(WebMessages.class);

    @Message(id = 7000, value = "Cannot load native PHP library")
    String errorLoadingPhp();

    @Message(id = 7001, value = "Error opening rewrite configuration")
    String errorOpeningRewriteConfiguration();

    @Message(id = 7002, value = "Error reading rewrite configuration")
    String errorReadingRewriteConfiguration();

    @Message(id = 7003, value = "Error reading rewrite configuration: %s")
    IllegalArgumentException invalidRewriteConfiguration(String line);

    @Message(id = 7004, value = "Invalid rewrite map class: %s")
    IllegalArgumentException invalidRewriteMap(String className);

    @Message(id = 7005, value = "Error reading rewrite flags in line %s as %s")
    IllegalArgumentException invalidRewriteFlags(String line, String flags);

    @Message(id = 7006, value = "Error reading rewrite flags in line %s")
    IllegalArgumentException invalidRewriteFlags(String line);

}
