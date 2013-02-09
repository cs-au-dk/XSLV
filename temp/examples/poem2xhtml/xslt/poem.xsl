<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" version="1.0">

<xsl:template match="/">
        <xsl:apply-templates/>
</xsl:template>
<xsl:template match="POEM">
        <HTML>
        <BODY BGCOLOR="#CCCCFF">
                <xsl:apply-templates/>
        </BODY>
        </HTML>
</xsl:template>
<xsl:template match="TITLE">
        <H1><FONT COLOR="darkblue">
                <xsl:value-of select="."/>
        </FONT></H1>
</xsl:template>
<xsl:template match="AUTHOR">
        <H2><xsl:apply-templates/></H2>
</xsl:template>
<xsl:template match="FIRSTNAME">
        <xsl:value-of select="."/>
</xsl:template>
<xsl:template match="LASTNAME">
        <xsl:value-of select="."/>
</xsl:template>
<xsl:template match="STANZA">
        <P><xsl:apply-templates/></P>
</xsl:template>
<xsl:template match="LINE">
        <xsl:value-of select="."/><BR/>
</xsl:template>

</xsl:stylesheet>
