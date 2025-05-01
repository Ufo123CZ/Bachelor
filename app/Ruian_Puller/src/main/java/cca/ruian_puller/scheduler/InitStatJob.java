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
    @Autowired
    private Scheduler scheduler;

    /**
     * Executes the job to download and process data for Stat Az Zsj.
     *
     * @param context the job execution context
     */
    @Override
    public void execute(JobExecutionContext context) {
        log.info("===================================================");
        log.info("Starting the first job: {}", context.getJobDetail().getKey().getName());
        // Skip initial run if needed
        if (ruianPullerApplication.getAppConfig().isSkipInitialRunStat()) {
            log.info("Skipping initial run.");
        } else {
            log.info("Downloading data for Stat Az Zsj.");
            ruianPullerApplication.getVdpClient().zpracovatStatAzZsj(inputStream -> {
                log.info("Data downloaded successfully.");
                log.info("Data processing started.");
                ruianPullerApplication.getVdpParser().processFile(inputStream);
                log.info("Data processing finished.");
            });
        }

        // Trigger the next job
        try {
            scheduler.triggerJob(new JobKey("initRegionJob"));
        } catch (Exception e) {
            log.error("Error while chaining jobs.", e);
        }
    }
}