/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.jni;

/** LibraryLoader
 *
 * @author Mladen Turk
 * @version $Revision: $, $Date: $
 */


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.logging.Logger;

public final class LibraryLoader {

    private static Logger log = Logger.getLogger(org.apache.catalina.core.AprLifecycleListener.class);

    public static String getDefaultPlatformName()
    {
        String name = System.getProperty("os.name");
        String platform = "unknown";

        if (name.startsWith("Windows"))
            platform = "windows";
        else if (name.startsWith("Mac OS"))
            platform = "macosx";
        else if (name.endsWith("BSD"))
            platform = "bsd";
        else if (name.equals("Linux"))
            platform = "linux2";
        else if (name.equals("Solaris"))
            platform = "solaris";
        else if (name.equals("SunOS"))
            platform = "solaris";
        else if (name.equals("HP-UX"))
            platform = "hpux";
        else if (name.equals("AIX"))
            platform = "aix";

        return platform;
    }

    private LibraryLoader()
    {
        // Disallow creation
    }

    protected static void load(String rootPath)
        throws SecurityException, IOException, UnsatisfiedLinkError
    {
        int count = 0;
        String name = getDefaultPlatformName();
        Properties props = new Properties();

        try {
            InputStream is = LibraryLoader.class.getResourceAsStream
                ("/org/apache/tomcat/jni/Library.properties");
            props.load(is);
            is.close();
            count = Integer.parseInt(props.getProperty(name + ".count"));
        }
        catch (Throwable t) {
            throw new UnsatisfiedLinkError("Can't use Library.properties for: " + name);
        }
        for (int i = 0; i < count; i++) {
            boolean optional = false;
            boolean full = false;
            String dlibName = props.getProperty(name + "." + i);
            if (dlibName.startsWith("?")) {
                dlibName = dlibName.substring(1);
                optional = true;
            }
            if (dlibName.startsWith("*")) {
                /* On windoze we can have a single library that contains all the stuff we need */
                dlibName = dlibName.substring(1);
                full = true;
            }

            /* AS7 jboss-modules takes care of the names */
            try {
                System.loadLibrary(dlibName);
                log.debug("Loaded: " + dlibName);
            } catch (Throwable d) {
                log.debug("Loading " + dlibName + " throws: " + d);
                if (optional)
                   continue;
                throw new UnsatisfiedLinkError(" Error: " + d.getMessage() + " " );
            }
            if (full)
                 break;
        }
    }

}
