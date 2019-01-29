package com.leadcore.sms.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * 
 * @author lss
 * @since  2016-12-06
 */
public class CallDBHelper extends SQLiteOpenHelper {

	private static final int VERSION = 1;
	private static final String DBNAME = "call.db";
	private static final String T_CALLINFO = "t_callInfo";
	
	public CallDBHelper(Context context){
		super(context, DBNAME, null, VERSION);
	}
	/**
	 * create t_callInfo table
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		//add callType by lss for declear callist type (video or audio)
		db.execSQL("create table " + T_CALLINFO + " (id integer primary key," +
				"calltime integer,date text,callstatus integer,senderIMEI varchar(20),"+
				"senderIP varchar(20),receiverIP varcher(20), receiverIMEI varchar(20),callType varchar(20), receiverName varchar(20))");
		//add end
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	} 
	
	public String getTableName(){
		return T_CALLINFO;
	}
}
