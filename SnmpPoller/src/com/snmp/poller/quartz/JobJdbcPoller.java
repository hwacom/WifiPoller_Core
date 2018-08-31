package com.snmp.poller.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.snmp.poller.enums.MsgLevel;
import com.snmp.poller.service.PollerService;
import com.snmp.poller.service.impl.PollerServiceImpl;
import com.snmp.poller.utils.impl.CommonUtils;

public class JobJdbcPoller implements Job {
	private static Logger log = LoggerFactory.getLogger(JobJdbcPoller.class);

	private static PollerService pollerService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			if (pollerService == null) {
				pollerService = new PollerServiceImpl();
			}

			pollerService.pollerJdbcData();

		} catch (Exception e) {
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, JobJdbcPoller.class, e.toString());
		}
	}

}
