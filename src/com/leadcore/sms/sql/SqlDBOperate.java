package com.leadcore.sms.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONStringer;
import org.sipdroid.sipua.utils.DateUtils;
import org.sipdroid.sipua.utils.MyLog;

import com.leadcore.sms.entity.Call;
import com.leadcore.sms.entity.Group;
import com.leadcore.sms.entity.Message;
import com.leadcore.sms.entity.Message.CONTENT_TYPE;
import com.leadcore.sms.entity.Users;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SqlDBOperate
{
	 private ChattingDBHelper chatInfoSQLHelper; // 聊天内容数据库操作类
	 private SQLiteDatabase chatInfoDataBase; 
	 private UserDBHelper userSQLHelper;         // 用户信息数据库操作类
	 private SQLiteDatabase userDataBase;
	 private GroupDBHelper groupSQLHelper;       // 组信息数据库操作类
	 private SQLiteDatabase groupDatabase;
	 private CallDBHelper callSQLHelper;         // 通话记录数据库操作类
	 private SQLiteDatabase callDatabase;
	 public int tableCount;//表message_chatting总记录数
	 
	/*
     * 构造函数参数：context对象通过db的方法来操作数据库的增删改查
     */
    public SqlDBOperate(Context context) {
    		chatInfoSQLHelper=new ChattingDBHelper(context);
    		chatInfoDataBase=chatInfoSQLHelper.getWritableDatabase();
    		userSQLHelper=new UserDBHelper(context);
    		userDataBase=userSQLHelper.getWritableDatabase();
    		groupSQLHelper = new GroupDBHelper(context);
    		groupDatabase = groupSQLHelper.getWritableDatabase();
    		callSQLHelper = new CallDBHelper(context);
    		callDatabase = callSQLHelper.getWritableDatabase();
    }
    /* 关闭数据库 */
    public void close() {
    	userSQLHelper.close();
    	userDataBase.close();
    	chatInfoSQLHelper.close();
    	chatInfoDataBase.close();
    	groupSQLHelper.close();
    	groupDatabase.close();
    	callSQLHelper.close();
		callDatabase.close();
    }
    
    //*************************** User DB Method start********************************************/
    
    /**
     * 添加用户信息至数据库 
     * @param user
     */
    public void addUserInfo(UserInfo user) {

        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("IMEI", user.getIMEI());
        values.put("ip", user.getIPAddr());
        int id=getIDByIMEI(user.getIMEI());
        if(id!=0)
        {
        	user.setId(id);
        	updateUserInfo(user);
        }
        else
        	userDataBase.insert(userSQLHelper.getTableName(), "id", values);
    }
    
    /**
     * 添加用户信息至数据库
     * @param people
     */
    public void addUserInfo(Users people)
    {
    	 ContentValues values = new ContentValues();
         values.put("name", people.getNickname());
         values.put("IMEI", people.getIMEI());
         values.put("ip", people.getIpaddress());
         int id=getIDByIMEI(people.getIMEI());
         if(id!=0)
         {
        	 userDataBase.update(userSQLHelper.getTableName(), values, "id = ?",
                     new String[] { String.valueOf(id) });
         }
         else
         	userDataBase.insert(userSQLHelper.getTableName(), "id", values);
    }
    
    /**
     * 参数：userInfo类 作用：用来更用户信息
     */
    public void updateUserInfo(UserInfo user) {
        // db=helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("IMEI", user.getIMEI());
        values.put("ip", user.getIPAddr());
        userDataBase.update(userSQLHelper.getTableName(), values, "id = ?",
                new String[] { String.valueOf(user.getId()) });
    }
    
    /**
     * 参数：用户的IMEI序列码结果返回IMEI码对应用户的ID
     */
    public int getIDByIMEI(String imei) {
        Cursor cursor = userDataBase.query(userSQLHelper.getTableName(), new String[] { "id" }, "IMEI=?",
                new String[] { imei }, null, null, null);
        if (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            cursor.close();
            return id;
        }
        cursor.close();
        return 0;
    }
    

    /*
     * 参数：用户对应序号ID 作用:用来查找对应的用户 返回IMEI
     */
    public String getIMEIByUserID(int id)
	{
    	Cursor cursor = userDataBase.query(userSQLHelper.getTableName(), new String[] {"IMEI"}, "id=?",
    			new String[] { String.valueOf(id) },null, null, null);
    	if (cursor.moveToNext()) {
    		String IMEI=cursor.getString(cursor.getColumnIndex("IMEI"));
            cursor.close();
            return IMEI;
        }
        cursor.close();
        return null;
	}

    /**
     * 参数：用户对应的IMEI码 作用:用来查找对应的用户 返回userInfo类
     */
    public UserInfo getUserInfoByIMEI(String imei) {
        Cursor cursor = userDataBase.query(userSQLHelper.getTableName(), new String[] { "id", "name",  "IMEI",
                 "ip", "status"}, "IMEI=?", new String[] { imei }, null, null,
                null);
        if (cursor.moveToNext()) {
            UserInfo userInfo = new UserInfo(cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("name")), 
                    cursor.getString(cursor.getColumnIndex("IMEI")),
                    cursor.getString(cursor.getColumnIndex("ip")));
            cursor.close();
            return userInfo;
        }
        cursor.close();
        return null;
    }

    //*************************** User DB Method end********************************************/

    //*************************** Chatting Info DB Method start****************************************/
    /**
     * 
     * @param senderID
     * @param recieverID
     * @param time
     * @param content
     * @param type
     * @param recordTime
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long addChattingInfo(int senderID,int recieverID,String time,String content,CONTENT_TYPE type,int recordTime) {

        ContentValues values = new ContentValues();
        values.put("sendID", senderID);
        values.put("receiverID", recieverID);
        values.put("chatting", content);
        values.put("date", time);
        values.put("style", getStyteByContentType(type));
        values.put("recordTime", recordTime);
        return chatInfoDataBase.insert(chatInfoSQLHelper.getTableName(), "id", values);
    }
    //add by lss for display message list
    /**
     * 短信列表显示
     * @param senderID 发送发ID
     * @param recieverID 接收方ID
     * @param time  发送时间
     * @param content  短信内容
     * @param type  短信类型
     * @param recordTime  录音时间
     */
    public void queryNewChattingInfo(int senderID,int recieverID,String time,String content,CONTENT_TYPE type,int recordTime){
    	ContentValues values = new ContentValues();
    	int countMessageChatting = getMessageChattingCount(senderID,recieverID);
    	MyLog.d("lss","countMessageChatting=="+countMessageChatting);
    	ChattingInfo info;
    	if(countMessageChatting==0){
    			//将最新一条数据插入message_chatting
    			values.put("sendID",senderID);
  		        values.put("receiverID",recieverID);
  		        values.put("chatting",content);
  		        values.put("date", time);
  		        values.put("readstatus", 0);
  		        values.put("style",  getStyteByContentType(type));
  		        values.put("isgroup", 0);//add by lss for processing the situation which group is deleted
  		        MyLog.d("lss","sendID"+senderID);
  		        chatInfoDataBase.insert(chatInfoSQLHelper.getMessageTableName(), "id", values);    			
    		}else{
    			Message me=queryNewOneChatting(senderID,recieverID,"",time,content,type,recordTime);
    			MyLog.d("lss","me+++++"+me);
    			info = getChatInfoByID(me.getId());       			
       			if(info!=null){
       					info.setSendID (senderID);
       				 MyLog.d("lss","sendID"+senderID);
       					info.setReceiverID(recieverID);
    					info.setInfo(me.getMsgContent());
    					info.setDate(time);
    					info.setReadStatus(0);
    					info.setSytle(getStyteByContentType(type));
    					info.setIsGroup(0);//add by lss for processing the situation which group is deleted
    					updateChatInfo(info);    					
    				}     	    			 
    		}      
    }
    //add by lss for group
    public void queryNewChattingInfo(int senderID,int recieverID,String time,String content,CONTENT_TYPE type,int recordTime,String receiverIP){
    	ContentValues values = new ContentValues();
    	if((!"".equals(receiverIP))&&(receiverIP!=null)){
    		//group
    		int countMessageChatting = getMessageChattingCount(senderID,recieverID,receiverIP);
        	MyLog.d("lss","group 1countMessageChatting=="+countMessageChatting);
        	ChattingInfo info;   		
        	if(countMessageChatting==0){
        			//将最新一条数据插入message_chatting
        				values.put("sendID", senderID);
        		        values.put("receiverID",recieverID);
        		        values.put("chatting",content);
        		        values.put("date", time);
        		        values.put("readstatus", 0);
        		        values.put("style",  getStyteByContentType(type));
        		        values.put("receiverIP", receiverIP);
        		        values.put("isgroup", 1);//add by lss for processing the situation which group is deleted
        		        MyLog.d("lss","receiverIP"+receiverIP);
        		        chatInfoDataBase.insert(chatInfoSQLHelper.getMessageTableName(), "id", values);
        		
        		}else{
        			Message me=queryNewOneChatting(senderID,recieverID,receiverIP,time,content,type,recordTime);
        			MyLog.d("lss","me==="+me);
        			info = getGroupChatInfoByID(me.getId());    
        			MyLog.d("lss","update group message list when sendID");
        				if(info!=null){
        					info.setSendID (senderID);
               				info.setReceiverID(recieverID);
        					info.setInfo(content);
        					info.setDate(time);
        					info.setReadStatus(0);
        					info.setSytle(getStyteByContentType(type));
        					info.setReceiverIP(receiverIP);
        					info.setIsGroup(1);//add by lss for processing the situation which group is deleted,1 is group 0 is p2p
        					MyLog.d("lss","senderID"+senderID);
        					updateChatInfo(info);    					
        				}     	
        		}
    	}else{
    		int countMessageChatting = getMessageChattingCount(senderID,recieverID);
        	MyLog.d("lss","countMessageChatting=="+countMessageChatting);
        	ChattingInfo info;
        	if(countMessageChatting==0){
        			//将最新一条数据插入message_chatting
        			values.put("sendID",senderID);
      		        values.put("receiverID",recieverID);
      		        values.put("chatting",content);
      		        values.put("date", time);
      		        values.put("readstatus", 0);
      		        values.put("style",  getStyteByContentType(type));
      		        values.put("isgroup", 0);//add by lss for processing the situation which group is deleted
      		        MyLog.d("lss","sendID"+senderID);
      		        chatInfoDataBase.insert(chatInfoSQLHelper.getMessageTableName(), "id", values);    			
        		}else{
        			Message me=queryNewOneChatting(senderID,recieverID,"",time,content,type,recordTime);
        			MyLog.d("lss","me+++++"+me);
        			info = getChatInfoByID(me.getId());       			
           			if(info!=null){
           					info.setSendID (senderID);
           				 MyLog.d("lss","sendID"+senderID);
           					info.setReceiverID(recieverID);
        					info.setInfo(me.getMsgContent());
        					info.setDate(time);
        					info.setReadStatus(0);
        					info.setSytle(getStyteByContentType(type));
        					info.setIsGroup(0);//add by lss for processing the situation which group is deleted
        					updateChatInfo(info);    					
        				}     	    			 
        		}      
    	}
    	
    }
    //add end
    /**
     * 短信列表显示
     * @param senderIMEI  发送方IMEI
     * @param recieverIMEI 接收方IMEI
     * @param time　　发送时间
     * @param content　短信内容
     * @param type  短信类型
     * @param recordTime  录音时间
     * @param receiverIP  群组IP
     */
    public void queryNewChattingInfo(String senderIMEI,String recieverIMEI,String receiverIP,String time,String content,CONTENT_TYPE type,int recordTime){   	
    	ContentValues values = new ContentValues();
    	 MyLog.d("lss","receiverIP"+receiverIP+"::content="+content);
    	if((!"".equals(receiverIP))&&(receiverIP!=null)){
    		//group
    		int countMessageChatting = getMessageChattingCount(getIDByIMEI(senderIMEI),getIDByIMEI(recieverIMEI),receiverIP);
        	MyLog.d("lss","group 2countMessageChatting=="+countMessageChatting);
        	ChattingInfo info;   		
        	if(countMessageChatting==0){
        			//将最新一条数据插入message_chatting
        				values.put("sendID", getIDByIMEI(senderIMEI));
        		        values.put("receiverID",getIDByIMEI(recieverIMEI));
        		        values.put("chatting",content);
        		        values.put("date", time);
        		        values.put("readstatus", 0);
        		        values.put("style",  getStyteByContentType(type));
        		        values.put("receiverIP", receiverIP);
        		        values.put("isgroup", 1);//add by lss for processing the situation which group is deleted
        		        MyLog.d("lss","receiverIP"+receiverIP);
        		        chatInfoDataBase.insert(chatInfoSQLHelper.getMessageTableName(), "id", values);
        		
        		}else{
        			Message me=queryNewOneChatting(getIDByIMEI(senderIMEI),getIDByIMEI(recieverIMEI),receiverIP,time,content,type,recordTime);
        			MyLog.d("lss","group update me==="+me);
        			info = getGroupChatInfoByID(me.getId()); 
        			MyLog.d("lss","update group info =="+info);
        				if(info!=null){
        					info.setSendID (getIDByIMEI(senderIMEI));
               				info.setReceiverID(getIDByIMEI(recieverIMEI));
        					info.setInfo(content);
        					info.setDate(time);
        					info.setReadStatus(0);
        					info.setSytle(getStyteByContentType(type));
        					info.setReceiverIP(receiverIP);
        					info.setIsGroup(1);//add by lss for processing the situation which group is deleted,1 is group 0 is p2p
        					MyLog.d("lss","getIDByIMEI(senderIMEI)"+getIDByIMEI(senderIMEI)+"::info="+info);
        					updateChatInfo(info);    					
        				}     	
        		}
    	}
    	else{
    		
        	//not group
    		int countMessageChatting = getMessageChattingCount(getIDByIMEI(senderIMEI),getIDByIMEI(recieverIMEI),receiverIP);
        	MyLog.d("lss","2countMessageChatting=="+countMessageChatting);
        	ChattingInfo info;   		
        	if(countMessageChatting==0){
        			//将最新一条数据插入message_chatting
        				values.put("sendID", getIDByIMEI(senderIMEI));
        		        values.put("receiverID",getIDByIMEI(recieverIMEI));
        		        values.put("chatting",content);
        		        values.put("date", time);
        		        values.put("readstatus", 0);
        		        values.put("style",  getStyteByContentType(type));
        		        values.put("isgroup", 0);//add by lss for processing the situation which group is deleted
        		        MyLog.d("lss","getIDByIMEI(senderIMEI)"+getIDByIMEI(senderIMEI));
        		        chatInfoDataBase.insert(chatInfoSQLHelper.getMessageTableName(), "id", values);
        		
        		}else{
        			Message me=queryNewOneChatting(getIDByIMEI(senderIMEI),getIDByIMEI(recieverIMEI),receiverIP,time,content,type,recordTime);
        			MyLog.d("lss","me==="+me);
        			info = getChatInfoByID(me.getId());       			       			
        				if(info!=null){
        					info.setSendID (getIDByIMEI(senderIMEI));
               				info.setReceiverID(getIDByIMEI(recieverIMEI));
        					info.setInfo(content);
        					info.setDate(time);
        					info.setReadStatus(0);
        					info.setSytle(getStyteByContentType(type));
        					info.setIsGroup(0);//add by lss for processing the situation which group is deleted
        					MyLog.d("lss","getIDByIMEI(senderIMEI)"+getIDByIMEI(senderIMEI));
        					updateChatInfo(info);    					
        				}     	
        		}
    	}
    	
}
    /**
     * 查询相同两个终端之间最新的一条信息
     * @param senderID
     * @param recieverID
     * @param time
     * @param content
     * @param type
     * @param recordTime
     * @return
     */
    public Message queryNewOneChatting(int senderID,int recieverID,String receiverIP,String time,String content,CONTENT_TYPE type,int recordTime){    	
       if((!"".equals(receiverIP))&&(receiverIP!=null)){
    	 //group
    	   Cursor cursor = chatInfoDataBase.query(chatInfoSQLHelper.getMessageTableName(),
    			   new String[] { "id", "sendID","receiverID", "chatting", "date" ,"readstatus","style","receiverIP","isgroup"},  
           		"receiverIP=? ",new String[] { String.valueOf(receiverIP)}, null, null, "id desc ", null);
           Message message=null;
          if (cursor.moveToNext()) {
       	   message=chattingInfoToMessage(new ChattingInfo(cursor.getInt(cursor.getColumnIndex("id")), cursor
                   .getInt(cursor.getColumnIndex ("sendID")), cursor.getInt(cursor
                   .getColumnIndex("receiverID")),cursor.getString(cursor.getColumnIndex("date")) ,
                   cursor.getInt(cursor.getColumnIndex("readstatus"))
                   ,cursor.getString(cursor.getColumnIndex("chatting"))
                   ,cursor.getInt(cursor.getColumnIndex("style")),
                   cursor.getString(cursor.getColumnIndex("receiverIP")),
                   cursor.getInt(cursor.getColumnIndex("isgroup"))));//modify  by lss for processing the situation which group is deleted  
           }
           cursor.close();       
           return message;
       }else{
       
         //not group  
    	   Cursor cursor = chatInfoDataBase.query(chatInfoSQLHelper.getMessageTableName(), new String[] { "id", "sendID","receiverID", "chatting", "date" ,"readstatus","style","isgroup"},  
           		"(sendID=? and receiverID=?) or (receiverID=? and sendID=?)",
                   new String[] { String.valueOf(senderID), String.valueOf(recieverID),String.valueOf(senderID), String.valueOf(recieverID) }, null, null, "id desc ", null);
           Message message=null;
          if (cursor.moveToNext()) {
       	   message=chattingInfoToMessage(new ChattingInfo(cursor.getInt(cursor.getColumnIndex("id")), cursor
                      .getInt(cursor.getColumnIndex ("sendID")), cursor.getInt(cursor
                      .getColumnIndex("receiverID")),cursor.getString(cursor.getColumnIndex("date")) ,
                      cursor.getInt(cursor.getColumnIndex("readstatus"))
                      ,cursor.getString(cursor.getColumnIndex("chatting"))
                      ,cursor.getInt(cursor.getColumnIndex("style")),
                      cursor.getInt(cursor.getColumnIndex("isgroup")))); //isgroup modify  by lss for processing the situation which group is deleted 
           }
           cursor.close();       
           return message;
       }
    	
    }
    
    //add end
    
    private int getStyteByContentType(CONTENT_TYPE type)
    {
    	if(type==CONTENT_TYPE.TEXT)
    	{
    		return 0;
    	}else if(type==CONTENT_TYPE.IMAGE)
    	{
    		return 1;
    	}else if (type==CONTENT_TYPE.FILE)
		{
			return 2;
		}else if(type==CONTENT_TYPE.VOICE)
		{
			return 3;
		}
    	return -1;
    }

    
    /**
     * 
     * @param senderIMEI
     * @param recieverIMEI
     * @param time
     * @param content
     * @param type
     * @return 
     */
    public long addChattingInfo(String senderIMEI,String recieverIMEI,String time,String content,CONTENT_TYPE type,int recordTime) {

        ContentValues values = new ContentValues();
        values.put("sendID", getIDByIMEI(senderIMEI));
        values.put("receiverID", getIDByIMEI(recieverIMEI));
        values.put("chatting", content);
        values.put("date", time);
        values.put("style", getStyteByContentType(type));
        values.put("recordTime", recordTime);
        return chatInfoDataBase.insert(chatInfoSQLHelper.getTableName(), "id", values);
    }
    
    /**
     * 参数：chattinginfo类 作用：用来更新文件的percent
     */
    public void updateChattingInfo(ChattingInfo info) {
        // db=helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("percent", info.getPercent());
        chatInfoDataBase.update(chatInfoSQLHelper.getTableName(), values, "id = ?",
                new String[] { String.valueOf(info.getId()) });
    }


    /*
     * 删除所有聊天记录
     */
    public void deteleAllChattingInfo()
    {
    	chatInfoDataBase.delete(chatInfoSQLHelper.getTableName(), null, null);
    }

    /**
     * 用来获取近期的一系列聊天记录 参数:start为开始位置，count为最大记录数，(倒序排列) 放回List<chattingInfo>
     */
    public List<Message> getScrollMessageOfChattingInfo(int start, int count,int senderID,int recieverID) {
        List<Message> messages = new ArrayList<Message>();
        Cursor cursor = chatInfoDataBase.query(chatInfoSQLHelper.getTableName(), new String[] { "id", "sendID",
                "receiverID", "chatting", "date" ,"style","recordTime","percent"},  "(sendID=? and receiverID=?) or (receiverID=? and sendID=?)",
                new String[] { String.valueOf(senderID), String.valueOf(recieverID),String.valueOf(senderID), String.valueOf(recieverID) }, null, null, "id desc", start + ","
                + count);
        
        while (cursor.moveToNext()) {
        	Message message=chattingInfoToMessage(new ChattingInfo(cursor.getInt(cursor.getColumnIndex("id")), cursor
                    .getInt(cursor.getColumnIndex("sendID")), cursor.getInt(cursor
                    .getColumnIndex("receiverID")), cursor.getString(cursor.getColumnIndex("date")),
                    cursor.getString(cursor.getColumnIndex("chatting")), 
                    cursor.getInt(cursor.getColumnIndex("style")),
                    cursor.getInt(cursor.getColumnIndex("recordTime")),
                    cursor.getInt(cursor.getColumnIndex("percent"))));
           messages.add(message);
        }
        cursor.close();
        Collections.reverse(messages);//更正最后一条数据存在最后的BUG
        return messages;
    }

    private Message chattingInfoToMessage(ChattingInfo chattingInfo)
    {
    	Message message=new Message();
    	message.setID(chattingInfo.getId());   	
    	message.setMsgContent(chattingInfo.getInfo()); //设置聊天信息内容
    	message.setSendTime(chattingInfo.getDate());//设置聊天信息发送时间
    	switch (chattingInfo.getStyle())//设置聊天信息类型
    	{
    		case 0:
    			message.setContentType(CONTENT_TYPE.TEXT);
    			break;
    		case 1:
    			message.setContentType(CONTENT_TYPE.IMAGE);
    			break;
    		case 2:
    			message.setContentType(CONTENT_TYPE.FILE);
    			break;
    		case 3:
    			message.setContentType(CONTENT_TYPE.VOICE);
    			break;
    	}
    	message.setSenderIMEI(getIMEIByUserID(chattingInfo.getSendID()));//设置发送方IMEI
    	MyLog.d("lss","message.setSenderIMEI="+message.getSenderIMEI()
    			+"----getIMEIByUserID(chattingInfo.getSendID())"+getIMEIByUserID(chattingInfo.getSendID())
    			+"----chattingInfo.getSendID()"+chattingInfo.getSendID());
    	message.setReceiverIMEI(getIMEIByUserID(chattingInfo.getReceiverID()));//设置接收方IMEI
    	message.setRecordTime(chattingInfo.getRecordTime());
    	message.setPercent(chattingInfo.getPercent());
    	message.setReadStatus(chattingInfo.getReadStatus());
    	message.setReceiveIP(chattingInfo.getReceiverIP());//add by lss for adding group message in message list
    	message.setIsGroup(chattingInfo.getIsGroup()); //add by lss for processing the situation which group is deleted
    	MyLog.d("lss","group message ="+message+":::message.getReceiverIP="+message.getReceiveIP()+
    			"message.getMsgContent()="+message.getMsgContent());
    	return message;
    }
    
   /**
    * 添加最新短消息到数据库
    * @param senderID
    * @param recieverID
    * @param time
    * @param content
    * @param readStatus  短消息读取状态
    * @param type
    */
   public void addChatMessageInfo(int senderID,int recieverID,String time,String content,int readStatus,CONTENT_TYPE type) {

        ContentValues values = new ContentValues();
        values.put("sendID", senderID);
        values.put("receiverID", recieverID);
        values.put("chatting", content);
        values.put("date", time);
        values.put("readstatus", readStatus);
        values.put("style",  getStyteByContentType(type));
        chatInfoDataBase.insert(chatInfoSQLHelper.getMessageTableName(), "id", values);
    }
 
    
    /**
     * 添加最新短消息到数据库
     * @param senderIMEI
     * @param recieverIMEI
     * @param time
     * @param content
     * @param readStatus   短消息读取状态
     * @param type
     */
    public void addChatMessageInfo(String senderIMEI,String recieverIMEI,String time,String content,int readStatus,CONTENT_TYPE type) {

        ContentValues values = new ContentValues();
        values.put("sendID", getIDByIMEI(senderIMEI));
        values.put("receiverID", getIDByIMEI(recieverIMEI));
        values.put("chatting", content);
        values.put("date", time);
        values.put("readstatus", readStatus);
        values.put("style",  getStyteByContentType(type));
        chatInfoDataBase.insert(chatInfoSQLHelper.getMessageTableName(), "id", values);
    }
    
   
    
    /**
     * 参数：chattinginfo类 作用：用来更新短信读取状态
     */
    public void updateChatReadStatusInfo(ChattingInfo info) {
    	 ContentValues values = new ContentValues();
         values.put("readstatus", info.getReadStatus());
         chatInfoDataBase.update(chatInfoSQLHelper.getMessageTableName(), values, "id = ?",
                 new String[] { String.valueOf(info.getId()) });
      
    }
    
    /**
     * 参数：chattinginfo类 作用：用来更新短信列表中的内容,发送时间，读取状态(update including group and p2p)
     */
    public void updateChatInfo(ChattingInfo info) {
    	 ContentValues values = new ContentValues();
    	 MyLog.d("lss","info"+info+"-----info.getSenderIMEI()==="+info.getSendID());
    	 values.put("chatting", info.getInfo());
         values.put("date", info.getDate());
         values.put("readstatus", info.getReadStatus());     
         values.put("sendID", info.getSendID());         
         values.put("receiverID",info.getReceiverID());
         values.put("style", info.getStyle());
    	 //modify by lss for adding group message in message list    	 
    	 if((!"".equals(info.getReceiverIP())) && info.getReceiverIP()!=null){
    		 // group 
    		 MyLog.d("lss","this is group update!");
    		 values.put("receiverIP", info.getReceiverIP());
    		 chatInfoDataBase.update(chatInfoSQLHelper.getMessageTableName(), values, "id=?",
            		 new String[] { String.valueOf(info.getId()) });    		 
    	 }else{
    		 //not group 
    		 MyLog.d("lss","this is p2p update!");
    		 chatInfoDataBase.update(chatInfoSQLHelper.getMessageTableName(), values, "id=?",
            		 new String[] { String.valueOf(info.getId()) });
    	 }                  
    }
  

    /**
     * 参数：聊天记录序号ID 作用:用来查找对应的一条聊天记录 返回chattinginfo类
     */
    public ChattingInfo getChatMessageInfoByID(long id) {
        
        Cursor cursor = chatInfoDataBase.query(chatInfoSQLHelper.getMessageTableName(), new String[] { "id", "sendID",
                "receiverID", "chatting", "date","readstatus","style","receiverIP","isgroup" }, "id=?", new String[] { String.valueOf(id) },
                null, null, null);
        if (cursor.moveToNext()) {
            ChattingInfo chattingInfo = new ChattingInfo(cursor.getInt(cursor.getColumnIndex("id")), 
		            		cursor.getInt(cursor.getColumnIndex ("sendID")),
		            		cursor.getInt(cursor.getColumnIndex("receiverID")),
		            		cursor.getString(cursor.getColumnIndex("date")) ,
                            cursor.getInt(cursor.getColumnIndex("readstatus")),
                            cursor.getString(cursor.getColumnIndex("chatting")),
                            cursor.getInt(cursor.getColumnIndex("style")),
                            cursor.getString(cursor.getColumnIndex("receiverIP")),
                            cursor.getInt(cursor.getColumnIndex("isgroup")));  //isgroup add by lss for processing the situation which group is deleted
            cursor.close();
            return chattingInfo;
        }
        return null;
    }
    //lss 获取id
public ChattingInfo getChatInfoByID(long id) {
        
        Cursor cursor = chatInfoDataBase.query(chatInfoSQLHelper.getTableName(), new String[] { "id", "sendID",
                "receiverID", "chatting", "date","style" }, "id=?", new String[] { String.valueOf(id) },
                null, null, null);
        if (cursor.moveToNext()) {
            ChattingInfo chattingInfo = new ChattingInfo(
                    cursor.getInt(cursor.getColumnIndex("id")), cursor.getInt(cursor
                            .getColumnIndex("sendID")), cursor.getInt(cursor
                            .getColumnIndex("receiverID")) ,cursor.getString(cursor
                                            .getColumnIndex("date")),cursor.getString(cursor
                                                    .getColumnIndex("chatting"))
                                                    ,cursor.getInt(cursor.getColumnIndex("style")));
            cursor.close();
            return chattingInfo;
        }
        return null;
    }
//add by lss for getting groupinfo id when update group message list
public ChattingInfo getGroupChatInfoByID(long id) {
    
    Cursor cursor = chatInfoDataBase.query(chatInfoSQLHelper.getGroupTableName(), new String[] { "id", "sendID",
            "receiveIP", "chatting", "date","style","recordTime","percent","senderName" }, "id=?", new String[] { String.valueOf(id) },
            null, null, null);
    if (cursor.moveToNext()) {
        ChattingInfo chattingInfo = new ChattingInfo(
                cursor.getInt(cursor.getColumnIndex("id")),
                cursor.getInt(cursor.getColumnIndex("sendID")),
                cursor.getString(cursor.getColumnIndex("receiveIP")), 
                cursor.getString(cursor.getColumnIndex("chatting")) ,
                cursor.getString(cursor.getColumnIndex("date")),
                cursor.getInt(cursor.getColumnIndex("style")),
                cursor.getInt(cursor.getColumnIndex("recordTime")),
                cursor.getInt(cursor.getColumnIndex("percent")),
                cursor.getString(cursor.getColumnIndex("senderName")));
        cursor.close();
        return chattingInfo;
    }
    return null;
}
//add end


    /*
     * 删除所有聊天记录
     */
    public void deteleAllChatMessageInfo()
    {
    	chatInfoDataBase.delete(chatInfoSQLHelper.getMessageTableName(), null, null);
    }
    
    //add by lss for delete message info by receiver id
    /**
     * 参数：通过发送者ID来删除聊天记录
     */
    public void deteleChatMessageInfo(ChattingInfo info) {
    	
    	//modify  by lss for adding group message in message list
		if((!"".equals(info.getReceiverIP()))&&(info.getReceiverIP()!=null)){
			//group
			chatInfoDataBase.delete(chatInfoSQLHelper.getMessageTableName(), "receiverIP=? ",
					new String[] { String.valueOf(info.getReceiverIP())}); 

			groupDatabase.delete(groupSQLHelper.getGroupName(), "groupIP=?"
	    			,new String[] { String.valueOf(info.getReceiverIP()) });
		}else{
			//not group
			chatInfoDataBase.delete(chatInfoSQLHelper.getMessageTableName(), "sendID=? or receiverID=?"
	    			,new String[]{String.valueOf(info.getSendID()),String.valueOf(info.getSendID())}); 
	    	
	    	chatInfoDataBase.delete(chatInfoSQLHelper.getTableName(), "sendID=? or receiverID=?"
	    			,new String[]{String.valueOf(info.getSendID()),String.valueOf(info.getSendID())});
		}
		//add end
    }
	//add by lss for delete group message in table messagechatting
public void deteleChatMessageGroupInfo(String groupIp) {
			chatInfoDataBase.delete(chatInfoSQLHelper.getMessageTableName(), "receiverIP=? ",
					new String[] { String.valueOf(groupIp)}); 
    }
	//add end
    /**
     * author ：liushasha
     * add for adding group message in message list
     * 查询chatting表总记录数
     */
    public int getMessageChattingCount(int sendId,int receiverId,String receiverIP){
    	if((!"".equals(receiverIP))&&(receiverIP!=null)){
    		//group
    		Cursor cursor =  chatInfoDataBase.query(true, chatInfoSQLHelper.getMessageTableName(),
            		new String[] {"id", "sendID","receiverID", "chatting", "date" ,"readstatus","style","receiverIP","isgroup"},
            		"receiverIP=? ",new String[] { String.valueOf(receiverIP)},null ,null, "id",null,null);
        	tableCount=cursor.getCount();
        	MyLog.d("lss","group tableCount="+tableCount);
        	cursor.close();
        	return tableCount;  	
    	}else{
    		
        	//not group
    		Cursor cursor =  chatInfoDataBase.query(true, chatInfoSQLHelper.getMessageTableName(),
            		new String[] {"id", "sendID","receiverID", "chatting", "date" ,"readstatus","style","receiverIP","isgroup"},
            		"(sendID=? and receiverID=?) or (receiverID=? and sendID=?)",
                    new String[] { String.valueOf(sendId), String.valueOf(receiverId),String.valueOf(sendId), String.valueOf(receiverId) },null ,null, "id",null,null);
        	tableCount=cursor.getCount();
        	MyLog.d("lss","p2p tableCount="+tableCount);
        	cursor.close();
        	return tableCount;  
    	}	
    }
    /**
     * 
     * @param sendId 发送方ID
     * @param receiverId 接收方ID
     * @return
     */
    public int getMessageChattingCount(int sendId,int receiverId){
    	
    		
    		Cursor cursor =  chatInfoDataBase.query(true, chatInfoSQLHelper.getMessageTableName(),
            		new String[] {"id", "sendID","receiverID", "chatting", "date" ,"readstatus","style","receiverIP","isgroup"},
            		"(sendID=? and receiverID=?) or (receiverID=? and sendID=?)",
                    new String[] { String.valueOf(sendId), String.valueOf(receiverId),String.valueOf(sendId), String.valueOf(receiverId) },null ,null, "id",null,null);
        	tableCount=cursor.getCount();
        	MyLog.d("lss","tableCount="+tableCount);
        	cursor.close();
        	return tableCount;  	
    	
    }
   
    
    /**
     * 用来获取最新的一系列聊天记录 参数:start为开始位置，count为最大记录数，(倒序排列) 放回List<chattingInfo>
     */
    public List<Message> getScrollChatMessageOfChattingInfo(int start,int receiverID) {
        List<Message> messages = new ArrayList<Message>();
      //add receiverIP by lss for adding group message in message list
        Cursor cursor =  chatInfoDataBase.query(true, chatInfoSQLHelper.getMessageTableName(),
        		new String[] { "id", "sendID",
            "receiverID", "chatting", "date","readstatus","style","receiverIP" ,"isgroup"},null,
            null,null,null, "id",null,null); 
        
        while (cursor.moveToNext()) {
        	Message message=chattingInfoToMessage(new ChattingInfo(cursor.getInt(cursor.getColumnIndex("id")), cursor
                    .getInt(cursor.getColumnIndex ("sendID")), cursor.getInt(cursor
                    .getColumnIndex("receiverID")),cursor.getString(cursor.getColumnIndex("date")) ,
                    cursor.getInt(cursor.getColumnIndex("readstatus"))
                    ,cursor.getString(cursor.getColumnIndex("chatting"))
                    ,cursor.getInt(cursor.getColumnIndex("style")),
                    cursor.getString(cursor.getColumnIndex("receiverIP")),
                    cursor.getInt(cursor.getColumnIndex("isgroup"))));
        
        	messages.add(message);
        	MyLog.d("lss","---messages---"+messages);

        }
        cursor.close();
        Collections.reverse(messages);//更正最后一条数据存在最后的BUG
        return messages;
    }

    //*************************** Chatting Info DB Method end****************************************/
    
    /*                The methods of operate GroupDB            */
	public void addGroupInfo(Group group) {
		ContentValues values = new ContentValues();
		values.put("name", group.getStrName());
		values.put("groupIP", group.getStrIP());
		values.put("masterID", group.getMasterID());
		int id = getGroupIDByIP(group.getStrIP());
		if (id != 0) {
			groupDatabase.update(groupSQLHelper.getGroupName(), values, "id=?",
					new String[] { String.valueOf(id) });
		} else {
			groupDatabase.insert(groupSQLHelper.getGroupName(), "id", values);
		}
	}

	public void delGroupInfo(String groupIP) {
		groupDatabase.delete(groupSQLHelper.getGroupName(), "groupIP=?",
				new String[] { groupIP });
	}

	public int getGroupIDByIP(String groupIP) {
		Cursor cursor = groupDatabase.query(groupSQLHelper.getGroupName(),
				new String[] { "id" }, "groupIP=?", new String[] { groupIP },
				null, null, null);
		if (cursor.moveToNext()) {
			int id = cursor.getInt(cursor.getColumnIndex("id"));
			cursor.close();
			return id;
		}
		cursor.close();
		return 0;
	}

	public List<Group> getAllGroupInfo() {
		List<Group> groups = new ArrayList<Group>();
		Cursor cursor = groupDatabase.query(groupSQLHelper.getGroupName(),
				new String[] { "name", "groupIP", "masterID" }, null, null,
				null, null, null);
		while (cursor.moveToNext()) {
			groups.add(new Group(cursor.getString(cursor
					.getColumnIndex("groupIP")), cursor.getString(cursor
					.getColumnIndex("name")), cursor.getString(cursor
					.getColumnIndex("masterID"))));

		}
		cursor.close();
		return groups;
	}
    	
    public void addMemberInfo(Users member){
    	ContentValues values = new ContentValues();
        values.put("name", member.getNickname());
        values.put("IMEI", member.getIMEI());
        values.put("ip", member.getIpaddress());
        values.put("groupIP", member.getGroupIP());
        int id = getMemberIDByIP(member.getIMEI(), member.getGroupIP());
        if (id != 0) {
        	groupDatabase.update(groupSQLHelper.getMemberName(), values, "id=?",
					new String[] { String.valueOf(id) });
		}else {
			groupDatabase.insert(groupSQLHelper.getMemberName(), "id", values);
		}
    }
    /**
     * 从数据库中获取组员
     * @param IMEI
     * @param groupIp
     */
    public Users getMemberInfo(String IMEI, String groupIp){
    	Users member = new Users();
    	Cursor cursor = groupDatabase.query(groupSQLHelper.getMemberName(),
    			new String[] {"name","IMEI","ip","groupIP"}, "IMEI=? and groupIP=?", 
    			new String[]{IMEI,groupIp}, null, null, null);
    	while (cursor.moveToNext()) {
			member.setNickname(cursor.getString(cursor.getColumnIndex("name")));
			member.setIMEI(cursor.getString(cursor.getColumnIndex("IMEI")));
			member.setIpaddress(cursor.getString(cursor.getColumnIndex("ip")));
			member.setGroupIP(cursor.getString(cursor.getColumnIndex("groupIP")));
		}
    	cursor.close();
    	return member;
    }
    
    public int getMemberIDByIP(String IMEI, String groupIp){
    	Cursor cursor = groupDatabase.query(groupSQLHelper.getMemberName(),
				new String[] { "id" }, "IMEI=? and groupIP=?", new String[] { IMEI,groupIp },
				null, null, null);
		if (cursor.moveToNext()) {
			int id = cursor.getInt(cursor.getColumnIndex("id"));
			cursor.close();
			return id;
		}
		cursor.close();
		return 0;
    }
    
    public void delMemberInfo(Users member){
    	groupDatabase.delete(groupSQLHelper.getMemberName(), "IMEI=? and groupIP=?", 
    			new String[]{member.getIMEI(),member.getGroupIP()});
    }
    //delete one member
    public void delMemberInfo(String IMEI, String groupIp){
    	groupDatabase.delete(groupSQLHelper.getMemberName(), "IMEI=? and groupIP=?", 
    			new String[]{IMEI,groupIp});
    }
    //delete members in one group
    public void delGroupMemInfo(String groupIp){
    	groupDatabase.delete(groupSQLHelper.getMemberName(), "groupIP=?", 
    			new String[]{groupIp});
    }
    
    
    public List<Users> getMembersByGroupIP(String groupIP){
    	List<Users> members = new ArrayList<Users>();
    	Cursor cursor = groupDatabase.query(groupSQLHelper.getMemberName(),
				new String[] {"name","IMEI","ip","groupIP"}, "groupIP=?", new String[]{groupIP},
				null, null, null);
    	while (cursor.moveToNext()) {
			members.add(new Users(cursor.getString(cursor.getColumnIndex("IMEI")), 
					cursor.getString(cursor.getColumnIndex("name")), 
					cursor.getString(cursor.getColumnIndex("ip")),
					cursor.getString(cursor.getColumnIndex("groupIP"))));
		}
    	cursor.close();
    	return members;
    }
    
    public void delGroups(){
    	groupDatabase.delete(groupSQLHelper.getGroupName(), null, null);
    	groupDatabase.delete(groupSQLHelper.getMemberName(), null, null);
    }
    
    public long addGroupChattingInfo(String senderID,String senderName,String recieverIP,String time,
    		String content,CONTENT_TYPE type,int recordTime) {

    	
        ContentValues values = new ContentValues();
        values.put("sendID", senderID);
        values.put("senderName", senderName);
        values.put("receiveIP", recieverIP);
        values.put("chatting", content);
        values.put("date", time);
        values.put("style", getStyteByContentType(type));
        values.put("recordTime", recordTime);
        return chatInfoDataBase.insert(chatInfoSQLHelper.getGroupTableName(), "id", values);
    }
    
    public void deleteGroupChattingInfo(String groupIp){
    	chatInfoDataBase.delete(chatInfoSQLHelper.getGroupTableName(), "receiveIP=?", new String[]{groupIp});
    }
    
    public void deteleAllGroupChattingInfo()
    {
    	chatInfoDataBase.delete(chatInfoSQLHelper.getGroupTableName(), null, null);
    }
    
    public List<Message> getScrollMsgOfGroupChattingInfo(int start, int count,String receiveIP) {
        List<Message> messages = new ArrayList<Message>();
        Cursor cursor = chatInfoDataBase.query(chatInfoSQLHelper.getGroupTableName(), new String[] { "id", "sendID",
                "receiveIP", "chatting", "date" ,"style","recordTime","percent","senderName"},  "receiveIP=?",
                new String[] { receiveIP }, null, null, "id desc", start + ","
                + count);
        while (cursor.moveToNext()) {
        	Message message= new Message(cursor.getString(cursor.getColumnIndex("sendID")),
        			cursor.getString(cursor.getColumnIndex("date")),
        			cursor.getString(cursor.getColumnIndex("chatting")),
        			null);
        	message.setSenderName(cursor.getString(cursor.getColumnIndex("senderName")));
        	switch (cursor.getInt(cursor.getColumnIndex("style")))//设置聊天信息类型
        	{
        		case 0:
        			message.setContentType(CONTENT_TYPE.TEXT);
        			break;
        		case 1:
        			message.setContentType(CONTENT_TYPE.IMAGE);
        			break;
        		case 2:
        			message.setContentType(CONTENT_TYPE.FILE);
        			break;
        		case 3:
        			message.setContentType(CONTENT_TYPE.VOICE);
        			break;
        	}
        	message.setRecordTime(cursor.getInt(cursor.getColumnIndex("recordTime")));
            messages.add(message);
        }
        cursor.close();
        Collections.reverse(messages);//更正最后一条数据存在最后的BUG
        return messages;
    }
    //add by lss for processing the solution of group equals null
    public Group getGroupById(int groupId){
    	Cursor cursor = groupDatabase.query(groupSQLHelper.getGroupName(),
				new String[] { "id","name","groupIP","masterID"}, "id=?", new String[] {String.valueOf(groupId)},
				null, null, null);
    	Group group = null;
    	if(cursor != null && cursor.moveToFirst())
    	{
    	group =new Group(cursor.getString(cursor.getColumnIndex("groupIP")),
    			cursor.getString(cursor.getColumnIndex("name")),
    			cursor.getString(cursor.getColumnIndex("masterID")));
    	MyLog.d("lss","1111group="+group);
    	}
    	
		cursor.close();
		return group;	
    }
    //add end
    
    //add by lss for call status
	/**
	 * 
	 * @param senderIMEI
	 * @param callstatus
	 * @param date
	 * @param calltime
	 * @param receiverIP
	 * 
	 */
	public void addCallInfo(String receiverIMEI,int callstatus,String date,int calltime,String receiverIP) {
	
		 ContentValues values = new ContentValues();
		    values.put("receiverIP", receiverIP);
		    values.put("receiverIMEI",receiverIMEI );
		    values.put("date", date);
		    values.put("calltime", calltime);
		    values.put("callstatus",callstatus);
		    Log.d("lss","recieverIMEI="+ receiverIMEI+":::date="+ date+":::receiverIP="+ receiverIP+
	               ":::calltime="+  calltime+ ":::callstatus="+callstatus);
		   callDatabase.insert(callSQLHelper.getTableName(), "id", values);
	}
	//add receiverName by lss for processing call die
	//add callType by lss for declear callist type (video or audio)
	public void addCallInfo(String receiverIMEI,int callstatus,String date,int calltime,String receiverIP,String callType) {
		
		 	ContentValues values = new ContentValues();
		 	String receiverName=getUserInfoByIMEI(receiverIMEI).getName();
		 	MyLog.d("lss","addCallInfo receiverName="+receiverName);
		    values.put("receiverIP", receiverIP);
		    values.put("receiverIMEI",receiverIMEI );
		    values.put("date", date);
		    values.put("calltime", calltime);
		    values.put("callstatus",callstatus);
		    values.put("callType",callType);
		    values.put("receiverName", receiverName);//add by lss for modifying the activity of calllog show senderName
		    Log.d("lss","addCallInfo recieverIMEI="+ receiverIMEI+":::date="+ date+":::receiverIP="+ receiverIP+
	               ":::calltime="+  calltime+ ":::callstatus="+callType+":::callstatus="+callType+
	               ":::receiverName="+  receiverName);
		   callDatabase.insert(callSQLHelper.getTableName(), "id", values);
	}
	//add end
	 

	
	/* * 删除所有通话记录*/
	 
	public void deteleAllCallInfo()
	{
		callDatabase.delete(callSQLHelper.getTableName(), null, null);
	}
	
	

	/* * 参数：通过接收者删除通话记录*/
	 
	public void deteleCallInfo(CallInfo callinfo) {
	   
	        callDatabase.delete(callSQLHelper.getTableName(), "id=?"
	        		, new String[] {String.valueOf(callinfo.getId())});
	    
	}
	
	
	public List<Call> getScrollCallOfCallInfo(int start, int count) {
		List<Call> calls = new ArrayList<Call>();
		Cursor cursor = callDatabase.query(callSQLHelper.getTableName(), new String[] {  "id",
           "receiverIMEI", "date", "calltime" ,"callstatus","receiverIP","callType","receiverName"},
            null, null, null, null, "id", start + ","
	            + count);
		
	    while (cursor.moveToNext()) {
	    	Call call=CallInfoToCall(new CallInfo(cursor.getString(cursor.getColumnIndex("callType")),
	    			cursor.getString(cursor.getColumnIndex("receiverIMEI")),
	    			cursor.getString(cursor.getColumnIndex("date")),
	                cursor.getInt(cursor.getColumnIndex("calltime")), 
	                cursor.getInt(cursor.getColumnIndex("callstatus"))
	                ,cursor.getString(cursor.getColumnIndex("receiverIP")),
	                cursor.getInt(cursor.getColumnIndex("id")),
	                cursor.getString(cursor.getColumnIndex("receiverName"))));
	    	Log.d("lss","id="+cursor.getInt(cursor.getColumnIndex("id"))+"receiverIMEI="+ cursor.getInt(cursor
	                .getColumnIndex("receiverIMEI"))+"date="+ cursor.getString(cursor.getColumnIndex("date"))+
	               "calltime="+  cursor.getInt(cursor.getColumnIndex("calltime"))+ 
	                "callstatus="+cursor.getInt(cursor.getColumnIndex("callstatus"))+
	                "receiverIP="+cursor.getString(cursor.getColumnIndex("receiverIP"))+
	                "callType="+cursor.getString(cursor.getColumnIndex("callType"))+
	                "receiverName="+cursor.getString(cursor.getColumnIndex("receiverName")));
	       calls.add(call);
	       Log.d("lss","call"+call);
	    }
	    cursor.close();
	    Collections.reverse(calls);//更正最后一条数据存在最后的BUG
	    return calls;
	}
	public String getReceiverIpByReceiverImei(String receiverImei){
		
		Cursor cursor = userDataBase.query(userSQLHelper.getTableName(), new String[] { "ip" }, "IMEI=?",
                new String[] { receiverImei }, null, null, null);
        if (cursor.moveToNext()) {
            String receiveIP= cursor.getString(cursor.getColumnIndex("ip"));
            cursor.close();
            return receiveIP;
        }
        cursor.close();
        return null;
	}
	private Call CallInfoToCall(CallInfo callInfo)
	{
		Call call=new Call();
		call.setSendTime(callInfo.getDate());//设置通话发出时间
		call.setCallTime(callInfo.getCallTime());
		call.setCallStatus(callInfo.getCallStatus());
		call.setReceiveIMEI(callInfo.getReceiverIMEI());
		call.setReceiveIP(callInfo.getReceiverIP());
		call.setCallType(callInfo.getCallType());
		call.setReceiverName(callInfo.getReceiverName());//add by lss for modifying the activity of calllog show senderName
		MyLog.d("lss","call.getReceiverName="+call.getReceiverName());
		return call;
	}
	
	/* * 作用: 用来获取表中通话记录总数量*/
	 
	public long getCountOfCallInfo() {
	    Cursor cursor = callDatabase.query(callSQLHelper.getTableName(), new String[] { "count(*)" }, null, null,
	            null, null, null);
	    if (cursor.moveToNext()) {
	        long count = cursor.getLong(0);
	        cursor.close();
	        return count;
	    }
	    return 0;
	}
	
	  /**
     * 参数：通话记录序号ID 作用:用来查找对应的一条通话记录 返回callinfo类
     */
    public CallInfo getCallInfoByID(long id) {
        
    	Cursor cursor = callDatabase.query(callSQLHelper.getTableName(), new String[] {  "id",
            "receiverIMEI", "date", "calltime" ,"callstatus","receiverIP"}, null,
	            null, null, null, "id desc", null);
        if (cursor.moveToNext()) {
            CallInfo callinfo = new CallInfo(cursor.getInt(cursor.getColumnIndex("id")), cursor.getString(cursor
	                .getColumnIndex("receiverIMEI")), cursor.getString(cursor.getColumnIndex("date")),
	                cursor.getInt(cursor.getColumnIndex("calltime")), 
	                cursor.getInt(cursor.getColumnIndex("callstatus"))
	                ,cursor.getString(cursor.getColumnIndex("receiverIP")));
            cursor.close();
            return callinfo;
        }
        return null;
    }
	
}
