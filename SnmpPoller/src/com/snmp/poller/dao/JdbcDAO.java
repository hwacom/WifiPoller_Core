package com.snmp.poller.dao;

import java.util.List;

import com.snmp.poller.model.MibOidSetting;
import com.snmp.poller.model.UserCurrentInfo;

public interface JdbcDAO {

	public List<UserCurrentInfo> findUserCurrentInfo();

	public List<UserCurrentInfo> findSpecifyDateUserCurrentInfo(String yyyyMMdd);

	public UserCurrentInfo findUserCurrentInfoByKeys(String userMacAddr, Integer apSlotId, String apName, String ssidName);

	public List<MibOidSetting> findMibOidSetting();

	public Integer insertUserCurrentInfo(UserCurrentInfo entity);

	public Integer updateUserCurrentInfo(UserCurrentInfo entity);

	public void cleanAllUserCurrentInfo();

	public void doCommit();
}
