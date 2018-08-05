package org.sipdroid.sipua.ui;

import java.text.BreakIterator;
import java.util.ArrayList;

import org.sipdroid.media.PttRtpStreamReceiver;
import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.UserAgent;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.SessionUtils;

import com.jstun.core.attribute.SourceAddress;
import com.leadcore.sms.entity.Users;

import android.app.KeyguardManager;
import android.app.KeyguardManager.OnKeyguardExitResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class PttCallActivity extends BaseActivity implements OnClickListener,OnLongClickListener,
	OnTouchListener{

	private static final String TAG = "PttCallActivity";
	private TextView tv_status;
	private Button btn_speak,btn_stop,btn_mute;
	private ArrayList<Users>  onlineContacts;	   //在线成员集合
	private boolean isSpeaking = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		initEvents();
		onlineContacts = (ArrayList<Users>) BaseActivity.mApplication.getOnlineUsers();
	}
	
	
	
	@Override
	protected void onStart() {
		if (Integer.parseInt(Build.VERSION.SDK) < 5 || Integer.parseInt(Build.VERSION.SDK) > 7)
			disableKeyguard();
		super.onStart();
	}



	@Override
	protected void onResume() {
		if (Integer.parseInt(Build.VERSION.SDK) >= 5 && Integer.parseInt(Build.VERSION.SDK) <= 7)
			disableKeyguard();
		super.onResume();
	}



	@Override
	protected void onPause() {
		if (Integer.parseInt(Build.VERSION.SDK) >= 5 && Integer.parseInt(Build.VERSION.SDK) <= 7)
			reenableKeyguard();
		super.onPause();
	}



	@Override
	protected void onStop() {
		if (Integer.parseInt(Build.VERSION.SDK) < 5 || Integer.parseInt(Build.VERSION.SDK) > 7)
			reenableKeyguard();
		super.onStop();
	}



	@Override
	protected void onDestroy() {
		unregisterReceiver(myReceiver);
		super.onDestroy();
	}
	@Override
	protected void initViews() {
		setContentView(R.layout.activity_ptt_call);
		tv_status = (TextView) findViewById(R.id.tv_status);
		tv_status.setText(R.string.text_ptt_free);
		btn_speak = (Button) findViewById(R.id.btn_speak);
		btn_mute  = (Button) findViewById(R.id.btn_mute);
		btn_stop = (Button) findViewById(R.id.btn_stop);
		if (Receiver.ptt_launcher.equals(SessionUtils.getLocalIPaddress())) {
			btn_stop.setVisibility(View.VISIBLE);
		}else {
			btn_stop.setVisibility(View.GONE);
		}
	}

	@Override
	protected void initEvents() {
		btn_speak.setOnClickListener(this);
		btn_speak.setOnLongClickListener(this);
		btn_speak.setOnTouchListener(this);
		btn_stop.setOnClickListener(this);
		btn_mute.setOnClickListener(this);
		registerReceiver(myReceiver, new IntentFilter(Receiver.ACTION_PTT_STATE_CHANGE));
	}

	@Override
	public void processMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onLongClick(View v) {
		if (!Receiver.engine(mContext).pttSpeak()) {
			showShortToast(R.string.text_ptt_occury);
		}else {
			isSpeaking = true;
		}
		MyLog.i(TAG, "onLongClick");
		return false;
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_stop:
			Receiver.engine(mContext).dismissPttSession(Receiver.ptt_group_ip);
			finish();
			MyLog.i(TAG, "closePttSession");
			break;
		case R.id.btn_speak:
			
			break;
		case R.id.btn_mute:
			Receiver.engine(mContext).stopPlayPtt();
			break;
		default:
			break;
		}
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			MyLog.i(TAG, "ACTION_UP");
			if (isSpeaking) {
				MyLog.i(TAG, "pttSpeak");
				Receiver.engine(mContext).pttSpeak();
				isSpeaking = false;
			}
			break;

		default:
			break;
		}
		return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			//nothing to do 
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_UP:
			PttRtpStreamReceiver.adjust(keyCode, true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_UP:
			PttRtpStreamReceiver.adjust(keyCode, false);
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			int ptt_state = intent.getIntExtra(Receiver.KEY_PTT_STATE, UserAgent.PTT_STATE_OFF);
			switch (ptt_state) {
			case UserAgent.PTT_STATE_OFF:
				finish();
				break;
			case UserAgent.PTT_STATE_ON:
				tv_status.setText(R.string.text_ptt_initing);
				break;
			
			case UserAgent.PTT_STATE_FREE:
				tv_status.setText(R.string.text_ptt_free);
				break;
			case UserAgent.PTT_STATE_SPEAKING:
				tv_status.setText(R.string.text_ptt_i_speaking);
				break;
			case UserAgent.PTT_STATE_OCCUPY:
				String SourceAddress = intent.getStringExtra(Receiver.KEY_PTT_SOURCE_ADDRESS);
				if (getSpeakingName(SourceAddress) != null) {
					tv_status.setText(getSpeakingName(SourceAddress)+getString(R.string.text_ptt_speaking));
				}else {
					tv_status.setText(R.string.text_ptt_other_speaking);
				}
				break;
			default:
				break;
			}
		}
	};
	
	private String getSpeakingName(String ip){
		for (Users  user: onlineContacts) {
			if (user.getIpaddress().equals(ip)) {
				return user.getNickname();
			}
		}
		return null;
	}
	
	//*********************add by zyf for 解锁屏幕  start***********************/
	long enabletime;
    KeyguardManager mKeyguardManager;
    KeyguardManager.KeyguardLock mKeyguardLock;
    boolean enabled;
    
	void disableKeyguard() {
    	if (mKeyguardManager == null) {
	        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
	        mKeyguardLock = mKeyguardManager.newKeyguardLock("Sipdroid.PttCallActivity");
	        enabled = true;
    	}
		if (enabled) {
			mKeyguardLock.disableKeyguard();
			if (Integer.parseInt(Build.VERSION.SDK) >= 16)
				mKeyguardManager.exitKeyguardSecurely(new OnKeyguardExitResult() {
				    public void onKeyguardExitResult(boolean success) {
				    }
				});
			enabled = false;
			enabletime = SystemClock.elapsedRealtime();
		}
	}
	
	void reenableKeyguard() {
		if (!enabled) {
				try {
					if (Integer.parseInt(Build.VERSION.SDK) < 5)
						Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			mKeyguardLock.reenableKeyguard();
			enabled = true;
		}
	}
	
	//*********************add by zyf for 解锁屏幕  end***********************/
}
