package com.snmp.poller.model;

import java.util.Date;

import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.Id;
import org.javers.core.metamodel.annotation.TypeName;

import com.snmp.poller.Env;

@TypeName("UserCurrentInfo")
public class UserCurrentInfo {

	@Id
	private String yyyymmdd;
	@Id
	private String userMacAddr;
	@Id
	private Integer apSlotId;
	@Id
	private String apName;
	@Id
	private String ssidName;

	private String apMacAddr;
	private String userIpAddr;
	private Integer channel;
	private Integer signalStrength;
	@DiffIgnore
	private Integer noiseLevel;
	private Integer snr;
	@DiffIgnore
	private Date firstConnectTime;
	@DiffIgnore
	private Date lastDisconnectTime;
	private Long sendDataBytes;
	@DiffIgnore
	private Long sendData;
	private Long receiveDataBytes;
	@DiffIgnore
	private Long receiveData;
	@DiffIgnore
	private Double avgSendData;
	@DiffIgnore
	private Double avgReceiveData;
	private String userMacAddrDecimal;
	private String apMacAddrDecimal;
	@DiffIgnore
	private Date createTime;
	@DiffIgnore
	private String createBy;
	@DiffIgnore
	private Date updateTime;
	@DiffIgnore
	private String updateBy;

	public String[] toArray() {
		String[] rowData = new String[Env.TABLE_HEADER_COLUMN_NAMES.length];
		rowData[0] = "";
		rowData[1] = getYyyymmdd();
		rowData[2] = getUserMacAddr();
		rowData[3] = getApSlotId() != null ? getApSlotId().toString() : Env.SYMBOL_OF_EMPTY_DATA;
		rowData[4] = getUserIpAddr();
		rowData[5] = getApMacAddr();
		rowData[6] = getApName();
		rowData[7] = getSsidName();
		rowData[8] = getChannel() != null ? getChannel().toString() : Env.SYMBOL_OF_EMPTY_DATA;
		rowData[9] = getSignalStrength() != null ? getSignalStrength().toString().concat(" ").concat(Env.UNIT_OF_SIGNAL) : Env.SYMBOL_OF_EMPTY_DATA;
		rowData[10] = getNoiseLevel() != null ? getNoiseLevel().toString().concat(" ").concat(Env.UNIT_OF_SIGNAL) : Env.SYMBOL_OF_EMPTY_DATA;
		rowData[11] = getSnr() != null ? getSnr().toString().concat(" ").concat(Env.UNIT_OF_SIGNAL) : Env.SYMBOL_OF_EMPTY_DATA;
		rowData[12] = getFirstConnectTime() != null ? Env.FORMAT_YYYYMMDDHHMISS.format(getFirstConnectTime()) : Env.SYMBOL_OF_EMPTY_DATA;
		rowData[13] = getLastDisconnectTime() != null ? Env.FORMAT_YYYYMMDDHHMISS.format(getLastDisconnectTime()) : Env.SYMBOL_OF_EMPTY_DATA;
		rowData[14] = getSendData() != null ? Env.df.format(getSendData()).concat(" ").concat(Env.UNIT_OF_TRANSFER) : Env.SYMBOL_OF_EMPTY_DATA;
		rowData[15] = getReceiveData() != null ? Env.df.format(getReceiveData()).concat(" ").concat(Env.UNIT_OF_TRANSFER) : Env.SYMBOL_OF_EMPTY_DATA;
		rowData[16] = getAvgSendData() != null ? Env.df_avg.format(getAvgSendData()).concat(" ").concat(Env.UNIT_OF_AVG_TRANSFER) : Env.SYMBOL_OF_EMPTY_DATA;
		rowData[17] = getAvgReceiveData() != null ? Env.df_avg.format(getAvgReceiveData()).concat(" ").concat(Env.UNIT_OF_AVG_TRANSFER) : Env.SYMBOL_OF_EMPTY_DATA;
		return rowData;
	}

	public UserCurrentInfo() {
		super();
	}

	public UserCurrentInfo(String yyyymmdd, String userMacAddr, Integer apSlotId, String apName, String ssidName,
			String apMacAddr, String userIpAddr, Integer channel, Integer signalStrength, Integer noiseLevel,
			Integer snr, Date firstConnectTime, Date lastDisconnectTime, Long sendDataBytes, Long sendData,
			Long receiveDataBytes, Long receiveData, Double avgSendData, Double avgReceiveData,
			String userMacAddrDecimal, String apMacAddrDecimal, Date createTime, String createBy, Date updateTime,
			String updateBy) {
		super();
		this.yyyymmdd = yyyymmdd;
		this.userMacAddr = userMacAddr;
		this.apSlotId = apSlotId;
		this.apName = apName;
		this.ssidName = ssidName;
		this.apMacAddr = apMacAddr;
		this.userIpAddr = userIpAddr;
		this.channel = channel;
		this.signalStrength = signalStrength;
		this.noiseLevel = noiseLevel;
		this.snr = snr;
		this.firstConnectTime = firstConnectTime;
		this.lastDisconnectTime = lastDisconnectTime;
		this.sendDataBytes = sendDataBytes;
		this.sendData = sendData;
		this.receiveDataBytes = receiveDataBytes;
		this.receiveData = receiveData;
		this.avgSendData = avgSendData;
		this.avgReceiveData = avgReceiveData;
		this.userMacAddrDecimal = userMacAddrDecimal;
		this.apMacAddrDecimal = apMacAddrDecimal;
		this.createTime = createTime;
		this.createBy = createBy;
		this.updateTime = updateTime;
		this.updateBy = updateBy;
	}

	public String getYyyymmdd() {
		return yyyymmdd;
	}

	public void setYyyymmdd(String yyyymmdd) {
		this.yyyymmdd = yyyymmdd;
	}

	public String getUserMacAddr() {
		return userMacAddr;
	}

	public void setUserMacAddr(String userMacAddr) {
		this.userMacAddr = userMacAddr;
	}

	public Integer getApSlotId() {
		return apSlotId;
	}

	public void setApSlotId(Integer apSlotId) {
		this.apSlotId = apSlotId;
	}

	public String getApName() {
		return apName;
	}

	public void setApName(String apName) {
		this.apName = apName;
	}

	public String getSsidName() {
		return ssidName;
	}

	public void setSsidName(String ssidName) {
		this.ssidName = ssidName;
	}

	public String getApMacAddr() {
		return apMacAddr;
	}

	public void setApMacAddr(String apMacAddr) {
		this.apMacAddr = apMacAddr;
	}

	public String getUserIpAddr() {
		return userIpAddr;
	}

	public void setUserIpAddr(String userIpAddr) {
		this.userIpAddr = userIpAddr;
	}

	public Integer getChannel() {
		return channel;
	}

	public void setChannel(Integer channel) {
		this.channel = channel;
	}

	public Integer getSignalStrength() {
		return signalStrength;
	}

	public void setSignalStrength(Integer signalStrength) {
		this.signalStrength = signalStrength;
	}

	public Integer getNoiseLevel() {
		return noiseLevel;
	}

	public void setNoiseLevel(Integer noiseLevel) {
		this.noiseLevel = noiseLevel;
	}

	public Integer getSnr() {
		return snr;
	}

	public void setSnr(Integer snr) {
		this.snr = snr;
	}

	public Date getFirstConnectTime() {
		return firstConnectTime;
	}

	public void setFirstConnectTime(Date firstConnectTime) {
		this.firstConnectTime = firstConnectTime;
	}

	public Date getLastDisconnectTime() {
		return lastDisconnectTime;
	}

	public void setLastDisconnectTime(Date lastDisconnectTime) {
		this.lastDisconnectTime = lastDisconnectTime;
	}

	public Long getSendDataBytes() {
		return sendDataBytes;
	}

	public void setSendDataBytes(Long sendDataBytes) {
		this.sendDataBytes = sendDataBytes;
	}

	public Long getSendData() {
		return sendData;
	}

	public void setSendData(Long sendData) {
		this.sendData = sendData;
	}

	public Long getReceiveDataBytes() {
		return receiveDataBytes;
	}

	public void setReceiveDataBytes(Long receiveDataBytes) {
		this.receiveDataBytes = receiveDataBytes;
	}

	public Long getReceiveData() {
		return receiveData;
	}

	public void setReceiveData(Long receiveData) {
		this.receiveData = receiveData;
	}

	public Double getAvgSendData() {
		return avgSendData;
	}

	public void setAvgSendData(Double avgSendData) {
		this.avgSendData = avgSendData;
	}

	public Double getAvgReceiveData() {
		return avgReceiveData;
	}

	public void setAvgReceiveData(Double avgReceiveData) {
		this.avgReceiveData = avgReceiveData;
	}

	public String getUserMacAddrDecimal() {
		return userMacAddrDecimal;
	}

	public void setUserMacAddrDecimal(String userMacAddrDecimal) {
		this.userMacAddrDecimal = userMacAddrDecimal;
	}

	public String getApMacAddrDecimal() {
		return apMacAddrDecimal;
	}

	public void setApMacAddrDecimal(String apMacAddrDecimal) {
		this.apMacAddrDecimal = apMacAddrDecimal;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
}
