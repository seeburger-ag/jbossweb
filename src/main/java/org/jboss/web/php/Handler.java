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

package org.jboss.web.php;

import static org.jboss.web.WebMessages.MESSAGES;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.web.WebLogger;

/**
 * Handler.
 *
 * @author Mladen Turk
 * @version $Revision: 1458 $, $Date: 2010-05-01 01:47:43 +0200 (Sat, 01 May 2010) $
 * @since 1.0
 */
public class Handler extends HttpServlet
{

    /** the debugging detail level for this servlet. */
    private int debug = 0;

    /** Buffer size. */
    private int bufferSize = 4096;

    /**
     * The Servlet configuration object we are associated with.  If this value
     * is null, this filter instance is not currently configured.
     */
    private ServletConfig servletConfig = null;

    /** Are doing source sysntax highlight. */
    protected boolean syntaxHighlight = false;

    /** the encoding to use for parameters */
    private String parameterEncoding = System.getProperty("file.encoding",
                                                          "UTF-8");

    /**
     *  The Script search path will start at
     *    webAppRootDir + File.separator + scriptPathPrefix
     *    (or webAppRootDir alone if scriptPathPrefix is
     *    null)
     */
    private String scriptPathPrefix = null;

    /**
     * Sets instance variables.
     * <P>
     * Modified from Craig R. McClanahan's InvokerServlet
     * </P>
     *
     * @param config                    a <code>ServletConfig</code> object
     *                                  containing the servlet's
     *                                  configuration and initialization
     *                                  parameters
     *
     * @exception ServletException      if an exception has occurred that
     *                                  interferes with the servlet's normal
     *                                  operation
     */
    public void init(ServletConfig servletConfig)
        throws ServletException
    {
        super.init(servletConfig);

        if (!Library.isInitialized()) {
            // try to load the library.
            try {
                Library.initialize(null);
            } catch(Throwable t) {
                WebLogger.ROOT_LOGGER.errorInitializingPhpLibrary(System.getProperty("java.library.path"), t);
            }
        }

        if (!Library.isInitialized())
            throw new UnavailableException(MESSAGES.errorLoadingPhp());

        this.servletConfig = servletConfig;

        // Set our properties from the initialization parameters
        String value = null;
        try {
            value = servletConfig.getInitParameter("debug");
            debug = Integer.parseInt(value);
            scriptPathPrefix =
                servletConfig.getInitParameter("scriptPathPrefix");
            value = servletConfig.getInitParameter("bufferSize");
            if (value != null) {
                bufferSize = Integer.parseInt(value);
                if (bufferSize < 1024)
                    bufferSize = 1024;
                log("init: bufferSize set to " + bufferSize);
            }
        } catch (Throwable t) {
            // Nothing.
        }

        value = servletConfig.getInitParameter("parameterEncoding");
        if (value != null) {
            parameterEncoding = value;
        }
    }

    /**
     * Finalize this servlet.
     */
    public void destroy()
    {
        this.servletConfig = null;
    }

    private static native int php(byte[] buf,
                                  ScriptEnvironment env,
                                  HttpServletRequest req,
                                  HttpServletResponse res,
                                  String requestMethod,
                                  String queryString,
                                  String contentType,
                                  String authUser,
                                  String requestURI,
                                  String pathTranslated,
                                  int contentLength,
                                  boolean syntaxHighlight);

    /**
     * Provides PHP Gateway service
     *
     * @param  req   HttpServletRequest passed in by servlet container
     * @param  res   HttpServletResponse passed in by servlet container
     *
     * @exception  ServletException  if a servlet-specific exception occurs
     * @exception  IOException  if a read/write exception occurs
     *
     * @see javax.servlet.http.HttpServlet
     *
     */
    protected void service(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {

        ScriptEnvironment env = new ScriptEnvironment(req,
                                                      getServletContext(),
                                                      scriptPathPrefix);
        if (env.isValid()) {
            byte[] buf = new byte[bufferSize];
            int rv = php(buf,
                         env,
                         req,
                         res,
                         req.getMethod(),
                         req.getQueryString(),
                         req.getContentType(),
                         req.getRemoteUser(),
                         req.getRequestURI(),
                         env.getFullPath(),
                         req.getContentLength(),
                         syntaxHighlight);
        }
        else {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    public static void log(Handler handler, String msg)
    {
        // TODO: Log the message    
    }
}
