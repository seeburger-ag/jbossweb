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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * PHP SAPI interface.
 *
 * @author Mladen Turk
 * @version $Revision: 515 $, $Date: 2008-03-17 22:02:23 +0100 (Mon, 17 Mar 2008) $
 * @since 1.0
 */
public final class SAPI
{

    public static int write(HttpServletResponse res,
                            byte[] buf, int len)
    {
        try {
            res.getOutputStream().write(buf, 0, len);
            return len;
        } catch (IOException e) {
            return -1;
        }
    }

    public static int read(HttpServletRequest req,
                           byte[] buf, int len)
    {
        try {
            return req.getInputStream().read(buf, 0, len);
        } catch (IOException e) {
            return -1;
        }
    }

    public static void log(Handler h, String msg)
    {
        h.log("php: " + msg);
    }

    public static int flush(HttpServletResponse res)
    {
        try {
            res.getOutputStream().flush();
            return 0;
        } catch (IOException e) {
            return -1;
        }
    }

    public static void header(boolean set,
                              HttpServletResponse res,
                              String name, String value)
    {
        if (name.equalsIgnoreCase("Content-type")) {
            res.setContentType(value);
        }
        else if (name.equalsIgnoreCase("Location")) {
            try {
                res.sendRedirect(value);
            } catch (IOException e) {
                // Nothing.
            }
        }
        else {
            if (set)
                res.setHeader(name, value);
            else
                res.addHeader(name, value);
        }
    }

    public static void status(HttpServletResponse res,
                              int sc)
    {
        res.setStatus(sc);
    }

    public static String[] env(ScriptEnvironment e)
    {
        return e.getEnvironmentArray();
    }

    public static String cookies(ScriptEnvironment e)
    {
        return (String)e.getEnvironment().get("HTTP_COOKIE");
    }


}
