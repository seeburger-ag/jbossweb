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
        
        public boolean validate() {
            try {
                isBefore(new Ordering());
                isAfter(new Ordering());
            } catch (IllegalStateException e) {
                return false;
            }
            return true;
        }
        
        public boolean isBefore(Ordering ordering) {
            Set<Ordering> checked = new HashSet<Ordering>();
            return isBeforeInternal(ordering, checked);
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
                    throw new IllegalStateException();
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
            Set<Ordering> checked = new HashSet<Ordering>();
            return isAfterInternal(ordering, checked);
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
                    throw new IllegalStateException();
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
                    Ordering ordering2 = workIterator.next();
                    if (name.equals(ordering2.ordering.getName())) {
                        if (found) {
                            // Duplicate name
                            throw new IllegalStateException(sm.getString("ordering.duplicateName", webOrdering.getJar()));
                        }
                        ordering.after.add(ordering2);
                        ordering2.before.add(ordering);
                        found = true;
                    }
                }
                if (!found) {
                    // Unknown name
                    throw new IllegalStateException(sm.getString("ordering.unkonwnName", webOrdering.getJar()));
                }
            }
            Iterator<String> before = webOrdering.getAfter().iterator();
            while (before.hasNext()) {
                String name = before.next();
                Iterator<Ordering> workIterator2 = work.iterator();
                boolean found = false;
                while (workIterator2.hasNext()) {
                    Ordering ordering2 = workIterator.next();
                    if (name.equals(ordering2.ordering.getName())) {
                        if (found) {
                            // Duplicate name
                            throw new IllegalStateException(sm.getString("ordering.duplicateName", webOrdering.getJar()));
                        }
                        ordering.before.add(ordering2);
                        ordering2.after.add(ordering);
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
                int insertBefore = tempOrder.size() - 1;
                boolean first = ordering.isFirstAfterOthers();
                int firstAfterOthers = tempOrder.size() - 1;
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
                int insertBefore = tempOrder.size() - 1;
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
    
    
}
