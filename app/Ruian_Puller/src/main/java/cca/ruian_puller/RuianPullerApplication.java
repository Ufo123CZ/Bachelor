package cca.ruian_puller;

import cca.ruian_puller.download.VdpClient;
import cca.ruian_puller.download.VdpParser;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.sql.Time;
import java.time.LocalDateTime;

@SpringBootApplication
@Log4j2
public class RuianPullerApplication implements CommandLineRunner {
    private final VdpClient vdpClient;
    private final VdpParser vdpParser;

    @Autowired
    public RuianPullerApplication(VdpClient vdpClient, VdpParser vdpParser) {
        this.vdpClient = vdpClient;
        this.vdpParser = vdpParser;
    }

    public static void main(String[] args) {
        SpringApplication.run(RuianPullerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        long timeStart = System.currentTimeMillis();
        vdpClient.zpracovatStatAzZsj(inputStream -> {
            log.info("Data downloaded successfully.");
            // Process the data
            log.info("Data processing started.");
            vdpParser.processFile(inputStream);
        });
        long timeEnd = System.currentTimeMillis();
        // Print the time it took to process the data
        Time time = new Time(timeEnd - timeStart);
        log.info("Data processing finished in {}", time.toLocalTime());
    }
}