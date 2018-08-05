
package com.leadcore.sip.login;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.leadcore.sms.entity.Group;

import android.util.Log;

public class Person implements Serializable{
	
	private static final long serialVersionUID = 4L;
    public int cmdType = 0;// 1:上线命令 ， 2 ：周期更新命令
    public long heartbeatTime = 0L;// 心跳时间戳
    public String ipAddress = null;
    public long loginTime = 0L;
    public String mobileNo = null;// 号码
    public String personNickeName = null;// 名字
    public List<String> mGroupIps = new ArrayList<String>();            //本用户所包含的所有组的ip地址的集合
    public List<Group> mGroups = new ArrayList<Group>();             //群主是本机的群组集合，用于更新群成员
    
    public Person() {
    }

    public Person(String paramString) {
//        Log.d("SKSPerson", "content:" + paramString);
        String[] arrayOfString = paramString.split(":");
        this.ipAddress = arrayOfString[0];
//        Log.d("SKSPerson", "ipAddress:" + this.ipAddress);
        this.personNickeName = arrayOfString[1];
//        Log.d("SKSPerson", "personNickeName:" + this.personNickeName);
        this.mobileNo = arrayOfString[2];
//        Log.d("SKSPerson", "mobileNo:" + this.mobileNo);
        this.loginTime = Long.parseLong(arrayOfString[3]);
//        Log.d("SKSPerson", "loginTime:" + this.loginTime);
        this.cmdType = Integer.parseInt(arrayOfString[4]);
//        this.cmdType = 1;
//        Log.d("SKSPerson", "cmdType:" + this.cmdType);
    }

    public Person(String ipAddress, String personNickeName, String mobileNo,
            long loginTime) {
        this.ipAddress = ipAddress;
        this.personNickeName = personNickeName;
        this.mobileNo = mobileNo;
        this.loginTime = loginTime;
    }
    
    public List<String> getGroupIps() {
		return mGroupIps;
	}

	public void setGroupIps(List<String> groups) {
		this.mGroupIps = groups;
	}
	
    public List<Group> getmGroups() {
		return mGroups;
	}

	public void setmGroups(List<Group> mGroups) {
		this.mGroups = mGroups;
	}

	public int getCmdType() {
        return this.cmdType;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public long getLoginTime() {
        return this.loginTime;
    }

    public String getPersonNickeName() {
        return this.personNickeName;
    }

    public void setCmdType(int paramInt) {
        this.cmdType = paramInt;
    }

    public void setIpAddress(String paramString) {
        this.ipAddress = paramString;
    }

    public void setLoginTime(long paramLong) {
        this.loginTime = paramLong;
    }

    public void setPersonNickeName(String paramString) {
        this.personNickeName = paramString;
    }

	public String toString() {
        String str1;
        String str2;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ipAddress == null ? "" : ipAddress);
        stringBuilder.append(":");
        stringBuilder.append(personNickeName == null ? "" : personNickeName);
        stringBuilder.append(":");
        stringBuilder.append(mobileNo == null ? "" : mobileNo);
        stringBuilder.append(":");
        stringBuilder.append(loginTime);
        stringBuilder.append(":");
        stringBuilder.append(cmdType);
        stringBuilder.append(":");
        
        return stringBuilder.toString();
    }
}

/*
 * Location: D:\tools\反编译工具\dex2jar和JD-GUI\classes_dex2jar.jar Qualified Name:
 * com.sks.adhoc.service.SKSPerson JD-Core Version: 0.7.0.1
 */
