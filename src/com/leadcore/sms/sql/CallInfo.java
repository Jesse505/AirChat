package com.leadcore.sms.sql;


/**
 * 
 * @author lss
 * @since  2016-12-06
 * 
 */
public class CallInfo {
    private long id; // ID序号
    private String receiverIMEI;
    private String date; // 通话时间
    private int calltime; // 通话总时长
    private int callstatus;//通话方向，主叫CALLING(0)，被叫ANSWER(1)
    private String receiverIP;
    private String sendIMEI;
    private String callType;//add by lss for declear calltype (audio or video)
    private String receiverName;//add by lss for modifying the activity of calllog show senderName

    public CallInfo() {

    }

    public CallInfo( String receiverIMEI, int calltime, String date) {
       // this.sendID = sendID;
        this.receiverIMEI = receiverIMEI;
        this.calltime = calltime; 
        this.date=date;
        
    }

    public CallInfo(int id, String receiverIMEI,  int calltime, String date) {
        this(receiverIMEI, calltime,date);
        this.id = id;
    }
    /**
     * 
     * @param id         ID序号
     *
     * @param receiverID 被叫方在用户表格所对应的ID
     * @param date       聊天信息的记录时间
     * @param calltime   通话时长
     * @param callstyle;//通话方向，主叫CALLING(0)，被叫ANSWER(1)
     */
    public CallInfo(int id,String receiverIMEI, String date,  int calltime,int callstatus,String receiverIP) {
        this( receiverIMEI,calltime, date);
        this.id = id;
        this.callstatus=callstatus; 
        this.receiverIP=receiverIP;
    }
    //add by lss for declear calltype (audio or video)
    public CallInfo(String callType,String receiverIMEI, String date,  int calltime,int callstatus,String receiverIP,int id) {
        this( receiverIMEI,calltime, date);
        this.id = id;
        this.callstatus=callstatus; 
        this.receiverIP=receiverIP;
        this.callType=callType;
    }
    //add end
  //add by lss for modifying the activity of calllog show senderName
    public CallInfo(String callType,String receiverIMEI, String date,  int calltime,int callstatus,String receiverIP,int id,String receiverName) {
        this( receiverIMEI,calltime, date);
        this.id = id;
        this.callstatus=callstatus; 
        this.receiverIP=receiverIP;
        this.callType=callType;
        this.receiverName=receiverName;
    }
    //add end
    
    public CallInfo(int id,String receiverIMEI, String date,  int calltime,int callstatus,String receiverIP,String sendIMEI) {
        this( receiverIMEI,calltime, date);
        this.id = id;
        this.callstatus=callstatus; 
        this.receiverIP=receiverIP;
        this.sendIMEI=sendIMEI;
    } 
    
    /* 设置ID序列 */
    public void setID(long id) {
        this.id = id;
    }
    /* 获取序列ID */
    public long getId() {
        return id;
    }    
    
    /* 设置发送方IMEI序列 */
    public void setSendIMEI(String sendIMEI) {
        this.sendIMEI = sendIMEI;
    }
    /* 获取序列ID */
    public String getSendIMEI() {
        return sendIMEI;
    }    
    
    /* 设置接收方IP */
    public void setReceiverIP(String receiverIP) {
        this.receiverIP = receiverIP;
    }
    /* 获取接收方IP*/
    public String getReceiverIP() {
        return receiverIP;
    }
    /* 设置接收方IMEI */
    public void setReceiverIMEI(String receiverIMEI) {
        this.receiverIMEI = receiverIMEI;
    }

    /* 获取接收方IMEI */
    public String getReceiverIMEI() {
        return receiverIMEI;
    }

    /* 设置聊天时间 */
    public void setDate(String date) {
        this.date = date;
    }

    /* 获取聊天时间 */
    public String getDate() {
        return date;
    }   
    /* 设置来电去电 */
    public void setCallStatus(int callstatus) {
        this.callstatus = callstatus;
    }
    /* 获取来电去电*/
    public int getCallStatus() {
        return callstatus;
    }
    
    /* 设置通话时长 */
    public void setCallTime(int calltime){
    	this.calltime = calltime;
    }
     /* 获取通话时长*/
    public int getCallTime(){
    	return calltime;
    }
    
  //add by lss for declear calltype (audio or video)
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
    //modify by lss for modifying the activity of calllog show senderName
      public String getReceiverName(){
      	return receiverName;
      }
      
      public void setReceiverName(String receiverName){
      	this.receiverName = receiverName;
      }
      //add end
    
    /* 输出所有通话记录 */
    public String toString() {
        return "ID:" + getId()  + " receiverIMEI:" + getReceiverIMEI()
                + " date:" + getDate() +"callstatus："+callstatus + " calltime: " + calltime
                + " receiverIP: "+receiverIP+ " callType: "+callType;
    } 
}
