package cca.ruian_puller;

import cca.ruian_puller.config.ConfigReader;
import cca.ruian_puller.download.VdpClient;
import cca.ruian_puller.download.VdpParser;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Log4j2
public class RuianPullerApplication implements CommandLineRunner {
    private final ConfigReader configReader;
    private final VdpClient vdpClient;
    private final VdpParser vdpParser;

    @Autowired
    public RuianPullerApplication(ConfigReader configReader, VdpClient vdpClient, VdpParser vdpParser) {
        this.configReader = configReader;
        this.vdpClient = vdpClient;
        this.vdpParser = vdpParser;
    }

    public static void main(String[] args) {
        SpringApplication.run(RuianPullerApplication.class, args);
    }

    @Override
    public void run(String... args) {
//        String configPath = Consts.CONFIG_FILE;
//        AppConfig appConfig = readConfig(configPath);
//        if (appConfig != null) {
//            log.info("Configuration loaded successfully.");
//        } else {
//            log.error("Configuration loading failed.");
//            return;
//        }

        // print the configuration
//        log.info("Database type: {}", appConfig.getDatabase().getType());
//        log.info("Database URL: {}", appConfig.getDatabase().getUrl());
//        log.info("Database username: {}", appConfig.getDatabase().getUsername());
//        log.info("Database password: {}", appConfig.getDatabase().getPassword());

        // Download the data, process it and save it to the database
        vdpClient.zpracovatStatAzZsj(inputStream -> {
            log.info("Data downloaded successfully.");
            // Process the data
            log.info("Data processing started.");
            vdpParser.processFile(inputStream);
        });
    }
//    private AppConfig readConfig(String path) {
//        URL resource = getClass().getClassLoader().getResource(path);
//        if (resource == null) {
//            log.error("Configuration file '{}' not found.", path);
//            return null;
//        }
//
//        return configReader.readConfig(resource.getPath());
//    }
}
