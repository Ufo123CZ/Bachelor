package cca.ruian_puller;

import cca.ruian_puller.download.VdpClient;
import cca.ruian_puller.download.VdpParser;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

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

//        List<Integer> vuscCodes = List.of(19, 27, 35, 43, 51, 60, 78, 86, 94, 108, 116, 124, 132, 141);
//
//        // Download data for each region
//        for (Integer vuscCode : vuscCodes) {
//            List<String> links = vdpClient.getListLinksObce(vuscCode);
//            vdpClient.downloadFilesFromLinks(links, inputStream -> {
//                log.info("Data downloaded successfully for region code: {}", vuscCode);
//                // Process the data
//                log.info("Data processing started for region code: {}", vuscCode);
//                vdpParser.processFile(inputStream);
//            });
//        }

        long timeEnd = System.currentTimeMillis();
        // Print the time it took to process the data
        long totalTime = timeEnd - timeStart;
        long seconds = totalTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        if (Math.floor((double)hours) > 0) {
            log.info("Data processing finished in {} hours {} minutes {} seconds", Math.floor((double)hours), Math.floor((double)minutes), Math.floor((double)seconds));
        } else if (Math.floor((double)minutes) > 0) {
            log.info("Data processing finished in {} minutes {} seconds", Math.floor((double)minutes), Math.floor((double)seconds));
        } else {
            log.info("Data processing finished in {} seconds", Math.floor((double)seconds));
        }
    }
}