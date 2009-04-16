<%@ page pageEncoding="UTF-8"%>
<%@ page import="java.io.*"%>
<HTML>
<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<TITLE>cookies#test.</TITLE>
</HEAD>
<BODY>
<%
  /*
   * Just read the header and process the corresponding tests
   */
  String test = request.getHeader("TEST");
  String action = request.getHeader("ACTION");
  int ntest = Integer.parseInt(test);

  response.setContentType("text/html; charset=UTF-8");

  if (action.compareTo("READ") == 0) {
    switch (ntest) {
      case 1: test(response, request, out, "foo", "bar", "a", "b"); break;
      case 2: test(response, request, out, "foo", "bar", "a", "b"); break;
      case 3: test(response, request, out, "foo", "bar", "a", "b"); break;
      case 4: test(response, request, out, "foo", "bar", "a", "b"); break;
      case 5: test(response, request, out, "foo", "bar", "a", "b"); break;

      case 6: test(response, request, out, "foo", "", "a", "b"); break;
      case 7: test(response, request, out, "foo", "", "a", "b"); break;

      case 8: test(response, request, out, "foo", "bar", "a", "b"); break;
      case 9: test(response, request, out, "foo", "bar", "a", "b"); break;
      
      case 10: test(response, request, out, "foo", "", "a", "b"); break;
      case 11: test(response, request, out, "foo", "", "a", "b"); break;
      case 12: test(response, request, out, "foo", "", "a", "b"); break;

      case 13: test(response, request, out, "foo", "bar", "a", "b"); break;

      case 14: test(response, request, out, "foo", "bar", "a", "b"); break;
      case 15: test(response, request, out, "foo", "bar", "a", "b"); break;

      case 16: test(response, request, out, "foo", "bar", "a", "b"); break;

      case 17: test(response, request, out, "foo", "b", "a", "b"); break;
      case 18: test(response, request, out, "foo", "b\"ar", "a", "b"); break;
      case 19: test(response, request, out, "foo", "b'ar", "a", "b"); break;

      case 20: test(response, request, out, "foo", "b", "a", "b"); break;

      case 21: test(response, request, out, "foo", "bar", "a", "b"); break;
      case 22: test(response, request, out, "foo", "bar", "a", "b"); break;

      case 23: test(response, request, out); break;

      case 24: test(response, request, out, "foo", "bar", "a", "b"); break;

      case 25: test(response, request, out, "foo", "", "a", "b", "bar", ""); break;
      case 26: test(response, request, out, "foo", "", "a", "b", "bar", ""); break;
      case 27: test(response, request, out, "foo", "", "a", "b", "bar", ""); break;
      case 28: test(response, request, out, "foo", "", "a", "b", "bar", ""); break;

      case 29: test(response, request, out, "foo", "", "a", "b", "bar", ""); break;
      case 30: test(response, request, out, "foo", "", "a", "b", "bar", ""); break;
      case 31: test(response, request, out, "foo", "", "a", "b", "bar", "rab"); break;
      case 32: test(response, request, out, "foo", "", "a", "b", "bar", "rab"); break;

      case 33: test(response, request, out, "a", "b", "#", "", "bar", "rab"); break;

      case 34: test(response, request, out, "a", "b", "bar", "rab"); break;

      case 35: test(response, request, out, "foo", "bar", "a", "b"); break;

      case 36: test(response, request, out, 1); break;
      case 37: test(response, request, out, 0); break;

      default: response.sendError(500, "Unknown test");break;
    }
  } else if (action.compareTo("CREATE") == 0) {
    switch (ntest) {
      case 1: out.println("OK");break;
      default: response.sendError(500, "Unknown test");break;
    }
  } else {
    response.sendError(500, "Unknown command");
  }
%>
</BODY>
</HTML>
<%!
void test(HttpServletResponse response, HttpServletRequest request, JspWriter out, String name1, String val1, String name2, String val2, String name3, String val3) throws Exception {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            if (cookies.length != 3) {
                response.sendError(500, "Wrong number of cookies");
                return;
            }
            if (name1.compareTo(cookies[0].getName()) == 0 &&
                val1.compareTo(cookies[0].getValue()) == 0 &&
                name2.compareTo(cookies[1].getName()) == 0 &&
                val2.compareTo(cookies[1].getValue()) == 0 &&
                name3.compareTo(cookies[2].getName()) == 0 &&
                val3.compareTo(cookies[2].getValue()) == 0 )
                out.println("OK");
            else {
                response.sendError(500, "Value or name don't match");
                return;
            }
        } else {
        response.sendError(500, "No cookies");
        }
   }
void test(HttpServletResponse response, HttpServletRequest request, JspWriter out, String name1, String val1, String name2, String val2) throws Exception {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            if (cookies.length != 2) {
                response.sendError(500, "Wrong number of cookies");
                return;
            }
            if (name1.compareTo(cookies[0].getName()) == 0 &&
                val1.compareTo(cookies[0].getValue()) == 0 &&
                name2.compareTo(cookies[1].getName()) == 0 &&
                val2.compareTo(cookies[1].getValue()) == 0)
                out.println("OK");
            else {
                response.sendError(500, "Value or name don't match");
                return;
            }
        } else {
        response.sendError(500, "No cookies");
        }
   }
void test(HttpServletResponse response, HttpServletRequest request, JspWriter out) throws Exception {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            if (cookies.length != 0) {
                response.sendError(500, "Wrong number of cookies");
                return;
            }
        }
        out.println("OK");
   } 
void test(HttpServletResponse response, HttpServletRequest request, JspWriter out, int version) throws Exception {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            if (cookies.length == 1) {
                if (cookies[0].getVersion() == version) {
                    out.println("OK");
                    return;
                }
            }
        }
        response.sendError(500, "Wrong number of cookies");
   }%>
