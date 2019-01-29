package com.leadcore.sip.ui;

import org.sipdroid.sipua.BaseApplication;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.utils.MyLog;
import com.leadcore.sip.login.AdhocManager;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;

public class TabHomeActivity extends TabActivity implements
		RadioGroup.OnCheckedChangeListener {

	private TabHost mHost = null;
	private RadioGroup mainTab = null;
	protected static boolean isTabActive;
	private BroadcastReceiver broadcastReceiver;
	public static final String ACTION_MESSAGELIST_REFRESH = "com.example.message_list.screen.refresh";
	RadioButton rbMessage;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initUI();
		
		
		
		MyLog.e("zhaoyifei", "TabHomeActivity onCreate");
//		startAdhoc();
//		String name = getIntent().getStringExtra(Sipdroid.NAME);
//		String number = getIntent().getStringExtra(Sipdroid.NUMBER);
//		Receiver.engine(this).login(name, number, this);
	}

	
	private void initUI(){
		setContentView(R.layout.activity_tab_home);
		mainTab = (RadioGroup) findViewById(R.id.main_tab);
		mainTab.setOnCheckedChangeListener(this);
		mHost = this.getTabHost();
//		mHost = (TabHost) findViewById(android.R.id.tabhost);
//	    LocalActivityManager lam = new LocalActivityManager(this, false);
//	    lam.dispatchCreate(savedInstanceState);
//	    mHost.setup(lam);
		TabHost.TabSpec localTabSpec1 = mHost.newTabSpec("TS_CALL");
		localTabSpec1.setIndicator("呼叫",
				getResources().getDrawable(R.drawable.n_maintab_call));
		localTabSpec1.setContent(new Intent(this, TabCallActivity.class));
		TabHost.TabSpec localTabSpec2 = mHost.newTabSpec("TS_CONTACTS");
		localTabSpec2.setIndicator("通讯录",
				getResources().getDrawable(R.drawable.s_maintab_contact));
		localTabSpec2.setContent(new Intent(this, TabContactActivity.class));
		TabHost.TabSpec localTabSpec3 = mHost.newTabSpec("TS_MESSAGE");
		localTabSpec3.setIndicator("短信",
				getResources().getDrawable(R.drawable.n_maintab_message));
		localTabSpec3.setContent(new Intent(this, TabMessageActivity.class));
		TabHost.TabSpec localTabSpec4 = mHost.newTabSpec("TS_MORE");
		localTabSpec4.setIndicator("更多",
				getResources().getDrawable(R.drawable.n_maintab_more));
		localTabSpec4.setContent(new Intent(this, TabMoreActivity.class));
		mHost.addTab(localTabSpec1);
		mHost.addTab(localTabSpec2);
		mHost.addTab(localTabSpec3);
		mHost.addTab(localTabSpec4);
		mHost.setCurrentTabByTag("TS_CONTACTS");
		updateBottomUI(1);
		rbMessage=(RadioButton)findViewById(R.id.radio_button2);//add by lss
		//add by lss for alert new message
		rbMessage.setBackground(getResources().getDrawable(R.drawable.n_maintab_message));
		rbMessage.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) { 
				// TODO Auto-generated method stub
				if(isChecked==true){
					rbMessage.setBackground(getResources().getDrawable(R.drawable.s_maintab_message));
				}
				else{
					rbMessage.setBackground(getResources().getDrawable(R.drawable.n_maintab_message));
				}
				
			}
			
		});
		rbMessage.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				rbMessage.setBackground(getResources().getDrawable(R.drawable.s_maintab_message));
			}
			
		});
		
		broadcastReceiver=new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action=intent.getAction();
				if(action.equals(ACTION_MESSAGELIST_REFRESH)){
					if(rbMessage.isChecked()==true){
						rbMessage.setBackground(getResources().getDrawable(R.drawable.s_maintab_message));
					}else{
						rbMessage.setBackground(getResources().getDrawable(R.drawable.maintab_message_highlight));
					}
					
					
					MyLog.d("lss","highlight!!");
				}
			}			
		};
		 IntentFilter localIntentFilter = new IntentFilter();
	     localIntentFilter.addAction(ACTION_MESSAGELIST_REFRESH);
	     registerReceiver(this.broadcastReceiver, localIntentFilter);
	     
	     //add end
		
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isTabActive = true;
		MyLog.e("zhaoyifei", "TabHomeActivity onResume");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		isTabActive = false;
		MyLog.e("zhaoyifei", "TabHomeActivity onPause");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);//add by lss
		MyLog.e("zhaoyifei", "TabHomeActivity onDestroy");
//		stopAdhoc();
//		Receiver.engine(this).logout(this);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		switch (checkedId) {
		case R.id.radio_button0:
			mHost.setCurrentTabByTag("TS_CALL");
			break;
		case R.id.radio_button1:
			mHost.setCurrentTabByTag("TS_CONTACTS");
			break;
		case R.id.radio_button2:
			mHost.setCurrentTabByTag("TS_MESSAGE");
			break;
		case R.id.radio_button3:
			mHost.setCurrentTabByTag("TS_MORE");
			break;
		default:
			break;
		}
	}
	
	private void startAdhoc(){
		AdhocManager.getInstance(this).StartAdhoc();
//        AdhocManager.getInstance(this).setShowText(showText);
        AdhocManager.getInstance(this).showContacts ();
	}
	private void stopAdhoc(){
		AdhocManager.getInstance(this).StopAdhoc();
	}
	
	private void updateBottomUI(int itemID){
		switch (itemID) {
		case 0:
			((RadioButton)findViewById(R.id.radio_button0)).setChecked(true);
			break;
		case 1:
			((RadioButton)findViewById(R.id.radio_button1)).setChecked(true);
			break;
		case 2:
			((RadioButton)findViewById(R.id.radio_button2)).setChecked(true);
			break;
		case 3:
			((RadioButton)findViewById(R.id.radio_button3)).setChecked(true);
			break;
		default:
			((RadioButton)findViewById(R.id.radio_button0)).setChecked(true);
			break;
		}
	}

	public static boolean getIsTabActive() {
		return isTabActive;
	}
	
	public static void sendEmptyMessage() {
		if (isTabActive)
			handler.sendEmptyMessage(0);
	}

	private static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int unReadPeopleSize = BaseApplication.getInstance()
					.getUnReadPeopleSize();
			switch (unReadPeopleSize) { // 判断人数作不同处理
			case 0: // 为0，隐藏数字提示
//				mHtvSessionNumber.setVisibility(View.GONE);
				break;

			default: // 不为0，则显示未读数
//				mHtvSessionNumber.setText(String.valueOf(unReadPeopleSize));
//				mHtvSessionNumber.setVisibility(View.VISIBLE);
				break;
			}
		}
	};
}
