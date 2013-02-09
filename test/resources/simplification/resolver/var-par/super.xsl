<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- should be plain visible from importing module -->
  <xsl:variable name="s" select="$e"/>
  
  <!-- should be overridden by importing module -->
  <xsl:variable name="t" select="'declared-in-import'"/>
  
  <!-- make binding of same name to different other variables in principal 
       and imported module, see that the right guy is rebound -->
  <xsl:variable name="x" select="$y"/>
  
</xsl:stylesheet>