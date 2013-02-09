<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:msxsl="urn:schemas-microsoft-com:xslt" version="1.0" xmlns="http://www.w3.org/1999/xhtml">
  <!-- Matches the root node. -->
<xsl:include href="Ontolica.searchdialog.xsl"/>

<xsl:variable name="foo" select="bar/baz"/>

  <xsl:template match="/"> 
<html>
<head><title>This is just a dummy stylesheet to get the real searchdialog started .. alternatively, we could have chosen a different output root element name than html</title></head>
<body>
<xsl:apply-templates/>
</body>
</html>
  </xsl:template>                  
</xsl:stylesheet>
