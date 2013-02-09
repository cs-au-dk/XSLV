<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://docbook.org/ns/docbook"
xmlns:doc="http://nwalsh.com/xsl/documentation/1.0"
                version="1.0"
                exclude-result-prefixes="doc d">

<!-- ********************************************************************
     $Id: manifest.xsl 4856 2005-05-26 07:27:34Z bobstayton $
     ********************************************************************

     This file is part of the XSL DocBook Stylesheet distribution.
     See ../README or http://nwalsh.com/docbook/xsl/ for copyright
     and other information.

     ******************************************************************** -->

<!-- ==================================================================== -->

<xsl:variable name="manifest.base.dir">
</xsl:variable>

<xsl:template name="generate.manifest">
  <xsl:param name="node" select="/"/>
  <xsl:call-template name="write.text.chunk">
    <xsl:with-param name="filename">
      <xsl:if test="$manifest.in.base.dir != 0">
        <xsl:value-of select="$base.dir"/>
      </xsl:if>
      <xsl:value-of select="$manifest"/>
    </xsl:with-param>
    <xsl:with-param name="method" select="'text'"/>
    <xsl:with-param name="content">
      <xsl:apply-templates select="$node" mode="enumerate-files"/>
    </xsl:with-param>
    <xsl:with-param name="encoding" select="$chunker.output.encoding"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="d:set|d:book|d:part|d:preface|d:chapter|d:appendix
                     |d:article
                     |d:reference|d:refentry
                     |d:sect1|d:sect2|d:sect3|d:sect4|d:sect5
                     |d:section
                     |d:book/d:glossary|d:article/d:glossary|d:part/d:glossary
                     |d:book/d:bibliography|d:article/d:bibliography|d:part/d:bibliography
                     |d:colophon"
              mode="enumerate-files">
  <xsl:variable name="ischunk"><xsl:call-template name="chunk"/></xsl:variable>
  <xsl:if test="$ischunk='1'">
    <xsl:call-template name="make-relative-filename">
      <xsl:with-param name="base.dir">
        <xsl:if test="$manifest.in.base.dir = 0">
          <xsl:value-of select="$base.dir"/>
        </xsl:if>
      </xsl:with-param>
      <xsl:with-param name="base.name">
        <xsl:apply-templates mode="chunk-filename" select="."/>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:text>&#10;</xsl:text>
  </xsl:if>
  <xsl:apply-templates select="*" mode="enumerate-files"/>
</xsl:template>

<xsl:template match="d:book/d:index|d:article/d:index|d:part/d:index"
              mode="enumerate-files">
  <xsl:if test="$htmlhelp.output != 1">
    <xsl:variable name="ischunk"><xsl:call-template name="chunk"/></xsl:variable>
    <xsl:if test="$ischunk='1'">
      <xsl:call-template name="make-relative-filename">
        <xsl:with-param name="base.dir">
          <xsl:if test="$manifest.in.base.dir = 0">
            <xsl:value-of select="$base.dir"/>
          </xsl:if>
        </xsl:with-param>
        <xsl:with-param name="base.name">
          <xsl:apply-templates mode="chunk-filename" select="."/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:text>&#10;</xsl:text>
    </xsl:if>
    <xsl:apply-templates select="*" mode="enumerate-files"/>
  </xsl:if>
</xsl:template>

<xsl:template match="d:legalnotice" mode="enumerate-files">
  <xsl:variable name="id"><xsl:call-template name="object.id"/></xsl:variable>
  <xsl:if test="$generate.legalnotice.link != 0">
    <xsl:call-template name="make-relative-filename">
      <xsl:with-param name="base.dir">
        <xsl:if test="$manifest.in.base.dir = 0">
          <xsl:value-of select="$base.dir"/>
        </xsl:if>
      </xsl:with-param>
      <xsl:with-param name="base.name">
        <xsl:apply-templates mode="chunk-filename" select="."/>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:text>&#10;</xsl:text>
  </xsl:if>
</xsl:template>

<xsl:template match="d:mediaobject[d:imageobject] | d:inlinemediaobject[d:imageobject]" mode="enumerate-files">
  <xsl:variable name="longdesc.uri">
    <xsl:call-template name="longdesc.uri">
      <xsl:with-param name="mediaobject"
                      select="."/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="mediaobject" select="."/>

  <xsl:if test="$html.longdesc != 0 and $mediaobject/d:textobject[not(d:phrase)]">
    <xsl:call-template name="longdesc.uri">
      <xsl:with-param name="mediaobject" select="$mediaobject"/>
    </xsl:call-template>
    <xsl:text>&#10;</xsl:text>
  </xsl:if>
</xsl:template>

<xsl:template match="text()" mode="enumerate-files">
</xsl:template>

</xsl:stylesheet>
