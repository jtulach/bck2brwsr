<?xml version="1.0" encoding="UTF-8"?>
<!--

    Back 2 Browser Bytecode Translator
    Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. Look for COPYING file in the top folder.
    If not, see http://opensource.org/licenses/GPL-2.0.

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text"/>

    <xsl:template match="/">
        <xsl:apply-templates select="testsuite/testcase"/>
    </xsl:template>
        
    
    <xsl:template match="testcase">
      <xsl:if test="contains(@name,'tenThousand')">
        <xsl:if test="not(contains(@name, '[Java]'))">
          <xsl:if test="not(contains(@name, '[Compare'))">
            <xsl:text>
</xsl:text>
            <xsl:value-of select="@name"/>
            <xsl:text>=</xsl:text>
            <xsl:value-of select="@time"/>
          </xsl:if>
        </xsl:if>
      </xsl:if>
    </xsl:template>

</xsl:stylesheet>
