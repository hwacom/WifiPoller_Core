package com.snmp.poller.init;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.snmp.poller.Env;
import com.snmp.poller.dao.JdbcDAO;
import com.snmp.poller.dao.impl.AccessDAOImpl;
import com.snmp.poller.model.MibOidSetting;

public class InitialConfig {
	private static Logger log = LoggerFactory.getLogger(InitialConfig.class);

	private JdbcDAO accessDAO;

	public InitialConfig() {
		if (accessDAO == null) {
			accessDAO = new AccessDAOImpl();
		}
	}

	public void init() {
		loadConfigFile();
		loadMibOidSetting();
	}

	private void loadMibOidSetting() {
		try {
			List<MibOidSetting> entities = accessDAO.findMibOidSetting();

			if (entities == null || (entities != null && entities.isEmpty())) {
				throw new Exception("Initial error >> [MIB_OID_SETTING] is empty!!");
			}

			if (Env.MIB_USERINFO_TABLE_FIELD_OID_MAPPING == null) {
				Env.MIB_USERINFO_TABLE_FIELD_OID_MAPPING = new HashMap<String, String>();
			}
			if (Env.MIB_OID_TABLE_FIELD_MAPPING == null) {
				Env.MIB_OID_TABLE_FIELD_MAPPING = new HashMap<String, String>();
			}
			if (Env.MIB_OID_POJO_MAPPING == null) {
				Env.MIB_OID_POJO_MAPPING = new HashMap<String, String>();
			}

			for (MibOidSetting entry : entities) {
				if (StringUtils.equals(entry.getFieldName(), Env.FIELD_NAME_CHANNEL)) {
					Env.MIB_CHANNEL_OID = entry.getMibOid();

				} else if (StringUtils.equals(entry.getFieldName(), Env.FIELD_NAME_APNAME)) {
					Env.MIB_APNAME_OID = entry.getMibOid();

				} else {
					if (StringUtils.equals(entry.getType(), "MOBILE") && !StringUtils.equals(entry.getKey(), "Y")) {
						Env.MIB_USERINFO_TABLE_FIELD_OID_MAPPING.put(entry.getFieldName(), entry.getMibOid());
					}
				}

				Env.MIB_OID_POJO_MAPPING.put(entry.getMibOid(), entry.getPojoFieldName());
				Env.MIB_OID_TABLE_FIELD_MAPPING.put(entry.getMibOid(), entry.getFieldName());

				if (StringUtils.equals(entry.getKey(), "Y")) {
					Env.MIB_KEYS.put(entry.getFieldName(), entry.getMibOid());
				}
			}

		} catch (Exception e) {
			log.error(e.toString(), e);
		}
	}

	private void loadConfigFile() {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			ClassLoader classLoader = getClass().getClassLoader();
			input = new FileInputStream(classLoader.getResource("config.properties").getFile());
			prop.load(input);

			System.out.println(prop);

			if (prop != null) {
				setConfigMap(prop);
			}

		} catch (Exception e) {
			log.error(e.toString(), e);

		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
					input = null;
					e.printStackTrace();
				}
			}
		}
	}

	private void setConfigMap(Properties prop) {
		Set<String> keys = prop.stringPropertyNames();

		Env env = new Env();
		for (final String propKey : keys) {
			try {
				final String propValue = prop.getProperty(propKey);

				Class<?> dynamicClass = Env.class.getDeclaredField(propKey).getType();

				if (dynamicClass.isAssignableFrom(Map.class)) {
					Map map = (Map)Env.class.getDeclaredField(propKey).get(null);

					String[] mapping = propValue.split("@~");

					String mapKey;
					String mapValue;
					for (String m : mapping) {
						mapKey = m.split(Pattern.quote("["))[0];
						mapValue = m.split(Pattern.quote("["))[1].replace("]", "");

						map.put(mapKey, mapValue);
					}

				} else if (dynamicClass.isAssignableFrom(String.class)) {
					Env.class.getDeclaredField(propKey).set(env, propValue == null ? "" : propValue);

				} else if (dynamicClass.isAssignableFrom(Integer.class)) {
					Env.class.getDeclaredField(propKey).set(env, propValue == null ? 0 : Integer.valueOf(propValue));

				}

			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		}
	}
}
