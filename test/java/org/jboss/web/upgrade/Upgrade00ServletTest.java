/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, JBoss Inc., and individual contributors as indicated
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


package org.jboss.web.upgrade;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.jboss.servlet.http.HttpEvent;
import org.jboss.servlet.http.HttpEventServlet;
import org.jboss.servlet.http.UpgradableHttpServletResponse;

@WebServlet("/upgrade00")
public class Upgrade00ServletTest extends HttpServlet implements HttpEventServlet {

    int count = 0;
    
    public void event(HttpEvent event) throws IOException, ServletException {
        System.out.println("[" + event.getHttpServletRequest().getSession(true).getId() + "] " + event.getType());
        ServletInputStream is = null;
        ServletOutputStream os = null;
        switch (event.getType()) {
        case BEGIN:
            event.setTimeout(20000);
            HttpServletResponse response = event.getHttpServletResponse();
            if (response instanceof UpgradableHttpServletResponse) {
                ((UpgradableHttpServletResponse) response).startUpgrade();
            } else {
                throw new IllegalStateException("Cannot upgrade connection");
            }
            is = event.getHttpServletRequest().getInputStream();
            byte[] buf = new byte[1024];
            int n = is.read(buf);
            // Do something with the content
            os = event.getHttpServletResponse().getOutputStream();
            os.println("Read: " + n);
            if (n > 0) {
                os.write(buf, 0, n);
            }
            response.setHeader("Upgrade", "WebSocket");
            response.setHeader("Sec-WebSocket-Protocol", "sample");
            response.setHeader("Connection", "Upgrade");
            ((UpgradableHttpServletResponse) response).sendUpgrade();
            break;
        case END:
            break;
        case ERROR:
            event.close();
            break;
        case EVENT:
            os = event.getHttpServletResponse().getOutputStream();
            // Using while (true): Not checking if the connection is available to writing immediately
            // will cause the write to be performed in blocking mode.
            // boolean b = true;
            // while (b) {
            while (event.isWriteReady()) {
                if (count % 100 == 0) {
                    os.println((count++) + " ");
                } else {
                    os.print((count++) + " ");
                }
            }
            //if (event.ready())
            //    os.flush();
            break;
        case READ:
            is = event.getHttpServletRequest().getInputStream();
            // Using while (true): Not checking if input is available will trigger a blocking
            // read. No other event should be triggered (the current READ event will be in progress
            // until the read timeouts, which will trigger an ERROR event due to an IOException).
            // while (true) {
            while (is.available() > 0) {
                int c = is.read();
                if (c > 0) {
                    System.out.print((char) c);
                } else {
                    System.out.print(c);
                    break;
                }
            }
            System.out.println();
            break;
        case TIMEOUT:
            // This will cause a generic event to be sent to the servlet every time the connection is idle for
            // a while.
            event.resume();
            break;
        case WRITE:
            break;
        }
    }

}
