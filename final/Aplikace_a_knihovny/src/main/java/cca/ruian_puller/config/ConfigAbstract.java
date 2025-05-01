package cca.ruian_puller.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static java.lang.System.exit;

/**
 * ConfigAbstract is an abstract class that provides common functionality for configuration classes.
 * It handles reading and parsing a JSON configuration file using Jackson's ObjectMapper.
 */
@Log4j2
public abstract class ConfigAbstract {

    protected ObjectMapper objectMapper;
    protected File configFile;
    protected JsonNode configNode;

    /**
     * Constructor to initialize the configuration object.
     *
     * @param objectMapper  The ObjectMapper instance for JSON processing.
     * @param configFilePath The path to the configuration file.
     * @throws IOException If an error occurs while reading the configuration file.
     */
    protected ConfigAbstract(ObjectMapper objectMapper, String configFilePath) throws IOException {
        this.objectMapper = objectMapper;
        this.configFile = Paths.get(configFilePath).toFile();

        // Check if the file exists before reading
        if (!configFile.exists()) {
            log.error("Configuration file not found: {}", configFilePath);
            exit(1);
        }

        this.configNode = objectMapper.readTree(configFile);
    }

    /**
     * Method to get String value from the configuration node.
     *
     * @param node The configuration node.
     * @param key  The key to look for in the configuration node.
     * @return The String value associated with the key, or an empty string if the key is not present.
     */
    protected String getTextValue(JsonNode node, String key) {
        JsonNode valueNode = node.get(key);
        return (valueNode != null) ? valueNode.asText() : "";
    }

    /**
     * Method to get boolean value from the configuration node.
     *
     * @param node The configuration node.
     * @param key The key to look for in the configuration node.
     * @return The boolean value associated with the key, or false if the key is not present.
     */
    protected boolean getBooleanValue(JsonNode node, String key) {
        JsonNode valueNode = node.get(key);
        return (valueNode != null) && valueNode.asBoolean();
    }

    /**
     * Method to get integer value from the configuration node.
     *
     * @param node The configuration node.
     * @param key  The key to look for in the configuration node.
     * @return The integer value associated with the key, or 0 if the key is not present.
     */
    protected int getIntValue(JsonNode node, String key) {
        JsonNode valueNode = node.get(key);
        return (valueNode != null) ? valueNode.asInt() : 0;
    }
}
