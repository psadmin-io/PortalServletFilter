package com.peoplesoft.pt.custom.filter;

import psft.pt8.jb.JBEntry;
import psft.pt8.net.NetSession;
import psft.pt8.util.PSSessionProp;
import weblogic.management.runtime.WebAppComponentRuntimeMBean;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PortalServletFilter implements Filter {

    private FilterConfig config;
    private Path propertiesPath;
    private WebAppComponentRuntimeMBean peoplesoftRuntimeBean;
    public static final String CONFIG_HEADER = "Portal Servlet Filter 0.1\n"
            + "# The following headers can be used\n"
            + "# X-PS-APPSERVER : Displays the appserver host with port\n"
            + "# X-PS-APPSTATUS : Displays the appserver's status\n"
            + "# X-PS-AUTHTOKEN : Displays the authtoken of PIA\n"
            + "# X-PS-CLIENTIP : Displays the client's ip address\n"
            + "# X-PS-COOKIE : Displays all cookies associated with request\n"
            + "# X-PS-DB : Displays the PS database\n"
            + "# X-PS-MENU : Displays the current menu being accessed\n"
            + "# X-PS-PWDDAYSLEFT : Displays the user's remaining days before password expires\n"
            + "# X-PS-ROLES : Displays the client's PS roles\n"
            + "# X-PS-SESSION-COOKIE : Displays the session cookie\n"
            + "# X-PS-SESSION-COUNT : Displays the current total open sessions to PIA\n"
            + "# X-PS-USERID : Displays the client's user id\n";
    private boolean isEnabled = true;
    private boolean prop_appServer = true;
    private boolean prop_appStatus = false;
    private boolean prop_authToken = false;
    private boolean prop_clientIp = true;
    private boolean prop_cookie = false;
    private boolean prop_database = false;
    private boolean prop_menu = false;
    private boolean prop_pwdDaysLeft = false;
    private boolean prop_roles = false;
    private boolean prop_sessionCookie = false;
    private boolean prop_userId = true;
    private boolean checkJoltInfo = false;

    public PortalServletFilter() {
        this.config = null;
        this.loadProperties();
    }

    private void loadProperties() {
        Properties prop = new Properties() {
            private static final long serialVersionUID = -1414273837759328933L;

            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public Enumeration keys() {
                Enumeration keysEnum = super.keys();
                Vector<String> keyList = new Vector<String>();
                while(keysEnum.hasMoreElements()){
                  keyList.add((String)keysEnum.nextElement());
                }
                Collections.sort(keyList);
                return keyList.elements();
             }  
        };
        try {
            this.propertiesPath = Paths.get("piaconfig", "properties", "PortalServletFilter.properties");
            if (!Files.exists(this.propertiesPath)) {
                Files.createDirectories(propertiesPath.getParent());
                Files.createFile(propertiesPath);
                prop.setProperty("appserver", "true");
                prop.setProperty("appstatus", "false");
                prop.setProperty("authtoken", "false");
                prop.setProperty("clientip", "true");
                prop.setProperty("cookie", "false");
                prop.setProperty("database", "false");
                prop.setProperty("menu", "false");
                prop.setProperty("pwddaysleft", "false");
                prop.setProperty("roles", "false");
                prop.setProperty("sessioncookie", "false");
                prop.setProperty("userid", "true");
                prop.store(new FileOutputStream(this.propertiesPath.toFile()), CONFIG_HEADER);
                this.checkJoltInfo = true;
            } else {
                InputStream is = new FileInputStream(this.propertiesPath.toFile());
                prop.load(is);
                this.prop_appServer = Boolean.parseBoolean(prop.getProperty("appserver", "true"));
                this.prop_appStatus = Boolean.parseBoolean(prop.getProperty("appstatus", "false"));
                this.prop_authToken = Boolean.parseBoolean(prop.getProperty("authtoken", "false"));
                this.prop_clientIp = Boolean.parseBoolean(prop.getProperty("clientip", "true"));
                this.prop_cookie = Boolean.parseBoolean(prop.getProperty("cookie", "false"));
                this.prop_database = Boolean.parseBoolean(prop.getProperty("database", "false"));
                this.prop_menu = Boolean.parseBoolean(prop.getProperty("menu", "false"));
                this.prop_pwdDaysLeft = Boolean.parseBoolean(prop.getProperty("pwddaysleft", "false"));
                this.prop_roles = Boolean.parseBoolean(prop.getProperty("roles", "false"));
                this.prop_sessionCookie = Boolean.parseBoolean(prop.getProperty("sessioncookie", "false"));
                this.prop_userId = Boolean.parseBoolean(prop.getProperty("userid", "true"));
                if (this.prop_appServer || this.prop_appStatus || this.prop_authToken || this.prop_menu || this.prop_pwdDaysLeft) {
                    this.checkJoltInfo = true;
                }
                if (!this.checkJoltInfo && !this.prop_userId && !this.prop_clientIp && !this.prop_roles && !this.prop_cookie && !this.prop_database) {
                    this.isEnabled = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init(final FilterConfig config) throws ServletException {
        this.config = config;
    }

    public void destroy() {
        this.config = null;
    }

    @SuppressWarnings("unchecked")
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (!this.isEnabled || !(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        final HttpServletRequest servletRequest = (HttpServletRequest) request;
        final HttpServletResponse servletResponse = (HttpServletResponse) response;
        final HttpSession session = servletRequest.getSession(false);
        if (session == null) {
            chain.doFilter(request, response);
            return;
        }

        if (this.prop_cookie) {
            final Cookie[] reqCookies = servletRequest.getCookies();
            if (reqCookies != null) {
                String cookies = "";
                for (Cookie cookie : reqCookies) {
                    cookies += cookie.getName() + "|";
                }
                servletResponse.addHeader("X-PS-COOKIE", cookies);
            }
        }

        final String userInfo = (String) session.getAttribute("USERID");
        if (userInfo == null) {
            chain.doFilter(request, response);
            return;
        }

        final int slashIndex = userInfo.indexOf("/");
        final int atIndex = userInfo.indexOf("@");
        if (userInfo == null || slashIndex == -1 || atIndex == -1) {
            // no data
            chain.doFilter(request, response);
            return;
        }

        final String userId = userInfo.substring(0, atIndex);
        if (this.prop_userId) {
            servletResponse.addHeader("X-PS-USERID", userId);
        }
        final String database = userInfo.substring(slashIndex + 1, userInfo.length());
        if (this.prop_clientIp) {
            final String clientIp = userInfo.substring(atIndex + 1, slashIndex);
            servletResponse.addHeader("X-PS-CLIENTIP", clientIp);
        }
        if (this.prop_database) {
            servletResponse.addHeader("X-PS-DB", database);
        }
        if (this.prop_roles) {
            final ConcurrentHashMap<String, String> roleMap = (ConcurrentHashMap<String, String>) session.getAttribute("ROLES");
            String roles = "";
            for (String role : roleMap.keySet()) {
                roles = role + "|";
            }
            servletResponse.addHeader("X-PS-ROLES", roles);
        }

        if (this.prop_authToken || this.prop_sessionCookie) {
            if (this.peoplesoftRuntimeBean == null) {
                this.peoplesoftRuntimeBean = (WebAppComponentRuntimeMBean) session.getServletContext().getAttribute("weblogic.servlet.WebAppComponentRuntimeMBean");
                if (this.peoplesoftRuntimeBean == null) {
                    chain.doFilter(request, response);
                    return;
                }
            }

            if (this.prop_authToken) {
                servletResponse.addHeader("X-PS-AUTHTOKEN", this.peoplesoftRuntimeBean.getSessionCookieDomain());
            }
            if (this.prop_sessionCookie) {
                servletResponse.addHeader("X-PS-SESSION-COOKIE", this.peoplesoftRuntimeBean.getSessionCookieName());
            }
        }

        if (!this.checkJoltInfo) {
            chain.doFilter(request, response);
            return;
        }

        PSSessionProp portalSessionProps = (PSSessionProp) session.getAttribute("portalSessionProps/" + database);
        if (portalSessionProps == null) {
            // check icSessionProps
            portalSessionProps = (PSSessionProp) session.getAttribute("icSessionProp/" + database);
        }
        if (portalSessionProps != null) {
            JBEntry jbe = (JBEntry) portalSessionProps.get("JBridge");
            if (jbe != null) {
                NetSession ns = (psft.pt8.net.NetSession) jbe.getSession();
                if (ns != null) {
                    if (this.prop_appStatus) {
                        try {
                            if (ns.isAlive()) {
                                servletResponse.addHeader("X-PS-APPSTATUS", "running");
                            } else {
                                servletResponse.addHeader("X-PS-APPSTATUS", "stopped");
                            }
                        } catch (Exception e) {
                            servletResponse.addHeader("X-PS-APPSTATUS", "stopped");
                        }
                    }
                    if (this.prop_appServer) {
                        servletResponse.addHeader("X-PS-APPSERVER", ns.getCurrentAppServer());
                    }
                    if (this.prop_menu) {
                        servletResponse.addHeader("X-PS-MENU", ns.getLoginInfo().getCurrentMenuName());
                    }
                    if (this.prop_pwdDaysLeft) {
                        servletResponse.addHeader("X-PS-PWDDAYSLEFT", Integer.toString(ns.getPwdDaysLeft()));
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }
}
