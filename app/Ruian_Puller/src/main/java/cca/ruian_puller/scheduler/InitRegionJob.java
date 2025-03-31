package cca.ruian_puller.scheduler;

import cca.ruian_puller.RuianPullerApplication;
import lombok.extern.log4j.Log4j2;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Log4j2
public class InitRegionJob implements Job {

    @Autowired
    private RuianPullerApplication ruianPullerApplication;
//    @Autowired
//    private Scheduler scheduler;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Skip initial run if needed
        if (ruianPullerApplication.getAppConfig().isSkipInitialRun()) {
            log.info("Skipping initial run.");
        } else {
            ruianPullerApplication.getAppConfig().getVuscCodes().forEach(this::sigleRegion);
        }
        // Trigger the next job
//        try {
//            scheduler.triggerJob(new JobKey("additionJob"));
//        } catch (SchedulerException e) {
//            throw new RuntimeException(e);
//        }
    }

    private void sigleRegion(int vuscCode, String vuscName) {
        log.info("================================================");
        log.info("Downloading data for Vusc {}", vuscName);
        List<String> links = ruianPullerApplication.getVdpClient().getListLinksObce(vuscCode);
        ruianPullerApplication.getVdpClient().downloadFilesFromLinks(
                links, ruianPullerApplication.getVdpParser()::processFile
        );
    }
}