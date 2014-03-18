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

package org.jboss.servlet.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.ServletException;

/**
 * An event filter, similar to regular filters, performs filtering tasks on either 
 * the request to a resource (an event driven Servlet), or on the response from a resource, or both.
 * 
 * @author Remy Maucherat
 */
public interface HttpEventFilter extends Filter {

    
    /**
     * The <code>doFilterEvent</code> method of the HttpEventFilter is called by the container
     * each time a request/response pair is passed through the chain due
     * to a client event for a resource at the end of the chain. The HttpEventFilterChain passed in to this
     * method allows the Filter to pass on the event to the next entity in the
     * chain.<p>
     * A typical implementation of this method would follow the following pattern:- <br>
     * 1. Examine the request<br>
     * 2. Optionally wrap the request object contained in the event with a custom implementation to
     * filter content or headers for input filtering and pass a HttpEvent instance containing
     * the wrapped request to the next filter<br>
     * 3. Optionally wrap the response object contained in the event with a custom implementation to
     * filter content or headers for output filtering and pass a HttpEvent instance containing
     * the wrapped request to the next filter<br>
     * 4. a) <strong>Either</strong> invoke the next entity in the chain using the HttpEventFilterChain 
     *       object (<code>chain.doFilterEvent()</code>), <br>   
     * 4. b) <strong>or</strong> not pass on the request/response pair to the next entity in the 
     *       filter chain to block the event processing<br>
     * 5. Directly set fields on the response after invocation of the next entity in the filter chain.
     * 
     * @param event the event that is being processed. Another event may be passed along the chain.
     * @param chain 
     * @throws IOException
     * @throws ServletException
     */
    public void doFilterEvent(HttpEvent event, HttpEventFilterChain chain)
        throws IOException, ServletException;
    

}
