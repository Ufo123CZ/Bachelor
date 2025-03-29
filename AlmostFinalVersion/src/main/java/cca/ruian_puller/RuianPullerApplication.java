package cca.ruian_puller;

import cca.ruian_puller.config.AppConfig;
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
    private final AppConfig appConfig;

    @Autowired
    public RuianPullerApplication(VdpClient vdpClient, VdpParser vdpParser, AppConfig appConfig) {
        this.vdpClient = vdpClient;
        this.vdpParser = vdpParser;
        this.appConfig = appConfig;
    }

    public static void main(String[] args) {
        SpringApplication.run(RuianPullerApplication.class, args);
    }

    @Override
    public void run(String... args) {

        // Initialize the Stat Az Zsj
        if (appConfig.isSkipInitialRun()) {
            log.info("Skipping initial run.");
        } else {
            initStatAzZsj();
        }

        // Initialize the regions
        for (int vuscCode : appConfig.getVuscCodes().keySet())
            initRegion(vuscCode, appConfig.getVuscCodes().get(vuscCode));
    }


    private void initStatAzZsj() {
        log.info("================================================");
        log.info("Downloading data for Stat Az Zsj.");
        long timeStart = System.currentTimeMillis();
        vdpClient.zpracovatStatAzZsj(inputStream -> {
            log.info("Data downloaded successfully.");
            // Process the data
            log.info("Data processing started.");
            vdpParser.processFile(inputStream);
        });
        long timeEnd = System.currentTimeMillis();
        // Print the time it took to process the data
        printTimeToFinish("Stat Az Zsj", timeEnd - timeStart);
    }

    private void initRegion(int vuscCode, String vuscName) {
        long timeStart = System.currentTimeMillis();
        log.info("================================================");
        log.info("Downloading data for Vusc {}", vuscName);
        List<String> links = vdpClient.getListLinksObce(vuscCode);
        vdpClient.downloadFilesFromLinks(links, vdpParser::processFile);
        long timeEnd = System.currentTimeMillis();
        // Print the time it took to process the data
        printTimeToFinish("Region " + vuscCode, timeEnd - timeStart);
    }

    private void printTimeToFinish(String what, long timeStartToEnd) {
        long seconds = timeStartToEnd / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (Math.floor((double)hours) > 0) {
            log.info("{} finished in {} hours {} minutes {} seconds", what, (int)Math.floor((double)hours), (int)Math.floor((double)(minutes - hours * 60)), (int)Math.floor((double)(seconds - hours * minutes * 60)));
        } else if (Math.floor((double)minutes) > 0) {
            log.info("{} finished in {} minutes {} seconds", what, (int)Math.floor((double)minutes), (int)Math.floor((double)(seconds - minutes * 60)));
        } else {
            log.info("{} finished in {} seconds", what, (int)Math.floor((double)seconds));
        }
    }
}