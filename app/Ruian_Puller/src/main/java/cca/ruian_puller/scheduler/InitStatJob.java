package cca.ruian_puller.scheduler;

import cca.ruian_puller.RuianPullerApplication;
import lombok.extern.log4j.Log4j2;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class InitStatJob implements Job {

    @Autowired
    private RuianPullerApplication ruianPullerApplication;
//    @Autowired
//    private Scheduler scheduler;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
//        ruianPullerApplication.initStatAzZsj();
        if (ruianPullerApplication.getAppConfig().isSkipInitialRun()) {
            log.info("Skipping initial run.");
        } else {
            log.info("================================================");
            log.info("Downloading data for Stat Az Zsj.");
            ruianPullerApplication.getVdpClient().zpracovatStatAzZsj(inputStream -> {
                log.info("Data downloaded successfully.");
                // Process the data
                log.info("Data processing started.");
                ruianPullerApplication.getVdpParser().processFile(inputStream);
            });
        }

//        // Trigger the next job
//        try {
//            scheduler.triggerJob(new JobKey("initRegionJob"));
//        } catch (Exception e) {
//            log.error("Error while chaining jobs.", e);
//        }
    }
}