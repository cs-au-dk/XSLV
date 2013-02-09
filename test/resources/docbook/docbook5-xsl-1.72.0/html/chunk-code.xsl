<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://docbook.org/ns/docbook"
xmlns:exsl="http://exslt.org/common"
                xmlns:cf="http://docbook.sourceforge.net/xmlns/chunkfast/1.0"
                xmlns:ng="http://docbook.org/docbook-ng"
                xmlns:db="http://docbook.org/ns/docbook"
                version="1.0"
                exclude-result-prefixes="exsl cf ng db d">

<!-- ********************************************************************
     $Id: chunk-code.xsl 6434 2006-11-18 09:00:48Z bobstayton $
     ********************************************************************

     This file is part of the XSL DocBook Stylesheet distribution.
     See ../README or http://nwalsh.com/docbook/xsl/ for copyright
     and other information.

     ******************************************************************** -->

<!-- ==================================================================== -->

<xsl:param name="onechunk" select="0"/>
<xsl:param name="refentry.separator" select="0"/>
<xsl:param name="chunk.fast" select="0"/>

<xsl:key name="genid" match="*" use="generate-id()"/>

<!-- ==================================================================== -->

<xsl:variable name="chunk.hierarchy">
  <xsl:if test="$chunk.fast != 0">
    <xsl:choose>
      <xsl:when test="function-available('exsl:node-set')">
        <xsl:message>Computing chunks...</xsl:message>
        <xsl:apply-templates select="/*" mode="find.chunks"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message>
          <xsl:text>Fast chunking requires exsl:node-set(). </xsl:text>
          <xsl:text>Using "slow" chunking.</xsl:text>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:variable>

<xsl:template match="*" mode="find.chunks">
  <xsl:variable name="chunk">
    <xsl:call-template name="chunk"/>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="$chunk != 0">
      <cf:div id="{generate-id()}">
        <xsl:apply-templates select="." mode="class.attribute"/>
        <xsl:apply-templates select="*" mode="find.chunks"/>
      </cf:div>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="*" mode="find.chunks"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="process-chunk-element">
  <xsl:param name="content">
    <xsl:apply-imports/>
  </xsl:param>

  <xsl:choose>
    <xsl:when test="$chunk.fast != 0 and function-available('exsl:node-set')">
      <xsl:variable name="chunks" select="exsl:node-set($chunk.hierarchy)//cf:div"/>
      <xsl:variable name="genid" select="generate-id()"/>

      <xsl:variable name="div" select="$chunks[@id=$genid or @xml:id=$genid]"/>

      <xsl:variable name="prevdiv"
                    select="($div/preceding-sibling::cf:div|$div/preceding::cf:div|$div/parent::cf:div)[last()]"/>
      <xsl:variable name="prev" select="key('genid', ($prevdiv/@id|$prevdiv/@xml:id)[1])"/>

      <xsl:variable name="nextdiv"
                    select="($div/following-sibling::cf:div|$div/following::cf:div|$div/cf:div)[1]"/>
      <xsl:variable name="next" select="key('genid', ($nextdiv/@id|$nextdiv/@xml:id)[1])"/>

      <xsl:choose>
        <xsl:when test="$onechunk != 0 and parent::*">
          <xsl:copy-of select="$content"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="process-chunk">
            <xsl:with-param name="prev" select="$prev"/>
            <xsl:with-param name="next" select="$next"/>
            <xsl:with-param name="content" select="$content"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:choose>
        <xsl:when test="$onechunk != 0 and not(parent::*)">
          <xsl:call-template name="chunk-all-sections">
            <xsl:with-param name="content" select="$content"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="$onechunk != 0">
          <xsl:copy-of select="$content"/>
        </xsl:when>
        <xsl:when test="$chunk.first.sections = 0">
          <xsl:call-template name="chunk-first-section-with-parent">
            <xsl:with-param name="content" select="$content"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="chunk-all-sections">
            <xsl:with-param name="content" select="$content"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="process-chunk">
  <xsl:param name="prev" select="."/>
  <xsl:param name="next" select="."/>
  <xsl:param name="content">
    <xsl:apply-imports/>
  </xsl:param>

  <xsl:variable name="ischunk">
    <xsl:call-template name="chunk"/>
  </xsl:variable>

  <xsl:variable name="chunkfn">
    <xsl:if test="$ischunk='1'">
      <xsl:apply-templates mode="chunk-filename" select="."/>
    </xsl:if>
  </xsl:variable>

  <xsl:if test="$ischunk='0'">
    <xsl:message>
      <xsl:text>Error </xsl:text>
      <xsl:value-of select="name(.)"/>
      <xsl:text> is not a chunk!</xsl:text>
    </xsl:message>
  </xsl:if>

  <xsl:variable name="filename">
    <xsl:call-template name="make-relative-filename">
      <xsl:with-param name="base.dir" select="$base.dir"/>
      <xsl:with-param name="base.name" select="$chunkfn"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:call-template name="write.chunk">
    <xsl:with-param name="filename" select="$filename"/>
    <xsl:with-param name="content">
      <xsl:call-template name="chunk-element-content">
        <xsl:with-param name="prev" select="$prev"/>
        <xsl:with-param name="next" select="$next"/>
        <xsl:with-param name="content" select="$content"/>
      </xsl:call-template>
    </xsl:with-param>
    <xsl:with-param name="quiet" select="$chunk.quietly"/>
  </xsl:call-template>
</xsl:template>

<xsl:template name="chunk-first-section-with-parent">
  <xsl:param name="content">
    <xsl:apply-imports/>
  </xsl:param>

  <!-- These xpath expressions are really hairy. The trick is to pick sections -->
  <!-- that are not first children and are not the children of first children -->

  <!-- Break these variables into pieces to work around
       http://nagoya.apache.org/bugzilla/show_bug.cgi?id=6063 -->

  <xsl:variable name="prev-v1"
     select="(ancestor::d:sect1[$chunk.section.depth &gt; 0
                               and preceding-sibling::d:sect1][1]

             |ancestor::d:sect2[$chunk.section.depth &gt; 1
                               and preceding-sibling::d:sect2
                               and parent::d:sect1[preceding-sibling::d:sect1]][1]

             |ancestor::d:sect3[$chunk.section.depth &gt; 2
                               and preceding-sibling::d:sect3
                               and parent::d:sect2[preceding-sibling::d:sect2]
                               and ancestor::d:sect1[preceding-sibling::d:sect1]][1]

             |ancestor::d:sect4[$chunk.section.depth &gt; 3
                               and preceding-sibling::d:sect4
                               and parent::d:sect3[preceding-sibling::d:sect3]
                               and ancestor::d:sect2[preceding-sibling::d:sect2]
                               and ancestor::d:sect1[preceding-sibling::d:sect1]][1]

             |ancestor::d:sect5[$chunk.section.depth &gt; 4
                               and preceding-sibling::d:sect5
                               and parent::d:sect4[preceding-sibling::d:sect4]
                               and ancestor::d:sect3[preceding-sibling::d:sect3]
                               and ancestor::d:sect2[preceding-sibling::d:sect2]
                               and ancestor::d:sect1[preceding-sibling::d:sect1]][1]

             |ancestor::d:section[$chunk.section.depth &gt; count(ancestor::d:section)
                                and not(ancestor::d:section[not(preceding-sibling::d:section)])][1])[last()]"/>

  <xsl:variable name="prev-v2"
     select="(preceding::d:sect1[$chunk.section.depth &gt; 0
                               and preceding-sibling::d:sect1][1]

             |preceding::d:sect2[$chunk.section.depth &gt; 1
                               and preceding-sibling::d:sect2
                               and parent::d:sect1[preceding-sibling::d:sect1]][1]

             |preceding::d:sect3[$chunk.section.depth &gt; 2
                               and preceding-sibling::d:sect3
                               and parent::d:sect2[preceding-sibling::d:sect2]
                               and ancestor::d:sect1[preceding-sibling::d:sect1]][1]

             |preceding::d:sect4[$chunk.section.depth &gt; 3
                               and preceding-sibling::d:sect4
                               and parent::d:sect3[preceding-sibling::d:sect3]
                               and ancestor::d:sect2[preceding-sibling::d:sect2]
                               and ancestor::d:sect1[preceding-sibling::d:sect1]][1]

             |preceding::d:sect5[$chunk.section.depth &gt; 4
                               and preceding-sibling::d:sect5
                               and parent::d:sect4[preceding-sibling::d:sect4]
                               and ancestor::d:sect3[preceding-sibling::d:sect3]
                               and ancestor::d:sect2[preceding-sibling::d:sect2]
                               and ancestor::d:sect1[preceding-sibling::d:sect1]][1]

             |preceding::d:section[$chunk.section.depth &gt; count(ancestor::d:section)
                                 and preceding-sibling::d:section
                                 and not(ancestor::d:section[not(preceding-sibling::d:section)])][1])[last()]"/>

  <xsl:variable name="prev"
    select="(preceding::d:book[1]
             |preceding::d:preface[1]
             |preceding::d:chapter[1]
             |preceding::d:appendix[1]
             |preceding::d:part[1]
             |preceding::d:reference[1]
             |preceding::d:refentry[1]
             |preceding::d:colophon[1]
             |preceding::d:article[1]
             |preceding::d:bibliography[parent::d:article or parent::d:book or parent::d:part][1]
             |preceding::d:glossary[parent::d:article or parent::d:book or parent::d:part][1]
             |preceding::d:index[$generate.index != 0]
                               [parent::d:article or parent::d:book or parent::d:part][1]
             |preceding::d:setindex[$generate.index != 0][1]
             |ancestor::d:set
             |ancestor::d:book[1]
             |ancestor::d:preface[1]
             |ancestor::d:chapter[1]
             |ancestor::d:appendix[1]
             |ancestor::d:part[1]
             |ancestor::d:reference[1]
             |ancestor::d:article[1]
             |$prev-v1
             |$prev-v2)[last()]"/>

  <xsl:variable name="next-v1"
    select="(following::d:sect1[$chunk.section.depth &gt; 0
                               and preceding-sibling::d:sect1][1]

             |following::d:sect2[$chunk.section.depth &gt; 1
                               and preceding-sibling::d:sect2
                               and parent::d:sect1[preceding-sibling::d:sect1]][1]

             |following::d:sect3[$chunk.section.depth &gt; 2
                               and preceding-sibling::d:sect3
                               and parent::d:sect2[preceding-sibling::d:sect2]
                               and ancestor::d:sect1[preceding-sibling::d:sect1]][1]

             |following::d:sect4[$chunk.section.depth &gt; 3
                               and preceding-sibling::d:sect4
                               and parent::d:sect3[preceding-sibling::d:sect3]
                               and ancestor::d:sect2[preceding-sibling::d:sect2]
                               and ancestor::d:sect1[preceding-sibling::d:sect1]][1]

             |following::d:sect5[$chunk.section.depth &gt; 4
                               and preceding-sibling::d:sect5
                               and parent::d:sect4[preceding-sibling::d:sect4]
                               and ancestor::d:sect3[preceding-sibling::d:sect3]
                               and ancestor::d:sect2[preceding-sibling::d:sect2]
                               and ancestor::d:sect1[preceding-sibling::d:sect1]][1]

             |following::d:section[$chunk.section.depth &gt; count(ancestor::d:section)
                                 and preceding-sibling::d:section
                                 and not(ancestor::d:section[not(preceding-sibling::d:section)])][1])[1]"/>

  <xsl:variable name="next-v2"
    select="(descendant::d:sect1[$chunk.section.depth &gt; 0
                               and preceding-sibling::d:sect1][1]

             |descendant::d:sect2[$chunk.section.depth &gt; 1
                               and preceding-sibling::d:sect2
                               and parent::d:sect1[preceding-sibling::d:sect1]][1]

             |descendant::d:sect3[$chunk.section.depth &gt; 2
                               and preceding-sibling::d:sect3
                               and parent::d:sect2[preceding-sibling::d:sect2]
                               and ancestor::d:sect1[preceding-sibling::d:sect1]][1]

             |descendant::d:sect4[$chunk.section.depth &gt; 3
                               and preceding-sibling::d:sect4
                               and parent::d:sect3[preceding-sibling::d:sect3]
                               and ancestor::d:sect2[preceding-sibling::d:sect2]
                               and ancestor::d:sect1[preceding-sibling::d:sect1]][1]

             |descendant::d:sect5[$chunk.section.depth &gt; 4
                               and preceding-sibling::d:sect5
                               and parent::d:sect4[preceding-sibling::d:sect4]
                               and ancestor::d:sect3[preceding-sibling::d:sect3]
                               and ancestor::d:sect2[preceding-sibling::d:sect2]
                               and ancestor::d:sect1[preceding-sibling::d:sect1]][1]

             |descendant::d:section[$chunk.section.depth &gt; count(ancestor::d:section)
                                 and preceding-sibling::d:section
                                 and not(ancestor::d:section[not(preceding-sibling::d:section)])])[1]"/>

  <xsl:variable name="next"
    select="(following::d:book[1]
             |following::d:preface[1]
             |following::d:chapter[1]
             |following::d:appendix[1]
             |following::d:part[1]
             |following::d:reference[1]
             |following::d:refentry[1]
             |following::d:colophon[1]
             |following::d:bibliography[parent::d:article or parent::d:book or parent::d:part][1]
             |following::d:glossary[parent::d:article or parent::d:book or parent::d:part][1]
             |following::d:index[$generate.index != 0]
                               [parent::d:article or parent::d:book or parent::d:part][1]
             |following::d:article[1]
             |following::d:setindex[$generate.index != 0][1]
             |descendant::d:book[1]
             |descendant::d:preface[1]
             |descendant::d:chapter[1]
             |descendant::d:appendix[1]
             |descendant::d:article[1]
             |descendant::d:bibliography[parent::d:article or parent::d:book or parent::d:part][1]
             |descendant::d:glossary[parent::d:article or parent::d:book or parent::d:part][1]
             |descendant::d:index[$generate.index != 0]
                               [parent::d:article or parent::d:book or parent::d:part][1]
             |descendant::d:colophon[1]
             |descendant::d:setindex[$generate.index != 0][1]
             |descendant::d:part[1]
             |descendant::d:reference[1]
             |descendant::d:refentry[1]
             |$next-v1
             |$next-v2)[1]"/>

  <xsl:call-template name="process-chunk">
    <xsl:with-param name="prev" select="$prev"/>
    <xsl:with-param name="next" select="$next"/>
    <xsl:with-param name="content" select="$content"/>
  </xsl:call-template>
</xsl:template>

<xsl:template name="chunk-all-sections">
  <xsl:param name="content">
    <xsl:apply-imports/>
  </xsl:param>

  <xsl:variable name="prev-v1"
    select="(preceding::d:sect1[$chunk.section.depth &gt; 0][1]
             |preceding::d:sect2[$chunk.section.depth &gt; 1][1]
             |preceding::d:sect3[$chunk.section.depth &gt; 2][1]
             |preceding::d:sect4[$chunk.section.depth &gt; 3][1]
             |preceding::d:sect5[$chunk.section.depth &gt; 4][1]
             |preceding::d:section[$chunk.section.depth &gt; count(ancestor::d:section)][1])[last()]"/>

  <xsl:variable name="prev-v2"
    select="(ancestor::d:sect1[$chunk.section.depth &gt; 0][1]
             |ancestor::d:sect2[$chunk.section.depth &gt; 1][1]
             |ancestor::d:sect3[$chunk.section.depth &gt; 2][1]
             |ancestor::d:sect4[$chunk.section.depth &gt; 3][1]
             |ancestor::d:sect5[$chunk.section.depth &gt; 4][1]
             |ancestor::d:section[$chunk.section.depth &gt; count(ancestor::d:section)][1])[last()]"/>

  <xsl:variable name="prev"
    select="(preceding::d:book[1]
             |preceding::d:preface[1]
             |preceding::d:chapter[1]
             |preceding::d:appendix[1]
             |preceding::d:part[1]
             |preceding::d:reference[1]
             |preceding::d:refentry[1]
             |preceding::d:colophon[1]
             |preceding::d:article[1]
             |preceding::d:bibliography[parent::d:article or parent::d:book or parent::d:part][1]
             |preceding::d:glossary[parent::d:article or parent::d:book or parent::d:part][1]
             |preceding::d:index[$generate.index != 0]
                               [parent::d:article or parent::d:book or parent::d:part][1]
             |preceding::d:setindex[$generate.index != 0][1]
             |ancestor::d:set
             |ancestor::d:book[1]
             |ancestor::d:preface[1]
             |ancestor::d:chapter[1]
             |ancestor::d:appendix[1]
             |ancestor::d:part[1]
             |ancestor::d:reference[1]
             |ancestor::d:article[1]
             |$prev-v1
             |$prev-v2)[last()]"/>

  <xsl:variable name="next-v1"
    select="(following::d:sect1[$chunk.section.depth &gt; 0][1]
             |following::d:sect2[$chunk.section.depth &gt; 1][1]
             |following::d:sect3[$chunk.section.depth &gt; 2][1]
             |following::d:sect4[$chunk.section.depth &gt; 3][1]
             |following::d:sect5[$chunk.section.depth &gt; 4][1]
             |following::d:section[$chunk.section.depth &gt; count(ancestor::d:section)][1])[1]"/>

  <xsl:variable name="next-v2"
    select="(descendant::d:sect1[$chunk.section.depth &gt; 0][1]
             |descendant::d:sect2[$chunk.section.depth &gt; 1][1]
             |descendant::d:sect3[$chunk.section.depth &gt; 2][1]
             |descendant::d:sect4[$chunk.section.depth &gt; 3][1]
             |descendant::d:sect5[$chunk.section.depth &gt; 4][1]
             |descendant::d:section[$chunk.section.depth 
                                  &gt; count(ancestor::d:section)][1])[1]"/>

  <xsl:variable name="next"
    select="(following::d:book[1]
             |following::d:preface[1]
             |following::d:chapter[1]
             |following::d:appendix[1]
             |following::d:part[1]
             |following::d:reference[1]
             |following::d:refentry[1]
             |following::d:colophon[1]
             |following::d:bibliography[parent::d:article or parent::d:book or parent::d:part][1]
             |following::d:glossary[parent::d:article or parent::d:book or parent::d:part][1]
             |following::d:index[$generate.index != 0]
                               [parent::d:article or parent::d:book][1]
             |following::d:article[1]
             |following::d:setindex[$generate.index != 0][1]
             |descendant::d:book[1]
             |descendant::d:preface[1]
             |descendant::d:chapter[1]
             |descendant::d:appendix[1]
             |descendant::d:article[1]
             |descendant::d:bibliography[parent::d:article or parent::d:book][1]
             |descendant::d:glossary[parent::d:article or parent::d:book or parent::d:part][1]
             |descendant::d:index[$generate.index != 0]
                               [parent::d:article or parent::d:book][1]
             |descendant::d:colophon[1]
             |descendant::d:setindex[$generate.index != 0][1]
             |descendant::d:part[1]
             |descendant::d:reference[1]
             |descendant::d:refentry[1]
             |$next-v1
             |$next-v2)[1]"/>

  <xsl:call-template name="process-chunk">
    <xsl:with-param name="prev" select="$prev"/>
    <xsl:with-param name="next" select="$next"/>
    <xsl:with-param name="content" select="$content"/>
  </xsl:call-template>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template match="/">
  <xsl:choose>
    <!-- include extra test for Xalan quirk -->
    <xsl:when test="namespace-uri(*[1]) != 'http://docbook.org/ns/docbook'">
  <xsl:message>Adding DocBook namespace to version 4 DocBook document</xsl:message>
  <xsl:variable name="addns">
    <xsl:apply-templates mode="addNS"/>
  </xsl:variable>
  <xsl:apply-templates select="exsl:node-set($addns)"/>
</xsl:when>
    <!-- Can't process unless namespace removed -->
    <xsl:when test="namespace-uri(*[1]) != 'http://docbook.org/ns/docbook'">
  <xsl:message>Adding DocBook namespace to version 4 DocBook document</xsl:message>
  <xsl:variable name="addns">
    <xsl:apply-templates mode="addNS"/>
  </xsl:variable>
  <xsl:apply-templates select="exsl:node-set($addns)"/>
</xsl:when>
    <xsl:otherwise>
      <xsl:choose>
        <xsl:when test="$rootid != ''">
          <xsl:choose>
            <xsl:when test="count(key('id',$rootid)) = 0">
              <xsl:message terminate="yes">
                <xsl:text>ID '</xsl:text>
                <xsl:value-of select="$rootid"/>
                <xsl:text>' not found in document.</xsl:text>
              </xsl:message>
            </xsl:when>
            <xsl:otherwise>
              <xsl:if test="$collect.xref.targets = 'yes' or
                            $collect.xref.targets = 'only'">
                <xsl:apply-templates select="key('id', $rootid)"
                                     mode="collect.targets"/>
              </xsl:if>
              <xsl:if test="$collect.xref.targets != 'only'">
                <xsl:apply-templates select="key('id',$rootid)"
                                     mode="process.root"/>
                <xsl:if test="$tex.math.in.alt != ''">
                  <xsl:apply-templates select="key('id',$rootid)"
                                       mode="collect.tex.math"/>
                </xsl:if>
                <xsl:if test="$generate.manifest != 0">
                  <xsl:call-template name="generate.manifest">
                    <xsl:with-param name="node" select="key('id',$rootid)"/>
                  </xsl:call-template>
                </xsl:if>
              </xsl:if>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:if test="$collect.xref.targets = 'yes' or
                        $collect.xref.targets = 'only'">
            <xsl:apply-templates select="/" mode="collect.targets"/>
          </xsl:if>
          <xsl:if test="$collect.xref.targets != 'only'">
            <xsl:apply-templates select="/" mode="process.root"/>
            <xsl:if test="$tex.math.in.alt != ''">
              <xsl:apply-templates select="/" mode="collect.tex.math"/>
            </xsl:if>
            <xsl:if test="$generate.manifest != 0">
              <xsl:call-template name="generate.manifest">
                <xsl:with-param name="node" select="/"/>
              </xsl:call-template>
            </xsl:if>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*" mode="process.root">
  <xsl:apply-templates select="."/>
</xsl:template>

<!-- ====================================================================== -->

<xsl:template match="d:set|d:book|d:part|d:preface|d:chapter|d:appendix
                     |d:article
                     |d:reference|d:refentry
                     |d:book/d:glossary|d:article/d:glossary|d:part/d:glossary
                     |d:book/d:bibliography|d:article/d:bibliography|d:part/d:bibliography
                     |d:colophon">
  <xsl:choose>
    <xsl:when test="$onechunk != 0 and parent::*">
      <xsl:apply-imports/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="process-chunk-element"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="d:sect1|d:sect2|d:sect3|d:sect4|d:sect5|d:section">
  <xsl:variable name="ischunk">
    <xsl:call-template name="chunk"/>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="not(parent::*)">
      <xsl:call-template name="process-chunk-element"/>
    </xsl:when>
    <xsl:when test="$ischunk = 0">
      <xsl:apply-imports/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="process-chunk-element"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="d:setindex
                     |d:book/d:index
                     |d:article/d:index
                     |d:part/d:index">
  <!-- some implementations use completely empty index tags to indicate -->
  <!-- where an automatically generated index should be inserted. so -->
  <!-- if the index is completely empty, skip it. -->
  <xsl:if test="count(*)>0 or $generate.index != '0'">
    <xsl:call-template name="process-chunk-element"/>
  </xsl:if>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="make.lots">
  <xsl:param name="toc.params" select="''"/>
  <xsl:param name="toc"/>

  <xsl:variable name="lots">
    <xsl:if test="contains($toc.params, 'toc')">
      <xsl:copy-of select="$toc"/>
    </xsl:if>

    <xsl:if test="contains($toc.params, 'figure')">
      <xsl:choose>
        <xsl:when test="$chunk.separate.lots != '0'">
          <xsl:call-template name="make.lot.chunk">
            <xsl:with-param name="type" select="'figure'"/>
            <xsl:with-param name="lot">
              <xsl:call-template name="list.of.titles">
                <xsl:with-param name="titles" select="'figure'"/>
                <xsl:with-param name="nodes" select=".//d:figure"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="list.of.titles">
            <xsl:with-param name="titles" select="'figure'"/>
            <xsl:with-param name="nodes" select=".//d:figure"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:if test="contains($toc.params, 'table')">
      <xsl:choose>
        <xsl:when test="$chunk.separate.lots != '0'">
          <xsl:call-template name="make.lot.chunk">
            <xsl:with-param name="type" select="'table'"/>
            <xsl:with-param name="lot">
              <xsl:call-template name="list.of.titles">
                <xsl:with-param name="titles" select="'table'"/>
                <xsl:with-param name="nodes" select=".//d:table"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="list.of.titles">
            <xsl:with-param name="titles" select="'table'"/>
            <xsl:with-param name="nodes" select=".//d:table"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:if test="contains($toc.params, 'example')">
      <xsl:choose>
        <xsl:when test="$chunk.separate.lots != '0'">
          <xsl:call-template name="make.lot.chunk">
            <xsl:with-param name="type" select="'example'"/>
            <xsl:with-param name="lot">
              <xsl:call-template name="list.of.titles">
                <xsl:with-param name="titles" select="'example'"/>
                <xsl:with-param name="nodes" select=".//d:example"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="list.of.titles">
            <xsl:with-param name="titles" select="'example'"/>
            <xsl:with-param name="nodes" select=".//d:example"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:if test="contains($toc.params, 'equation')">
      <xsl:choose>
        <xsl:when test="$chunk.separate.lots != '0'">
          <xsl:call-template name="make.lot.chunk">
            <xsl:with-param name="type" select="'equation'"/>
            <xsl:with-param name="lot">
              <xsl:call-template name="list.of.titles">
                <xsl:with-param name="titles" select="'equation'"/>
                <xsl:with-param name="nodes" select=".//d:equation"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="list.of.titles">
            <xsl:with-param name="titles" select="'equation'"/>
            <xsl:with-param name="nodes" select=".//d:equation"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:if test="contains($toc.params, 'procedure')">
      <xsl:choose>
        <xsl:when test="$chunk.separate.lots != '0'">
          <xsl:call-template name="make.lot.chunk">
            <xsl:with-param name="type" select="'procedure'"/>
            <xsl:with-param name="lot">
              <xsl:call-template name="list.of.titles">
                <xsl:with-param name="titles" select="'procedure'"/>
                <xsl:with-param name="nodes" select=".//d:procedure[d:title]"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="list.of.titles">
            <xsl:with-param name="titles" select="'procedure'"/>
            <xsl:with-param name="nodes" select=".//d:procedure[d:title]"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:variable>

  <xsl:if test="string($lots) != ''">
    <xsl:choose>
      <xsl:when test="$chunk.tocs.and.lots != 0 and not(parent::*)">
        <xsl:call-template name="write.chunk">
          <xsl:with-param name="filename">
            <xsl:call-template name="make-relative-filename">
              <xsl:with-param name="base.dir" select="$base.dir"/>
              <xsl:with-param name="base.name">
                <xsl:call-template name="dbhtml-dir"/>
                <xsl:apply-templates select="." mode="recursive-chunk-filename">
                  <xsl:with-param name="recursive" select="true()"/>
                </xsl:apply-templates>
                <xsl:text>-toc</xsl:text>
                <xsl:value-of select="$html.ext"/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:with-param>
          <xsl:with-param name="content">
            <xsl:call-template name="chunk-element-content">
              <xsl:with-param name="prev" select="/d:foo"/>
              <xsl:with-param name="next" select="/d:foo"/>
              <xsl:with-param name="nav.context" select="'toc'"/>
              <xsl:with-param name="content">
                <xsl:if test="$chunk.tocs.and.lots.has.title != 0">
                  <h1>
                    <xsl:apply-templates select="." mode="object.title.markup"/>
                  </h1>
                </xsl:if>
                <xsl:copy-of select="$lots"/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:with-param>
          <xsl:with-param name="quiet" select="$chunk.quietly"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="$lots"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:template>

<xsl:template name="make.lot.chunk">
  <xsl:param name="type" select="''"/>
  <xsl:param name="lot"/>

  <xsl:if test="string($lot) != ''">
    <xsl:variable name="filename">
      <xsl:call-template name="make-relative-filename">
        <xsl:with-param name="base.dir" select="$base.dir"/>
        <xsl:with-param name="base.name">
          <xsl:call-template name="dbhtml-dir"/>
          <xsl:value-of select="$type"/>
          <xsl:text>-toc</xsl:text>
          <xsl:value-of select="$html.ext"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="href">
      <xsl:call-template name="make-relative-filename">
        <xsl:with-param name="base.name">
          <xsl:call-template name="dbhtml-dir"/>
          <xsl:value-of select="$type"/>
          <xsl:text>-toc</xsl:text>
          <xsl:value-of select="$html.ext"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsl:call-template name="write.chunk">
      <xsl:with-param name="filename" select="$filename"/>
      <xsl:with-param name="content">
        <xsl:call-template name="chunk-element-content">
          <xsl:with-param name="prev" select="/d:foo"/>
          <xsl:with-param name="next" select="/d:foo"/>
          <xsl:with-param name="nav.context" select="'toc'"/>
          <xsl:with-param name="content">
            <xsl:copy-of select="$lot"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="quiet" select="$chunk.quietly"/>
    </xsl:call-template>
    <!-- And output a link to this file -->
    <div>
      <xsl:attribute name="class">
        <xsl:text>ListofTitles</xsl:text>
      </xsl:attribute>
      <a href="{$href}">
        <xsl:call-template name="gentext">
          <xsl:with-param name="key">
            <xsl:choose>
              <xsl:when test="$type='table'">ListofTables</xsl:when>
              <xsl:when test="$type='figure'">ListofFigures</xsl:when>
              <xsl:when test="$type='equation'">ListofEquations</xsl:when>
              <xsl:when test="$type='example'">ListofExamples</xsl:when>
              <xsl:when test="$type='procedure'">ListofProcedures</xsl:when>
              <xsl:otherwise>ListofUnknown</xsl:otherwise>
            </xsl:choose>
          </xsl:with-param>
        </xsl:call-template>
      </a>
    </div>
  </xsl:if>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="in.other.chunk">
  <xsl:param name="chunk" select="."/>
  <xsl:param name="node" select="."/>

  <xsl:variable name="is.chunk">
    <xsl:call-template name="chunk">
      <xsl:with-param name="node" select="$node"/>
    </xsl:call-template>
  </xsl:variable>

<!--
  <xsl:message>
    <xsl:text>in.other.chunk: </xsl:text>
    <xsl:value-of select="name($chunk)"/>
    <xsl:text> </xsl:text>
    <xsl:value-of select="name($node)"/>
    <xsl:text> </xsl:text>
    <xsl:value-of select="$chunk = $node"/>
    <xsl:text> </xsl:text>
    <xsl:value-of select="$is.chunk"/>
  </xsl:message>
-->

  <xsl:choose>
    <xsl:when test="$chunk = $node">0</xsl:when>
    <xsl:when test="$is.chunk = 1">1</xsl:when>
    <xsl:when test="count($node) = 0">0</xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="in.other.chunk">
        <xsl:with-param name="chunk" select="$chunk"/>
        <xsl:with-param name="node" select="$node/parent::*"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="count.footnotes.in.this.chunk">
  <xsl:param name="node" select="."/>
  <xsl:param name="footnotes" select="$node//d:footnote"/>
  <xsl:param name="count" select="0"/>

<!--
  <xsl:message>
    <xsl:text>count.footnotes.in.this.chunk: </xsl:text>
    <xsl:value-of select="name($node)"/>
  </xsl:message>
-->

  <xsl:variable name="in.other.chunk">
    <xsl:call-template name="in.other.chunk">
      <xsl:with-param name="chunk" select="$node"/>
      <xsl:with-param name="node" select="$footnotes[1]"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="count($footnotes) = 0">
      <xsl:value-of select="$count"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:choose>
        <xsl:when test="$in.other.chunk != 0">
          <xsl:call-template name="count.footnotes.in.this.chunk">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="footnotes"
                            select="$footnotes[position() &gt; 1]"/>
            <xsl:with-param name="count" select="$count"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="$footnotes[1]/ancestor::d:table
                        |$footnotes[1]/ancestor::d:informaltable">
          <xsl:call-template name="count.footnotes.in.this.chunk">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="footnotes"
                            select="$footnotes[position() &gt; 1]"/>
            <xsl:with-param name="count" select="$count"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="count.footnotes.in.this.chunk">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="footnotes"
                            select="$footnotes[position() &gt; 1]"/>
            <xsl:with-param name="count" select="$count + 1"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="process.footnotes.in.this.chunk">
  <xsl:param name="node" select="."/>
  <xsl:param name="footnotes" select="$node//d:footnote"/>

<!--
  <xsl:message>process.footnotes.in.this.chunk</xsl:message>
-->

  <xsl:variable name="in.other.chunk">
    <xsl:call-template name="in.other.chunk">
      <xsl:with-param name="chunk" select="$node"/>
      <xsl:with-param name="node" select="$footnotes[1]"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="count($footnotes) = 0">
      <!-- nop -->
    </xsl:when>
    <xsl:otherwise>
      <xsl:choose>
        <xsl:when test="$in.other.chunk != 0">
          <xsl:call-template name="process.footnotes.in.this.chunk">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="footnotes"
                            select="$footnotes[position() &gt; 1]"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="$footnotes[1]/ancestor::d:table
                        |$footnotes[1]/ancestor::d:informaltable">
          <xsl:call-template name="process.footnotes.in.this.chunk">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="footnotes"
                            select="$footnotes[position() &gt; 1]"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="$footnotes[1]"
                               mode="process.footnote.mode"/>
          <xsl:call-template name="process.footnotes.in.this.chunk">
            <xsl:with-param name="node" select="$node"/>
            <xsl:with-param name="footnotes"
                            select="$footnotes[position() &gt; 1]"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="process.footnotes">
  <xsl:variable name="footnotes" select=".//d:footnote"/>
  <xsl:variable name="fcount">
    <xsl:call-template name="count.footnotes.in.this.chunk">
      <xsl:with-param name="node" select="."/>
      <xsl:with-param name="footnotes" select="$footnotes"/>
    </xsl:call-template>
  </xsl:variable>

<!--
  <xsl:message>
    <xsl:value-of select="name(.)"/>
    <xsl:text> fcount: </xsl:text>
    <xsl:value-of select="$fcount"/>
  </xsl:message>
-->

  <!-- Only bother to do this if there's at least one non-table footnote -->
  <xsl:if test="$fcount &gt; 0">
    <div class="footnotes">
      <br/>
      <hr width="100" align="left"/>
      <xsl:call-template name="process.footnotes.in.this.chunk">
        <xsl:with-param name="node" select="."/>
        <xsl:with-param name="footnotes" select="$footnotes"/>
      </xsl:call-template>
    </div>
  </xsl:if>

  <!-- FIXME: When chunking, only the annotations actually used
              in this chunk should be referenced. I don't think it
              does any harm to reference them all, but it adds
              unnecessary bloat to each chunk. -->
  <xsl:if test="$annotation.support != 0 and //d:annotation">
    <div class="annotation-list">
      <div class="annotation-nocss">
        <p>The following annotations are from this essay. You are seeing
        them here because your browser doesn’t support the user-interface
        techniques used to make them appear as ‘popups’ on modern browsers.</p>
      </div>

      <xsl:apply-templates select="//d:annotation"
                           mode="annotation-popup"/>
    </div>
  </xsl:if>
</xsl:template>

<xsl:template name="process.chunk.footnotes">
  <xsl:variable name="is.chunk">
    <xsl:call-template name="chunk"/>
  </xsl:variable>
  <xsl:if test="$is.chunk = 1">
    <xsl:call-template name="process.footnotes"/>
  </xsl:if>
</xsl:template>

<!-- ====================================================================== -->

<!-- Resolve xml:base attributes -->
<xsl:template match="@fileref">
  <!-- need a check for absolute urls -->
  <xsl:choose>
    <xsl:when test="contains(., ':')">
      <!-- it has a uri scheme so it is an absolute uri -->
      <xsl:value-of select="."/>
    </xsl:when>
    <xsl:when test="$keep.relative.image.uris != 0">
      <!-- leave it alone -->
      <xsl:value-of select="."/>
    </xsl:when>
    <xsl:otherwise>
      <!-- its a relative uri -->
      <xsl:call-template name="relative-uri">
        <xsl:with-param name="destdir">
          <xsl:call-template name="dbhtml-dir">
            <xsl:with-param name="context" select=".."/>
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
