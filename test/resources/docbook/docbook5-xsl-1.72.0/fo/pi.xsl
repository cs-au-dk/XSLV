<?xml version='1.0'?>
<xsl:stylesheet exclude-result-prefixes="d"
                 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://docbook.org/ns/docbook"
xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version='1.0'>

<!-- ********************************************************************
     $Id: pi.xsl 4353 2005-03-08 08:36:29Z bobstayton $
     ********************************************************************

     This file is part of the XSL DocBook Stylesheet distribution.
     See ../README or http://nwalsh.com/docbook/xsl/ for copyright
     and other information.

     ******************************************************************** -->

<xsl:template match="processing-instruction()">
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="dbfo-attribute">
  <xsl:param name="pis" select="processing-instruction('dbfo')"/>
  <xsl:param name="attribute">filename</xsl:param>

  <xsl:call-template name="pi-attribute">
    <xsl:with-param name="pis" select="$pis"/>
    <xsl:with-param name="attribute" select="$attribute"/>
  </xsl:call-template>
</xsl:template>

<xsl:template name="dbfo-filename">
  <xsl:param name="pis" select="./processing-instruction('dbfo')"/>
  <xsl:call-template name="dbfo-attribute">
    <xsl:with-param name="pis" select="$pis"/>
    <xsl:with-param name="attribute">filename</xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template name="dbfo-dir">
  <xsl:param name="pis" select="./processing-instruction('dbfo')"/>
  <xsl:call-template name="dbfo-attribute">
    <xsl:with-param name="pis" select="$pis"/>
    <xsl:with-param name="attribute">dir</xsl:with-param>
  </xsl:call-template>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="process.cmdsynopsis.list">
  <xsl:param name="cmdsynopses"/><!-- empty node list by default -->
  <xsl:param name="count" select="1"/>

  <xsl:choose>
    <xsl:when test="$count>count($cmdsynopses)"></xsl:when>
    <xsl:otherwise>
      <xsl:variable name="cmdsyn" select="$cmdsynopses[$count]"/>

       <dt>
       <a>
         <xsl:attribute name="href">
           <xsl:call-template name="object.id">
             <xsl:with-param name="object" select="$cmdsyn"/>
           </xsl:call-template>
         </xsl:attribute>

         <xsl:choose>
           <xsl:when test="$cmdsyn/@xreflabel">
             <xsl:call-template name="xref.xreflabel">
               <xsl:with-param name="target" select="$cmdsyn"/>
             </xsl:call-template>
           </xsl:when>
           <xsl:otherwise>
             <xsl:apply-templates select="$cmdsyn" mode="xref-to">
               <xsl:with-param name="target" select="$cmdsyn"/>
             </xsl:apply-templates>
           </xsl:otherwise>
         </xsl:choose>
       </a>
       </dt>

        <xsl:call-template name="process.cmdsynopsis.list">
          <xsl:with-param name="cmdsynopses" select="$cmdsynopses"/>
          <xsl:with-param name="count" select="$count+1"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template match="processing-instruction('dbcmdlist')">
  <xsl:variable name="cmdsynopses" select="..//d:cmdsynopsis"/>

  <xsl:if test="count($cmdsynopses)&lt;1">
    <xsl:message><xsl:text>No cmdsynopsis elements matched dbcmdlist PI, perhaps it's nested too deep?</xsl:text>
    </xsl:message>
  </xsl:if>

  <dl>
    <xsl:call-template name="process.cmdsynopsis.list">
      <xsl:with-param name="cmdsynopses" select="$cmdsynopses"/>
    </xsl:call-template>
  </dl>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="process.funcsynopsis.list">
  <xsl:param name="funcsynopses"/><!-- empty node list by default -->
  <xsl:param name="count" select="1"/>

  <xsl:choose>
    <xsl:when test="$count>count($funcsynopses)"></xsl:when>
    <xsl:otherwise>
      <xsl:variable name="cmdsyn" select="$funcsynopses[$count]"/>

       <dt>
       <a>
         <xsl:attribute name="href">
           <xsl:call-template name="object.id">
             <xsl:with-param name="object" select="$cmdsyn"/>
           </xsl:call-template>
         </xsl:attribute>

         <xsl:choose>
           <xsl:when test="$cmdsyn/@xreflabel">
             <xsl:call-template name="xref.xreflabel">
               <xsl:with-param name="target" select="$cmdsyn"/>
             </xsl:call-template>
           </xsl:when>
           <xsl:otherwise>
              <xsl:apply-templates select="$cmdsyn" mode="xref-to">
                <xsl:with-param name="target" select="$cmdsyn"/>
              </xsl:apply-templates>
           </xsl:otherwise>
         </xsl:choose>
       </a>
       </dt>

        <xsl:call-template name="process.funcsynopsis.list">
          <xsl:with-param name="funcsynopses" select="$funcsynopses"/>
          <xsl:with-param name="count" select="$count+1"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template match="processing-instruction('dbfunclist')">
  <xsl:variable name="funcsynopses" select="..//d:funcsynopsis"/>

  <xsl:if test="count($funcsynopses)&lt;1">
    <xsl:message><xsl:text>No funcsynopsis elements matched dbfunclist PI, perhaps it's nested too deep?</xsl:text>
    </xsl:message>
  </xsl:if>

  <dl>
    <xsl:call-template name="process.funcsynopsis.list">
      <xsl:with-param name="funcsynopses" select="$funcsynopses"/>
    </xsl:call-template>
  </dl>
</xsl:template>

<!-- ==================================================================== -->

<!-- "need" processing instruction, a kind of soft page break -->
<!-- A "need" is a request for space on a page.  If the requested space
     is not available, the page breaks and the content that follows
     the need request appears on the next page. If the requested
     space is available, then the request is ignored. -->

<xsl:template match="processing-instruction('dbfo-need')">

  <xsl:variable name="pi-height">
    <xsl:call-template name="dbfo-attribute">
      <xsl:with-param name="pis" select="."/>
      <xsl:with-param name="attribute" select="'height'"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="height">
    <xsl:choose>
      <xsl:when test="$pi-height != ''">
        <xsl:value-of select="$pi-height"/>
      </xsl:when>
      <xsl:otherwise>0pt</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="pi-before">
    <xsl:call-template name="dbfo-attribute">
      <xsl:with-param name="pis" select="."/>
      <xsl:with-param name="attribute" select="'space-before'"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="spacer">
    <fo:block-container width="100%" height="{$height}">
      <fo:block><fo:leader leader-length="0pt"/></fo:block>
    </fo:block-container>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="$fop.extensions != 0">
      <!-- Doesn't work in fop -->
    </xsl:when>
    <xsl:when test="$pi-before != '' and
                    not(following-sibling::d:listitem) and
                    not(following-sibling::d:step)">
      <fo:block space-after="0pt" space-before="{$pi-before}">
        <xsl:copy-of select="$spacer"/>
      </fo:block>
    </xsl:when>
    <xsl:when test="following-sibling::d:para">
      <fo:block space-after="0pt" 
                xsl:use-attribute-sets="normal.para.spacing">
        <xsl:copy-of select="$spacer"/>
      </fo:block>
    </xsl:when>
    <xsl:when test="following-sibling::d:table or
                    following-sibling::d:figure or
                    following-sibling::d:example or
                    following-sibling::d:equation">
      <fo:block space-after="0pt" 
                xsl:use-attribute-sets="formal.object.properties">
        <xsl:copy-of select="$spacer"/>
      </fo:block>
    </xsl:when>
    <xsl:when test="following-sibling::d:informaltable or
                    following-sibling::d:informalfigure or
                    following-sibling::d:informalexample or
                    following-sibling::d:informalequation">
      <fo:block space-after="0pt" 
                xsl:use-attribute-sets="informal.object.properties">
        <xsl:copy-of select="$spacer"/>
      </fo:block>
    </xsl:when>
    <xsl:when test="following-sibling::d:itemizedlist or
                    following-sibling::d:orderedlist or
                    following-sibling::d:variablelist or
                    following-sibling::d:simplelist">
      <fo:block space-after="0pt" 
                xsl:use-attribute-sets="informal.object.properties">
        <xsl:copy-of select="$spacer"/>
      </fo:block>
    </xsl:when>
    <xsl:when test="following-sibling::d:listitem or
                    following-sibling::d:step">
      <fo:list-item space-after="0pt" 
                xsl:use-attribute-sets="informal.object.properties">
        <fo:list-item-label/>
        <fo:list-item-body start-indent="0pt" end-indent="0pt">
          <xsl:copy-of select="$spacer"/>
        </fo:list-item-body>
      </fo:list-item>
    </xsl:when>
    <xsl:when test="following-sibling::d:sect1 or
                    following-sibling::d:sect2 or
                    following-sibling::d:sect3 or
                    following-sibling::d:sect4 or
                    following-sibling::d:sect5 or
                    following-sibling::d:section">
      <fo:block space-after="0pt" 
                xsl:use-attribute-sets="section.title.properties">
        <xsl:copy-of select="$spacer"/>
      </fo:block>
    </xsl:when>
    <xsl:otherwise>
      <fo:block space-after="0pt" space-before="0em">
        <xsl:copy-of select="$spacer"/>
      </fo:block>
    </xsl:otherwise>
  </xsl:choose>

  <xsl:choose>
    <xsl:when test="$fop.extensions != 0">
      <!-- Doesn't work in fop -->
    </xsl:when>
    <xsl:when test="following-sibling::d:listitem or
                    following-sibling::d:step">
      <fo:list-item space-before.precedence="force"
                space-before="-{$height}"
                space-after="0pt"
                space-after.precedence="force">
        <fo:list-item-label/>
        <fo:list-item-body start-indent="0pt" end-indent="0pt"/>
      </fo:list-item>
    </xsl:when>
    <xsl:otherwise>
      <fo:block space-before.precedence="force"
                space-before="-{$height}"
                space-after="0pt"
                space-after.precedence="force">
      </fo:block>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- ==================================================================== -->

</xsl:stylesheet>
