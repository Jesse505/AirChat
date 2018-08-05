package com.leadcore.sms;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.BaseApplication;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.adapter.ChatAdapter;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.utils.AudioRecorderUtils;
import org.sipdroid.sipua.utils.DateUtils;
import org.sipdroid.sipua.utils.FileUtils;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.SessionUtils;
import org.sipdroid.sipua.widget.ChatListView;
import org.sipdroid.sipua.widget.HeaderLayout;
import org.sipdroid.sipua.widget.HeaderLayout.onLeftImageButtonClickListener;
import org.sipdroid.sipua.widget.HeaderLayout.onRightImageButtonClickListener;

import com.leadcore.sip.ui.GroupMembersActivity;
import com.leadcore.sms.entity.Group;
import com.leadcore.sms.entity.Message;
import com.leadcore.sms.entity.Message.CONTENT_TYPE;
import com.leadcore.sms.entity.Users;
import com.leadcore.sms.file.FileState;
import com.leadcore.sms.socket.tcp.TcpClient;
import com.leadcore.sms.socket.tcp.TcpFileServer;
import com.leadcore.sms.socket.udp.IPMSGConst;
import com.leadcore.sms.socket.udp.IPMSGProtocol;
import com.leadcore.sms.sql.SqlDBOperate;

import android.R.integer;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public abstract class BaseMessageActivity extends BaseActivity implements 
        OnClickListener, OnLongClickListener, OnTouchListener, TextWatcher,onLeftImageButtonClickListener {

    protected static final int FILE_SELECT_CODE = 4;
    public static String IMAG_PATH;
    public static String THUMBNAIL_PATH;
    public static String VOICE_PATH;
    public static String FILE_PATH;

    protected HeaderLayout mHeaderLayout;
    protected ChatListView mClvList;

    /*********加号按钮*********************/
    protected ImageButton mIbTextDitorPlus;
    /*********输入框***********************/
    protected EditText meditText;
    /*********发送按钮*********************/
    protected Button mBtnTextDitorSend;
    /*********语音按钮*********************/
    protected ImageView mIvTextDitorAudio;
    /*********键盘按钮*********************/
    protected ImageButton mIbTextDitorKeyBoard;
    /*********录音按钮*********************/
    protected Button mAudioBtn;

    protected LinearLayout mLayoutFullScreenMask;
    protected LinearLayout mLayoutMessagePlusBar;
    protected LinearLayout mLayoutMessagePlusPicture;
    protected LinearLayout mLayoutMessagePlusCamera;
    protected LinearLayout mLayoutMessagePlusFile;
    protected LinearLayout mLayoutMessagePlusPtt;
    

    protected Bitmap mRoundsSelected;
    protected Bitmap mRoundsNormal;

    protected List<Message> mMessagesList; // 消息列表
    protected ChatAdapter mAdapter;
    protected Users mPeople; // 聊天的对象
    protected Group mGroup;
    protected Boolean isGroup;
    protected String groupIP;
    protected SqlDBOperate mDBOperate;
    protected String mCameraImagePath; //原始照片的路径或者照相的存储路径

    // 录音变量
    protected String mVoicePath;
    protected static final int MAX_RECORD_TIME = 30; // 最长录制时间，单位秒，0为无时间限制
    protected static final int MIN_RECORD_TIME = 1; // 最短录制时间，单位秒，0为无时间限制
    protected static final int RECORD_OFF = 0; // 不在录音
    protected static final int RECORD_ON = 1; // 正在录音
    protected String RECORD_FILENAME; // 录音文件名

    protected TextView mTvRecordDialogTxt;
    protected ImageView mIvRecVolume;

    protected Dialog mRecordDialog;
    protected AudioRecorderUtils mAudioRecorder;
    protected MediaPlayer mMediaPlayer;
    protected Thread mRecordThread;

    protected boolean isPlay = false; // 播放状态
    protected int recordState = 0; // 录音状态
    protected float recodeTime = 0.0f; // 录音时长
    protected double voiceValue = 0.0; // 录音的音量值
    protected boolean isMove = false; // 手指是否移动
    protected float downY;
    private float mlastRecordTime;

    // 文件传输变量
    protected String sendFilePath; // 文件路径
    
    protected TcpClient tcpClient = null;
//    protected HashMap<String, FileState> sendFileStates;
    protected HashMap<String, FileState> reciveFileStates;

    protected String mNickName;
    protected String mIMEI;
    protected String mLocalIP;
    protected int mID;
    protected int mSenderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initViews();
        initEvents();
    }

    public class OnRightImageButtonClickListener implements onRightImageButtonClickListener {

        @Override
        public void onClick() {
        	if (isGroup) {
        		Intent intent = new Intent(BaseMessageActivity.this, GroupMembersActivity.class);
                intent.putExtra(Group.ENTITY_GROUOP, mGroup);
                startActivity(intent);
			}else {
				//nothing to do
			}
        }
    }

    protected void showKeyBoard() { 
        meditText.requestFocus();
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(
        		meditText, 0);
    }

    protected void hideKeyBoard() {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                BaseMessageActivity.this.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
    

    protected void showPlusBar() {
    	hideKeyBoard();
        mLayoutFullScreenMask.setEnabled(true);
        mLayoutMessagePlusBar.setEnabled(true);
        mLayoutMessagePlusPicture.setEnabled(true);
        mLayoutMessagePlusCamera.setEnabled(true);
        mLayoutMessagePlusFile.setEnabled(true);
        mLayoutMessagePlusPtt.setEnabled(true);
        Animation animation = AnimationUtils.loadAnimation(BaseMessageActivity.this,
                R.anim.controller_enter);
        mLayoutMessagePlusBar.setAnimation(animation);
        mLayoutMessagePlusBar.setVisibility(View.VISIBLE);
        mLayoutFullScreenMask.setVisibility(View.VISIBLE);
    }

    protected void hidePlusBar() {
        mLayoutFullScreenMask.setEnabled(false);
        mLayoutMessagePlusBar.setEnabled(false);
        mLayoutMessagePlusPicture.setEnabled(false);
        mLayoutMessagePlusCamera.setEnabled(false);
        mLayoutMessagePlusFile.setEnabled(false);
        mLayoutMessagePlusPtt.setEnabled(false);
        mLayoutFullScreenMask.setVisibility(View.GONE);
        Animation animation = AnimationUtils.loadAnimation(BaseMessageActivity.this,
                R.anim.controller_exit);
        animation.setInterpolator(AnimationUtils.loadInterpolator(BaseMessageActivity.this,
                android.R.anim.anticipate_interpolator));
        mLayoutMessagePlusBar.setAnimation(animation);
        mLayoutMessagePlusBar.setVisibility(View.GONE);
    }


    public void refreshAdapter() {
        mAdapter.setData(mMessagesList);
        mAdapter.notifyDataSetChanged();
        setLvSelection(mMessagesList.size());
    }

    public void setLvSelection(int position) {
        mClvList.setSelection(position);
    }

    /**
     * createSavePath 存储目录初始化
     */
    protected void initfolder() {
        if (null != BaseApplication.IMAG_PATH) {
        	if (isGroup) {
				String groupIP = mGroup.getStrIP();
				createSavePath(groupIP);
			}else {
				String imei = mPeople.getIMEI();
	            createSavePath(imei);
			}
        }
    }

    private void createSavePath(String path){
    	IMAG_PATH = BaseApplication.IMAG_PATH + File.separator + path;
        THUMBNAIL_PATH = BaseApplication.THUMBNAIL_PATH + File.separator + path;
        VOICE_PATH = BaseApplication.VOICE_PATH + File.separator + path;
        FILE_PATH = BaseApplication.FILE_PATH + File.separator + path;
        if (!FileUtils.isFileExists(IMAG_PATH))
            FileUtils.createDirFile(IMAG_PATH);
        if (!FileUtils.isFileExists(THUMBNAIL_PATH))
            FileUtils.createDirFile(THUMBNAIL_PATH);
        if (!FileUtils.isFileExists(VOICE_PATH))
            FileUtils.createDirFile(VOICE_PATH);
        if (!FileUtils.isFileExists(FILE_PATH))
            FileUtils.createDirFile(FILE_PATH);
    }
    
    public void sendVoiceMsg(String content, CONTENT_TYPE type, float recordTime){
    	mlastRecordTime = recordTime;
    	sendMessage(content, type);
    	
    }
    
    public void sendMessage(String content, CONTENT_TYPE type) {
    	String nowtime = DateUtils.getDisplayTime();
    	String messageListTime=DateUtils.getMessageListTime();
    	Message msg ;
    	if (isGroup) {
        	if (type == CONTENT_TYPE.VOICE) {
        		msg = new Message(mIMEI, nowtime, content, type, (int)mlastRecordTime);
    		}else {
    			msg = new Message(mIMEI, nowtime, content, type);
    		}
            msg.setSenderName(SessionUtils.getNickname());
            msg.setReceiveIP(mGroup.getStrIP());
            mMessagesList.add(msg);
            mApplication.addLastMsgCache(mGroup.getStrIP(), msg); // 更新消息缓存

            switch (type) {
                case TEXT:
                    Receiver.engine(mContext).sendMultiSMSdata(IPMSGConst.IPMSG_GROUP_SENDMSG, 
                    		mGroup.getStrIP(), msg);
                    break;

                case IMAGE:
                	TcpFileServer.getInstance(mContext).startServer();
                    Receiver.engine(mContext).sendMultiSMSdata(IPMSGConst.IPMSG_GROUP_SEND_IMAGE,
                    		mGroup.getStrIP(), msg, content);
                    break;

                case VOICE:
                	TcpFileServer.getInstance(mContext).startServer();
                    Receiver.engine(mContext).sendMultiSMSdata(IPMSGConst.IPMSG_GROUP_SEND_VOICE,
                    		mGroup.getStrIP(), msg, content);
                    break;

            }
//            long id =  mDBOperate.addChattingInfo(mID, mSenderID, nowtime, content, type, (int)mlastRecordTime);
            long id = mDBOperate.addGroupChattingInfo(mIMEI,SessionUtils.getNickname(), mGroup.getStrIP(), 
            		nowtime, content, type, (int)mlastRecordTime);
            //add by lss for group message list
            mDBOperate.queryNewChattingInfo(mIMEI, SessionUtils.getNickname(), mGroup.getStrIP(), 
            		DateUtils.getMessageListTime(), content, type, (int)mlastRecordTime);
            //add end
		}else {
	    	if (type == CONTENT_TYPE.VOICE) {
	    		msg = new Message(mIMEI, nowtime, content, type, (int)mlastRecordTime);
			}else {
				msg = new Message(mIMEI, nowtime, content, type);
			}
	    	long id =  mDBOperate.addChattingInfo(mID, mSenderID, nowtime, content, type, (int)mlastRecordTime);
	        msg.setID(id);
	        mMessagesList.add(msg);
	        mApplication.addLastMsgCache(mPeople.getIMEI(), msg); // 更新消息缓存

	        switch (type) {
	            case TEXT:
	                Receiver.engine(mContext).sendSMSdata(IPMSGConst.IPMSG_SENDMSG,
	                		mPeople.getIpaddress(), msg);
	                break;

	            case IMAGE:
	                Receiver.engine(mContext).sendSMSdata(IPMSGConst.IPMSG_SEND_IMAGE_DATA,
	                		mPeople.getIpaddress());
	                break;

	            case VOICE:
	                Receiver.engine(mContext).sendSMSdata(IPMSGConst.IPMSG_SEND_VOICE_DATA,
	                		mPeople.getIpaddress());
	                break;

	            case FILE:
	                Message fileMsg = msg.clone();
	                fileMsg.setID(id);
	                fileMsg.setMsgContent(FileUtils.getNameByPath(msg.getMsgContent()));
	                Receiver.engine(mContext).sendSMSdata(IPMSGConst.IPMSG_SENDMSG,
	                		mPeople.getIpaddress(),fileMsg);
	                break;

	        }
	        
	        	        	       
	        //add by lss
	        mDBOperate.queryNewChattingInfo(mID, mSenderID, DateUtils.getMessageListTime(), content, type, (int)mlastRecordTime,"");
	        MyLog.d("lss","mID"+mID
					+":::mSenderID"+mSenderID);
	        MyLog.d("lss","send query success!!!!");
	        //add end
	        
		}
  
    }

    // 录音时显示Dialog
    protected void showVoiceDialog(int flag) {
        if (mRecordDialog == null) {
            mRecordDialog = new Dialog(BaseMessageActivity.this, R.style.DialogStyle);
            mRecordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mRecordDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mRecordDialog.setContentView(R.layout.record_dialog);
            mIvRecVolume = (ImageView) mRecordDialog.findViewById(R.id.record_dialog_img);
            mTvRecordDialogTxt = (TextView) mRecordDialog.findViewById(R.id.record_dialog_txt);
        }
        switch (flag) {
            case 1:
                mIvRecVolume.setImageResource(R.drawable.record_cancel);
                mTvRecordDialogTxt.setText(getString(R.string.chat_dialog_record_cancel_up));
                break;

            default:
                mIvRecVolume.setImageResource(R.drawable.record_animate_01);
                mTvRecordDialogTxt.setText(getString(R.string.chat_dialog_record_cancel_move));
                break;
        }
        mTvRecordDialogTxt.setTextSize(14);
        mRecordDialog.show();
    }

    // 录音Dialog图片随声音大小切换
    protected void setDialogImage() {
        if (voiceValue < 800.0) {
            mIvRecVolume.setImageResource(R.drawable.record_animate_01);
        }
        else if (voiceValue > 800.0 && voiceValue < 1200.0) {
            mIvRecVolume.setImageResource(R.drawable.record_animate_02);
        }
        else if (voiceValue > 1200.0 && voiceValue < 1400.0) {
            mIvRecVolume.setImageResource(R.drawable.record_animate_03);
        }
        else if (voiceValue > 1400.0 && voiceValue < 1600.0) {
            mIvRecVolume.setImageResource(R.drawable.record_animate_04);
        }
        else if (voiceValue > 1600.0 && voiceValue < 1800.0) {
            mIvRecVolume.setImageResource(R.drawable.record_animate_05);
        }
        else if (voiceValue > 1800.0 && voiceValue < 2000.0) {
            mIvRecVolume.setImageResource(R.drawable.record_animate_06);
        }
        else if (voiceValue > 2000.0 && voiceValue < 3000.0) {
            mIvRecVolume.setImageResource(R.drawable.record_animate_07);
        }
        else if (voiceValue > 3000.0 && voiceValue < 4000.0) {
            mIvRecVolume.setImageResource(R.drawable.record_animate_08);
        }
        else if (voiceValue > 4000.0 && voiceValue < 5000.0) {
            mIvRecVolume.setImageResource(R.drawable.record_animate_09);
        }
        else if (voiceValue > 5000.0 && voiceValue < 6000.0) {
            mIvRecVolume.setImageResource(R.drawable.record_animate_10);
        }
        else if (voiceValue > 6000.0 && voiceValue < 8000.0) {
            mIvRecVolume.setImageResource(R.drawable.record_animate_11);
        }
        else if (voiceValue > 8000.0 && voiceValue < 10000.0) {
            mIvRecVolume.setImageResource(R.drawable.record_animate_12);
        }
        else if (voiceValue > 10000.0 && voiceValue < 12000.0) {
            mIvRecVolume.setImageResource(R.drawable.record_animate_13);
        }
        else if (voiceValue > 12000.0) {
            mIvRecVolume.setImageResource(R.drawable.record_animate_14);
        }
    }

    // 录音时间太短时Toast显示
    protected void showWarnToast(int toastTextId) {
        showWarnToast(getString(toastTextId));
    }

    protected void showWarnToast(String toastText) {
        Toast toast = new Toast(BaseMessageActivity.this);
        LinearLayout linearLayout = new LinearLayout(BaseMessageActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(20, 20, 20, 20);

        ImageView imageView = new ImageView(BaseMessageActivity.this);
        imageView.setImageResource(R.drawable.voice_to_short);

        TextView mTv = new TextView(BaseMessageActivity.this);
        mTv.setText(toastText);
        mTv.setTextSize(14);
        mTv.setTextColor(Color.WHITE);

        // 将ImageView和ToastView合并到Layout中
        linearLayout.addView(imageView);
        linearLayout.addView(mTv);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setBackgroundResource(R.drawable.record_bg);

        toast.setView(linearLayout);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /** 调用文件选择软件来选择文件 **/
    protected void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(
                    Intent.createChooser(intent, getString(R.string.text_file_send_select)),
                    FILE_SELECT_CODE);
        }
        catch (ActivityNotFoundException ex) {
            Toast.makeText(BaseMessageActivity.this, R.string.toast_file_manager_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

}
