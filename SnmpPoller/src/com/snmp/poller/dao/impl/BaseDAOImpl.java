package com.snmp.poller.dao.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

import com.snmp.poller.Env;
import com.snmp.poller.enums.MsgLevel;
import com.snmp.poller.utils.impl.CommonUtils;

public class BaseDAOImpl {
	protected Connection connDB = null;
	protected Statement st = null;

	protected Connection getConnect() throws Exception {
		if (connDB == null) {
			//建立驅動程式，連結odbc至Microsoft Access
			Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

			//下列字串的://之後要加上access檔案存放的地方
			String dataSource = "jdbc:ucanaccess://D:/accessDB/WifyPoller.accdb";

			connDB = DriverManager.getConnection(dataSource);
			connDB.setAutoCommit(false);
		}

		return connDB;
	}

	protected void commit() {
		try {
			if (connDB != null) {
				connDB.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
			CommonUtils.outputMsg(MsgLevel.ERROR, BaseDAOImpl.class, e.toString());

		} finally {
			close();
		}
	}

	protected void close() {
		try {
			if (st != null) {
				st.close();
			}
			if (connDB != null) {
				connDB.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
			CommonUtils.outputMsg(MsgLevel.ERROR, BaseDAOImpl.class, e.toString());

		} finally {
			st = null;
			connDB = null;
		}
	}

	protected Object excuteQuery(String sql) throws Exception {
		try {
			if (connDB == null) {
				getConnect();
			}

			st = connDB.createStatement();
			st.execute(sql);
			return mapping2ModelEntity(st.getResultSet());

		} finally {
			close();
		}
	}

	protected Object excuteQuery(String oriSql, Object[] queryValues) throws Exception {
		try {
			if (connDB == null) {
				getConnect();
			}

			String newSql = "";
			String[] sqlSlice = oriSql.split(Pattern.quote("?"));

			if (sqlSlice.length-1 != queryValues.length) {
				throw new Exception("SQL ? count not equal to queryValues >> sql:[" + oriSql + "] , queryValues.length: " + queryValues.length);
			}

			for (int i=0; i<queryValues.length; i++) {
				Object val = queryValues[i];

				if (val instanceof String) {
					newSql = newSql.concat(sqlSlice[i]).concat("'").concat(String.valueOf(val)).concat("'");

				} else if (val instanceof Integer) {
					newSql = newSql.concat(sqlSlice[i]).concat(String.valueOf(val));

				} else {
					throw new Exception("Not support value type >> [" + val + "]: " + val.toString() + " , type: " + val.getClass().getDeclaredClasses());
				}

				if (i == queryValues.length-1) {
					newSql = newSql.concat(sqlSlice[i+1]);
				}
			}

			st = connDB.createStatement();
			st.execute(newSql);
			return mapping2ModelEntity(st.getResultSet());

		} finally {
			close();
		}
	}

	protected Integer excuteUpdate(String sql) throws Exception {
		try {
			if (connDB == null) {
				getConnect();
			}

			st = connDB.createStatement();
			return st.executeUpdate(sql);

		} finally {
			close();
		}
	}

	private String transColumnName2ModelVariableName(String columnName) {
		String retVal = StringUtils.lowerCase(columnName);
		if (StringUtils.isNotBlank(retVal)) {
			String[] f = retVal.split("_");

			String var = "";
			boolean first = true;
			for (String t : f) {
				if (!first) {
					t = StringUtils.upperCase(t.substring(0, 1)).concat(t.substring(1));
				}

				var = var.concat(t);
				first = false;
			}

			retVal = var;
		}

		return retVal;
	}

	private Object mapping2ModelEntity(ResultSet rs) throws Exception {
		Object retObj = null;
		if (rs != null) {
			ResultSetMetaData rsmd = rs.getMetaData();

			List<Map<String, Object>> tmpEntities = new ArrayList<Map<String, Object>>();

			while (rs.next()) {
				Map<String, Object> modelObjs = new HashMap<String, Object>();

				String tableName;
				String modelClassName;
				Object modelObj;
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					tableName = rsmd.getTableName(i);

					if (!Env.MODEL_MAPPING.containsKey(tableName)) {
						throw new Exception("Model對應設定錯誤!! >> tableName: " + tableName + " 查無對應");
					}

					modelClassName = Env.MODEL_MAPPING.get(tableName);

					if (!modelObjs.containsKey(modelClassName)) {
						modelObj = Class.forName(modelClassName).newInstance();
						modelObjs.put(modelClassName, modelObj);

					} else {
						modelObj = modelObjs.get(modelClassName);
					}

					final String columnName = rsmd.getColumnName(i);
					final Object columnValue = rs.getObject(i);

					BeanUtils.setProperty(modelObj, transColumnName2ModelVariableName(columnName), columnValue);
				}

				tmpEntities.add(modelObjs);
			}

			int modelCount = (tmpEntities != null && !tmpEntities.isEmpty() && tmpEntities.get(0) != null) ? tmpEntities.get(0).size() : 0;

			if (modelCount == 1) {
				retObj = new ArrayList<Object>();

			} else if (modelCount > 1) {
				retObj = new ArrayList<Object[]>();
			}

			for (Map<String, Object> entityMap : tmpEntities) {
				Iterator<?> it = entityMap.values().iterator();

				if (modelCount == 1) {
					((ArrayList<Object>)retObj).add(it.next());

				} else if (modelCount > 1) {
					Object[] objs = new Object[modelCount];

					int j = 0;
					while (it.hasNext()) {
						objs[j] = it.next();
						j++;
					}

					((ArrayList<Object[]>)retObj).add(objs);
				}
			}
		}

		return retObj;
	}
}
