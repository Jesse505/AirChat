package com.leadcore.sms.sql;


/**
 * 
 * @author zhaoyifei
 * 
 */
public class ChattingInfo {
    private long id; // ID序号
    private int sendID; // 发送者在用户表格所对应的ID
    private String sendIMEI; // 发送者IMEI
    private int receiverID; // 接收方在用户表格所对应的ID
    private String date; // 聊天信息的记录时间
    private String info; // 聊天信息的内容
    private int style;//聊天信息类型 TEXT(0), IMAGE(1), FILE(2), VOICE(3);
    private int recordTime;//录音时长
    private int percent;//文件进度
    private int readStatus;//读取状态
    private String receiverIP;//add by lss for adding group message in message list
    private int isGroup;//add by lss for processing the situation which group is deleted

    // 以下是该类的构造函数
    public ChattingInfo() {

    }

    public ChattingInfo(int sendID, int receiverID, String date, String info) {
        this.sendID = sendID;
        this.receiverID = receiverID;
        this.date = date;
        this.info = info;
    }

    public ChattingInfo(int id, int sendID, int receiverID, String date, String info) {
        this(sendID, receiverID, date, info);
        this.id = id;
    }
    
    public ChattingInfo(int id, String sendIMEI, String date) {
        this.sendIMEI = sendIMEI;
        this.date = date; 
        this.id = id;
    }
    /**
     * 
     * @param id         ID序号
     * @param sendID     发送者在用户表格所对应的ID
     * @param receiverID 接收方在用户表格所对应的ID
     * @param date       聊天信息的记录时间
     * @param info       聊天信息的内容
     * @param style      聊天信息类型 TEXT(0), IMAGE(1), FILE(2), VOICE(3)
     */
    public ChattingInfo(int id, int sendID, int receiverID, String date, String info,int style) {
        this(sendID, receiverID, date, info);
        this.id = id;
        this.style=style;
    }
    
    public ChattingInfo(int id, int sendID, int receiverID, String date, int readStatus,String info) {
        this(sendID, receiverID, date, info);
        this.id = id;
        this.readStatus=readStatus;
    }
  //modify  by lss for processing the situation which group is deleted
    public ChattingInfo(int id, int sendID, int receiverID, String date, int readStatus,String info,int style,int isGroup) {
        this(sendID, receiverID, date, info);
        this.id = id;
        this.readStatus=readStatus;
        this.style=style;
        this.isGroup=isGroup;
    }
    //modify end
    //add by lss for adding group message in message list
    public ChattingInfo(int id, int sendID, int receiverID, String date, int readStatus,String info,int style,String receiverIP,int isGroup) {
        this(sendID, receiverID, date, info);
        this.id = id;
        this.readStatus=readStatus;
        this.style=style;
        this.receiverIP=receiverIP;
        this.isGroup=isGroup;//add by lss for processing the situation which group is deleted
    }
    //add end
    //add by lss for update group message list
    public ChattingInfo(int id, int sendID, String receiverIP,String content, String date,int style, int recordTime,int percent,String senderName) {
       this.sendID=sendID;
       this.recordTime=recordTime;
       this.info=content;
       this.date=date;
       this.percent=percent;
    
        this.id = id;       
        this.style=style;
        this.receiverIP=receiverIP;
        
    }
    //add end
    
    public ChattingInfo(int id, int sendID, int receiverID, String date, String info,int style,int recordTime,int percent) {
        this(sendID, receiverID, date, info);
        this.id = id;
        this.style=style;
        this.recordTime = recordTime;
        this.percent = percent;
    }
    
    public ChattingInfo(int id, int sendID, int receiverID, int readStatus,String date, String info) {
        this(sendID, receiverID, date, info);
        this.id = id;
        this.readStatus=readStatus;  
    }
    public ChattingInfo(int id, int sendID, int receiverID, int readStatus,String date, String info,int style) {
        this(sendID, receiverID, date, info);
        this.id = id;
        this.readStatus=readStatus;  
        this.style=style;
    }

    /* 设置ID序列 */
    public void setID(long id) {
        this.id = id;
    }

    /* 获取序列ID */
    public long getId() {
        return id;
    }
    
    /* 设置ID序列 */
    public void setReadStatus(int readStatus) {
        this.readStatus= readStatus;
    }

    /* 获取序列ID */
    public int getReadStatus() {
        return readStatus;
    }
    
    /* 设置发送方IMEI */
    public void setSenderIMEI(String senderImei) {
        this.sendIMEI = senderImei;
    }

    /* 获取发送方IMEI */
    public String getSenderIMEI() {
        return sendIMEI;
    }

    /* 设置发送方ID */
    public void setSendID(int sendID) {
        this.sendID = sendID;
    }

    /* 获取发送方ID */
    public int getSendID() {
        return sendID;
    }

    /* 设置接收方ID */
    public void setReceiverID(int receiverID) {
        this.receiverID = receiverID;
    }

    /* 获取接收方ID */
    public int getReceiverID() {
        return receiverID;
    }

    /* 设置聊天时间 */
    public void setDate(String date) {
        this.date = date;
    }

    /* 获取聊天时间 */
    public String getDate() {
        return date;
    }

    /* 设置聊天信息 */
    public void setInfo(String info) {
        this.info = info;
    }

    /* 获取聊天信息 */
    public String getInfo() {
        return info;
    }
    
    /* 设置聊天信息类型 */
    public void setSytle(int style) {
        this.style = style;
    }

    /* 获取聊天信息类型 */
    public int getStyle() {
        return style;
    }
    
    public void setRecordTime(int recordTime){
    	this.recordTime = recordTime;
    }
    
    public int getRecordTime(){
    	return recordTime;
    }
    
    public void setPercent(int percent){
    	this.percent = percent;
    }
    
    public int getPercent(){
    	return percent;
    }
  //add by lss for adding group message in message list
    public void setReceiverIP(String receiverIP){
    	this.receiverIP = receiverIP;
    }
    
    public String getReceiverIP(){
    	return receiverIP;
    }
    //add end
   // add by lss for processing the situation which group is deleted
    public void setIsGroup(int isGroup){
    	this.isGroup = isGroup;
    }
    
    public int getIsGroup(){
    	return isGroup;
    }
    //add end
    
    /* 输出所有聊天信息 */
    public String toString() {
        return "ID:" + getId() + " sendID:" + getSendID() + " receiverID:" + getReceiverID()
                + " date:" + getDate() + " info:" + getInfo()+" style："+style + " recordTime: " + recordTime
                + " receiverIP: " + receiverIP;
    }
}
