package cca.ruian_puller.scheduler;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
public class QuartzConfig {

    @Bean()
    public Scheduler scheduler(SchedulerFactoryBean factory) throws SchedulerException {
        Scheduler scheduler = factory.getScheduler();
        scheduler.start();
        return scheduler;
    }

    //region JobDetailFactoryBean
    @Bean
    public JobDetailFactoryBean initStatJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(InitStatJob.class);
        factoryBean.setDescription("Job to run initStat");
        factoryBean.setDurability(true);
        factoryBean.setName("initStatJob");
        return factoryBean;
    }

    @Bean
    public JobDetailFactoryBean initRegionJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(InitRegionJob.class);
        factoryBean.setDescription("Job to run initRegion");
        factoryBean.setDurability(true);
        factoryBean.setName("initRegionJob");
        return factoryBean;
    }

    @Bean
    public JobDetailFactoryBean additionJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(AdditionJob.class);
        factoryBean.setDescription("Job to run based on cron expression");
        factoryBean.setDurability(true);
        factoryBean.setName("additionJob");
        return factoryBean;
    }
    //endregion

    //region TriggerFactoryBean
    @Bean
    public SimpleTriggerFactoryBean initStatTrigger(@Qualifier("initStatJobDetail") JobDetailFactoryBean initStatJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(initStatJobDetail.getObject());
        factoryBean.setRepeatInterval(0);
        factoryBean.setRepeatCount(0);
        return factoryBean;
    }

//    @Bean
//    public SimpleTriggerFactoryBean initRegionTrigger(@Qualifier("initRegionJobDetail") JobDetailFactoryBean initRegionJobDetail) {
//        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
//        factoryBean.setJobDetail(initRegionJobDetail.getObject());
//        factoryBean.setRepeatInterval(0);
//        factoryBean.setRepeatCount(0);
//        return factoryBean;
//    }
//
//    @Bean
//    public CronTriggerFactoryBean additionTrigger(@Qualifier("additionJobDetail") JobDetailFactoryBean additionJobDetail, AppConfig appConfig) {
//        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
//        factoryBean.setJobDetail(additionJobDetail.getObject());
//        factoryBean.setCronExpression(String.valueOf(appConfig.getCronExpression()));
//        return factoryBean;
//    }
    //endregion
}