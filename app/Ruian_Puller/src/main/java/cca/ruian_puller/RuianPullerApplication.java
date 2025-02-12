package cca.ruian_puller;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.ConfigReader;
import cca.ruian_puller.db.DBCommunication;
import cca.ruian_puller.db.SQLConst;
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
            String dbUrl = ac.getDatabase().getUrl();
            String dbUsername = ac.getDatabase().getUsername();
            String dbPassword = ac.getDatabase().getPassword();
            LoggerUtil.LOGGER.info("Database URL: {}", dbUrl);
            LoggerUtil.LOGGER.info("Database Username: {}", dbUsername);

            // DB Connect
            if (DBCommunication.getInstance().connect(dbUrl, dbUsername, dbPassword)) {
                LoggerUtil.LOGGER.info("Connected to the database.");
            } else {
                LoggerUtil.LOGGER.error("Connection to the database failed.");
                return;
            }
            if (DBCommunication.getInstance().sendQuery(SQLConst.INSERT, "cisladomovni", "cislo1, cislo2, cislo4", "1, 3, 5")) {
                LoggerUtil.LOGGER.info("Query sent successfully.");
            } else {
                LoggerUtil.LOGGER.error("Query send failed.");
            }

        } else {
            LoggerUtil.LOGGER.error("Configuration read failed.");
        }
    }

}
