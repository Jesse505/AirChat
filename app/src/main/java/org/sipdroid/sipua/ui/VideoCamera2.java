package org.sipdroid.sipua.ui;

/**
 * @author liuyang4 2016.11.21
 * 
 * VideoCall UI 
 * Use hardware encode
 * 
 */

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.sipdroid.media.LCVideoHardEncode;
import org.sipdroid.media.RtpStreamReceiver;
import org.sipdroid.media.RtpStreamSender;
import org.sipdroid.net.RtpPacket;
import org.sipdroid.net.RtpSocket;
import org.sipdroid.net.SipdroidSocket;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.phone.Call;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoCamera2 extends CallScreen implements 
	SipdroidListener, SurfaceHolder.Callback, OnClickListener {
	
	Thread t;
	Context mContext = this;

    private static final String TAG = "VideoCall";
    public static boolean start = false;
	private SurfaceHolder displayholder,pre_hold;
	private SurfaceView videoview,preview;
	private LCVideoHardEncode lcTest;
	private Chronometer mChronometer;
    private	int cameraID = 1;
	private static final int MAX_BUFFER_LENGHT = 1024*256;
    private boolean mMediaRecorderRecording = false;
    private boolean VideoRec = true;
    
    private final int ENABLE_INVERT = 100;
    
   
    private Button Bthuangup,Btswitch,Btinvert;
    
    private Handler mHandler = new Handler (){

		@Override
		public void dispatchMessage(Message msg) {
			// TODO Auto-generated method stub
			super.dispatchMessage(msg);
			
			switch (msg.what) {
			case ENABLE_INVERT:
				
				Btinvert.setClickable(true);
				break;

			default:
				break;
			}
		}
    	
    };


    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.d(TAG, "Video Camera enter!!!!");
        start = true;
        setContentView(R.layout.video_camera2);
        initview();
		setonlistener();
    }

	public void initview() {
		videoview = (SurfaceView)findViewById(R.id.sfv_video);
		preview = (SurfaceView) findViewById(R.id.pre_view);
		Bthuangup = (Button) findViewById(R.id.huangup);
		Btinvert = (Button) findViewById(R.id.InvertCamera);
		Btswitch = (Button) findViewById(R.id.switch_voice);
		mChronometer = (Chronometer) findViewById(R.id.chronometer1);
				
		displayholder = videoview.getHolder();
		pre_hold = preview.getHolder();
		
		preview.setZOrderOnTop(true);
	 	pre_hold.setFormat(PixelFormat.TRANSPARENT);
	 	pre_hold.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
	 
	
	}

	public void setonlistener() {
		Bthuangup.setOnClickListener(this);
		Btinvert.setOnClickListener(this);
		Btswitch.setOnClickListener(this);

		pre_hold.addCallback(this);
	}

	int speakermode;
	@Override
    public void onStart() {
        super.onStart();
        speakermode = Receiver.engine(this).speaker(AudioManager.MODE_NORMAL);
	}

	@Override
    public void onResume() {
		if (!Sipdroid.release) Log.i("SipUA:","on resume");

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        Receiver.engine(this).speaker(speakermode);
		VideoRec = false;
		if(socket!=null){
			socket.disconnect();
			socket.close();
			socket = null;
			Log.d(TAG,"close socket!!!!");
		}
		
		if(rtp_socket!=null){
		rtp_socket.close();
		rtp_socket = null;
		}
		finish();
    }

	/*
     * catch the back and call buttons to return to the in call activity.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
        	// finish for these events
            case KeyEvent.KEYCODE_CALL:
       			Receiver.engine(this).togglehold();            	
            case KeyEvent.KEYCODE_BACK:
            	finish();
            	return true;
                
            case KeyEvent.KEYCODE_CAMERA:
                // Disable the CAMERA button while in-call since it's too
                // easy to press accidentally.
            	return true;
            	
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            	RtpStreamReceiver.adjust(keyCode,true);
            	return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    private boolean initializeVideo() {
        Log.v(TAG, "initializeVideo");
        
      if (preview == null) {
            Log.v(TAG, "preview is null");
            return false;
        }
        try {
			lcTest.startCamera(cameraID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG,"startCamera failed!!!!!");
			//lcTest.restartCamera(cameraID);
			e.printStackTrace();
		}
        mMediaRecorderRecording = true;

        return true;
    }


        

    
    private void startVideoRecording() {
        Log.v(TAG, "startVideoRecording");

           if (Receiver.listener_video == null) {
    			Receiver.listener_video = this;   	
                RtpStreamSender.delay = 1;

                try {
					if (rtp_socket == null){
					rtp_socket = new RtpSocket(new SipdroidSocket(Receiver.engine(mContext).getLocalVideo()),
							InetAddress.getByName(Receiver.engine(mContext).getRemoteAddr()),
							Receiver.engine(mContext).getRemoteVideo());
					Log.d(TAG,"RemoteAddr == "+Receiver.engine(mContext).getRemoteAddr());
					}
				} catch (Exception e) {
					if (!Sipdroid.release) e.printStackTrace();
					return;
				}	
                    lcTest.InitRtpSocket(rtp_socket);
    				lcTest.start();	  
            }
    }

    
    private void StoptVideoRecording() {
        Log.v(TAG, "StoptVideoRecording");

        if (Receiver.listener_video != null)
           {
       		Receiver.listener_video =null;
    		lcTest.StopVideoCall();
    		VideoRec = false;
    		if(socket!=null){
    		socket.close();
    		}
    		if(rtp_socket!=null){
    		rtp_socket.close();
    		}   
           }
    }
	public void onHangup() {
		Log.d(TAG,"onHangup!!!!!!!!");
		VideoRec = false;
		//finish();
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_DOWN:
        case KeyEvent.KEYCODE_VOLUME_UP:
        	RtpStreamReceiver.adjust(keyCode,false);
        	return true;
        case KeyEvent.KEYCODE_ENDCALL:
        	if (Receiver.pstn_state == null ||
				(Receiver.pstn_state.equals("IDLE") && (SystemClock.elapsedRealtime()-Receiver.pstn_time) > 3000)) {
        			Receiver.engine(mContext).rejectcall();
        			return true;		
        	}
        	break;
		}
		return false;
	}
	
	static TelephonyManager tm;
	
	static boolean videoValid() {
		if (Receiver.on_wlan)
			return true;
		if (tm == null) tm = (TelephonyManager) Receiver.mContext.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm.getNetworkType() < TelephonyManager.NETWORK_TYPE_UMTS)
			return false;
		return true;	
	}

	@Override
	public void onClick(View v) {
       switch (v.getId()) {
	case R.id.switch_voice:
		//StoptVideoRecording();	
		Receiver.engine(mContext).switch2audiocallrequest();
      //finish();
		break;
		case R.id.huangup:

		lcTest.StopVideoCall();	
	    huangup();
	//	finish();
		break;
	case R.id.InvertCamera:
		
		Btinvert.setClickable(false);
		Message msg = mHandler.obtainMessage();
		
		lcTest.InvertCamera();
		msg.what = ENABLE_INVERT;
		mHandler.sendMessageDelayed(msg, 1500);
		break;
	default:
		break;
	}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
      
		mChronometer.start();
		//pre_hold = preview.getHolder();
		Log.d(TAG, "Video surfaceCreated enter!!!!");
		lcTest = LCVideoHardEncode.getInstance(displayholder.getSurface(),pre_hold);
        	//start send data
       	Log.d(TAG,"sending!!!!!");
	        if (!mMediaRecorderRecording) 
	        	initializeVideo();
	            startVideoRecording();
	        //  lcTest.start();
         
	         //recive  data
	       if (Receiver.engine(mContext).getRemoteVideo() != 0) {
			
        	new Thread( new Runnable() {
				
				public void run() {
					// TODO Auto-generated method stub
					Log.d(TAG,"runing!!!!!");
					RtpPacket keepalive = new RtpPacket(new byte[12],0);
					RtpPacket videopacket = new RtpPacket(new byte[1024*100],0);
					ByteBuffer IframeBuffer = ByteBuffer.allocate(MAX_BUFFER_LENGHT);
					IframeBuffer.clear();
		        	// get socket 
		            try {
						if (rtp_socket == null){
							
							Log.d(TAG,"rtp_socket == null   @@@@@@");
							rtp_socket = new RtpSocket(socket = new SipdroidSocket(Receiver.engine(mContext).getLocalVideo()),
								InetAddress.getByName(Receiver.engine(mContext).getRemoteAddr()),
								Receiver.engine(mContext).getRemoteVideo());	
						}
					} catch (Exception e) {
						if (!Sipdroid.release) e.printStackTrace();
						return;
					}	
		            //start decodeThread
		        	lcTest.StartDecodeThread();
		            // receive video packet and decode
		        	while(VideoRec){
		        //	for( ; ; ){
						try {
							//Log.d(TAG," before receive dataing!!!!!");
								rtp_socket.receive(videopacket);
							//	Log.d(TAG,"receive video frame data type ="+videopacket.getPayloadType());
								if(videopacket.getPayloadType()==104){// handle I fragment 
							Log.d(TAG,"receive large I frame data lenght=="+videopacket.getPayloadLength());
							    byte [] iFramefragment= videopacket.getPayload();
							    IframeBuffer.put(iFramefragment);
							}else if(videopacket.getPayloadType()==127){ // I fragment end
						        byte [] iFramefragment_end= videopacket.getPayload();
							    IframeBuffer.put(iFramefragment_end);
								Log.d(TAG," recieve large Iframe data end clear bytebuffer!!!");
								byte [] videodata = new byte [IframeBuffer.position()];
								IframeBuffer.get(videodata);
								lcTest.setDecodeData(videodata);
								IframeBuffer.clear();
							} else{
							lcTest.setDecodeData(videopacket.getPayload()); // P/B fragment 
							}
							//Log.d(TAG," before receive dataing!!!!! length="+videopacket.getPayloadLength());
						} catch (IOException e) {
/*						if(rtp_socket!=null){
							rtp_socket.getDatagramSocket().disconnect();
							Log.d(TAG,"receive socket disconnect!!!!");
							try {
							rtp_socket.send(keepalive);
							} catch (IOException e1) {
								return;
							}
							}*/
						}
		        	
		        	}	
				}
			}).start();
        }
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.d(TAG,"video surfaceDestroyed!!!!!");	
	}
	
	public void huangup() {
		if (Receiver.ccCall != null) {
			Receiver.stopRingtone();
			Receiver.ccCall.setState(Call.State.DISCONNECTED);
		}
        (new Thread() {
			public void run() {
    		Receiver.engine(mContext).rejectcall();
			}
		}).start();   	
    }
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mChronometer.stop();
		StoptVideoRecording();
	}

}
