package cca.ruian_puller;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.download.VdpClient;
import cca.ruian_puller.download.VdpParser;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.Semaphore;

@Getter
@SpringBootApplication
@Log4j2
public class RuianPullerApplication implements CommandLineRunner {
    private final VdpClient vdpClient;
    private final VdpParser vdpParser;
    private final AppConfig appConfig;

    // Semaphore to control the flow of jobs
    public static Semaphore semaphore = new Semaphore(0);

    @Autowired
    private Scheduler scheduler;

    /**
     * Constructor for RuianPullerApplication.
     *
     * @param vdpClient  the VdpClient instance
     * @param vdpParser  the VdpParser instance
     * @param appConfig  the AppConfig instance
     */
    @Autowired
    public RuianPullerApplication(VdpClient vdpClient, VdpParser vdpParser, AppConfig appConfig) {
        this.vdpClient = vdpClient;
        this.vdpParser = vdpParser;
        this.appConfig = appConfig;
    }

    /**
     * Main method to run the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(RuianPullerApplication.class, args);
    }

    @Override
    public void run(String... args){}
}