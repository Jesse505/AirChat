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

package org.sipdroid.sipua;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.sipdroid.net.KeepAliveSip;
import org.sipdroid.sipua.ui.ChangeAccount;
import org.sipdroid.sipua.ui.LoopAlarm;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.ui.Settings;
import org.sipdroid.sipua.ui.Sipdroid;
import org.sipdroid.sipua.utils.FileUtils;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.NetworkUtil;
import org.zoolu.net.IpAddress;
import org.zoolu.net.SocketAddress;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;

import com.dt.adhoc.service.IGroupInfoCfgRspListener;
import com.leadcore.sip.login.AdhocManager;
import com.leadcore.sip.login.GlobalVar;
import com.leadcore.sms.Sms;
import com.leadcore.sms.entity.Group;
import com.leadcore.sms.entity.Users;
import com.leadcore.sms.socket.tcp.TcpClient;
import com.leadcore.sms.socket.tcp.TcpService;
import com.leadcore.sms.socket.udp.IPMSGProtocol;
import com.leadcore.sms.sql.UserInfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class SipdroidEngine implements RegisterAgentListener {

	public static final int LINES = 1;
	public int pref;
	
	public static final int UNINITIALIZED = 0x0;
	public static final int INITIALIZED = 0x2;
	private final String TAG = "SipdroidEngine";
	
	/** User Agent */
	public UserAgent[] uas;
	public UserAgent ua;

	/** Register Agent */
	public RegisterAgent[] ras;

	private KeepAliveSip[] kas;
	
	/** UserAgentProfile */
	public UserAgentProfile[] user_profiles;

	public SipProvider[] sip_providers;
	
	static PowerManager.WakeLock[] wl;
	public static PowerManager.WakeLock[] pwl;
	static WifiManager.WifiLock[] wwl;
	
	UserAgentProfile getUserAgentProfile(String suffix) {
		UserAgentProfile user_profile = new UserAgentProfile(null);
		
		user_profile.username = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_USERNAME+suffix, Settings.DEFAULT_USERNAME); // modified
		user_profile.passwd = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_PASSWORD+suffix, Settings.DEFAULT_PASSWORD);
		if (PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_DOMAIN+suffix, Settings.DEFAULT_DOMAIN).length() == 0) {
			user_profile.realm = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_SERVER+suffix, Settings.DEFAULT_SERVER);
		} else {
			user_profile.realm = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_DOMAIN+suffix, Settings.DEFAULT_DOMAIN);
		}
		user_profile.realm_orig = user_profile.realm;
		if (PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_FROMUSER+suffix, Settings.DEFAULT_FROMUSER).length() == 0) {
			user_profile.from_url = user_profile.username;
		} else {
			user_profile.from_url = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_FROMUSER+suffix, Settings.DEFAULT_FROMUSER);
		}
		
		// MMTel configuration (added by mandrajg)
		user_profile.qvalue = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_MMTEL_QVALUE, Settings.DEFAULT_MMTEL_QVALUE);
		user_profile.mmtel = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getBoolean(Settings.PREF_MMTEL, Settings.DEFAULT_MMTEL);

		user_profile.pub = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getBoolean(Settings.PREF_EDGE+suffix, Settings.DEFAULT_EDGE) ||
			PreferenceManager.getDefaultSharedPreferences(getUIContext()).getBoolean(Settings.PREF_3G+suffix, Settings.DEFAULT_3G);
		return user_profile;
	}

	public boolean StartEngine() {
			PowerManager pm = (PowerManager) getUIContext().getSystemService(Context.POWER_SERVICE);
			WifiManager wm = (WifiManager) getUIContext().getSystemService(Context.WIFI_SERVICE);
			if (wl == null) {
				if (!PreferenceManager.getDefaultSharedPreferences(getUIContext()).contains(org.sipdroid.sipua.ui.Settings.PREF_KEEPON)) {
					Editor edit = PreferenceManager.getDefaultSharedPreferences(getUIContext()).edit();
	
					edit.putBoolean(org.sipdroid.sipua.ui.Settings.PREF_KEEPON, true);
					edit.commit();
				}
				wl = new PowerManager.WakeLock[LINES];
				pwl = new PowerManager.WakeLock[LINES];
				wwl = new WifiManager.WifiLock[LINES];
			}
			pref = ChangeAccount.getPref(Receiver.mContext);

			uas = new UserAgent[LINES];
			ras = new RegisterAgent[LINES];
			kas = new KeepAliveSip[LINES];
			lastmsgs = new String[LINES];
			sip_providers = new SipProvider[LINES];
			user_profiles = new UserAgentProfile[LINES];
			user_profiles[0] = getUserAgentProfile("");
			for (int i = 1; i < LINES; i++)
				user_profiles[1] = getUserAgentProfile(""+i);
			
			SipStack.init(null);
			int i = 0;
			
			for (UserAgentProfile user_profile : user_profiles) {
				if (wl[i] == null) {
					wl[i] = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sipdroid.SipdroidEngine");
					pwl[i] = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Sipdroid.SipdroidEngine");
					if (!PreferenceManager.getDefaultSharedPreferences(getUIContext()).getBoolean(org.sipdroid.sipua.ui.Settings.PREF_KEEPON, org.sipdroid.sipua.ui.Settings.DEFAULT_KEEPON)) {
						wwl[i] = wm.createWifiLock(3, "Sipdroid.SipdroidEngine");
						wwl[i].setReferenceCounted(false);
					}
				}
				
				try {
					SipStack.debug_level = 0;
		//			SipStack.log_path = "/data/data/org.sipdroid.sipua";
					SipStack.max_retransmission_timeout = 4000;
//					SipStack.default_transport_protocols = new String[1];
//					SipStack.default_transport_protocols[0] = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_PROTOCOL+(i!=0?i:""),
//							user_profile.realm.equals(Settings.DEFAULT_SERVER)?"tcp":"udp"); //modify by zyf
					
					String version = "Sipdroid/" + Sipdroid.getVersion() + "/" + Build.MODEL;
					SipStack.ua_info = version;
					SipStack.server_info = version;
						
					IpAddress.setLocalIpAddress();
					sip_providers[i] = new SipProvider(IpAddress.localIpAddress, 0);
					user_profile.contact_url = getContactURL(user_profile.username,sip_providers[i]);
					
					if (user_profile.from_url.indexOf("@") < 0) {
						user_profile.from_url +=
							"@"
							+ IpAddress.localIpAddress;
//							+ user_profile.realm;          //modify by zyf
					}
//					CheckEngine();      modify by zyf
//					MyLog.i("call:", "startEngine from_url: "+ user_profile.from_url);
					// added by mandrajg
					String icsi = null;
					if (user_profile.mmtel == true){
						icsi = "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"";
					}
		
					uas[i] = ua = new UserAgent(sip_providers[i], user_profile);
					ras[i] = new RegisterAgent(sip_providers[i], user_profile.from_url, // modified
							user_profile.contact_url, user_profile.username,
							user_profile.realm, user_profile.passwd, this, user_profile,
							user_profile.qvalue, icsi, user_profile.pub); // added by mandrajg
					kas[i] = new KeepAliveSip(sip_providers[i],100000);
					CheckEngine();      //modify by zyf
				} catch (Exception e) {
					MyLog.e(TAG, "StartEngine ",e);
				}
				i++;
			}
			register();
			listen();

			return true;
	}

	private String getContactURL(String username,SipProvider sip_provider) {
		int i = username.indexOf("@");
		if (i != -1) {
			// if the username already contains a @ 
			//strip it and everthing following it
			username = username.substring(0, i);
		}

		return username + "@" + IpAddress.localIpAddress
		+ (sip_provider.getPort() != 0?":"+sip_provider.getPort():"")
		+ ";transport=" + sip_provider.getDefaultTransport();		
	}
	
	void setOutboundProxy(SipProvider sip_provider,int i) {
		try {
			if (sip_provider != null) sip_provider.setOutboundProxy(new SocketAddress(
					IpAddress.getByName(PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_DNS+i, Settings.DEFAULT_DNS)),
					Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_PORT+(i!=0?i:""), Settings.DEFAULT_PORT))));
		} catch (Exception e) {
		}
	}
	
	public void CheckEngine() {
		int i = 0;
		for (SipProvider sip_provider : sip_providers) {
			if (sip_provider != null && !sip_provider.hasOutboundProxy())
				setOutboundProxy(sip_provider,i);
			i++;
		}
		updateUserProfile();   //modify by zyf
	}
	// modify by zyf
	public void updateUserProfile(){
			
			if (pref == 0) {
				user_profiles[pref] = getUserAgentProfile("");
				if (null != uas[pref]) {
					uas[pref].updateUserProfile(user_profiles[pref]);
				}
			}else {
				user_profiles[pref] = getUserAgentProfile(""+pref);
				if (null != uas[pref]) {
					uas[pref].updateUserProfile(user_profiles[pref]);
				}
			}
	}
	
	public Context getUIContext() {
		return Receiver.mContext;
	}
	
	public int getRemoteVideo() {
		return ua.remote_video_port;
	}
	
	public int getLocalVideo() {
		return ua.local_video_port;
	}
	
	public String getRemoteAddr() {
		return ua.remote_media_address;
	}
	
	public void expire() {
		Receiver.expire_time = 0;
		int i = 0;
		for (RegisterAgent ra : ras) {
			if (ra != null && ra.CurrentState == RegisterAgent.REGISTERED) {
				ra.CurrentState = RegisterAgent.UNREGISTERED;
				Receiver.onText(Receiver.REGISTER_NOTIFICATION+i, null, 0, 0);
			}
			i++;
		}
		register();
	}
	
	public void unregister(int i) {
			if (user_profiles[i] == null || user_profiles[i].username.equals("") ||
					user_profiles[i].realm.equals("")) return;

			RegisterAgent ra = ras[i];
			if (ra != null && ra.unregister()) {
				Receiver.alarm(0, LoopAlarm.class);
				Receiver.onText(Receiver.REGISTER_NOTIFICATION+i,getUIContext().getString(R.string.reg),R.drawable.sym_presence_idle,0);
				wl[i].acquire();
			} else
				Receiver.onText(Receiver.REGISTER_NOTIFICATION+i, null, 0, 0);
	}
	
	public void registerMore() {
		IpAddress.setLocalIpAddress();
//		int i = 0;
//		for (RegisterAgent ra : ras) {
//			try {
//				if (user_profiles[i] == null || user_profiles[i].username.equals("") ||
//						user_profiles[i].realm.equals("")) {
//					i++;
//					continue;
//				}
//				user_profiles[i].contact_url = getContactURL(user_profiles[i].from_url,sip_providers[i]);
//		
//				if (ra != null && !ra.isRegistered() && Receiver.isFast(i)  && ra.register()) {//modify by zyf remove the default register
//					Receiver.onText(Receiver.REGISTER_NOTIFICATION+i,getUIContext().getString(R.string.reg),R.drawable.sym_presence_idle,0);
//					wl[i].acquire();
//				}
//			} catch (Exception ex) {
//				
//			}
//			i++;
//		}
	}
	
	public void register() {
		IpAddress.setLocalIpAddress();
//		int i = 0;
//		for (RegisterAgent ra : ras) {
//			try {
//				if (user_profiles[i] == null || user_profiles[i].username.equals("") ||
//						user_profiles[i].realm.equals("")) {
//					i++;
//					continue;
//				}
//				user_profiles[i].contact_url = getContactURL(user_profiles[i].from_url,sip_providers[i]);
//		
//				if (!Receiver.isFast(i)) {
//					unregister(i);
//				} else {
//					if (ra != null && ra.register()) {  //modify by zyf remove the default register
//						Receiver.onText(Receiver.REGISTER_NOTIFICATION+i,getUIContext().getString(R.string.reg),R.drawable.sym_presence_idle,0);
//						wl[i].acquire();
//					}
//				}
//			} catch (Exception ex) {
//				
//			}
//			i++;
//		}
	}
	
	public void registerUdp() {
		IpAddress.setLocalIpAddress();
		int i = 0;
		for (RegisterAgent ra : ras) {
			try {
				if (user_profiles[i] == null || user_profiles[i].username.equals("") ||
						user_profiles[i].realm.equals("") ||
						sip_providers[i] == null ||
						sip_providers[i].getDefaultTransport() == null ||
						sip_providers[i].getDefaultTransport().equals("tcp")) {
					i++;
					continue;
				}
				user_profiles[i].contact_url = getContactURL(user_profiles[i].from_url,sip_providers[i]);
		
				if (!Receiver.isFast(i)) {
					unregister(i);
				} else {
					if (ra != null && ra.register()) {//modify by zyf remove the default register
						Receiver.onText(Receiver.REGISTER_NOTIFICATION+i,getUIContext().getString(R.string.reg),R.drawable.sym_presence_idle,0);
						wl[i].acquire();
					}
				}
			} catch (Exception ex) {
				
			}
			i++;
		}
	}

	public void halt() { // modified
		long time = SystemClock.elapsedRealtime();
		
		int i = 0;
		for (RegisterAgent ra : ras) {
//			unregister(i);
//			while (ra != null && ra.CurrentState != RegisterAgent.UNREGISTERED && SystemClock.elapsedRealtime()-time < 2000)
//				try {
//					Thread.sleep(100);
//				} catch (InterruptedException e1) {
//				}
			if (wl[i].isHeld()) {
				wl[i].release();
				if (pwl[i] != null && pwl[i].isHeld()) pwl[i].release();
				if (wwl[i] != null && wwl[i].isHeld()) wwl[i].release();
			}
			if (kas[i] != null) {
				Receiver.alarm(0, LoopAlarm.class);
				kas[i].halt();
			}
//			Receiver.onText(Receiver.REGISTER_NOTIFICATION+i, null, 0, 0);
			if (ra != null)
				ra.halt();
			if (uas[i] != null){
				uas[i].hangup();
				uas[i].closeDB();
			}
				
			if (sip_providers[i] != null)
				sip_providers[i].halt();
			i++;
		}
	}

	public boolean isRegistered()
	{
		for (RegisterAgent ra : ras)
			if (ra != null && ra.isRegistered())
				return true;
		return false;
	}
	
	public boolean isRegistered(int i)
	{
		if (ras[i] == null)
		{
			return false;
		}
		return ras[i].isRegistered();
	}
	
	public void onUaRegistrationSuccess(RegisterAgent reg_ra, NameAddress target,
			NameAddress contact, String result) {
    	int i = 0;
    	for (RegisterAgent ra : ras) {
    		if (ra == reg_ra) break;
    		i++;
    	}
		if (isRegistered(i)) {
			if (Receiver.on_wlan)
				Receiver.alarm(60, LoopAlarm.class);
			Receiver.onText(Receiver.REGISTER_NOTIFICATION+i,getUIContext().getString(i == pref?R.string.regpref:R.string.regclick),R.drawable.sym_presence_available,0);
			reg_ra.subattempts = 0;
			reg_ra.startMWI();
			Receiver.registered();
		} else
			Receiver.onText(Receiver.REGISTER_NOTIFICATION+i, null, 0,0);
		if (wl[i].isHeld()) {
			wl[i].release();
			if (pwl[i] != null && pwl[i].isHeld()) pwl[i].release();
			if (wwl[i] != null && wwl[i].isHeld()) wwl[i].release();
		}
	}

	String[] lastmsgs;
	
    public void onMWIUpdate(RegisterAgent mwi_ra, boolean voicemail, int number, String vmacc) {
    	int i = 0;
    	for (RegisterAgent ra : ras) {
    		if (ra == mwi_ra) break;
    		i++;
    	}
    	if (i != pref) return;
		if (voicemail) {
			String msgs = getUIContext().getString(R.string.voicemail);
			if (number != 0) {
				msgs = msgs + ": " + number;
			}
			Receiver.MWI_account = vmacc;
			if (lastmsgs[i] == null || !msgs.equals(lastmsgs[i])) {
				Receiver.onText(Receiver.MWI_NOTIFICATION, msgs,android.R.drawable.stat_notify_voicemail,0);
				lastmsgs[i] = msgs;
			}
		} else {
			Receiver.onText(Receiver.MWI_NOTIFICATION, null, 0,0);
			lastmsgs[i] = null;
		}
	}

	static long lasthalt,lastpwl;
	
	/** When a UA failed on (un)registering. */
	public void onUaRegistrationFailure(RegisterAgent reg_ra, NameAddress target,
			NameAddress contact, String result) {
		boolean retry = false;
    	int i = 0;
    	for (RegisterAgent ra : ras) {
    		if (ra == reg_ra) break;
    		i++;
    	}
    	if (isRegistered(i)) {
    		reg_ra.CurrentState = RegisterAgent.UNREGISTERED;
    		Receiver.onText(Receiver.REGISTER_NOTIFICATION+i, null, 0, 0);
    	} else {
    		retry = true;
    		Receiver.onText(Receiver.REGISTER_NOTIFICATION+i,getUIContext().getString(R.string.regfailed)+" ("+result+")",R.drawable.sym_presence_away,0);
    	}
    	if (retry) {
    		retry = false;
    		if (SystemClock.uptimeMillis() > lastpwl + 45000) {
				if (pwl[i] != null && !pwl[i].isHeld()) {
					if ((!Receiver.on_wlan && Build.MODEL.contains("HTC One")) || (Receiver.on_wlan && wwl[i] == null)) {
						pwl[i].acquire();
						retry = true;
					}
				}
				if (wwl[i] != null && !wwl[i].isHeld() && Receiver.on_wlan) {
					wwl[i].acquire();
					retry = true;
				}
    		}
    	}
		if (retry) {
			lastpwl = SystemClock.uptimeMillis();
			if (wl[i].isHeld()) {
				wl[i].release();
			}
			register();
			if (!wl[i].isHeld() && pwl[i] != null && pwl[i].isHeld()) pwl[i].release();
			if (!wl[i].isHeld() && wwl[i] != null && wwl[i].isHeld()) wwl[i].release();
		} else if (wl[i].isHeld()) {
			wl[i].release();
			if (pwl[i] != null && pwl[i].isHeld()) pwl[i].release();
			if (wwl[i] != null && wwl[i].isHeld()) wwl[i].release();
		}
		if (SystemClock.uptimeMillis() > lasthalt + 45000) {
			lasthalt = SystemClock.uptimeMillis();
			sip_providers[i].haltConnections();
		}
		if (!Thread.currentThread().getName().equals("main"))
			updateDNS();
		reg_ra.stopMWI();
    	WifiManager wm = (WifiManager) Receiver.mContext.getSystemService(Context.WIFI_SERVICE);
    	wm.startScan();
	}
	
	public void updateDNS() {
		Editor edit = PreferenceManager.getDefaultSharedPreferences(getUIContext()).edit();
		int i = 0;
		for (SipProvider sip_provider : sip_providers) {
			try {
				edit.putString(Settings.PREF_DNS+i, IpAddress.getByName(PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_SERVER+(i!=0?i:""), "")).toString());
			} catch (UnknownHostException e1) {
				i++;
				continue;
			}
			edit.commit();
			setOutboundProxy(sip_provider,i);
			i++;
		}
	}

	/** Receives incoming calls (auto accept) and sms data */
	public void listen() 
	{
		for (UserAgent ua : uas) {
			if (ua != null) {
				ua.printLog("UAS: WAITING FOR INCOMING CALL");
				
				if (!ua.user_profile.audio && !ua.user_profile.video)
				{
					ua.printLog("ONLY SIGNALING, NO MEDIA");
				}
				
				ua.listen();
				ua.listenSMS();  //add by zyf
			}
		}
	}
	
	public void info(char c, int duration) {
		ua.info(c, duration);
	}
	
	public void call(String target_url,String dst_ip,String calltype){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext);
		Editor editor = preferences.edit();
		editor.putString(Settings.PREF_SERVER, dst_ip);
		editor.commit();
		call(target_url, true,calltype);
	}
	
	/** Makes a new call */
	public boolean call(String target_url,boolean force,String calltype) {
		int p = pref;
		boolean found = false;
		
		if (isRegistered(p) && Receiver.isFast(p))
			found = true;
		else {
			for (p = 0; p < LINES; p++)
				if (isRegistered(p) && Receiver.isFast(p)) {
					found = true;
					break;
				}
			if (!found && force) {
				p = pref;
				/*modify by zyf start*/
				found = true;
//				if (Receiver.isFast(p))
//				{
//					found = true;
//					MyLog.e("zhaoyf", "3");
//				}
//				else for (p = 0; p < LINES; p++)
//					if (Receiver.isFast(p)) {
//						found = true;
//						MyLog.e("zhaoyf", "4");
//						break;
//					}
				/*modify by zyf end */
			}
		}
		if (!found || (ua = uas[p]) == null) {
			if (PreferenceManager.getDefaultSharedPreferences(getUIContext()).getBoolean(Settings.PREF_CALLBACK, Settings.DEFAULT_CALLBACK) &&
					PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString(Settings.PREF_POSURL, Settings.DEFAULT_POSURL).length() > 0) {
				Receiver.url("n="+Uri.decode(target_url));
				return true;
			}
			return false;
		}

		ua.printLog("UAC: CALLING " + target_url);
		
		if (!ua.user_profile.audio && !ua.user_profile.video)
		{
			 ua.printLog("ONLY SIGNALING, NO MEDIA");
		}
		return ua.call(target_url, false,calltype);
	}

	public void answercall() 
	{
		Receiver.stopRingtone();
		ua.accept();
	}
//add by liuyang for video call  start
	public void answervideocall() 
	{
		Receiver.stopRingtone();
		ua.acceptvideocall();
	}
	
	public void switch2audiocallrequest(){
		ua.Switch2AudioCallReq();
	}
//add by liuyang for video call end
	public void rejectcall() {
		ua.printLog("UA: HANGUP");
		ua.hangup();
	}

	public void togglehold() {
		ua.reInvite(null, 0);
	}

	public void transfer(String number) {
		ua.callTransfer(number, 0);
	}
	
	public boolean togglemute() {
		return ua.muteMediaApplication();
//		if (ua.muteMediaApplication())
//			Receiver.onText(Receiver.CALL_NOTIFICATION, getUIContext().getString(R.string.menu_mute), android.R.drawable.stat_notify_call_mute,Receiver.ccCall.base);
//		else
//			Receiver.progress();
	}
	
	public void togglebluetooth() {
		ua.bluetoothMediaApplication();
		Receiver.progress();
	}
	
	public int speaker(int mode) {
		int ret = ua.speakerMediaApplication(mode);
//		Receiver.progress();
		return ret;
	}
	
	public void keepAlive() {
		int i = 0;
		for (KeepAliveSip ka : kas) {
			if (ka != null && Receiver.on_wlan && isRegistered(i))
				try {
					ka.sendToken();
					Receiver.alarm(60, LoopAlarm.class);
				} catch (IOException e) {
					if (!Sipdroid.release) e.printStackTrace();
				}
			i++;
		}
	}
	
	
	public void login(String name , String number ,Context context){
		IpAddress.setLocalIpAddress();
		Receiver.isFast(0);
		if (TextUtils.isEmpty(name) || TextUtils.isEmpty(number)) {
            Toast.makeText(context, Receiver.mContext.getResources().getString(R.string.login_toast), Toast.LENGTH_LONG).show();
            return;
        }
		AdhocManager.getInstance(context).childrenMap.clear();
		GlobalVar.displayname = name;
        GlobalVar.number = number;
//		AdhocManager.getInstance(context).StartAdhoc();
//      AdhocManager.getInstance(this).setShowText(showText);
//		AdhocManager.getInstance(context).showContacts ();
		Intent serviceIntnt = new Intent();
		serviceIntnt.setAction("com.leadcore.sipdroid.loginservice");
		serviceIntnt.setPackage(context.getPackageName());
        context.startService(serviceIntnt);
		
		Receiver.onText(Receiver.REGISTER_NOTIFICATION,
				getUIContext().getString(R.string.regok),R.drawable.sym_presence_available,0);
		if (wl[0].isHeld()) {
			wl[0].release();
			if (pwl[0] != null && pwl[0].isHeld()) pwl[0].release();
			if (wwl[0] != null && wwl[0].isHeld()) wwl[0].release();
		}
	}
	
	public void logout(Context context){
//		AdhocManager.getInstance(context).StopAdhoc();
		Intent loginIntent = new Intent("com.leadcore.sipdroid.loginservice");
		loginIntent.setPackage("org.sipdroid.sipua");
		context.stopService(loginIntent);
		//清除通知
		Receiver.onText(Receiver.REGISTER_NOTIFICATION, null, 0, 0);
		Receiver.showNotification(Receiver.SMS_GROUP_NOTIFICATION, null, null);
		Receiver.showNotification(Receiver.SMS_NOTIFICATION, null, null);
	}
	
    public synchronized void sendSMSdata(int commandNo, String targetIP) {
        ua.sendSMSdata(commandNo, targetIP, null);
    }
    
    public synchronized void sendSMSdata(int commandNo, String targetIP, Object addData) {
        ua.sendSMSdata(commandNo, targetIP, addData);
    }
    
    public synchronized void sendMultiSMSdata(int commandNo, String targetIP) {
        ua.sendMultiSMSdata(commandNo, targetIP, null);
    }
    
    public synchronized void sendMultiSMSdata(int commandNo, String targetIP, Object addData) {
        ua.sendMultiSMSdata(commandNo, targetIP, addData);
    }
    
    public synchronized void sendMultiSMSdata(int commandNo, String targetIP, Object addData, String filePath) {
        ua.sendMultiSMSdata(commandNo, targetIP, addData, filePath);
    }
    
    public void stopMultiProvider(String groupIp){
    	ua.stopMultiProvider(groupIp);
    }
    
    public void onReceivedMultiSMSdata(Sms sms, IPMSGProtocol ipmsgProtocol,
			String senderIP) {
    	ua.onReceivedMultiSMSdata(sms, ipmsgProtocol, senderIP);
    }
    
    public void addUserToDB(Users user){
    	MyLog.i(TAG, "addUserToDB");
    	ua.addUser(user);
    }
    
    public UserInfo getUserInfo(String imei){
    	return ua.getUserInfo(imei);
    }
    
    public void delMemInfo(String IMEI, String groupIp){
    	ua.delMemInfo(IMEI, groupIp);
    }
    
    public void addMemberInfo(Users user){
    	ua.addMemberInfo(user);
    }
    
    public Users getMemberInfo(String IMEI, String groupIP){
    	return ua.getMemberInfo(IMEI, groupIP);
    }
    
    public List<Group> getMyGroups(){
    	return ua.getMyGroups();
    }
    //add by  lss for processing the solution of group equals null
    public Group getGroup(String groupIp){
    	return ua.getGroup(groupIp);
    }
    //add end
    
    public void startTCPClient(String path, String ipAddress, com.leadcore.sms.entity.Message.CONTENT_TYPE  type, int recordTime){
    	ua.startTCPClient(path, ipAddress, type, recordTime);
    }
    /**
     * start the tcp client to send file
     * @param path       the absolute path of sending file
     * @param dstAddress 
     * @param type
     * @param localId    the local ID in DB of the sending file
     * @param remoteId   the remote ID in DB of the sending file
     */
    public void startFileSend(String path, String dstAddress, com.leadcore.sms.entity.Message.CONTENT_TYPE  type,
    		long localId, long remoteId){
    	ua.startTCPClient(path, dstAddress, type, localId, remoteId);
    }
    
    public void delGroupDatabase(){
    	ua.delGroupDatabase();
    }
    
    public void requestGroupInfoCfg(String groupIP, List<String> members, IGroupInfoCfgRspListener.Stub listener){
    	ua.requestGroupInfoCfg(groupIP, members, listener);
    }
    
    //********************************* PTT start *******************************
    
    public void pausePttSend(){
    	ua.pausePttSend();
    }
    
    public void pausePttRece(){
    	ua.pausePttReceive();
    }
    
    public boolean stopPlayPtt(){
    	return ua.stopPlayPtt();
    }
    
	public int pttSpeaker(int mode){
		int ret = ua.speakerPttApplication(mode);
		return ret;
	}
	public void launchPttSession(String targetIp){
		ua.launchPttSession(targetIp);
	}
	
	public boolean pttSpeak(){
		return ua.pttSpeak();
	}
	
	public void dismissPttSession(String targetIp){
		ua.dismissPttSession(targetIp);
	}
	
	public void closePttSession(){
		ua.closePttSession();
	}
	//******************************** PTT end *********************************
}
