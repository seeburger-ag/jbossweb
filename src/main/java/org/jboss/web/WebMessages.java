/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.web;

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;

/**
 * Logging IDs 7000-7500
 * @author remm
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
