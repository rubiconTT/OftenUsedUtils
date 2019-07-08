package com.dg.jw.quartz;

import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SyncTask implements Job {

	//	private static Scheduler scheduler = null;
		private static Logger log=Logger.getLogger(SyncTask.class.getName());

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			
			log.info("Job : Contacts Sync executing at " + new Date());
			try {
				//同步程序
			} catch (Exception e) {
				log.error(e.getMessage());
				JobExecutionException e2 =   
		                new JobExecutionException(e);  
		            e2.setRefireImmediately(true);  
		            throw e2;  
			}
		}

	

}
