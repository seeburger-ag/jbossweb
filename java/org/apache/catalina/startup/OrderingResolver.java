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


package org.apache.catalina.startup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.catalina.deploy.WebOrdering;

/**
 * Resolves the relative ordering of web fragments. This is in a separate class
 * because of the relative complexity.
 * 
 * @author Remy Maucherat
 */
public class OrderingResolver {

    protected class Ordering {
        protected WebOrdering ordering;
        protected List<WebOrdering> after = new ArrayList<WebOrdering>();
        protected List<WebOrdering> before = new ArrayList<WebOrdering>();
        protected boolean afterOthers = false;
        protected boolean beforeOthers = false;
    }

    /**
     * Generate the Jar processing order.
     * 
     * @param webOrderings The list of orderings, as parsed from the fragments
     * @param order The generated order list
     */
    public static void resolveOrder(List<WebOrdering> webOrderings, List<String> order) {
        
    }
    
}
