/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
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
package org.sipdroid.media;

import org.sipdroid.codecs.Codecs;
import org.sipdroid.net.SipdroidMultiSocket;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.ui.Sipdroid;
import org.sipdroid.sipua.utils.MyLog;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Log;
import org.zoolu.tools.LogLevel;

import android.preference.PreferenceManager;

/**
 * Ptt Audio Launder based on javax.sound
 * 
 * @author zhaoyifei
 * 
 */
public class PttAudioLauncher extends MediaLauncher implements PttMediaLogic {
   /** Event logger. */
   Log log=null;

	/** Sample rate [bytes] */
	int sample_rate = 8000;
	/** Sample size [bytes] */
	int sample_size = 1;
	/** Frame size [bytes] */
	int frame_size = 160;
	/** Frame rate [frames per second] */
	int frame_rate = 50; // =sample_rate/(frame_size/sample_size);
   boolean signed=false; 
   boolean big_endian=false;
   //String filename="audio.wav"; 

   /** Test tone */
   public static final String TONE="TONE";

   /** Test tone frequency [Hz] */
   public static int tone_freq=100;
   /** Test tone ampliture (from 0.0 to 1.0) */
   public static double tone_amp=1.0;

   /** Runtime media process */
   Process media_process=null;
	int dir; // duplex= 0, recv-only= -1, send-only= +1;

	SipdroidMultiSocket multiSocket = null; // modify by zyf
	PttRtpStreamSender sender = null;
	PttRtpStreamReceiver receiver = null;

	// change DTMF
	boolean useDTMF = false; // zero means not use outband DTMF

	/** Costructs the audio launcher */
	public PttAudioLauncher(PttRtpStreamSender sender, PttRtpStreamReceiver receiver) {
		this.sender = sender;
		this.receiver = receiver;
	}

	/** Costructs the audio launcher */
	public PttAudioLauncher(int local_port, String remote_addr,
			int remote_port, int direction, String audiofile_in,
			String audiofile_out, int sample_rate, int sample_size,
			int frame_size, Codecs.Map payload_type, int dtmf_pt) {
		frame_rate = sample_rate / frame_size;
		useDTMF = (dtmf_pt != 0);
		try {
			CallRecorder call_recorder = null;
			if (PreferenceManager
					.getDefaultSharedPreferences(Receiver.mContext).getBoolean(
							org.sipdroid.sipua.ui.Settings.PREF_CALLRECORD,
							org.sipdroid.sipua.ui.Settings.DEFAULT_CALLRECORD))
				call_recorder = new CallRecorder(null,
						payload_type.codec.samp_rate()); // Autogenerate
															// filename from
															// date.
			multiSocket = new SipdroidMultiSocket(local_port);
			dir = direction;
			// sender
			if (dir >= 0) {
				MyLog.i("ptt", "new audio sender to " + remote_addr + ":"
						+ remote_port);
				// audio_input=new AudioInput();
				sender = new PttRtpStreamSender(true, payload_type, frame_rate,
						frame_size, multiSocket, remote_addr, remote_port,
						call_recorder);
				sender.setSyncAdj(2);
				sender.setDTMFpayloadType(dtmf_pt);
			}

			// receiver
			if (dir <= 0) {
				MyLog.i("ptt", "new audio receiver on " + local_port);
				receiver = new PttRtpStreamReceiver(multiSocket, payload_type,
						call_recorder, remote_addr);
			}
		} catch (Exception e) {
			MyLog.e("ptt", "Costructs PttAudioLauncher", e);
		}
	}

	@Override
	public boolean startMedia() {
		MyLog.i("ptt", "start ptt audio....");
		if (sender != null) {
			MyLog.i("ptt", "start sending");
			sender.start();
		}
		if (receiver != null) {
			MyLog.i("ptt", "start receiving");
			receiver.start();
		}

		return true;
	}
	
	@Override
	public boolean stopMedia() {
		if (sender != null) {
			sender.halt();
			sender = null;
			MyLog.i("ptt", "sender halt");
		}
		if (receiver != null) {
			receiver.halt();
			receiver = null;
			MyLog.i("ptt", "receiver halted");
		}
		if (multiSocket != null)
			multiSocket.close();
		return true;
	}

	@Override
	public boolean muteMedia() {
		if (sender != null)
			return sender.mute();
		return false;
	}

	@Override
	public int speakerMedia(int mode) {
		if (receiver != null)
			return receiver.speaker(mode);
		return 0;
	}

   public void bluetoothMedia()
   {
//	   if (receiver != null)
//		   receiver.bluetooth();
   }

	@Override
	public boolean sendDTMF(char c) {
		if (!useDTMF)
			return false;
		sender.sendDTMF(c);
		return true;
	}

	@Override
	public boolean pausePttSend() {
		if (sender != null) {
			return sender.mute();
		}
		return false;
	}

	@Override
	public boolean pausePttRece() {
		if (receiver != null) {
			return receiver.pauseReceive();
		}
		return false;
	}

	public boolean stopPlay(){
		if (receiver != null) {
			return receiver.stopPlay();
		}
		return false;
	}

}
