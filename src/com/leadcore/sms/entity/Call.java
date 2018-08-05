package com.leadcore.sms.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * 电话实体类
 * 
 * @author _Hill3
 */
public class Call extends Entity implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String senderName;
    private String senderIMEI;  
    private String sendTime;
    private long id; // ID序号
    
    private int calltime;
    private String date;
    private int callStatus;
    private String receiveIP;//when is group message ,must set the receiveIP
    private String receiveIMEI;
    private List<Users> callLog = new ArrayList<Users>();	
    private String callType;//add by lss for declear calltype (audio or vedio)
    private String receiveName;//add by lss for modifying the activity of calllog show senderName
    public Call() {
    	
    }

    public Call(String paramSenderIMEI,String receiveIMEI, String paramSendTime,int callStatus,String receiveIP) {
        this.senderIMEI = paramSenderIMEI;
        this.sendTime = paramSendTime; 
        this.receiveIMEI=receiveIMEI;
        this.callStatus=callStatus;
        this.receiveIP=receiveIP;
    }
    
 
    public Call(String paramSenderIMEI,String receiveIMEI, String paramSendTime,int callStatus, int recordTime,String receiveIP,List<Users> callLog){
    	this(paramSenderIMEI,receiveIMEI, paramSendTime,callStatus,receiveIP);
    	this.callLog=callLog;
    }
    
    public Call(int id,String paramSenderIMEI,String receiveIMEI, String paramSendTime,int callStatus,String receiveIP) {
        this.senderIMEI = paramSenderIMEI;
        this.sendTime = paramSendTime; 
        this.receiveIMEI=receiveIMEI;
        this.callStatus=callStatus;
        this.receiveIP=receiveIP;
        this.id=id;
    }
    
 
    public Call(int id, String paramSenderIMEI,String receiveIMEI, String paramSendTime,int callStatus, int recordTime,String receiveIP,List<Users> callLog){
    	this(paramSenderIMEI,receiveIMEI, paramSendTime,callStatus,receiveIP);
    	this.callLog=callLog;
    	this.id=id;
    }
    //add by lss for declear calltype(audio or vedio)
    public Call(int id, String paramSenderIMEI,String receiveIMEI, String paramSendTime,int callStatus, int recordTime,String receiveIP,List<Users> callLog,String callType){
    	this(paramSenderIMEI,receiveIMEI, paramSendTime,callStatus,receiveIP);
    	this.callLog=callLog;
    	this.id=id;
    	this.callType=callType;
    }
    
    public Call(String paramSenderIMEI,String receiveIMEI, String paramSendTime,int callStatus,String receiveIP,String callType) {
        this.senderIMEI = paramSenderIMEI;
        this.sendTime = paramSendTime; 
        this.receiveIMEI=receiveIMEI;
        this.callStatus=callStatus;
        this.receiveIP=receiveIP;
        this.callType=callType;
    }
    //add by lss for  modifying the activity of calllog show senderName
    public Call(int id, String paramSenderIMEI,String receiveIMEI, String paramSendTime,int callStatus, int recordTime,String receiveIP,List<Users> callLog,String callType,String receiverName){
    	this(paramSenderIMEI,receiveIMEI, paramSendTime,callStatus,receiveIP);
    	this.callLog=callLog;
    	this.id=id;
    	this.callType=callType;
    	this.receiveName=receiverName;
    }
    
    public Call(String paramSenderIMEI,String receiveIMEI, String paramSendTime,int callStatus,String receiveIP,String callType,String receiverName) {
        this.senderIMEI = paramSenderIMEI;
        this.sendTime = paramSendTime; 
        this.receiveIMEI=receiveIMEI;
        this.callStatus=callStatus;
        this.receiveIP=receiveIP;
        this.callType=callType;
        this.receiveName=receiverName;
    }
    
    //add end
    
    /**
     * 获取电话拨出时间
     * 
     * @return
     */
    public String getSendTime() {
        return sendTime;
    }

    /**
     * 设置电话拨出时间
     * 
     * @param paramSendTime
     *            拨出时间,格式 xx年xx月xx日 xx:xx:xx
     */
    public void setSendTime(String paramSendTime) {
        this.sendTime = paramSendTime;
    }
    
    
    /**
     * 获取id
     * 
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     * 设置id
     * 
     * @param id
     *            
     */
    public void setId(long id) {
        this.id = id;
    }
    
    //add by lss for declear calltype (audio or vedio)
	/**
	 * 设置calltype
	 */
    public void setCallType(String type){
    	this.callType=type;
    }
    /**
     * 获取callType
     */
    public String getCallType(){
    	return callType;
    }
    //add end
  

    /**
     * 克隆对象
     * 
     * @param
     */

    public Call clone() {
        return new Call(senderIMEI,receiveIMEI, sendTime,callStatus,receiveIP,callType);//add calltype by lss for declear calltype(audio or vedio)
    }

    
    
    public String getReceiveIP(){
    	return receiveIP;
    }
    
    public void setReceiveIP(String receiveIP){
    	this.receiveIP = receiveIP;
    }
    
    public String getReceiveIMEI(){
    	return receiveIMEI;
    }
    
    public void setReceiveIMEI(String receiveIMEI){
    	this.receiveIMEI = receiveIMEI;
    }
    
    public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	
	public List<Users> getCallLog() {
		return callLog;
	}

	public void setCallLog(List<Users> callLog) {
		this.callLog = callLog;
	}
    /* 设置通话时长 */
    public void setCallTime(int calltime) {
        this.calltime = calltime;
    }

    /* 获取通话时长 */
    public int getCallTime() {
        return calltime;
    }
    
    /* 设置电话日期 */
    public void setDate(String date) {
        this.date = date;
    }

    /* 获取电话日期*/
    public String getDate() {
        return date;
    }

    public int getCallStatus(){
    	return callStatus;
    }
    
    public void setCallStatus(int callStatus){
    	this.callStatus = callStatus;
    }
  //modify by lss for modifying the activity of calllog show senderName
    public String getReceiverName(){
    	return receiveName;
    }
    
    public void setReceiverName(String receiverName){
    	this.receiveName = receiverName;
    }
    //add end
	public String toString(){
    	return "senderIMEI:" + senderIMEI + " sendTime:" + sendTime+ " receiveIMEI:" + receiveIMEI+"callStatus:"+callStatus 
    			+ " calltime:" + calltime+"date:"+date+"::::callType="+callType;
    }

}
