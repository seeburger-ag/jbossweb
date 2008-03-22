<%@ page pageEncoding="UTF-8"%>
<%@ page import="java.io.*"%>
<HTML>
<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<TITLE>request#getReader test.</TITLE>
</HEAD>
<BODY>
readCharb.jsp is called.
<HR>

<%
            request.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=UTF-8");
            BufferedReader reader = request.getReader();
            StringBuffer sb = new StringBuffer();

            readCharB(reader, sb, 1);
            
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
	
	void readCharB(BufferedReader br, StringBuffer sb, int bufferSize) throws IOException{
	    char[] buf = new char[bufferSize];
	    int read = 0;
    	while((read = br.read(buf))!= -1){
    	    sb.append(buf, 0, read);
    	}
	}
	
	void outln(JspWriter out, String str) throws IOException{
	    out.println(str+"<BR>");
	    System.out.println(str);
	}
%>