package com.leadcore.sip.login;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LoginService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		AdhocManager.getInstance(this).StartAdhoc();
//      AdhocManager.getInstance(this).setShowText(showText);
		AdhocManager.getInstance(this).showContacts ();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		AdhocManager.getInstance(this).StopAdhoc();
	}
	
	

}
