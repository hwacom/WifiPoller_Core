package com.snmp.poller.service;

public interface PollerService {

	public final String TRIGGER_SNMP = "TRIGGER_SNMP";
	public final String TRIGGER_JDBC = "TRIGGER_JDBC";
	public final String JOB_OF_SNMP_POLLER = "SNMP_POLLER";
	public final String JOB_OF_JDBC_POLLER = "JDBC_POLLER";

	public void startUpJobs();

	public void pauseJobs();

	public void pollerSnmpInfo();

	public void pollerJdbcData();
}
