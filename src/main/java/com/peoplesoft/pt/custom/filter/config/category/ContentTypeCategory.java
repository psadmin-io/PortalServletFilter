package com.peoplesoft.pt.custom.filter.config.category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class ContentTypeCategory extends ConfigCategory {

    @Setting(value = "whitelist", comment = "The following PeopleSoft content types are available : \n" + 
            "c - Represents a component.\n" + 
            "s - Represents a script.\n" + 
            "w - Represents a worklist.\n" + 
            "f - Represents a file.\n" + 
            "t - Represents a template.\n" + 
            "h - Represents a homepage.\n" + 
            "e - Represents external.\n" + 
            "l - Represents a lifeevent.\n" + 
            "q - Represents a query\n" + 
            "j - Represents JSR 168.\n" + 
            "n - Represents a worklistnav.\n" + 
            "Note: If you want other non-PeopleSoft related content types, add it exactly as it appears in the request")
    public List<String> whitelist = new ArrayList<>();

    public ContentTypeCategory() {
        this.whitelist.add("c");
    }
}
