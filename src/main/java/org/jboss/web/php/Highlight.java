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

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Highlight.
 *
 * @author Mladen Turk
 * @version $Revision: 515 $, $Date: 2008-03-17 22:02:23 +0100 (Mon, 17 Mar 2008) $
 * @since 1.0
 */
public class Highlight extends Handler
{

    /**
     * Provides PHP Highlight Gateway service
     *
     * @param  req   HttpServletRequest passed in by servlet container
     * @param  res   HttpServletResponse passed in by servlet container
     *
     * @exception  ServletException  if a servlet-specific exception occurs
     * @exception  IOException  if a read/write exception occurs
     *
     * @see org.apache.catalina.servlets.php.Handler
     *
     */
    protected void service(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        syntaxHighlight = true;
        super.service(req, res);
    }
}
