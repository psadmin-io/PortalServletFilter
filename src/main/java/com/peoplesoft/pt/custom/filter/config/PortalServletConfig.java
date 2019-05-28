package com.peoplesoft.pt.custom.filter.config;

import com.peoplesoft.pt.custom.filter.PortalServletFilter;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class PortalServletConfig {

    public static final String CONFIG_HEADER = "Portal Servlet Filter 0.4";

    private static final ConfigurationOptions LOADER_OPTIONS = ConfigurationOptions.defaults()
            .setHeader(CONFIG_HEADER);

    private final Path path = Paths.get("piaconfig", "properties", "PortalServletFilter.conf");

    /**
     * The loader (mapped to a file) used to read/write the config to disk
     */
    private HoconConfigurationLoader loader;

    /**
     * A node representation of "whats actually in the file".
     */
    private CommentedConfigurationNode fileData = SimpleCommentedConfigurationNode.root(LOADER_OPTIONS);

    /**
     * A node representation of {@link #fileData}, merged with the data of {@link #parent}.
     */
    private CommentedConfigurationNode data = SimpleCommentedConfigurationNode.root(LOADER_OPTIONS);

    /**
     * The mapper instance used to populate the config instance
     */
    private ObjectMapper<PortalServletConfigData>.BoundInstance configMapper;

    public PortalServletConfig() {
        try {
            Files.createDirectories(this.path.getParent());
            if (Files.notExists(this.path)) {
                Files.createFile(this.path);
            }

            this.loader = HoconConfigurationLoader.builder().setFile(this.path.toFile()).build();
            this.configMapper = ObjectMapper.forClass(PortalServletConfigData.class).bindToNew();

            load();
            save();
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName(), "Failed to load configuration at path \" + path.toAbsolutePath()\n" + e.getMessage());
        }
    }

    public PortalServletConfigData getConfig() {
        return this.configMapper.getInstance();
    }

    public boolean save() {
        try {
            // save from the mapped object --> node
            CommentedConfigurationNode saveNode = SimpleCommentedConfigurationNode.root(LOADER_OPTIONS);
            this.configMapper.serialize(saveNode.getNode(PortalServletFilter.FILTER_ID));

            // save the data to disk
            this.loader.save(saveNode);

            return true;
        } catch (IOException | ObjectMappingException e) {
            Logger.getLogger(this.getClass().getName(), "Failed to save configuration\n" + e.getMessage());
            return false;
        }
    }

    public void load() throws IOException, ObjectMappingException {
        // load settings from file
        CommentedConfigurationNode loadedNode = this.loader.load();

        // store "what's in the file" separately in memory
        this.fileData = loadedNode;

        this.data = this.fileData;

        // populate the config object
        populateInstance();
    }

    private void populateInstance() throws ObjectMappingException {
        this.configMapper.populate(this.data.getNode(PortalServletFilter.FILTER_ID));
    }

    public CommentedConfigurationNode getRootNode() {
        return this.data.getNode(PortalServletFilter.FILTER_ID);
    }

    public Path getPath() {
        return this.path;
    }
}