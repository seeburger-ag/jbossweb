/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina.startup;

import java.io.File;
import java.util.jar.JarEntry;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;

import org.apache.catalina.Context;

public class ClassLoadingContextScanner
    extends BaseContextScanner {

    /**
     * Scan class for interesting annotations.
     */
    public Class<?> scanClass(Context context, String className, File file, JarEntry entry) {
        // Load the class using the classloader, and see if it implements one of the web annotations
        try {
            Class<?> clazz = context.getLoader().getClassLoader().loadClass(className);
            if (handlesTypesArray != null) {
                for (int i = 0; i < handlesTypesArray.length; i++) {
                    if (handlesTypesArray[i].isAssignableFrom(clazz)) {
                        JarServletContainerInitializerServiceImpl jarServletContainerInitializerService = 
                            handlesTypes.get(handlesTypesArray[i]);
                        jarServletContainerInitializerService.addInterestClassName(clazz.getName());
                    }
                }
            }
            if (clazz.isAnnotationPresent(MultipartConfig.class)
                    || clazz.isAnnotationPresent(WebFilter.class)
                    || clazz.isAnnotationPresent(WebInitParam.class)
                    || clazz.isAnnotationPresent(WebListener.class)
                    || clazz.isAnnotationPresent(WebServlet.class)) {
                return clazz;
            }
        } catch (Throwable t) {
            // Ignore classloading errors here
        }
        return null;
    }
    
    
}
