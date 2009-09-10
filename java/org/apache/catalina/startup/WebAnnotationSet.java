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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.annotation.security.TransportProtected;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceEnvRef;
import org.apache.catalina.deploy.ContextService;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.MessageDestinationRef;
import org.apache.catalina.deploy.Multipart;
import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;

/**
 * <p><strong>AnnotationSet</strong> for processing the annotations of the web application
 * classes (<code>/WEB-INF/classes</code> and <code>/WEB-INF/lib</code>).</p>
 *
 * @author Fabien Carrion
 * @version $Revision$ $Date$
 */

public class WebAnnotationSet {
    
    
    // --------------------------------------------------------- Public Methods
    
    
    /**
     * Process the annotations on a context.
     */
    public static void loadApplicationAnnotations(Context context) {
        
        loadApplicationListenerAnnotations(context);
        loadApplicationFilterAnnotations(context);
        loadApplicationServletAnnotations(context);
        
    }
    
    
    // -------------------------------------------------------- protected Methods
    
    
    /**
     * Process the annotations for the listeners.
     */
    protected static void loadApplicationListenerAnnotations(Context context) {
        String[] applicationListeners = context.findApplicationListeners();
        for (int i = 0; i < applicationListeners.length; i++) {
            loadClassAnnotation(context, applicationListeners[i]);
        }
    }
    
    
    /**
     * Process the annotations for the filters.
     */
    protected static void loadApplicationFilterAnnotations(Context context) {
        FilterDef[] filterDefs = context.findFilterDefs();
        for (int i = 0; i < filterDefs.length; i++) {
            loadClassAnnotation(context, filterDefs[i].getFilterClass());
        }
    }
    
    
    /**
     * Process the annotations for the servlets.
     */
    protected static void loadApplicationServletAnnotations(Context context) {
        
        ClassLoader classLoader = context.getLoader().getClassLoader();
        StandardWrapper wrapper = null;
        Class<?> classClass = null;
        
        Container[] children = context.findChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof StandardWrapper) {
                
                wrapper = (StandardWrapper) children[i];
                if (wrapper.getServletClass() == null) {
                    continue;
                }
                
                try {
                    classClass = classLoader.loadClass(wrapper.getServletClass());
                } catch (ClassNotFoundException e) {
                    // We do nothing
                } catch (NoClassDefFoundError e) {
                    // We do nothing
                }
                
                if (classClass == null) {
                    continue;
                }
                
                loadClassAnnotation(context, wrapper.getServletClass());

                // Multipart configuration annotation
                if (classClass.isAnnotationPresent(MultipartConfig.class)) {
                    MultipartConfig annotation = 
                        (MultipartConfig) classClass.getAnnotation(MultipartConfig.class);
                    Multipart multipartConfig = new Multipart();
                    multipartConfig.setLocation(annotation.location());
                    multipartConfig.setMaxRequestSize(annotation.maxRequestSize());
                    multipartConfig.setMaxFileSize(annotation.maxFileSize());
                    multipartConfig.setFileSizeThreshold(annotation.fileSizeThreshold());
                    wrapper.setMultipartConfig(multipartConfig);
                }

                // Process JSR 250 access control annotations
                // Process PermitAll, TransportProtected and RolesAllowed on the class
                boolean classPA = false, classTP = false;
                String[] classRA = null;
                ArrayList<String> methodOmissions = new ArrayList<String>();
                if (classClass.isAnnotationPresent(PermitAll.class)) {
                    classPA = true;
                }
                if (classClass.isAnnotationPresent(TransportProtected.class)) {
                    TransportProtected annotation = (TransportProtected) 
                        classClass.getAnnotation(TransportProtected.class);
                    classTP = annotation.value();
                }
                if (classClass.isAnnotationPresent(RolesAllowed.class)) {
                    RolesAllowed annotation = (RolesAllowed) 
                        classClass.getAnnotation(RolesAllowed.class);
                    classRA = annotation.value();
                }
                if (classClass.isAnnotationPresent(RunAs.class)) {
                    RunAs annotation = (RunAs) 
                        classClass.getAnnotation(RunAs.class);
                    wrapper.setRunAs(annotation.value());
                }

                // Process HttpServlet methods annotations
                if (HttpServlet.class.isAssignableFrom(classClass)) {
                    
                    Method[] methods = null;
                    final Class<?> classClass2 = classClass;
                    if (Globals.IS_SECURITY_ENABLED) {
                        methods = AccessController.doPrivileged(
                                new PrivilegedAction<Method[]>(){
                            public Method[] run(){
                                return classClass2.getDeclaredMethods();
                            }
                        });
                    } else {
                        methods = classClass.getDeclaredMethods();
                    }
                    for (Method method : methods) {
                        if (!((method.getParameterTypes().length == 2)
                                && (method.getParameterTypes()[0] == HttpServletRequest.class)
                                && (method.getParameterTypes()[1] == HttpServletResponse.class))) {
                            continue;
                        }
                        String methodName = null;
                        if (method.getName().startsWith("do")) {
                            methodName = method.getName().substring(2).toUpperCase();
                        }
                        if (methodName == null) {
                            continue;
                        }
                        boolean methodAnnotation = false;
                        boolean methodPA = false, methodDA = false, methodTP = false;
                        String[] methodRA = null;
                        if (method.isAnnotationPresent(PermitAll.class)) {
                            methodPA = true;
                            methodAnnotation = true;
                        }
                        if (method.isAnnotationPresent(DenyAll.class)) {
                            methodDA = true;
                            methodAnnotation = true;
                        }
                        if (method.isAnnotationPresent(TransportProtected.class)) {
                            TransportProtected annotation = (TransportProtected) 
                                method.getAnnotation(TransportProtected.class);
                            methodTP = annotation.value();
                            methodAnnotation = true;
                        }
                        if (method.isAnnotationPresent(RolesAllowed.class)) {
                            RolesAllowed annotation = (RolesAllowed) 
                                method.getAnnotation(RolesAllowed.class);
                            methodRA = annotation.value();
                            methodAnnotation = true;
                        }
                        if (methodAnnotation) {
                            methodOmissions.add(methodName);
                            // Define a constraint specific for the method
                            SecurityConstraint constraint = new SecurityConstraint();
                            if (methodDA) {
                                constraint.setAuthConstraint(true);
                            }
                            if (methodPA) {
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
                            collection.addMethod(methodName);
                            String[] urlPatterns = wrapper.findMappings();
                            for (String urlPattern : urlPatterns) {
                                collection.addPattern(urlPattern);
                            }
                            constraint.addCollection(collection);
                            context.addConstraint(constraint);
                        }
                        
                    }
                    
                }
                
                if (classPA || classTP || classRA != null) {
                    // Define a constraint for the class
                    SecurityConstraint constraint = new SecurityConstraint();
                    if (classPA) {
                        constraint.addAuthRole("*");
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
                    String[] urlPatterns = wrapper.findMappings();
                    for (String urlPattern : urlPatterns) {
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
        
        
    }
    
    
    /**
     * Process the annotations on a context for a given className.
     */
    protected static void loadClassAnnotation(Context context, String fileString) {
        
        ClassLoader classLoader = context.getLoader().getClassLoader();
        Class<?> classClass = null;
        
        try {
            classClass = classLoader.loadClass(fileString);
        } catch (ClassNotFoundException e) {
            // We do nothing
        } catch (NoClassDefFoundError e) {
            // We do nothing
        }
        
        if (classClass == null) {
            return;
        }
        
        // Initialize the annotations
        
        if (classClass.isAnnotationPresent(Resource.class)) {
            Resource annotation = (Resource) 
                classClass.getAnnotation(Resource.class);
            addResource(context, annotation);
        }
        /* Process Resources annotation.
         * Ref JSR 250
         */
        if (classClass.isAnnotationPresent(Resources.class)) {
            Resources annotation = (Resources) 
                classClass.getAnnotation(Resources.class);
            for (int i = 0; annotation.value() != null && i < annotation.value().length; i++) {
                addResource(context, annotation.value()[i]);
            }
        }
        /* Process EJB annotation.
         * Ref JSR 224, equivalent to the ejb-ref or ejb-local-ref
         * element in the deployment descriptor.
        if (classClass.isAnnotationPresent(EJB.class)) {
            EJB annotation = (EJB) 
            classClass.getAnnotation(EJB.class);
            
            if ((annotation.mappedName().length() == 0) ||
                    annotation.mappedName().equals("Local")) {
                
                ContextLocalEjb ejb = new ContextLocalEjb();
                
                ejb.setName(annotation.name());
                ejb.setType(annotation.beanInterface().getCanonicalName());
                ejb.setDescription(annotation.description());
                
                ejb.setHome(annotation.beanName());
                
                context.getNamingResources().addLocalEjb(ejb);
                
            } else if (annotation.mappedName().equals("Remote")) {
                
                ContextEjb ejb = new ContextEjb();
                
                ejb.setName(annotation.name());
                ejb.setType(annotation.beanInterface().getCanonicalName());
                ejb.setDescription(annotation.description());
                
                ejb.setHome(annotation.beanName());
                
                context.getNamingResources().addEjb(ejb);
                
            }
            
        }
         */
        /* Process WebServiceRef annotation.
         * Ref JSR 224, equivalent to the service-ref element in 
         * the deployment descriptor.
         * The service-ref registration is not implemented
        if (classClass.isAnnotationPresent(WebServiceRef.class)) {
            WebServiceRef annotation = (WebServiceRef) 
            classClass.getAnnotation(WebServiceRef.class);
            
            ContextService service = new ContextService();
            
            service.setName(annotation.name());
            service.setWsdlfile(annotation.wsdlLocation());
            
            service.setType(annotation.type().getCanonicalName());
            
            if (annotation.value() == null)
                service.setServiceinterface(annotation.type().getCanonicalName());
            
            if (annotation.type().getCanonicalName().equals("Service"))
                service.setServiceinterface(annotation.type().getCanonicalName());
            
            if (annotation.value().getCanonicalName().equals("Endpoint"))
                service.setServiceendpoint(annotation.type().getCanonicalName());
            
            service.setPortlink(annotation.type().getCanonicalName());
            
            context.getNamingResources().addService(service);
            
            
        }
         */
        /* Process DeclareRoles annotation.
         * Ref JSR 250, equivalent to the security-role element in
         * the deployment descriptor
         */
        if (classClass.isAnnotationPresent(DeclareRoles.class)) {
            DeclareRoles annotation = (DeclareRoles) 
                classClass.getAnnotation(DeclareRoles.class);
            for (int i = 0; annotation.value() != null && i < annotation.value().length; i++) {
                context.addSecurityRole(annotation.value()[i]);
            }
        }
        
        
    }
    
    
    /**
     * Process a Resource annotation to set up a Resource.
     * Ref JSR 250, equivalent to the resource-ref,
     * message-destination-ref, env-ref, resource-env-ref
     * or service-ref element in the deployment descriptor.
     */
    protected static void addResource(Context context, Resource annotation) {
        
        if (annotation.type().getCanonicalName().equals("java.lang.String") ||
                annotation.type().getCanonicalName().equals("java.lang.Character") ||
                annotation.type().getCanonicalName().equals("java.lang.Integer") ||
                annotation.type().getCanonicalName().equals("java.lang.Boolean") ||
                annotation.type().getCanonicalName().equals("java.lang.Double") ||
                annotation.type().getCanonicalName().equals("java.lang.Byte") ||
                annotation.type().getCanonicalName().equals("java.lang.Short") ||
                annotation.type().getCanonicalName().equals("java.lang.Long") ||
                annotation.type().getCanonicalName().equals("java.lang.Float")) {
            
            // env-ref element
            ContextEnvironment resource = new ContextEnvironment();
            
            resource.setName(annotation.name());
            resource.setType(annotation.type().getCanonicalName());
            
            resource.setDescription(annotation.description());
            
            resource.setValue(annotation.mappedName());
            
            context.getNamingResources().addEnvironment(resource);
            
        } else if (annotation.type().getCanonicalName().equals("javax.xml.rpc.Service")) {
            
            // service-ref element
            ContextService service = new ContextService();
            
            service.setName(annotation.name());
            service.setWsdlfile(annotation.mappedName());
            
            service.setType(annotation.type().getCanonicalName());
            service.setDescription(annotation.description());
            
            context.getNamingResources().addService(service);
            
        } else if (annotation.type().getCanonicalName().equals("javax.sql.DataSource") ||
                annotation.type().getCanonicalName().equals("javax.jms.ConnectionFactory") ||
                annotation.type().getCanonicalName()
                .equals("javax.jms.QueueConnectionFactory") ||
                annotation.type().getCanonicalName()
                .equals("javax.jms.TopicConnectionFactory") ||
                annotation.type().getCanonicalName().equals("javax.mail.Session") ||
                annotation.type().getCanonicalName().equals("java.net.URL") ||
                annotation.type().getCanonicalName()
                .equals("javax.resource.cci.ConnectionFactory") ||
                annotation.type().getCanonicalName().equals("org.omg.CORBA_2_3.ORB") ||
                annotation.type().getCanonicalName().endsWith("ConnectionFactory")) {
            
            // resource-ref element
            ContextResource resource = new ContextResource();
            
            resource.setName(annotation.name());
            resource.setType(annotation.type().getCanonicalName());
            
            if (annotation.authenticationType()
                    == Resource.AuthenticationType.CONTAINER) {
                resource.setAuth("Container");
            }
            else if (annotation.authenticationType()
                    == Resource.AuthenticationType.APPLICATION) {
                resource.setAuth("Application");
            }
            
            resource.setScope(annotation.shareable() ? "Shareable" : "Unshareable");
            resource.setProperty("mappedName", annotation.mappedName());
            resource.setDescription(annotation.description());
            
            context.getNamingResources().addResource(resource);
            
        } else if (annotation.type().getCanonicalName().equals("javax.jms.Queue") ||
                annotation.type().getCanonicalName().equals("javax.jms.Topic")) {
            
            // message-destination-ref
            MessageDestinationRef resource = new MessageDestinationRef();
            
            resource.setName(annotation.name());
            resource.setType(annotation.type().getCanonicalName());
            
            resource.setUsage(annotation.mappedName());
            resource.setDescription(annotation.description());
            
            context.getNamingResources().addMessageDestinationRef(resource);
            
        } else if (annotation.type().getCanonicalName()
                .equals("javax.resource.cci.InteractionSpec") ||
                annotation.type().getCanonicalName()
                .equals("javax.transaction.UserTransaction") ||
                true) {
            
            // resource-env-ref
            ContextResourceEnvRef resource = new ContextResourceEnvRef();
            
            resource.setName(annotation.name());
            resource.setType(annotation.type().getCanonicalName());
            
            resource.setProperty("mappedName", annotation.mappedName());
            resource.setDescription(annotation.description());
            
            context.getNamingResources().addResourceEnvRef(resource);
            
        }
        
        
    }
    
    
}
