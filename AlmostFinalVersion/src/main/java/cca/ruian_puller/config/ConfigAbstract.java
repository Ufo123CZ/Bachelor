package cca.ruian_puller.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static java.lang.System.exit;

@Log4j2
public abstract class ConfigAbstract {

    protected ObjectMapper objectMapper;
    protected File configFile;
    protected JsonNode configNode;

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

    protected String getTextValue(JsonNode node, String key) {
        JsonNode valueNode = node.get(key);
        return (valueNode != null) ? valueNode.asText() : "";
    }

    protected boolean getBooleanValue(JsonNode node, String key) {
        JsonNode valueNode = node.get(key);
        return (valueNode != null) && valueNode.asBoolean();
    }

    protected int getIntValue(JsonNode node, String key) {
        JsonNode valueNode = node.get(key);
        return (valueNode != null) ? valueNode.asInt() : 0;
    }
}
