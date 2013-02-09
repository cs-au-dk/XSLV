<?xml version="1.0" standalone="no"?>
<!-- edited with XML Spy v3.0 (http://www.xmlspy.com) by sd (LEVU) -->
<!-- XSL fro stored procedures
     $Author: dongfang $
     $Date: 2005/11/25 10:31:43 $
     $Revision: 1.1.1.1 $
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" version="1.0">

	<xsl:template match="/">
		<html>
			<head>
				<title>
					Stored procedures
				</title>
				<style type="text/css"><![CDATA[ 
a, a:link, a:visited {
	color: navy;
	text-decoration: underline;
}
a:hover, a:active {
	color: red;
	text-decoration: underline;
}
p, td, li {
	color: black;
	font-family: Arial, Helvetica, Vedrana;
	font-size: 10pt;
	text-align: left;
}
h1, h2, h3 {
	color: navy;
	font-family: Arial, Helvetica, Vedrana;
}
h1 {
	font-size: 14pt;
	font-weight: bold;
}
h2 {
	font-size: 12pt;
	color: navy;
	background-color: #CCCCCC;
	font-style: italic;
}
h3 {
	font-size: 10pt;
	font-weight: bold;
}
body {
	background-color:white;
}

.context {
	background-color: white;
	font-weight: normal;
}
.fieldname {
	font-weight: bold;
	text-align: center;
	vertical-align: text-top;
	color: white;
	background-color: navy;
}
.name {
	text-decoration: none;
}
]]></style>
			</head>
			<body>
				<xsl:apply-templates select="procs"/>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="procs">
		<h1>Stored procedures</h1>
		<p>
			<i>Date: <xsl:value-of select="@date"/>
			</i>
		</p>
		<hr size="2"/>
		<h2>Contents</h2>
		<ol>
      <li>
        <xsl:choose>
          <xsl:when test="name">
      			<a>
			      	<xsl:attribute name="href">#<xsl:value-of select="proc/name"/></xsl:attribute>
      				<xsl:value-of select="proc/name"/>
			      </a>
          </xsl:when>
          <xsl:otherwise>
    			  N/A
          </xsl:otherwise>
        </xsl:choose>
      </li>
		</ol>
		<hr size="2"/>
		<xsl:apply-templates select="proc"/>
	</xsl:template>

	<xsl:template match="execlist/proc">
		<ul>
      <li>
        <xsl:choose>
          <xsl:when test="name">
      			<a>
			      	<xsl:attribute name="href">#<xsl:value-of select="proc/name"/></xsl:attribute>
      				<xsl:value-of select="proc/name"/>
			      </a>
          </xsl:when>
          <xsl:otherwise>
    			  N/A
          </xsl:otherwise>
        </xsl:choose>
      </li>
		</ul>
	</xsl:template>

	<xsl:template match="proc/name">
		<li>
			<a>
				<xsl:attribute name="href">#<xsl:value-of select="node()"/></xsl:attribute>
				<xsl:value-of select="node()"/>
			</a>
		</li>
	</xsl:template>

	<xsl:template match="procs/proc">
		<xsl:if test="description">
			<h2>
				<a class="name">
					<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
					<xsl:value-of select="name"/>
				</a>
			</h2>
			<pre>
				<b>
					<i>Description:</i>
				</b>
				<xsl:value-of select="description"/>
			</pre>
			<xsl:apply-templates select="input"/>
			<xsl:apply-templates select="output"/>
			<xsl:apply-templates select="resultset"/>
			<xsl:apply-templates select="errorcodes"/>
			<xsl:apply-templates select="execlist"/>
			<hr size="1"/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="input">
		<xsl:if test="param">
			<h3>Input parameters</h3>
			<table border="1" cellpadding="1" cellspacing="0">
				<tr>
					<td class="fieldname">Name</td>
					<td class="fieldname">Type</td>
					<td class="fieldname">Required</td>
					<td class="fieldname">Description</td>
				</tr>
				<xsl:apply-templates select="param"/>
			</table>
		</xsl:if>
	</xsl:template>

	<xsl:template match="output">
		<xsl:if test="param">
			<h3>Output parameters</h3>
			<table border="1" cellpadding="1" cellspacing="0">
				<tr>
					<td class="fieldname">Name</td>
					<td class="fieldname">Type</td>
					<td class="fieldname">Description</td>
				</tr>
				<xsl:apply-templates select="param"/>
			</table>
		</xsl:if>
	</xsl:template>

	<xsl:template match="resultset">
		<xsl:if test="param">
			<h3>Resultset</h3>
			<p>
				<xsl:value-of select="description"/>
			</p>
			<table border="1" cellpadding="1" cellspacing="0">
				<tr>
					<td class="fieldname">Name</td>
					<td class="fieldname">Type</td>
					<td class="fieldname">Description</td>
				</tr>
				<xsl:apply-templates select="param"/>
			</table>
		</xsl:if>
	</xsl:template>

	<xsl:template match="errorcodes">
		<xsl:if test="code">
			<h3>Error codes</h3>
			<table border="1" cellpadding="1" cellspacing="0">
				<tr>
					<td class="fieldname">Value</td>
					<td class="fieldname">Description</td>
				</tr>
				<xsl:apply-templates select="code"/>
			</table>
		</xsl:if>
	</xsl:template>

	<xsl:template match="code">
		<tr>
			<td class="context">
				<xsl:value-of select="value"/>
			</td>
			<td class="context">
				<xsl:value-of select="description"/><font color="white">.</font>
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="execlist">
		<xsl:if test="proc">
			<h3>Called procedures list</h3>
			<xsl:apply-templates select="proc"/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="input/param">
		<tr>
			<td class="context">
				<xsl:value-of select="name"/>
			</td>
			<td class="context">
				<xsl:value-of select="type"/>
			</td>
			<td class="context">
				<xsl:value-of select="required"/>
			</td>
			<td class="context">
				<xsl:value-of select="description"/><font color="white">.</font>
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="output/param">
		<tr>
			<td class="context">
				<xsl:value-of select="name"/>
			</td>
			<td class="context">
				<xsl:value-of select="type"/>
			</td>
			<td class="context">
				<xsl:value-of select="description"/><font color="white">.</font>
			</td>
		</tr>
	</xsl:template>
	
	<xsl:template match="resultset/param">
		<tr>
			<td class="context">
				<xsl:value-of select="name"/>
			</td>
			<td class="context">
				<xsl:value-of select="type"/>
			</td>
			<td class="context">
				<xsl:value-of select="description"/><font color="white">.</font>
			</td>
		</tr>
	</xsl:template>
</xsl:stylesheet>
