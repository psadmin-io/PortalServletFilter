package com.peoplesoft.pt.custom.filter.config.util;

import com.peoplesoft.pt.custom.filter.config.PortalServletConfig;

import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class PSRequestInfo {

    private final int PS_CONTENT_TYPE;
    private final int PS_NODE_TYPE;
    private final boolean contentTypeAllowed;
    private final String contentTypeFriendlyName;
    private final String regex = "\\/([A-Z_]+)\\.([^. ]+)";
    private String portalMenu = "";
    private String portalComponent = "";
    private String requestDataComponentKey = "";

    public PSRequestInfo(final PortalServletConfig config, final HttpServletRequest request) {
        final String pathInfo = request.getPathInfo();
        final String[] parts = pathInfo == null ? null : pathInfo.split("/");
        if (parts == null || parts.length < 5) {
            PS_CONTENT_TYPE = PSContentTypes.NONE;
            PS_NODE_TYPE = PSContentTypes.NONE;
            contentTypeAllowed = false;
            contentTypeFriendlyName = "none";
            return;
        }

        final String contentType = parts[4];
        final List<String> contentTypeWhitelist = config.getConfig().general.contentType.whitelist;
        if ("c".equalsIgnoreCase(contentType)) {
            PS_CONTENT_TYPE = PSContentTypes.COMPONENT;
            PS_NODE_TYPE = PSContentTypes.PSC;
            contentTypeFriendlyName = "component";
            if (pathInfo != null) {
                final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                final Matcher matcher = pattern.matcher(pathInfo);
                if (matcher.find()) {
                    this.portalMenu = matcher.group(1);
                    this.portalComponent = matcher.group(2);
                    this.requestDataComponentKey = this.portalMenu.toLowerCase() + "." + this.portalComponent.toLowerCase();
                }
            }
        } else if (contentType.equalsIgnoreCase("s")) {
            PS_CONTENT_TYPE = PSContentTypes.SCRIPT;
            PS_NODE_TYPE = PSContentTypes.PSC;
            contentTypeFriendlyName = "script";
        } else if (contentType.equalsIgnoreCase("w")) {
            PS_CONTENT_TYPE = PSContentTypes.WORKLIST;
            PS_NODE_TYPE = PSContentTypes.PSC;
            contentTypeFriendlyName = "worklist";
        } else if (contentType.equalsIgnoreCase("l")) {
            PS_CONTENT_TYPE = PSContentTypes.LIFEEVENT;
            PS_NODE_TYPE = PSContentTypes.PSC;
            contentTypeFriendlyName = "lifeevent";
        } else if (contentType.equalsIgnoreCase("f")) {
            PS_CONTENT_TYPE = PSContentTypes.FILE;
            PS_NODE_TYPE = PSContentTypes.PSC;
            contentTypeFriendlyName = "file";
        } else if (contentType.equalsIgnoreCase("t")) {
            PS_CONTENT_TYPE = PSContentTypes.TEMPLATE;
            PS_NODE_TYPE = PSContentTypes.PSP;
            contentTypeFriendlyName = "template";
        } else if (contentType.equalsIgnoreCase("h")) {
            PS_CONTENT_TYPE = PSContentTypes.HOMEPAGE;
            PS_NODE_TYPE = PSContentTypes.PSP;
            contentTypeFriendlyName = "homepage";
        } else if (contentType.equalsIgnoreCase("e")) {
            PS_CONTENT_TYPE = PSContentTypes.EXTERNAL;
            PS_NODE_TYPE = PSContentTypes.PSP;
            contentTypeFriendlyName = "external";
        } else if (contentType.equalsIgnoreCase("q")) {
            PS_CONTENT_TYPE = PSContentTypes.QUERY;
            PS_NODE_TYPE = PSContentTypes.PSC;
            contentTypeFriendlyName = "query";
        } else if (contentType.equalsIgnoreCase("j")) {
            PS_CONTENT_TYPE = PSContentTypes.JSR168;
            PS_NODE_TYPE = PSContentTypes.JSR;
            contentTypeFriendlyName = "jsr";
        } else if (contentType.equalsIgnoreCase("n")) {
            PS_CONTENT_TYPE = PSContentTypes.WORKLISTNAV;
            PS_NODE_TYPE = PSContentTypes.PSC;
            contentTypeFriendlyName = "worklistnav";
        } else {
            PS_CONTENT_TYPE = PSContentTypes.NONE;
            PS_NODE_TYPE = PSContentTypes.NONE;
            contentTypeFriendlyName = contentType;
        }
        if (containsIgnoreCase(contentTypeWhitelist, contentType)) {
            contentTypeAllowed = true;
        } else {
            contentTypeAllowed = false;
        }
    }

    public int getContentType() {
        return PS_CONTENT_TYPE;
    }

    public int getNodeType() {
        return PS_NODE_TYPE;
    }

    public boolean contentTypeAllowed() {
        return this.contentTypeAllowed;
    }

    public String getPortalMenu() {
        return this.portalMenu;
    }

    public String getPortalComponent() {
        return this.portalComponent;
    }

    public String getRequestDataKey() {
        return this.requestDataComponentKey;
    }

    private boolean containsIgnoreCase(List<String> list, String key) {
        for (String entry : list) {
            if (entry.equalsIgnoreCase(key)) {
                return true;
            }
        }

        return false;
    }
}
