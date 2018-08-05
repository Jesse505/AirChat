package com.leadcore.sip.ui;

import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.R;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import com.leadcore.sms.sql.SqlDBOperate;

public class ClearAllChattingActivity extends BaseActivity implements OnClickListener, OnPreferenceClickListener {

	

	private SqlDBOperate mDBOperate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	private void init(){
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setMessage(R.string.alert_clear);
		builder.setTitle(R.string.clear_all_chatting);
		builder.setPositiveButton(R.string.clear_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mDBOperate.deteleAllChatMessageInfo();
				dialog.dismiss();  
			}
		});

		builder.setNegativeButton(R.string.clear_cancel,
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();  
					}
				});

		builder.create().show();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
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
	@Override
	public void finish() {
		if(mDBOperate!=null){
			mDBOperate.close();
			mDBOperate = null;
		}
		super.finish();
		
	}
	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		return false;
	}

}
