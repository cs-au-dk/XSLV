<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="/">
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><title>whatever</title></head>
      <body>
<p align="center"><b><font size="5" color="#800000">Affordable Supplies</font></b></p>
<p align="center"><font color="#00FF00"><b>Your reliable source for Printing Supplies</b></font></p>
<p align="center">
<xsl:value-of select="/Catalog/Supplier/Address/text()"/>
</p>
<div align="center">
  <center>
  <table border="1" cellpadding="1" cellspacing="1">
    <tr>
      <td>Supplier PartID</td>
      <td>Description</td>
      <td>Manufacturer</td>
      <td>Man. Part ID</td>
      <td>Unit</td>
      <td>Price</td>
      <td>Currency</td>
      <td>Picture</td>
    </tr>
          <xsl:for-each select="/Catalog/Index/IndexItem">
            <tr>
      <td><xsl:value-of select="ItemID/SupplierPartID"/></td>
      <td><xsl:value-of select="ItemDetail/Description"/></td>
      <td><xsl:value-of select="ItemDetail/ManufacturerName"/></td>
      <td><xsl:value-of select="ItemDetail/ManufacturerPartID"/></td>
      <td><xsl:value-of select="ItemDetail/UnitOfMeasure"/></td>
      <td><xsl:value-of select="UnitPrice/Money"/></td>
      <td><xsl:value-of select="UnitPrice/Money/@currency"/></td>
      <td><img src="{ItemDetail/Description[2]}" width="100"
      height="100" alt="bla"/></td>
            </tr>
          </xsl:for-each>
        </table>
</center>
</div>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
