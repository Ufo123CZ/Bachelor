package cca.ruian_puller.scheduler;

import lombok.extern.log4j.Log4j2;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class JobCompletionListener implements JobListener {

    @Autowired
    private Scheduler scheduler;

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {

    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {

    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        JobKey jobKey = context.getJobDetail().getKey();
        try {
            if (jobKey.equals(JobKey.jobKey("additionJob"))) {
                scheduler.triggerJob(JobKey.jobKey("additionJob"));
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}
