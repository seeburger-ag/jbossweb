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
 * 
 * 
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 1999-2009 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.jasper.compiler;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletContext;

import org.apache.catalina.Globals;
import org.apache.jasper.JasperException;
import org.apache.jasper.xmlparser.ParserUtils;
import org.apache.jasper.xmlparser.TreeNode;
import org.jboss.logging.Logger;
import org.xml.sax.InputSource;

/**
 * Handles the jsp-config element in WEB_INF/web.xml.  This is used
 * for specifying the JSP configuration information on a JSP page
 *
 * @author Kin-man Chung
 * @author Remy Maucherat
 */

public class JspConfig {

    private static final String WEB_XML = "/WEB-INF/web.xml";

    // Logger
    private Logger log = Logger.getLogger(JspConfig.class);

    private ArrayList<JspPropertyGroup> jspProperties = null;
    private ServletContext ctxt;
    private boolean initialized = false;

    private String defaultIsXml = null;		// unspecified
    private String defaultIsELIgnored = null;	// unspecified
    private String defaultIsScriptingInvalid = null;
    private String defaultDeferedSyntaxAllowedAsLiteral = null;
    private String defaultTrimDirectiveWhitespaces = null;
    private JspProperty defaultJspProperty;

    public JspConfig(ServletContext ctxt) {
        this.ctxt = ctxt;
    }

    private double getVersion(TreeNode webApp) {
        String v = webApp.findAttribute("version");
        if (v != null) {
            try {
                return Double.parseDouble(v);
            } catch (NumberFormatException e) {
            }
        }
        return 2.3;
    }

    private void processWebDotXml(ServletContext ctxt) throws JasperException {

        InputStream is = null;

        try {
            URL uri = ctxt.getResource(WEB_XML);
            if (uri == null) {
                // no web.xml
                return;
            }

            is = uri.openStream();
            InputSource ip = new InputSource(is);
            ip.setSystemId(uri.toExternalForm()); 

            ParserUtils pu = new ParserUtils();
            TreeNode webApp = pu.parseXMLDocument(WEB_XML, ip);

            if (webApp == null
                    || getVersion(webApp) < 2.4) {
                defaultIsELIgnored = "true";
                return;
            }
            TreeNode jspConfig = webApp.findChild("jsp-config");
            if (jspConfig == null) {
                return;
            }

            jspProperties = new ArrayList<JspPropertyGroup>();
            Iterator jspPropertyList = jspConfig.findChildren("jsp-property-group");
            while (jspPropertyList.hasNext()) {

                TreeNode element = (TreeNode) jspPropertyList.next();
                Iterator list = element.findChildren();

                ArrayList<String> urlPatterns = new ArrayList<String>();
                String pageEncoding = null;
                String scriptingInvalid = null;
                String elIgnored = null;
                String isXml = null;
                ArrayList<String> includePrelude = new ArrayList<String>();
                ArrayList<String> includeCoda = new ArrayList<String>();
                String deferredSyntaxAllowedAsLiteral = null;
                String trimDirectiveWhitespaces = null;

                while (list.hasNext()) {

                    element = (TreeNode) list.next();
                    String tname = element.getName();

                    if ("url-pattern".equals(tname))
                        urlPatterns.add( element.getBody() );
                    else if ("page-encoding".equals(tname))
                        pageEncoding = element.getBody();
                    else if ("is-xml".equals(tname))
                        isXml = element.getBody();
                    else if ("el-ignored".equals(tname))
                        elIgnored = element.getBody();
                    else if ("scripting-invalid".equals(tname))
                        scriptingInvalid = element.getBody();
                    else if ("include-prelude".equals(tname))
                        includePrelude.add(element.getBody());
                    else if ("include-coda".equals(tname))
                        includeCoda.add(element.getBody());
                    else if ("deferred-syntax-allowed-as-literal".equals(tname))
                        deferredSyntaxAllowedAsLiteral = element.getBody();
                    else if ("trim-directive-whitespaces".equals(tname))
                        trimDirectiveWhitespaces = element.getBody();
                }

                if (urlPatterns.size() == 0) {
                    continue;
                }

                // Add one JspPropertyGroup for each URL Pattern.  This makes
                // the matching logic easier.
                for( int p = 0; p < urlPatterns.size(); p++ ) {
                    String urlPattern = urlPatterns.get(p);
                    String path = null;
                    String extension = null;

                    if (urlPattern.indexOf('*') < 0) {
                        // Exact match
                        path = urlPattern;
                    } else {
                        int i = urlPattern.lastIndexOf('/');
                        String file;
                        if (i >= 0) {
                            path = urlPattern.substring(0,i+1);
                            file = urlPattern.substring(i+1);
                        } else {
                            file = urlPattern;
                        }

                        // pattern must be "*", or of the form "*.jsp"
                        if (file.equals("*")) {
                            extension = "*";
                        } else if (file.startsWith("*.")) {
                            extension = file.substring(file.indexOf('.')+1);
                        }

                        // The url patterns are reconstructed as the follwoing:
                        // path != null, extension == null:  / or /foo/bar.ext
                        // path == null, extension != null:  *.ext
                        // path != null, extension == "*":   /foo/*
                        boolean isStar = "*".equals(extension);
                        if ((path == null && (extension == null || isStar))
                                || (path != null && !isStar)) {
                            log.warn(Localizer.getMessage(
                                    "jsp.warning.bad.urlpattern.propertygroup",
                                    urlPattern));
                            continue;
                        }
                    }

                    JspProperty property = new JspProperty(isXml,
                            elIgnored,
                            scriptingInvalid,
                            pageEncoding,
                            includePrelude,
                            includeCoda,
                            deferredSyntaxAllowedAsLiteral,
                            trimDirectiveWhitespaces);
                    JspPropertyGroup propertyGroup =
                        new JspPropertyGroup(path, extension, property);

                    jspProperties.add(propertyGroup);
                }
            }
        } catch (Exception ex) {
            throw new JasperException(ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable t) {}
            }
        }
    }

    private void init() throws JasperException {

        if (!initialized) {
            //processWebDotXml(ctxt);
            HashMap<String, org.apache.catalina.deploy.JspPropertyGroup> jspPropertyGroups =
                (HashMap<String, org.apache.catalina.deploy.JspPropertyGroup>) 
                ctxt.getAttribute(Globals.JSP_PROPERTY_GROUPS);

            String versionString = (String) ctxt.getAttribute(Globals.SERVLET_VERSION);
            double version = 2.3;
            if (versionString != null) {
                try {
                    version =  Double.parseDouble(versionString);
                } catch (NumberFormatException e) {
                }
            }
            if (version < 2.4) {
                defaultIsELIgnored = "true";
            }
            
            jspProperties = new ArrayList<JspPropertyGroup>();
            Iterator<String> urlPatternIterator = jspPropertyGroups.keySet().iterator();
            while (urlPatternIterator.hasNext()) {
                String urlPattern = urlPatternIterator.next();
                org.apache.catalina.deploy.JspPropertyGroup jspPropertyGroup =
                    jspPropertyGroups.get(urlPattern);
                
                String path = null;
                String extension = null;

                if (urlPattern.indexOf('*') < 0) {
                    // Exact match
                    path = urlPattern;
                } else {
                    int i = urlPattern.lastIndexOf('/');
                    String file;
                    if (i >= 0) {
                        path = urlPattern.substring(0,i+1);
                        file = urlPattern.substring(i+1);
                    } else {
                        file = urlPattern;
                    }

                    // pattern must be "*", or of the form "*.jsp"
                    if (file.equals("*")) {
                        extension = "*";
                    } else if (file.startsWith("*.")) {
                        extension = file.substring(file.indexOf('.')+1);
                    }

                    // The url patterns are reconstructed as the follwoing:
                    // path != null, extension == null:  / or /foo/bar.ext
                    // path == null, extension != null:  *.ext
                    // path != null, extension == "*":   /foo/*
                    boolean isStar = "*".equals(extension);
                    if ((path == null && (extension == null || isStar))
                            || (path != null && !isStar)) {
                        log.warn(Localizer.getMessage(
                                "jsp.warning.bad.urlpattern.propertygroup",
                                urlPattern));
                        continue;
                    }
                }

                JspProperty property = new JspProperty(jspPropertyGroup.getIsXml(),
                        jspPropertyGroup.getElIgnored(),
                        jspPropertyGroup.getScriptingInvalid(),
                        jspPropertyGroup.getPageEncoding(),
                        jspPropertyGroup.getIncludePreludes(),
                        jspPropertyGroup.getIncludeCodas(),
                        jspPropertyGroup.getDeferredSyntaxAllowedAsLiteral(),
                        jspPropertyGroup.getTrimDirectiveWhitespaces());
                JspPropertyGroup propertyGroup =
                    new JspPropertyGroup(path, extension, property);

                jspProperties.add(propertyGroup);
                
            }
            
            defaultJspProperty = new JspProperty(defaultIsXml,
                    defaultIsELIgnored,
                    defaultIsScriptingInvalid,
                    null, null, null, defaultDeferedSyntaxAllowedAsLiteral, 
                    defaultTrimDirectiveWhitespaces);
            initialized = true;
        }
    }

    /**
     * Select the property group that has more restrictive url-pattern.
     * In case of tie, select the first.
     */
    private JspPropertyGroup selectProperty(JspPropertyGroup prev,
            JspPropertyGroup curr) {
        if (prev == null) {
            return curr;
        }
        if (prev.getExtension() == null) {
            // exact match
            return prev;
        }
        if (curr.getExtension() == null) {
            // exact match
            return curr;
        }
        String prevPath = prev.getPath();
        String currPath = curr.getPath();
        if (prevPath == null && currPath == null) {
            // Both specifies a *.ext, keep the first one
            return prev;
        }
        if (prevPath == null && currPath != null) {
            return curr;
        }
        if (prevPath != null && currPath == null) {
            return prev;
        }
        if (prevPath.length() >= currPath.length()) {
            return prev;
        }
        return curr;
    }


    /**
     * Find a property that best matches the supplied resource.
     * @param uri the resource supplied.
     * @return a JspProperty indicating the best match, or some default.
     */
    public JspProperty findJspProperty(String uri) throws JasperException {

        init();

        // JSP Configuration settings do not apply to tag files	    
        if (jspProperties == null || uri.endsWith(".tag")
                || uri.endsWith(".tagx")) {
            return defaultJspProperty;
        }

        String uriPath = null;
        int index = uri.lastIndexOf('/');
        if (index >=0 ) {
            uriPath = uri.substring(0, index+1);
        }
        String uriExtension = null;
        index = uri.lastIndexOf('.');
        if (index >=0) {
            uriExtension = uri.substring(index+1);
        }

        ArrayList<String> includePreludes = new ArrayList<String>();
        ArrayList<String> includeCodas = new ArrayList<String>();

        JspPropertyGroup isXmlMatch = null;
        JspPropertyGroup elIgnoredMatch = null;
        JspPropertyGroup scriptingInvalidMatch = null;
        JspPropertyGroup pageEncodingMatch = null;
        JspPropertyGroup deferedSyntaxAllowedAsLiteralMatch = null;
        JspPropertyGroup trimDirectiveWhitespacesMatch = null;

        Iterator<JspPropertyGroup> iter = jspProperties.iterator();
        while (iter.hasNext()) {

            JspPropertyGroup jpg = iter.next();
            JspProperty jp = jpg.getJspProperty();

            // (arrays will be the same length)
            String extension = jpg.getExtension();
            String path = jpg.getPath();

            if (extension == null) {
                // exact match pattern: /a/foo.jsp
                if (!uri.equals(path)) {
                    // not matched;
                    continue;
                }
            } else {
                // Matching patterns *.ext or /p/*
                if (path != null && uriPath != null &&
                        ! uriPath.startsWith(path)) {
                    // not matched
                    continue;
                }
                if (!extension.equals("*") &&
                        !extension.equals(uriExtension)) {
                    // not matched
                    continue;
                }
            }
            // We have a match
            // Add include-preludes and include-codas
            if (jp.getIncludePrelude() != null) {
                includePreludes.addAll(jp.getIncludePrelude());
            }
            if (jp.getIncludeCoda() != null) {
                includeCodas.addAll(jp.getIncludeCoda());
            }

            // If there is a previous match for the same property, remember
            // the one that is more restrictive.
            if (jp.isXml() != null) {
                isXmlMatch = selectProperty(isXmlMatch, jpg);
            }
            if (jp.isELIgnored() != null) {
                elIgnoredMatch = selectProperty(elIgnoredMatch, jpg);
            }
            if (jp.isScriptingInvalid() != null) {
                scriptingInvalidMatch =
                    selectProperty(scriptingInvalidMatch, jpg);
            }
            if (jp.getPageEncoding() != null) {
                pageEncodingMatch = selectProperty(pageEncodingMatch, jpg);
            }
            if (jp.isDeferedSyntaxAllowedAsLiteral() != null) {
                deferedSyntaxAllowedAsLiteralMatch =
                    selectProperty(deferedSyntaxAllowedAsLiteralMatch, jpg);
            }
            if (jp.isTrimDirectiveWhitespaces() != null) {
                trimDirectiveWhitespacesMatch =
                    selectProperty(trimDirectiveWhitespacesMatch, jpg);
            }
        }


        String isXml = defaultIsXml;
        String isELIgnored = defaultIsELIgnored;
        String isScriptingInvalid = defaultIsScriptingInvalid;
        String pageEncoding = null;
        String isDeferedSyntaxAllowedAsLiteral = defaultDeferedSyntaxAllowedAsLiteral;
        String isTrimDirectiveWhitespaces = defaultTrimDirectiveWhitespaces;

        if (isXmlMatch != null) {
            isXml = isXmlMatch.getJspProperty().isXml();
        }
        if (elIgnoredMatch != null) {
            isELIgnored = elIgnoredMatch.getJspProperty().isELIgnored();
        }
        if (scriptingInvalidMatch != null) {
            isScriptingInvalid =
                scriptingInvalidMatch.getJspProperty().isScriptingInvalid();
        }
        if (pageEncodingMatch != null) {
            pageEncoding = pageEncodingMatch.getJspProperty().getPageEncoding();
        }
        if (deferedSyntaxAllowedAsLiteralMatch != null) {
            isDeferedSyntaxAllowedAsLiteral =
                deferedSyntaxAllowedAsLiteralMatch.getJspProperty().isDeferedSyntaxAllowedAsLiteral();
        }
        if (trimDirectiveWhitespacesMatch != null) {
            isTrimDirectiveWhitespaces =
                trimDirectiveWhitespacesMatch.getJspProperty().isTrimDirectiveWhitespaces();
        }

        return new JspProperty(isXml, isELIgnored, isScriptingInvalid,
                pageEncoding, includePreludes, includeCodas, 
                isDeferedSyntaxAllowedAsLiteral, isTrimDirectiveWhitespaces);
    }

    /**
     * To find out if an uri matches an url pattern in jsp config.  If so,
     * then the uri is a JSP page.  This is used primarily for jspc.
     */
    public boolean isJspPage(String uri) throws JasperException {

        init();
        if (jspProperties == null) {
            return false;
        }

        String uriPath = null;
        int index = uri.lastIndexOf('/');
        if (index >=0 ) {
            uriPath = uri.substring(0, index+1);
        }
        String uriExtension = null;
        index = uri.lastIndexOf('.');
        if (index >=0) {
            uriExtension = uri.substring(index+1);
        }

        Iterator iter = jspProperties.iterator();
        while (iter.hasNext()) {

            JspPropertyGroup jpg = (JspPropertyGroup) iter.next();
            JspProperty jp = jpg.getJspProperty();

            String extension = jpg.getExtension();
            String path = jpg.getPath();

            if (extension == null) {
                if (uri.equals(path)) {
                    // There is an exact match
                    return true;
                }
            } else {
                if ((path == null || path.equals(uriPath)) &&
                        (extension.equals("*") || extension.equals(uriExtension))) {
                    // Matches *, *.ext, /p/*, or /p/*.ext
                    return true;
                }
            }
        }
        return false;
    }

    static class JspPropertyGroup {
        private String path;
        private String extension;
        private JspProperty jspProperty;

        JspPropertyGroup(String path, String extension,
                JspProperty jspProperty) {
            this.path = path;
            this.extension = extension;
            this.jspProperty = jspProperty;
        }

        public String getPath() {
            return path;
        }

        public String getExtension() {
            return extension;
        }

        public JspProperty getJspProperty() {
            return jspProperty;
        }
    }

    static public class JspProperty {

        private String isXml;
        private String elIgnored;
        private String scriptingInvalid;
        private String pageEncoding;
        private ArrayList<String> includePrelude;
        private ArrayList<String> includeCoda;
        private String deferedSyntaxAllowedAsLiteral;
        private String trimDirectiveWhitespaces;

        public JspProperty(String isXml, String elIgnored,
                String scriptingInvalid, String pageEncoding,
                ArrayList<String> includePrelude, ArrayList<String> includeCoda,
                String deferedSyntaxAllowedAsLiteral, 
                String trimDirectiveWhitespaces) {

            this.isXml = isXml;
            this.elIgnored = elIgnored;
            this.scriptingInvalid = scriptingInvalid;
            this.pageEncoding = pageEncoding;
            this.includePrelude = includePrelude;
            this.includeCoda = includeCoda;
            this.deferedSyntaxAllowedAsLiteral = deferedSyntaxAllowedAsLiteral;
            this.trimDirectiveWhitespaces = trimDirectiveWhitespaces;
        }

        public String isXml() {
            return isXml;
        }

        public String isELIgnored() {
            return elIgnored;
        }

        public String isScriptingInvalid() {
            return scriptingInvalid;
        }

        public String getPageEncoding() {
            return pageEncoding;
        }

        public ArrayList<String> getIncludePrelude() {
            return includePrelude;
        }

        public ArrayList<String> getIncludeCoda() {
            return includeCoda;
        }
        
        public String isDeferedSyntaxAllowedAsLiteral() {
            return deferedSyntaxAllowedAsLiteral;
        }
        
        public String isTrimDirectiveWhitespaces() {
            return trimDirectiveWhitespaces;
        }
    }
}
