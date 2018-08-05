package com.leadcore.sms.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChattingDBHelper extends SQLiteOpenHelper
{

	private static final int VERSION = 1;               //数据库版本
	private static final String DBNAME="chatting.db";   //数据库名
	private static final String T_NAME="chatting";      //数据表名
	private static final String T_GROUP = "group_chatting";
	private static final String T_MESSAGELIST="message_chatting";//短信列表List
	
	/*获取数据库中操作表名字*/
	public String getTableName()
	{
		return T_NAME;
	}
	
	public String getGroupTableName(){
		return T_GROUP;
	}
	
	
	
	public String getMessageTableName(){
		return T_MESSAGELIST;
	}
	
	/*构造函数
	 * 参数为版本，数据库名
	 */
	public ChattingDBHelper(Context context)
	{
		super(context, DBNAME, null, VERSION);
	}

	
	/* T_NAME为创建数据表的名字
	 * 该函数初始化类的时候会调用，T_NAME为表名,如果表不存在则创建，如果存在则打开
	 * id(int) 
	 * sendID(int)
	 * receiverID(int)
	 * chatting(text)
	 * date(text)
	 * style(integer) TEXT, IMAGE, FILE, VOICE;
	 * recordTime(integer) 
	 */
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("create table " + T_NAME + " (id integer primary key,sendID integer,receiverID integer," +
				"chatting text,date text,style integer,recordTime integer,percent integer)");
		db.execSQL("create table " + T_GROUP + "(id integer primary key,sendID varchar(20),receiveIP varchar(20)," +
				"chatting text,date text,style integer,recordTime integer,percent integer,senderName text)");
		//add receiverIP by lss for adding group message in message list
		db.execSQL("create table " + T_MESSAGELIST + " (id integer primary key,sendID integer,receiverID integer," +
				"chatting text,date text,readstatus integer,style integer,receiverIP varchar(20),isgroup integer)");//add by lss isgroup
		//add end
	}


	/*该函数当检测到版本数增加时候会自动调用
	 * 一般用来数据库表格的改动或者扩展
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		
	}

}
