package com.leadcore.sms.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.sipdroid.sipua.R;


import android.os.Parcel;
import android.os.Parcelable;


/**
 * @fileName NearByPeople.java
 * @description 附近个人实体类
 * @author _Hill3
 */
public class Users extends Entity implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 3L;
	/** 用户信息常量 **/
    public static final String ID = "ID";
    public static final String NICKNAME = "Nickname";
    public static final String IMEI = "IMEI";
    public static final String IPADDRESS = "Ipaddress";
    public static final String SERVERIPADDRESS = "serverIPaddress";
    public static final String ENTITY_PEOPLE = "entity_people";

    private String mIMEI;
    private String mNickname;
    private String mIpaddress;
    private int msgCount;
    private String groupIP;
    private List<String> mGroupIps = new ArrayList<String>();
    private List<Group> mGroups = new ArrayList<Group>();             //群主是本机的群组集合，用于更新群成员

    public Users() {
        msgCount = 0;
    }

    public Users(String paramIMEI, String paramNickname, String paramIP) {
        super();
        this.mIMEI = paramIMEI;
        this.mNickname = paramNickname;
        this.mIpaddress = paramIP;
    }

    public Users(String mIMEI, String mNickname, String mIpaddress,
			String groupIP) {
		super();
		this.mIMEI = mIMEI;
		this.mNickname = mNickname;
		this.mIpaddress = mIpaddress;
		this.groupIP = groupIP;
	}

	public String getIMEI() {
        return this.mIMEI;
    }

    public void setIMEI(String paramIMEI) {
        this.mIMEI = paramIMEI;
    }


    public String getNickname() {
        return this.mNickname;
    }

    public void setNickname(String paramNickname) {
        this.mNickname = paramNickname;
    }



    public String getIpaddress() {
        return this.mIpaddress;
    }

    public void setIpaddress(String paramIpaddress) {
        this.mIpaddress = paramIpaddress;
    }


    public int getMsgCount() {
        return this.msgCount;
    }

    public void setMsgCount(int paramMsgCount) {
        this.msgCount = paramMsgCount;
    }
    
    
    public String getGroupIP() {
		return groupIP;
	}

	public void setGroupIP(String groupIP) {
		this.groupIP = groupIP;
	}

	public List<String> getmGroupIps() {
		return mGroupIps;
	}

	public void setmGroupIps(List<String> mGroupIps) {
		this.mGroupIps = mGroupIps;
	}

	public List<Group> getmGroups() {
		return mGroups;
	}

	public void setmGroups(List<Group> mGroups) {
		this.mGroups = mGroups;
	}
	
	

//	public Users(Parcel parcel){
//    	mIMEI = parcel.readString();
//    	mNickname = parcel.readString();
//    	mIpaddress = parcel.readString();
//    	msgCount = parcel.readInt();
//    	groupIP = parcel.readString();
//    	
//    }
//
//    public static Parcelable.Creator<Users> getCreator() {
//        return CREATOR;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(mIMEI);
//        dest.writeString(mNickname);
//        dest.writeString(mIpaddress);
//        dest.writeInt(msgCount);
//        dest.writeString(groupIP);
//    }
//
//    public static final Parcelable.Creator<Users> CREATOR = new Parcelable.Creator<Users>() {
//
//        @Override
//        public Users createFromParcel(Parcel source) {
//            Users people = new Users();
//            people.setIMEI(source.readString());
//            people.setNickname(source.readString());
//            people.setIpaddress(source.readString());
//            people.setMsgCount(source.readInt());
//            people.setGroupIP(source.readString());
//            return people;
//        }
//
//        @Override
//        public Users[] newArray(int size) {
//            return new Users[size];
//        }
//    };
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }

}
