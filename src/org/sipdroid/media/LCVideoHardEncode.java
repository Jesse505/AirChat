package org.sipdroid.media;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.sipdroid.net.RtpPacket;
import org.sipdroid.net.RtpSocket;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;


public class LCVideoHardEncode
{
    private static LCVideoHardEncode lcVideoTest;

    private static Camera camera;
    int cameraPosition = 0;

    private int mWidth;

    private int mHight;

    private boolean isStart = false;

    private boolean isReady = false;

    private static final String TAG = "LCVideo";
    private static final String TAG2 = "DECODE";
    private static final String TAG3 = "PPS";
    private MediaCodec encodeCodec;
    private MediaCodec decodeCodec;

    ByteBuffer[] decoderInputBuffers;
    ByteBuffer[] decoderOutputBuffers;
    
    private byte Yuv420[]=null;

    private byte colorUV[]=null;
    private byte rotate90[]=null;
    
    private RingBuffer mRingBuffer;

    private RingBuffer mDecodeRingBuffer;

    private RingBuffer mDisplayRingBuffer;

    private long timeoutUs = 100;
    
    private static final int MAX_LENGHT= 30000;

    //private long timeoutUs = 50;
    
    private RtpSocket mRtpSocket;
    
    String mime = "video/avc";
    
    private Surface mSurface;
    private SurfaceHolder preSurface;
    
    private boolean DecodenTheadRun = true ;
    private boolean canrun = true;
    
   // MediaOut out = new MediaOut();

    public static LCVideoHardEncode getInstance(Surface surface,SurfaceHolder pre)
    {
/*        if (lcVideoTest != null)
        {
            return lcVideoTest;
        }*/
        return (lcVideoTest = new LCVideoHardEncode(surface,pre));
    }

    public LCVideoHardEncode(Surface surface,SurfaceHolder pre)
    {
        mSurface = surface;
        preSurface = pre;
        Log.d(TAG,"LCVideoHardEncode!!!!!!");
        mRingBuffer = new RingBuffer("encodeRingBuffer");
        mDecodeRingBuffer = new RingBuffer("decodeRingBuffer");
        mDisplayRingBuffer = new RingBuffer("diplayRingBuffer");
    }
    
    public void InitRtpSocket(RtpSocket socket){
    	if(mRtpSocket==null)
    	mRtpSocket = socket;
    }
    public void startCamera(int cameraID)throws Exception
    {
/*        if (isStart)
        {
            close();
        }
        if (!isStart)
        {*/
        	//close();
        Log.d(TAG, "camera.open");
        camera = Camera.open(cameraID);
        Log.d(TAG,"after open camera");
        //}
        //out.nativeTvoutOpen(0x11);
        isReady = false;
        Camera.Parameters pams = camera.getParameters();

        List<Size> sizes = pams.getSupportedPreviewSizes();

        if (sizes != null && sizes.size() > 0)
        {   
        	Log.d(TAG,"set Camera Parameters!!!");
            setCameraParmters(176, 144,cameraID);// sizes.get(0).width// sizes.get(0).height);
            start();
        }
    }
    public void restartCamera(int cameraID)
    {

        if(camera!=null){
    	camera.release();
       
        isReady = false;
        Camera.Parameters pams = camera.getParameters();

        List<Size> sizes = pams.getSupportedPreviewSizes();

        if (sizes != null && sizes.size() > 0)
        {   
        	Log.d(TAG,"set Camera Parameters!!!");
            setCameraParmters(176, 144,cameraID);
        }
        	}
    }

   // SurfaceTexture sf;

    private boolean setCameraParmters(int width, int hight,int CameraID)
    {
        Log.d(TAG, "setCameraParmters width =" + width + " hight = " + hight);
        camera.setPreviewCallback(new CameraPreviewCallBackImpl());
       // camera.setDisplayOrientation(270);
        Yuv420 = new byte[width*hight*3/2]; 
        colorUV = new byte[width*hight*3/2];
        rotate90 = new byte [width*hight*3/2];
        CameraInfo mCameraInfo = new CameraInfo();
        Camera.getCameraInfo(CameraID, mCameraInfo);
        int rotateangle = mCameraInfo.orientation;
        Log.d(TAG,"rotateangle= "+rotateangle);
         if(mCameraInfo.facing==CameraInfo.CAMERA_FACING_FRONT){
        	 camera.setDisplayOrientation(360-rotateangle); 
         }else{
        	 camera.setDisplayOrientation(rotateangle); 
         }
        
       // camera.setDisplayOrientation(90);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFpsRange(25,35);
        parameters.setPreviewFormat(ImageFormat.NV21);//(ImageFormat.NV21);//ImageFormat.YV12
        this.mWidth = width;
        this.mHight = hight;
        parameters.setPreviewSize(mWidth, mHight);
        Log.d(TAG,"setPreviewSize!!!!!");
        try {
			camera.setParameters(parameters);
			Log.d(TAG," after setParameters !!!!!");
		} catch (RuntimeException  e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.d(TAG,"setParameters failed!!!!!");
		}
        try {
        	if(preSurface==null){
    		Log.d(TAG,"no preSurface!!!");     	   
        	}
        	Log.d(TAG,"start preview !!!!");
			camera.setPreviewDisplay(preSurface);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG,"setPreviewDisplay failed!!!!");
			e.printStackTrace();
		}
        isReady = true;
        
        return isReady;
    }

    public boolean start()
    {
        if (isStart)
        {
            Log.e(TAG, "isStart== true");
            return false;
        }
        Log.d(TAG,"start!!!");
        if (!isReady)
        {
            Log.e(TAG, "the camera is not ready!!!");
            return false;
        }

        new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    camera.startPreview();
                    Log.d(TAG,"startPreview!!!!");
                    camera.autoFocus(null);
                    isStart = true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
        return isStart;
    }

    public synchronized boolean  close()
    {
        Log.e(TAG, "close-->isStart = " + isStart);
        if (isStart && camera != null)
        {
            try
            {
/*                new Thread(new Runnable()
                {

                    @Override
                    public void run()
                    {  */  
                    	if(camera!=null){
                            camera.setPreviewCallback(null);
                           // camera.stopPreview();
                            camera.release();
                            
                            Log.e(TAG, "camera.release()");
                            camera = null;	
                    	}

                         //out.nativeTvoutClose();
                        isStart = false;
             /*       }
                }).start();*/
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        // TODO
        return true;
    }

    EncodeThrad mThrad = null;

    class CameraPreviewCallBackImpl implements PreviewCallback
    {

        @SuppressLint("NewApi")
        @Override
        public void onPreviewFrame(byte[] data, Camera _camera)
        {    
            mRingBuffer.set(data);
            if (mThrad == null)
            {
                mThrad = new EncodeThrad();
                mThrad.start();
            }
        }
    }
    
    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    private static int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) {
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            if (isRecognizedFormat(colorFormat)) {
                return colorFormat;
            }
        }
        Log.d(TAG,"couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return 0;   // not reached
    }
    
    private static boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            // these are the formats we know how to handle for this test
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                return false;
        }
    }
	public void setDecodeData(byte[] encodedata) {
		mDecodeRingBuffer.set(encodedata);
	}
    private void EnableIndecodeThread(){
    	DecodenTheadRun = true;
    }
  
    private void StopIndecodeThread(){
    	DecodenTheadRun = false;
    }
    
    private void EnableEncodeThread(){
    	canrun = true;
    }
    
    private void StopEncodeThread (){
    	canrun = false;	
    }
    
    private void initEcodeMediaCodec(String mime)
    {
    	Log.d(TAG,"initEcodeMediaCodec!!!!!!");
        encodeCodec = MediaCodec.createEncoderByType(mime);
        //MediaCodecInfo mediaCodecInfo = selectCodec(mime);
        //encodeCodec = MediaCodec.createByCodecName(mediaCodecInfo.getName());
       // int colorFormat = selectColorFormat(mediaCodecInfo, mime);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(mime, mHight ,mWidth);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,106444);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
        MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        encodeCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        EnableEncodeThread();
        encodeCodec.start();
    }

    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    long encodeFrameCount = 0;
    long printData = 0;

    long printTime = 0;
    
    DecodeThread mDecodeThread;
	int frame_size = 140000;
	byte[] buffer = new byte [frame_size+14];
	int seqn = 0;

    public void onFrame(byte[] buf, int offset, int length)
    {
        ByteBuffer[] inputBuffers = encodeCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = encodeCodec.getOutputBuffers();
        
    	buffer[12] = 4;
    	RtpPacket rtp_packet = new RtpPacket(buffer, 0);

       // swapYV12toI420(buf, Yuv420,mWidth, mHight);
    	swapUV(colorUV, buf,mWidth, mHight);
        YUV420spRotateNegative90(rotate90, colorUV, mWidth, mHight);
        int inputBufferIndex = encodeCodec.dequeueInputBuffer(timeoutUs);
        if (inputBufferIndex >= 0)
        {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            if(inputBuffer.capacity() < length)
            {
                return;
            }
            inputBuffer.clear();
           // inputBuffer.put(buf, offset, length);
           // inputBuffer.put(Yuv420, offset, length);
           // inputBuffer.put(colorUV, offset, length);
            inputBuffer.put(rotate90, offset, length);
            encodeCodec.queueInputBuffer(inputBufferIndex, 0, length, 0, 0);
        }
        int outputBufferIndex = encodeCodec.dequeueOutputBuffer(bufferInfo, timeoutUs);
        if (outputBufferIndex >= 0)
        {
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            outputBuffer.position(bufferInfo.offset);
            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

            byte[] encodedata = new byte[bufferInfo.size];
          //  Log.d("SEND"," encodedata lenght= "+bufferInfo.size);
            outputBuffer.get(encodedata);
           // setDecodeData(encodedata);
            if(seqn==0){
            	for (int i=0;i<encodedata.length;i++){
            	Log.d(TAG3, "sps pps ="+encodedata[i]);
            	}
            }

        	int data_lenght = encodedata.length;    
        	int count = data_lenght%MAX_LENGHT==0?data_lenght/MAX_LENGHT:data_lenght/MAX_LENGHT+1;
        	if(count>1){
        	int start = 0;
        	int end = MAX_LENGHT;
        	
            for (int i=0;i<count-1;i++){
           byte [] IFrame_data = Arrays.copyOfRange(encodedata, start, end);
           start += MAX_LENGHT-1;
           end +=MAX_LENGHT-1;
               
            rtp_packet.setPayload(IFrame_data, MAX_LENGHT);
   			rtp_packet.setPayloadType(104);
   			rtp_packet.setMarker(true);				
   	 		rtp_packet.setSequenceNumber(seqn++);
   	 		//Log.d("SEND","send rtp data length = "+IFrame_data.length);
   		 	rtp_packet.setTimestamp(SystemClock.elapsedRealtime()*90);   
   		 	
   		 	try {
   				mRtpSocket.send(rtp_packet);
   			} catch (IOException e) {
   				// TODO Auto-generated catch block
   				e.printStackTrace();
   			}
            }
                byte [] IFrame_data_last = Arrays.copyOfRange(encodedata, start,data_lenght-1);
                rtp_packet.setPayload(IFrame_data_last, IFrame_data_last.length);
     			rtp_packet.setPayloadType(127);
     			rtp_packet.setMarker(true);				
     	 		rtp_packet.setSequenceNumber(seqn++);
     	 	//	Log.d("SEND","send rtp data length = "+IFrame_data_last.length);
     		 	rtp_packet.setTimestamp(SystemClock.elapsedRealtime()*90);   
     		 	
     		 	try {
     				mRtpSocket.send(rtp_packet);
     				
     			} catch (IOException e) {
     				// TODO Auto-generated catch block
     				e.printStackTrace();
     			}              
        }else{
        	rtp_packet.setPayload(encodedata, bufferInfo.size);
 			rtp_packet.setPayloadType(103);
 			rtp_packet.setMarker(true);				
 	 		rtp_packet.setSequenceNumber(seqn++);
 	 	//	rtp_packet.setPayloadLength(bufferInfo.size);
 	 	//Log.d("SEND","send rtp data length = "+bufferInfo.size);
 		 	rtp_packet.setTimestamp(SystemClock.elapsedRealtime()*90);   
 		 	
 		 	try {
 				mRtpSocket.send(rtp_packet);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
            	
            } 	
            encodeCodec.releaseOutputBuffer(outputBufferIndex, false);
           // Log.d(TAG, " encodeFrameCount = " +  encodeFrameCount);
        }
        else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
        {
            outputBuffers = encodeCodec.getOutputBuffers();

            Log.d(TAG, "encoder output buffers have changed.");
        }
        else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
        {
            MediaFormat encformat = encodeCodec.getOutputFormat();

            Log.d(TAG, "encoder output format has changed to " + encformat);
        }
        else {
            Log.d(TAG, "encode dequeueOutputBuffer fail " + outputBufferIndex );
        }
        
        
    }



    private void configureDecoder(String mimeType, int width, int height, ByteBuffer buf)
    {
        // TODO Auto-generated method stub
        Log.d(TAG, "create decoder...");
        decodeCodec = MediaCodec.createDecoderByType(mimeType);
        MediaFormat format = MediaFormat.createVideoFormat(mimeType, width, height);
        format.setByteBuffer("csd-0", buf);
        Log.d(TAG, "Configuring decoder with input format : " + format);
        decodeCodec.configure(format, // The format of the input data (decoder)
                mSurface, // a surface on which to render the output of this
                      // decoder.
                null, // a crypto object to facilitate secure decryption of the
                      // media data.
                0 // configure the component as an decoder.
                );
        EnableIndecodeThread();
        decodeCodec.start();
        
    }
    private void configureDecoder(String mimeType, int width, int height)
    {
        // TODO Auto-generated method stub
        Log.d(TAG, "create decoder...");
  /*      byte[] sps = {0,0,0,1,39,66,-32,42,-115,104,20,31,-95,0,0,11,-72,0,1,95,-112,15,20,34,-96}; 
        byte[] pps = {0,0,0,1,40,-50,6,-55,32};*/
        
        byte[] sps = {0,0,0,1,(byte)0x27,(byte)0x42,(byte)0xE0,(byte)0x29,(byte)0x8D,(byte)0x68,(byte)0x24,(byte)0x5E,(byte)0x84,0,0,(byte)0x2E,(byte)0xE0,0,5,(byte)0x7E,(byte)0x40,(byte)0x3C,(byte)0x50,(byte)0x8A,(byte)0x80}; 
        byte[] pps = {0,0,0,1,(byte)0x28,(byte)0xCE,2,(byte)0x92,(byte)0x48};
        
        decodeCodec = MediaCodec.createDecoderByType(mimeType);
        Log.d(TAG,"width="+width+"height="+height);
        MediaFormat format = MediaFormat.createVideoFormat(mimeType, width, height);
        Log.d(TAG, "Configuring decoder with input format : " + format);
        format.setByteBuffer("csd-0"  , ByteBuffer.wrap(sps));
        format.setByteBuffer("csd-1"  , ByteBuffer.wrap(pps));
        decodeCodec.configure(format, // The format of the input data (decoder)
                mSurface, // a surface on which to render the output of this
                      // decoder.
                null, // a crypto object to facilitate secure decryption of the
                      // media data.
                0 // configure the component as an decoder.
                );
        Log.d(TAG, "after Configuring decoder !!!!");
        //EnableIndecodeThread();
        decodeCodec.start();
        Log.d(TAG, "after decodeCodec start()!!!!");
    }

    public void StopVideoCall(){
   
    	close();
    	StopEncodeThread();
    	StopIndecodeThread();
    	
    }
    
    public void StartDecodeThread(){
        mDecodeThread = new DecodeThread();
        mDecodeThread.start();
    }
    

    private class DecodeThread 
    {
        ByteBuffer[] inputBuffers;
        ByteBuffer[] outputBuffers;
        BufferInfo decodeBufferInfo = new BufferInfo();
        long decodeFrameCount = 0;

        ByteBuffer buffer;
        
        public DecodeThread(ByteBuffer buffer)
        {
            configureDecoder(mime, mWidth, mHight, buffer);
            
        }
        
        public DecodeThread() {
			// TODO Auto-generated constructor stub
        	configureDecoder(mime, 176, 144);
		}

		public void start()
        {
            if (decodeCodec != null)
            {
               new DecodeInThread().start();
               //new DecodeOutThread().start();                
            }
        }

        DisplayThrad mThrad;
        
        private class DecodeInThread extends Thread
        {
            @Override
            public void run()
            {
                outputBuffers = decodeCodec.getOutputBuffers();
                long startMs = System.currentTimeMillis();
              //for(;;)
               while(DecodenTheadRun)
                {
                    if(decodeCodec != null)
                    {
                        byte[] buffer = mDecodeRingBuffer.get();
                        if(buffer !=null)
                        {   
                        	//Log.d(TAG2,"data get !==null!!! length ="+buffer.length);
                            inputBuffers = decodeCodec.getInputBuffers();
                            int decindex = decodeCodec.dequeueInputBuffer(0);
                            if(decindex >=0)
                            {   //Log.d(TAG2,"decindex==="+decindex);
                                ByteBuffer inputBuffer = inputBuffers[decindex];
                                inputBuffers[decindex].clear();
                                if(inputBuffer.capacity() >= buffer.length)
                                {
                                    inputBuffers[decindex].put(buffer,0,buffer.length);
                                    if(buffer.length==35){
                                    decodeCodec.queueInputBuffer(decindex, 0, buffer.length,
                                                0, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);	
                                    }else{
                                    decodeCodec.queueInputBuffer(decindex, 0, buffer.length,
                                    0, 0);
                                    }
                                }
                            }
                        }
                       // Log.d(TAG2,"data get null!!!");
                    }
                    
                    if(decodeCodec != null)
                    {
                        
                        int index = decodeCodec.dequeueOutputBuffer(decodeBufferInfo, timeoutUs);
                      //  Log.d(TAG,"index=="+index);
                        if(mThrad == null)
                        {
                            //mThrad = new DisplayThrad();
                            //mThrad.start();
                        }
                        while (index >= 0)
                        {
                            //if(index >= 0)
                            //{
                                ByteBuffer outBuffer = outputBuffers[index];
                                
                               if(outBuffer != null)
                               {
                                    byte[] outdata = new byte[mWidth * mHight * 3 / 2];
                                    if(outBuffer.position() != 0)
                                    {
                                        outBuffer.flip();
                                    }
                                    if(outBuffer.remaining() >= outdata.length)
                                    {
                                        outBuffer.get(outdata);
                                        //outBuffer.clear();
                                        //outBuffer = null;
                                        //out.nativeTvoutWrite(outdata, mWidth, mHight);
                                        //mDisplayRingBuffer.set(outdata);
                                    }
                               }
                                while (decodeBufferInfo.presentationTimeUs / 1000 > System
                                        .currentTimeMillis() - startMs) {
                                    try {
                                        sleep(10);
                                        Log.d(TAG, "sleep!!!!!");
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                        break;
                                    }
                                }
                                decodeCodec.releaseOutputBuffer(index, true);
                                decodeFrameCount ++;
                              //  Log.d(TAG, "Decode finish, decodeFrameCount = " + decodeFrameCount);
                                index = decodeCodec.dequeueOutputBuffer(decodeBufferInfo, timeoutUs);
                                
                            //}
                            
                        }                    
                        if(index == MediaCodec.INFO_TRY_AGAIN_LATER)
                        {
                            //Log.d(TAG, "no output from decoder available");
                        }
                        else if(index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
                        {
                            MediaFormat format = decodeCodec.getOutputFormat();
                        }
                        else if(index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
                        {
                            outputBuffers = decodeCodec.getOutputBuffers();
                        }
                    }
                }
               
               
               decodeCodec.stop();
               decodeCodec.release();
            }
        }
        

        
        private class DecodeOutThread extends Thread
        {
            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                for(;;)
                {
                if(decodeCodec != null)
                {
                    outputBuffers = decodeCodec.getOutputBuffers();
                    int index = decodeCodec.dequeueOutputBuffer(decodeBufferInfo, timeoutUs);
                    while (index >= 0)
                    {
                        //if(index >= 0)
                        //{
                            ByteBuffer outBuffer = outputBuffers[index];
                            byte[] outdata = new byte[mWidth * mHight * 3 / 2];
                            outBuffer.get(outdata);
                            mDisplayRingBuffer.set(outdata);
                            decodeCodec.releaseOutputBuffer(index, false);
                            decodeFrameCount ++;
                         //   Log.d(TAG, "Decode finish, decodeFrameCount = " + decodeFrameCount);
                            index = decodeCodec.dequeueOutputBuffer(decodeBufferInfo, timeoutUs);
                        //}
                        
                    }                    
                    if(index == MediaCodec.INFO_TRY_AGAIN_LATER)
                    {
                        Log.d(TAG, "no output from decoder available");
                    }
                    else if(index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
                    {
                        MediaFormat format = decodeCodec.getOutputFormat();
                    }
                    else if(index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
                    {
                        outputBuffers = decodeCodec.getOutputBuffers();
                    }
                }
                }
            }
        }
        
    }

    
    private class EncodeThrad extends Thread
    {
        @Override
        public void run()
        {
            // TODO Auto-generated method stub
            initEcodeMediaCodec(mime);
            while(canrun)
      //     for (;;)
            {
                byte[] buf = mRingBuffer.get();
                if (buf != null)
                {
                    onFrame(buf, 0, buf.length);
                }
            }
            if(encodeCodec != null)
            {
                encodeCodec.stop();
                encodeCodec.release();
            }
        }
    }
    

    
    private class DisplayThrad extends Thread
    {
        byte[] outdata;

        public DisplayThrad()
        {
            //outdata = new byte[mWidth * mHight * 3 / 2];
        }

        @Override
        public void run()
        {
            for (;;)
            {
                byte[] buf = mDisplayRingBuffer.get();
                if (buf != null)
                {
                   // out.nativeTvoutWrite(buf, mWidth, mHight);
                    buf = null;
                }
            }
        }
    }

    private class RingBuffer
    {
        private static final int ringLen = 1;
        private byte[][] mArrays = new byte[ringLen][];

        private int setpost = 0;
        private int getpost = 0;

        private String bufferName;

        public RingBuffer()
        {
            bufferName = "";
        }

        public RingBuffer(String bufferName)
        {
            this.bufferName = bufferName;
        }

        public synchronized void set(byte[] buf)
        {
            int index = setpost % ringLen;
            byte[] old_buffer = mArrays[index];
            mArrays[index] = buf;
            if (old_buffer != null)
            {
//                Log.d(TAG, bufferName + " set is faster then get!");
                old_buffer = null;
            }
            setpost++;
        }

        public synchronized byte[] get()
        {
            if (getpost > setpost)
            {
               // Log.d(TAG, bufferName + "get is faster then set !");
                return null;
            }
            int index = getpost % ringLen;
            byte[] buffer = mArrays[index];
            mArrays[index] = null;
            getpost++;
            return buffer;
        }
        
        public int getCurrentLen()
        {
            return setpost;
        }
    }
    /**
     * @author liuyang4
     * @date 2016.12.26
     * switch camera 
     */
    public synchronized void  InvertCamera(){
        int cameraCount = 0;
        CameraInfo cameraInfo = new CameraInfo();
        cameraCount = Camera.getNumberOfCameras();//寰楀埌鎽勫儚澶寸殑涓暟

        for(int i = 0; i < cameraCount; i ++  ) {
            Camera.getCameraInfo(i, cameraInfo);//寰楀埌姣忎竴涓憚鍍忓ご鐨勪俊鎭�
            if(cameraPosition == 1) {
       
  
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//CAMERA_FACING_FRONT閸撳秶鐤�     CAMERA_FACING_BACK閸氬海鐤� 
                    camera.setPreviewCallback(null);
                	//camera.stopPreview();
                    camera.release();
                    camera = null;
                    try {
						invertstartCamera(i);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    cameraPosition = 0;
                    break;
                }
            } else {
    
                //閻滄澘婀弰顖氬缂冾噯绱�閸欐ɑ娲挎稉鍝勬倵缂冿拷
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {//CAMERA_FACING_FRONT閸撳秶鐤�     CAMERA_FACING_BACK閸氬海鐤� 
                	camera.setPreviewCallback(null);
                	//camera.stopPreview();
                    camera.release();
                    camera = null;
                    try {
						invertstartCamera(i);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    cameraPosition = 1;

    }
           break;
            }
        }

    }
    private  synchronized void invertstartCamera(int cameraID) throws Exception
    {

            Log.d(TAG, "camera.open");
            camera = Camera.open(cameraID);

        Camera.Parameters pams = camera.getParameters();

        List<Size> sizes = pams.getSupportedPreviewSizes();
        if (sizes != null && sizes.size() > 0)
        {
            //camera.setParameters(pams);
            setCameraParmters(176, 144,cameraID);// sizes.get(0).width,
                                        // sizes.get(0).height);
            Thread.sleep(50);
            new Thread(new Runnable()
            {

                @Override
                public void run()
                {
                    try
                    {   
                    	Log.d(TAG,"start preview !!!!");
                        camera.startPreview();
                        camera.autoFocus(null);
                        isStart = true;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    private void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height) 
    {      
    	System.arraycopy(yv12bytes, 0, i420bytes, 0,width*height);
    	System.arraycopy(yv12bytes, width*height+width*height/4, i420bytes, width*height,width*height/4);
    	System.arraycopy(yv12bytes, width*height, i420bytes, width*height+width*height/4,width*height/4);  
    }
    private void swapUV (byte[] desbytes, byte[] srcbytes, int width, int height) 
    {      
    	
    	for(int i=width*height;i<width*height*3/2-1;){
    		byte temp = 0;
    		temp = srcbytes[i];
    	    srcbytes[i]=srcbytes[i+1];
    	    srcbytes[i+1] = temp;
    	    i+=2;
    	}
    	System.arraycopy(srcbytes, 0, desbytes, 0,width*height*3/2);
    	
    } 
    
    void YUV420spRotateNegative90(byte [] dst, byte[] src, int srcWidth, int srcheight)
    {
        int nWidth = 0, nHeight = 0;
        int wh = 0;
        int uvHeight = 0;
        if(srcWidth != nWidth || srcheight != nHeight)
        {
            nWidth = srcWidth;
            nHeight = srcheight;
            wh = srcWidth *srcheight;
            uvHeight = srcheight >> 1;
        }

        //鏃嬭浆Y
        int k = 0;
        for(int i = 0; i < srcWidth; i++){
            int nPos = srcWidth - 1;
            for(int j = 0; j < srcheight; j++)
            {
                dst[k] = src[nPos - i];
                k++;
                nPos += srcWidth;
            }
        }

        for(int i = 0; i < srcWidth; i+=2){
            int nPos = wh + srcWidth - 1;
            for(int j = 0; j < uvHeight; j++) {
                dst[k] = src[nPos - i - 1];
                dst[k + 1] = src[nPos - i];
                k += 2;
                nPos += srcWidth;
            }
        }

        return;
    }
}
