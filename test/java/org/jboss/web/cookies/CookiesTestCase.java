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
 * @author Jean-Frederic Clere
 */


package org.jboss.web.cookies;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class CookiesTestCase extends TestCase {

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public CookiesTestCase(String name) {
        super(name);
    }

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(CookiesTestCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
    }
   
    /* should create foo (bar) and a (b) */ 
    public void testTest1() { mytest(1, "foo=bar; a=b"); }
    public void testTest2() { mytest(2, "foo=bar;a=b"); }
    public void testTest3() { mytest(3, "foo=bar;a=b;"); }
    public void testTest4() { mytest(4, "foo=bar;a=b; "); }
    public void testTest5() { mytest(5, "foo=bar;a=b; ;"); }

    /* should create foo () and a (b) */
    public void testTest6() { mytest(6, "foo=;a=b; ;"); }
    public void testTest7() { mytest(7, "foo;a=b; ;"); }

    /* v1 create foo (bar) and a (b) */
    public void testTest8() { mytest(8, "$Version=1; foo=bar;a=b"); }
    public void testTest9() { mytest(9, "$Version=1;foo=bar;a=b; ; "); }
    /* v1 create foo () and a (b) */
    public void testTest10() { mytest(10, "$Version=1;foo=;a=b; ; "); }
    public void testTest11() { mytest(11, "$Version=1;foo= ;a=b; ; "); }
    public void testTest12() { mytest(12, "$Version=1;foo;a=b; ; "); }

    /* v1 create foo (bar) and a (b) */
    public void testTest13() { mytest(13, "$Version=1;foo=\"bar\";a=b; ; "); }
    /* use domain */
    public void testTest14() { mytest(14, "$Version=1;foo=\"bar\";$Domain=apache.org;a=b"); }
    public void testTest15() { mytest(15, "$Version=1;foo=\"bar\";$Domain=apache.org;a=b;$Domain=yahoo.com"); }
    /* rfc2965 */
    public void testTest16() { mytest(16, "$Version=1;foo=\"bar\";$Domain=apache.org;$Port=8080;a=b"); }
    // make sure these never split into two cookies - JVK
    public void testTest17() { mytest(17, "$Version=1;foo=\"b\"ar\";$Domain=apache.org;$Port=8080;a=b"); }
    public void testTest18() { mytest(18, "$Version=1;foo=\"b\\\"ar\";$Domain=apache.org;$Port=8080;a=b"); }
    public void testTest19() { mytest(19, "$Version=1;foo=\"b'ar\";$Domain=apache.org;$Port=8080;a=b"); }
    // JFC: sure it is "b" and not b'ar ?
    public void testTest20() { mytest(20, "$Version=1;foo=b'ar;$Domain=apache.org;$Port=8080;a=b"); }
    // Ends in quoted value
    public void testTest21() { mytest(21, "foo=bar;a=\"b\""); }
    public void testTest22() { mytest(22, "foo=bar;a=\"b\";"); }

    // Testing bad stuff
    public void testTest23() { mytest(23, "$Version=\"1\"; foo='bar'; $Path=/path; $Domain=\"localhost\""); }

    // wrong, path should not have '/' JVK ???
    public void testTest24() { mytest(24, "$Version=1;foo=\"bar\";$Path=/examples;a=b; ; "); }
    // Test name-only at the end of the header
    public void testTest25() { mytest(25, "foo;a=b;bar"); }
    public void testTest26() { mytest(26, "foo;a=b;bar;"); }
    public void testTest27() { mytest(27, "foo;a=b;bar "); }
    public void testTest28() { mytest(28, "foo;a=b;bar ;"); }
    // BUG -- the ' ' needs to be skipped.
    public void testTest29() { mytest(29, "foo;a=b; ;bar"); }
    // BUG -- ';' needs skipping
    public void testTest30() { mytest(30, "foo;a=b;;bar"); }
    public void testTest31() { mytest(31, "foo;a=b; ;;bar=rab"); }
    public void testTest32() { mytest(32, "foo;a=b;; ;bar=rab"); }

    public void testTest33() { mytest(33, "a=b;#;bar=rab"); }
    public void testTest34() { mytest(34, "a=b;;\\;bar=rab"); }

    // Try all the separators of version1 in version0 cookie.
    public void testTest35() { mytest(35, "a=()<>@:\\\"/[]?={}\t; foo=bar; a=b"); }

    // Just test the version.
    public void testTest36() { mytest(36, "$Version=1;foo=bar"); }
    public void testTest37() { mytest(37, "$Version=0;foo=bar"); }

    public void mytest(int test, String cookie) {
        try {
        String result = Mytest(test, cookie);
        if (result != null)
           fail(result);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Test failed because of " + ex);
        }
    }
    public String Mytest(int test, String cookie) throws Exception {
        Socket socket = new Socket("localhost", 8080);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // writer.write("GET /cookies/test.jsp HTTP/1.0");
        writer.write("GET /myapp/test.jsp HTTP/1.0\r\n");
        writer.write("User-Agent: CookiesTestCase/1.0\r\n");
        writer.write("Connection: Keep-Alive\r\n");
        writer.write("TEST: " + test + "\r\n");
        writer.write("ACTION: CREATE\r\n");
        writer.write("Cookie: " + cookie + "\r\n");
        writer.write("\r\n");
        writer.flush();

        String responseStatus = reader.readLine();
        if (responseStatus == null) {
            return "Can't read answer" ;
        }
        responseStatus = responseStatus.substring(responseStatus.indexOf(' ') + 1, responseStatus.indexOf(' ', responseStatus.indexOf(' ') + 1));
        int status = Integer.parseInt(responseStatus);
        if (status != 200) {
            return "Error " + status + " from Servlet";
        }
        // read all the headers.
        String header = reader.readLine();
        int contentLength = 0;
        while (!"".equals(header)) {
            int colon = header.indexOf(':');
            String headerName = header.substring(0, colon).trim();
            String headerValue = header.substring(colon + 1).trim();
            if ("content-length".equalsIgnoreCase(headerName)) {
                contentLength = Integer.parseInt(headerValue);
            }
            header = reader.readLine();
        }
        if (contentLength > 0) {
            char[] buf = new char[512];
            while (contentLength > 0) {
                int thisTime = (contentLength > buf.length) ? buf.length : contentLength;
                int n = reader.read(buf, 0, thisTime);
                if (n <= 0) {
                    return "Read content failed";
                } else {
                    contentLength -= n;
                }
           }
        }
        return null;
    }
    
}
