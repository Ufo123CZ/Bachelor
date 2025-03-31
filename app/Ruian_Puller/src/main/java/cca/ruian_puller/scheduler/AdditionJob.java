package cca.ruian_puller.scheduler;

import cca.ruian_puller.RuianPullerApplication;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdditionJob implements Job {

    @Autowired
    private RuianPullerApplication ruianPullerApplication;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Implement the procedure to run based on cron expression
//        ruianPullerApplication.runCronProcedure();
    }
}