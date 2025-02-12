package cca.ruian_puller.config;

import cca.ruian_puller.utils.LoggerUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ConfigReader {

    public static AppConfig readConfig(String path) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(path), AppConfig.class);
        } catch (IOException e) {
            LoggerUtil.LOGGER.error("Error reading configuration file: {}", e.getMessage());
            return null;
        }
    }
}