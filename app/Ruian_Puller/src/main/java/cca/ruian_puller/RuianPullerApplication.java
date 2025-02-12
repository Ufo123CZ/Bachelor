package cca.ruian_puller;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.ConfigReader;
import cca.ruian_puller.utils.Consts;
import cca.ruian_puller.utils.LoggerUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.Objects;

@SpringBootApplication
public class RuianPullerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RuianPullerApplication.class, args);
        LoggerUtil.LOGGER.info("Starting the application.");

        // Todo: List
        // 1. Read the configuration file
        // 2. Download the data from the RUIAN
        // 3. Parse the data
        // 4. Save the data to the database

        AppConfig ac = null;
        try{
            ac = ConfigReader.readConfig(
                    Objects.requireNonNull(
                            RuianPullerApplication.class.getClassLoader().getResource(
                                    Consts.CONFIG_FILE
                            )
                    ).getPath()
            );
        } catch (NullPointerException e) {
            LoggerUtil.LOGGER.error("Configuration file not found.");
        }


        // Print all read information
        if (ac != null) {
            LoggerUtil.LOGGER.info("Configuration read successfully.");
            for (String s : Arrays.asList(ac.getDatabase().getUrl(), ac.getDatabase().getUsername(), ac.getDatabase().getPassword())) {
                LoggerUtil.LOGGER.info("Configuration: {}", s);
            }
        } else {
            LoggerUtil.LOGGER.error("Configuration read failed.");
        }
    }

}
