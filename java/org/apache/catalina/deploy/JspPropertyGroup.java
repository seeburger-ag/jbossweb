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

package org.apache.catalina.deploy;

import java.io.Serializable;

public class JspPropertyGroup implements Serializable {

    protected String urlPattern = null;
    protected boolean elIgnored = false;
    protected String pageEncoding = null;
    protected boolean scriptingInvalid = false;
    protected boolean isXml = false;
    protected String includePrelude = null;
    protected String includeCoda = null;
    protected boolean deferredSyntaxAllowedAsLiteral = false;
    protected boolean trimDirectiveWhitespaces = false;
    protected String defaultContentType = null;
    protected String buffer = null;
    protected boolean errorOnUndeclaredNamespace = false;

    public String getUrlPattern() {
        return urlPattern;
    }
    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }
    public boolean isElIgnored() {
        return elIgnored;
    }
    public void setElIgnored(boolean elIgnored) {
        this.elIgnored = elIgnored;
    }
    public String getPageEncoding() {
        return pageEncoding;
    }
    public void setPageEncoding(String pageEncoding) {
        this.pageEncoding = pageEncoding;
    }
    public boolean isScriptingInvalid() {
        return scriptingInvalid;
    }
    public void setScriptingInvalid(boolean scriptingInvalid) {
        this.scriptingInvalid = scriptingInvalid;
    }
    public boolean isXml() {
        return isXml;
    }
    public void setXml(boolean isXml) {
        this.isXml = isXml;
    }
    public String getIncludePrelude() {
        return includePrelude;
    }
    public void setIncludePrelude(String includePrelude) {
        this.includePrelude = includePrelude;
    }
    public String getIncludeCoda() {
        return includeCoda;
    }
    public void setIncludeCoda(String includeCoda) {
        this.includeCoda = includeCoda;
    }
    public boolean isDeferredSyntaxAllowedAsLiteral() {
        return deferredSyntaxAllowedAsLiteral;
    }
    public void setDeferredSyntaxAllowedAsLiteral(
            boolean deferredSyntaxAllowedAsLiteral) {
        this.deferredSyntaxAllowedAsLiteral = deferredSyntaxAllowedAsLiteral;
    }
    public boolean isTrimDirectiveWhitespaces() {
        return trimDirectiveWhitespaces;
    }
    public void setTrimDirectiveWhitespaces(boolean trimDirectiveWhitespaces) {
        this.trimDirectiveWhitespaces = trimDirectiveWhitespaces;
    }
    public String getDefaultContentType() {
        return defaultContentType;
    }
    public void setDefaultContentType(String defaultContentType) {
        this.defaultContentType = defaultContentType;
    }
    public String getBuffer() {
        return buffer;
    }
    public void setBuffer(String buffer) {
        this.buffer = buffer;
    }
    public boolean isErrorOnUndeclaredNamespace() {
        return errorOnUndeclaredNamespace;
    }
    public void setErrorOnUndeclaredNamespace(boolean errorOnUndeclaredNamespace) {
        this.errorOnUndeclaredNamespace = errorOnUndeclaredNamespace;
    }

}
