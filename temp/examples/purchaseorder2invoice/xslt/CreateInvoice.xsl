<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="invoice">

  <xsl:template match="/">
     <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="PurchaseOrder">
    <Invoice>
     <Header>
      <xsl:apply-templates select="//OrderRequest/OrderRequestHeader"/>
      <xsl:apply-templates select="//Header"/>
      </Header>
      <xsl:apply-templates select="//ItemOut"/>
      <xsl:apply-templates select="//ShipTo"/>
    </Invoice>
  </xsl:template>
  
<xsl:template match="OrderRequestHeader">
<OrderNo>
 <xsl:value-of select="./@orderID"/>
</OrderNo>
<OrderDate>
 <xsl:value-of select="./@orderDate"/>
</OrderDate>
<OrderTotal>
<xsl:copy-of select="./Total/Money/@currency"/>
 <xsl:value-of select="./Total/Money/text()"/>
 </OrderTotal>
</xsl:template>

<!-- Generate Header-->
   <xsl:template match="Header">
    <Purchaser>
      <xsl:attribute name="name">
        <xsl:value-of select="./From/text()"/>
      </xsl:attribute>
    </Purchaser>
    <Seller>
      <xsl:attribute name="name">
        <xsl:value-of select="./To/text()"/>
      </xsl:attribute>
    </Seller>
  </xsl:template>

<!-- Generate Line Item-->
<xsl:template match="ItemOut">
<Entry>
   <Qty> <xsl:value-of select="@quantity"/> </Qty>
   <ItemDescription><xsl:value-of select="./ItemDetail/Description/text()"/></ItemDescription>
<UnitPrice>
<xsl:attribute name="currency">
<xsl:value-of select="./UnitPrice/Money/@currency"/>
</xsl:attribute>
<xsl:copy-of select="./UnitPrice/Money/text()"/></UnitPrice>
<xsl:element name="SubTotal">
<xsl:attribute name="currency">
<xsl:value-of select="./SubTotal/Money/@currency"/>
</xsl:attribute>
<xsl:copy-of select="./SubTotal/Money/text()"/>
</xsl:element>
</Entry>
</xsl:template>

<!-- Generate Shipping Address-->
<xsl:template match="ShipTo">
<ShippingAddress>
  <xsl:value-of select="./Address/text()"/>
</ShippingAddress>
</xsl:template>



</xsl:stylesheet>
