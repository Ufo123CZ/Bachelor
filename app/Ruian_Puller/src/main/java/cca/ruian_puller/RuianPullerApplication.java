package cca.ruian_puller;

import cca.ruian_puller.config.ConfigReader;
import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.db.DBCommunication;
import cca.ruian_puller.db.SQLConst;
import cca.ruian_puller.download.VdpClient;
import cca.ruian_puller.utils.Consts;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

@SpringBootApplication
@Log4j2
public class RuianPullerApplication implements CommandLineRunner {
    private final ConfigReader configReader;
    private final VdpClient vdpClient;

    @Autowired
    public RuianPullerApplication(ConfigReader configReader, VdpClient vdpClient) {
        this.configReader = configReader;
        this.vdpClient = vdpClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(RuianPullerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        String configPath = Consts.CONFIG_FILE;
        AppConfig appConfig = readConfig(configPath);
        if (appConfig != null) {
            log.info("Configuration loaded successfully.");
        } else {
            log.error("Configuration loading failed.");
            return;
        }

        // print the configuration
        log.info("Database type: {}", appConfig.getDatabase().getType());
        log.info("Database URL: {}", appConfig.getDatabase().getUrl());
        log.info("Database username: {}", appConfig.getDatabase().getUsername());
        log.info("Database password: {}", appConfig.getDatabase().getPassword());

        // DB Connect and send test INSERT query
        if (!DBCommunication.connect(
                appConfig.getDatabase().getUrl(),
                appConfig.getDatabase().getUsername(),
                appConfig.getDatabase().getPassword())) {
            return;
        }
//        ArrayList<String> values = new ArrayList<>(Arrays.asList("1", "2", "3023", "45"));
//        if (DBCommunication.sendQuery(SQLConst.INSERT, "cisladomovni", "cislo1, cislo2", values)) {
//            log.info("Query sent successfully.");
//        } else {
//            log.error("Query send failed.");
//        }

        // Download the data
        vdpClient.zpracovatStatAzZsj(inputStream -> {
            log.info("Data downloaded successfully.");
        });
    }

    private AppConfig readConfig(String path) {
        URL resource = getClass().getClassLoader().getResource(path);
        if (resource == null) {
            log.error("Configuration file '{}' not found.", path);
            return null;
        }

        return configReader.readConfig(resource.getPath());
    }
}
