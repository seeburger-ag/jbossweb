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


package org.apache.catalina.core;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.annotation.InitParam;
import javax.servlet.annotation.ServletFilter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.WebServletContextListener;

import org.apache.catalina.Context;
import org.apache.naming.resources.Resource;

public class AnnotationScanner {

    // FIXME: probably should not be static, and tied to a Context, to allow pluggability ?
    
    public static final Class<?>[] ANNOTATIONS_TYPES =
    { InitParam.class, ServletFilter.class, WebServlet.class, WebServletContextListener.class };
    
    public static final boolean USE_JAVASSIST = 
        Boolean.valueOf(System.getProperty("org.apache.catalina.core.AnnotationScanner.USE_JAVASSIST", "false")).booleanValue();

    /**
     * Scan the given context's default locations for annotations.
     * 
     * @param context
     */
    public static void scan(Context context) {
        // FIXME: obviously needs to return a result
        
        if (context.getLoader().findLoaderRepositories() != null) {
            String[] repositories = context.getLoader().findLoaderRepositories();
            for (int i = 0; i < repositories.length; i++) {
                System.out.println("Repo: " + repositories[i]);
                if (repositories[i].endsWith(".jar")) {
                    try {
                        scanJar(context, new JarFile(repositories[i]));
                    } catch (IOException e) {
                        // Ignore
                    }
                } else {
                    scanClasses(context, new File(repositories[i]), "");
                }
            }
        }

        /*
        DirContext resources = context.getResources();
        DirContext webInfClasses = null;
        DirContext webInfLib = null;

        try {
            webInfClasses = (DirContext) resources.lookup("/WEB-INF/classes");
        } catch (Exception e) {
            // Ignore, /WEB-INF/classes not found, or not a folder
        }
        if (webInfClasses != null) {
            scanClasses(context, webInfClasses, "");
        }
        
        try {
            webInfLib = (DirContext) resources.lookup("/WEB-INF/lib");
        } catch (Exception e) {
            // Ignore, /WEB-INF/classes not found, or not a folder
        }
        if (webInfLib != null) {
            scanJars(context, webInfLib);
        }*/
        
    }
    
    
    /**
     * Scan folder containing class files.
     */
    public static void scanClasses(Context context, File folder, String path) {
        String[] files = folder.list();
        for (int i = 0; i < files.length; i++) {
            File file = new File(folder, files[i]);
            if (file.isDirectory()) {
                scanClasses(context, file, path + "/" + files[i]);
            } else if (files[i].endsWith(".class")) {
                String className = getClassName(path + "/" + files[i]);
                scanClass(context, className, file, null);
            }
        }
    }
    
    
    /**
     * Scan folder containing class files.
     */
    /*
    public static void scanClasses(Context context, DirContext folder, String path) {
        try {
            NamingEnumeration<Binding> enumeration = folder.listBindings(path);
            while (enumeration.hasMore()) {
                Binding binding = enumeration.next();
                Object object = binding.getObject();
                
                if (object instanceof Resource) {
                    // This is a class, so we should load it
                    String className = getClassName(path + "/" + binding.getName());
                    scanClass(context, className, (Resource) object, null, null);
                } else if (object instanceof DirContext) {
                    scanClasses(context, folder, path + "/" + binding.getName());
                }
                
            }            
        } catch (NamingException e) {
            // Ignore for now
            e.printStackTrace();
        }
    }*/
    
    
    /**
     * Scan folder containing JAR files.
     */
    public static void scanJars(Context context, DirContext folder) {
        if (context.getLoader().findLoaderRepositories() != null) {
            String[] repositories = context.getLoader().findLoaderRepositories();
            for (int i = 0; i < repositories.length; i++) {
                System.out.println("Repo: " + repositories[i]);
                if (repositories[i].endsWith(".jar")) {
                    try {
                        scanJar(context, new JarFile(repositories[i]));
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }
        /*else {
            try {
                NamingEnumeration<Binding> enumeration = folder.listBindings("");
                while (enumeration.hasMore()) {
                    Binding binding = enumeration.next();
                    Object object = binding.getObject();

                    if (object instanceof Resource && binding.getName().endsWith(".jar")) {
                        // This is normally a JAR, put it in the work folder
                        File destDir = null;
                        File workDir =
                            (File) context.getServletContext().getAttribute(Globals.WORK_DIR_ATTR);
                        destDir = new File(workDir, "WEB-INF/lib");
                        destDir.mkdirs();
                        File destFile = new File(destDir, binding.getName());
                        
                        scanJar(context, (Resource) object);
                    }

                }            
            } catch (NamingException e) {
                // Ignore for now
                e.printStackTrace();
            }
        }*/
    }
    
    
    /**
     * Scan all class files in the given JAR.
     */
    public static void scanJar(Context context, JarFile file) {
        Enumeration<JarEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class")) {
                scanClass(context, getClassName(entry.getName()), null, entry);
            }
        }
        try {
            file.close();
        } catch (IOException e) {
            // Ignore
        }
    }
    
    
    /**
     * Get class name given a path to a classfile.
     * /my/class/MyClass.class -> my.class.MyClass
     */
    public static String getClassName(String filePath) {
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        if (filePath.endsWith(".class")) {
            filePath = filePath.substring(0, filePath.length() - ".class".length());
        }
        return filePath.replace('/', '.');
    }
    
    
    /**
     * Scan class for interesting annotations.
     */
    public static boolean scanClass(Context context, String className, File file, JarEntry entry) {
        if (USE_JAVASSIST) {
            // FIXME: Javassist implementation
        } else {
            // Load the class using the classloader, and see if it implements one of the web annotations
            try {
                System.out.println("Scan class: " + className);
                Class<?> clazz = context.getLoader().getClassLoader().loadClass(className);
            } catch (Throwable t) {
                // Ignore classloading errors here
                System.out.println("CL Error: " + t);
            }
        }
        return false;
    }
    
    
}
