package com.snmp.poller.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.VariableBinding;

import com.snmp.poller.Env;
import com.snmp.poller.dao.JdbcDAO;
import com.snmp.poller.dao.impl.AccessDAOImpl;
import com.snmp.poller.enums.MsgLevel;
import com.snmp.poller.model.UserCurrentInfo;
import com.snmp.poller.quartz.JobJdbcPoller;
import com.snmp.poller.quartz.JobSnmpPoller;
import com.snmp.poller.service.PollerService;
import com.snmp.poller.utils.SnmpUtils;
import com.snmp.poller.utils.impl.CommonUtils;
import com.snmp.poller.utils.impl.FileUtils;
import com.snmp.poller.utils.impl.SnmpV2UtilsImpl;
import com.snmp.poller.utils.impl.SnmpV3UtilsImpl;

public class PollerServiceImpl implements PollerService {
	private static Logger log = LoggerFactory.getLogger(PollerServiceImpl.class);

	private SnmpUtils snmpUtils;
	private JdbcDAO accessDAO;

	public PollerServiceImpl() {
		if (snmpUtils == null) {
			switch (Env.SNMP_VERSION) {
			case "V2":
				snmpUtils = new SnmpV2UtilsImpl();
				break;

			case "V3":
				snmpUtils = new SnmpV3UtilsImpl();
				break;
			}
		}

		if (accessDAO == null) {
			accessDAO = new AccessDAOImpl();
		}
	}

	@Override
	public void startUpJobs() {
		try {
			// Grab the Scheduler instance from the Factory
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

			TriggerKey snmpTriggerKey = TriggerKey.triggerKey(TRIGGER_SNMP, TRIGGER_SNMP);
			TriggerKey jdbcTriggerKey = TriggerKey.triggerKey(TRIGGER_JDBC, TRIGGER_JDBC);

			// SNMP Poller
			JobKey snmpJobKey = JobKey.jobKey(JOB_OF_SNMP_POLLER, JOB_OF_SNMP_POLLER);
			// JDBC Poller
			JobKey jdbcJobKey = JobKey.jobKey(JOB_OF_JDBC_POLLER, JOB_OF_JDBC_POLLER);

			if (!scheduler.checkExists(snmpTriggerKey) || !scheduler.checkExists(snmpJobKey)) {
				//				System.out.println("Env.SNMP_POLLER_CRON_EXPRESSION: "+Env.SNMP_POLLER_CRON_EXPRESSION);
				createJob(scheduler, TRIGGER_SNMP, JOB_OF_SNMP_POLLER, Env.SNMP_POLLER_CRON_EXPRESSION, JobSnmpPoller.class);
			} else {
				resumeJobs(scheduler, snmpTriggerKey, snmpJobKey);
			}

			if (!scheduler.checkExists(jdbcTriggerKey) || !scheduler.checkExists(jdbcJobKey)) {
				//				System.out.println("Env.JDBC_POLLER_CRON_EXPRESSION: "+Env.JDBC_POLLER_CRON_EXPRESSION);
				createJob(scheduler, TRIGGER_JDBC, JOB_OF_JDBC_POLLER, Env.JDBC_POLLER_CRON_EXPRESSION, JobJdbcPoller.class);
			} else {
				resumeJobs(scheduler, jdbcTriggerKey, jdbcJobKey);
			}

			if (!scheduler.isStarted()) {
				scheduler.start();
			}

			scheduler.triggerJob(jdbcJobKey);

			List<JobExecutionContext> list = scheduler.getCurrentlyExecutingJobs();

		} catch (Exception e) {
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, PollerServiceImpl.class, e.toString());
		}
	}

	/**
	 * 創建JOB
	 * @param scheduler
	 * @param triggerKey
	 * @param jobKey
	 * @param cronExpression
	 * @param jobClass
	 */
	private void createJob(Scheduler scheduler, String triggerKey, String jobKey, String cronExpression, Class jobClass) {
		// Add SNMP Poller
		JobDetail jobDetail = JobBuilder
				.newJob(jobClass)
				.withIdentity(jobKey, jobKey)
				.build();
		//build cron expression
		CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
				.cronSchedule(cronExpression)
				.withMisfireHandlingInstructionDoNothing();

		//build a new trigger with new cron expression
		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(triggerKey, triggerKey)
				.withSchedule(scheduleBuilder)
				.build();

		try {
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, PollerServiceImpl.class, e.toString());
		}
	}

	public void resumeJobs(Scheduler scheduler, TriggerKey triggerKey, JobKey jobKey) {
		try {
			scheduler.resumeTrigger(triggerKey);
			scheduler.resumeJob(jobKey);

		} catch (Exception e) {
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, PollerServiceImpl.class, e.toString());
		}
	}

	@Override
	public void pauseJobs() {
		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

			scheduler.pauseJob(JobKey.jobKey(JOB_OF_SNMP_POLLER, JOB_OF_SNMP_POLLER));
			scheduler.pauseJob(JobKey.jobKey(JOB_OF_JDBC_POLLER, JOB_OF_JDBC_POLLER));

			scheduler.pauseTrigger(TriggerKey.triggerKey(TRIGGER_SNMP, TRIGGER_SNMP));
			scheduler.pauseTrigger(TriggerKey.triggerKey(TRIGGER_JDBC, TRIGGER_JDBC));

		} catch (Exception e) {
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, PollerServiceImpl.class, e.toString());
		}
	}

	private String getVbKey(String queryOid, String returnOid) {
		return StringUtils.replace(returnOid, queryOid.substring(1), "").substring(1);
	}

	public static class SettingSource {

		private static SettingSource instance;
		public static Map<String, Env.SETTING_SOURCE> sourceMap;

		public static SettingSource build() {
			instance = new SettingSource();
			instance.sourceMap = new HashMap<String, Env.SETTING_SOURCE>();
			return instance;
		}

		public static SettingSource add(String name, Env.SETTING_SOURCE type) {
			instance.sourceMap.put(name, type);
			return instance;
		}
	}

	private void setEntityVal(Map<String, UserCurrentInfo> entityMap, Map<String, List<VariableBinding>> resutMap, Map<String, Env.SETTING_SOURCE> settings) {
		try {
			for (Map.Entry<String, List<VariableBinding>> result : resutMap.entrySet()) {
				final String oid = result.getKey();

				for (VariableBinding vb : result.getValue()) {
					final String vbkey = getVbKey(oid, vb.getOid().toString());
					final String vbValue = vb.getVariable().toString();

					UserCurrentInfo entity = entityMap.get(vbkey);

					for (Map.Entry<String, Env.SETTING_SOURCE> setting : settings.entrySet()) {
						final String pojoFieldName = setting.getKey();
						final String settingValue = setting.getValue() == Env.SETTING_SOURCE.KEY ? vbkey : vbValue;

						BeanUtils.setProperty(entity, pojoFieldName, settingValue);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			resutMap = null;
		}
	}

	@Override
	public void pollerSnmpInfo() {
		try {
			long startTime = System.currentTimeMillis();

			Map<String, UserCurrentInfo> userEntity = new HashMap<String, UserCurrentInfo>();
			Map<String, String> vbkeyMacMapping = new HashMap<String, String>();

			// Connect to device
			snmpUtils.connect(Env.TARGET_UDP_ADDR, Env.TARGET_COMMUNITY);

			if (StringUtils.isBlank(Env.MIB_MOBILE_KEY_OID)) {
				return;
			}

			/*
			 * 先查詢 AP MOBILE USER KEY (MOBILE_MAC_ADDR) (因MCA為後續資料的key值內容之一)
			 */
			List<String> oidList = new ArrayList<String>();
			oidList.add(Env.MIB_MOBILE_KEY_OID);
			Map<String, List<VariableBinding>> baseKeyMap = snmpUtils.pollData(oidList);

			for (Map.Entry<String, List<VariableBinding>> entry : baseKeyMap.entrySet()) {
				final String oid = entry.getKey();

				for (VariableBinding vb : entry.getValue()) {
					boolean userMacAddrIsValid = false;
					
					final String vbkey = getVbKey(oid, vb.getOid().toString());
					final String vbValue = vb.getVariable().toString();
					vbkeyMacMapping.put(vbkey, vbValue);

					UserCurrentInfo entity = new UserCurrentInfo();
					entity.setYyyymmdd(Env.FORMAT_YYYYMMDD_NO_SLASH.format(new Date()));

					try {
						log.error("userMacAddr: {} , userMacAddrDecimal: {}", vbValue, vbkey);
						if (StringUtils.indexOf(vbValue, ":") != -1 && StringUtils.split(vbValue, ":").length == 6) {
							userMacAddrIsValid = true;
							BeanUtils.setProperty(entity, "userMacAddr", vbValue);
							
						} else {
							// 若 USER_MAC_ADDR 內容格式不符則不處理此筆資料
							log.error("entity: {}, pojoField: userMacAddr, vbValue: {} >>> discard this record !", entity, vbValue);
						}
						
					} catch (Exception e) {
						log.error("entity: "+entity+", pojoField: userMacAddr, vbValue: "+vbValue);
						log.error(e.toString(), e);
						throw new Exception(e.toString()+" >> entity: "+entity+", pojoField: userMacAddr, vbValue: "+vbValue);
					}

					if (userMacAddrIsValid) {
						try {
							BeanUtils.setProperty(entity, "userMacAddrDecimal", vbkey);
						} catch (Exception e) {
							log.error("entity: "+entity+", pojoField: userMacAddrDecimal, vbValue: "+vbValue);
							log.error(e.toString(), e);
							throw new Exception(e.toString()+" >> entity: "+entity+", pojoField: userMacAddrDecimal, vbValue: "+vbValue);
						}
						userEntity.put(vbkey, entity);
					}
				}
			}

			//初始化 & GC
			oidList.clear();

			/*
			 * 查詢 AP MOBILE USER 相關資料
			 */
			oidList.addAll(Env.MIB_USERINFO_TABLE_FIELD_OID_MAPPING.values());
			Map<String, List<VariableBinding>> infoMap = snmpUtils.pollData(oidList);

			for (Map.Entry<String, List<VariableBinding>> entry : infoMap.entrySet()) {
				final String oid = entry.getKey();
				final String pojoField = Env.MIB_OID_POJO_MAPPING.get(oid);

				for (VariableBinding vb : entry.getValue()) {
					final String vbkey = getVbKey(oid, vb.getOid().toString());
					final String vbValue = vb.getVariable().toString();
					UserCurrentInfo entity = userEntity.get(vbkey);

					if (entity != null) {
						try {
							BeanUtils.setProperty(entity, pojoField, vbValue);
						} catch (Exception e) {
							log.error("entity: "+entity+", pojoField: "+pojoField+", vbValue: "+vbValue);
							log.error(e.toString(), e);
							throw new Exception(e.toString()+" >> entity: "+entity+", pojoField: "+pojoField+", vbValue: "+vbValue);
						}
					} else {
						log.error("entity not found. vbkey >> " + vbkey + " , pojoField: " + pojoField);
					}
				}
			}

			//初始化 & GC
			oidList.clear();

			/*
			 * 查詢 AP KEY (AP_MAC_ADDR) (因MCA為後續資料的key值內容之一)
			 */
			oidList.add(Env.MIB_AP_KEY_OID);
			Map<String, List<VariableBinding>> apkeyMap = snmpUtils.pollData(oidList);

			for (Map.Entry<String, List<VariableBinding>> entry : apkeyMap.entrySet()) {
				final String oid = entry.getKey();

				for (VariableBinding vb : entry.getValue()) {
					final String vbkey = getVbKey(oid, vb.getOid().toString());
					final String vbValue = vb.getVariable().toString();

					for (UserCurrentInfo entity : userEntity.values()) {
						final String apMacAddr = entity.getApMacAddr();

						if (vbValue.equals(apMacAddr)) {
							try {
								BeanUtils.setProperty(entity, "apMacAddrDecimal", vbkey);
							} catch (Exception e) {
								log.error("entity: "+entity+", pojoField: apMacAddrDecimal, vbValue: "+vbValue);
								log.error(e.toString(), e);
								throw new Exception(e.toString()+" >> entity: "+entity+", pojoField: apMacAddrDecimal, vbValue: "+vbValue);
							}
						}
					}
				}
			}

			//初始化 & GC
			oidList.clear();

			/*
			 * 查詢 AP_NAME 資料 >> Key = AP_MAC_DECIMAL
			 */
			oidList.add(Env.MIB_APNAME_OID);
			Map<String, List<VariableBinding>> apnameMap = snmpUtils.pollData(oidList);

			for (Map.Entry<String, List<VariableBinding>> entry : apnameMap.entrySet()) {
				final String oid = entry.getKey();

				for (VariableBinding vb : entry.getValue()) {
					final String vbkey = getVbKey(oid, vb.getOid().toString());
					final String vbValue = vb.getVariable().toString();

					for (UserCurrentInfo entity : userEntity.values()) {
						final String apMacAddrDecimal = entity.getApMacAddrDecimal();

						if (vbkey.equals(apMacAddrDecimal)) {
							try {
								BeanUtils.setProperty(entity, "apName", vbValue);
							} catch (Exception e) {
								log.error("entity: "+entity+", pojoField: apName, vbValue: "+vbValue);
								log.error(e.toString(), e);
								throw new Exception(e.toString()+" >> entity: "+entity+", pojoField: apName, vbValue: "+vbValue);
							}
						}
					}
				}
			}

			//初始化 & GC
			oidList.clear();

			/*
			 * 查詢 CNANNEL 資料 >> Key = USER_MAC + AP_SLOT
			 */
			oidList.add(Env.MIB_CHANNEL_OID);
			Map<String, List<VariableBinding>> channelMap = snmpUtils.pollData(oidList);

			for (Map.Entry<String, List<VariableBinding>> entry : channelMap.entrySet()) {
				final String oid = entry.getKey();

				for (VariableBinding vb : entry.getValue()) {
					final String vbkey = getVbKey(oid, vb.getOid().toString());
					final String vbValue = vb.getVariable().toString();

					/*
					 * 此時vbkey為 USER_MAC + AP_SLOT (bsnAPIfPhyChannelNumber)
					 */
					for (UserCurrentInfo entity : userEntity.values()) {
						if (entity.getApMacAddrDecimal() == null || entity.getApSlotId() == null) {
							continue;
						}

						final String channelKey = entity.getApMacAddrDecimal().concat(".").concat(String.valueOf(entity.getApSlotId()));

						if (vbkey.equals(channelKey)) {
							try {
								BeanUtils.setProperty(entity, "channel", vbValue);
							} catch (Exception e) {
								log.error("entity: "+entity+", pojoField: channel, vbValue: "+vbValue);
								log.error(e.toString(), e);
								throw new Exception(e.toString()+" >> entity: "+entity+", pojoField: channel, vbValue: "+vbValue);
							}
						}
					}
				}
			}

			oidList = null;

			long endTime = System.currentTimeMillis();
			CommonUtils.outputMsg(MsgLevel.INFO, PollerServiceImpl.class, "Wifi polling success.. " + (endTime-startTime) + " (ms)");

			startTime = System.currentTimeMillis();

			checkInsertOrUpdateDB(userEntity);

			endTime = System.currentTimeMillis();
			CommonUtils.outputMsg(MsgLevel.INFO, PollerServiceImpl.class, "Write data to DB finish.. " + (endTime-startTime) + " (ms)");

		} catch (Exception e) {
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, PollerServiceImpl.class, e.toString());
		}
	}

	private Double calculateQty(long firstTime, long lastTime, long addedBytes, String mac) {
		// AVG_SEND_DATA = SEND_DATA / (NOW_TIME - LAST_DISCONNECT_TIME)[分]
		/*
		 * bytesPerMilesecond / 1000 >> KB
		 * milesecond / 1000 / 60 >> minute
		 * >> KB / minute
		 */
		BigDecimal beginTimeInMillis = new BigDecimal(firstTime);
		BigDecimal endTimeInMillis = new BigDecimal(lastTime);
		BigDecimal bytes = new BigDecimal(addedBytes);

		BigDecimal minute = (endTimeInMillis.subtract(beginTimeInMillis)).divide(new BigDecimal(1000)).divide(new BigDecimal(60), 1, RoundingMode.HALF_UP);
		BigDecimal kBytes = bytes.divide(new BigDecimal(1000));
		BigDecimal kBytesPerMinute = kBytes.divide(minute, 3, RoundingMode.HALF_UP);

		//		if (mac.equals("6c:b7:49:8d:07:25")) {
		//			System.out.println("["+new Date()+"] beginTimeInMillis: "+beginTimeInMillis+", endTimeInMillis: "+endTimeInMillis+", bytes: "+bytes+", minute: "+minute+", kBytes: "+kBytes+", kBytesPerMinute: "+kBytesPerMinute);
		//		}

		return kBytesPerMinute.doubleValue();
	}

	private void checkInsertOrUpdateDB(Map<String, UserCurrentInfo> entities) {
		final String yyyymmdd = Env.FORMAT_YYYYMMDD_NO_SLASH.format(new Date());
		List<UserCurrentInfo> dbEntities = accessDAO.findSpecifyDateUserCurrentInfo(yyyymmdd);

		List<UserCurrentInfo> updateEntities = new ArrayList<UserCurrentInfo>();
		List<UserCurrentInfo> nowEntities = new ArrayList<UserCurrentInfo>();
		nowEntities.addAll(entities.values());

		if (dbEntities != null) {
			Collections.sort(nowEntities, new Comparator<UserCurrentInfo>() {
				@Override
				public int compare(UserCurrentInfo o1, UserCurrentInfo o2) {
					if (!o1.getUserMacAddr().equals(o2.getUserMacAddr())) {
						return o1.getUserMacAddr().compareTo(o2.getUserMacAddr());

					} else if (o1.getApSlotId() != o2.getApSlotId()) {
						return o1.getApSlotId().compareTo(o2.getApSlotId());

					} else if (!o1.getApName().equals(o2.getApName())) {
						return o1.getApName().compareTo(o2.getApName());

					} else if (!o1.getSsidName().equals(o2.getSsidName())) {
						return o1.getSsidName().compareTo(o2.getSsidName());

					} else {
						return 0;
					}
				}
			});

			for (UserCurrentInfo db : dbEntities) {
				for (int i=0; i<nowEntities.size(); i++) {
					UserCurrentInfo now = nowEntities.get(i);

					/*
					System.out.println("db vs now >> UserMacAddr: ["+db.getUserMacAddr()+" * "+now.getUserMacAddr()+"], "
							+" ApSlotId: ["+db.getApSlotId()+" * "+now.getApSlotId()+"], "
							+" ApName: ["+db.getApName()+" * "+now.getApName()+"], "
							+" SsidName: ["+db.getSsidName()+" * "+now.getSsidName()+"]");
					 */
					if (StringUtils.equals(db.getUserMacAddr(), now.getUserMacAddr())
							&& db.getApSlotId().equals(now.getApSlotId())
							&& StringUtils.equals(db.getApName(), now.getApName())
							&& StringUtils.equals(db.getSsidName(), now.getSsidName())) {

						/*
						 * Javers => 可用於比對兩個Bean內容差異
						Javers javers = JaversBuilder.javers()
								.withListCompareAlgorithm(ListCompareAlgorithm.LEVENSHTEIN_DISTANCE)
								.build();
						Diff diff = javers.compare(db, now);
						System.out.println(diff);
						if (diff.getChanges() != null && !diff.getChanges().isEmpty()) {
						 */

						/*
						 * 有撈回資料表示USER仍在連網狀態，因此就算當下無任何傳輸或接收等差異，仍必須更新LAST_DISCONNECT_TIME
						 */
						final Long sendDataAddedQty = now.getSendDataBytes() - db.getSendDataBytes();
						final Long receiveDataAddedQty = now.getReceiveDataBytes() - db.getReceiveDataBytes();

						final Date nowDate = new Date();
						// SNR = SIGNAL_STRENGTH - NOISE_LEVEL >> NOISE_LEVEL = SIGNAL_STRENGTH - SNR
						db.setNoiseLevel(now.getSignalStrength() - now.getSnr());
						db.setChannel(now.getChannel());
						db.setSignalStrength(now.getSignalStrength());
						db.setSnr(now.getSnr());
						db.setSendDataBytes(now.getSendDataBytes());
						db.setSendData(new Long(Math.round(now.getSendDataBytes() / 1000)));			// Bytes convert to KB
						db.setReceiveDataBytes(now.getReceiveDataBytes());
						db.setReceiveData(new Long(Math.round(now.getReceiveDataBytes() / 1000)));	// Bytes convert to KB
						db.setAvgSendData(
								sendDataAddedQty <= 0
								? new Double(0)
										: calculateQty(db.getLastDisconnectTime().getTime(), nowDate.getTime(), sendDataAddedQty, db.getUserMacAddr()));
						db.setAvgReceiveData(
								receiveDataAddedQty <= 0
								? new Double(0)
										: calculateQty(db.getLastDisconnectTime().getTime(), nowDate.getTime(), receiveDataAddedQty, db.getUserMacAddr()));
						db.setUpdateBy(Env.SYSTEM_USER_NAME);
						db.setUpdateTime(nowDate);
						db.setLastDisconnectTime(nowDate);

						updateEntities.add(db);

						nowEntities.remove(i);
						break;
					}
				}
			}
		}

		CommonUtils.outputMsg(MsgLevel.WARNING, PollerServiceImpl.class, "Add: "+nowEntities.size()+", Update: "+updateEntities.size());
		for (UserCurrentInfo updateEntity : updateEntities) {
			accessDAO.updateUserCurrentInfo(updateEntity);
		}

		for (UserCurrentInfo insertEntity : nowEntities) {
			if (StringUtils.isBlank(insertEntity.getYyyymmdd()) || StringUtils.isBlank(insertEntity.getUserMacAddr())
					|| insertEntity.getApSlotId() == null || StringUtils.isBlank(insertEntity.getApName())
					|| StringUtils.isBlank(insertEntity.getSsidName())) {
				log.error("Key field is null or empty >> [yyyymmdd]: " + insertEntity.getYyyymmdd() + " , [userMacAddr]: " + insertEntity.getUserMacAddr()
				+ " , [apSlotId]: " + insertEntity.getApSlotId() + " , [apName]: " + insertEntity.getApName() + " , [ssidName]: " + insertEntity.getSsidName()
				+ " , [apMacAddr]: " + insertEntity.getApMacAddr());
				continue;
			}

			final Date nowDate = new Date();
			// SNR = SIGNAL_STRENGTH - NOISE_LEVEL >> NOISE_LEVEL = SIGNAL_STRENGTH - SNR
			insertEntity.setNoiseLevel(
					(insertEntity.getSignalStrength() == null ? 0 : insertEntity.getSignalStrength())
					-
					(insertEntity.getSnr() == null ? 0 : insertEntity.getSnr()));

			insertEntity.setSendDataBytes(
					insertEntity.getSendDataBytes() == null ? new Long(0) : insertEntity.getSendDataBytes());
			insertEntity.setReceiveDataBytes(
					insertEntity.getReceiveDataBytes() == null ? new Long(0) : insertEntity.getReceiveDataBytes());

			insertEntity.setLastDisconnectTime(nowDate);
			insertEntity.setUpdateBy(Env.SYSTEM_USER_NAME);
			insertEntity.setUpdateTime(nowDate);
			insertEntity.setSendData(new Long(Math.round(insertEntity.getSendDataBytes() / 1000)));			// Bytes convert to KB
			insertEntity.setReceiveData(new Long(Math.round(insertEntity.getReceiveDataBytes() / 1000)));	// Bytes convert to KB
			insertEntity.setFirstConnectTime(nowDate);
			insertEntity.setAvgSendData(new Double(0));			// 第一次出現時平均值給0
			insertEntity.setAvgReceiveData(new Double(0));		// 第一次出現時平均值給0
			insertEntity.setCreateBy(Env.SYSTEM_USER_NAME);
			insertEntity.setCreateTime(nowDate);
			accessDAO.insertUserCurrentInfo(insertEntity);
		}

		accessDAO.doCommit();

		dbEntities = null;
		updateEntities = null;
		nowEntities = null;

		/*
		for (UserCurrentInfo entity : entities.values()) {
//			UserCurrentInfo dbEntity = accessDAO.findUserCurrentInfoByKeys(entity.getUserMacAddr(), entity.getApSlotId(), entity.getApName(), entity.getSsidName());
			final Date nowDate = new Date();
			// SNR = SIGNAL_STRENGTH - NOISE_LEVEL >> NOISE_LEVEL = SIGNAL_STRENGTH - SNR
			entity.setNoiseLevel(entity.getSignalStrength() - entity.getSnr());
			entity.setLastDisconnectTime(nowDate);
			entity.setUpdateBy(Env.SYSTEM_USER_NAME);
			entity.setUpdateTime(nowDate);
			entity.setSendData(Math.round(entity.getSendData() / 1000));		// Bytes convert to KB
			entity.setReceiveData(Math.round(entity.getReceiveData() / 1000));	// Bytes convert to KB
			if (dbEntity != null) {
				final int sendDataAddedQty = entity.getSendData() - dbEntity.getSendData();
				final int receiveDataAddedQty = entity.getReceiveData() - dbEntity.getReceiveData();
				dbEntity.setChannel(entity.getChannel());
				dbEntity.setSignalStrength(entity.getSignalStrength());
				dbEntity.setNoiseLevel(entity.getNoiseLevel());
				dbEntity.setSnr(entity.getSnr());
				dbEntity.setLastDisconnectTime(entity.getLastDisconnectTime());
				dbEntity.setSendData(entity.getSendData());
				dbEntity.setReceiveData(entity.getReceiveData());
				dbEntity.setAvgSendData(
						calculateQty(dbEntity.getFirstConnectTime().getTime(), dbEntity.getLastDisconnectTime().getTime(), sendDataAddedQty));
				dbEntity.setAvgReceiveData(
						calculateQty(dbEntity.getFirstConnectTime().getTime(), dbEntity.getLastDisconnectTime().getTime(), receiveDataAddedQty));
				dbEntity.setUpdateTime(entity.getUpdateTime());
				accessDAO.updateUserCurrentInfo(dbEntity);
			} else {
				entity.setFirstConnectTime(new Date());
				entity.setAvgSendData(0);		// 第一次出現時平均值給0
				entity.setAvgReceiveData(0);	// 第一次出現時平均值給0
				entity.setCreateBy(Env.SYSTEM_USER_NAME);
				entity.setCreateTime(nowDate);
				accessDAO.insertUserCurrentInfo(entity);
			}
		}
		 */
	}

	@Override
	public void pollerJdbcData() {
		try {
			final String yyyymmdd = Env.FORMAT_YYYYMMDD_NO_SLASH.format(new Date());
			List<UserCurrentInfo> entities = accessDAO.findSpecifyDateUserCurrentInfo(yyyymmdd);

			if (entities != null) {
				long startTime = System.currentTimeMillis();
				CommonUtils.refreshDataTable(entities);
				long endTime = System.currentTimeMillis();
				CommonUtils.outputMsg(MsgLevel.INFO, FileUtils.class, "Loading DB data finish.. "+(endTime-startTime)+" (ms)");

				startTime = System.currentTimeMillis();
				String fileName = FileUtils.output(entities);
				endTime = System.currentTimeMillis();
				CommonUtils.outputMsg(MsgLevel.INFO, FileUtils.class, "Output file success.. ["+fileName+"] "+(endTime-startTime)+" (ms)");
			}

			CommonUtils.refreshTime();

		} catch (Exception e) {
			log.error(e.toString(), e);
			CommonUtils.outputMsg(MsgLevel.ERROR, PollerServiceImpl.class, e.toString());
		}
	}
}