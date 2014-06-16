/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomcat.websocket.pojo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.apache.catalina.ThreadBindingListener;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.websocket.InstanceHandle;
import org.apache.tomcat.websocket.WsSession;
import org.jboss.web.WebsocketsLogger;

/**
 * Base implementation (client and server have different concrete
 * implementations) of the wrapper that converts a POJO instance into a
 * WebSocket endpoint instance.
 */
public abstract class PojoEndpointBase extends Endpoint {

    private Object pojo;
    private InstanceHandle instanceHandle;
    private Map<String,String> pathParameters;
    private PojoMethodMapping methodMapping;


    protected final void doOnOpen(Session session, EndpointConfig config) {
        PojoMethodMapping methodMapping = getMethodMapping();
        Object pojo = getPojo();
        Map<String,String> pathParameters = getPathParameters();

        // Add message handlers before calling onOpen since that may trigger a
        // message which in turn could trigger a response and/or close the
        // session
        for (MessageHandler mh : methodMapping.getMessageHandlers(pojo,
                pathParameters, session, config)) {
            session.addMessageHandler(mh);
        }

        if (methodMapping.getOnOpen() != null) {
            ThreadBindingListener tbl = ((WsSession) session).getThreadBindingListener();
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(((WsSession)session).getClassLoader());
                tbl.bind();
                methodMapping.getOnOpen().invoke(pojo,
                        methodMapping.getOnOpenArgs(
                                pathParameters, session, config));

            } catch (IllegalAccessException e) {
                // Reflection related problems
                WebsocketsLogger.ROOT_LOGGER.onOpenFailed(pojo.getClass().getName(), e);
                handleOnOpenError(session, e);
                return;
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                handleOnOpenError(session, cause);
                return;
            } catch (Throwable t) {
                handleOnOpenError(session, t);
                return;
            } finally {
                try {
                    tbl.unbind();
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
        }
    }


    private void handleOnOpenError(Session session, Throwable t) {
        // If really fatal - re-throw
        ExceptionUtils.handleThrowable(t);

        // Trigger the error handler and close the session
        onError(session, t);
        try {
            session.close();
        } catch (IOException ioe) {
            WebsocketsLogger.ROOT_LOGGER.closeSessionFailed(ioe);
        }
    }

    @Override
    public final void onClose(Session session, CloseReason closeReason) {
        try {
            if (methodMapping.getOnClose() != null) {
                ThreadBindingListener tbl = ((WsSession) session).getThreadBindingListener();
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(((WsSession)session).getClassLoader());
                    tbl.bind();
                    methodMapping.getOnClose().invoke(pojo,
                            methodMapping.getOnCloseArgs(pathParameters, session, closeReason));
                } catch (Throwable t) {
                    WebsocketsLogger.ROOT_LOGGER.onCloseFailed(pojo.getClass().getName(), t);
                    handleOnCloseError(session, t);
                } finally {
                    try {
                        tbl.unbind();
                    } finally {
                        Thread.currentThread().setContextClassLoader(old);
                    }
                }
            }

            // Trigger the destroy method for any associated decoders
            Set<MessageHandler> messageHandlers = session.getMessageHandlers();
            for (MessageHandler messageHandler : messageHandlers) {
                if (messageHandler instanceof PojoMessageHandlerWholeBase<?>) {
                    ((PojoMessageHandlerWholeBase<?>) messageHandler).onClose();
                }
            }
        } finally {
            if (instanceHandle != null) {
                instanceHandle.release();
                instanceHandle = null;
            }
        }
    }


    private void handleOnCloseError(Session session, Throwable t) {
        try {
            // If really fatal - re-throw
            ExceptionUtils.handleThrowable(t);

            // Trigger the error handler and close the session
            onError(session, t);
            try {
                session.close();
            } catch (IOException ioe) {
                WebsocketsLogger.ROOT_LOGGER.closeSessionFailed(ioe);
            }
        } finally {
            if(instanceHandle != null) {
                instanceHandle.release();
                instanceHandle = null;
            }
        }
    }

    @Override
    public final void onError(Session session, Throwable throwable) {

        if (methodMapping.getOnError() == null) {
            WebsocketsLogger.ROOT_LOGGER.noOnError(pojo.getClass().getName(), throwable);
        } else {
            ThreadBindingListener tbl = ((WsSession) session).getThreadBindingListener();
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(((WsSession)session).getClassLoader());
                tbl.bind();
                methodMapping.getOnError().invoke(
                        pojo,
                        methodMapping.getOnErrorArgs(pathParameters, session,
                                throwable));
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                WebsocketsLogger.ROOT_LOGGER.onErrorFailed(pojo.getClass().getName(), t);
            } finally {
                try {
                    tbl.unbind();
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
        }
    }

    protected Object getPojo() { return pojo; }
    protected void setPojo(Object pojo) { this.pojo = pojo; }

    public InstanceHandle getInstanceHandle() { return instanceHandle; }

    public void setInstanceHandle(InstanceHandle instanceHandle) { this.instanceHandle = instanceHandle; }

    protected Map<String,String> getPathParameters() { return pathParameters; }
    protected void setPathParameters(Map<String,String> pathParameters) {
        this.pathParameters = pathParameters;
    }


    protected PojoMethodMapping getMethodMapping() { return methodMapping; }
    protected void setMethodMapping(PojoMethodMapping methodMapping) {
        this.methodMapping = methodMapping;
    }
}
