package com.leadcore.sms.sql;

/**该类存放是用户信息
 * 所有用户信息的属性是私有的
 *能够通过该类的公用方法获取里面的私有信息
 */
public class UserInfo {
	private int id; // 用户ID
	private String name; // 用户名字
	private String imei; // 用户手机序列码（先用用户号码代替，后续用户号码会加上SIM的号码，那就唯一了）
	private String ipAddr; // 用户IP地址

	public UserInfo() {
		super();
	}

	public UserInfo(String name, String imei) {
		this.name = name;
		this.imei = imei;
	}


	public UserInfo(String name,  String imei,
			String ipAddr) {
		this(name,  imei);
		this.ipAddr = ipAddr;
	}

	/**
	 * 
	 * @param id       用户ID
	 * @param name     用户名称
	 * @param imei     用户设备唯一ID，暂时用号码代替
	 * @param ipAddr   用户设备ip地址
	 */
	public UserInfo(int id, String name, String imei,
			String ipAddr) {
		this(name, imei, ipAddr);
		this.id = id;
	}


	// 设置ID函数
	public void setId(int id) {
		this.id = id;
	}

	// 获取ID函数
	public int getId() {
		return id;
	}

	// 设置用户名函数
	public void setName(String name) {
		this.name = name;
	}

	// 获取用户名函数
	public String getName() {
		return name;
	}





	// 设置手机序列码
	public void setIMEI(String imei) {
		this.imei = imei;
	}

	// 获取手机序列码
	public String getIMEI() {
		return imei;
	}

	// 设置IP地址
	public void setIPAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	// 获取IP地址
	public String getIPAddr() {
		return ipAddr;
	}


	// 输出所有用户信息
	public String toString() {
		return "id:" + getId() + " name:" + getName()
				+ " imei:" + getIMEI() + " ip:"
				+ getIPAddr()  ;
	}
}
