<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

<!-- Incomplete transformation for converting an HTML output XSLT
     transformation to outputting XHTML. Not yet implemented is
     downcase-ing enumerated attribute values. This is a tedious task
     to implement in an XSLT transformation. -->

<xsl:output method="xml"/>
<xsl:preserve-space elements="*"/>



<!-- XSLT content: -->
<xsl:template match="xsl:output" priority="11">
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:attribute name="method">xml</xsl:attribute>
    <xsl:attribute name="doctype-public">-//W3C//DTD XHTML 1.0 Transitional//EN</xsl:attribute>
    <xsl:attribute name="doctype-system">http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd</xsl:attribute>
  </xsl:copy>
</xsl:template>


<xsl:template match="xsl:element" priority="11">
  <!-- make sure literal xsl:element declarations propagate the right namespace -->
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:attribute name="name">
      <xsl:value-of select="translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/>
    </xsl:attribute>
    <xsl:attribute name="namespace">http://www.w3.org/1999/xhtml</xsl:attribute>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

<xsl:template match="xsl:attribute" priority="11">
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:attribute name="name">
      <xsl:value-of select="translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/>
    </xsl:attribute>
    
    <xsl:if test="translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz') = 'name'">
      <xsl:attribute name="name">id</xsl:attribute>
    </xsl:if>

    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>




<xsl:template match="xsl:*" priority="10">
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>



<!-- HTML content:-->
<xsl:template match="*">
  <xsl:element
  name="{translate(local-name(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')}" namespace="http://www.w3.org/1999/xhtml">
    <xsl:apply-templates select="child::node()|@*"/>
  </xsl:element>
</xsl:template>

<xsl:template match="@*">
  <xsl:choose>
    <xsl:when test="translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz') != 'name'">
      <xsl:attribute name="{translate(local-name(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')}">
        <xsl:value-of select="."/>
      </xsl:attribute>
    </xsl:when>
  
    <xsl:otherwise>
      <xsl:attribute name="id">
        <xsl:value-of select="."/>
      </xsl:attribute>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>






<!-- TODO:
ENUMERATED ATTRIB: button.type:  [button, submit, reset]
ENUMERATED ATTRIB: button.disabled:  [disabled]
ENUMERATED ATTRIB: button.dir:  [ltr, rtl]
ENUMERATED ATTRIB: textarea.disabled:  [disabled]
ENUMERATED ATTRIB: textarea.readonly:  [readonly]
ENUMERATED ATTRIB: textarea.dir:  [ltr, rtl]
ENUMERATED ATTRIB: em.dir:  [ltr, rtl]
ENUMERATED ATTRIB: small.dir:  [ltr, rtl]
ENUMERATED ATTRIB: area.shape:  [rect, circle, poly, default]
ENUMERATED ATTRIB: area.dir:  [ltr, rtl]
ENUMERATED ATTRIB: area.nohref:  [nohref]
ENUMERATED ATTRIB: noframes.dir:  [ltr, rtl]
ENUMERATED ATTRIB: bdo.dir:  [ltr, rtl]
ENUMERATED ATTRIB: form.method:  [get, post]
ENUMERATED ATTRIB: form.dir:  [ltr, rtl]
ENUMERATED ATTRIB: link.dir:  [ltr, rtl]
ENUMERATED ATTRIB: label.dir:  [ltr, rtl]
ENUMERATED ATTRIB: dt.dir:  [ltr, rtl]
ENUMERATED ATTRIB: span.dir:  [ltr, rtl]
ENUMERATED ATTRIB: isindex.dir:  [ltr, rtl]
ENUMERATED ATTRIB: title.dir:  [ltr, rtl]
ENUMERATED ATTRIB: strong.dir:  [ltr, rtl]
ENUMERATED ATTRIB: script.defer:  [defer]
ENUMERATED ATTRIB: script.xml:space:  [preserve]
ENUMERATED ATTRIB: div.dir:  [ltr, rtl]
ENUMERATED ATTRIB: div.align:  [left, center, right, justify]
ENUMERATED ATTRIB: dl.compact:  [compact]
ENUMERATED ATTRIB: dl.dir:  [ltr, rtl]
ENUMERATED ATTRIB: blockquote.dir:  [ltr, rtl]
ENUMERATED ATTRIB: kbd.dir:  [ltr, rtl]
ENUMERATED ATTRIB: menu.compact:  [compact]
ENUMERATED ATTRIB: menu.dir:  [ltr, rtl]
ENUMERATED ATTRIB: body.dir:  [ltr, rtl]
ENUMERATED ATTRIB: dir.compact:  [compact]
ENUMERATED ATTRIB: dir.dir:  [ltr, rtl]
ENUMERATED ATTRIB: ins.dir:  [ltr, rtl]
ENUMERATED ATTRIB: map.dir:  [ltr, rtl]
ENUMERATED ATTRIB: dd.dir:  [ltr, rtl]
ENUMERATED ATTRIB: fieldset.dir:  [ltr, rtl]
ENUMERATED ATTRIB: head.dir:  [ltr, rtl]
ENUMERATED ATTRIB: col.valign:  [top, middle, bottom, baseline]
ENUMERATED ATTRIB: col.dir:  [ltr, rtl]
ENUMERATED ATTRIB: col.align:  [left, center, right, justify, char]
ENUMERATED ATTRIB: big.dir:  [ltr, rtl]
ENUMERATED ATTRIB: meta.dir:  [ltr, rtl]
ENUMERATED ATTRIB: code.dir:  [ltr, rtl]
ENUMERATED ATTRIB: tbody.valign:  [top, middle, bottom, baseline]
ENUMERATED ATTRIB: tbody.dir:  [ltr, rtl]
ENUMERATED ATTRIB: tbody.align:  [left, center, right, justify, char]
ENUMERATED ATTRIB: option.disabled:  [disabled]
ENUMERATED ATTRIB: option.selected:  [selected]
ENUMERATED ATTRIB: option.dir:  [ltr, rtl]
ENUMERATED ATTRIB: u.dir:  [ltr, rtl]
ENUMERATED ATTRIB: s.dir:  [ltr, rtl]
ENUMERATED ATTRIB: q.dir:  [ltr, rtl]
ENUMERATED ATTRIB: p.dir:  [ltr, rtl]
ENUMERATED ATTRIB: p.align:  [left, center, right, justify]
ENUMERATED ATTRIB: thead.valign:  [top, middle, bottom, baseline]
ENUMERATED ATTRIB: thead.dir:  [ltr, rtl]
ENUMERATED ATTRIB: thead.align:  [left, center, right, justify, char]
ENUMERATED ATTRIB: ol.compact:  [compact]
ENUMERATED ATTRIB: ol.dir:  [ltr, rtl]
ENUMERATED ATTRIB: ul.type:  [disc, square, circle]
ENUMERATED ATTRIB: ul.compact:  [compact]
ENUMERATED ATTRIB: ul.dir:  [ltr, rtl]
ENUMERATED ATTRIB: i.dir:  [ltr, rtl]
ENUMERATED ATTRIB: pre.dir:  [ltr, rtl]
ENUMERATED ATTRIB: pre.xml:space:  [preserve]
ENUMERATED ATTRIB: optgroup.disabled:  [disabled]
ENUMERATED ATTRIB: optgroup.dir:  [ltr, rtl]
ENUMERATED ATTRIB: img.dir:  [ltr, rtl]
ENUMERATED ATTRIB: img.align:  [top, middle, bottom, left, right]
ENUMERATED ATTRIB: img.ismap:  [ismap]
ENUMERATED ATTRIB: caption.dir:  [ltr, rtl]
ENUMERATED ATTRIB: caption.align:  [top, bottom, left, right]
ENUMERATED ATTRIB: b.dir:  [ltr, rtl]
ENUMERATED ATTRIB: a.shape:  [rect, circle, poly, default]
ENUMERATED ATTRIB: a.dir:  [ltr, rtl]
ENUMERATED ATTRIB: br.clear:  [left, all, right, none]
ENUMERATED ATTRIB: style.dir:  [ltr, rtl]
ENUMERATED ATTRIB: style.xml:space:  [preserve]
ENUMERATED ATTRIB: hr.noshade:  [noshade]
ENUMERATED ATTRIB: hr.dir:  [ltr, rtl]
ENUMERATED ATTRIB: hr.align:  [left, center, right]
ENUMERATED ATTRIB: param.valuetype:  [data, ref, object]
ENUMERATED ATTRIB: table.rules:  [none, groups, rows, cols, all]
ENUMERATED ATTRIB: table.frame:  [void, above, below, hsides, lhs, rhs, vsides, box, border]
ENUMERATED ATTRIB: table.dir:  [ltr, rtl]
ENUMERATED ATTRIB: table.align:  [left, center, right]
ENUMERATED ATTRIB: applet.align:  [top, middle, bottom, left, right]
ENUMERATED ATTRIB: tt.dir:  [ltr, rtl]
ENUMERATED ATTRIB: tr.valign:  [top, middle, bottom, baseline]
ENUMERATED ATTRIB: tr.dir:  [ltr, rtl]
ENUMERATED ATTRIB: tr.align:  [left, center, right, justify, char]
ENUMERATED ATTRIB: th.scope:  [row, col, rowgroup, colgroup]
ENUMERATED ATTRIB: th.valign:  [top, middle, bottom, baseline]
ENUMERATED ATTRIB: th.nowrap:  [nowrap]
ENUMERATED ATTRIB: th.dir:  [ltr, rtl]
ENUMERATED ATTRIB: th.align:  [left, center, right, justify, char]
ENUMERATED ATTRIB: center.dir:  [ltr, rtl]
ENUMERATED ATTRIB: td.scope:  [row, col, rowgroup, colgroup]
ENUMERATED ATTRIB: td.valign:  [top, middle, bottom, baseline]
ENUMERATED ATTRIB: td.nowrap:  [nowrap]
ENUMERATED ATTRIB: td.dir:  [ltr, rtl]
ENUMERATED ATTRIB: td.align:  [left, center, right, justify, char]
ENUMERATED ATTRIB: samp.dir:  [ltr, rtl]
ENUMERATED ATTRIB: tfoot.valign:  [top, middle, bottom, baseline]
ENUMERATED ATTRIB: tfoot.dir:  [ltr, rtl]
ENUMERATED ATTRIB: tfoot.align:  [left, center, right, justify, char]
ENUMERATED ATTRIB: font.dir:  [ltr, rtl]
ENUMERATED ATTRIB: dfn.dir:  [ltr, rtl]
ENUMERATED ATTRIB: noscript.dir:  [ltr, rtl]
ENUMERATED ATTRIB: colgroup.valign:  [top, middle, bottom, baseline]
ENUMERATED ATTRIB: colgroup.dir:  [ltr, rtl]
ENUMERATED ATTRIB: colgroup.align:  [left, center, right, justify, char]
ENUMERATED ATTRIB: object.dir:  [ltr, rtl]
ENUMERATED ATTRIB: object.align:  [top, middle, bottom, left, right]
ENUMERATED ATTRIB: object.declare:  [declare]
ENUMERATED ATTRIB: sup.dir:  [ltr, rtl]
ENUMERATED ATTRIB: html.dir:  [ltr, rtl]
ENUMERATED ATTRIB: h6.dir:  [ltr, rtl]
ENUMERATED ATTRIB: h6.align:  [left, center, right, justify]
ENUMERATED ATTRIB: h5.dir:  [ltr, rtl]
ENUMERATED ATTRIB: h5.align:  [left, center, right, justify]
ENUMERATED ATTRIB: h4.dir:  [ltr, rtl]
ENUMERATED ATTRIB: h4.align:  [left, center, right, justify]
ENUMERATED ATTRIB: h3.dir:  [ltr, rtl]
ENUMERATED ATTRIB: h3.align:  [left, center, right, justify]
ENUMERATED ATTRIB: h2.dir:  [ltr, rtl]
ENUMERATED ATTRIB: h2.align:  [left, center, right, justify]
ENUMERATED ATTRIB: h1.dir:  [ltr, rtl]
ENUMERATED ATTRIB: h1.align:  [left, center, right, justify]
ENUMERATED ATTRIB: iframe.scrolling:  [yes, no, auto]
ENUMERATED ATTRIB: iframe.align:  [top, middle, bottom, left, right]
ENUMERATED ATTRIB: iframe.frameborder:  [1, 0]
ENUMERATED ATTRIB: strike.dir:  [ltr, rtl]
ENUMERATED ATTRIB: sub.dir:  [ltr, rtl]
ENUMERATED ATTRIB: acronym.dir:  [ltr, rtl]
ENUMERATED ATTRIB: select.disabled:  [disabled]
ENUMERATED ATTRIB: select.dir:  [ltr, rtl]
ENUMERATED ATTRIB: select.multiple:  [multiple]
ENUMERATED ATTRIB: del.dir:  [ltr, rtl]
ENUMERATED ATTRIB: li.dir:  [ltr, rtl]
ENUMERATED ATTRIB: cite.dir:  [ltr, rtl]
ENUMERATED ATTRIB: var.dir:  [ltr, rtl]
ENUMERATED ATTRIB: legend.dir:  [ltr, rtl]
ENUMERATED ATTRIB: legend.align:  [top, bottom, left, right]
ENUMERATED ATTRIB: abbr.dir:  [ltr, rtl]
ENUMERATED ATTRIB: input.readonly:  [readonly]
ENUMERATED ATTRIB: input.checked:  [checked]
ENUMERATED ATTRIB: input.disabled:  [disabled]
ENUMERATED ATTRIB: input.align:  [top, middle, bottom, left, right]
ENUMERATED ATTRIB: input.dir:  [ltr, rtl]
ENUMERATED ATTRIB: input.type:  [text, password, checkbox, radio, submit, reset, file, hidden, image, button]
ENUMERATED ATTRIB: address.dir:  [ltr, rtl]
-->



<!-- Other content:-->
<xsl:template match="comment()|processing-instruction()|text()">
  <xsl:copy/>
</xsl:template>

</xsl:stylesheet>
