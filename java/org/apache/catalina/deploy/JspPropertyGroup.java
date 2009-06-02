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
import java.util.ArrayList;

public class JspPropertyGroup implements Serializable {

    protected ArrayList<String> urlPatterns = new ArrayList<String>();
    protected boolean elIgnored = false;
    protected String pageEncoding = null;
    protected boolean scriptingInvalid = false;
    protected boolean isXml = false;
    protected ArrayList<String> includePreludes = new ArrayList<String>();
    protected ArrayList<String> includeCodas = new ArrayList<String>();
    protected boolean deferredSyntaxAllowedAsLiteral = false;
    protected boolean trimDirectiveWhitespaces = false;
    protected String defaultContentType = null;
    protected String buffer = null;
    protected boolean errorOnUndeclaredNamespace = false;

    public String[] getUrlPatterns() {
        return urlPatterns.toArray(new String[0]);
    }
    public void addUrlPattern(String urlPattern) {
        urlPatterns.add(urlPattern);
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
    public String[] getIncludePreludes() {
        return includePreludes.toArray(new String[0]);
    }
    public void addIncludePrelude(String includePrelude) {
        includePreludes.add(includePrelude);
    }
    public String[] getIncludeCodas() {
        return includeCodas.toArray(new String[0]);
    }
    public void addIncludeCoda(String includeCoda) {
        includeCodas.add(includeCoda);
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
