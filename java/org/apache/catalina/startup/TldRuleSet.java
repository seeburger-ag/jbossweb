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


import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSetBase;


/**
 * <p><strong>RuleSet</strong> for processing the contents of a tag library
 * descriptor resource.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class TldRuleSet extends RuleSetBase {


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
    public TldRuleSet() {

        this("");

    }


    /**
     * Construct an instance of this <code>RuleSet</code> with the specified
     * matching pattern prefix.
     *
     * @param prefix Prefix for matching pattern rules (including the
     *  trailing slash character)
     */
    public TldRuleSet(String prefix) {

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

        // uri and prefix attributes
        digester.addSetProperties(prefix + "taglib");
        
        digester.addCallMethod(prefix + "taglib/tlibversion",
                "setTlibversion", 0);
        digester.addCallMethod(prefix + "taglib/tlib-version",
                "setTlibversion", 0);
        digester.addCallMethod(prefix + "taglib/jspversion",
                "setJspversion", 0);
        digester.addCallMethod(prefix + "taglib/jsp-version",
                "setJspversion", 0);
        digester.addCallMethod(prefix + "taglib/shortname",
                "setShortname", 0);
        digester.addCallMethod(prefix + "taglib/short-name",
                "setShortname", 0);
        digester.addCallMethod(prefix + "taglib/uri",
                "setUri", 0);
        digester.addCallMethod(prefix + "taglib/info",
                "setInfo", 0);
        digester.addCallMethod(prefix + "taglib/description",
                "setInfo", 0);
        digester.addCallMethod(prefix + "taglib/listener/listener-class",
                "addListener", 0);
        
        // validator element
        digester.addObjectCreate(prefix + "taglib/validator",
                "org.apache.catalina.deploy.jsp.TagLibraryValidatorInfo");
        digester.addSetNext(prefix + "taglib/validator",
                "setValidator",
                "org.apache.catalina.deploy.jsp.TagLibraryValidatorInfo");
        digester.addCallMethod(prefix + "taglib/validator/validator-class",
                "setValidatorClass", 0);
        digester.addCallMethod(prefix + "taglib/validator/init-param",
                "addInitParam", 2);
        digester.addCallParam(prefix + "taglib/validator/init-param/param-name", 0);
        digester.addCallParam(prefix + "taglib/validator/init-param/param-value", 1);

        // tag element
        digester.addObjectCreate(prefix + "taglib/tag",
                "org.apache.catalina.deploy.jsp.TagInfo");
        digester.addSetNext(prefix + "taglib/tag",
                "addTagInfo",
                "org.apache.catalina.deploy.jsp.TagInfo");
        digester.addCallMethod(prefix + "taglib/tag/name",
                "setTagName", 0);
        digester.addCallMethod(prefix + "taglib/tag/tagclass",
                "setTagClassName", 0);
        digester.addCallMethod(prefix + "taglib/tag/tag-class",
                "setTagClassName", 0);
        digester.addCallMethod(prefix + "taglib/tag/teiclass",
                "setTagExtraInfo", 0);
        digester.addCallMethod(prefix + "taglib/tag/tei-class",
                "setTagExtraInfo", 0);
        digester.addCallMethod(prefix + "taglib/tag/bodycontent",
                "setBodyContent", 0);
        digester.addCallMethod(prefix + "taglib/tag/body-content",
                "setBodyContent", 0);
        digester.addCallMethod(prefix + "taglib/tag/display-name",
                "setDisplayName", 0);
        digester.addCallMethod(prefix + "taglib/tag/small-icon",
                "setSmallIcon", 0);
        digester.addCallMethod(prefix + "taglib/tag/large-icon",
                "setLargeIcon", 0);
        digester.addCallMethod(prefix + "taglib/tag/icon/small-icon",
                "setSmallIcon", 0);
        digester.addCallMethod(prefix + "taglib/tag/icon/large-icon",
                "setLargeIcon", 0);
        digester.addCallMethod(prefix + "taglib/tag/info",
                "setInfoString", 0);
        digester.addCallMethod(prefix + "taglib/tag/description",
                "setInfoString", 0);
        digester.addCallMethod(prefix + "taglib/tag/dynamic-attributes",
                "setDynamicAttributes", 0);
        
        // tag/variable element
        digester.addObjectCreate(prefix + "taglib/tag/variable",
                "org.apache.catalina.deploy.jsp.TagVariableInfo");
        digester.addSetNext(prefix + "taglib/tag/variable",
                "addTagVariableInfo",
                "org.apache.catalina.deploy.jsp.TagVariableInfo");
        digester.addCallMethod(prefix + "taglib/tag/variable/name-given",
                "setNameGiven", 0);
        digester.addCallMethod(prefix + "taglib/tag/variable/name-from-attribute",
                "setNameFromAttribute", 0);
        digester.addCallMethod(prefix + "taglib/tag/variable/class",
                "setClassName", 0);
        digester.addCallMethod(prefix + "taglib/tag/variable/declare",
                "setDeclare", 0);
        digester.addCallMethod(prefix + "taglib/tag/variable/scope",
                "setScope", 0);

        // tag/attribute element
        digester.addObjectCreate(prefix + "taglib/tag/attribute",
                "org.apache.catalina.deploy.jsp.TagAttributeInfo");
        digester.addSetNext(prefix + "taglib/tag/attribute",
                "addTagAttributeInfo",
                "org.apache.catalina.deploy.jsp.TagAttributeInfo");
        digester.addCallMethod(prefix + "taglib/tag/attribute/name",
                "setName", 0);
        digester.addCallMethod(prefix + "taglib/tag/attribute/type",
                "setType", 0);
        digester.addCallMethod(prefix + "taglib/tag/attribute/rtexprvalue",
                "setReqTime", 0);
        digester.addCallMethod(prefix + "taglib/tag/attribute/required",
                "setRequired", 0);
        digester.addCallMethod(prefix + "taglib/tag/attribute/fragment",
                "setFragment", 0);
        digester.addCallMethod(prefix + "taglib/tag/attribute/description",
                "setDescription", 0);
        digester.addCallMethod(prefix + "taglib/tag/attribute/required",
                "setRequired", 0);
        digester.addCallMethod(prefix + "taglib/tag/attribute/deferred-value",
                "setDeferredValue", 0);
        digester.addCallMethod(prefix + "taglib/tag/attribute/deferred-value/type",
                "setExpectedTypeName", 0);
        digester.addCallMethod(prefix + "taglib/tag/attribute/deferred-method",
                "setDeferredMethod", 0);
        digester.addCallMethod(prefix + "taglib/tag/attribute/deferred-value/method-signature",
                "setMethodSignature", 0);

        // tag/function element
        digester.addObjectCreate(prefix + "taglib/function",
                "org.apache.catalina.deploy.jsp.FunctionInfo");
        digester.addSetNext(prefix + "taglib/function",
                "addFunctionInfo",
                "org.apache.catalina.deploy.jsp.FunctionInfo");
        digester.addCallMethod(prefix + "taglib/function/name",
                "setName", 0);
        digester.addCallMethod(prefix + "taglib/function/description",
                "setDescription", 0);
        digester.addCallMethod(prefix + "taglib/function/function-class",
                "setFunctionClass", 0);
        digester.addCallMethod(prefix + "taglib/function/function-signature",
                "setFunctionSignature", 0);

    }


}
