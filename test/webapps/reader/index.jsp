<%@ page pageEncoding="UTF-8"%>
<%@ page import="java.io.*"%>
<HTML>
<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<TITLE>request#getReader test.</TITLE>
</HEAD>
<BODY>

<%!
   int charSize = 8193;
   char character = 'あ';
%>

<%
response.setContentType("text/html; charset=UTF-8");
StringBuffer sb = new StringBuffer(charSize);
for(int i=0 ; i < charSize ; i++){
    sb.append(character);
}
%>

<FORM method="POST" action="readLine.jsp" enctype="text/plain">
request#getReader()#readLine test<BR>
<input type="text" name="thetext" value="<%= sb.toString()+sb.toString()+sb.toString() %>"/>
<input type="submit" value="send" />
</FORM>

<FORM method="POST" action="read.jsp" enctype="text/plain">
request#getReader()#read() test<BR>
<input type="text" name="thetext" value="<%= sb.toString() %>"/>
<input type="submit" value="send" />
</FORM>

<FORM method="POST" action="readCharB.jsp" enctype="text/plain">
request#getReader()#read(char[1]) test<BR>
<input type="text" name="thetext" value="<%= sb.toString() %>"/>
<input type="submit" value="send" />
</FORM>

</BODY>
</HTML>