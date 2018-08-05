package com.leadcore.sip.ui;

import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.SessionUtils;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MorePersonalInfoActivity extends BaseActivity {

	
	private Button back;
	private TextView txtMorePersonalInfo;
	private RelativeLayout morePersonalInfo;
	private TextView txtLocalName;
	private TextView txtLocalNumber;
	private TextView txtLocalIP;
	String localName;
	String localNumber;
	String localIP;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	private void init(){
		setContentView(R.layout.activity_more_personal_info);
		morePersonalInfo=(RelativeLayout)findViewById(R.id.more_personal_info);
		txtMorePersonalInfo=(TextView)morePersonalInfo.findViewById(R.id.txt_more);
		back=(Button)morePersonalInfo.findViewById(R.id.btn_back);
		txtMorePersonalInfo.setText(R.string.personal_info);
		back.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();				
			}
			
		});
		txtLocalName=(TextView)findViewById(R.id.txt_user_name);
		txtLocalNumber=(TextView)findViewById(R.id.txt_user_number);
		txtLocalIP=(TextView)findViewById(R.id.txt_user_ip);
		localName =getResources().getString(R.string.txt_user_name)
				+ SessionUtils.getNickname().toString();
		localNumber = getResources().getString(R.string.txt_user_number)
				+ SessionUtils.getIMEI().toString();
		localIP = getResources().getString(R.string.txt_user_ip) 
				+ SessionUtils.getLocalIPaddress().toString();
		MyLog.d("lss","localName="+localName+"::localNumber="+localNumber+"::localIP="+localIP);
		
		txtLocalName.setText(localName);
		txtLocalNumber.setText(localNumber);
		txtLocalIP.setText(localIP);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
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
}
