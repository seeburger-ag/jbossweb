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

import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;
import static org.jboss.logging.Logger.Level.DEBUG;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

/**
 * Logging IDs 5000-6000
 * @author remm
 */
@MessageLogger(projectCode = "JBWEB")
public interface JasperLogger extends BasicLogger {

    /**
     * A logger with the category of the package name.
     */
    JasperLogger ROOT_LOGGER = Logger.getMessageLogger(JasperLogger.class, "org.apache.jasper");

    /**
     * A logger with the category of the package name.
     */
    JasperLogger COMPILER_LOGGER = Logger.getMessageLogger(JasperLogger.class, "org.apache.jasper.compiler");

    /**
     * A logger with the category of the package name.
     */
    JasperLogger SERVLET_LOGGER = Logger.getMessageLogger(JasperLogger.class, "org.apache.jasper.servlet");

    @LogMessage(level = WARN)
    @Message(id = 5000, value = "Invalid %s value for the initParam keepgenerated. Will use the default value of \"false\"")
    void invalidKeepGeneratedValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5001, value = "Invalid %s value for the initParam trimSpaces. Will use the default value of \"false\"")
    void invalidTrimSpacesValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5002, value = "Invalid %s value for the initParam enablePooling. Will use the default value of \"false\"")
    void invalidEnablePoolingValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5003, value = "Invalid %s value for the initParam mappedfile. Will use the default value of \"true\"")
    void invalidMappedFileValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5004, value = "Invalid %s value for the initParam sendErrToClient. Will use the default value of \"false\"")
    void invalidSendErrToClientValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5005, value = "Invalid %s value for the initParam classdebuginfo. Will use the default value of \"true\"")
    void invalidClassDebugInfoValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5006, value = "Invalid %s value for the initParam checkInterval. Will disable periodic checking")
    void invalidCheckIntervalValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5007, value = "Invalid %s value for the initParam modificationTestInterval. Will use the default value of \"4\" seconds")
    void invalidModificationTestIntervalValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5008, value = "Invalid %s value for the initParam recompileOnFail. Will use the default value of \"false\"")
    void invalidRecompileOnFailValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5009, value = "Invalid %s value for the initParam development. Will use the default value of \"true\"")
    void invalidDevelopmentValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5010, value = "Invalid %s value for the initParam suppressSmap. Will use the default value of \"false\"")
    void invalidSuppressSmapValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5011, value = "Invalid %s value for the initParam dumpSmap. Will use the default value of \"false\"")
    void invalidDumpSmapValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5012, value = "Invalid %s value for the initParam genStrAsCharArray. Will use the default value of \"false\"")
    void invalidGenStrAsCharArrayValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5013, value = "Invalid %s value for the initParam errorOnUseBeanInvalidClassAttribute. Will use the default value of \"true\"")
    void invalidErrorOnUseBeanInvalidClassAttributeValue(String value);

    @LogMessage(level = ERROR)
    @Message(id = 5014, value = "The JSP container needs a work directory")
    void missingWorkDirectory();

    @LogMessage(level = ERROR)
    @Message(id = 5015, value = "The JSP container needs a valid work directory [%s]")
    void missingWorkDirectory(String workDirectory);

    @LogMessage(level = WARN)
    @Message(id = 5016, value = "Invalid %s value for the initParam fork. Will use the default value of \"true\"")
    void invalidForkValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5017, value = "Invalid %s value for the initParam xpoweredBy. Will use the default value of \"true\"")
    void invalidXpoweredByValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5018, value = "Invalid %s value for the initParam displaySourceFragment. Will use the default value of \"true\"")
    void invalidDisplaySourceFragmentValue(String value);

    @LogMessage(level = WARN)
    @Message(id = 5019, value = "Failed loading Java compiler %s")
    void failedLoadingJavaCompiler(String className, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 5020, value = "Failed loading custom options class %s")
    void failedLoadingOptions(String className, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 5021, value = "File \"%s\" not found")
    void fileNotFound(String uri);

    @LogMessage(level = ERROR)
    @Message(id = 5022, value = "Error destroying JSP Servlet instance")
    void errorDestroyingServletInstance(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 5023, value = "Bad value %s in the url-pattern subelement in the webapp descriptor")
    void invalidJspPropertyGroupsUrlPattern(String value);

    @LogMessage(level = DEBUG)
    @Message(id = 5024, value = "Exception closing reader")
    void errorClosingReader(@Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 5025, value = "Parent class loader is: %s")
    void logParentClassLoader(String parentClassLoader);

    @LogMessage(level = DEBUG)
    @Message(id = 5026, value = "Compilation classpath: %s")
    void logCompilationClasspath(String classpath);

}
