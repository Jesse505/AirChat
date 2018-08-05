package com.leadcore.sms.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * Group Entity
 * @author zhaoyifei
 * @since 2016-11-28
 */
public class Group extends Entity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	public static final String ENTITY_GROUOP = "entity_group";
	
	private String strIP;												// group IP
	private String strName;												// group name		
	private String strMasterID;	                						// group master ID
	private int memberNum;												// num of group members
	private List<Users> members = new ArrayList<Users>();				// members of group
	
	
	public Group() {
		super();
	}

	public Group(String strIP, String strName, String strMasterID) {
		super();
		this.strIP = strIP;
		this.strName = strName;
		this.strMasterID = strMasterID;
	}



	public Group(String strIP, String strName, String strMasterID,
			int memberNum,List<Users> members) {
		super();
		this.strIP = strIP;
		this.strName = strName;
		this.strMasterID = strMasterID;
		this.memberNum = memberNum;
		this.members = members;
	}

	public String getStrIP() {
		return strIP;
	}

	public void setStrIP(String strIP) {
		this.strIP = strIP;
	}

	public String getStrName() {
		return strName;
	}

	public void setStrName(String strName) {
		this.strName = strName;
	}

	public int getMemberNum() {
		return memberNum;
	}

	public void setMemberNum(int memberNum) {
		this.memberNum = memberNum;
	}

	public List<Users> getMembers() {
		return members;
	}

	public void setMembers(List<Users> members) {
		this.members = members;
	}
	
	
	
	public String getMasterID() {
		return strMasterID;
	}

	public void setMasterID(String strMasterID) {
		this.strMasterID = strMasterID;
	}

//	public Group(Parcel parcel){
//		strIP = parcel.readString();
//		strName = parcel.readString();
//		strMasterIP = parcel.readString();
//		memberNum = parcel.readInt();
//		Parcelable[] pars = parcel.readParcelableArray(Users.class.getClassLoader());
//		members = Arrays.asList(Arrays.asList(pars).toArray(new Users[pars.length]));
//	}
//
//	@Override
//	public int describeContents() {
//		return 0;
//	}
//
//	@Override
//	public void writeToParcel(Parcel dest, int flags) {
//		dest.writeString(strIP);
//		dest.writeString(strName);
//		dest.writeString(strMasterIP);
//		dest.writeInt(memberNum);
//		dest.writeParcelableArray(members.toArray(new Users[members.size()]), flags);
//		
//	}
//	
//	public static final Parcelable.Creator<Group> CREATOR = new Parcelable.Creator<Group>() {
//
//		@Override
//		public Group createFromParcel(Parcel source) {
//			return new Group(source);
//		}
//
//		@Override
//		public Group[] newArray(int size) {
//			return new Group[size];
//		}
//	};
//	
//    public static Parcelable.Creator<Group> getCreator() {
//        return CREATOR;
//    }
	
}
