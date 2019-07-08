package com.dg.jw.quartz;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.impl.StdSchedulerFactory;

public class SyncTaskCronTrigger extends Thread {
	
private static Logger log=Logger.getLogger(SyncTaskCronTrigger.class.getName());
	
	private static Scheduler scheduler=null;

	@Override
	public void run() {
		SchedulerFactory sf = new StdSchedulerFactory();
		  try {
			  scheduler = sf.getScheduler();
			  log.info("------- Initialization Complete --------");
	
			  log.info("------- Scheduling Jobs ----------------");
	
			  //job 1 每天晚上03:00执行
			  JobDetail job = newJob(SyncTask.class).withIdentity("job1", "group1").build();
	
			  //CronTrigger trigger = newTrigger().withIdentity("trigger1", "group1").withSchedule(cronSchedule("0 0,10,20,30,40,50 * * * ?")).build();
			  CronTrigger trigger = newTrigger().withIdentity("trigger1", "group1").withSchedule(cronSchedule("0 0 3 * * ?")).build();
	
			  Date ft = scheduler.scheduleJob(job, trigger);
			  log.info(job.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: " + trigger.getCronExpression());
	
			  scheduler.start();
	
			  log.info("------- Started Scheduler -----------------");
	
			  SchedulerMetaData metaData = scheduler.getMetaData();
			  log.info("已执行任务数： " + metaData.getNumberOfJobsExecuted());
		
		  } catch (Exception e) {
		      log.error(e.getMessage());
	 }
	}
	
	public static void stopScheduler(){
		  
		if(scheduler != null){
			  try {
				  scheduler.shutdown();
				log.info("Sync Scheduler is shutdown!");
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
		  }else{
			  log.info("Sync Scheduler is null!");
		  }
	  }

}
