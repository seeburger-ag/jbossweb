/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.startup;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.HttpMethodConstraintElement;
import javax.servlet.ServletSecurityElement;
import javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;

import org.apache.catalina.Authenticator;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;
import org.jboss.web.CatalinaLogger;

/**
 * Startup event listener for a <b>Context</b> that configures the properties
 * of that Context, and the associated defined servlets.
 *
 * @author Craig R. McClanahan
 * @author Jean-Francois Arcand
 * @version $Revision: 1500 $ $Date: 2010-07-02 11:46:55 +0200 (Fri, 02 Jul 2010) $
 */

public class ContextConfig
    implements LifecycleListener {

    // ----------------------------------------------------- Instance Variables


    /**
     * Custom mappings of login methods to authenticators
     */
    protected Map customAuthenticators;


    /**
     * The set of Authenticators that we know how to configure.  The key is
     * the name of the implemented authentication method, and the value is
     * the fully qualified Java class name of the corresponding Valve.
     */
    protected static Properties authenticators = null;

    static {
        // Load our mapping properties
        authenticators = new Properties();
        try {
            InputStream is = ContextConfig.class.getClassLoader().getResourceAsStream("org/apache/catalina/startup/Authenticators.properties");
            if (is != null) {
                authenticators.load(is);
            } else {
                CatalinaLogger.STARTUP_LOGGER.cannotFindAuthenticatoMappings();
            }
        } catch (IOException e) {
            CatalinaLogger.STARTUP_LOGGER.failedLoadingAuthenticatoMappings(e);
        }
    }

    /**
     * The Context we are associated with.
     */
    protected Context context = null;


    /**
     * Track any fatal errors during startup configuration processing.
     */
    protected boolean ok = false;


    /**
     * Deployment count.
     */
    protected static long deploymentCount = 0L;
    
    
    protected static final LoginConfig DUMMY_LOGIN_CONFIG =
                                new LoginConfig("NONE", null, null, null);


    // ------------------------------------------------------------- Properties


    /**
     * Sets custom mappings of login methods to authenticators.
     *
     * @param customAuthenticators Custom mappings of login methods to
     * authenticators
     */
    public void setCustomAuthenticators(Map customAuthenticators) {
        this.customAuthenticators = customAuthenticators;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Process events for an associated Context.
     *
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        // Identify the context we are associated with
        context = (Context) event.getLifecycle();

        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT)) {
            start();
        } else if (event.getType().equals(Lifecycle.BEFORE_START_EVENT)) {
            beforeStart();
        } else if (event.getType().equals(Lifecycle.AFTER_START_EVENT)) {
            
        } else if (event.getType().equals(Context.COMPLETE_CONFIG_EVENT)) {
            completeConfig();
        } else if (event.getType().equals(Lifecycle.STOP_EVENT)) {
            stop();
        } else if (event.getType().equals(Lifecycle.INIT_EVENT)) {
            init();
        } else if (event.getType().equals(Lifecycle.DESTROY_EVENT)) {
            destroy();
        }

    }


    // -------------------------------------------------------- Protected Methods


    /**
     * Process the application configuration file, if it exists.
     */
    protected void applicationWebConfig() {
    }
    
    /**
     * Parse TLDs. This is separate, and is not subject to the order defined. Also,
     * all TLDs from all JARs are parsed.
     */
    protected void applicationTldConfig() {
        
    }
    

    /**
     * Set up an Authenticator automatically if required, and one has not
     * already been configured.
     */
    protected void authenticatorConfig() {

        // Does this Context require an Authenticator?
        SecurityConstraint constraints[] = context.findConstraints();
        if ((constraints == null) || (constraints.length == 0))
            return;
        LoginConfig loginConfig = context.getLoginConfig();
        if (loginConfig == null) {
            loginConfig = DUMMY_LOGIN_CONFIG;
            context.setLoginConfig(loginConfig);
        }

        // Has an authenticator been configured already?
        if (context instanceof Authenticator)
            return;
        if (context instanceof ContainerBase) {
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            if (pipeline != null) {
                Valve basic = pipeline.getBasic();
                if ((basic != null) && (basic instanceof Authenticator)) {
                    if (context.getAuthenticator() == null) {
                        context.setAuthenticator((Authenticator) basic);
                    }
                    return;
                }
                Valve valves[] = pipeline.getValves();
                for (int i = 0; i < valves.length; i++) {
                    if (valves[i] instanceof Authenticator) {
                        if (context.getAuthenticator() == null) {
                            context.setAuthenticator((Authenticator) valves[i]);
                        }
                        return;
                    }
                }
            }
        } else {
            return;     // Cannot install a Valve even if it would be needed
        }

        // Has a Realm been configured for us to authenticate against?
        if (context.getRealm() == null) {
            CatalinaLogger.STARTUP_LOGGER.noRealmFound();
            ok = false;
            return;
        }

        /*
         * First check to see if there is a custom mapping for the login
         * method. If so, use it. Otherwise, check if there is a mapping in
         * org/apache/catalina/startup/Authenticators.properties.
         */
        Valve authenticator = null;
        if (customAuthenticators != null) {
            authenticator = (Valve)
                customAuthenticators.get(loginConfig.getAuthMethod());
        }
        if (authenticator == null) {
            // Identify the class name of the Valve we should configure
            String authenticatorName = null;
            authenticatorName =
                    authenticators.getProperty(loginConfig.getAuthMethod());
            if (authenticatorName == null) {
                CatalinaLogger.STARTUP_LOGGER.noAuthenticatorForAuthMethod(loginConfig.getAuthMethod());
                ok = false;
                return;
            }

            // Instantiate and install an Authenticator of the requested class
            try {
                Class authenticatorClass = Class.forName(authenticatorName);
                authenticator = (Valve) authenticatorClass.newInstance();
            } catch (Throwable t) {
                CatalinaLogger.STARTUP_LOGGER.failedLoadingAuthenticator(authenticatorName, t);
                ok = false;
            }
        }

        if (authenticator instanceof Authenticator) {
            context.setAuthenticator((Authenticator) authenticator);
        }
        if (authenticator != null && context instanceof ContainerBase) {
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            if (pipeline != null) {
                ((ContainerBase) context).addValve(authenticator);
                CatalinaLogger.STARTUP_LOGGER.authenticatorConfigured(loginConfig.getAuthMethod());
            }
        }

    }


    protected String getBaseDir() {
        Container engineC=context.getParent().getParent();
        if( engineC instanceof StandardEngine ) {
            return ((StandardEngine)engineC).getBaseDir();
        }
        return System.getProperty("catalina.base");
    }

    /**
     * Process the default configuration file, if it exists.
     * The default config must be read with the container loader - so
     * container servlets can be loaded
     */
    protected void defaultWebConfig() {
        
    }


    /**
     * Parse fragments order.
     */
    protected void createFragmentsOrder() {
        
    }
    
    
    /**
     * Process additional descriptors: TLDs, web fragments, and map overlays.
     */
    protected void applicationExtraDescriptorsConfig() {
        
    }
    
    
    /**
     * Find and parse ServletContainerInitializer service in specified JAR.
     */
    public void applicationServletContainerInitializerConfig() {
        
    }
    
    
    /**
     * Process a "init" event for this Context.
     */
    protected void init() {
        context.setConfigured(false);
        ok = true;
    }
    
    
    /**
     * Process a "before start" event for this Context.
     */
    protected void beforeStart() {
    }
    
    
    /**
     * Process a "start" event for this Context.
     */
    protected void start() {
        // Called from StandardContext.start()

        // Process the default and application web.xml files
        if (ok) {
            defaultWebConfig();
        }
        // Scan the main descriptors
        if (ok) {
            applicationWebConfig();
        }
        // Parse any Servlet context initializer defined in a Jar
        if (ok) {
            applicationServletContainerInitializerConfig();
        }
        // Parse fragment order
        if (ok) {
            createFragmentsOrder();
        }
        // Scan fragments, TLDs and annotations
        if (ok) {
            applicationExtraDescriptorsConfig();
        }
        // Parse any TLDs found for listeners
        if (ok) {
            applicationTldConfig();
        }

        // Dump the contents of this pipeline if requested
        if ((CatalinaLogger.STARTUP_LOGGER.isDebugEnabled()) && (context instanceof ContainerBase)) {
            CatalinaLogger.STARTUP_LOGGER.debug("Pipeline Configuration:");
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            Valve valves[] = null;
            if (pipeline != null)
                valves = pipeline.getValves();
            if (valves != null) {
                for (int i = 0; i < valves.length; i++) {
                    CatalinaLogger.STARTUP_LOGGER.debug("  " + valves[i].getInfo());
                }
            }
            CatalinaLogger.STARTUP_LOGGER.debug("======================");
        }

        // Make our application available if no problems were encountered
        if (ok) {
            context.setConfigured(true);
        } else {
            CatalinaLogger.STARTUP_LOGGER.contextUnavailable();
            context.setConfigured(false);
        }

    }

    /**
     * Process a "start" event for this Context.
     */
    protected void completeConfig() {
        
    }

    /**
     * Process a "stop" event for this Context.
     */
    protected void stop() {

        int i;

        // Removing children
        Container[] children = context.findChildren();
        for (i = 0; i < children.length; i++) {
            context.removeChild(children[i]);
        }

        // Removing application parameters
        /*
        ApplicationParameter[] applicationParameters =
            context.findApplicationParameters();
        for (i = 0; i < applicationParameters.length; i++) {
            context.removeApplicationParameter
                (applicationParameters[i].getName());
        }
        */

        // Removing security constraints
        SecurityConstraint[] securityConstraints = context.findConstraints();
        for (i = 0; i < securityConstraints.length; i++) {
            context.removeConstraint(securityConstraints[i]);
        }

        // Removing errors pages
        ErrorPage[] errorPages = context.findErrorPages();
        for (i = 0; i < errorPages.length; i++) {
            context.removeErrorPage(errorPages[i]);
        }

        // Removing filter defs
        FilterDef[] filterDefs = context.findFilterDefs();
        for (i = 0; i < filterDefs.length; i++) {
            context.removeFilterDef(filterDefs[i]);
        }

        // Removing filter maps
        FilterMap[] filterMaps = context.findFilterMaps();
        for (i = 0; i < filterMaps.length; i++) {
            context.removeFilterMap(filterMaps[i]);
        }

        // Removing Mime mappings
        String[] mimeMappings = context.findMimeMappings();
        for (i = 0; i < mimeMappings.length; i++) {
            context.removeMimeMapping(mimeMappings[i]);
        }

        // Removing parameters
        String[] parameters = context.findParameters();
        for (i = 0; i < parameters.length; i++) {
            context.removeParameter(parameters[i]);
        }

        // Removing sercurity role
        String[] securityRoles = context.findSecurityRoles();
        for (i = 0; i < securityRoles.length; i++) {
            context.removeSecurityRole(securityRoles[i]);
        }

        // Removing servlet mappings
        String[] servletMappings = context.findServletMappings();
        for (i = 0; i < servletMappings.length; i++) {
            context.removeServletMapping(servletMappings[i]);
        }

        // FIXME : Removing status pages

        // Removing taglibs
        String[] taglibs = context.findTaglibs();
        for (i = 0; i < taglibs.length; i++) {
            context.removeTaglib(taglibs[i]);
        }

        // FIXME: remove JSP property groups
        
        // FIXME: remove JSP tag libraries
        
        // Removing welcome files
        String[] welcomeFiles = context.findWelcomeFiles();
        for (i = 0; i < welcomeFiles.length; i++) {
            context.removeWelcomeFile(welcomeFiles[i]);
        }

        // Removing wrapper lifecycles
        String[] wrapperLifecycles = context.findWrapperLifecycles();
        for (i = 0; i < wrapperLifecycles.length; i++) {
            context.removeWrapperLifecycle(wrapperLifecycles[i]);
        }

        // Removing wrapper listeners
        String[] wrapperListeners = context.findWrapperListeners();
        for (i = 0; i < wrapperListeners.length; i++) {
            context.removeWrapperListener(wrapperListeners[i]);
        }

        ok = true;

    }
    
    
    /**
     * Process a "destroy" event for this Context.
     */
    protected void destroy() {
        // Called from StandardContext.destroy()

        // Changed to getWorkPath per Bugzilla 35819.
        String workDir = ((StandardContext) context).getWorkPath();
        if (workDir != null)
            ExpandWar.delete(new File(workDir));
    }
    
    
    /**
     * Translate servlet security associated with Servlets to security constraints.
     */
    protected void resolveServletSecurity() {
        // Skip all patterns for which a static security constraint has been defined
        HashSet<String> excludedPatterns = new HashSet<String>();
        SecurityConstraint[] staticConstraints = context.findConstraints();
        for (SecurityConstraint staticConstraint : staticConstraints) {
            for (SecurityCollection collection : staticConstraint.findCollections()) {
                for (String urlPattern : collection.findPatterns()) {
                    excludedPatterns.add(urlPattern);
                }
            }
        }
        // Iterate over servlet security objects
        Container wrappers[] = context.findChildren();
        for (int i = 0; i < wrappers.length; i++) {
            Wrapper wrapper = (Wrapper) wrappers[i];
            ServletSecurityElement servletSecurity = wrapper.getServletSecurity();
            if (servletSecurity != null) {
                
                ArrayList<String> methodOmissions = new ArrayList<String>();
                boolean classPA = servletSecurity.getEmptyRoleSemantic().equals(EmptyRoleSemantic.PERMIT);
                boolean classDA = servletSecurity.getEmptyRoleSemantic().equals(EmptyRoleSemantic.DENY);
                boolean classTP = servletSecurity.getTransportGuarantee().equals(TransportGuarantee.CONFIDENTIAL);
                String[] classRA = servletSecurity.getRolesAllowed();
                Collection<HttpMethodConstraintElement> httpMethodConstraints = 
                    servletSecurity.getHttpMethodConstraints();

                // Process method constraints
                if (httpMethodConstraints != null && httpMethodConstraints.size() > 0)
                {
                   for (HttpMethodConstraintElement httpMethodConstraint : httpMethodConstraints)
                   {
                       String method = toHttpMethod(httpMethodConstraint.getMethodName());
                      methodOmissions.add(method);
                      boolean methodPA = httpMethodConstraint.getEmptyRoleSemantic().equals(EmptyRoleSemantic.PERMIT);
                      boolean methodDA = httpMethodConstraint.getEmptyRoleSemantic().equals(EmptyRoleSemantic.DENY);
                      boolean methodTP = httpMethodConstraint.getTransportGuarantee().equals(TransportGuarantee.CONFIDENTIAL);
                      String[] methodRA = httpMethodConstraint.getRolesAllowed();
                      if (methodDA || methodTP || (methodRA != null && methodRA.length > 0))
                      {
                         // Define a constraint specific for the method
                         SecurityConstraint constraint = new SecurityConstraint();
                         if (methodDA) {
                             constraint.setAuthConstraint(true);
                         }
                         if (methodPA && (methodRA == null || methodRA.length == 0)) {
                             constraint.addAuthRole("*");
                         }
                         if (methodRA != null) {
                             for (String role : methodRA) {
                                 constraint.addAuthRole(role);
                             }
                         }
                         if (methodTP) {
                             constraint.setUserConstraint(org.apache.catalina.realm.Constants.CONFIDENTIAL_TRANSPORT);
                         }
                         SecurityCollection collection = new SecurityCollection();
                         collection.addMethod(method);
                         // Determine pattern set
                         String[] urlPatterns = wrapper.findMappings();
                         Set<String> servletSecurityPatterns = new HashSet<String>();
                         for (String urlPattern : urlPatterns) {
                             if (!excludedPatterns.contains(urlPattern)) {
                                 servletSecurityPatterns.add(urlPattern);
                             }
                         }
                         for (String urlPattern : servletSecurityPatterns) {
                             collection.addPattern(urlPattern);
                         }
                         constraint.addCollection(collection);
                         context.addConstraint(constraint);
                      }

                   }

                }

                if (classDA || classTP || (classRA != null && classRA.length > 0))
                {
                    // Define a constraint for the class
                    SecurityConstraint constraint = new SecurityConstraint();
                    if (classPA && (classRA == null || classRA.length == 0)) {
                        constraint.addAuthRole("*");
                    }
                    if (classDA) {
                        constraint.setAuthConstraint(true);
                    }
                    if (classRA != null) {
                        for (String role : classRA) {
                            constraint.addAuthRole(role);
                        }
                    }
                    if (classTP) {
                        constraint.setUserConstraint(org.apache.catalina.realm.Constants.CONFIDENTIAL_TRANSPORT);
                    }
                    SecurityCollection collection = new SecurityCollection();
                    // Determine pattern set
                    String[] urlPatterns = wrapper.findMappings();
                    Set<String> servletSecurityPatterns = new HashSet<String>();
                    for (String urlPattern : urlPatterns) {
                        if (!excludedPatterns.contains(urlPattern)) {
                            servletSecurityPatterns.add(urlPattern);
                        }
                    }
                    for (String urlPattern : servletSecurityPatterns) {
                        collection.addPattern(urlPattern);
                    }
                    for (String methodOmission : methodOmissions) {
                        collection.addMethodOmission(methodOmission);
                    }
                    constraint.addCollection(collection);
                    context.addConstraint(constraint);
                }
                
            }
        }
        // Add constraints for uncovered methods, if needed
        SecurityConstraint[] newConstraints =
                SecurityConstraint.findUncoveredHttpMethods(context.findConstraints(),
                        context.getDenyUncoveredHttpMethods());
        for (SecurityConstraint constraint : newConstraints) {
            context.addConstraint(constraint);
        }
    }
    
    
    /**
     * Although this does not comply with the spec, it is likely Java method names
     * will be used in the annotations. Since it is not possible to validate, this
     * would be an error that is invisible for the user.
     * @param method
     * @return
     */
    protected String toHttpMethod(String method) {
        if (method == null || method.length() < 3 || (!method.startsWith("do")))
            return method;
        return method.substring(2).toUpperCase();
    }
    
    
    /**
     * Validate the usage of security role names in the web application
     * deployment descriptor.  If any problems are found, issue warning
     * messages (for backwards compatibility) and add the missing roles.
     * (To make these problems fatal instead, simply set the <code>ok</code>
     * instance variable to <code>false</code> as well).
     */
    protected void validateSecurityRoles() {

        // Check if ** role was defined by the application itself
        if (context.findSecurityRole(SecurityConstraint.ROLE_ALL_AUTHENTICATED_USERS)) {
            SecurityConstraint constraints[] = context.findConstraints();
            for (SecurityConstraint constraint : constraints) {
                constraint.treatAllAuthenticatedUsersAsApplicationRole();
            }
        }

        // Check role names used in <security-constraint> elements
        SecurityConstraint constraints[] = context.findConstraints();
        for (int i = 0; i < constraints.length; i++) {
            String roles[] = constraints[i].findAuthRoles();
            for (int j = 0; j < roles.length; j++) {
                if (!SecurityConstraint.ROLE_ALL_ROLES.equals(roles[j])
                        && !SecurityConstraint.ROLE_ALL_AUTHENTICATED_USERS.equals(roles[j])
                        && !context.findSecurityRole(roles[j])) {
                    CatalinaLogger.STARTUP_LOGGER.roleValidationAuth(roles[j]);
                    context.addSecurityRole(roles[j]);
                }
            }
        }

        // Check role names used in <servlet> elements
        Container wrappers[] = context.findChildren();
        for (int i = 0; i < wrappers.length; i++) {
            Wrapper wrapper = (Wrapper) wrappers[i];
            String runAs = wrapper.getRunAs();
            if ((runAs != null) && !context.findSecurityRole(runAs)) {
                CatalinaLogger.STARTUP_LOGGER.roleValidationRunAs(runAs);
                context.addSecurityRole(runAs);
            }
            String names[] = wrapper.findSecurityReferences();
            for (int j = 0; j < names.length; j++) {
                String link = wrapper.findSecurityReference(names[j]);
                if ((link != null) && !context.findSecurityRole(link)) {
                    CatalinaLogger.STARTUP_LOGGER.roleValidationLink(link);
                    context.addSecurityRole(link);
                }
            }
        }

    }


    protected String getHostConfigPath(String resourceName) {
        StringBuilder result = new StringBuilder();
        Container container = context;
        Container host = null;
        Container engine = null;
        while (container != null) {
            if (container instanceof Host)
                host = container;
            if (container instanceof Engine)
                engine = container;
            container = container.getParent();
        }
        if (engine != null) {
            result.append(engine.getName()).append('/');
        }
        if (host != null) {
            result.append(host.getName()).append('/');
        }
        result.append(resourceName);
        return result.toString();
    }


}
