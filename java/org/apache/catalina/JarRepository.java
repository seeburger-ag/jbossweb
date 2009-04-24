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


package org.apache.catalina;

import java.util.jar.JarFile;


/**
 * A JARRepository manages a set of JARs associated with a Context, and
 * allows efficient access to them.
 * 
 * @author Remy Maucherat
 * @version $Revision: 515 $ $Date: 2008-03-17 22:02:23 +0100 (Mon, 17 Mar 2008) $
 */
public interface JarRepository {


    // ------------------------------------------------------------- Properties


    /**
     * Return the Container with which this JARRepository has been associated.
     */
    public Container getContainer();


    /**
     * Set the Container with which this JARRepository has been associated.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container);


    /**
     * Return descriptive information about this JARRepository implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();


    // --------------------------------------------------------- Public Methods


    /**
     * Execute a periodic task, such as reloading, etc. This method will be
     * invoked inside the classloading context of this container. Unexpected
     * throwables will be caught and logged.
     */
    public void backgroundProcess();


    /**
     * Get a JarFile with the specified name.
     * 
     * @param name
     * @return
     */
    public JarFile getJar(String name);


    /**
     * Find all JarFile managed by the JARRepository.
     * 
     * @return All JarFile
     */
    public JarFile[] findJars();


}
