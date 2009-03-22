/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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


package org.apache.catalina.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.naming.directory.DirContext;

import org.apache.catalina.AnnotationScanner;
import org.apache.catalina.Context;

public abstract class BaseAnnotationScanner
    implements AnnotationScanner {

    /**
     * Scan the given context's default locations for annotations.
     * 
     * @param context
     */
    public List<Class<?>> scan(Context context) {
        ArrayList<Class<?>> result = new ArrayList<Class<?>>();
        
        if (context.getLoader().findLoaderRepositories() != null) {
            String[] repositories = context.getLoader().findLoaderRepositories();
            for (int i = 0; i < repositories.length; i++) {
                if (repositories[i].endsWith(".jar")) {
                    try {
                        scanJar(result, context, new JarFile(repositories[i]));
                    } catch (IOException e) {
                        // Ignore
                    }
                } else {
                    scanClasses(result, context, new File(repositories[i]), "");
                }
            }
        }

        return result;
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
    public void scanClasses(List<Class<?>> result, Context context, File folder, String path) {
        String[] files = folder.list();
        for (int i = 0; i < files.length; i++) {
            File file = new File(folder, files[i]);
            if (file.isDirectory()) {
                scanClasses(result, context, file, path + "/" + files[i]);
            } else if (files[i].endsWith(".class")) {
                String className = getClassName(path + "/" + files[i]);
                Class<?> annotated = scanClass(context, className, file, null);
                if (annotated != null) {
                    result.add(annotated);
                }
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
    public void scanJars(List<Class<?>> result, Context context, DirContext folder) {
        if (context.getLoader().findLoaderRepositories() != null) {
            String[] repositories = context.getLoader().findLoaderRepositories();
            for (int i = 0; i < repositories.length; i++) {
                if (repositories[i].endsWith(".jar")) {
                    try {
                        scanJar(result, context, new JarFile(repositories[i]));
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
    public void scanJar(List<Class<?>> result, Context context, JarFile file) {
        Enumeration<JarEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class")) {
                Class<?> annotated = scanClass(context, getClassName(entry.getName()), null, entry);
                if (annotated != null) {
                    result.add(annotated);
                }
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
    public String getClassName(String filePath) {
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
    public abstract Class<?> scanClass(Context context, String className, File file, JarEntry entry);
    
    
}
