package com.snmp.poller;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class Env {

	public static enum SETTING_SOURCE {
		KEY, VALUE
	}

	public final static SimpleDateFormat FORMAT_YYYYMMDD_NO_SLASH = new SimpleDateFormat("yyyyMMdd");
	public final static SimpleDateFormat FORMAT_HHMISS = new SimpleDateFormat("HH:mm:ss");
	public final static SimpleDateFormat FORMAT_YYYYMMDD = new SimpleDateFormat("yyyy/MM/dd");
	public final static SimpleDateFormat FORMAT_YYYYMMDDHHMISS_NO_SYMBOL = new SimpleDateFormat("yyyyMMddHHmmss");
	public final static SimpleDateFormat FORMAT_YYYYMMDDHHMISS = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public final static SimpleDateFormat FORMAT_YYYYMMDDHHMI = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	public static final DecimalFormat df = new DecimalFormat("###,###,###", new DecimalFormatSymbols(new Locale("zh", "TW")));
	public static final DecimalFormat df_avg = new DecimalFormat("###,###,###.###", new DecimalFormatSymbols(new Locale("zh", "TW")));
	public static final String[] TABLE_HEADER_COLUMN_NAMES = new String[] {
			"No.", "日期", "用戶MAC", "SID", "用戶IP", "AP MAC", "連線AP", "連線SSID", "頻道", "訊號強度", "雜訊等級", "SNR", "首次出現", "最後出現", "傳輸量", "接收量", "傳輸平均", "接收平均"
	};
	public static final String[][] TABLE_CELL_DATAS = new String[][] {
		{"7C:D1:C3:ED:D1:17", "10.10.6.106", "AP2606", "Hwacom_WLAN", "6", "-48 dBm", "-80 dBm", "32 dBm", "2018/8/24 08:35", "2018/8/24 17:15", "12135 KB", "5739 KB", "3617 KB/分", "698 KB/分"}
	};
	public static Integer MAX_MSG_LINES = 150;

	public static Map<String, String> MIB_KEYS = new LinkedHashMap<String, String>();
	public static Map<String, String> MIB_USERINFO_TABLE_FIELD_OID_MAPPING = new HashMap<String, String>();
	public static Map<String, String> MIB_OID_TABLE_FIELD_MAPPING = new HashMap<String, String>();
	public static Map<String, String> MIB_OID_POJO_MAPPING = new HashMap<String, String>();
	public static Map<String, String> MODEL_MAPPING = new HashMap<String, String>();

	public static String FIELD_NAME_CHANNEL;
	public static String FIELD_NAME_APNAME;
	public static String FIELD_NAME_APMACADDR;
	public static String MIB_MOBILE_KEY_OID; //MOBILE USER端於MIB中的KEY節點OID >> bsnMobileStationMacAddress [.1.3.6.1.4.1.14179.2.1.4.1.1]
	public static String MIB_AP_KEY_OID; //AP端於MIB中的KEY節點OID >> bsnAPDot3MacAddress [.1.3.6.1.4.1.14179.2.2.1.1.1]
	public static String MIB_CHANNEL_OID;
	public static String MIB_APNAME_OID;
	public static String SNMP_POLLER_CRON_EXPRESSION;
	public static String JDBC_POLLER_CRON_EXPRESSION;
	public static String SNMP_VERSION;

	public static String TARGET_UDP_ADDR;
	public static String TARGET_PORT;
	public static String TARGET_COMMUNITY;
	public static String TABLE_COLUMN_NAMES;

	public static String OUTPUT_FILE_PATH;
	public static String OUTPUT_FILE_NAME;
	public static String SYMBOL_OF_EMPTY_DATA;
	public static String UNIT_OF_SIGNAL;
	public static String UNIT_OF_TRANSFER;
	public static String UNIT_OF_AVG_TRANSFER;

	public static String SYSTEM_USER_NAME;
	public static String OUTPUT_FILE_SEPARATOR_SYMBOL;
}
