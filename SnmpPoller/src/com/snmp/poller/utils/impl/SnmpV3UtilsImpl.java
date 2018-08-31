package com.snmp.poller.utils.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.VariableBinding;

import com.snmp.poller.utils.SnmpUtils;

public class SnmpV3UtilsImpl implements SnmpUtils {
	private static Logger log = LoggerFactory.getLogger(SnmpV3UtilsImpl.class);

	@Override
	public boolean connect(final String udpAddress, final String community) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean login(String account, String password) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, List<VariableBinding>> pollData(List<String> oids) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean logout() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean disconnect() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

}
