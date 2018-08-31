package com.snmp.poller.utils;

import java.util.List;
import java.util.Map;

import org.snmp4j.smi.VariableBinding;

public interface SnmpUtils {

	public boolean connect(final String udpAddress, final String community) throws Exception;

	public boolean login(final String account, final String password) throws Exception;

	public Map<String, List<VariableBinding>> pollData(List<String> oids) throws Exception;

	public boolean logout() throws Exception;

	public boolean disconnect() throws Exception;
}
