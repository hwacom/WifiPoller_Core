package com.snmp.poller.model;

public class MibOidSetting {

	private String fieldName;
	private String type;
	private String key;
	private Integer keyOrder;
	private String pojoFieldName;
	private String mibNodeName;
	private String mibOid;
	private String description;

	public MibOidSetting() {
		super();
	}

	public MibOidSetting(String fieldName, String type, String key, Integer keyOrder, String pojoFieldName,
			String mibNodeName, String mibOid, String description) {
		super();
		this.fieldName = fieldName;
		this.type = type;
		this.key = key;
		this.keyOrder = keyOrder;
		this.pojoFieldName = pojoFieldName;
		this.mibNodeName = mibNodeName;
		this.mibOid = mibOid;
		this.description = description;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Integer getKeyOrder() {
		return keyOrder;
	}

	public void setKeyOrder(Integer keyOrder) {
		this.keyOrder = keyOrder;
	}

	public String getPojoFieldName() {
		return pojoFieldName;
	}

	public void setPojoFieldName(String pojoFieldName) {
		this.pojoFieldName = pojoFieldName;
	}

	public String getMibNodeName() {
		return mibNodeName;
	}

	public void setMibNodeName(String mibNodeName) {
		this.mibNodeName = mibNodeName;
	}

	public String getMibOid() {
		return mibOid;
	}

	public void setMibOid(String mibOid) {
		this.mibOid = mibOid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
