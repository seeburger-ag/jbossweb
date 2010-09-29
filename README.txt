Install Rewrite Valve in Tomcat
-------------------------------

Copy jbossweb-rewrite.jar to $CATALINA_BASE/server/lib

server.xml (on context.xml files)
Add (to either Engine, Host or Context elements depending on the desired
rewrite scope):
  <Valve className="org.jboss.web.rewrite.RewriteValve"/>

Documentation:
http://labs.jboss.com/file-access/default/members/jbossweb/freezone/modules/rewrite/index.html

Install PHP Servlet in Tomcat
-----------------------------

Install PHP 5.1.x
Add PHP dir to PATH
Compile and copy php5servlet to PHP dir.
Copy servlets-php.jar to $CATALINA_BASE/server/lib

server.xml
Add (to the main Server element):
  <Listener className="org.apache.catalina.servlets.php.LifecycleListener"/>

web.xml:
Add:
    <servlet>
        <servlet-name>php</servlet-name>
        <servlet-class>org.apache.catalina.servlets.php.Handler</servlet-class>
        <init-param>
          <param-name>debug</param-name>
          <param-value>0</param-value>
        </init-param>
         <load-on-startup>6</load-on-startup>
    </servlet>
    <servlet>
        <servlet-name>phps</servlet-name>
        <servlet-class>org.apache.catalina.servlets.php.Highlight</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>php</servlet-name>
        <url-pattern>*.php</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>phps</servlet-name>
        <url-pattern>*.phps</url-pattern>
    </servlet-mapping>

Documentation:
http://labs.jboss.com/file-access/default/members/jbossweb/freezone/modules/php/index.html

Install .NET support in Tomcat
------------------------------

Documentation:
http://labs.jboss.com/file-access/default/members/jbossweb/freezone/modules/dotnet/index.html
