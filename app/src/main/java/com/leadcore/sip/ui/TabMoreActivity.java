package com.leadcore.sip.ui;

import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class TabMoreActivity extends TabItemActivity implements OnClickListener{
	private TextView txtTitle;
	private RelativeLayout tabMoreTitle;
	
	private LinearLayout mLinearPersonalInfo;
	private LinearLayout mLinearAbout;
	private LinearLayout mLinearSetting;
	private LinearLayout mLinearExit;
	 //private 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		init();// add by lss for UI
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MyLog.i("tab", "TabMoreActivity onResume");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	//add by lss for UI begin
	public void init(){
			setContentView(R.layout.activity_tab_more);
			//add by lss for UI title begin
			tabMoreTitle=(RelativeLayout)findViewById(R.id.tab_more_title);
			txtTitle=(TextView)tabMoreTitle.findViewById(R.id.txt_title);
			txtTitle.setText(R.string.more_list);
			
			mLinearPersonalInfo=(LinearLayout)findViewById(R.id.linear_personal_info);
			mLinearAbout=(LinearLayout)findViewById(R.id.linear_about);
			mLinearSetting=(LinearLayout)findViewById(R.id.linear_setting);
			mLinearExit=(LinearLayout)findViewById(R.id.linear_exit);
			
			mLinearPersonalInfo.setOnClickListener(this);
			mLinearAbout.setOnClickListener(this);
			mLinearSetting.setOnClickListener(this);
			mLinearExit.setOnClickListener(this);	
			
		}
	@Override
	public void onClick(View v) {
		Intent intent=new Intent();
		switch (v.getId()) {
		
		case R.id.linear_personal_info:
			
			intent.setClass(TabMoreActivity.this, MorePersonalInfoActivity.class);
			startActivity(intent);
			break;
		case R.id.linear_about:
			
			intent.setClass(TabMoreActivity.this, MoreAboutActivity.class);
			startActivity(intent);
			break;
		case R.id.linear_setting:
			
			intent.setClass(TabMoreActivity.this,  MoreSettingActivity .class);
			startActivity(intent);
			break;
		case R.id.linear_exit:			
			exit();
			break;
		default:
			break;
		}
		
	}
	
	
		
}
