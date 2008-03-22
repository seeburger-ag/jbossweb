<%@ page pageEncoding="UTF-8"%>
<%@ page import="java.io.*"%>
<HTML>
<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<TITLE>request#getReader test.</TITLE>
</HEAD>
<BODY>
read.jsp is called.
<HR>

<%
            request.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=UTF-8");
            BufferedReader reader = request.getReader();
            StringBuffer sb = new StringBuffer();

            read(reader, sb);

            // TODO check if the read data is correct.
            //outln(out,sb.toString());
            outln(out,"Content-Type:" + request.getContentType());
            outln(out,"Character Encoding:" + request.getCharacterEncoding());
            outln(out, "Content-Length:"+ request.getContentLength());
            outln(out, "read:" + sb.length()); // includes form text area name "thetext="

%>

</BODY>
</HTML>
<%!
	void read(BufferedReader br, StringBuffer sb) throws IOException{
    	int read = 0;
    	while((read = br.read())!= -1){
    	    sb.append((char)read);
    	}
    }

	void outln(JspWriter out, String str) throws IOException{
	    out.println(str+"<BR>");
	    System.out.println(str);
	}
%>