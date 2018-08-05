/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
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

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.sipdroid.codecs.Codec;
import org.sipdroid.codecs.Codecs;
import org.sipdroid.media.JAudioLauncher;
import org.sipdroid.media.MediaLauncher;
import org.sipdroid.media.PttAudioLauncher;
import org.sipdroid.media.PttRtpStreamReceiver;
import org.sipdroid.media.RtpStreamReceiver;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.ui.Settings;
import org.sipdroid.sipua.ui.Sipdroid;
import org.sipdroid.sipua.utils.ActivityCollectorUtils;
import org.sipdroid.sipua.utils.DateUtils;
import org.sipdroid.sipua.utils.FileUtils;
import org.sipdroid.sipua.utils.ImageUtils;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.NetworkUtil;
import org.sipdroid.sipua.utils.SessionUtils;
import org.zoolu.net.IpAddress;
import org.zoolu.sdp.AttributeField;
import org.zoolu.sdp.ConnectionField;
import org.zoolu.sdp.MediaDescriptor;
import org.zoolu.sdp.MediaField;
import org.zoolu.sdp.SessionDescriptor;
import org.zoolu.sdp.TimeField;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.call.Call;
import org.zoolu.sip.call.CallListenerAdapter;
import org.zoolu.sip.call.ExtendedCall;
import org.zoolu.sip.call.SdpTools;
import org.zoolu.sip.header.StatusLine;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Log;
import org.zoolu.tools.LogLevel;
import org.zoolu.tools.Parser;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.RemoteException;

import com.dt.adhoc.service.IGroupInfoCfgRspListener;
import com.dt.adhoc.service.ILinkListener;
import com.leadcore.sip.login.AdhocManager;
import com.leadcore.sms.Sms;
import com.leadcore.sms.SmsListener;
import com.leadcore.sms.entity.Entity;
import com.leadcore.sms.entity.Group;
import com.leadcore.sms.entity.Users;
import com.leadcore.sms.entity.Message.CONTENT_TYPE;
import com.leadcore.sms.entity.Users;
import com.leadcore.sms.file.FileState;
import com.leadcore.sms.socket.tcp.TcpClient;
import com.leadcore.sms.socket.tcp.TcpClientListener;
import com.leadcore.sms.socket.tcp.TcpFileClient;
import com.leadcore.sms.socket.tcp.TcpFileServer;
import com.leadcore.sms.socket.tcp.TcpClient;
import com.leadcore.sms.socket.tcp.TcpService;
import com.leadcore.sms.socket.tcp.TcpServiceListener;
import com.leadcore.sms.socket.udp.IPMSGConst;
import com.leadcore.sms.socket.udp.IPMSGProtocol;
import com.leadcore.sms.sql.ChattingInfo;
import com.leadcore.sms.sql.SqlDBOperate;
import com.leadcore.sms.sql.UserInfo;

import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;

/**
 * Simple SIP user agent (UA). It includes audio/video applications.
 * <p>
 * It can use external audio/video tools as media applications. Currently only
 * RAT (Robust Audio Tool) and VIC are supported as external applications.
 */
public class UserAgent extends CallListenerAdapter implements SmsListener,TcpServiceListener,TcpClientListener{
	/** Event logger. */
	Log log;

	/** UserAgentProfile */
	public UserAgentProfile user_profile;

	/** SipProvider */
	protected SipProvider sip_provider;

	/** Call */
	// Call call;
	protected ExtendedCall call;
	
	/** SMS */
	protected Sms sms;

	/** Call transfer */
	protected ExtendedCall call_transfer;

	/** Audio application */
	public JAudioLauncher audio_app = null;
	/** Ptt Audio application */
	public PttAudioLauncher ptt_app = null;

	/** Local sdp */
	protected String local_session = null;
	
	public static final int UA_STATE_IDLE = 0;
	public static final int UA_STATE_INCOMING_CALL = 1;
	public static final int UA_STATE_OUTGOING_CALL = 2;
	public static final int UA_STATE_INCALL = 3;
	public static final int UA_STATE_VIDEO_INCALL = 4; //modify by liuyang for add video call
	public static final int UA_STATE_HOLD = 5;
	public static final int UA_STATE_RINGING = 6; //modify by zyf
	
	//add by liuyang for video call start 
	public static final String VoiceCall = "audio";
	public static final String VideoCall = "video";
	public String CallType ;
		
	public static final int PTT_STATE_OFF = 6;       //ptt 关闭
	public static final int PTT_STATE_ON  = 7;       //ptt 开启
	public static final int PTT_STATE_SPEAKING= 8;   //ptt 讲话中
	public static final int PTT_STATE_OCCUPY = 9;    //ptt 占用
	public static final int PTT_STATE_FREE   = 10;    //ptt 空闲
	int ptt_state = PTT_STATE_OFF;
	String remote_ptt_media_address;
	
	PowerManager pm;
	PowerManager.WakeLock pwl1,pwl2,pwl3;

	private SqlDBOperate mDBOperate;
	public TcpService tcpService;
	public TcpClient tcpClient;
	
	
	int call_state = UA_STATE_IDLE;
	String remote_media_address;
	int remote_video_port,local_video_port;
	private final String TAG = "UserAgent";
	
	public com.dt.adhoc.sdk.AdhocManager adhocManager = null;
	public ArrayList<Integer> portList = null;
	public int linkServID = 0;
	public int linkServType = 0;
	public boolean isCreateLinkSuccess = false;
	private static Object linkLock = new Object();
	public static final String ACTION_MESSAGELIST_REFRESH = "com.example.message_list.screen.refresh";

	// *************************** Basic methods ***************************

	/** Changes the call state */
	protected synchronized void changeStatus(int state,String caller,String type) {
		call_state = state;
		CallType = type;
		Receiver.onState(state, caller,type);
	}
	
	protected void changeStatus(int state) {
		changeStatus(state, null,null);
	}

	/** Checks the call state */
	protected boolean statusIs(int state) {
		return (call_state == state);
	}

	/**
	 * Sets the automatic answer time (default is -1 that means no auto accept
	 * mode)
	 */
	public void setAcceptTime(int accept_time) {
		user_profile.accept_time = accept_time;
	}

	/**
	 * Sets the automatic hangup time (default is 0, that corresponds to manual
	 * hangup mode)
	 */
	public void setHangupTime(int time) {
		user_profile.hangup_time = time;
	}

	/** Sets the redirection url (default is null, that is no redircetion) */
	public void setRedirection(String url) {
		user_profile.redirect_to = url;
	}

	/** Sets the no offer mode for the invite (default is false) */
	public void setNoOfferMode(boolean nooffer) {
		user_profile.no_offer = nooffer;
	}

	/** Enables audio */
	public void setAudio(boolean enable) {
		user_profile.audio = enable;
	}

	/** Sets the receive only mode */
	public void setReceiveOnlyMode(boolean r_only) {
		user_profile.recv_only = r_only;
	}

	/** Sets the send only mode */
	public void setSendOnlyMode(boolean s_only) {
		user_profile.send_only = s_only;
	}

	/** Sets the send tone mode */
	public void setSendToneMode(boolean s_tone) {
		user_profile.send_tone = s_tone;
	}

	/** Sets the send file */
	
	public void setSendFile(String file_name) {
		user_profile.send_file = file_name;
	}

	/** Sets the recv file */
	
	public void setRecvFile(String file_name) {
		user_profile.recv_file = file_name;
	}
	
	/** Gets the local SDP */
	public String getSessionDescriptor() {
		return local_session;
	}

	//change start (multi codecs)
	/** Inits the local SDP (no media spec) */
	public void initSessionDescriptor(Codecs.Map c) {
		SessionDescriptor sdp = new SessionDescriptor(
				user_profile.from_url,
				sip_provider.getViaAddress());
		
		local_session = sdp.toString();
		
		//We will have at least one media line, and it will be 
		//audio
		if (user_profile.audio || !user_profile.video)
		{
//			addMediaDescriptor("audio", user_profile.audio_port, c, user_profile.audio_sample_rate);
			addMediaDescriptor("audio", user_profile.audio_port, c);
		}
		
		if (user_profile.video)
		{
			addMediaDescriptor("video", user_profile.video_port,
					user_profile.video_avp, "h264", 90000);
		}
	}
	//change end
	
	/** Adds a single media to the SDP */
	private void addMediaDescriptor(String media, int port, int avp,
					String codec, int rate) {
		SessionDescriptor sdp = new SessionDescriptor(local_session);
		
		String attr_param = String.valueOf(avp);
		
		if (codec != null)
		{
			attr_param += " " + codec + "/" + rate;
		}
		sdp.addMedia(new MediaField(media, port, 0, "RTP/AVP", 
				String.valueOf(avp)), 
				new AttributeField("rtpmap", attr_param));
		
		local_session = sdp.toString();
	}
	
	/** Adds a set of media to the SDP */
//	private void addMediaDescriptor(String media, int port, Codecs.Map c,int rate) {
	private void addMediaDescriptor(String media, int port, Codecs.Map c) {
		SessionDescriptor sdp = new SessionDescriptor(local_session);
	
		Vector<String> avpvec = new Vector<String>();
		Vector<AttributeField> afvec = new Vector<AttributeField>();
		if (c == null) {
			// offer all known codecs
			for (int i : Codecs.getCodecs()) {
				Codec codec = Codecs.get(i);
				if (i == 0) codec.init();
				avpvec.add(String.valueOf(i));
				if (codec.number() == 9)
					afvec.add(new AttributeField("rtpmap", String.format("%d %s/%d", i, codec.userName(), 8000))); // kludge for G722. See RFC3551.
				else
					afvec.add(new AttributeField("rtpmap", String.format("%d %s/%d", i, codec.userName(), codec.samp_rate())));
			}
		} else {
			c.codec.init();
			avpvec.add(String.valueOf(c.number));
			if (c.codec.number() == 9)
				afvec.add(new AttributeField("rtpmap", String.format("%d %s/%d", c.number, c.codec.userName(), 8000))); // kludge for G722. See RFC3551.
			else
				afvec.add(new AttributeField("rtpmap", String.format("%d %s/%d", c.number, c.codec.userName(), c.codec.samp_rate())));
		}
		if (user_profile.dtmf_avp != 0){
			avpvec.add(String.valueOf(user_profile.dtmf_avp));
			afvec.add(new AttributeField("rtpmap", String.format("%d telephone-event/%d", user_profile.dtmf_avp, user_profile.audio_sample_rate)));
			afvec.add(new AttributeField("fmtp", String.format("%d 0-15", user_profile.dtmf_avp)));
		}
				
		//String attr_param = String.valueOf(avp);
		
		sdp.addMedia(new MediaField(media, port, 0, "RTP/AVP", avpvec), afvec);
		
		local_session = sdp.toString();
	}

	// *************************** Public Methods **************************

	
	/** Costructs a UA with a default media port */
	public UserAgent(SipProvider sip_provider, UserAgentProfile user_profile) {
		this.sip_provider = sip_provider;
		log = sip_provider.getLog();
		this.user_profile = user_profile;
		realm = user_profile.realm;
		// if no contact_url and/or from_url has been set, create it now
		user_profile.initContactAddress(sip_provider);
		mDBOperate = new SqlDBOperate(Receiver.mContext);
		MyLog.i("UserAgent", "new SqlDBOperate");
		adhocManager = new com.dt.adhoc.sdk.AdhocManager(Receiver.mContext, null);
	}

	String realm;
	
	/**
	 * when start the sipdroidEngine every time, must update the UserProfile
	 * @param user_profile
	 */
	public void updateUserProfile(UserAgentProfile user_profile){
		this.user_profile = user_profile;
		realm = user_profile.realm;
		user_profile.initContactAddress(sip_provider);
	}
	
	/** Makes a new call (acting as UAC). */
	public boolean call(String target_url, boolean send_anonymous,String calltype) {
		
		if (Receiver.call_state != UA_STATE_IDLE)
		{
			//We can initiate or terminate a call only when
			//we are in an idle state
			printLog("Call attempted in state" + this.getSessionDescriptor() + " : Failing Request", LogLevel.HIGH);
			return false;
		}
		hangup(); // modified
		changeStatus(UA_STATE_OUTGOING_CALL,target_url,calltype);
		
		String from_url;
		
		if (!send_anonymous)
		{
			from_url = user_profile.from_url;
		}
		else
		{
			from_url = "sip:anonymous@anonymous.com";
		}

		//change start multi codecs
		createOffer();
		//change end
		call = new ExtendedCall(sip_provider, from_url,
				user_profile.contact_url, user_profile.username,
				user_profile.realm, user_profile.passwd, this);
		
		// in case of incomplete url (e.g. only 'user' is present), try to
		// complete it
		if (target_url.indexOf("@") < 0) {
			if (user_profile.realm.equals(Settings.DEFAULT_SERVER))
				target_url = "&" + target_url;
			target_url = target_url + "@" + realm; // modified
		}
		
		// MMTel addition to define MMTel ICSI to be included in INVITE (added by mandrajg)
		String icsi = null;	
		if (user_profile.mmtel == true){
			icsi = "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"";
		}		
		
		target_url = sip_provider.completeNameAddress(target_url).toString();
		
		if (user_profile.no_offer)
		{
			call.call(target_url,calltype);
		}
		else
		{
			call.call(target_url, local_session, icsi,calltype);		// modified by mandrajg
		}
		
		return true;
	}

	public void info(char c, int duration)
	{
		boolean use2833 = audio_app != null && audio_app.sendDTMF(c); // send out-band DTMF (rfc2833) if supported

		if (!use2833 && call != null)
			call.info(c, duration);
	}
	
	/** Waits for an incoming call (acting as UAS). */
	public boolean listen() {
		
		if (Receiver.call_state != UA_STATE_IDLE)
		{
			//We can listen for a call only when
			//we are in an idle state
			printLog("Call listening mode initiated in " + this.getSessionDescriptor() + " : Failing Request", LogLevel.HIGH);
			return false;
		}
		
		hangup();
		
		call = new ExtendedCall(sip_provider, user_profile.from_url,
				user_profile.contact_url, user_profile.username,
				user_profile.realm, user_profile.passwd, this);
		call.listen();
		
		return true;
	}

	/** Closes an ongoing, incoming, or pending call */
	public void hangup() 
	{
		printLog("HANGUP");
		closeMediaApplication();
		
		if (call != null)
		{
			call.hangup();
		}
		
		changeStatus(UA_STATE_IDLE);
		MyLog.i("UserAgent", "HANGUP");
		
	}

	/** Accepts an incoming call */
	public boolean accept() 
	{
		if (call == null)
		{
			return false;
		}
		
		printLog("ACCEPT");
		changeStatus(UA_STATE_INCALL); // modified

		call.accept(local_session);
		
		return true;
	}
	/** Accepts an vieo incoming call */
	public boolean acceptvideocall() 
	{
		if (call == null)
		{
			return false;
		}
		
		printLog("ACCEPT VIDEO CALL");
		changeStatus(UA_STATE_VIDEO_INCALL);

		call.accept(local_session);
		
		return true;
	}
	/** Redirects an incoming call */
	public void redirect(String redirection) 
	{
		if (call != null)
		{
			call.redirect(redirection);
		}
	}

	/** Launches the Media Application (currently, the RAT audio tool) */
	protected void launchMediaApplication(String callType) {
		// exit if the Media Application is already running
		if (audio_app != null) {
			printLog("DEBUG: media application is already running",
					LogLevel.HIGH);
			return;
		}
		MyLog.i(TAG, "launchMediaApplication");
		Codecs.Map c;
		// parse local sdp
		SessionDescriptor local_sdp = new SessionDescriptor(call
				.getLocalSessionDescriptor());
		int local_audio_port = 0;
		local_video_port = 0;
		int dtmf_pt = 0;
		c = Codecs.getCodec(local_sdp);
		if (c == null) {
			Receiver.call_end_reason = R.string.card_title_ended_no_codec;
			hangup();
			return;
		}
		MyLog.i(TAG, "launchMediaApplication c: " + c.toString());
		MediaDescriptor m = local_sdp.getMediaDescriptor("video");
		if ( m != null)
			local_video_port = m.getMedia().getPort();
		m = local_sdp.getMediaDescriptor("audio");
		if (m != null) {
			local_audio_port = m.getMedia().getPort();
			if (m.getMedia().getFormatList().contains(String.valueOf(user_profile.dtmf_avp)))
				dtmf_pt = user_profile.dtmf_avp;
		}
		// parse remote sdp
		SessionDescriptor remote_sdp = new SessionDescriptor(call
				.getRemoteSessionDescriptor());
		MyLog.i(TAG, "remote_sdp " + remote_sdp.toString());
		remote_media_address = (new Parser(remote_sdp.getConnection()
				.toString())).skipString().skipString().getString();
		int remote_audio_port = 0;
		remote_video_port = 0;
		for (Enumeration<MediaDescriptor> e = remote_sdp.getMediaDescriptors()
				.elements(); e.hasMoreElements();) {
			MediaField media = e.nextElement().getMedia();
			if (media.getMedia().equals("audio"))
				remote_audio_port = media.getPort();
			if (media.getMedia().equals("video"))
				remote_video_port = media.getPort();
		}

		// select the media direction (send_only, recv_ony, fullduplex)
		int dir = 0;
		if (user_profile.recv_only)
			dir = -1;
		else if (user_profile.send_only)
			dir = 1;

		if (NetworkUtil.network_type == NetworkUtil.ADHOC_NETWORK) {
			if (adhocManager != null && local_audio_port != 0) {
				portList = new ArrayList<Integer>();
				if (callType.equals(VoiceCall)) {
					portList.add(local_audio_port);
					linkServID = adhocManager.requestLinkSetup(9,32000,  portList, remote_media_address, 
							SessionUtils.getLocalIPaddress(), new MyLinkListener());
				}else if (callType.equals(VideoCall)) {
					portList.add(local_audio_port);
					portList.add(local_video_port);
					linkServID = adhocManager.requestLinkSetup(9,512000,  portList, remote_media_address, 
							SessionUtils.getLocalIPaddress(), new MyLinkListener());
				}
			}
			synchronized (linkLock) {
				try {
					linkLock.wait(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (!isCreateLinkSuccess) {
					MyLog.i(TAG, "zhaoyifei_call requestAudioLinkSetup failed");
					Receiver.call_end_reason = R.string.card_title_ended_no_link;
					hangup();
					return;
				}
			}
		}else if (NetworkUtil.network_type == NetworkUtil.WIFI) {
			//nothing to do
		}
		
		
		
		if (user_profile.audio && local_audio_port != 0
				&& remote_audio_port != 0) { // create an audio_app and start
												// it

			if (audio_app == null) { // for testing..
				String audio_in = null;
				if (user_profile.send_tone) {
					audio_in = JAudioLauncher.TONE;
				} else if (user_profile.send_file != null) {
					audio_in = user_profile.send_file;
				}
				String audio_out = null;
				if (user_profile.recv_file != null) {
					audio_out = user_profile.recv_file;
				}
				MyLog.i(TAG, "JAudioLauncher remote_media_address: " + remote_media_address);
				MyLog.i(TAG, "JAudioLauncher local_audio_port: " + local_audio_port);
				MyLog.i(TAG, "JAudioLauncher remote_audio_port: " + remote_audio_port);
				audio_app = new JAudioLauncher(local_audio_port,
						remote_media_address, remote_audio_port, dir, audio_in,
						audio_out, c.codec.samp_rate(),
						user_profile.audio_sample_size,
						c.codec.frame_size(), log, c, dtmf_pt);
			}
			audio_app.startMedia();
		}
	}

	/** Close the Media Application */
	protected void closeMediaApplication() {
		MyLog.i(TAG, "closeMediaApplication");
		if (audio_app != null) {
			audio_app.stopMedia();
			audio_app = null;
			if (NetworkUtil.network_type == NetworkUtil.ADHOC_NETWORK) {
//				if (adhocManager != null) {
//					linkServType = 9; //servType audio is 8,video is 9
//					MyLog.i(TAG, "zhaoyifei_call requestLinkDelete linkServID:" + linkServID);
//					adhocManager.requestLinkDelete(linkServType, linkServID);
//				}
			}else if (NetworkUtil.network_type == NetworkUtil.WIFI) {
				//nothing to do
			}
		}
	}
	
	public boolean muteMediaApplication() {
		if (audio_app != null)
			return audio_app.muteMedia();
		return false;
	}

	public int speakerMediaApplication(int mode) {
		int old;
		
		if (audio_app != null)
			return audio_app.speakerMedia(mode);
		old = RtpStreamReceiver.speakermode;
		RtpStreamReceiver.speakermode = mode;
		return old;
	}

	public void bluetoothMediaApplication() {
		if (audio_app != null)
			audio_app.bluetoothMedia();
	}

	private void createOffer() {
		initSessionDescriptor(null);
	}

	private void createAnswer(SessionDescriptor remote_sdp) {

		Codecs.Map c = Codecs.getCodec(remote_sdp);
		if (c == null)
			throw new RuntimeException("Failed to get CODEC: AVAILABLE : " + remote_sdp);
		initSessionDescriptor(c);
		sessionProduct(remote_sdp);
	}

	private void sessionProduct(SessionDescriptor remote_sdp) {
		SessionDescriptor local_sdp = new SessionDescriptor(local_session);
		SessionDescriptor new_sdp = new SessionDescriptor(local_sdp
				.getOrigin(), local_sdp.getSessionName(), local_sdp
				.getConnection(), local_sdp.getTime());
		new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
		new_sdp = SdpTools.sdpMediaProduct(new_sdp, remote_sdp
				.getMediaDescriptors());
		//new_sdp = SdpTools.sdpAttirbuteSelection(new_sdp, "rtpmap"); ////change multi codecs
		local_session = new_sdp.toString();
		if (call!=null) call.setLocalSessionDescriptor(local_session);
	}

	// ********************** Call callback functions **********************

	/**
	 * Callback function called when arriving a new INVITE method (incoming
	 * call)
	 */
	public void onCallIncoming(Call call, NameAddress callee,
			NameAddress caller, String sdp, Message invite) {
		printLog("onCallIncoming()", LogLevel.LOW);
		MyLog.e("zyf", "onCallIncoming");
		
		if (call != this.call) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		
		String calltype = invite.getCallTypeHeader().getValue();
		MyLog.d("UA", "new call incoming the type ="+calltype);
		printLog("INCOMING", LogLevel.HIGH);
		int i = 0;
		for (UserAgent ua : Receiver.mSipdroidEngine.uas) {
			if (ua == this) break;
			i++;
		}
		if (Receiver.call_state != UA_STATE_IDLE || !Receiver.isFast(i)) { 
			call.busy();
			listen();
			return;
		}
		
		if (Receiver.mSipdroidEngine != null)
			Receiver.mSipdroidEngine.ua = this;
		changeStatus(UA_STATE_INCOMING_CALL,caller.toString(),calltype);

		if (sdp == null) {
			createOffer();
		}
		else { 
			SessionDescriptor remote_sdp = new SessionDescriptor(sdp);
			try {
				createAnswer(remote_sdp);
			} catch (Exception e) {
				// only known exception is no codec
				Receiver.call_end_reason = R.string.card_title_ended_no_codec;
				changeStatus(UA_STATE_IDLE);
				return;
			}
		}
		call.ring(local_session);	
		launchMediaApplication(calltype);
	}

	/**
	 * Callback function called when arriving a new Re-INVITE method
	 * (re-inviting/call modify)
	 */
	public void onCallModifying(Call call, String sdp, Message invite) 
	{
		printLog("onCallModifying()", LogLevel.LOW);
		if (call != this.call) 
		{
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("RE-INVITE/MODIFY", LogLevel.HIGH);

		// to be implemented.
		// currently it simply accepts the session changes (see method
		// onCallModifying() in CallListenerAdapter)
		super.onCallModifying(call, sdp, invite);
	}

	/**
	 * Callback function that may be overloaded (extended). Called when arriving
	 * a 180 Ringing or a 183 Session progress with SDP 
	 */
	public void onCallRinging(Call call, Message resp) {
		printLog("onCallRinging()", LogLevel.LOW);
		MyLog.i(TAG, "onCallRinging()");
		if (call != this.call && call != call_transfer) 
		{
			MyLog.i(TAG, "NOT the current call");
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		
		String remote_sdp = call.getRemoteSessionDescriptor();
		if (remote_sdp==null || remote_sdp.length()==0) {
			MyLog.i(TAG, "RINGING");
			printLog("RINGING", LogLevel.HIGH);
			RtpStreamReceiver.ringback(true);
		}
		else {
			MyLog.i(TAG, "RINGING(with SDP)");
			printLog("RINGING(with SDP)", LogLevel.HIGH);
			if (! user_profile.no_offer) { 
				MyLog.i(TAG, "launchMediaApplication()");
				RtpStreamReceiver.ringback(false);
				// Update the local SDP along with offer/answer 
				sessionProduct(new SessionDescriptor(remote_sdp));
				/*start 2016-9-13 zyf for bug:phone not connected ,but can communicate*/
				Receiver.call_state = UA_STATE_RINGING;
				/* end  2016-9-13 zyf for bug:phone not connected ,but can communicate*/
				String calltype = resp.getCallTypeHeader().getCallType();
				launchMediaApplication(calltype);
			}
		}
	}

	/** Callback function called when arriving a 2xx (call accepted) */
	public void onCallAccepted(Call call, String sdp, Message resp) 
	{
		printLog("onCallAccepted()", LogLevel.LOW);
		
		if (call != this.call && call != call_transfer) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("ACCEPTED/CALL", LogLevel.HIGH);
		String calltype = resp.getCallTypeHeader().getCallType();//add by liuyang
	    MyLog.d(TAG, " calltype == "+calltype);	
		if (!statusIs(UA_STATE_OUTGOING_CALL)) { // modified
			hangup();
			return;
		}
		if(VideoCall.equals(calltype))
		changeStatus(UA_STATE_VIDEO_INCALL);
		else
		changeStatus(UA_STATE_INCALL);
		
		SessionDescriptor remote_sdp = new SessionDescriptor(sdp);
		if (user_profile.no_offer) {
			// answer with the local sdp
			createAnswer(remote_sdp);
			call.ackWithAnswer(local_session);
		} else {
			// Update the local SDP along with offer/answer 
			sessionProduct(remote_sdp);
		}
		launchMediaApplication(calltype);
		
		if (call == call_transfer) 
		{
			StatusLine status_line = resp.getStatusLine();
			int code = status_line.getCode();
			String reason = status_line.getReason();
			this.call.notify(code, reason);
		}
	}
	/** close the video call and switch to audio call request*/
	public void Switch2AudioCallReq() 
	{
		printLog("switch to audio call request");
		//changeStatus(UA_STATE_INCALL);
		this.call.audioswitchreq();
		MyLog.i("UserAgent", "switch to audio call request");
		
	}
	/** Callback function called when arriving an ACK method (call confirmed) */
	public void onCallConfirmed(Call call, String sdp, Message ack) 
	{
		printLog("onCallConfirmed()", LogLevel.LOW);
	
		if (call != this.call) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		
		printLog("CONFIRMED/CALL", LogLevel.HIGH);

//		changeStatus(UA_STATE_INCALL); modified
		
		if (user_profile.hangup_time > 0)
		{
			this.automaticHangup(user_profile.hangup_time);
		}
	}

	/** Callback function called when arriving a 2xx (re-invite/modify accepted) */
	public void onCallReInviteAccepted(Call call, String sdp, Message resp) {
		printLog("onCallReInviteAccepted()", LogLevel.LOW);
		if (call != this.call) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("RE-INVITE-ACCEPTED/CALL", LogLevel.HIGH);
		if (statusIs(UA_STATE_HOLD))
			changeStatus(UA_STATE_INCALL);
		else
			changeStatus(UA_STATE_HOLD);
	}

	/** Callback function called when arriving a 4xx (re-invite/modify failure) */
	public void onCallReInviteRefused(Call call, String reason, Message resp) {
		printLog("onCallReInviteRefused()", LogLevel.LOW);
		if (call != this.call) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("RE-INVITE-REFUSED (" + reason + ")/CALL", LogLevel.HIGH);
	}

	/** Callback function called when arriving a 4xx (call failure) */
	public void onCallRefused(Call call, String reason, Message resp) {
		printLog("onCallRefused()", LogLevel.LOW);
		if (call != this.call) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("REFUSED (" + reason + ")", LogLevel.HIGH);
		MyLog.call_i("UserAgent onCallRefused (" + reason + ")");
		if (reason.equalsIgnoreCase("not acceptable here")) {
			// bummer we have to string compare, this is sdp 488
			Receiver.call_end_reason = R.string.card_title_ended_no_codec;
		}
		if (reason.equalsIgnoreCase("Busy Here")) {
			// bummer we have to string compare, this is sdp 486
			Receiver.call_end_reason = R.string.card_title_ended_busy;
		}
		changeStatus(UA_STATE_IDLE);
		
		if (call == call_transfer) 
		{
			StatusLine status_line = resp.getStatusLine();
			int code = status_line.getCode();
			// String reason=status_line.getReason();
			this.call.notify(code, reason);
			call_transfer = null;
		}
	}

	/** Callback function called when arriving a 3xx (call redirection) */
	public void onCallRedirection(Call call, String reason,
			Vector<String> contact_list, Message resp) {
		printLog("onCallRedirection()", LogLevel.LOW);
		if (call != this.call) 
		{
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("REDIRECTION (" + reason + ")", LogLevel.HIGH);
		call.call(((String) contact_list.elementAt(0)),null);
	}

	/**
	 * Callback function that may be overloaded (extended). Called when arriving
	 * a CANCEL request
	 */
	public void onCallCanceling(Call call, Message cancel) {
		printLog("onCallCanceling()", LogLevel.LOW);
		if (call != this.call) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("CANCEL", LogLevel.HIGH);
		changeStatus(UA_STATE_IDLE);
	}

	/** Callback function called when arriving a BYE request */
	public void onCallClosing(Call call, Message bye) {
		printLog("onCallClosing()", LogLevel.LOW);
		if (call != this.call && call != call_transfer) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}

		if (call != call_transfer && call_transfer != null) {
			printLog("CLOSE PREVIOUS CALL", LogLevel.HIGH);
			this.call = call_transfer;
			call_transfer = null;
			return;
		}
		// else
		printLog("CLOSE", LogLevel.HIGH);
		closeMediaApplication();
		changeStatus(UA_STATE_IDLE);
	}

	/**
	 * Callback function called when arriving a response after a BYE request
	 * (call closed)
	 */
	public void onCallClosed(Call call, Message resp) {
		printLog("onCallClosed()", LogLevel.LOW);
		if (call != this.call) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("CLOSE/OK", LogLevel.HIGH);
		
		changeStatus(UA_STATE_IDLE);
	}
	
	@Override
	public void onCallNoAnswer(Call call) {
		MyLog.call_i("UserAgent onCallNoAnswer");
		if (call != this.call) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		changeStatus(UA_STATE_IDLE);
		
	}
	
	//add by liuyang for vidie call switch start 
	/** Callback function called when arriving a SWITCH request */
	public void onCallSwitching(Call call, Message bye) {
		printLog("onCallSwitching()", LogLevel.LOW);
		if (call != this.call && call != call_transfer) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}

		if (call != call_transfer && call_transfer != null) {
			printLog("CLOSE PREVIOUS CALL", LogLevel.HIGH);
			this.call = call_transfer;
			call_transfer = null;
			return;
		}
		// else
		printLog("SWITCH", LogLevel.HIGH);
		changeStatus(UA_STATE_INCALL);
	}
	
	/** Callback function called arriving a response after a Switch request*/
	public void onCallSwitched(Call call, Message bye) {
		printLog("onCallSwitched()", LogLevel.LOW);
		if (call != this.call) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		// else
		printLog("SWITCH", LogLevel.HIGH);
		changeStatus(UA_STATE_INCALL);
	}

//add by liuyang for video call switch end

	/** Callback function called when the invite expires */
	public void onCallTimeout(Call call) {
		printLog("onCallTimeout()", LogLevel.LOW);
		MyLog.call_i("UserAgent onCallTimeout");
		if (call != this.call) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("NOT FOUND/TIMEOUT", LogLevel.HIGH);
		changeStatus(UA_STATE_IDLE);
		if (call == call_transfer) {
			int code = 408;
			String reason = "Request Timeout";
			this.call.notify(code, reason);
			call_transfer = null;
		}
	}

	// ****************** ExtendedCall callback functions ******************

	/**
	 * Callback function called when arriving a new REFER method (transfer
	 * request)
	 */
	public void onCallTransfer(ExtendedCall call, NameAddress refer_to,
			NameAddress refered_by, Message refer) {
		printLog("onCallTransfer()", LogLevel.LOW);
		if (call != this.call) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("Transfer to " + refer_to.toString(), LogLevel.HIGH);
		call.acceptTransfer();
		call_transfer = new ExtendedCall(sip_provider, user_profile.from_url,
				user_profile.contact_url, this);
		call_transfer.call(refer_to.toString(), local_session, null,null); 		// modified by mandrajg
	}

	/** Callback function called when a call transfer is accepted. */
	public void onCallTransferAccepted(ExtendedCall call, Message resp) {
		printLog("onCallTransferAccepted()", LogLevel.LOW);
		if (call != this.call) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("Transfer accepted", LogLevel.HIGH);
	}

	/** Callback function called when a call transfer is refused. */
	public void onCallTransferRefused(ExtendedCall call, String reason,
			Message resp) {
		printLog("onCallTransferRefused()", LogLevel.LOW);
		if (call != this.call) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("Transfer refused", LogLevel.HIGH);
	}

	/** Callback function called when a call transfer is successfully completed */
	public void onCallTransferSuccess(ExtendedCall call, Message notify) {
		printLog("onCallTransferSuccess()", LogLevel.LOW);
		if (call != this.call) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("Transfer successed", LogLevel.HIGH);
		call.hangup();
	}

	/**
	 * Callback function called when a call transfer is NOT sucessfully
	 * completed
	 */
	public void onCallTransferFailure(ExtendedCall call, String reason,
			Message notify) {
		printLog("onCallTransferFailure()", LogLevel.LOW);
		if (call != this.call) {
			printLog("NOT the current call", LogLevel.LOW);
			return;
		}
		printLog("Transfer failed", LogLevel.HIGH);
	}

	// ************************* Schedule events ***********************

	/** Schedules a re-inviting event after <i>delay_time</i> secs. */
	void reInvite(final String contact_url, final int delay_time) {
		SessionDescriptor sdp = new SessionDescriptor(local_session);
		sdp.IncrementOLine();
		final SessionDescriptor new_sdp;
		if (statusIs(UserAgent.UA_STATE_INCALL)) { // modified
			new_sdp = new SessionDescriptor(
					sdp.getOrigin(), sdp.getSessionName(), new ConnectionField(
							"IP4", "0.0.0.0"), new TimeField());
		} else {
			new_sdp = new SessionDescriptor(
					sdp.getOrigin(), sdp.getSessionName(), new ConnectionField(
							"IP4", IpAddress.localIpAddress), new TimeField());
		}
		new_sdp.addMediaDescriptors(sdp.getMediaDescriptors());
		local_session = sdp.toString();
		(new Thread() {
			public void run() {
				runReInvite(contact_url, new_sdp.toString(), delay_time);
			}
		}).start();
	}

	/** Re-invite. */
	private void runReInvite(String contact, String body, int delay_time) {
		try {
			if (delay_time > 0)
				Thread.sleep(delay_time * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
			printLog("RE-INVITING/MODIFYING");
			if (call != null && call.isOnCall()) {
				printLog("REFER/TRANSFER");
				call.modify(contact, body);
			}
	}

	/** Schedules a call-transfer event after <i>delay_time</i> secs. */
	void callTransfer(final String transfer_to, final int delay_time) {
		// in case of incomplete url (e.g. only 'user' is present), try to
		// complete it
		final String target_url;
		if (transfer_to.indexOf("@") < 0)
			target_url = transfer_to + "@" + realm; // modified
		else
			target_url = transfer_to;
		(new Thread() {
			public void run() {
				runCallTransfer(target_url, delay_time);
			}
		}).start();
	}

	/** Call-transfer. */
	private void runCallTransfer(String transfer_to, int delay_time) {
		try {
			if (delay_time > 0)
				Thread.sleep(delay_time * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
			if (call != null && call.isOnCall()) {
				printLog("REFER/TRANSFER");
				call.transfer(transfer_to);
			}
	}

	/** Schedules an automatic answer event after <i>delay_time</i> secs. */
	void automaticAccept(final int delay_time) {
		(new Thread() {
			public void run() {
				runAutomaticAccept(delay_time);
			}
		}).start();
	}

	/** Automatic answer. */
	private void runAutomaticAccept(int delay_time) {
		try {
			if (delay_time > 0)
				Thread.sleep(delay_time * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
			if (call != null) {
				printLog("AUTOMATIC-ANSWER");
				accept();
			}
	}

	/** Schedules an automatic hangup event after <i>delay_time</i> secs. */
	void automaticHangup(final int delay_time) {
		(new Thread() {
			public void run() {
				runAutomaticHangup(delay_time);
			}
		}).start();
	}

	/** Automatic hangup. */
	private void runAutomaticHangup(int delay_time) {
		try {
			if (delay_time > 0)
				Thread.sleep(delay_time * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
			if (call != null && call.isOnCall()) {
				printLog("AUTOMATIC-HANGUP");
				hangup();
			}

	}

	// ****************************** Logs *****************************

	/** Adds a new string to the default Log */
	void printLog(String str) {
		printLog(str, LogLevel.HIGH);
	}

	/** Adds a new string to the default Log */
	void printLog(String str, int level) {
		if (Sipdroid.release) return;
		if (log != null)
			log.println("UA: " + str, level + SipStack.LOG_LEVEL_UA);
		if ((user_profile == null || !user_profile.no_prompt)
				&& level <= LogLevel.HIGH)
			System.out.println("UA: " + str);
	}

	/** Adds the Exception message to the default Log */
	void printException(Exception e, int level) {
		if (Sipdroid.release) return;
		if (log != null)
			log.printException(e, level + SipStack.LOG_LEVEL_UA);
	}

	
	public void sendSMSdata(int commandNo, String targetIP, Object addData){
		// 构造发送协议数据
        IPMSGProtocol ipmsgProtocol = null;
        if (addData == null) {
            ipmsgProtocol = new IPMSGProtocol(SessionUtils.getIMEI(), commandNo);
        }
        else if (addData instanceof Entity) {
            ipmsgProtocol = new IPMSGProtocol(SessionUtils.getIMEI(), commandNo, (Entity) addData);
        }
        else if (addData instanceof String) {
            ipmsgProtocol = new IPMSGProtocol(SessionUtils.getIMEI(), commandNo, (String) addData);
        }
        
        sms.sendSMSdata(ipmsgProtocol, targetIP);
	}
	
	public void sendMultiSMSdata(int commandNo, String targetIP, Object addData){
		IPMSGProtocol ipmsgProtocol = null;
        if (addData == null) {
            ipmsgProtocol = new IPMSGProtocol(SessionUtils.getIMEI(), commandNo, targetIP);
        }
        else if (addData instanceof Entity) {
            ipmsgProtocol = new IPMSGProtocol(SessionUtils.getIMEI(), commandNo, (Entity) addData);
        }
        else if (addData instanceof String) {
            ipmsgProtocol = new IPMSGProtocol(SessionUtils.getIMEI(), commandNo, (String) addData);
        }
        sms.sendMultiSMSdata(ipmsgProtocol, targetIP);
	}
	
	public void sendMultiSMSdata(int commandNo, String targetIP, Object addData, String filePath){
		IPMSGProtocol ipmsgProtocol = null;
        ipmsgProtocol = new IPMSGProtocol(SessionUtils.getIMEI(), commandNo, (Entity) addData, filePath);
        sms.sendMultiSMSdata(ipmsgProtocol, targetIP);
	}
	
	public void stopMultiProvider(String groupIp){
		sms.stopMultiProvider(groupIp);
	}

	public void listenSMS(){
		sms = new Sms(sip_provider, this);
		sms.listen();
	}
	
	private void processIpmsg(IPMSGProtocol ipmsgProtocol, String senderIP){
		com.leadcore.sms.entity.Message msg = (com.leadcore.sms.entity.Message) ipmsgProtocol.getAddObject();
		msg.setSendTime(DateUtils.getDisplayTime());
		
		switch (msg.getContentType()) {
		case TEXT:
			break;
		case IMAGE:
			msg.setMsgContent(BaseApplication.IMAG_PATH + File.separator
                    + msg.getSenderIMEI() + File.separator + msg.getMsgContent());
			break;
		case VOICE:
            msg.setMsgContent(BaseApplication.VOICE_PATH + File.separator
                    + msg.getSenderIMEI() + File.separator + msg.getMsgContent());
			break;
		case FILE:
			msg.setMsgContent(BaseApplication.FILE_PATH+File.separator
					+msg.getSenderIMEI() + File.separator + msg.getMsgContent());
			startTCPService(BaseApplication.FILE_PATH);
//			sendSMSdata(IPMSGConst.IPMSG_RECIEVE_FILE_DATA, senderIP, msg);
			break;
		default:
			break;
		}
		
		Receiver.showNotification(Receiver.SMS_NOTIFICATION, msg);
		
		long id = mDBOperate.addChattingInfo(msg.getSenderIMEI(), SessionUtils.getIMEI(),
				msg.getSendTime(), msg.getMsgContent(), msg.getContentType(),(int)msg.getRecordTime());
		//如果是文件，需要将文件在数据库中的ID发送给对端
        if (msg.getContentType() == CONTENT_TYPE.FILE) {
        	msg.setmReservedID(id);
        	sendSMSdata(IPMSGConst.IPMSG_RECIEVE_FILE_DATA, senderIP, msg);
		}
		//添加入最新消息列表 lss
			mDBOperate.queryNewChattingInfo(msg.getSenderIMEI(), SessionUtils.getIMEI(),"", DateUtils.getMessageListTime(), 
					msg.getMsgContent(), msg.getContentType(), (int)msg.getRecordTime());
		//发送广播通知TabMessage页面刷新List
		Receiver.mContext.sendBroadcast(new Intent(ACTION_MESSAGELIST_REFRESH));
        //end
		
        msg.setID(id);
		if (!(BaseActivity.isExistActiveActivity(msg))) {
			BaseActivity.mApplication.addUnReadPeople(BaseActivity.mApplication.getOnlineUser(msg.getSenderIMEI()));
		}
		BaseActivity.mApplication.addLastMsgCache(msg.getSenderIMEI(), msg);
		BaseActivity.sendEmptyMessage(IPMSGConst.IPMSG_SENDMSG);
	}
	@Override
	public void onReceivedSMSdata(Sms sms, IPMSGProtocol ipmsgProtocol,
			String senderIP) {
		MyLog.e("UserAgent", "Useragent " + ipmsgProtocol.toString() + "\nsenderIP " + senderIP);
		int commandNo = ipmsgProtocol.getCommandNo();
		switch (commandNo) {
		case IPMSGConst.IPMSG_SENDMSG:
		{
			if (pwl1 == null) {
				pm = (PowerManager) Receiver.mContext.getSystemService(Context.POWER_SERVICE);
				pwl1 = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
						PowerManager.ACQUIRE_CAUSES_WAKEUP, "Sipdroid.UserAgent.onReceivedSMSdata");
			}
			pwl1.acquire();
			processIpmsg(ipmsgProtocol, senderIP);
			pwl1.release();
		}
			break;
			
		case IPMSGConst.IPMSG_SEND_IMAGE_DATA:
			startTCPService(BaseApplication.IMAG_PATH);
			sendSMSdata(IPMSGConst.IPMSG_RECEIVE_IMAGE_DATA, senderIP, null);
			break;
			
		case IPMSGConst.IPMSG_SEND_VOICE_DATA:
			startTCPService(BaseApplication.VOICE_PATH);
			sendSMSdata(IPMSGConst.IPMSG_RECIEVE_VOICE_DATA, senderIP, null);
			break;
		case IPMSGConst.IPMSG_RECIEVE_FILE_DATA:
			android.os.Message msg = new android.os.Message();
			msg.what = IPMSGConst.IPMSG_RECIEVE_FILE_DATA;
			msg.obj = ipmsgProtocol.getAddObject();
			BaseActivity.sendMessage(msg);
			break;
		case IPMSGConst.IPMSG_NEW_GROUP:
		case IPMSGConst.IPMSG_GROUP_ADD_MEM:
			//if sender is myselef, nothing to do
			if (senderIP.equals(SessionUtils.getLocalIPaddress())) {
				return;
			}
			MyLog.i("jesse", "IPMSG_GROUP_ADD_MEM");
			Group group = (Group) ipmsgProtocol.getAddObject();
			mDBOperate.addGroupInfo(group);
			List<Users> members = group.getMembers();
			for (Users member : members) {
				MyLog.i("group", "UserAgent_IPMSG_GROUP_ADD_MEM member.ip: " + member.getIpaddress());
				mDBOperate.addMemberInfo(member);
				//更新在线成员所携带的群组列表信息      modify by zyf
				Users onlineMember = BaseActivity.mApplication.getOnlineUser(member.getIMEI());
				if (null != onlineMember) {
					onlineMember.getmGroupIps().add(group.getStrIP());
					onlineMember.getmGroups().add(group);
				}
			}
			BaseActivity.mApplication.addOnlineGroup(group);
			Receiver.mContext.sendBroadcast(new Intent(AdhocManager.ACTION_GROUP_REFRESH));
			break;
		case IPMSGConst.IPMSG_GROUP_DEL_MEM:
			MyLog.i("group", "UserAgent IPMSG_GROUP_DEL_MEM");
			BaseActivity.mApplication.removeOnlineGroup(ipmsgProtocol.getAddStr());
			mDBOperate.delGroupInfo(ipmsgProtocol.getAddStr());
			mDBOperate.delGroupMemInfo(ipmsgProtocol.getAddStr());
			mDBOperate.deteleChatMessageGroupInfo(ipmsgProtocol.getAddStr());//add by lss for delete group message when user quit or delete group
			sendMultiSMSdata(IPMSGConst.IPMSG_GROUP_QUIT, ipmsgProtocol.getAddStr(),null);
			stopMultiProvider(ipmsgProtocol.getAddStr());
			Receiver.mContext.sendBroadcast(new Intent(AdhocManager.ACTION_GROUP_REFRESH));
			break;
		default:
			BaseActivity.sendEmptyMessage(commandNo);
			break;
		}
	}
	
	private void processMultiMsg(com.leadcore.sms.entity.Message msg){
		
		switch (msg.getContentType()) {
		case TEXT:
			
			break;
		case IMAGE:
			String imagePath = msg.getMsgContent();
			String imageName = imagePath.substring(imagePath.lastIndexOf(File.separator)+1);
			msg.setMsgContent(BaseApplication.IMAG_PATH + File.separator
					+ msg.getReceiveIP() + File.separator + imageName);
			onImageReceived(msg.getMsgContent());   						//create the thumbnail

			break;
		case VOICE:
			String voicePath = msg.getMsgContent();
			String voiceName = voicePath.substring(voicePath.lastIndexOf(File.separator)+1);
			msg.setMsgContent(BaseApplication.VOICE_PATH + File.separator
					+ msg.getReceiveIP() + File.separator + voiceName);
			break;
		default:
			break;
		}
		Receiver.showNotification(Receiver.SMS_GROUP_NOTIFICATION, msg);
		long id = mDBOperate.addGroupChattingInfo(msg.getSenderIMEI(),
				msg.getSenderName(), msg.getReceiveIP(), msg.getSendTime(),
				msg.getMsgContent(), msg.getContentType(),msg.getRecordTime());
		//add by lss for adding group message in message list
		mDBOperate.queryNewChattingInfo(msg.getSenderIMEI(), msg.getSenderName(), msg.getReceiveIP()
				, DateUtils.getMessageListTime(), msg.getMsgContent(), msg.getContentType(), (int)msg.getRecordTime());
		//add end
		//发送广播通知TabMessage页面刷新List
		Receiver.mContext.sendBroadcast(new Intent(ACTION_MESSAGELIST_REFRESH));
		//end
		
		if (!(BaseActivity.isExistActiveActivity(msg))) {
			BaseActivity.mApplication.addUnReadGroups(BaseActivity.mApplication
					.getOnlineGroup(msg.getReceiveIP()));
		}
		BaseActivity.mApplication.addLastMsgCache(msg.getReceiveIP(), msg);
		BaseActivity.sendEmptyMessage(IPMSGConst.IPMSG_SENDMSG);
	}
	
	/**when received a Muti SMS data****/
	@Override
	public void onReceivedMultiSMSdata(Sms sms, IPMSGProtocol ipmsgProtocol,
			String senderIP) {
		int commond = ipmsgProtocol.getCommandNo();
		if (commond == IPMSGConst.IPMSG_GROUP_LAUNCH_PTT) {
			//if the msg is launch ptt session
			MyLog.i("Test", "IPMSG_GROUP_LAUNCH_PTT senderIP:" + senderIP );
			onPttCallComing(ipmsgProtocol.getAddStr(), senderIP);
			return;
		}else if (commond == IPMSGConst.IPMSG_GROUP_DISMISS_PTT) {
			MyLog.i("Test", "IPMSG_GROUP_DISMISS_PTT");
			closePttSession();
			return;
		}
		//if sender is myselef, nothing to do
		if (senderIP.equals(SessionUtils.getLocalIPaddress())) {
			return;
		}
		
		com.leadcore.sms.entity.Message msg = (com.leadcore.sms.entity.Message) ipmsgProtocol
				.getAddObject();
		if (null != msg) {
			MyLog.i("group", "msg.content: " + msg.getMsgContent() + " senderIP: "
					+ senderIP);
			msg.setSendTime(DateUtils.getDisplayTime());
		}
		switch (commond) {
		case IPMSGConst.IPMSG_GROUP_SENDMSG:
		{
			if (pwl2 == null) {
				pm = (PowerManager) Receiver.mContext.getSystemService(Context.POWER_SERVICE);
				pwl2 = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
						PowerManager.ACQUIRE_CAUSES_WAKEUP, "Sipdroid.UserAgent.onReceivedMultiSMSdata");
			}
			pwl2.acquire();
			processMultiMsg(msg);
			pwl2.release();
			
		}
			break;
		case IPMSGConst.IPMSG_GROUP_SEND_VOICE:
			MyLog.i("groupFile", "onReceivedMultiSMSdata start to receive voice");
			TcpFileClient.getInstance(Receiver.mContext).receiveFile(BaseApplication.VOICE_PATH, senderIP, msg);
			break;
		case IPMSGConst.IPMSG_GROUP_SEND_IMAGE:
			MyLog.i("groupFile", "onReceivedMultiSMSdata start to receive image");
			TcpFileClient.getInstance(Receiver.mContext).receiveFile(BaseApplication.IMAG_PATH, senderIP, msg);
			break;
		case IPMSGConst.IPMSG_GROUP_QUIT:
			MyLog.i("zhaoyf", "UserAgent onReceivedMultiSMSdata IPMSG_GROUP_QUIT start");
			mDBOperate.delMemberInfo(ipmsgProtocol.getSenderIMEI(), ipmsgProtocol.getAddStr());
			MyLog.i("zhaoyf", "UserAgent onReceivedMultiSMSdata IPMSG_GROUP_QUIT end");
			break;
		case IPMSGConst.IPMSG_GROUP_DISMISS:
			MyLog.i("GM", "UserAgent onReceivedMultiSMSdata IPMSG_GROUP_DISMISS");
			BaseActivity.mApplication.removeOnlineGroup(ipmsgProtocol.getAddStr());
			Receiver.mContext.sendBroadcast(new Intent(AdhocManager.ACTION_GROUP_REFRESH));
			mDBOperate.delGroupInfo(ipmsgProtocol.getAddStr());
			mDBOperate.delGroupMemInfo(ipmsgProtocol.getAddStr());
			mDBOperate.deleteGroupChattingInfo(ipmsgProtocol.getAddStr());
			mDBOperate.deteleChatMessageGroupInfo(ipmsgProtocol.getAddStr());//add by lss for delete group message when user quit or delete group
			stopMultiProvider(ipmsgProtocol.getAddStr());
			break;
		default:
			break;
		}

	}
	
    public void startTCPService(String fileSavePath){
    	tcpService = TcpService.getInstance(Receiver.mContext);
        tcpService.setSavePath(fileSavePath);
        tcpService.setListener(this);
        tcpService.startReceive();
    }
    
    public void startTCPClient(String path, String ipAddress, com.leadcore.sms.entity.Message.CONTENT_TYPE  type, int recordTime){
    	tcpClient = TcpClient.getInstance(Receiver.mContext);
    	tcpClient.setListener(this);
//		tcpClient.startSend();
		if (FileUtils.isFileExists(path))
		tcpClient.sendFile(path, ipAddress, type, recordTime);
    }
    
    public void startTCPClient(String path, String ipAddress, com.leadcore.sms.entity.Message.CONTENT_TYPE  type,
    		long localId, long remoteId){
    	tcpClient = TcpClient.getInstance(Receiver.mContext);
    	tcpClient.setListener(this);
//		tcpClient.startSend();
		if (FileUtils.isFileExists(path))
		tcpClient.sendFile(path, ipAddress, type, localId, remoteId);
    }

    /**
     * 添加用户到在线列表中 (线程安全的)
     * 
     * @param 
     */
    public synchronized void addUser(Users user) {
        mDBOperate.addUserInfo(user);
    }
    
    public UserInfo getUserInfo(String imei){
    	return mDBOperate.getUserInfoByIMEI(imei);
    }
    
    /**
     * 删除组员
     */
    public synchronized void delMemInfo(String IMEI, String groupIp){
    	mDBOperate.delMemberInfo(IMEI, groupIp);
    }
    /**
     * 添加组员
     */
    public synchronized void addMemberInfo(Users user){
    	mDBOperate.addMemberInfo(user);
    }
    /**
     * 获取组员
     * @return
     */
    public synchronized Users getMemberInfo(String IMEI, String groupIP){
    	return mDBOperate.getMemberInfo(IMEI, groupIP);
    }
    /**
     * 获取群主是本机的群组集合
     * @return
     */
    public List<Group> getMyGroups(){
    	ArrayList<Group> myGroups = BaseActivity.mApplication.getMyGroups();
    	for (Group group : myGroups) {
			List<Users> memList =  mDBOperate.getMembersByGroupIP(group.getStrIP());
			group.setMembers(memList);
		}
    	return myGroups;
    }
  //add by  lss for processing the solution of group equals null
    public Group getGroup(String groupIp){
    	int groupId=mDBOperate.getGroupIDByIP(groupIp);
    	Group  group=mDBOperate.getGroupById(groupId);
    	return group;
    }
    //add end

    public void closeDB(){
    	if (null != mDBOperate) {// 关闭数据库连接
			mDBOperate.close();
			mDBOperate = null;
		}
    }
    

	@Override
	public void onImageReceived(String imageSavePath) {
		if (null != imageSavePath) {
			String senderIMEI = FileUtils.getNameByPath(FileUtils.getPathByFullPath(imageSavePath));
			MyLog.e("UserAgent", "onImageReceived senderIMEI >>> " + senderIMEI);
//			ImageUtils.createThumbnail(Receiver.mContext, imageSavePath, BaseApplication.THUMBNAIL_PATH 
//					+ File.separator + senderIMEI + File.separator);
			BaseActivity.sendEmptyMessage(IPMSGConst.IPMSG_REFRESH);
		}
		
	}

	@Override
	public void onFileReceive(FileState fileState) {
		android.os.Message msg = new android.os.Message();
		msg.obj = fileState;
		msg.what = IPMSGConst.IPMSG_RECEIVE_FILE;
		if (ActivityCollectorUtils.getLastActivity() != null) {
			ActivityCollectorUtils.getLastActivity().processMessage(msg);
		}
		MyLog.i(TAG, "onFileReceive >>> " + fileState.toString());
	}

	@Override
	public void onFileReceiveSuccess(FileState fileState) {
		android.os.Message msg = new android.os.Message();
		msg.obj = fileState;
		msg.what = IPMSGConst.IPMSG_RECEIVE_FILE;
		if (ActivityCollectorUtils.getLastActivity() != null) {
			ActivityCollectorUtils.getLastActivity().processMessage(msg);
		}
		ChattingInfo chattingInfo = new ChattingInfo();
		chattingInfo.setID(fileState.id);
		chattingInfo.setPercent(100);
		if (mDBOperate != null) {
			mDBOperate.updateChattingInfo(chattingInfo);
		}
		MyLog.i(TAG, "onFileReceiveSuccess >>> " + fileState.toString());
	}
	
	@Override
	public void onFileReceiveFailed(FileState fileState) {
		fileState.percent = -1;
		android.os.Message msg = new android.os.Message();
		msg.obj = fileState;
		msg.what = IPMSGConst.IPMSG_RECEIVE_FILE;
		if (ActivityCollectorUtils.getLastActivity() != null) {
			ActivityCollectorUtils.getLastActivity().processMessage(msg);
		}
		ChattingInfo chattingInfo = new ChattingInfo();
		chattingInfo.setID(fileState.id);
		chattingInfo.setPercent(-1);
		if (mDBOperate != null) {
			mDBOperate.updateChattingInfo(chattingInfo);
		}
		MyLog.i(TAG, "onFileReceiveFailed >>> " + fileState.toString());
		
	}
	
	@Override
	public void onFileSend(FileState fileState) {
		// TODO Auto-generated method stub
		android.os.Message msg = new android.os.Message();
		msg.obj = fileState;
		msg.what = IPMSGConst.IPMSG_SEND_FILE;
		if (ActivityCollectorUtils.getLastActivity() != null) {
			ActivityCollectorUtils.getLastActivity().processMessage(msg);
		}
		MyLog.i(TAG, "onFileSend >>> " + fileState.toString());
	}

	@Override
	public void onFileSendSuccess(FileState fileState) {
		android.os.Message msg = new android.os.Message();
		msg.obj = fileState;
		msg.what = IPMSGConst.IPMSG_SEND_FILE;
		if (ActivityCollectorUtils.getLastActivity() != null) {
			ActivityCollectorUtils.getLastActivity().processMessage(msg);
		}
		ChattingInfo chattingInfo = new ChattingInfo();
		chattingInfo.setID(fileState.id);
		chattingInfo.setPercent(100);
		if (mDBOperate != null) {
			mDBOperate.updateChattingInfo(chattingInfo);
		}
		MyLog.i(TAG, "onFileSendSuccess >>> " + fileState.toString());
	}
	
	@Override
	public void onFileSendFailed(FileState fileState) {
		fileState.percent = -1;
		android.os.Message msg = new android.os.Message();
		msg.obj = fileState;
		msg.what = IPMSGConst.IPMSG_SEND_FILE;
		if (ActivityCollectorUtils.getLastActivity() != null) {
			ActivityCollectorUtils.getLastActivity().processMessage(msg);
		}
		ChattingInfo chattingInfo = new ChattingInfo();
		chattingInfo.setID(fileState.id);
		chattingInfo.setPercent(-1);
		if (mDBOperate != null) {
			mDBOperate.updateChattingInfo(chattingInfo);
		}
		MyLog.i(TAG, "onFileSendFailed >>> " + fileState.toString());
		
	}

	public void delGroupDatabase(){
		mDBOperate.delGroups();
	}

	/**
	 * Create Group
	 * @param groupIP
	 * @param members
	 * @param listener
	 */
	public void requestGroupInfoCfg(String groupIP, List<String> members, IGroupInfoCfgRspListener.Stub listener){
		adhocManager.requestGroupInfoCfg(groupIP, members, listener);
	}
	
	public class MyLinkListener extends ILinkListener.Stub{

		@Override
		public void onLinkDeleteRsp(boolean isSuccessful, int servId, int servType)
				throws RemoteException {
			MyLog.i(TAG, "zhaoyifei_call onLinkDeleteRsp isSuccessful: " + isSuccessful);
		}

		@Override
		public void onLinkSetupRsp(boolean isSuccessful, int servId, int servType)
				throws RemoteException {
//			linkServType = servType;
			isCreateLinkSuccess = isSuccessful;
			synchronized (linkLock) {
				linkLock.notifyAll();
			}
			MyLog.i(TAG, "zhaoyifei_call onLinkSetupRsp isSuccessful: " + isSuccessful
					+" servId: "+servId + " servType: " + servType);
		}

		@Override
		public void onLinkStatusInd(int status, int servId, int servType)
				throws RemoteException {
			MyLog.i(TAG, "zhaoyifei_call onLinkStatusInd status: " + status);
			adhocManager.linkStatusConfirm(servType, status, servId);
		}
	}
	
	
	//****************************  PTT start  ************************************
	
	/** Launches the Ptt Application (currently, the RAT audio tool) */
	protected void launchPttApplication(String sdp) {
		// exit if the Ptt Application is already running
		if (ptt_app != null) {
			MyLog.i("ptt", "ptt_app is already running");
			return;
		}
		Codecs.Map c;
		// parse local sdp
		SessionDescriptor local_sdp = new SessionDescriptor(sdp);
		int local_audio_port = 0;
		local_video_port = 0;
		int dtmf_pt = 0;
		c = Codecs.getCodec(local_sdp);
		if (c == null) {
			Receiver.call_end_reason = R.string.card_title_ended_no_codec;
			hangup();
			return;
		}
		MyLog.i(TAG, "launchMediaApplication c: " + c.toString());
		MediaDescriptor m = local_sdp.getMediaDescriptor("video");
		if ( m != null)
			local_video_port = m.getMedia().getPort();
		m = local_sdp.getMediaDescriptor("audio");
		if (m != null) {
			local_audio_port = m.getMedia().getPort();
			if (m.getMedia().getFormatList().contains(String.valueOf(user_profile.dtmf_avp)))
				dtmf_pt = user_profile.dtmf_avp;
		}

		// select the media direction (send_only, recv_ony, fullduplex)
		int dir = 0;
		if (user_profile.recv_only)
			dir = -1;
		else if (user_profile.send_only)
			dir = 1;

		if (user_profile.audio && local_audio_port != 0) { // create an audio_app and start
												// it
			if (ptt_app == null) { // for testing..
				String audio_in = null;
				if (user_profile.send_tone) {
					audio_in = JAudioLauncher.TONE;
				} else if (user_profile.send_file != null) {
					audio_in = user_profile.send_file;
				}
				String audio_out = null;
				if (user_profile.recv_file != null) {
					audio_out = user_profile.recv_file;
				}
				MyLog.i("ptt", "JAudioLauncher local_audio_port:" + local_audio_port
						+ "\nremote_media_address:" + remote_ptt_media_address
						+ "\nremote_audio_port:" + local_audio_port
						+ "\ndir:" + dir
						+ "\naudio_in:" + audio_in
						+ "\naudio_out:" + audio_out
						+ "\nsample_rate:" + c.codec.samp_rate()
						+ "\nsample_size:" + user_profile.audio_sample_size
						+ "\nframe_size:" + c.codec.frame_size()
						+ "\npayload_type:" + c.toString()
						+ "\ndtmf_pt:" + dtmf_pt);
				ptt_app = new PttAudioLauncher(local_audio_port, 
						remote_ptt_media_address, local_audio_port, dir, audio_in, 
						audio_out, c.codec.samp_rate(), 
						user_profile.audio_sample_size, 
						c.codec.frame_size(), c, dtmf_pt);
			}
			ptt_app.startMedia();
		}
	}
	
	/** Close the Ptt Application */
	protected void closePttApplication() {
		if (ptt_app != null) {
			ptt_app.stopMedia();
			ptt_app = null;
		}
	}
	
	/** pause the thread of sending ptt packags*/
	public boolean pausePttSend(){
		if (ptt_app != null) {
			return ptt_app.pausePttSend();
		}
		return false;
	}
	
	/** pause the thread of receiving ptt packags*/
	public boolean pausePttReceive(){
		if (ptt_app != null) {
			return ptt_app.pausePttRece();
		}
		return false;
	}
	
	public boolean stopPlayPtt(){
		if (ptt_app != null) {
			return ptt_app.stopPlay();
		}
		return false;
	}

	public int speakerPttApplication(int mode) {
		int old;
		if (ptt_app != null)
			return ptt_app.speakerMedia(mode);
		old = PttRtpStreamReceiver.speakermode;
		PttRtpStreamReceiver.speakermode = mode;
		return old;
	}
	
	protected synchronized void changePttStatus(int status){
		ptt_state = status;
		Receiver.onPttState(status,null);
	}
	
	/** launch a ptt session(acting as UAC). */
	public boolean launchPttSession(String targetIp) {
		
		if (Receiver.ptt_state != PTT_STATE_OFF)
		{
			MyLog.i("Test", "fail to make ptt call ,because ptt is " + Receiver.ptt_state);
			return false;
		}
		remote_ptt_media_address = targetIp;
		//change start multi codecs
		createOffer();
		//change end
		String msg = targetIp + ";" + local_session;
		stopSend = false;
		new Thread(new sendLaunchPttMsg(targetIp, msg)).start();
		MyLog.i("Test", "sendLaunchPttMsg start ,ptt is " + Receiver.ptt_state);
		return true;
	}
	
	public boolean pttSpeak(){
		if (Receiver.ptt_state != PTT_STATE_OCCUPY ) {
//			pausePttReceive();
			pausePttSend();
//			if (Receiver.ptt_state == PTT_STATE_SPEAKING) {
//				changePttStatus(PTT_STATE_FREE);
//			}else {
//				changePttStatus(PTT_STATE_SPEAKING);
//			}
			return true;
		}
		return false;
	}
	
	public void closePttSession(){
		if (Receiver.ptt_state == PTT_STATE_OFF) {
			return;
		}
		if (pwl3 != null && pwl3.isHeld()) {
			pwl3.release();
			pwl3 = null;
		}
		stopCheckPtt = true;
		changePttStatus(PTT_STATE_OFF);
		closePttApplication();
	}
	
	public void dismissPttSession(String targetIp){
		stopSend = true;
		if (targetIp != null) {
			sendMultiSMSdata(IPMSGConst.IPMSG_GROUP_DISMISS_PTT, targetIp, null);
		}
		closePttSession();
	}
	
	private void onPttCallComing(String msg, String senderIp){
		lastPttComingTime = System.currentTimeMillis();
		if (Receiver.ptt_state != PTT_STATE_OFF) {
			return;
		}
		//modify by zyf
		if (pwl3 == null) {
			pm = (PowerManager) Receiver.mContext.getSystemService(Context.POWER_SERVICE);
			pwl3 = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | 
					PowerManager.ACQUIRE_CAUSES_WAKEUP, "Sipdroid.UserAgent.onPttCallComing");
		}
		pwl3.acquire();
		
		if (!senderIp.equals(SessionUtils.getLocalIPaddress())) {
			stopCheckPtt = false;
			new Thread(new checkPttSession()).start();
		}
		int index = msg.indexOf(";", 0);
		remote_ptt_media_address = msg.substring(0, index);
		Receiver.ptt_group_ip = remote_ptt_media_address;
		Receiver.ptt_launcher = senderIp;
		String remote_sdp = msg.substring(index + 1);
		changePttStatus(PTT_STATE_ON);
		MyLog.i("Test", "launchPttApplication");
		launchPttApplication(remote_sdp);
		pausePttSend();
		changePttStatus(PTT_STATE_FREE);
//		speakerPttApplication(AudioManager.MODE_NORMAL);
	}
	
	private boolean stopSend = false;
	private boolean stopCheckPtt =false;
	private long lastPttComingTime = 0L;
	private class sendLaunchPttMsg implements Runnable{
		private String targetIp;
		private String msg;
		
		public sendLaunchPttMsg(String targetIp, String msg) {
			super();
			this.targetIp = targetIp;
			this.msg = msg;
		}

		@Override
		public void run() { 
			while(true){
				if (stopSend) {
					return;
				}
				MyLog.i("Test", "sendMultiSMSdata   IPMSG_GROUP_LAUNCH_PTT");
				sendMultiSMSdata(IPMSGConst.IPMSG_GROUP_LAUNCH_PTT, targetIp, msg);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	private class checkPttSession implements Runnable{

		@Override
		public void run() {
			while(true){
				if (stopCheckPtt) {
					return;
				}
				if (System.currentTimeMillis() - lastPttComingTime > 30000
						&& Receiver.ptt_state != PTT_STATE_OFF) {
					closePttSession();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	//******************************  PTT end  *****************************************



}
