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


import org.apache.catalina.deploy.WebAbsoluteOrdering;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.Rule;
import org.apache.tomcat.util.digester.RuleSetBase;
import org.xml.sax.Attributes;


/**
 * <p><strong>RuleSet</strong> for processing the absolute-ordering element
 * of web.xml.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 515 $ $Date: 2008-03-17 22:02:23 +0100 (Mon, 17 Mar 2008) $
 */

public class WebAbsoluteOrderingRuleSet extends RuleSetBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The matching pattern prefix to use for recognizing our elements.
     */
    protected String prefix = null;


    // ------------------------------------------------------------ Constructor


    /**
     * Construct an instance of this <code>RuleSet</code> with the default
     * matching pattern prefix.
     */
    public WebAbsoluteOrderingRuleSet() {

        this("");

    }


    /**
     * Construct an instance of this <code>RuleSet</code> with the specified
     * matching pattern prefix.
     *
     * @param prefix Prefix for matching pattern rules (including the
     *  trailing slash character)
     */
    public WebAbsoluteOrderingRuleSet(String prefix) {

        super();
        this.namespaceURI = null;
        this.prefix = prefix;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Add the set of Rule instances defined in this RuleSet to the
     * specified <code>Digester</code> instance, associating them with
     * our namespace URI (if any).  This method should only be called
     * by a Digester instance.</p>
     *
     * @param digester Digester instance to which the new Rule instances
     *  should be added.
     */
    public void addRuleInstances(Digester digester) {

        digester.addObjectCreate(prefix + "web-app",
                               "org.apache.catalina.deploy.WebAbsoluteOrdering");
        digester.addCallMethod(prefix + "web-app/name",
                "setName", 0);
        digester.addCallMethod(prefix + "web-app/absolute-ordering/name",
                "addName", 0);
        digester.addRule(prefix + "web-app/absolute-ordering/others",
                new AddOthersRule());

    }


    final class AddOthersRule extends Rule {
        public AddOthersRule() {
        }

        public void begin(String namespace, String name, Attributes attributes)
            throws Exception {
            WebAbsoluteOrdering ordering = (WebAbsoluteOrdering) digester.peek();
            ordering.addName("");
        }

    }
}
