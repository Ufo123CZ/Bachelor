package cca.ruian_puller.scheduler;

import cca.ruian_puller.RuianPullerApplication;
import lombok.extern.log4j.Log4j2;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class AdditionJob implements Job {

    @Autowired
    private RuianPullerApplication ruianPullerApplication;

    /**
     * Executes the job to download and process data for additions.
     *
     * @param context the job execution context
     */
    @Override
    public void execute(JobExecutionContext context) {
        if (RuianPullerApplication.semaphore.tryAcquire()) {
            log.info("===================================================");
            log.info("All requirements are met, starting the job.");
        }

        log.info("Downloading data for additions.");
        ruianPullerApplication.getVdpClient().getAdditions(inputStream -> {
            log.info("Data downloaded successfully.");
            log.info("Data processing started.");
            ruianPullerApplication.getVdpParser().processFile(inputStream);
            log.info("Data processing finished.");
        });
        RuianPullerApplication.semaphore.release();
        log.info("===================================================");
    }
}