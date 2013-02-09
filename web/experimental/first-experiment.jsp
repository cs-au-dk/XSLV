<?xml version="1.0" encoding="utf-8"?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" xmlns="http://www.w3.org/1999/xhtml" xmlns:jstl="http://java.sun.com/jstl/core" version="2.0">
<jsp:directive.page language="java" session="true" isThreadSafe="true" info="text" isELIgnored="false" import="java.util.*,  org.apache.commons.fileupload.*,  org.apache.commons.fileupload.disk.*,  org.apache.commons.fileupload.servlet.*" contentType="application/xhtml+xml; charset=utf-8"/>

<jsp:useBean id="filer" scope="session" type="org.apache.commons.fileupload.servlet.ServletFileUpload" class="org.apache.commons.fileupload.servlet.ServletFileUpload"/>

<jsp:output doctype-root-element="html" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" doctype-system="http://www.w3c.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

<!-- This is a very simple XML (XHTML) page example -->

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<body>
<h1>Yr Hdrs</h1>
<table border="1">
<jsp:scriptlet>
Enumeration hdrns = request.getHeaderNames();
while(hdrns.hasMoreElements()) {
</jsp:scriptlet>
<tr>
<td>
<jsp:scriptlet>
String hdrn = hdrns.nextElement().toString();
out.print(hdrn);
</jsp:scriptlet>
</td>
<td>
<jsp:scriptlet>
out.print(request.getHeader(hdrn));
</jsp:scriptlet>
</td>
</tr>
<jsp:scriptlet>
}
</jsp:scriptlet>
</table>

<form method="post" action="./first-experiment.jsp" enctype="multipart/form-data" id="input">

Upload a file:
<input name="somefileH" id="somefile" type="file"/>
<br/>

<input value="Submit" type="submit"/>
</form>
<p>

<h1>Yr Files</h1>

<table border="1">

<jsp:scriptlet>
try {
filer.setFileItemFactory(new DiskFileItemFactory());

List items = filer.parseRequest(request);

for (Iterator it=items.iterator(); it.hasNext();) {
FileItem fi = (FileItem)it.next();
</jsp:scriptlet>
<tr>
<td>
<jsp:scriptlet>
out.print(fi.getContentType());
</jsp:scriptlet>
</td>
<td>
<jsp:scriptlet>
out.print(fi.getName());
</jsp:scriptlet>
</td>
</tr>
<jsp:scriptlet>
}
} catch(FileUploadException ex) {
  out.println(ex);
}
</jsp:scriptlet>


</table>
</p>
<p>
<jsp:scriptlet>
session.setAttribute("foo", "bar");
pageContext.setAttribute("foo", "bar");
</jsp:scriptlet>
${foo}
<br/>
${session.foo}
<br/>
<!--${pageContext.getAttribute("df:foo")}-->
<br/>
&gt;
<jsp:expression>
pageContext.findAttribute("foo")
</jsp:expression>
&lt;
</p>
</body>
</html>

</jsp:root>
