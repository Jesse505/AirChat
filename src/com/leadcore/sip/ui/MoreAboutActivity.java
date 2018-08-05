package com.leadcore.sip.ui;

import org.sipdroid.sipua.R;
import org.sipdroid.sipua.utils.MyLog;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MoreAboutActivity extends Activity {

	
	private Button back;
	private TextView txtMoreAbout;
	private RelativeLayout moreAbout;
	private TextView txtVersionNumber;//add by lss for adding version number
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	//add by lss for adding version number
	/** 
	 * ��ȡ����汾�� 
	 */  
	private double getAPPVersion() {  
	    PackageManager pm = this.getPackageManager();//�õ�PackageManager����  	  
	    try {  
	        PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);//�õ�PackageInfo���󣬷�װ��һЩ���������Ϣ������  
	        double appVersion = pi.versionCode;//��ȡ�嵥�ļ���versionCode�ڵ��ֵ  
	        MyLog.d("lss", "appVersion="+appVersion);  
	       return appVersion;
	    } catch (NameNotFoundException e) {  
	        e.printStackTrace();  
	        MyLog.e("lss", "getAppVersion:"+e.getCause());  
	    }
	    return 0;
	}  
	//add end
	private void init(){
		setContentView(R.layout.activity_more_about);
		moreAbout=(RelativeLayout)findViewById(R.id.more_about);
		txtMoreAbout=(TextView)moreAbout.findViewById(R.id.txt_more);
		back=(Button)moreAbout.findViewById(R.id.btn_back);
		//add by lss for adding version number
		txtVersionNumber=(TextView)findViewById(R.id.txt_version_number);
		MyLog.d("lss","String.valueOf(getAPPVersion())="+String.valueOf(getAPPVersion()));		
		txtVersionNumber.setText(String.valueOf(getAPPVersion()));
		//add end
		txtMoreAbout.setText(R.string.tab_more_about);		
		back.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();	
			}			
		});		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}
}
