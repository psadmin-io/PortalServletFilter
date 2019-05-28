package com.peoplesoft.pt.custom.filter.config.category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class HeaderCategory extends ConfigCategory {

    @Setting(value = "X-PS-APPSERVER", comment = "Displays the appserver host with port.")
    public boolean appServer = false;

    @Setting(value = "X-PS-APPSTATUS", comment = "Displays the appserver's status.")
    public boolean appStatus = false;

    @Setting(value = "X-PS-AUTHTOKEN", comment = "Displays the authtoken of PIA.")
    public boolean authToken = false;

    @Setting(value = "X-PS-CLIENTIP", comment = "Displays the client's ip address.")
    public boolean clientIp = true;

    @Setting(value = "X-PS-COOKIE", comment = "Displays all cookies associated with request.")
    public boolean cookie = false;

    @Setting(value = "X-PS-MENU", comment = "Displays the current menu being accessed.")
    public boolean menu = false;

    @Setting(value = "X-PS-PWDDAYSLEFT", comment = "Displays the user's remaining days before password expires.")
    public boolean pwdDaysLeft = false;

    @Setting(value = "X-PS-ROLES", comment = "Displays the client's PS roles.")
    public boolean roles = false;

    @Setting(value = "X-PS-REQ-META", comment = "Displays a comma separated list of request metadata in the format : 'key=value'.")
    public boolean requestMetaData = true;

    @Setting(value = "X-PS-REQ-SRCH", comment = "Displays a comma separated list of request search data in the format : 'key=value'.")
    public boolean requestSearchData = true;

    @Setting(value = "X-PS-SESSION-COOKIE", comment = "Displays the session cookie.")
    public boolean sessionCookie = false;

    @Setting(value = "X-PS-SESSION-COUNT", comment = "Displays the current total open sessions to PIA.")
    public boolean sessionCount = true;

    @Setting(value = "X-PS-SITE", comment = "Displays the PIA site name.")
    public boolean site = false;

    @Setting(value = "X-PS-SRID", comment = "Displays the SRID for the user's session.")
    public boolean srid = true;

    @Setting(value = "X-PS-USERID", comment = "Displays the client's user id.")
    public boolean userid = true;

    public boolean checkSessionProps() {
        if (this.appServer|| this.appStatus || this.authToken || this.menu || this.pwdDaysLeft || this.srid) {
            return true;
        }

        return false;
    }
}
