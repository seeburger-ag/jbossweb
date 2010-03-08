<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Content Stylesheet for "jbossweb-docs" Documentation -->

<!-- $Id: style.xsl 4354 2006-05-22 17:53:20Z mladen.turk@jboss.com $ -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- Output method -->
  <xsl:output method="html" encoding="iso-8859-1" indent="no"/>

  <!-- Defined parameters (overrideable) -->
  <xsl:param    name="home-name"        select="'JBoss Inc.'"/>
  <xsl:param    name="home-href"        select="'http://www.jboss.com/'"/>
  <xsl:param    name="home-logo"        select="'/images/jbosslogo.gif'"/>
  <xsl:param    name="printer-logo"     select="'/images/printer.gif'"/>
  <xsl:param    name="relative-path"    select="'.'"/>
  <xsl:param    name="void-image"       select="'/images/void.gif'"/>
  <xsl:param    name="project-menu"     select="'menu'"/>
  <xsl:param    name="standalone"       select="''"/>
  <xsl:param    name="buglink"          select="'http://jira.jboss.com/jira/browse/JBWEB-'"/>
  <xsl:param    name="home-site"        select="'http://labs.jboss.com/portal/index.html?ctrl:id=page.default.info&amp;project=jbossweb'"/>
  <!-- Process an entire document into an HTML page -->
  <xsl:template match="document">
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">

    <xsl:comment>
        !!!  This file is generated from xml source: DO NOT EDIT !!!
    </xsl:comment>
    <head>
    <title><xsl:value-of select="project/title"/> - <xsl:value-of select="properties/title"/></title>
    <xsl:for-each select="properties/author">
      <xsl:variable name="name">
        <xsl:value-of select="."/>
      </xsl:variable>
      <xsl:variable name="email">
        <xsl:value-of select="@email"/>
      </xsl:variable>
      <meta name="author" value="{$name}"/>
      <meta name="email" value="{$email}"/>
    </xsl:for-each>
    <link href="{$relative-path}/style.css" type="text/css" rel="stylesheet"/>
    </head>
    <body>
    <table border="0" width="100%" cellspacing="4">
      <xsl:comment>PAGE HEADER</xsl:comment>
      <tr><td colspan="2">
        <xsl:comment>JBOSS LOGO</xsl:comment>
        <xsl:variable name="alt">
          <xsl:value-of select="$home-name"/>
        </xsl:variable>
        <xsl:variable name="href">
          <xsl:value-of select="$home-href"/>
        </xsl:variable>
        <xsl:variable name="src">
          <xsl:value-of select="$relative-path"/><xsl:value-of select="$home-logo"/>
        </xsl:variable>
        <a href="{$href}">
          <img src="{$src}" align="left" alt="{$alt}" border="0"/>
        </a>
        <xsl:if test="project/logo">
          <xsl:variable name="alt">
            <xsl:value-of select="project/logo"/>
          </xsl:variable>
          <xsl:variable name="home">
            <xsl:value-of select="project/@href"/>
          </xsl:variable>
          <xsl:variable name="src">
            <xsl:value-of select="$relative-path"/><xsl:value-of select="project/logo/@href"/>
          </xsl:variable>

          <xsl:comment>PROJECT LOGO</xsl:comment>
          <a href="{$home-site}">
            <img src="{$src}" align="right" alt="{$alt}" border="0"/>
          </a>
        </xsl:if>
      </td></tr>

      <xsl:comment>HEADER SEPARATOR</xsl:comment>
      <tr>
        <td colspan="2">
          <hr noshade="noshade" size="1"/>
        </td>
      </tr>

      <tr>

        <!-- Don't generate a menu if styling printer friendly docs -->
        <xsl:if test="$project-menu = 'menu'">
          <xsl:comment>LEFT SIDE NAVIGATION</xsl:comment>
          <td width="20%" valign="top" nowrap="true">
            <xsl:apply-templates select="project/body/menu"/>
          </td>
        </xsl:if>

        <xsl:comment>RIGHT SIDE MAIN BODY</xsl:comment>
        <td width="80%" valign="top" align="left">
          <table border="0" width="100%" cellspacing="4">
            <tr>
              <td align="left" valign="top">
                <h1><xsl:value-of select="project/title"/></h1>
                <h2><xsl:value-of select="properties/title"/></h2>
              </td>
              <td align="right" valign="top" nowrap="true">
                <!-- Add the printer friendly link for docs with a menu -->
                <xsl:if test="$project-menu = 'menu'">
                  <xsl:variable name="src">
                    <xsl:value-of select="$relative-path"/><xsl:value-of select="$printer-logo"/>
                  </xsl:variable>
                  <xsl:variable name="url">
                    <xsl:value-of select="/document/@url"/>
                  </xsl:variable>
                  <small>
                    <a href="printer/{$url}">
                      <img src="{$src}" border="0" alt="Printer Friendly Version"/>
                      <br />print-friendly<br />version
                    </a>
                  </small>
                </xsl:if>
                <xsl:if test="$project-menu != 'menu'">
                  <xsl:variable name="void">
                    <xsl:value-of select="$relative-path"/><xsl:value-of select="$void-image"/>
                    </xsl:variable>
                  <img src="{$void}" width="1" height="1" vspace="0" hspace="0" border="0"/>
                </xsl:if>
              </td>
            </tr>
          </table>
          <xsl:apply-templates select="body/section"/>
        </td>
      </tr>

      <xsl:comment>FOOTER SEPARATOR</xsl:comment>
      <tr>
        <td colspan="2">
          <hr noshade="noshade" size="1"/>
        </td>
      </tr>

      <xsl:comment>PAGE FOOTER</xsl:comment>
      <tr><td colspan="2">
        <div align="center"><font color="#525D76" size="-1"><em>
        Copyright &#169; 2005, JBoss Inc.
        </em></font></div>
      </td></tr>

    </table>
    </body>
    </html>

  </xsl:template>


  <!-- Process a menu for the navigation bar -->
  <xsl:template match="menu">
    <p><strong><xsl:value-of select="@name"/></strong></p>
    <ul>
      <xsl:apply-templates select="item"/>
    </ul>
  </xsl:template>


  <!-- Process a menu item for the navigation bar -->
  <xsl:template match="item">
    <xsl:variable name="href">
      <xsl:value-of select="@href"/>
    </xsl:variable>
    <li><a href="{$href}"><xsl:value-of select="@name"/></a></li>
  </xsl:template>

  <!-- Process a figure -->
  <xsl:template match="figure">
    <xsl:variable name="src">
      <xsl:value-of select="$relative-path"/><xsl:value-of select="@src"/>
    </xsl:variable>
    <xsl:variable name="name">
      <xsl:value-of select="@name"/>
    </xsl:variable>
    <table border="0" cellspacing="0" cellpadding="0">
    <tr><td><img src="{$src}" alt="{$name}" border="0"/></td></tr>
    <tr><td class="figure"><xsl:value-of select="@name"/>. <xsl:value-of select="@text"/></td></tr>
    </table>
  </xsl:template>

  <xsl:template match="image">
    <xsl:variable name="src">
      <xsl:value-of select="$relative-path"/><xsl:value-of select="@src"/>
    </xsl:variable>
    <xsl:variable name="alt">
      <xsl:value-of select="@alt"/>
    </xsl:variable>
    <img src="{$src}" alt="{$alt}" border="0"/>
  </xsl:template>

  <!-- Process a documentation section -->
  <xsl:template match="section">
    <xsl:variable name="name">
      <xsl:value-of select="@name"/>
    </xsl:variable>
    <table border="0" cellspacing="0" cellpadding="0" width="100%">
      <!-- Section heading -->
      <tr><td class="section" colspan="2">
          <a name="{$name}">
          <xsl:value-of select="@name"/></a>
      </td></tr>
      <!-- Section body -->
      <tr>
      <td width="20px"></td>
      <td><br />
        <xsl:apply-templates/>
      </td></tr>
    </table>
  </xsl:template>


  <!-- Process a documentation subsection -->
  <xsl:template match="subsection">
    <xsl:variable name="name">
      <xsl:value-of select="@name"/>
    </xsl:variable>
    <table border="0" cellspacing="0" cellpadding="2" width="100%">
      <!-- Subsection heading -->
      <tr><td class="subsection" colspan="2">
          <a name="{$name}">
          <xsl:value-of select="@name"/></a>
      </td></tr>
      <!-- Subsection body -->
      <tr>
      <td width="20px"></td>
      <td><br />
        <xsl:apply-templates/>
      </td></tr>
    </table>
    <br/>
  </xsl:template>


  <!-- Process an attributes list with nested attribute elements -->
  <xsl:template match="attributes">
    <table border="1" cellpadding="5" width="100%">
      <tr>
        <th width="15%" class="directive">
          <xsl:choose>
            <xsl:when test="@name != ''">
               <xsl:value-of select="@name"/>
            </xsl:when>
            <xsl:otherwise>
               Attribute
            </xsl:otherwise>
          </xsl:choose>
        </th>
        <th width="85%" class="directive">
          <xsl:choose>
            <xsl:when test="@value != ''">
               <xsl:value-of select="@value"/>
            </xsl:when>
            <xsl:otherwise>
               Description
            </xsl:otherwise>
          </xsl:choose>
        </th>
      </tr>
      <xsl:for-each select="attribute">
        <tr>
          <td align="left" valign="center">
            <xsl:if test="@required = 'true'">
              <strong><code><xsl:value-of select="@name"/></code></strong>
            </xsl:if>
            <xsl:if test="@required != 'true'">
              <code><xsl:value-of select="@name"/></code>
            </xsl:if>
          </td>
          <td align="left" valign="center">
            <xsl:apply-templates/>
          </td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <!-- Process an directives list with nested directive elements -->
  <xsl:template match="directives">
    <table border="1" cellpadding="5" width="100%">
      <tr>
        <th width="15%" class="directive">
          <xsl:choose>
            <xsl:when test="@name != ''">
               <xsl:value-of select="@name"/>
            </xsl:when>
            <xsl:otherwise>
               Directive
            </xsl:otherwise>
          </xsl:choose>
        </th>
        <th width="10%" class="directive">
          <xsl:choose>
            <xsl:when test="@default != ''">
               <xsl:value-of select="@default"/>
            </xsl:when>
            <xsl:otherwise>
               Default
            </xsl:otherwise>
          </xsl:choose>
        </th>
        <th width="75%" class="directive">
          <xsl:choose>
            <xsl:when test="@description != ''">
               <xsl:value-of select="@description"/>
            </xsl:when>
            <xsl:otherwise>
               Description
            </xsl:otherwise>
          </xsl:choose>
        </th>
      </tr>
      <xsl:for-each select="directive">
        <tr>
          <td align="left" valign="center">
            <xsl:if test="@required = 'true'">
              <strong><code><xsl:value-of select="@name"/></code></strong>
            </xsl:if>
            <xsl:if test="@required != 'true'">
              <code><xsl:value-of select="@name"/></code>
            </xsl:if>
          </td>
          <xsl:choose>
            <xsl:when test="@default != ''">
               <td align="center" valign="center">
               <code><xsl:value-of select="@default"/></code>
              </td>
            </xsl:when>
            <xsl:otherwise>
              <td align="center" valign="center">
              <code>-</code>
              </td>
            </xsl:otherwise>
          </xsl:choose>
          <td align="left" valign="center">
            <xsl:apply-templates/>
          </td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <!-- Fix relative links in printer friendly versions of the docs -->
  <xsl:template match="a">
    <xsl:variable name="href" select="@href"/>
    <xsl:choose>
      <xsl:when test="$standalone = 'standalone'">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="$project-menu != 'menu' and starts-with(@href,'../')">
        <a href="../{$href}"><xsl:apply-templates/></a>
      </xsl:when>
      <xsl:when test="$project-menu != 'menu' and starts-with(@href,'./') and contains(substring(@href,3),'/')">
        <a href=".{$href}"><xsl:apply-templates/></a>
      </xsl:when>
      <xsl:when test="$project-menu != 'menu' and not(contains(@href,'//')) and not(starts-with(@href,'/')) and not(starts-with(@href,'#')) and contains(@href,'/')">
        <a href="../{$href}"><xsl:apply-templates/></a>
      </xsl:when>
      <xsl:when test="$href != ''">
        <a href="{$href}"><xsl:apply-templates/></a>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="name" select="@name"/>
        <a name="{$name}"><xsl:apply-templates/></a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Warning -->
  <xsl:template match="warn">
    <p>
    <div class="warn">
    <xsl:apply-templates/>
    </div>
    </p>
  </xsl:template>
  <xsl:template match="home">
    <a href="{$home-site}">
    <xsl:apply-templates/>
    </a>
  </xsl:template>

  <!-- Changelog related tags -->
  <xsl:template match="changelog">
    <table border="0" cellpadding="2" cellspacing="2">
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="changelog/add">
    <tr>
      <xsl:variable name="src"><xsl:value-of select="$relative-path"/>/images/add.gif</xsl:variable>
      <td valign="top"><img alt="add" class="icon" src="{$src}"/></td>
      <td><xsl:apply-templates/></td>
    </tr>
  </xsl:template>

  <xsl:template match="changelog/update">
    <tr>
      <xsl:variable name="src"><xsl:value-of select="$relative-path"/>/images/update.gif</xsl:variable>
      <td valign="top"><img alt="update" class="icon" src="{$src}"/></td>
      <td><xsl:apply-templates/></td>
    </tr>
  </xsl:template>

  <xsl:template match="changelog/design">
    <tr>
      <xsl:variable name="src"><xsl:value-of select="$relative-path"/>/images/design.gif</xsl:variable>
      <td valign="top"><img alt="design" class="icon" src="{$src}"/></td>
      <td><xsl:apply-templates/></td>
    </tr>
  </xsl:template>

  <xsl:template match="changelog/docs">
    <tr>
      <xsl:variable name="src"><xsl:value-of select="$relative-path"/>/images/docs.gif</xsl:variable>
      <td valign="top"><img alt="docs" class="icon" src="{$src}"/></td>
      <td><xsl:apply-templates/></td>
    </tr>
  </xsl:template>

  <xsl:template match="changelog/fix">
    <tr>
      <xsl:variable name="src"><xsl:value-of select="$relative-path"/>/images/fix.gif</xsl:variable>
      <td valign="top"><img alt="fix" class="icon" src="{$src}"/></td>
      <td><xsl:apply-templates/></td>
    </tr>
  </xsl:template>

  <xsl:template match="changelog/scode">
    <tr>
      <xsl:variable name="src"><xsl:value-of select="$relative-path"/>/images/code.gif</xsl:variable>
      <td valign="top"><img alt="code" class="icon" src="{$src}"/></td>
      <td><xsl:apply-templates/></td>
    </tr>
  </xsl:template>

  <!-- Process an attributes list with nested attribute elements -->
  <xsl:template match="status">
    <table border="1" cellpadding="5" width="100%">
      <tr>
        <th class="attribute">
          Priority
        </th>
        <th class="attribute">
          Progress
        </th>
        <th class="attribute">
          Estimated
        </th>
        <th width="75%" class="attribute">
          Action Item
        </th>
        <th width="75%" class="attribute">
          Volunteers
        </th>
        <xsl:for-each select="item">
        <tr>
          <td align="left" valign="top">
          <xsl:choose>
            <xsl:when test="@priority = 'hi'">
              <xsl:variable name="src"><xsl:value-of select="$relative-path"/>/images/upr.gif</xsl:variable>
              <img alt="High" class="icon" src="{$src}"/>
            </xsl:when>
            <xsl:when test="@priority = 'low'">
              <xsl:variable name="src"><xsl:value-of select="$relative-path"/>/images/downg.gif</xsl:variable>
              <img alt="Low" class="icon" src="{$src}"/>
            </xsl:when>
            <xsl:when test="@priority = 'done'">
              <xsl:variable name="src"><xsl:value-of select="$relative-path"/>/images/ok.gif</xsl:variable>
              <img alt="Done" class="icon" src="{$src}"/>
            </xsl:when>
            <xsl:when test="@priority = 'del'">
              <xsl:variable name="src"><xsl:value-of select="$relative-path"/>/images/x.gif</xsl:variable>
              <img alt="Removed" class="icon" src="{$src}"/>
            </xsl:when>
            <xsl:when test="@priority = 'lock'">
              <xsl:variable name="src"><xsl:value-of select="$relative-path"/>/images/lock.gif</xsl:variable>
              <img alt="Locked" class="icon" src="{$src}"/>
            </xsl:when>
            <xsl:when test="@priority = 'block'">
              <xsl:variable name="src"><xsl:value-of select="$relative-path"/>/images/excl.gif</xsl:variable>
              <img alt="Blocker" class="icon" src="{$src}"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@priority"/>
            </xsl:otherwise>
          </xsl:choose>
          </td>
          <td align="left" valign="top">
          <xsl:choose>
          <xsl:when test="@progress != ''">
              <xsl:variable name="rank"><xsl:value-of select="@progress"/></xsl:variable>
              <xsl:variable name="src"><xsl:value-of select="$relative-path"/>/images/rank<xsl:value-of select="@progress"/>.gif</xsl:variable>
              <img alt="{$rank}0%" class="icon" src="{$src}"/>
          </xsl:when>
          <xsl:otherwise>
              <xsl:variable name="src"><xsl:value-of select="$relative-path"/>/images/rank0.gif</xsl:variable>
              <img alt="0%" class="icon" src="{$src}"/>
          </xsl:otherwise>
          </xsl:choose>
          </td>

          <td align="left" valign="top">
            <xsl:value-of select="@estimate"/>
          </td>
          <td align="left" valign="top">
            <xsl:apply-templates/>
          </td>
          <td align="left" valign="top">
            <xsl:value-of select="@owner"/>
          </td>
        </tr>
        </xsl:for-each>
      </tr>
    </table>
  </xsl:template>

  <!-- Link to a bug report -->
  <xsl:template match="bug">
      <xsl:variable name="link"><xsl:value-of select="$buglink"/><xsl:value-of select="text()"/></xsl:variable>
      <a href="{$link}"><xsl:apply-templates/></a>
  </xsl:template>


  <xsl:template match="code">
    <b class="code"><xsl:apply-templates select="text()"/></b>
  </xsl:template>

  <xsl:template match="todo">
    <xsl:variable name="note"><xsl:value-of select="text()"/></xsl:variable>
    <p class="todo">
      This paragraph has not been written yet, but <b>you</b> can contribute to it.
      <xsl:if test="string-length($note) > 0">
        The original author left a note attached to this TO-DO item:
        <b><xsl:value-of select="$note"/></b>
      </xsl:if>
    </p>
  </xsl:template>

  <!-- Process a source code example -->
  <xsl:template match="source">
    <xsl:variable name="void">
      <xsl:value-of select="$relative-path"/><xsl:value-of select="$void-image"/>
    </xsl:variable>
    <div class="example"><pre>
        <xsl:value-of select="."/>
        </pre>
    </div>
  </xsl:template>

  <!-- Screens -->

  <xsl:template match="screen">
    <xsl:variable name="void">
      <xsl:value-of select="$relative-path"/><xsl:value-of select="$void-image"/>
    </xsl:variable>
    <div class="screen">
      <table width="100%" class="screen" cellspacing="0" cellpadding="10">
        <tr>
          <td class="screen">
            <xsl:apply-templates select="note|wait|type|typedos|type5250|typenext|read"/>
          </td>
        </tr>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="note">
    <code>
      <nobr>
      <xsl:text>. </xsl:text>
      <b class="note"><xsl:value-of select="text()"/></b>
      </nobr>
    </code>
    <br/>
  </xsl:template>

  <xsl:template match="wait">
    <code>
      <nobr>
      <xsl:text>. </xsl:text>
      <b class="screen">[...]</b>
      </nobr>
    </code>
    <br/>
  </xsl:template>

  <xsl:template match="type">
    <code>
      <nobr>
        <em class="screen">
          <xsl:text>[user@host] ~</xsl:text>
          <xsl:if test="string-length(@dir) > 0">
            <xsl:text>/</xsl:text>
            <xsl:value-of select="@dir"/>
          </xsl:if>
          <xsl:text> $ </xsl:text>
        </em>
        <xsl:if test="string-length(text()) > 0">
          <b class="screen"><xsl:value-of select="text()"/></b>
        </xsl:if>
      </nobr>
    </code>
    <br/>
  </xsl:template>

  <xsl:template match="typedos">
    <code>
      <nobr>
        <em class="screen">
          <xsl:text>C:\</xsl:text>
          <xsl:if test="string-length(@dir) > 0">
            <xsl:value-of select="@dir"/>
          </xsl:if>
          <xsl:text>> </xsl:text>
        </em>
        <xsl:if test="string-length(text()) > 0">
          <b class="screen"><xsl:value-of select="text()"/></b>
        </xsl:if>
      </nobr>
    </code>
    <br/>
  </xsl:template>

  <xsl:template match="type5250">
    <code>
      <nobr>
        <em class="screen">
          <xsl:text>===></xsl:text>
        </em>
        <xsl:if test="string-length(text()) > 0">
          <b class="screen"><xsl:value-of select="text()"/></b>
        </xsl:if>
      </nobr>
    </code>
    <br/>
  </xsl:template>

  <xsl:template match="typenext">
    <code>
      <nobr>
        <em class="screen">
          <xsl:text> </xsl:text>
        </em>
        <xsl:if test="string-length(text()) > 0">
          <b class="screen"><xsl:value-of select="text()"/></b>
        </xsl:if>
      </nobr>
    </code>
    <br/>
  </xsl:template>

  <xsl:template match="read">
    <code class="screen">
      <nobr>
        <xsl:apply-templates select="text()|enter"/>
      </nobr>
    </code>
    <br/>
  </xsl:template>

  <xsl:template match="enter">
    <b class="screen"><xsl:value-of select="text()"/></b>
  </xsl:template>



  <!-- Process everything else by just passing it through -->
  <xsl:template match="*|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
