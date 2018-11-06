# PortalServletFilter

* [Source]
* [Issues]
* [Downloads]

Provides additional logging for PeopleSoft applications.

## Available Headers
* `X-PS-APPSERVER` : Displays the appserver host with port (Enabled by default)
* `X-PS-APPSTATUS` : Displays the appserver's status (running/stopped)
* `X-PS-AUTHTOKEN`: Displays the authtoken of PIA
* `X-PS-CLIENTIP` : Displays the client's ip address (Enabled by default)
* `X-PS-COOKIE` : Displays all cookies associated with request
* `X-PS-DB` : Displays the PS database
* `X-PS-MENU` : Displays the current menu being accessed
* `X-PS-PWDDAYSLEFT` : Displays the user's remaining days before password expires
* `X-PS-ROLES` : Displays the client's PS roles
* `X-PS-SESSION-COOKIE` : Displays the session cookie
* `X-PS-SESSION-COUNT` : Displays the current total open sessions to PIA
* `X-PS-SRID` : Displays the SRID for the user's session
* `X-PS-USERID` : Displays the client's user id (Enabled by default)

## Prerequisites
* [Java 8]
* BEA Library - You can normally find this library in your PIA install under `domain/applications/peoplesoft/lib/bea.jar`
* PIA Common Library - You can normally find this library in your PIA install under `domain/applications/peoplesoft/pspc.war/WEB-INF/lib/piacommon.jar`
* WebLogic Server Library API - You can normally find this library in your WLS install under `ORACLE_HOME/wlserver/server/lib/wls-api.jar`
Note: BEA, PIA Common, and WLS API libs need to be copied to the libs folder of this project or it will not build properly.

## Cloning

`git clone https://github.com/bloodmc/PortalServletFilter.git`

## Building
In order to build PortalServletFilter you simply need to run the `gradlew` command. On Windows systems you should run `gradlew.bat` instead of `./gradlew` to invoke the Gradle wrapper. You can find the compiled JAR file in `./build/libs` named `portalservlet-x.x.jar`.

## Installation
* Copy the portalservlet-x.x.jar to your `domain/applications/peoplesoft/PORTAL.war/WEB-INF/lib` folder.
* Edit `web.xml` under `domain/applications/peoplesoft/PORTAL.war/WEB-INF` to use the new filter.
Here is an example of how it should look
```
  <filter>
    <filter-name>PortalServletFilter</filter-name>
    <filter-class>com.peoplesoft.pt.custom.filter.PortalServletFilter</filter-class>
    <async-supported>true</async-supported>
  </filter>
  <filter-mapping>
    <filter-name>PortalServletFilter</filter-name>
    <url-pattern>/psp/*</url-pattern>
    <url-pattern>/psc/*</url-pattern>
    <url-pattern>/cs/*</url-pattern>
  </filter-mapping>
```
* Login to your weblogic console
* Open `http://server:port/console` and login.
* Go to `Environment > Servers > PIA > Logging > HTTP`.
* Click `Lock & Edit`.
* Select the checkbox for `HTTP access log file enabled`.
* Save the changes.
* Expand the `Advanced` section.
* Change the Format to Extended.
* Add each sc(HEADER) to the Extended Logging Format Fields. Replace `HEADER` with what you want to use.
* (Optional) Set the Log File Buffer to 0 if you are debugging and need to see entries immediately.
* Save the changes.
* Click the `Release Configuration` button.
* Restart the web server.

## Configuration
You can find the PortalServletFilter configuration in your PIA install under `domain/piaconfig/properties/PortalServletFilter.properties`.
This config allows you to customize what headers you wanted added to responses. After making changes, you will need to restart the PIA server.

## IMPORTANT
All headers are added to the client response. If you do NOT want these headers to appear on client side then you will have to strip them. 
If you are using a load balancer, here is how you can strip them from the response : 

1. **[F5]**
* [remove-x-headers-from-web-server-response](https://devcentral.f5.com/codeshare/remove-x-headers-from-web-server-response)

2. **[KEMP]**
* Create a Content Rule with type `Delete Header` and set the pattern to `/X-PS.*/`

3. **[HAProxy]** (credit to coryfazzini for this one)
* Add the following to your frontend or backend section haproxy.cfg : `rspidel ^X-PS-.*`

[Source]: https://github.com/bloodmc/PortalServletFilter
[Issues]: https://github.com/bloodmc/PortalServletFilter/issues
[Downloads]: https://github.com/bloodmc/PortalServletFilter/releases
[Java 8]: http://java.oracle.com
[F5]: https://f5.com/glossary/load-balancer
[KEMP]: https://kemptechnologies.com/load-balancer/
[HAProxy]: https://www.haproxy.org/