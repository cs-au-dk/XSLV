<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml">

<xsl:template match="/">
<html>
<head>
<style>
h1 {
color:red;
font-weight:bold;
background-color:green;
}
</style>
</head>
<xsl:apply-templates/>
</html>
</xsl:template>

<xsl:template match="agenda">
<body>
<h1>
<xsl:value-of select="./@context"/>
</h1>
<xsl:for-each select="event">
<p>
<small>
[by:<xsl:value-of select="submitter"/> /
<xsl:value-of select="./@submission-time"/> /
<xsl:value-of select="./@submission-date"/>]
</small>
<br/>
<strong>
<xsl:value-of select="title"/>
</strong>
<br/>
<xsl:value-of select="description"/><br/>
</p>
</xsl:for-each>
</body>
</xsl:template>

</xsl:stylesheet>
