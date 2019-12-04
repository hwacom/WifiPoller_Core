package com.snmp.poller.dao.impl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v1.util.ArrayUtils;
import com.snmp.poller.dao.JdbcDAO;
import com.snmp.poller.enums.MsgLevel;
import com.snmp.poller.model.MibOidSetting;
import com.snmp.poller.model.UserCurrentInfo;
import com.snmp.poller.utils.impl.CommonUtils;

public class AccessDAOImpl extends BaseDAOImpl implements JdbcDAO {
	private static Logger log = LoggerFactory.getLogger(AccessDAOImpl.class);

	@Override
	public List<UserCurrentInfo> findUserCurrentInfo() {
		try {
			List<UserCurrentInfo> entities = (List<UserCurrentInfo>)excuteQuery(" SELECT * FROM USER_CURRENT_INFO ORDER BY USER_MAC_ADDR, AP_SLOT_ID, AP_NAME, SSID_NAME ");
			return entities;

		} catch (Exception e) {
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, AccessDAOImpl.class, e.toString());
			return null;
		}
	}

	@Override
	public List<MibOidSetting> findMibOidSetting() {
		try {
			List<MibOidSetting> entities = (List<MibOidSetting>)excuteQuery(" SELECT * FROM MIB_OID_SETTING ");
			return entities;

		} catch (Exception e) {
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, AccessDAOImpl.class, e.toString());
			return null;
		}
	}

	@Override
	public Integer insertUserCurrentInfo(UserCurrentInfo entity) {
		try {
			StringBuffer sql = new StringBuffer();
			sql.append(" INSERT INTO USER_CURRENT_INFO ")
			.append("    ( USER_MAC_ADDR, AP_SLOT_ID, USER_IP_ADDR, AP_MAC_ADDR, AP_NAME, SSID_NAME, CHANNEL, SIGNAL_STRENGTH, ")
			.append("      NOISE_LEVEL, SNR, FIRST_CONNECT_TIME, LAST_DISCONNECT_TIME, SEND_DATA, ")
			.append("      RECEIVE_DATA, AVG_SEND_DATA, AVG_RECEIVE_DATA, CREATE_TIME, CREATE_BY, ")
			.append("	  UPDATE_TIME, UPDATE_BY, USER_MAC_ADDR_DECIMAL, AP_MAC_ADDR_DECIMAL, SEND_DATA_BYTES, RECEIVE_DATA_BYTES, ")
			.append("     YYYYMMDD ) ")
			.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");

			PreparedStatement st = getConnect().prepareStatement(sql.toString());
			st.setString(1, entity.getUserMacAddr());
			st.setInt(2, entity.getApSlotId());
			st.setString(3, entity.getUserIpAddr());
			st.setString(4, entity.getApMacAddr());
			st.setString(5, entity.getApName());
			st.setString(6, entity.getSsidName());
			st.setInt(7, entity.getChannel() == null ? 999 : entity.getChannel());
			st.setInt(8, entity.getSignalStrength());
			st.setInt(9, entity.getNoiseLevel());
			st.setInt(10, entity.getSnr());
			st.setDate(11, new Date(entity.getFirstConnectTime().getTime()));
			st.setDate(12, new Date(entity.getLastDisconnectTime().getTime()));
			st.setLong(13, entity.getSendData());
			st.setLong(14, entity.getReceiveData());
			st.setDouble(15, entity.getAvgSendData());
			st.setDouble(16, entity.getAvgReceiveData());
			st.setDate(17, new Date(entity.getCreateTime().getTime()));
			st.setString(18, entity.getCreateBy());
			st.setDate(19, new Date(entity.getUpdateTime().getTime()));
			st.setString(20, entity.getUpdateBy());
			st.setString(21, entity.getUserMacAddrDecimal());
			st.setString(22, entity.getApMacAddrDecimal());
			st.setLong(23, entity.getSendDataBytes());
			st.setLong(24, entity.getReceiveDataBytes());
			st.setString(25, entity.getYyyymmdd());
			return st.executeUpdate();

		} catch (Exception e) {
			log.error("insert failed! entity: {}" + ArrayUtils.toString(entity.toArray()));
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, AccessDAOImpl.class, e.toString());
			return 0;
		}
	}

	@Override
	public void cleanAllUserCurrentInfo() {
		try {
			String sql = "DELETE FROM USER_CURRENT_INFO";
			excuteUpdate(sql);

		} catch (Exception e) {
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, AccessDAOImpl.class, e.toString());
		}
	}

	@Override
	public UserCurrentInfo findUserCurrentInfoByKeys(String userMacAddr, Integer apSlotId, String apName, String ssidName) {
		try {
			StringBuffer sql = new StringBuffer();
			sql.append(" SELECT * FROM USER_CURRENT_INFO WHERE 1=1 ")
			.append(" AND USER_MAC_ADDR = ? ")
			.append(" AND AP_SLOT_ID = ? ")
			.append(" AND AP_NAME = ? ")
			.append(" AND SSID_NAME = ? ");
			Object[] queryValues = new Object[] {userMacAddr, apSlotId, apName, ssidName};

			List<UserCurrentInfo> entities = (List<UserCurrentInfo>)excuteQuery(sql.toString(), queryValues);
			return entities != null ? entities.get(0) : null;

		} catch (Exception e) {
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, AccessDAOImpl.class, e.toString());
			return null;
		}
	}

	@Override
	public synchronized Integer updateUserCurrentInfo(UserCurrentInfo entity) {
		try {
			StringBuffer sql = new StringBuffer();
			sql.append(" UPDATE USER_CURRENT_INFO ")
			.append(" SET CHANNEL = ? , SIGNAL_STRENGTH = ? , NOISE_LEVEL = ? , SNR = ? , LAST_DISCONNECT_TIME = ? , ")
			.append("     SEND_DATA = ? , RECEIVE_DATA = ? , AVG_SEND_DATA = ? , AVG_RECEIVE_DATA = ?, UPDATE_TIME = ?, ")
			.append("     SEND_DATA_BYTES = ? , RECEIVE_DATA_BYTES = ? ")
			.append(" WHERE 1=1 ")
			.append(" AND YYYYMMDD = ? AND USER_MAC_ADDR = ? AND AP_SLOT_ID = ? AND AP_MAC_ADDR = ? ");

			PreparedStatement st = getConnect().prepareStatement(sql.toString());
			st.setInt(1, entity.getChannel());										// CHANNEL
			st.setInt(2, entity.getSignalStrength());								// SIGNAL_STRENGTH
			st.setInt(3, entity.getNoiseLevel());									// NOISE_LEVEL
			st.setInt(4, entity.getSnr());											// SNR
			st.setDate(5, new Date(entity.getLastDisconnectTime().getTime()));		// LAST_DISCONNECT_TIME
			st.setLong(6, entity.getSendData());									// SEND_DATA
			st.setLong(7, entity.getReceiveData());									// RECEIVE_DATA
			st.setDouble(8, entity.getAvgSendData());								// AVG_SEND_DATA
			st.setDouble(9, entity.getAvgReceiveData());							// AVG_RECEIVE_DATA
			st.setDate(10, new Date(entity.getUpdateTime().getTime()));				// UPDATE_TIME
			st.setLong(11, entity.getSendDataBytes());								// SEND_DATA_BYTES
			st.setLong(12, entity.getReceiveDataBytes());							// RECEIVE_DATA_BYTES
			st.setString(13, entity.getYyyymmdd());									// YYYYMMDD
			st.setString(14, entity.getUserMacAddr());								// USER_MAC_ADDR
			st.setInt(15, entity.getApSlotId());									// AP_SLOT_ID
			st.setString(16, entity.getApMacAddr());								// AP_MAC_ADDR
			return st.executeUpdate();

		} catch (Exception e) {
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, AccessDAOImpl.class, e.toString());
			return 0;
		}
	}

	@Override
	public void doCommit() {
		super.commit();
	}

	@Override
	public List<UserCurrentInfo> findSpecifyDateUserCurrentInfo(String yyyyMMdd) {
		try {
			StringBuffer sql = new StringBuffer();
			sql.append(" SELECT * FROM USER_CURRENT_INFO WHERE 1=1 ")
			.append(" AND YYYYMMDD = ? ");
			Object[] queryValues = new Object[] {yyyyMMdd};

			List<UserCurrentInfo> entities = (List<UserCurrentInfo>)excuteQuery(sql.toString(), queryValues);
			return entities;

		} catch (Exception e) {
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, AccessDAOImpl.class, e.toString());
			return null;
		}
	}
}
