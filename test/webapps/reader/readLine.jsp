<%@ page pageEncoding="UTF-8"%>
<%@ page import="java.io.*"%>
<HTML>
<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<TITLE>request#getReader test.</TITLE>
</HEAD>
<BODY>
readLine.jsp is called.
<HR>

<%
            request.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=UTF-8");
            BufferedReader reader = request.getReader();
            StringBuffer sb = new StringBuffer();
            
            readLine(reader, sb);
            
            // TODO check if the read data is correct.
            
            //outln(out,sb.toString());
            outln(out,"Content-Type:" + request.getContentType());
            outln(out,"Character Encoding:" + request.getCharacterEncoding());
            outln(out, "Content-Length:"+ request.getContentLength());
            outln(out, "read:" + sb.length()); // includes form text area name "thetext=" and last CR/LF

%>

</BODY>
</HTML>
<%!

	void readLine(BufferedReader br, StringBuffer sb) throws IOException{
		String read = null;
		while((read = br.readLine())!=null){
		    sb.append(read);// CR/LF lost.
		}
	}
	
	void outln(JspWriter out, String str) throws IOException{
	    out.println(str+"<BR>");
	    System.out.println(str);
	}
%>