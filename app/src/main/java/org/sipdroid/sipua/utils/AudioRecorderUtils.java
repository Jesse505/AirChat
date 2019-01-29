package org.sipdroid.sipua.utils;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioRecorderUtils {
    private static int SAMPLE_RATE_IN_HZ = 8000; // 采样率���

    private MediaRecorder mMediaRecorder;
    private String mVoicePath;

    public AudioRecorderUtils() {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
    }

    public void setVoicePath(String path, String filename) {
        this.mVoicePath = path + File.separator + filename + ".amr";
    }

    public String getVoicePath() {
        return this.mVoicePath;
    }

    public void start() throws IOException {
        File directory = new File(mVoicePath).getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Path to file could not be created");
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setAudioSamplingRate(SAMPLE_RATE_IN_HZ);
        mMediaRecorder.setOutputFile(mVoicePath);
        mMediaRecorder.prepare();
        mMediaRecorder.start();
    }

    public void stop() throws IOException {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    public double getAmplitude() {
        if (mMediaRecorder != null) {
            return (mMediaRecorder.getMaxAmplitude());
        }
        else
            return 0;
    }
    
    
    // 音频获取源
    public static int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    public static int sampleRateInHz = 44100;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    public static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    public static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    public static int bufferSizeInBytes = 0;
    /**
     * 判断是是否有录音权限
     */
    public static boolean isHasPermission(final Context context){
        bufferSizeInBytes = 0;
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        AudioRecord audioRecord =  new AudioRecord(audioSource, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);
        //开始录制音频
        try{
            // 防止某些手机崩溃，例如联想
            audioRecord.startRecording();
        }catch (IllegalStateException e){
            e.printStackTrace();
        }
        /**
         * 根据开始录音判断是否有录音权限
         */
        if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            return false;
        }
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
 
        return true;
    }
}