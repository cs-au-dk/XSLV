<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml">

  <xsl:template match="/">
    <html>
      <xsl:apply-templates/>
    </html>
  </xsl:template>

  <xsl:template match="link-collection">
    <head>
      <title>link-collection</title>
      <style type="text/css">
        <xsl:text>
          &lt;!--
          a.menu { text-decoration:none; }  
          a.ll   { color:#aaaaff; }  
          //--&gt;
        </xsl:text>
      </style>
    </head>
    <body bgcolor="#000000" text="#DDDDDD" 
      link="#AEAEAE" vlink="#8E8E8E" alink="#880000">
      <xsl:apply-templates/>
    </body>
  </xsl:template>

  <xsl:template match="section">
    <xsl:apply-templates select="./name"/>
    <table border="0" cellspacing="10" cellpadding="10" width="100%">
      <xsl:apply-templates select="link"/>
    </table>
    <br/>
  </xsl:template>

  <xsl:template match="section/name">
    <h2>
      <xsl:apply-templates/>
    </h2>
  </xsl:template>
  
  <xsl:template match="link">
    <tr>
      <xsl:apply-templates select="to|desc"/>
    </tr>
  </xsl:template>
  
  <xsl:template match="link/name">
      <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="link/to">
    <td valign="top" bgcolor="#000044" width="30%">
      <a>
        <xsl:attribute name="class">
          <xsl:text>ll</xsl:text>
        </xsl:attribute>
        <xsl:attribute name="href">
          <xsl:value-of select='.'/>
        </xsl:attribute>

        <xsl:choose>
          <xsl:when test="not(../name)">
            <xsl:value-of select='.'/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="../name"/>
          </xsl:otherwise>
        </xsl:choose>
      </a>
    </td>
  </xsl:template>
  
  <xsl:template match="link/desc">
    <xsl:choose>
      <xsl:when test="not(../name) and not(../to)">
        <td></td>
      </xsl:when>
      <xsl:when test="not(../to)">
        <td valign="top" bgcolor="#000044" width="30%">
          <xsl:apply-templates select="../name"/>
        </td>
      </xsl:when>
    </xsl:choose>
    <td bgcolor="#222222" width="70%">
      <xsl:apply-templates/>
    </td>
  </xsl:template>
  
  
  <xsl:template match="a">
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="@href"/>
      </xsl:attribute>
      
      <xsl:apply-templates/>
    </a>
  </xsl:template>

  <xsl:template match="url">
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="."/>
      </xsl:attribute>
      <xsl:value-of select="."/>
    </a>
  </xsl:template>

  <xsl:template match="email">
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select='concat("mailto:",.)'/>
      </xsl:attribute>
      <xsl:value-of select="."/>
    </a>
  </xsl:template>

  <xsl:template match="sup|sub|br">
    <xsl:element name="{name()}" namespace="http://www.w3.org/1999/xhtml">
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>

