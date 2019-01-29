package com.leadcore.sms.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * 
 * @author zhaoyifei
 * @since  2016-11-28
 */
public class GroupDBHelper extends SQLiteOpenHelper {

	private static final int VERSION = 1;
	private static final String DBNAME = "group.db";
	private static final String T_GROUPNAME = "t_group";
	private static final String T_MEMBERNAME = "t_member";
	
	public GroupDBHelper(Context context){
		super(context, DBNAME, null, VERSION);
	}
	/**
	 * create group table and members table
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table " + T_GROUPNAME + " (id integer primary key,name varchar(20)," +
				"groupIP varchar(20),masterID varchar(20))" );
		
		db.execSQL("create table " + T_MEMBERNAME + " (id integer primary key,name varchar(20)," +
				"IMEI varchar(20),ip varchar(20),groupIP varchar(20))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	public String getGroupName(){
		return T_GROUPNAME;
	}
	
	public String getMemberName(){
		return T_MEMBERNAME;
	}

}
