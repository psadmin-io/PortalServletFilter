package com.peoplesoft.pt.custom.filter.config.category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CommandCategory {

    @Setting
    public ReloadCommandCategory reload = new ReloadCommandCategory();
}
