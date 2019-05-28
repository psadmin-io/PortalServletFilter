package com.peoplesoft.pt.custom.filter.config.category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class ReloadCommandCategory extends ConfigCategory {

    @Setting(value = "allowReload", comment = "If enabled, passing 'srvcmd=reload' in request URL will trigger the configuration to reload.\nNote: The client IP address must be whitelisted.")
    public boolean allowReload = false;

    @Setting(value = "whitelist", comment = "Contains a list of IP addresses allowed to reload configuration.\nNote: All IP addresses should be in CIDR notation.")
    public List<String> whitelist = new ArrayList<>();
}
