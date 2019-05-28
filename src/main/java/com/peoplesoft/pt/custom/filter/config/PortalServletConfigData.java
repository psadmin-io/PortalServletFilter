package com.peoplesoft.pt.custom.filter.config;

import com.peoplesoft.pt.custom.filter.config.category.CommandCategory;
import com.peoplesoft.pt.custom.filter.config.category.GeneralCategory;
import com.peoplesoft.pt.custom.filter.config.category.HeaderCategory;
import com.peoplesoft.pt.custom.filter.config.category.RequestDataCategory;
import ninja.leaping.configurate.objectmapping.Setting;

public class PortalServletConfigData {

    @Setting(value = "commands", comment = "Contains a list of available commands that can be passed in URL. Format is 'srvcmd=<command>'.")
    public CommandCategory commands = new CommandCategory();

    @Setting(value = "general")
    public GeneralCategory general = new GeneralCategory();

    @Setting(value = "headers", comment = "Contains a list of additional headers that can be used with WebLogic access logs.")
    public HeaderCategory headers = new HeaderCategory();

    @Setting(value = "requestdata", comment = "Allows customization of request data tracking."
            + "\nNote: As this section contains both a blacklist and whitelist, see https://commons.apache.org/proper/commons-io/javadocs/api-2.6/org/apache/commons/io/FilenameUtils.html#wildcardMatch-java.lang.String-java.lang.String-" 
            + "\n on how the lists are matched.")
    public RequestDataCategory requestData = new RequestDataCategory();
}
