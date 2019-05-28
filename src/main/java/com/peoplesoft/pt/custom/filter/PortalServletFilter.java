package com.peoplesoft.pt.custom.filter;

import com.google.common.io.ByteSource;
import com.peoplesoft.pt.custom.filter.config.PortalServletConfig;
import com.peoplesoft.pt.custom.filter.config.category.HeaderCategory;
import com.peoplesoft.pt.custom.filter.config.category.ReloadCommandCategory;
import com.peoplesoft.pt.custom.filter.config.util.PSRequestInfo;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.util.SubnetUtils;
import psft.pt8.jb.JBEntry;
import psft.pt8.net.NetSession;
import psft.pt8.util.PSSessionProp;
import weblogic.management.runtime.WebAppComponentRuntimeMBean;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

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

    public static final String FILTER_ID = "PortalServletFilter";
    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private FilterConfig config;
    private PortalServletConfig portalConfig;
    private HeaderCategory headerCategory;
    private ReloadCommandCategory reloadCategory;
    private WebAppComponentRuntimeMBean peoplesoftRuntimeBean;
    private boolean isEnabled = true;
    private boolean checkSessionProps = false;

    public PortalServletFilter() {
        this.config = null;
        this.loadProperties();
    }

    private void loadProperties() {
        this.portalConfig = new PortalServletConfig();
        this.headerCategory = portalConfig.getConfig().headers;
        this.reloadCategory = portalConfig.getConfig().commands.reload;
        if (this.headerCategory.appServer|| headerCategory.appStatus || headerCategory.authToken || headerCategory.menu || headerCategory.pwdDaysLeft || headerCategory.srid) {
            this.checkSessionProps = true;
        }
        if (!this.checkSessionProps && !headerCategory.userid && !headerCategory.clientIp && !headerCategory.roles && !headerCategory.cookie && !headerCategory.site && !headerCategory.requestSearchData) {
            this.isEnabled = false;
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
        final PSRequestInfo requestInfo = new PSRequestInfo(this.portalConfig, servletRequest);
        if (!requestInfo.contentTypeAllowed()) {
            chain.doFilter(request, response);
            return;
        }

        final HttpServletResponse servletResponse = (HttpServletResponse) response;
        final HttpSession session = servletRequest.getSession(false);

        String clientIp = "";
        if (this.reloadCategory.allowReload) {
            final String queryString = servletRequest.getQueryString();
            if (queryString != null) {
                clientIp = this.getClientIp(servletRequest);
                if (queryString.contains("srvcmd=reload") && this.canReload(clientIp)) {
                    this.reloadConfig();
                }
            }
        }
        if (this.headerCategory.clientIp) {
            if (clientIp.isEmpty()) {
                clientIp = this.getClientIp(servletRequest);
            }
            servletResponse.addHeader("X-PS-CLIENTIP", clientIp);
        }
        if (session == null) {
            chain.doFilter(request, response);
            return;
        }

        if (this.headerCategory.cookie) {
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
        if (this.headerCategory.userid) {
            servletResponse.addHeader("X-PS-USERID", userId);
        }
        final String site = userInfo.substring(slashIndex + 1, userInfo.length());
        if (this.headerCategory.site) {
            servletResponse.addHeader("X-PS-SITE", site);
        }
        if (this.headerCategory.roles) {
            final Hashtable<String, String> roleMap = (Hashtable<String, String>) session.getAttribute("ROLES");
            String roles = "";
            for (String role : roleMap.keySet()) {
                roles = role + "|";
            }
            servletResponse.addHeader("X-PS-ROLES", roles);
        }

        if (this.headerCategory.authToken || this.headerCategory.sessionCookie || this.headerCategory.sessionCount) {
            if (this.peoplesoftRuntimeBean == null) {
                this.peoplesoftRuntimeBean = (WebAppComponentRuntimeMBean) session.getServletContext().getAttribute("weblogic.servlet.WebAppComponentRuntimeMBean");
                if (this.peoplesoftRuntimeBean == null) {
                    chain.doFilter(request, response);
                    return;
                }
            }

            if (this.headerCategory.authToken) {
                servletResponse.addHeader("X-PS-AUTHTOKEN", this.peoplesoftRuntimeBean.getSessionCookieDomain());
            }
            if (this.headerCategory.sessionCookie) {
                servletResponse.addHeader("X-PS-SESSION-COOKIE", this.peoplesoftRuntimeBean.getSessionCookieName());
            }
            if (this.headerCategory.sessionCount) {
                servletResponse.addHeader("X-PS-SESSION-COUNT", Integer.toString(this.peoplesoftRuntimeBean.getAllServletSessions().size()));
            }
        }

        PortalServletRequestWrapper requestWrapper = null;
        if (this.headerCategory.requestSearchData && !(request instanceof PortalServletRequestWrapper)) {
            final String contentType = servletRequest.getContentType();
            final boolean isFormPost =  (contentType != null && contentType.contains(FORM_CONTENT_TYPE)
                        && "POST".equalsIgnoreCase(servletRequest.getMethod()));
            if (isFormPost && this.requestTracked(requestInfo.getRequestDataKey())) {
                requestWrapper = new PortalServletRequestWrapper(servletRequest);
                try {
                    final String read = ByteSource.wrap(requestWrapper.getCachedData())
                        .asCharSource(StandardCharsets.UTF_8).read();
                    // strip ICBcDomData data
                    final String delim1 = "&ICBcDomData=";
                    final int p1 = read.indexOf(delim1);
                    final int p2 = read.indexOf("&ICPanelName");
                    String result = read;
                    if (p1 >= 0 && p2 > p1) {
                        result = result.substring(0, p1+delim1.length())
                                   + ""
                                   + result.substring(p2);
                    }
                    final String decoded = java.net.URLDecoder.decode(result, StandardCharsets.UTF_8.name());
                    final String[] postParams = decoded.split("&");
                    final List<String> searchData = new ArrayList<>();
                    final List<String> metaData = new ArrayList<>();
                    final boolean isSearchRequest = decoded.contains("ICAction=#KEY") || decoded.contains("ICAction=#ICSearch");
                    for (String param : postParams) {
                        final int index = param.indexOf("=");
                        if (index == -1) {
                            continue;
                        }

                        final String[] parts = param.split("=");
                        if (parts.length < 2) {
                            // Ignore keys with no data
                            continue;
                        }

                        final String key = parts[0];
                        final String value = parts[1];
                        if (key.equals("ICBcDomData") || key.equals("FacetPath") || key.equals("ResponsetoDiffFrame") || key.equals("TargetFrameName")) {
                            // ignore
                            continue;
                        }

                        if (isSearchRequest && !key.startsWith("IC") && !key.startsWith("#IC") && !this.searchRecordBlacklisted(key)) {
                            searchData.add(param);
                        }
                        if (metaDataTracked(param)) {
                            metaData.add(param);
                        }
                    }

                    if (!metaData.isEmpty()) {
                        // make sure to strip CRLF endings
                        final String data = String.join(",", metaData).replace("\n", "").replace("\r", "");
                        servletResponse.addHeader("X-PS-REQ-META", data);
                    }
                    if (!searchData.isEmpty()) {
                        // make sure to strip CRLF endings
                        final String data = String.join(",", searchData).replace("\n", "").replace("\r", "");
                        servletResponse.addHeader("X-PS-REQ-SRCH", data);
                    }
                } catch (Throwable t) {
                    // ignore
                }
            }
        }

        if (!this.checkSessionProps) {
            if (requestWrapper != null) {
                chain.doFilter(requestWrapper, response);
                return;
            }

            chain.doFilter(request, response);
            return;
        }

        PSSessionProp sessionProps = (PSSessionProp) session.getAttribute("portalSessionProps/" + site);
        if (sessionProps == null) {
            // check icSessionProps
            sessionProps = (PSSessionProp) session.getAttribute("icSessionProp/" + site);
        }
        if (sessionProps != null) {
            if (this.headerCategory.srid) {
                final String SRID = (String) sessionProps.get("SRID");
                if (SRID != null) {
                    servletResponse.addHeader("X-PS-SRID", SRID);
                }
            }

            JBEntry jbe = (JBEntry) sessionProps.get("JBridge");
            if (jbe != null) {
                NetSession ns = (psft.pt8.net.NetSession) jbe.getSession();
                if (ns != null) {
                    if (this.headerCategory.appStatus) {
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
                    if (this.headerCategory.appServer) {
                        servletResponse.addHeader("X-PS-APPSERVER", ns.getCurrentAppServer());
                    }
                    if (this.headerCategory.menu) {
                        servletResponse.addHeader("X-PS-MENU", ns.getLoginInfo().getCurrentMenuName());
                    }
                    if (this.headerCategory.pwdDaysLeft) {
                        servletResponse.addHeader("X-PS-PWDDAYSLEFT", Integer.toString(ns.getPwdDaysLeft()));
                    }
                }
            }
        }

        if (requestWrapper != null) {
            chain.doFilter(requestWrapper, response);
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest servletRequest) {
        String clientIp = servletRequest.getHeader("X-FORWARDED-FOR");
        if (clientIp == null) {
            clientIp = servletRequest.getRemoteAddr();
        }
        return clientIp;
    }

    private boolean canReload(String clientIp) {
        for (String subnet : this.reloadCategory.whitelist) {
            final SubnetUtils utils = new SubnetUtils(subnet);
            if (utils.getInfo().isInRange(clientIp)) {
                return true;
            }
        }

        return false;
    }

    private void reloadConfig() {
        try {
            this.portalConfig.load();
            this.headerCategory = this.portalConfig.getConfig().headers;
            this.reloadCategory = this.portalConfig.getConfig().commands.reload;
            Logger.getLogger(this.getClass().getName()).info("Reloaded configuration.");
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean requestTracked(String key) {
        for (String entry : portalConfig.getConfig().requestData.request_whitelist) {
            if (FilenameUtils.wildcardMatch(key.toLowerCase(), entry.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean searchRecordBlacklisted(String key) {
        for (String entry : portalConfig.getConfig().requestData.search_record_blacklist) {
            if (FilenameUtils.wildcardMatch(key.toLowerCase(), entry.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean metaDataTracked(String key) {
        for (String entry : portalConfig.getConfig().requestData.metadata_whitelist) {
            if (FilenameUtils.wildcardMatch(key.toLowerCase(), entry.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
