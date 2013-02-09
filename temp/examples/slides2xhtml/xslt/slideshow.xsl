<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns="http://www.w3.org/1999/xhtml"
                >
  <xsl:output
    method="xml"
    encoding="ISO-8859-1"
    indent="yes"/>

  <xsl:template match="show">
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
      <head>
        <title><xsl:value-of select="title/text()"/></title>
        <meta http-equiv="content-type" content="text/html; charset=iso-8859-1"/>
        <link rel="stylesheet" type="text/css" href="slides.css"/>
        <xsl:element name="script">
          <xsl:attribute name="src">slides.js</xsl:attribute>
          <xsl:text></xsl:text>
        </xsl:element>
      </head>
      <body>
        <div style="display:block">
          <div class="float">
            <img onclick="return select_prev()" src="left.gif"/>
            <img onclick="return select_up()" src="up.gif"/>
            <img onclick="return select_next()" src="right.gif"/>
          </div>
          <xsl:apply-templates select="title"/>
          <xsl:apply-templates select="slide" mode="summary"/>
        </div>
        <xsl:apply-templates select="slide"/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="slide" mode="summary">
    <xsl:element name="div">
      <xsl:attribute name="onclick">
        return select_nth("slide<xsl:value-of select='position()' />")
      </xsl:attribute>
      <xsl:attribute name="style">
        cursor: pointer;
      </xsl:attribute>
      <h2 onclick="return true">
        <xsl:value-of select="title/text()"/>
      </h2>
    </xsl:element>
  </xsl:template>

  <xsl:template match="slide">
    <xsl:variable name="id" select="position()"/>
    <xsl:element name="div">
      <xsl:attribute name="class">slide</xsl:attribute>
      <xsl:attribute name="id">slide<xsl:value-of select="$id"/></xsl:attribute>
      <div class="float">
        <img onclick="return select_prev()" src="left.gif"/>
        <img onclick="return select_up()" src="up.gif"/>
        <img onclick="return select_next()" src="right.gif"/>
      </div>
      <xsl:apply-templates select="title"/>
      <xsl:apply-templates select="h1|h2|ul|screenshot|figure"/>
      <p>
        <xsl:apply-templates select="link"/>
      </p>
    </xsl:element>
  </xsl:template>

  <xsl:template match="title">
    <h1 class="h1"><xsl:value-of select="text()"/></h1>
  </xsl:template>

  <xsl:template match="h1">
    <h2><xsl:value-of select="text()"/></h2>
  </xsl:template>

  <xsl:template match="h2">
    <h3><xsl:value-of select="text()"/></h3>
  </xsl:template>

  <xsl:template match="ul">
    <ul><xsl:apply-templates select="li"/></ul>
  </xsl:template>

  <xsl:template match="li">
    <li><xsl:value-of select="text()"/></li>
  </xsl:template>

  <xsl:template match="screenshot">
    <table class="screenshot">
      <tr>
        <td>
          <div class="screenshot"><xsl:apply-templates select="child::*"/></div>
        </td>
      </tr>
    </table>
  </xsl:template>

  <xsl:template match="userinput">
    <div class="userinput">$<xsl:text> </xsl:text><xsl:value-of select="text()"/></div><br/>
  </xsl:template>

  <xsl:template match="systemresponse/text()">
    <pre class="systemresponse"><xsl:value-of select="self::text()"/></pre>
    <xsl:apply-templates select="slide"/>
  </xsl:template>

  <xsl:template match="slide">
    <div class="slide">...</div>
  </xsl:template>

  <xsl:template match="figure">
    <div class="figure"><img src="{@src}"/></div>
  </xsl:template>

  <xsl:template match="link">
    <div class="link"><a href="{@target}"><xsl:value-of select="text()"/></a></div>
  </xsl:template>

</xsl:stylesheet>
