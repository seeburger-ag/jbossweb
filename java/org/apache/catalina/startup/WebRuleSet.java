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


import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.deploy.ContextHandler;
import org.apache.catalina.deploy.ContextService;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.deploy.WebAbsoluteOrdering;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.digester.CallMethodRule;
import org.apache.tomcat.util.digester.CallParamRule;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.Rule;
import org.apache.tomcat.util.digester.RuleSetBase;
import org.apache.tomcat.util.digester.SetNextRule;
import org.xml.sax.Attributes;


/**
 * <p><strong>RuleSet</strong> for processing the contents of a web application
 * deployment descriptor (<code>/WEB-INF/web.xml</code>) resource.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class WebRuleSet extends RuleSetBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The fragment flag
     */    
    protected boolean fragment;


    /**
     * The matching pattern prefix to use for recognizing our elements.
     */
    protected String prefix = null;
    
    
    /**
     * The <code>SetSessionConfig</code> rule used to parse the web.xml
     */
    protected SetSessionConfig sessionConfig;
    
    
    /**
     * The <code>SetLoginConfig</code> rule used to parse the web.xml
     */
    protected SetLoginConfig loginConfig;

    
    /**
     * The <code>SetJspConfig</code> rule used to parse the web.xml
     */    
    protected SetJspConfig jspConfig;


    // ------------------------------------------------------------ Constructor


    /**
     * Construct an instance of this <code>RuleSet</code> with the default
     * matching pattern prefix.
     */
    public WebRuleSet() {

        this("", false);

    }


    /**
     * Construct an instance of this <code>RuleSet</code> with the specified
     * matching pattern prefix.
     *
     * @param prefix Prefix for matching pattern rules (including the
     *  trailing slash character)
     */
    public WebRuleSet(String prefix, boolean fragment) {

        super();
        this.fragment = fragment;
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
        String elementName = "web-app";
        if (fragment) {
            elementName = "web-fragment";
        }
        sessionConfig = new SetSessionConfig();
        jspConfig = new SetJspConfig();
        loginConfig = new SetLoginConfig();
        
        digester.addRule(prefix + elementName,
                         new SetPublicIdRule("setPublicId"));
        digester.addRule(prefix + elementName,
                         new IgnoreAnnotationsRule());

        digester.addCallMethod(prefix + elementName + "/context-param",
                               "addParameter", 2);
        digester.addCallParam(prefix + elementName + "/context-param/param-name", 0);
        digester.addCallParam(prefix + elementName + "/context-param/param-value", 1);

        digester.addCallMethod(prefix + elementName + "/display-name",
                               "setDisplayName", 0);

        digester.addRule(prefix + elementName + "/distributable",
                         new SetDistributableRule());

        configureNamingRules(digester);

        digester.addObjectCreate(prefix + elementName + "/error-page",
                                 "org.apache.catalina.deploy.ErrorPage");
        digester.addSetNext(prefix + elementName + "/error-page",
                            "addErrorPage",
                            "org.apache.catalina.deploy.ErrorPage");

        digester.addCallMethod(prefix + elementName + "/error-page/error-code",
                               "setErrorCode", 0);
        digester.addCallMethod(prefix + elementName + "/error-page/exception-type",
                               "setExceptionType", 0);
        digester.addCallMethod(prefix + elementName + "/error-page/location",
                               "setLocation", 0);

        digester.addObjectCreate(prefix + elementName + "/filter",
                                 "org.apache.catalina.deploy.FilterDef");
        digester.addSetNext(prefix + elementName + "/filter",
                            "addFilterDef",
                            "org.apache.catalina.deploy.FilterDef");

        digester.addCallMethod(prefix + elementName + "/filter/description",
                               "setDescription", 0);
        digester.addCallMethod(prefix + elementName + "/filter/display-name",
                               "setDisplayName", 0);
        digester.addCallMethod(prefix + elementName + "/filter/filter-class",
                               "setFilterClass", 0);
        digester.addCallMethod(prefix + elementName + "/filter/filter-name",
                               "setFilterName", 0);
        digester.addCallMethod(prefix + elementName + "/filter/large-icon",
                               "setLargeIcon", 0);
        digester.addCallMethod(prefix + elementName + "/filter/small-icon",
                               "setSmallIcon", 0);

        digester.addCallMethod(prefix + elementName + "/filter/init-param",
                               "addInitParameter", 2);
        digester.addCallParam(prefix + elementName + "/filter/init-param/param-name",
                              0);
        digester.addCallParam(prefix + elementName + "/filter/init-param/param-value",
                              1);

        digester.addObjectCreate(prefix + elementName + "/filter-mapping",
                                 "org.apache.catalina.deploy.FilterMap");
        digester.addSetNext(prefix + elementName + "/filter-mapping",
                                 "addFilterMap",
                                 "org.apache.catalina.deploy.FilterMap");

        digester.addCallMethod(prefix + elementName + "/filter-mapping/filter-name",
                               "setFilterName", 0);
        digester.addCallMethod(prefix + elementName + "/filter-mapping/servlet-name",
                               "addServletName", 0);
        digester.addCallMethod(prefix + elementName + "/filter-mapping/url-pattern",
                               "addURLPattern", 0);

        digester.addCallMethod(prefix + elementName + "/filter-mapping/dispatcher",
                               "setDispatcher", 0);

        digester.addRule(prefix + elementName + "/jsp-config",
                         jspConfig);
        
        digester.addCallMethod(prefix + elementName + "/jsp-config/jsp-property-group/url-pattern",
                               "addJspMapping", 0);

        digester.addCallMethod(prefix + elementName + "/listener/listener-class",
                               "addApplicationListener", 0);
        
        digester.addRule(prefix + elementName + "/login-config",
                         loginConfig);

        digester.addObjectCreate(prefix + elementName + "/login-config",
                                 "org.apache.catalina.deploy.LoginConfig");
        digester.addSetNext(prefix + elementName + "/login-config",
                            "setLoginConfig",
                            "org.apache.catalina.deploy.LoginConfig");

        digester.addCallMethod(prefix + elementName + "/login-config/auth-method",
                               "setAuthMethod", 0);
        digester.addCallMethod(prefix + elementName + "/login-config/realm-name",
                               "setRealmName", 0);
        digester.addCallMethod(prefix + elementName + "/login-config/form-login-config/form-error-page",
                               "setErrorPage", 0);
        digester.addCallMethod(prefix + elementName + "/login-config/form-login-config/form-login-page",
                               "setLoginPage", 0);

        digester.addCallMethod(prefix + elementName + "/mime-mapping",
                               "addMimeMapping", 2);
        digester.addCallParam(prefix + elementName + "/mime-mapping/extension", 0);
        digester.addCallParam(prefix + elementName + "/mime-mapping/mime-type", 1);


        digester.addObjectCreate(prefix + elementName + "/security-constraint",
                                 "org.apache.catalina.deploy.SecurityConstraint");
        digester.addSetNext(prefix + elementName + "/security-constraint",
                            "addConstraint",
                            "org.apache.catalina.deploy.SecurityConstraint");

        digester.addRule(prefix + elementName + "/security-constraint/auth-constraint",
                         new SetAuthConstraintRule());
        digester.addCallMethod(prefix + elementName + "/security-constraint/auth-constraint/role-name",
                               "addAuthRole", 0);
        digester.addCallMethod(prefix + elementName + "/security-constraint/display-name",
                               "setDisplayName", 0);
        digester.addCallMethod(prefix + elementName + "/security-constraint/user-data-constraint/transport-guarantee",
                               "setUserConstraint", 0);

        digester.addObjectCreate(prefix + elementName + "/security-constraint/web-resource-collection",
                                 "org.apache.catalina.deploy.SecurityCollection");
        digester.addSetNext(prefix + elementName + "/security-constraint/web-resource-collection",
                            "addCollection",
                            "org.apache.catalina.deploy.SecurityCollection");
        digester.addCallMethod(prefix + elementName + "/security-constraint/web-resource-collection/http-method",
                               "addMethod", 0);
        digester.addCallMethod(prefix + elementName + "/security-constraint/web-resource-collection/url-pattern",
                               "addPattern", 0);
        digester.addCallMethod(prefix + elementName + "/security-constraint/web-resource-collection/web-resource-name",
                               "setName", 0);

        digester.addCallMethod(prefix + elementName + "/security-role/role-name",
                               "addSecurityRole", 0);

        digester.addRule(prefix + elementName + "/servlet",
                         new WrapperCreateRule());
        digester.addSetNext(prefix + elementName + "/servlet",
                            "addChild",
                            "org.apache.catalina.Container");

        digester.addCallMethod(prefix + elementName + "/servlet/description",
                "setDescription", 0);

        digester.addCallMethod(prefix + elementName + "/servlet/init-param",
                               "addInitParameter", 2);
        digester.addCallParam(prefix + elementName + "/servlet/init-param/param-name",
                              0);
        digester.addCallParam(prefix + elementName + "/servlet/init-param/param-value",
                              1);

        digester.addCallMethod(prefix + elementName + "/servlet/jsp-file",
                               "setJspFile", 0);
        digester.addCallMethod(prefix + elementName + "/servlet/load-on-startup",
                               "setLoadOnStartupString", 0);
        digester.addCallMethod(prefix + elementName + "/servlet/run-as/role-name",
                               "setRunAs", 0);

        digester.addCallMethod(prefix + elementName + "/servlet/security-role-ref",
                               "addSecurityReference", 2);
        digester.addCallParam(prefix + elementName + "/servlet/security-role-ref/role-link", 1);
        digester.addCallParam(prefix + elementName + "/servlet/security-role-ref/role-name", 0);

        digester.addCallMethod(prefix + elementName + "/servlet/servlet-class",
                              "setServletClass", 0);
        digester.addCallMethod(prefix + elementName + "/servlet/servlet-name",
                              "setName", 0);

        digester.addRule(prefix + elementName + "/servlet-mapping",
                               new CallMethodMultiRule("addServletMapping", 2, 0));
        digester.addCallParam(prefix + elementName + "/servlet-mapping/servlet-name", 1);
        digester.addRule(prefix + elementName + "/servlet-mapping/url-pattern", new CallParamMultiRule(0));

        digester.addRule(prefix + elementName + "/session-config",
                         sessionConfig);
        
        digester.addCallMethod(prefix + elementName + "/session-config/session-timeout",
                               "setSessionTimeout", 1,
                               new Class[] { Integer.TYPE });
        digester.addCallParam(prefix + elementName + "/session-config/session-timeout", 0);

        digester.addCallMethod(prefix + elementName + "/taglib",
                               "addTaglib", 2);
        digester.addCallParam(prefix + elementName + "/taglib/taglib-location", 1);
        digester.addCallParam(prefix + elementName + "/taglib/taglib-uri", 0);

        digester.addCallMethod(prefix + elementName + "/welcome-file-list/welcome-file",
                               "addWelcomeFile", 0);

        digester.addCallMethod(prefix + elementName + "/locale-encoding-mapping-list/locale-encoding-mapping",
                              "addLocaleEncodingMappingParameter", 2);
        digester.addCallParam(prefix + elementName + "/locale-encoding-mapping-list/locale-encoding-mapping/locale", 0);
        digester.addCallParam(prefix + elementName + "/locale-encoding-mapping-list/locale-encoding-mapping/encoding", 1);

        // absolute-ordering rules
        if (!fragment) {
            digester.addCallMethod(prefix + elementName + "/name", "setLogicalName", 0);
            digester.addObjectCreate(prefix + elementName + "/absolute-ordering",
                    "org.apache.catalina.deploy.WebAbsoluteOrdering");
            digester.addSetNext(prefix + elementName + "/absolute-ordering",
                    "setWebAbsoluteOrdering",
                    "org.apache.catalina.deploy.WebAbsoluteOrdering");
            digester.addCallMethod(prefix + elementName + "/absolute-ordering/name",
                    "addName", 0);
            digester.addRule(prefix + elementName + "/absolute-ordering/others",
                    new AddOthersRule());
        }

    }

    protected void configureNamingRules(Digester digester) {
        String elementName = "web-app";
        if (fragment) {
            elementName = "web-fragment";
        }
        //ejb-local-ref
        digester.addObjectCreate(prefix + elementName + "/ejb-local-ref",
                                 "org.apache.catalina.deploy.ContextLocalEjb");
        digester.addRule(prefix + elementName + "/ejb-local-ref",
                new SetNextNamingRule("addLocalEjb",
                            "org.apache.catalina.deploy.ContextLocalEjb"));

        digester.addCallMethod(prefix + elementName + "/ejb-local-ref/description",
                               "setDescription", 0);
        digester.addCallMethod(prefix + elementName + "/ejb-local-ref/ejb-link",
                               "setLink", 0);
        digester.addCallMethod(prefix + elementName + "/ejb-local-ref/ejb-ref-name",
                               "setName", 0);
        digester.addCallMethod(prefix + elementName + "/ejb-local-ref/ejb-ref-type",
                               "setType", 0);
        digester.addCallMethod(prefix + elementName + "/ejb-local-ref/local",
                               "setLocal", 0);
        digester.addCallMethod(prefix + elementName + "/ejb-local-ref/local-home",
                               "setHome", 0);
        configureInjectionRules(digester, elementName + "/ejb-local-ref/");

        //ejb-ref
        digester.addObjectCreate(prefix + elementName + "/ejb-ref",
                                 "org.apache.catalina.deploy.ContextEjb");
        digester.addRule(prefix + elementName + "/ejb-ref",
                new SetNextNamingRule("addEjb",
                            "org.apache.catalina.deploy.ContextEjb"));

        digester.addCallMethod(prefix + elementName + "/ejb-ref/description",
                               "setDescription", 0);
        digester.addCallMethod(prefix + elementName + "/ejb-ref/ejb-link",
                               "setLink", 0);
        digester.addCallMethod(prefix + elementName + "/ejb-ref/ejb-ref-name",
                               "setName", 0);
        digester.addCallMethod(prefix + elementName + "/ejb-ref/ejb-ref-type",
                               "setType", 0);
        digester.addCallMethod(prefix + elementName + "/ejb-ref/home",
                               "setHome", 0);
        digester.addCallMethod(prefix + elementName + "/ejb-ref/remote",
                               "setRemote", 0);
        configureInjectionRules(digester, elementName + "/ejb-ref/");

        //env-entry
        digester.addObjectCreate(prefix + elementName + "/env-entry",
                                 "org.apache.catalina.deploy.ContextEnvironment");
        digester.addRule(prefix + elementName + "/env-entry",
                new SetNextNamingRule("addEnvironment",
                            "org.apache.catalina.deploy.ContextEnvironment"));

        digester.addCallMethod(prefix + elementName + "/env-entry/description",
                               "setDescription", 0);
        digester.addCallMethod(prefix + elementName + "/env-entry/env-entry-name",
                               "setName", 0);
        digester.addCallMethod(prefix + elementName + "/env-entry/env-entry-type",
                               "setType", 0);
        digester.addCallMethod(prefix + elementName + "/env-entry/env-entry-value",
                               "setValue", 0);
        configureInjectionRules(digester, elementName + "/env-entry/");

        //resource-env-ref
        digester.addObjectCreate(prefix + elementName + "/resource-env-ref",
            "org.apache.catalina.deploy.ContextResourceEnvRef");
        digester.addRule(prefix + elementName + "/resource-env-ref",
                    new SetNextNamingRule("addResourceEnvRef",
                        "org.apache.catalina.deploy.ContextResourceEnvRef"));

        digester.addCallMethod(prefix + elementName + "/resource-env-ref/resource-env-ref-name",
                "setName", 0);
        digester.addCallMethod(prefix + elementName + "/resource-env-ref/resource-env-ref-type",
                "setType", 0);
        configureInjectionRules(digester, elementName + "/ejb-local-ref/");

        //message-destination
        digester.addObjectCreate(prefix + elementName + "/message-destination",
                                 "org.apache.catalina.deploy.MessageDestination");
        digester.addSetNext(prefix + elementName + "/message-destination",
                            "addMessageDestination",
                            "org.apache.catalina.deploy.MessageDestination");

        digester.addCallMethod(prefix + elementName + "/message-destination/description",
                               "setDescription", 0);
        digester.addCallMethod(prefix + elementName + "/message-destination/display-name",
                               "setDisplayName", 0);
        digester.addCallMethod(prefix + elementName + "/message-destination/icon/large-icon",
                               "setLargeIcon", 0);
        digester.addCallMethod(prefix + elementName + "/message-destination/icon/small-icon",
                               "setSmallIcon", 0);
        digester.addCallMethod(prefix + elementName + "/message-destination/message-destination-name",
                               "setName", 0);

        //message-destination-ref
        digester.addObjectCreate(prefix + elementName + "/message-destination-ref",
                                 "org.apache.catalina.deploy.MessageDestinationRef");
        digester.addSetNext(prefix + elementName + "/message-destination-ref",
                            "addMessageDestinationRef",
                            "org.apache.catalina.deploy.MessageDestinationRef");

        digester.addCallMethod(prefix + elementName + "/message-destination-ref/description",
                               "setDescription", 0);
        digester.addCallMethod(prefix + elementName + "/message-destination-ref/message-destination-link",
                               "setLink", 0);
        digester.addCallMethod(prefix + elementName + "/message-destination-ref/message-destination-ref-name",
                               "setName", 0);
        digester.addCallMethod(prefix + elementName + "/message-destination-ref/message-destination-type",
                               "setType", 0);
        digester.addCallMethod(prefix + elementName + "/message-destination-ref/message-destination-usage",
                               "setUsage", 0);

        configureInjectionRules(digester, elementName + "/message-destination-ref/");

        //resource-ref
        digester.addObjectCreate(prefix + elementName + "/resource-ref",
                                 "org.apache.catalina.deploy.ContextResource");
        digester.addRule(prefix + elementName + "/resource-ref",
                new SetNextNamingRule("addResource",
                            "org.apache.catalina.deploy.ContextResource"));

        digester.addCallMethod(prefix + elementName + "/resource-ref/description",
                               "setDescription", 0);
        digester.addCallMethod(prefix + elementName + "/resource-ref/res-auth",
                               "setAuth", 0);
        digester.addCallMethod(prefix + elementName + "/resource-ref/res-ref-name",
                               "setName", 0);
        digester.addCallMethod(prefix + elementName + "/resource-ref/res-sharing-scope",
                               "setScope", 0);
        digester.addCallMethod(prefix + elementName + "/resource-ref/res-type",
                               "setType", 0);
        configureInjectionRules(digester, elementName + "/resource-ref/");

        //service-ref
        digester.addObjectCreate(prefix + elementName + "/service-ref",
                                 "org.apache.catalina.deploy.ContextService");
        digester.addRule(prefix + elementName + "/service-ref",
                         new SetNextNamingRule("addService",
                         "org.apache.catalina.deploy.ContextService"));

        digester.addCallMethod(prefix + elementName + "/service-ref/description",
                               "setDescription", 0);
        digester.addCallMethod(prefix + elementName + "/service-ref/display-name",
                               "setDisplayname", 0);
        digester.addCallMethod(prefix + elementName + "/service-ref/icon",
                               "setIcon", 0);
        digester.addCallMethod(prefix + elementName + "/service-ref/service-ref-name",
                               "setName", 0);
        digester.addCallMethod(prefix + elementName + "/service-ref/service-interface",
                               "setType", 0);
        digester.addCallMethod(prefix + elementName + "/service-ref/wsdl-file",
                               "setWsdlfile", 0);
        digester.addCallMethod(prefix + elementName + "/service-ref/jaxrpc-mapping-file",
                               "setJaxrpcmappingfile", 0);
        digester.addRule(prefix + elementName + "/service-ref/service-qname", new ServiceQnameRule());

        digester.addRule(prefix + elementName + "/service-ref/port-component-ref",
                               new CallMethodMultiRule("addPortcomponent", 2, 1));
        digester.addCallParam(prefix + elementName + "/service-ref/port-component-ref/service-endpoint-interface", 0);
        digester.addRule(prefix + elementName + "/service-ref/port-component-ref/port-component-link", 
                new CallParamMultiRule(1));

        digester.addObjectCreate(prefix + elementName + "/service-ref/handler",
                                 "org.apache.catalina.deploy.ContextHandler");
        digester.addRule(prefix + elementName + "/service-ref/handler",
                         new SetNextRule("addHandler",
                         "org.apache.catalina.deploy.ContextHandler"));

        digester.addCallMethod(prefix + elementName + "/service-ref/handler/handler-name",
                               "setName", 0);
        digester.addCallMethod(prefix + elementName + "/service-ref/handler/handler-class",
                               "setHandlerclass", 0);

        digester.addCallMethod(prefix + elementName + "/service-ref/handler/init-param",
                               "setProperty", 2);
        digester.addCallParam(prefix + elementName + "/service-ref/handler/init-param/param-name",
                              0);
        digester.addCallParam(prefix + elementName + "/service-ref/handler/init-param/param-value",
                              1);

        digester.addRule(prefix + elementName + "/service-ref/handler/soap-header", new SoapHeaderRule());

        digester.addCallMethod(prefix + elementName + "/service-ref/handler/soap-role",
                               "addSoapRole", 0);
        digester.addCallMethod(prefix + elementName + "/service-ref/handler/port-name",
                               "addPortName", 0);
        configureInjectionRules(digester, elementName + "/service-ref/");


    }

    protected void configureInjectionRules(Digester digester, String base) {

        digester.addCallMethod(prefix + base + "injection-target", "addInjectionTarget", 2);
        digester.addCallParam(prefix + base + "injection-target/injection-target-class", 0);
        digester.addCallParam(prefix + base + "injection-target/injection-target-name", 1);

    }


    /**
     * Reset counter used for validating the web.xml file.
     */
    public void recycle(){
        jspConfig.isJspConfigSet = false;
        sessionConfig.isSessionConfigSet = false;
        loginConfig.isLoginConfigSet = false;
    }
}


// ----------------------------------------------------------- Private Classes


/**
 * Rule to check that the <code>login-config</code> is occuring 
 * only 1 time within the web.xml
 */
final class SetLoginConfig extends Rule {
    protected boolean isLoginConfigSet = false;
    public SetLoginConfig() {
    }

    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {
        if (isLoginConfigSet){
            throw new IllegalArgumentException(
            "<login-config> element is limited to 1 occurrence");
        }
        isLoginConfigSet = true;
    }

}


/**
 * Rule to check that the <code>jsp-config</code> is occuring 
 * only 1 time within the web.xml
 */
final class SetJspConfig extends Rule {
    protected boolean isJspConfigSet = false;
    public SetJspConfig() {
    }

    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {
        if (isJspConfigSet){
            throw new IllegalArgumentException(
            "<jsp-config> element is limited to 1 occurrence");
        }
        isJspConfigSet = true;
    }

}


/**
 * Rule to check that the <code>session-config</code> is occuring 
 * only 1 time within the web.xml
 */
final class SetSessionConfig extends Rule {
    protected boolean isSessionConfigSet = false;
    public SetSessionConfig() {
    }

    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {
        if (isSessionConfigSet){
            throw new IllegalArgumentException(
            "<session-config> element is limited to 1 occurrence");
        }
        isSessionConfigSet = true;
    }

}

/**
 * A Rule that calls the <code>setAuthConstraint(true)</code> method of
 * the top item on the stack, which must be of type
 * <code>org.apache.catalina.deploy.SecurityConstraint</code>.
 */

final class SetAuthConstraintRule extends Rule {

    public SetAuthConstraintRule() {
    }

    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {
        SecurityConstraint securityConstraint =
            (SecurityConstraint) digester.peek();
        securityConstraint.setAuthConstraint(true);
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger()
               .debug("Calling SecurityConstraint.setAuthConstraint(true)");
        }
    }

}


/**
 * Class that calls <code>setDistributable(true)</code> for the top object
 * on the stack, which must be a <code>org.apache.catalina.Context</code>.
 */

final class SetDistributableRule extends Rule {

    public SetDistributableRule() {
    }

    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {
        Context context = (Context) digester.peek();
        context.setDistributable(true);
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug
               (context.getClass().getName() + ".setDistributable( true)");
        }
    }

}


/**
 * Class that calls a property setter for the top object on the stack,
 * passing the public ID of the entity we are currently processing.
 */

final class SetPublicIdRule extends Rule {

    public SetPublicIdRule(String method) {
        this.method = method;
    }

    private String method = null;

    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {

        Context context = (Context) digester.peek(digester.getCount() - 1);
        Object top = digester.peek();
        Class paramClasses[] = new Class[1];
        paramClasses[0] = "String".getClass();
        String paramValues[] = new String[1];
        paramValues[0] = digester.getPublicId();

        Method m = null;
        try {
            m = top.getClass().getMethod(method, paramClasses);
        } catch (NoSuchMethodException e) {
            digester.getLogger().error("Can't find method " + method + " in "
                                       + top + " CLASS " + top.getClass());
            return;
        }

        m.invoke(top, (Object [])paramValues);
        if (digester.getLogger().isDebugEnabled())
            digester.getLogger().debug("" + top.getClass().getName() + "." 
                                       + method + "(" + paramValues[0] + ")");

    }

}


/**
 * A Rule that calls the factory method on the specified Context to
 * create the object that is to be added to the stack.
 */

final class WrapperCreateRule extends Rule {

    public WrapperCreateRule() {
    }

    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {
        Context context =
            (Context) digester.peek(digester.getCount() - 1);
        Wrapper wrapper = context.createWrapper();
        digester.push(wrapper);
        if (digester.getLogger().isDebugEnabled())
            digester.getLogger().debug("new " + wrapper.getClass().getName());
    }

    public void end(String namespace, String name)
        throws Exception {
        Wrapper wrapper = (Wrapper) digester.pop();
        if (digester.getLogger().isDebugEnabled())
            digester.getLogger().debug("pop " + wrapper.getClass().getName());
    }

}


/**
 * A Rule that can be used to call multiple times a method as many times as needed
 * (used for addServletMapping).
 */
final class CallParamMultiRule extends CallParamRule {

    public CallParamMultiRule(int paramIndex) {
        super(paramIndex);
    }

    public void end(String namespace, String name) {
        if (bodyTextStack != null && !bodyTextStack.empty()) {
            // what we do now is push one parameter onto the top set of parameters
            Object parameters[] = (Object[]) digester.peekParams();
            ArrayList params = (ArrayList) parameters[paramIndex];
            if (params == null) {
                params = new ArrayList();
                parameters[paramIndex] = params;
            }
            params.add(bodyTextStack.pop());
        }
    }

}


/**
 * A Rule that can be used to call multiple times a method as many times as needed
 * (used for addServletMapping).
 */
final class CallMethodMultiRule extends CallMethodRule {

    protected int multiParamIndex = 0;
    
    public CallMethodMultiRule(String methodName, int paramCount, int multiParamIndex) {
        super(methodName, paramCount);
        this.multiParamIndex = multiParamIndex;
    }

    public void end() throws Exception {

        // Retrieve or construct the parameter values array
        Object parameters[] = null;
        if (paramCount > 0) {
            parameters = (Object[]) digester.popParams();
        } else {
            super.end();
        }
        
        ArrayList multiParams = (ArrayList) parameters[multiParamIndex];
        
        // Construct the parameter values array we will need
        // We only do the conversion if the param value is a String and
        // the specified paramType is not String. 
        Object paramValues[] = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (i != multiParamIndex) {
                // convert nulls and convert stringy parameters 
                // for non-stringy param types
                if(parameters[i] == null || (parameters[i] instanceof String 
                        && !String.class.isAssignableFrom(paramTypes[i]))) {
                    paramValues[i] =
                        IntrospectionUtils.convert((String) parameters[i], paramTypes[i]);
                } else {
                    paramValues[i] = parameters[i];
                }
            }
        }

        // Determine the target object for the method call
        Object target;
        if (targetOffset >= 0) {
            target = digester.peek(targetOffset);
        } else {
            target = digester.peek(digester.getCount() + targetOffset);
        }

        if (target == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("[CallMethodRule]{");
            sb.append("");
            sb.append("} Call target is null (");
            sb.append("targetOffset=");
            sb.append(targetOffset);
            sb.append(",stackdepth=");
            sb.append(digester.getCount());
            sb.append(")");
            throw new org.xml.sax.SAXException(sb.toString());
        }
        
        if (multiParams == null) {
            paramValues[multiParamIndex] = null;
            Object result = IntrospectionUtils.callMethodN(target, methodName,
                    paramValues, paramTypes);   
            return;
        }
        
        for (int j = 0; j < multiParams.size(); j++) {
            Object param = multiParams.get(j);
            if(param == null || (param instanceof String 
                    && !String.class.isAssignableFrom(paramTypes[multiParamIndex]))) {
                paramValues[multiParamIndex] =
                    IntrospectionUtils.convert((String) param, paramTypes[multiParamIndex]);
            } else {
                paramValues[multiParamIndex] = param;
            }
            Object result = IntrospectionUtils.callMethodN(target, methodName,
                    paramValues, paramTypes);   
        }
        
    }

}



/**
 * A Rule that check if the annotations have to be loaded.
 * 
 */

final class IgnoreAnnotationsRule extends Rule {

    public IgnoreAnnotationsRule() {
    }

    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {
        Context context = (Context) digester.peek(digester.getCount() - 1);
        String value = attributes.getValue("metadata-complete");
        if ("true".equals(value)) {
            context.setIgnoreAnnotations(true);
        }
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug
                (context.getClass().getName() + ".setIgnoreAnnotations( " +
                    context.getIgnoreAnnotations() + ")");
        }
    }

}

/**
 * A Rule that sets soap headers on the ContextHandler.
 * 
 */
final class SoapHeaderRule extends Rule {

    public SoapHeaderRule() {
    }

    public void body(String text)
        throws Exception {
        String namespaceuri = null;
        String localpart = text;
        int colon = text.indexOf(':');
        if (colon >= 0) {
            String prefix = text.substring(0,colon);
            namespaceuri = digester.findNamespaceURI(prefix);
            localpart = text.substring(colon+1);
        }
        ContextHandler contextHandler = (ContextHandler)digester.peek();
        contextHandler.addSoapHeaders(localpart,namespaceuri);
    }
}

/**
 * A Rule that sets service qname on the ContextService.
 * 
 */
final class ServiceQnameRule extends Rule {

    public ServiceQnameRule() {
    }

    public void body(String text)
        throws Exception {
        String namespaceuri = null;
        String localpart = text;
        int colon = text.indexOf(':');
        if (colon >= 0) {
            String prefix = text.substring(0,colon);
            namespaceuri = digester.findNamespaceURI(prefix);
            localpart = text.substring(colon+1);
        }
        ContextService contextService = (ContextService)digester.peek();
        contextService.setServiceqnameLocalpart(localpart);
        contextService.setServiceqnameNamespaceURI(namespaceuri);
    }
}

final class AddOthersRule extends Rule {
    public AddOthersRule() {
    }

    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {
        WebAbsoluteOrdering ordering = (WebAbsoluteOrdering) digester.peek();
        ordering.addName("*");
    }

}
