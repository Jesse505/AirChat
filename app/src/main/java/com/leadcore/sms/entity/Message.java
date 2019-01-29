package com.leadcore.sms.entity;

import java.io.Serializable;


/**
 * 消息实体类
 * 
 * @author _Hill3
 */
public class Message extends Entity implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String senderName;
    private String senderIMEI;  
    private String sendTime;
    private String MsgContent;
    private CONTENT_TYPE contentType;
    private int percent;
    private int recordTime; 
    private int readStatus;//读取状态
    
    private String receiveIP;//when is group message ,must set the receiveIP
    private String receiveIMEI;
    private long id;
    private int isGroup;//add by lss for processing the situation which group is deleted
    
    private long mReservedID;   //保留位

    public Message() {
    }

    public Message(int id,String paramSenderIMEI, String paramSendTime, String paramMsgContent,
            CONTENT_TYPE paramContentType,int readStatus) {
    	this.id=id;
        this.senderIMEI = paramSenderIMEI;
        this.sendTime = paramSendTime;
        this.MsgContent = paramMsgContent;
        this.contentType = paramContentType;
        this.readStatus=readStatus;
        
    }
   
    public Message(int id,String paramSenderIMEI, String paramSendTime, String paramMsgContent,
            CONTENT_TYPE paramContentType,String paramReceiverIMEI,int readStatus) {
    	this.id=id;
        this.senderIMEI = paramSenderIMEI;
        this.sendTime = paramSendTime;
        this.MsgContent = paramMsgContent;
        this.contentType = paramContentType;
        this.receiveIMEI=paramReceiverIMEI;
        this.readStatus=readStatus;
    }
  
    public Message(int id,String paramSenderIMEI, String paramSendTime, String paramMsgContent,
            CONTENT_TYPE paramContentType, int recordTime,int readStatus){
    	this(id,paramSenderIMEI, paramSendTime, paramMsgContent, paramContentType,readStatus);
    	this.recordTime = recordTime;
    }
    public Message(int id,String paramSenderIMEI, String paramSendTime, String paramMsgContent,
            CONTENT_TYPE paramContentType, int recordTime,String paramReceiverIMEI,int readStatus){
    	this(id,paramSenderIMEI, paramSendTime, paramMsgContent, paramContentType,paramReceiverIMEI,readStatus);
    	this.recordTime = recordTime;
    }
    
    public Message(String paramSenderIMEI, String paramSendTime, String paramMsgContent,
            CONTENT_TYPE paramContentType) {
    	
        this.senderIMEI = paramSenderIMEI;
        this.sendTime = paramSendTime;
        this.MsgContent = paramMsgContent;
        this.contentType = paramContentType;
        
    }
    public Message(String paramSenderIMEI, String paramSendTime, String paramMsgContent,
            CONTENT_TYPE paramContentType,String paramReceiverIMEI) {
    	
        this.senderIMEI = paramSenderIMEI;
        this.sendTime = paramSendTime;
        this.MsgContent = paramMsgContent;
        this.contentType = paramContentType;
        this.receiveIMEI=paramReceiverIMEI;
        
    }
    
    public Message(String paramSenderIMEI, String paramSendTime, String paramMsgContent,
            CONTENT_TYPE paramContentType, int recordTime){
    	this(paramSenderIMEI, paramSendTime, paramMsgContent, paramContentType);
    	this.recordTime = recordTime;
    }
    public Message(String paramSenderIMEI, String paramSendTime, String paramMsgContent,
            CONTENT_TYPE paramContentType, int recordTime,String paramReceiverIMEI){
    	this(paramSenderIMEI, paramSendTime, paramMsgContent, paramContentType,paramReceiverIMEI);
    	this.recordTime = recordTime;
    }
  //add by lss for processing the situation which group is deleted
    public Message(int id,String paramSenderIMEI, String paramSendTime, String paramMsgContent,
            CONTENT_TYPE paramContentType,String paramReceiverIMEI,int readStatus,int isGroup) {
    	this.id=id;
        this.senderIMEI = paramSenderIMEI;
        this.sendTime = paramSendTime;
        this.MsgContent = paramMsgContent;
        this.contentType = paramContentType;
        this.receiveIMEI=paramReceiverIMEI;
        this.readStatus=readStatus;
        this.isGroup=isGroup;
    }
    public Message(int id,String paramSenderIMEI, String paramSendTime, String paramMsgContent,
            CONTENT_TYPE paramContentType, int recordTime,String paramReceiverIMEI,int readStatus,int isGroup){
    	this(id,paramSenderIMEI, paramSendTime, paramMsgContent, paramContentType,paramReceiverIMEI,readStatus,isGroup);
    	this.recordTime = recordTime;
    }
    
    public void setIsGroup(int isGroup){
    	this.isGroup = isGroup;
    }
    
    public int getIsGroup(){
    	return isGroup;
    }
    //add end

    /** 消息内容类型 **/
    public enum CONTENT_TYPE {
        TEXT, IMAGE, FILE, VOICE;
    }
    
   
    public int getRecordTime(){
    	return recordTime;
    }
    
    public void setRecordTime(int recordTime){
    	this.recordTime = recordTime;
    }
    
    /* 设置ID序列 */
    public void setID(long id) {
        this.id = id;
    }
    /* 获取序列ID */
    public long getId() {
        return id;
    }   
    /**
     * 获取消息发送方IMEI
     * 
     * @return
     */

    public String getSenderIMEI() {
        return senderIMEI;
    }

    /**
     * 设置消息发送方IMEI
     * 
     * @param paramSenderIMEI
     * 
     */
    public void setSenderIMEI(String paramSenderIMEI) {
        this.senderIMEI = paramSenderIMEI;
    }

    /**
     * 获取消息内容类型
     * 
     * @return
     * @see CONTENT_TYPE
     */
    public CONTENT_TYPE getContentType() {
        return contentType;
    }

    /**
     * 设置消息内容类型
     * 
     * @param paramContentType
     * @see CONTENT_TYPE
     */
    public void setContentType(CONTENT_TYPE paramContentType) {
        this.contentType = paramContentType;
    }

    /**
     * 获取消息发送时间
     * 
     * @return
     */
    public String getSendTime() {
        return sendTime;
    }

    /**
     * 设置消息发送时间
     * 
     * @param paramSendTime
     *            发送时间,格式 xx年xx月xx日 xx:xx:xx
     */
    public void setSendTime(String paramSendTime) {
        this.sendTime = paramSendTime;
    }

    /**
     * 获取消息内容
     * 
     * @return
     */
    public String getMsgContent() {
        return MsgContent;
    }

    /**
     * 设置消息内容
     * 
     * @param paramMsgContent
     */
    public void setMsgContent(String paramMsgContent) {
        this.MsgContent = paramMsgContent;
    }
    /**
     * 获取消息接收方IMEI
     * 
     * @return
     */

    public String getReceiverIMEI() {
        return receiveIMEI;
    }

    /**
     * 设置消息读取状态
     * 
     * @param paramSenderIMEI
     * 
     */
    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }
    
    /**
     * 获取消息接收方IMEI
     * 
     * @return
     */

    public int getReadStatus() {
        return readStatus;
    }

    /**
     * 设置消息接收方IMEI
     * 
     * @param paramSenderIMEI
     * 
     */
    public void setReceiverIMEI(String paramReceiverIMEI) {
        this.receiveIMEI = paramReceiverIMEI;
    }

    /**
     * 克隆对象
     * 
     * @param
     */

    public Message clone() {
        return new Message(senderIMEI, sendTime, MsgContent, contentType,readStatus/*,isGroup*/);//modify by lss for processing the situation which group is deleted
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }
    
    public String getReceiveIP(){
    	return receiveIP;
    }
    
    public void setReceiveIP(String receiveIP){
    	this.receiveIP = receiveIP;
    }
    
    public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	
	public long getmReservedID() {
		return mReservedID;
	}

	public void setmReservedID(long mReservedID) {
		this.mReservedID = mReservedID;
	}

	public String toString(){
    	return "senderIMEI:" + senderIMEI + " sendTime:" + sendTime + " MsgContent:" + MsgContent + " contentType:" + contentType
    			+" percent:" + percent + " recordTime:" + recordTime;
    }

//	@Override
//	public int describeContents() {
//		return 0;
//	}
//
//	@Override
//	public void writeToParcel(Parcel dest, int flags) {
//		dest.writeString(senderIMEI);
//		dest.writeString(sendTime);
//		dest.writeString(MsgContent);
//		switch (contentType) {
//		case TEXT:
//			dest.writeInt(CONTENT_TYPE.TEXT.ordinal());
//			break;
//		case IMAGE:
//			dest.writeInt(CONTENT_TYPE.IMAGE.ordinal());
//			break;
//		case FILE:
//			dest.writeInt(CONTENT_TYPE.FILE.ordinal());
//			break;
//		case VOICE:
//			dest.writeInt(CONTENT_TYPE.VOICE.ordinal());
//			break;
//		default:
//			break;
//		}
//		dest.writeInt(percent);
//		dest.writeInt(recordTime);
//		
//	}
//	
//	public Message(Parcel parcel){
//		senderIMEI = parcel.readString();
//		sendTime = parcel.readString();
//		MsgContent = parcel.readString();
//		contentType = CONTENT_TYPE.values()[parcel.readInt()];
//		percent = parcel.readInt();
//		recordTime = parcel.readInt();
//	}
//	
//    public static Parcelable.Creator<Message> getCreator() {
//        return CREATOR;
//    }
//    
//    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
//
//        @Override
//        public Message createFromParcel(Parcel source) {
//        	Message msg = new Message();
//            msg.setSenderIMEI(source.readString());
//            msg.setSendTime(source.readString());
//            msg.setMsgContent(source.readString());
//            msg.setContentType(CONTENT_TYPE.values()[source.readInt()]);
//            msg.setPercent(source.readInt());
//            msg.setRecordTime(source.readInt());
//            return msg;
//        }
//
//        @Override
//        public Message[] newArray(int size) {
//            return new Message[size];
//        }
//    };

}
