package com.peoplesoft.pt.custom.filter.config.category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class RequestDataCategory extends ConfigCategory {

    @Setting(value = "request_whitelist", comment = "Contains a list of allowed request path's to track within 'X-PS-REQ-SRCH' header."
            + "\nThe following content types are currently supported : \n" 
            + "Type: Component, Format: '<menu>.<component>'" 
            + "\nEx. If you want to track all components for a specific menu then it would be 'menu.*'")
    public List<String> request_whitelist = new ArrayList<>();

    @Setting(value = "metadata_whitelist", comment = "Contains a list of metadata to track within 'X-PS-REQ-META' header if request is allowed."
            + "\nA metadata parameter is in the form of 'key=value'. "
            + "\nNote: Specifying only the metadata key in format 'key=*' will allow ALL possible values for key."
            + "\nEx. The entry 'ICAction=*' would track all ICAction data."
            + "\nEx. The entry 'ICAction=#ICSave' would track only this combination with key being 'ICAction' and the value being '#ICSave'.")
    public List<String> metadata_whitelist = new ArrayList<>();

    @Setting(value = "search_record_blacklist", comment = "Contains a list of search record keys that will be excluded from 'X-PS-REQ-SRCH' header if found."
            + "\nThis list is useful if your environment has additional security measures on specific data."
            + "\nEx. Adding '*EMPLID*' would block all search records that include EMPLID in its name.")
    public List<String> search_record_blacklist = new ArrayList<>();

    public RequestDataCategory() {
        this.metadata_whitelist.add("ICAction=*");
        this.request_whitelist.add("*");
    }
}
