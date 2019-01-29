package com.leadcore.sms.sql;

/**
 * 
 * @author zhaoyifei
 * @since 2016-11-28
 */
public class GroupInfo {

	private int id;
	private String name;
	private String groupIP;
	private String masterID;
	
	public GroupInfo(int id, String name, String groupIP, String masterID) {
		super();
		this.id = id;
		this.name = name;
		this.groupIP = groupIP;
		this.masterID = masterID;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroupIP() {
		return groupIP;
	}

	public void setGroupIP(String groupIP) {
		this.groupIP = groupIP;
	}

	public String getMasterID() {
		return masterID;
	}

	public void setMasterIP(String masterID) {
		this.masterID = masterID;
	}

	@Override
	public String toString() {
		return "GroupInfo [id=" + id + ", name=" + name + ", groupIP="
				+ groupIP + ", masterID=" + masterID + "]";
	}
	
	
	
}
