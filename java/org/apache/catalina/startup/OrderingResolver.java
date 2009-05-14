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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.catalina.deploy.WebOrdering;
import org.apache.catalina.util.StringManager;

/**
 * Resolves the relative ordering of web fragments. This is in a separate class
 * because of the relative complexity.
 * 
 * @author Remy Maucherat
 */
public class OrderingResolver {

    protected static org.jboss.logging.Logger log =
        org.jboss.logging.Logger.getLogger(OrderingResolver.class);

    /**
     * The string resources for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);

    protected static class Ordering {
        protected WebOrdering ordering;
        protected Set<Ordering> after = new HashSet<Ordering>();
        protected Set<Ordering> before = new HashSet<Ordering>();
        protected boolean afterOthers = false;
        protected boolean beforeOthers = false;
        
        public boolean addAfter(Ordering ordering) {
            return after.add(ordering);
        }
        
        public boolean addBefore(Ordering ordering) {
            return before.add(ordering);
        }
        
        public void validate() {
            isBefore(new Ordering());
            isAfter(new Ordering());
        }
        
        /**
         * Check (recursively) if a fragment is before the specified fragment.
         * 
         * @param ordering
         * @return
         */
        public boolean isBefore(Ordering ordering) {
            return isBeforeInternal(ordering, new HashSet<Ordering>());
        }
        
        protected boolean isBeforeInternal(Ordering ordering, Set<Ordering> checked) {
            checked.add(this);
            if (before.contains(ordering)) {
                return true;
            }
            Iterator<Ordering> beforeIterator = before.iterator();
            while (beforeIterator.hasNext()) {
                Ordering check = beforeIterator.next();
                if (checked.contains(check)) {
                    throw new IllegalStateException(sm.getString("ordering.orderConflict", this.ordering.getJar()));
                }
                if (check.isBeforeInternal(ordering, checked)) {
                    return false;
                }
            }
            return false;
        }
        
        /**
         * Check (recursively) if a fragment is after the specified fragment.
         * 
         * @param ordering
         * @return
         */
        public boolean isAfter(Ordering ordering) {
            return isAfterInternal(ordering, new HashSet<Ordering>());
        }
        
        protected boolean isAfterInternal(Ordering ordering, Set<Ordering> checked) {
            checked.add(this);
            if (after.contains(ordering)) {
                return true;
            }
            Iterator<Ordering> afterIterator = after.iterator();
            while (afterIterator.hasNext()) {
                Ordering check = afterIterator.next();
                if (checked.contains(check)) {
                    throw new IllegalStateException(sm.getString("ordering.orderConflict", this.ordering.getJar()));
                }
                if (check.isAfterInternal(ordering, checked)) {
                    return false;
                }
            }
            return false;
        }
        
        /**
         * Check is a fragment marked as before others is after a fragment that is not.
         * 
         * @return true if a fragment marked as before others is after a fragment that is not
         */
        public boolean isLastBeforeOthers() {
            if (!beforeOthers) {
                throw new IllegalStateException();
            }
            Iterator<Ordering> beforeIterator = before.iterator();
            while (beforeIterator.hasNext()) {
                Ordering check = beforeIterator.next();
                if (!check.beforeOthers) {
                    return true;
                } else if (check.isLastBeforeOthers()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Check is a fragment marked as after others is before a fragment that is not.
         * 
         * @return true if a fragment marked as after others is before a fragment that is not
         */
        public boolean isFirstAfterOthers() {
            if (!afterOthers) {
                throw new IllegalStateException();
            }
            Iterator<Ordering> afterIterator = after.iterator();
            while (afterIterator.hasNext()) {
                Ordering check = afterIterator.next();
                if (!check.afterOthers) {
                    return true;
                } else if (check.isFirstAfterOthers()) {
                    return true;
                }
            }
            return false;
        }
        
    }

    /**
     * Generate the Jar processing order.
     * 
     * @param webOrderings The list of orderings, as parsed from the fragments
     * @param order The generated order list
     */
    public static void resolveOrder(List<WebOrdering> webOrderings, List<String> order) {
        List<Ordering> work = new ArrayList<Ordering>();
        
        // Populate the work Ordering list
        Iterator<WebOrdering> webOrderingsIterator = webOrderings.iterator();
        while (webOrderingsIterator.hasNext()) {
            WebOrdering webOrdering = webOrderingsIterator.next();
            Ordering ordering = new Ordering();
            ordering.ordering = webOrdering;
            ordering.afterOthers = webOrdering.isAfterOthers();
            ordering.beforeOthers = webOrdering.isBeforeOthers();
            if (ordering.afterOthers && ordering.beforeOthers) {
                // Cannot be both after and before others
                throw new IllegalStateException(sm.getString("ordering.afterAndBeforeOthers", webOrdering.getJar()));
            }
            work.add(ordering);
        }
        
        // Create double linked relationships between the orderings,
        // and resolve names
        Iterator<Ordering> workIterator = work.iterator();
        while (workIterator.hasNext()) {
            Ordering ordering = workIterator.next();
            WebOrdering webOrdering = ordering.ordering;
            Iterator<String> after = webOrdering.getAfter().iterator();
            while (after.hasNext()) {
                String name = after.next();
                Iterator<Ordering> workIterator2 = work.iterator();
                boolean found = false;
                while (workIterator2.hasNext()) {
                    Ordering ordering2 = workIterator2.next();
                    if (name.equals(ordering2.ordering.getName())) {
                        if (found) {
                            // Duplicate name
                            throw new IllegalStateException(sm.getString("ordering.duplicateName", webOrdering.getJar()));
                        }
                        ordering.addAfter(ordering2);
                        ordering2.addBefore(ordering);
                        found = true;
                    }
                }
                if (!found) {
                    // Unknown name
                    throw new IllegalStateException(sm.getString("ordering.unkonwnName", webOrdering.getJar()));
                }
            }
            Iterator<String> before = webOrdering.getBefore().iterator();
            while (before.hasNext()) {
                String name = before.next();
                Iterator<Ordering> workIterator2 = work.iterator();
                boolean found = false;
                while (workIterator2.hasNext()) {
                    Ordering ordering2 = workIterator2.next();
                    if (name.equals(ordering2.ordering.getName())) {
                        if (found) {
                            // Duplicate name
                            throw new IllegalStateException(sm.getString("ordering.duplicateName", webOrdering.getJar()));
                        }
                        ordering.addBefore(ordering2);
                        ordering2.addAfter(ordering);
                        found = true;
                    }
                }
                if (!found) {
                    // Unknown name
                    throw new IllegalStateException(sm.getString("ordering.unkonwnName", webOrdering.getJar()));
                }
            }
        }
        
        // Validate ordering
        workIterator = work.iterator();
        while (workIterator.hasNext()) {
            workIterator.next().validate();
        }
        
        // Create three ordered lists that will then be merged
        List<Ordering> tempOrder = new ArrayList<Ordering>();

        // Create the ordered list of fragments which are before others
        workIterator = work.iterator();
        while (workIterator.hasNext()) {
            Ordering ordering = workIterator.next();
            if (ordering.beforeOthers) {
                // Insert at the first possible position
                int insertAfter = -1;
                boolean last = ordering.isLastBeforeOthers();
                int lastBeforeOthers = -1;
                for (int i = 0; i < tempOrder.size(); i++) {
                    if (ordering.isAfter(tempOrder.get(i))) {
                        insertAfter = i;
                    }
                    if (tempOrder.get(i).beforeOthers) {
                        lastBeforeOthers = i;
                    }
                }
                int pos = insertAfter;
                if (last && lastBeforeOthers > insertAfter) {
                    pos = lastBeforeOthers;
                }
                tempOrder.add(pos + 1, ordering);
            } else if (ordering.afterOthers) {
                // Insert at the last possible element
                int insertBefore = tempOrder.size();
                boolean first = ordering.isFirstAfterOthers();
                int firstAfterOthers = tempOrder.size();
                for (int i = tempOrder.size() - 1; i >= 0; i--) {
                    if (ordering.isBefore(tempOrder.get(i))) {
                        insertBefore = i;
                    }
                    if (tempOrder.get(i).afterOthers) {
                        firstAfterOthers = i;
                    }
                }
                int pos = insertBefore;
                if (first && firstAfterOthers < insertBefore) {
                    pos = firstAfterOthers;
                }
                tempOrder.add(pos, ordering);
            } else {
                // Insert according to other already inserted elements
                int insertAfter = -1;
                int insertBefore = tempOrder.size();
                for (int i = 0; i < tempOrder.size(); i++) {
                    if (ordering.isAfter(tempOrder.get(i)) || tempOrder.get(i).beforeOthers) {
                        insertAfter = i;
                    }
                    if (ordering.isBefore(tempOrder.get(i)) || tempOrder.get(i).afterOthers) {
                        insertBefore = i;
                    }
                }
                if (insertAfter > insertBefore) {
                    // Conflicting order (probably caught earlier)
                    throw new IllegalStateException(sm.getString("ordering.orderConflict", ordering.ordering.getJar()));
                }
                // Insert somewhere in the range
                tempOrder.add(insertAfter + 1, ordering);
            }
        }
        
        // Create the final ordered list
        Iterator<Ordering> tempOrderIterator = tempOrder.iterator();
        while (tempOrderIterator.hasNext()) {
            Ordering ordering = tempOrderIterator.next();
            order.add(ordering.ordering.getJar());
        }
        
    }
    
    
    public static void main(String args[]) {
        ArrayList<String> order = new ArrayList<String>();
        ArrayList<WebOrdering> webOrderings = new ArrayList<WebOrdering>();
        resolveOrder(webOrderings, order);
        
        test1();
        test2();
        test3();
    }

    public static void test1() {

        ArrayList<String> order = new ArrayList<String>();
        ArrayList<WebOrdering> webOrderings = new ArrayList<WebOrdering>();

        WebOrdering A = new WebOrdering();
        A.setName("A");
        A.setJar("A.jar");
        A.setAfterOthers(true);
        A.addAfter("C");
        webOrderings.add(A);
        
        WebOrdering B = new WebOrdering();
        B.setName("B");
        B.setJar("B.jar");
        B.setBeforeOthers(true);
        webOrderings.add(B);
        
        WebOrdering C = new WebOrdering();
        C.setName("C");
        C.setJar("C.jar");
        C.setAfterOthers(true);
        webOrderings.add(C);
        
        WebOrdering D = new WebOrdering();
        D.setName("D");
        D.setJar("D.jar");
        webOrderings.add(D);
        
        WebOrdering E = new WebOrdering();
        E.setName("E");
        E.setJar("E.jar");
        webOrderings.add(E);
        
        WebOrdering F = new WebOrdering();
        F.setName("F");
        F.setJar("F.jar");
        F.setBeforeOthers(true);
        F.addBefore("B");
        webOrderings.add(F);
        
        long n1 = System.nanoTime();
        resolveOrder(webOrderings, order);
        long n2 = System.nanoTime();
        
        System.out.print("Result: ");
        Iterator<String> orderIterator = order.iterator();
        while (orderIterator.hasNext()) {
            System.out.print(orderIterator.next() + " ");
        }
        System.out.println("ns: " + (n2 - n1));
        
    }
    
    public static void test2() {

        ArrayList<String> order = new ArrayList<String>();
        ArrayList<WebOrdering> webOrderings = new ArrayList<WebOrdering>();

        WebOrdering A = new WebOrdering();
        A.setJar("noid.jar");
        A.setAfterOthers(true);
        A.addBefore("C");
        webOrderings.add(A);
        
        WebOrdering B = new WebOrdering();
        B.setName("B");
        B.setJar("B.jar");
        B.setBeforeOthers(true);
        webOrderings.add(B);
        
        WebOrdering C = new WebOrdering();
        C.setName("C");
        C.setJar("C.jar");
        webOrderings.add(C);
        
        WebOrdering D = new WebOrdering();
        D.setName("D");
        D.setJar("D.jar");
        D.setAfterOthers(true);
        webOrderings.add(D);
        
        WebOrdering E = new WebOrdering();
        E.setName("E");
        E.setJar("E.jar");
        E.setBeforeOthers(true);
        webOrderings.add(E);
        
        WebOrdering F = new WebOrdering();
        F.setName("F");
        F.setJar("F.jar");
        webOrderings.add(F);
        
        long n1 = System.nanoTime();
        resolveOrder(webOrderings, order);
        long n2 = System.nanoTime();
        
        System.out.print("Result: ");
        Iterator<String> orderIterator = order.iterator();
        while (orderIterator.hasNext()) {
            System.out.print(orderIterator.next() + " ");
        }
        System.out.println("ns: " + (n2 - n1));
        
    }
    
    public static void test3() {

        ArrayList<String> order = new ArrayList<String>();
        ArrayList<WebOrdering> webOrderings = new ArrayList<WebOrdering>();

        WebOrdering A = new WebOrdering();
        A.setName("A");
        A.setJar("A.jar");
        A.addAfter("B");
        webOrderings.add(A);
        
        WebOrdering B = new WebOrdering();
        B.setName("B");
        B.setJar("B.jar");
        webOrderings.add(B);
        
        WebOrdering C = new WebOrdering();
        C.setName("C");
        C.setJar("C.jar");
        C.setBeforeOthers(true);
        webOrderings.add(C);
        
        WebOrdering D = new WebOrdering();
        D.setName("D");
        D.setJar("D.jar");
        webOrderings.add(D);
        
        long n1 = System.nanoTime();
        resolveOrder(webOrderings, order);
        long n2 = System.nanoTime();
        
        System.out.print("Result: ");
        Iterator<String> orderIterator = order.iterator();
        while (orderIterator.hasNext()) {
            System.out.print(orderIterator.next() + " ");
        }
        System.out.println("ns: " + (n2 - n1));
        
    }
    
}
