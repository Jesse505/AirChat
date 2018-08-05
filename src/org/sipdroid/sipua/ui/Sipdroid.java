/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * Copyright (C) 2008 Hughes Systique Corporation, USA (http://www.hsc.com)
 * 
 * This file is part of Sipdroid (http://www.sipdroid.org)
 * 
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.sipdroid.sipua.ui;

import java.util.ArrayList;
import java.util.List;

import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.SipdroidEngine;
import org.sipdroid.sipua.UserAgent;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.NetworkUtil;
import org.sipdroid.sipua.utils.SessionUtils;
import org.zoolu.net.IpAddress;
import org.zoolu.tools.Random;




import com.dt.adhoc.service.IGroupInfoCfgRspListener;
import com.leadcore.sip.login.AdhocManager;
import com.leadcore.sip.login.GlobalVar;
import com.leadcore.sip.ui.TabHomeActivity;
import com.leadcore.sip.ui.ContactsChooseActivity.MyGroupInfoListener;
import com.leadcore.sms.entity.Group;
import com.leadcore.sms.entity.Users;
import com.leadcore.sms.sql.SqlDBOperate;
import com.leadcore.sms.sql.UserInfo;


import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.provider.Contacts.People;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/////////////////////////////////////////////////////////////////////
// this the main activity of Sipdroid
// for modifying it additional terms according to section 7, GPL apply
// see ADDITIONAL_TERMS.txt
/////////////////////////////////////////////////////////////////////
public class Sipdroid extends BaseActivity implements OnDismissListener {

	public static final boolean release = false; //modify by zyf
	public static final boolean market = false;

	/* Following the menu item constants which will be used for menu creation */
	public static final int FIRST_MENU_ID = Menu.FIRST;
	public static final int CONFIGURE_MENU_ITEM = FIRST_MENU_ID + 1;
	public static final int ABOUT_MENU_ITEM = FIRST_MENU_ID + 2;
	public static final int EXIT_MENU_ITEM = FIRST_MENU_ID + 3;

	private static AlertDialog m_AlertDlg;
	AutoCompleteTextView sip_uri_box,sip_uri_box2;
	Button createButton,btn_send;
	EditText editText;
	
	private EditText et_name;
	private EditText et_number;
	private Button btn_login;
	
	public static final String NAME = "name";
	public static final String NUMBER = "number";
	public static final String IP = "ip";
	
	private SqlDBOperate mSqlDBOperate;// 数据库操作实例
	private UserInfo mUserInfo; // 用户信息类实例
	private String name;
	private String number;
	private String localIP;
	
	private static Object newGroupLocks[];
	
	@Override
	public void onStart() {
		super.onStart();
//		Receiver.engine(this).registerMore();
//	    ContentResolver content = getContentResolver();
//	    Cursor cursor = content.query(Calls.CONTENT_URI,
//	            PROJECTION, Calls.NUMBER+" like ?", new String[] { "%@%" }, Calls.DEFAULT_SORT_ORDER);
//	    CallsAdapter adapter = new CallsAdapter(this, cursor);
//	    sip_uri_box.setAdapter(adapter);
//	    sip_uri_box2.setAdapter(adapter);
//	    MyLog.e("zyf", "isOnwlan " + Receiver.isFast(0));
	    //setDefaultSettingVal();
	}
	
	
	private void save(String name , String number){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor editor = preferences.edit();
		editor.putString(NAME,name);
		editor.putString(NUMBER, number);
		editor.commit();
	}
	
	private void initUser(EditText name, EditText number){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		String namesString = preferences.getString(NAME, "");
		String numberString = preferences.getString(NUMBER, "");
		name.setText(namesString);
		number.setText(numberString);
	}
	
	public static class CallsCursor extends CursorWrapper {
		List<String> list;
		
		public int getCount() {
			return list.size();
		}
		
		public String getString(int i) {
			return list.get(getPosition());
		}
		
		public CallsCursor(Cursor cursor) {
			super(cursor);
			list = new ArrayList<String>();
			for (int i = 0; i < cursor.getCount(); i++) {
				moveToPosition(i);
 		        String phoneNumber = super.getString(1);
		        String cachedName = super.getString(2);
		        if (cachedName != null && cachedName.trim().length() > 0)
		        	phoneNumber += " <" + cachedName + ">";
		        if (list.contains(phoneNumber)) continue;
				list.add(phoneNumber);
			}
			moveToFirst();
		}
		
	}
	
//	public static class CallsAdapter extends CursorAdapter implements Filterable {
//	    public CallsAdapter(Context context, Cursor c) {
//	        super(context, c);
//	        mContent = context.getContentResolver();
//	    }
//	
//	    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//	        final LayoutInflater inflater = LayoutInflater.from(context);
//	        final TextView view = (TextView) inflater.inflate(
//	                android.R.layout.simple_dropdown_item_1line, parent, false);
//	    	String phoneNumber = cursor.getString(1); 
//	        view.setText(phoneNumber);
//	        return view;
//	    }
//	
//	    @Override
//	    public void bindView(View view, Context context, Cursor cursor) {
//	    	String phoneNumber = cursor.getString(1);
//	        ((TextView) view).setText(phoneNumber);
//	    }
//	
//	    @Override
//	    public String convertToString(Cursor cursor) {
//	    	String phoneNumber = cursor.getString(1);
//	    	if (phoneNumber.contains(" <"))
//	    		phoneNumber = phoneNumber.substring(0,phoneNumber.indexOf(" <"));
//	        return phoneNumber;
//	    }
//	
//	    @Override
//	    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
//	        if (getFilterQueryProvider() != null) {
//	            return new CallsCursor(getFilterQueryProvider().runQuery(constraint));
//	        }
//	
//	        StringBuilder buffer;
//	        String[] args;
//	        buffer = new StringBuilder();
//	        buffer.append(Calls.NUMBER);
//	        buffer.append(" LIKE ? OR ");
//	        buffer.append(Calls.CACHED_NAME);
//	        buffer.append(" LIKE ?");
//	        String arg = "%" + (constraint != null && constraint.length() > 0?
//       				constraint.toString() : "@") + "%";
//	        args = new String[] { arg, arg};
//	
//	        return new CallsCursor(mContent.query(Calls.CONTENT_URI, PROJECTION,
//	                buffer.toString(), args,
//	                Calls.NUMBER + " asc"));
//	    }
//	
//	    private ContentResolver mContent;        
//	}
	
	private static final String[] PROJECTION = new String[] {
        Calls._ID,
        Calls.NUMBER,
        Calls.CACHED_NAME
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		MyLog.e("zhaoyifei", "sipdroid onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sipdroid);
		sip_uri_box = (AutoCompleteTextView) findViewById(R.id.txt_callee);
		sip_uri_box2 = (AutoCompleteTextView) findViewById(R.id.txt_callee2);
		sip_uri_box.setOnKeyListener(new OnKeyListener() {
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        if (event.getAction() == KeyEvent.ACTION_DOWN &&
		        		keyCode == KeyEvent.KEYCODE_ENTER) {
		          call_menu(sip_uri_box);
		          return true;
		        }
		        return false;
		    }
		});
		sip_uri_box.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				call_menu(sip_uri_box);
			}
		});
		sip_uri_box2.setOnKeyListener(new OnKeyListener() {
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        if (event.getAction() == KeyEvent.ACTION_DOWN &&
		        		keyCode == KeyEvent.KEYCODE_ENTER) {
		          call_menu(sip_uri_box2);
		          return true;
		        }
		        return false;
		    }
		});
		sip_uri_box2.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				call_menu(sip_uri_box2);
			}
		});
		on(this,true);

		Button contactsButton = (Button) findViewById(R.id.contacts_button);
		contactsButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(Intent.ACTION_DIAL);
				startActivity(myIntent);
			}
		});

		final Context mContext = this;
		final OnDismissListener listener = this;
		
		createButton = (Button) findViewById(R.id.create_button);
		createButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				CreateAccount createDialog = new CreateAccount(mContext);
				createDialog.setOnDismissListener(listener);
		        createDialog.show();
			}
		});
		
		btn_send = (Button) findViewById(R.id.btn_send);
		editText = (EditText) findViewById(R.id.txt_input);
		
		et_name = (EditText) findViewById(R.id.et_name);
		et_number = (EditText) findViewById(R.id.et_number);
		initUser(et_name, et_number);
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_login.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				name = et_name.getText().toString();
		        number = et_number.getText().toString();
		        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(number)) {
		            Toast.makeText(Sipdroid.this, Sipdroid.this.getResources().getString(R.string.login_toast), Toast.LENGTH_LONG).show();
		            return;
		        }
		        doLogin();
//				overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
			} 
		});
	}

	public static boolean on(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Settings.PREF_ON, Settings.DEFAULT_ON);
	}

	public static void on(Context context,boolean on) {
		Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		edit.putBoolean(Settings.PREF_ON, on);
		edit.commit();
//        if (on) Receiver.engine(context).isRegistered();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (Receiver.call_state != UserAgent.UA_STATE_IDLE) Receiver.moveTop();
		String text;
		text = Integer.parseInt(Build.VERSION.SDK) >= 5?CreateAccount.isPossible(this):null;
		if (text != null && !text.contains("Google Voice") &&
				(Checkin.createButton == 0 || Random.nextInt(Checkin.createButton) != 0))
			text = null;
		if (text != null) {
			createButton.setVisibility(View.VISIBLE);
			createButton.setText(text);
		} else
			createButton.setVisibility(View.GONE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		MenuItem m = menu.add(0, ABOUT_MENU_ITEM, 0, R.string.menu_about);
		m.setIcon(android.R.drawable.ic_menu_info_details);
		m = menu.add(0, EXIT_MENU_ITEM, 0, R.string.menu_exit);
		m.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		m = menu.add(0, CONFIGURE_MENU_ITEM, 0, R.string.menu_settings);
		m.setIcon(android.R.drawable.ic_menu_preferences);
						
		return result;
	}

	void call_menu(AutoCompleteTextView view)
	{
		String target = view.getText().toString();
		if (m_AlertDlg != null) 
		{
			m_AlertDlg.cancel();
		}
		if (target.length() == 0)
			m_AlertDlg = new AlertDialog.Builder(this)
				.setMessage(R.string.empty)
				.setTitle(R.string.app_name)
				.setIcon(R.drawable.icon22)
				.setCancelable(true)
				.show();
		else if (!Receiver.engine(this).call(target,true,null))
			m_AlertDlg = new AlertDialog.Builder(this)
				.setMessage(R.string.notfast)
				.setTitle(R.string.app_name)
				.setIcon(R.drawable.icon22)
				.setCancelable(true)
				.show();
	}
	
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);
		Intent intent = null;

		switch (item.getItemId()) {
		case ABOUT_MENU_ITEM:
			if (m_AlertDlg != null) 
			{
				m_AlertDlg.cancel();
			}
			m_AlertDlg = new AlertDialog.Builder(this)
			.setMessage(getString(R.string.about).replace("\\n","\n").replace("${VERSION}", getVersion(this)))
			.setTitle(getString(R.string.menu_about))
			.setIcon(R.drawable.icon22)
			.setCancelable(true)
			.show();
			break;
			
		case EXIT_MENU_ITEM: 
			on(this,false);
			Receiver.pos(true);
			Receiver.engine(this).halt();
			Receiver.mSipdroidEngine = null;
			Receiver.reRegister(0);
//			stopService(new Intent(this,RegisterService.class));
			finish();
			break;
			
		case CONFIGURE_MENU_ITEM: {
			dialog_setting();
//			try {
//				intent = new Intent(this, org.sipdroid.sipua.ui.Settings.class);
//				startActivity(intent);
//			} catch (ActivityNotFoundException e) {
//			}
		}
			break;
		}

		return result;
	}
	
	public static String getVersion() {
		return getVersion(Receiver.mContext);
	}
	
	public static String getVersion(Context context) {
		final String unknown = "Unknown";
		
		if (context == null) {
			return unknown;
		}
		
		try {
	    	String ret = context.getPackageManager()
			   .getPackageInfo(context.getPackageName(), 0)
			   .versionName;
	    	if (ret.contains(" + "))
	    		ret = ret.substring(0,ret.indexOf(" + "))+"b";
	    	return ret;
		} catch(NameNotFoundException ex) {}
		
		return unknown;		
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		onResume();
	}
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		MyLog.e("zhaoyifei", "sipdroid onDestroy");
	}
    
    /**
     * IP地址正确性验证
     * 
     * @return boolean 返回是否为正确， 正确(true),不正确(false)
     */
    private boolean isValidated() {
    	localIP = NetworkUtil.getLocalWifiIP(this);
        String nullIP = "0.0.0.0";
        if (nullIP.equals(localIP) || localIP == null ) {
            Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initEvents() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}


    /** 执行登陆 **/
    private void doLogin() {
        if (!isValidated()) {
            return;
        }
        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showLoadingDialog(getString(R.string.dialog_login_saveInfo));
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                	Receiver.mSipdroidEngine = null;
                    mSqlDBOperate = new SqlDBOperate(mContext);
                    String IMEI = number;
                    String nickname = name;
                    // 若数据库中有IMEI对应的用户记录，则更新此记录; 无则创建新用户
                    if ((mUserInfo = mSqlDBOperate.getUserInfoByIMEI(IMEI)) != null) {
                        mUserInfo.setIPAddr(localIP);
                        mUserInfo.setName(nickname);
                        mSqlDBOperate.updateUserInfo(mUserInfo);
                    }
                    else {
                        mUserInfo = new UserInfo(nickname, IMEI, localIP);
                        mSqlDBOperate.addUserInfo(mUserInfo);
                    }

                    ArrayList<Group> groups = (ArrayList<Group>) mSqlDBOperate.getAllGroupInfo();
                    MyLog.i("group", "Sipdroid_doLogin mSqlDBOperate.getAllGroupInfo() size:" + groups.size());
                    for (Group group : groups) {
                    	MyLog.i("group", "Sipdroid_doLogin mApplication.addOnlineGroup groupIP:" + group.getStrIP());
						mApplication.addOnlineGroup(group);
					}
                    
                    int usserID = mSqlDBOperate.getIDByIMEI(IMEI); // 获取用户id
                    // 设置用户Session
                    SessionUtils.setLocalUserID(usserID);
                    SessionUtils.setLocalIPaddress(localIP);
                    SessionUtils.setIMEI(IMEI);
                    SessionUtils.setNickname(nickname);

                    // 在SD卡中存储登陆信息
                    save(nickname, IMEI);

                  //if the network type is adhoc, we need to config groupInfo to adhoc network module,otherwise don't need
                    if (NetworkUtil.network_type == NetworkUtil.ADHOC_NETWORK) {
						newGroup(groups);
					}
                    Receiver.engine(mContext).login(nickname, IMEI, mContext);
                    return true;
                }
                catch (Exception e) {
                	Log.e("zyf", "Exception",e);
                	
                    e.printStackTrace();
                }
                finally {
                	Log.e("zyf", "finally");
                    if (null != mSqlDBOperate) {
                        mSqlDBOperate.close();
                        mSqlDBOperate = null;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                dismissLoadingDialog();
                if (result) {
                    startActivity(TabHomeActivity.class);
                    finish();
                }
                else {
                    showShortToast(getString(R.string.network_error));
                }
            }
        });
    }

    /**
     * config group info to the adhocNetwork module
     * @param groups
     */
    private void newGroup(List<Group> groups){
    	List<Group> myGroups = new ArrayList<Group>();         //群主是本机的群组
    	for (int i = 0; i < groups.size(); i++) {
			if (groups.get(i).getMasterID().equals(number)) {
				myGroups.add(groups.get(i));
			}
		}
    	int groupSize = myGroups.size();
    	MyLog.i("group", "Sipdroid_doLogin myGroup.size: " + groupSize);
    	newGroupLocks = new Object[groupSize];
    	
    	for (int i = 0; i < groupSize; i++) {
    		newGroupLocks[i] = new Object();                  //modify by zyf for 登录失败，newGroupLocks[i]空指针
    		List<Users> members = myGroups.get(i).getMembers();
    		List<String> memberIPList = new ArrayList<String>();
    		for (Users member : members) {
				memberIPList.add(member.getIpaddress());
			}
    		Receiver.engine(mContext).requestGroupInfoCfg(myGroups.get(i).getStrIP(), memberIPList, new MyGroupInfoListener(i));
    		synchronized (newGroupLocks[i]) {
    			try {
    				newGroupLocks[i].wait(3000);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    			
    		}
		}
    	
    }
    
    
	//the callback of create group
	public class MyGroupInfoListener extends IGroupInfoCfgRspListener.Stub{

		int groupIndex;
		public MyGroupInfoListener(int groupIndex) {
			this.groupIndex = groupIndex;
		}
		@Override
		public void onGroupInfoCfgRsp(boolean isSuccessful, String groupIP)
				throws RemoteException {
			MyLog.i("group", "Login new group:" + groupIP + " isSuccessful:" + isSuccessful);
			synchronized (newGroupLocks[groupIndex]) {
				newGroupLocks[groupIndex].notifyAll();
			}
		}
		
	}
    
	private void dialog_setting(){
    	String itemid[] = {getString(R.string.text_setting_wifi),getString(R.string.text_setting_adhoc_network)};
		AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.menu_settings)
				.setSingleChoiceItems(itemid, NetworkUtil.network_type, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (which == 0) {
							NetworkUtil.network_type = NetworkUtil.WIFI;
						}else {
							NetworkUtil.network_type = NetworkUtil.ADHOC_NETWORK;
						}
					}
				}).setNegativeButton(getString(R.string.text_setting_cancel), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).show();
	}
    
	public void goSettings(View viw){
		try {
			Intent intent = new Intent(this, org.sipdroid.sipua.ui.Settings.class);
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
		}
	}
    
}
