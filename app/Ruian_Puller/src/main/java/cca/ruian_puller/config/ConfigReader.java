package cca.ruian_puller.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@Log4j2
public class ConfigReader {
    private final ObjectMapper objectMapper;

    public ConfigReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DatabaseParams readDBConfig(String path) {
        File configFile = new File(path);

        // Check if the file exists before reading
        if (!configFile.exists()) {
            log.error("Configuration file not found: {}", path);
            return null;
        }

        try {
            JsonNode configNode = objectMapper.readTree(configFile);

            // Check if the "database" node exists
            JsonNode databaseNode = configNode.get("database");
            if (databaseNode == null) {
                log.error("Missing 'database' section in config file: {}", path);
                return null;
            }
            // Read database configuration and return it
            return new DatabaseParams(
                    getTextValue(databaseNode, "type"),
                    getTextValue(databaseNode, "url"),
                    getTextValue(databaseNode, "username"),
                    getTextValue(databaseNode, "password")
            );
        } catch (IOException e) {
            log.error("Error reading configuration file: {}", e.getMessage());
            return null;
        }
    }

//    public AppConfig readConfig(String path) {
//        File configFile = new File(path);
//
//        // Check if the file exists before reading
//        if (!configFile.exists()) {
//            log.error("Configuration file not found: {}", path);
//            return null;
//        }
//
//        try {
//            JsonNode configNode = objectMapper.readTree(configFile);
//
//            // Check if the "database" node exists
//            JsonNode databaseNode = configNode.get("database");
//            if (databaseNode == null) {
//                log.error("Missing 'database' section in config file: {}", path);
//                return null;
//            }
//
//            // Read database configuration safely
//            DatabaseConfig databaseConfig = new DatabaseConfig(
//                    getTextValue(databaseNode, "type"),
//                    getTextValue(databaseNode, "url"),
//                    getTextValue(databaseNode, "username"),
//                    getTextValue(databaseNode, "password")
//            );
//
//            // Create AppConfig object and set configuration
//            AppConfig ac = new AppConfig();
//            ac.setDatabase(databaseConfig);
//
//            return ac;
//        } catch (IOException e) {
//            log.error("Error reading configuration file: {}", e.getMessage());
//            return null;
//        }
//    }

    private String getTextValue(JsonNode node, String key) {
        JsonNode valueNode = node.get(key);
        return (valueNode != null) ? valueNode.asText() : "";
    }
}
