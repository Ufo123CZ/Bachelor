package cca.ruian_puller.config;

import cca.ruian_puller.utils.LoggerUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ConfigReader {

    public static AppConfig readConfig(String path) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode configNode = objectMapper.readTree(new File(path));
            JsonNode databaseNode = configNode.get("database");

            // Read database configuration
            DatabaseConfig databaseConfig = new DatabaseConfig(
                    databaseNode.get("type").asText(),
                    databaseNode.get("url").asText(),
                    databaseNode.get("username").asText(),
                    databaseNode.get("password").asText()
            );

            return new AppConfig(databaseConfig);
        } catch (IOException e) {
            LoggerUtil.LOGGER.error("Error reading configuration file: {}", e.getMessage());
            return null;
        }
    }
}