<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" version="1.0">

<xsl:template match="/">
        <xsl:apply-templates/>
</xsl:template>
<xsl:template match="POEM">
        <html>
        <head><title>whatever</title></head>
        <body bgcolor="#CCCCFF">
                <xsl:apply-templates/>
        </body>
        </html>
</xsl:template>
<xsl:template match="TITLE">
        <h1><font color="blue">
                <xsl:value-of select="."/>
        </font></h1>
</xsl:template>
<xsl:template match="AUTHOR">
        <h2><xsl:apply-templates/></h2>
</xsl:template>
<xsl:template match="FIRSTNAME">
        <xsl:value-of select="."/>
</xsl:template>
<xsl:template match="LASTNAME">
        <xsl:value-of select="."/>
</xsl:template>
<xsl:template match="STANZA">
        <p><xsl:apply-templates/></p>
</xsl:template>
<xsl:template match="LINE">
        <xsl:value-of select="."/><br/>
</xsl:template>

</xsl:stylesheet>
