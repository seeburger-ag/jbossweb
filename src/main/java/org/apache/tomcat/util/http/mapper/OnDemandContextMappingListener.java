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

package org.apache.tomcat.util.http.mapper;

/**
 * A listener for notifications when a request is being mapped to a context 
 * path that was 
 * {@link Mapper#addOnDemandContext(String, String) registered as an on-demand context}.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public interface OnDemandContextMappingListener
{
   void onDemandContextMapped(String host, String context);
}
