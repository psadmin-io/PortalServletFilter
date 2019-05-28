package com.peoplesoft.pt.custom.filter.config.category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class GeneralCategory extends ConfigCategory {

    @Setting(value = "content_type", comment = "Controls various 'Content-Type' settings."
            + "\nNote: As this section contains a whitelist, see https://commons.apache.org/proper/commons-io/javadocs/api-2.6/org/apache/commons/io/FilenameUtils.html#wildcardMatch-java.lang.String-java.lang.String-" 
            + "\n on how the list is matched.")
    public ContentTypeCategory contentType = new ContentTypeCategory();
}
