<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">
  
  <!-- Try to make the output look half decent -->
  <xsl:output indent="yes"/>
  
  <xsl:template match="Order">
    <fo:root>

      <fo:layout-master-set>
        <fo:simple-page-master master-name="only">
          <fo:region-body margin-left="0.5in" 
                          margin-top="0.5in"/>
        </fo:simple-page-master>
      </fo:layout-master-set>

      <fo:page-sequence master-reference="only">

        <fo:flow flow-name="xsl-region-body">
          <xsl:apply-templates select="Customer"/>
          <xsl:apply-templates select="ShipTo"/>
          <fo:table font-size="12pt" 
                    space-before="24pt" space-after="24pt">
            <fo:table-column column-width="2in"/>
            <fo:table-column column-width="1in"/>
            <fo:table-column column-width="1in"/>
            <fo:table-column column-width="1in"/>
            <fo:table-body>
              <fo:table-row font-weight="bold">
                <fo:table-cell>
                  <fo:block>Product</fo:block>
                </fo:table-cell>
                <fo:table-cell>
                  <fo:block>Quantity</fo:block>
                </fo:table-cell>
                <fo:table-cell>
                  <fo:block>Unit Price</fo:block>
                </fo:table-cell>
                <fo:table-cell>
                  <fo:block>Subtotal</fo:block>
                </fo:table-cell>
              </fo:table-row>
              <xsl:apply-templates select="Product"/>
            </fo:table-body>
          </fo:table>
          <xsl:apply-templates select="Tax"/>
          <xsl:apply-templates select="Shipping"/>
          <xsl:apply-templates select="Total"/>
        </fo:flow>

      </fo:page-sequence>

    </fo:root>  
  </xsl:template>
  
  <xsl:template match="Customer">
    <fo:block font-size="16pt" font-family="serif"
              line-height="20pt">
       Ship to:
    </fo:block>
    <fo:block font-size="16pt" font-family="serif" 
              margin-left="0.5in" line-height="20pt">
       <xsl:apply-templates/>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="ShipTo">
    <fo:block font-size="16pt" font-family="sans-serif"
              line-height="18pt" margin-top="20pt" 
              margin-left="0.5in">
      <xsl:apply-templates select="Street"/>
    </fo:block>
    <fo:block font-size="16pt" font-family="sans-serif"
              line-height="18pt" margin-left="0.5in">
      <xsl:apply-templates select="City"/>&#xA0;
      <xsl:apply-templates select="State"/>&#xA0;
      <xsl:apply-templates select="Zip"/>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="Product">
    <fo:table-row>
      <fo:table-cell>
        <fo:block><xsl:value-of select="Name"/></fo:block>
      </fo:table-cell>
      <fo:table-cell>
        <fo:block><xsl:value-of select="Quantity"/></fo:block>
      </fo:table-cell>
      <fo:table-cell>
        <fo:block>$<xsl:value-of select="Price"/></fo:block>
      </fo:table-cell>
      <fo:table-cell>
        <fo:block>
          $<xsl:value-of select="Price*Quantity"/>
        </fo:block>
      </fo:table-cell>
    </fo:table-row>
  </xsl:template>
  
  <xsl:template match="Tax|Shipping|Total">
    <fo:block font-size="16pt" font-family="serif"
              line-height="20pt">
      <xsl:value-of select="name()"/>: $<xsl:apply-templates/>
    </fo:block>
  </xsl:template>
  
  <!-- want to leave this one out of the output -->
  <xsl:template match="SKU"/>

</xsl:stylesheet>
