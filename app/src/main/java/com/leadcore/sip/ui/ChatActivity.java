package com.leadcore.sip.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.sipdroid.sipua.R;
import org.sipdroid.sipua.adapter.ChatAdapter;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.utils.AudioRecorderUtils;
import org.sipdroid.sipua.utils.FileUtils;
import org.sipdroid.sipua.utils.ImageUtils;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.SessionUtils;
import org.sipdroid.sipua.widget.ChatListView;
import org.sipdroid.sipua.widget.HeaderLayout;
import org.sipdroid.sipua.widget.HeaderLayout.HeaderStyle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;

import com.leadcore.sms.BaseMessageActivity;
import com.leadcore.sms.entity.Group;
import com.leadcore.sms.entity.Message;
import com.leadcore.sms.entity.Message.CONTENT_TYPE;
import com.leadcore.sms.entity.Users;
import com.leadcore.sms.socket.tcp.TcpClient;
import com.leadcore.sms.socket.udp.IPMSGConst;
import com.leadcore.sms.socket.udp.OnActiveChatActivityListenner;
import com.leadcore.sms.sql.SqlDBOperate;

public class ChatActivity extends BaseMessageActivity implements
		OnActiveChatActivityListenner {

	private final String TAG = "ChatActivity";
	
	private final int TYPE_EDIT = 0;
	private final int TYPE_AUDIO = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		changeActiveChatActivity(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
	}

	@Override
	public void onBackPressed() {
		back();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void finish() {
		removeActiveChatActivity(); // 移除监听
		if (null != mDBOperate) {// 关闭数据库连接
			mDBOperate.close();
			mDBOperate = null;
		}
		mRecordThread = null;
		super.finish();
	}

	@Override
	protected void initViews() {
		mHeaderLayout = (HeaderLayout) findViewById(R.id.chat_header);
		mHeaderLayout.init(HeaderStyle.TITLE_CHAT);

		mClvList = (ChatListView) findViewById(R.id.chat_clv_list);

		mIbTextDitorPlus = (ImageButton) findViewById(R.id.chat_textditor_ib_plus);
		mIvTextDitorAudio = (ImageView) findViewById(R.id.chat_textditor_iv_audio);
		mBtnTextDitorSend = (Button) findViewById(R.id.chat_textditor_btn_send);
		meditText = (EditText) findViewById(R.id.chat_textditor_eet_editer);

		mIbTextDitorKeyBoard = (ImageButton) findViewById(R.id.chat_textditor_ib_keyboard);
		mAudioBtn = (Button) findViewById(R.id.chat_btn_speak);

		mLayoutFullScreenMask = (LinearLayout) findViewById(R.id.fullscreen_mask);
		mLayoutMessagePlusBar = (LinearLayout) findViewById(R.id.message_plus_layout_bar);
		mLayoutMessagePlusPicture = (LinearLayout) findViewById(R.id.message_plus_layout_picture);
		mLayoutMessagePlusCamera = (LinearLayout) findViewById(R.id.message_plus_layout_camera);
		mLayoutMessagePlusFile = (LinearLayout) findViewById(R.id.message_plus_layout_file);
		mLayoutMessagePlusPtt  = (LinearLayout) findViewById(R.id.message_plus_layout_ptt);
	}

	@Override
	protected void initEvents() {
		mHeaderLayout.setOnLeftIamgeButtonClickListener(this);
		mIbTextDitorPlus.setOnClickListener(this);
		mBtnTextDitorSend.setOnClickListener(this);
		mIvTextDitorAudio.setOnClickListener(this);
		meditText.addTextChangedListener(this);
        meditText.setOnTouchListener(this);
        mIbTextDitorKeyBoard.setOnClickListener(this);
		mAudioBtn.setOnTouchListener(this);
		mAudioBtn.setOnLongClickListener(this);
		mLayoutFullScreenMask.setOnTouchListener(this);
		mLayoutMessagePlusPicture.setOnClickListener(this);
		mLayoutMessagePlusCamera.setOnClickListener(this);
		mLayoutMessagePlusFile.setOnClickListener(this);
		mLayoutMessagePlusPtt.setOnClickListener(this);
	}

	private void init() {
		
//		mPeople = getIntent().getParcelableExtra(Users.ENTITY_PEOPLE);
		mPeople = (Users) getIntent().getSerializableExtra(Users.ENTITY_PEOPLE);
		mDBOperate = new SqlDBOperate(this);
		//get infos of localUser
		mID = SessionUtils.getLocalUserID();
		mNickName = SessionUtils.getNickname();
		mIMEI = SessionUtils.getIMEI();
		mLocalIP = SessionUtils.getLocalIPaddress();
		hidePlusBar();
		if (mPeople != null) {
			isGroup = false;
			mLayoutMessagePlusFile.setVisibility(View.VISIBLE);
			mLayoutMessagePlusPtt.setVisibility(View.GONE);
			mSenderID = mDBOperate.getIDByIMEI(mPeople.getIMEI());
			mMessagesList = new ArrayList<Message>();
			mMessagesList = mDBOperate.getScrollMessageOfChattingInfo(0, 50,
					mSenderID, mID);
			mHeaderLayout.setTitleChat(mPeople.getNickname(), mPeople.getIMEI(),
					R.drawable.ic_user_avatar, new OnRightImageButtonClickListener());
			mHeaderLayout.setRightBtnVisibility(false);
		}else {
			isGroup = true;
			mLayoutMessagePlusFile.setVisibility(View.GONE);
			mLayoutMessagePlusPtt.setVisibility(View.VISIBLE);
			mGroup = (Group) getIntent().getSerializableExtra(Group.ENTITY_GROUOP);
			mMessagesList = new ArrayList<Message>();
			mMessagesList = mDBOperate.getScrollMsgOfGroupChattingInfo(0, 50,
					mGroup.getStrIP());
			mHeaderLayout.setTitleChat(mGroup.getStrName(), mGroup.getStrIP(),
					R.drawable.ic_user_avatar, new OnRightImageButtonClickListener());
			mHeaderLayout.setRightBtnVisibility(true);
		}
		
		initfolder();

		mAdapter = new ChatAdapter(mApplication, ChatActivity.this,
				mMessagesList, isGroup);
		mAdapter.setListView(mClvList);
		mClvList.setAdapter(mAdapter);
		mClvList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Message msg = mMessagesList.get((int) id);
				switch (msg.getContentType()) {
				case IMAGE:
					goImageBrowser(msg.getMsgContent());
					break;

				case VOICE:
					// 播放录音
					final ImageView imgView = (ImageView) view
							.findViewById(R.id.voice_message_iv_msgimage);
					if (!isPlay) {
						mMediaPlayer = new MediaPlayer();
						String filePath = msg.getMsgContent();
						try {
							mMediaPlayer.setDataSource(filePath);
							mMediaPlayer.prepare();
							imgView.setImageResource(R.drawable.voicerecord_stop);
							isPlay = true;
							mMediaPlayer.start();
							// 设置播放结束时监听
							mMediaPlayer
									.setOnCompletionListener(new OnCompletionListener() {

										@Override
										public void onCompletion(MediaPlayer mp) {
											if (isPlay) {
												imgView.setImageResource(R.drawable.voicerecord_right);
												isPlay = false;
												mMediaPlayer.stop();
												mMediaPlayer.release();
											}
										}
									});
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						if (mMediaPlayer.isPlaying()) {
							mMediaPlayer.stop();
							mMediaPlayer.release();
							isPlay = false;
						} else {
							isPlay = false;
							mMediaPlayer.release();
						}
						imgView.setImageResource(R.drawable.voicerecord_right);
					}

					break;

				case FILE:
					goFileBrowser(msg.getMsgContent());
//					Intent fileIntent = new Intent();
//					fileIntent.setType("*/*");
//					fileIntent.setData(Uri.parse("file://"
//							+ FileUtils.getPathByFullPath(msg.getMsgContent())));
//					fileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					MyLog.i(TAG, "msgcontent >>> " + msg.getMsgContent()) ;
//					MyLog.i(TAG, "setData >>> " + Uri.parse("file://"
//							+ FileUtils.getPathByFullPath(msg.getMsgContent())));
//					mContext.startActivity(fileIntent);
					break;

				default:
					break;

				}

			}

		});
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.chat_textditor_ib_plus:
			if (!mLayoutMessagePlusBar.isShown()) {
				showPlusBar();
			}
			break;

		case R.id.chat_textditor_btn_send:
			String content = meditText.getText().toString().trim();
			if (!TextUtils.isEmpty(content)) {
				meditText.setText(null);
				sendMessage(content, CONTENT_TYPE.TEXT);
				refreshAdapter();
			}
			break;

		case R.id.chat_textditor_iv_audio:
			onBottomLayoutChanged(TYPE_AUDIO);
			break;

		case R.id.chat_textditor_ib_keyboard:
			onBottomLayoutChanged(TYPE_EDIT);
			break;

		case R.id.message_plus_layout_picture:
			ImageUtils.selectPhoto(ChatActivity.this);
			hidePlusBar();
			break;

		case R.id.message_plus_layout_camera:
			mCameraImagePath = ImageUtils.takePicture(ChatActivity.this);
			hidePlusBar();
			break;

		case R.id.message_plus_layout_file:
			if (isGroup) {
				sendMessage("/storage/sdcard0/自组网/image/239.0.0.60/test.jpg", CONTENT_TYPE.IMAGE);
			}else {
				showFileChooser();
				hidePlusBar();
			}
			break;
		case R.id.message_plus_layout_ptt:
			Receiver.engine(mContext).launchPttSession(mGroup.getStrIP());
			break;
		}

	}

	@Override
	public boolean onLongClick(View v) {
		try {
			startRecord();
		} catch (IllegalStateException e) { //modify by zyf 其他设备占用麦克风或者录音权限被禁止
			showShortToast(R.string.chat_toast_record_fail);
		}
		
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			if (recordState == RECORD_OFF) {
				switch (v.getId()) {
				case R.id.chat_textditor_eet_editer:
					showKeyBoard();
					break;

				case R.id.fullscreen_mask:
					hidePlusBar();
					break;
				}
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (recordState == RECORD_ON) {
				float moveY = event.getY();
				if (moveY - downY < -50) {
					isMove = true;
					showVoiceDialog(1);
				} else if (moveY - downY < -20) {
					isMove = false;
					showVoiceDialog(0);
				}
			}
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			sendVoiceData();
			break;

		}
		return false;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (TextUtils.isEmpty(s)) {
			mIvTextDitorAudio.setVisibility(View.VISIBLE);
			mBtnTextDitorSend.setVisibility(View.GONE);
		} else {
			mIvTextDitorAudio.setVisibility(View.GONE);
			mBtnTextDitorSend.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isThisActivityMsg(Message msg) {
		if (isGroup) {
			if (mGroup.getStrIP().equals(msg.getReceiveIP())) {
				mMessagesList.add(msg);
				return true;
			}
		}else {
			if (msg.getReceiveIP() == null) {//receiveIP为空，说明是点对点短消息 modify by zyf
				if (mPeople.getIMEI().equals(msg.getSenderIMEI())) {
					mMessagesList.add(msg);
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void processMessage(android.os.Message msg) {
		switch (msg.what) {
		case IPMSGConst.IPMSG_SENDMSG:
		case IPMSGConst.IPMSG_REFRESH:
			refreshAdapter();
			break;

		case IPMSGConst.IPMSG_RECEIVE_IMAGE_DATA: { // 图片开始发送
		 Receiver.engine(mContext).startTCPClient(mCameraImagePath, mPeople.getIpaddress(),
				 Message.CONTENT_TYPE.IMAGE, 0);
		}
			break;

		case IPMSGConst.IPMSG_RECIEVE_VOICE_DATA: { // 语音开始发送
		 Receiver.engine(mContext).startTCPClient(mVoicePath, mPeople.getIpaddress(),
				 Message.CONTENT_TYPE.VOICE, (int)recodeTime);
		}
			break;

		case IPMSGConst.IPMSG_RECIEVE_FILE_DATA: { // 文件开始发送
		MyLog.i(TAG, "startTCPClient sendFilePath >>> " + sendFilePath);
		Receiver.engine(mContext).startFileSend(sendFilePath, mPeople.getIpaddress(), 
				Message.CONTENT_TYPE.FILE, ((Message)msg.obj).getId(), ((Message)msg.obj).getmReservedID());
		}
			break;
			
		case IPMSGConst.IPMSG_SEND_FILE:
		case IPMSGConst.IPMSG_RECEIVE_FILE:
			if (mAdapter != null) {
				mAdapter.updateFileState(msg);
			}
			break;
		
		}
	}

	
	
	
	
	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case ImageUtils.INTENT_REQUEST_CODE_ALBUM:
            if (data == null) {
                return;
            }
            if (resultCode == RESULT_OK) {
                if (data.getData() == null) {
                    return;
                }
                if (!FileUtils.isSdcardExist()) {
                    showShortToast(R.string.toast_sdcard_unavailable);
                    return;
                }
                Uri uri = data.getData();
                String[] proj = { MediaStore.Images.Media.DATA };
                Cursor cursor = managedQuery(uri, proj, null, null, null);
                if (cursor != null) {
                    int column_index = cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                        String path = cursor.getString(column_index);
                        mCameraImagePath = path;
                            if (path != null) {
//                                ImageUtils.createThumbnail(this, path, THUMBNAIL_PATH
//                                        + File.separator);
                                sendMessage(path, CONTENT_TYPE.IMAGE);
                                refreshAdapter();
                            }
//                        }
                    }
                }
            }
            break;

        case ImageUtils.INTENT_REQUEST_CODE_CAMERA:
            if (resultCode == RESULT_OK) {
                if (mCameraImagePath != null) {
                	String thumbnailName = FileUtils.getNameByPath(mCameraImagePath);
                    String thumbnailPath = ImageUtils.savePhotoToSDCard(
                            ImageUtils.CompressionPhoto(mScreenWidth, mCameraImagePath, 2),
                            BaseMessageActivity.THUMBNAIL_PATH + File.separator, thumbnailName);
                    MyLog.i(TAG, "from camera thumbnailPath >>> " + thumbnailPath);
                    sendMessage(mCameraImagePath, CONTENT_TYPE.IMAGE);
                    refreshAdapter();
                }
            }
//             mCameraImagePath = null;
            break;
            
        case FILE_SELECT_CODE: {
            if (resultCode == RESULT_OK) {
            	if (data == null || "".equals(data)) {
            		showShortToast(R.string.text_chat_file_choose_error);
					return;
				}
                Uri uri = data.getData();
                String path = uri.getPath();
                if (path != null) {
					MyLog.e(TAG, "uri.getPath() >>> " + uri.getPath());
					if (path.startsWith("/external")) {
						Cursor actualimagecursor;
		                String[] proj = {MediaStore.Images.Media.DATA};
		                if (Build.VERSION.SDK_INT < 11) {
		                	actualimagecursor = managedQuery(uri, proj, null, null, null);
						}else {
							CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);
							actualimagecursor = cursorLoader.loadInBackground();
						}
		                if (actualimagecursor == null) {
		                	showShortToast(R.string.text_chat_file_choose_error);
							return;
						}
		                int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		                actualimagecursor.moveToFirst();
		                path = actualimagecursor.getString(actual_image_column_index);
					}
				}else {
					showShortToast(R.string.text_chat_file_choose_error);
					return;
				}
              
                
                if (path != null) {
                    sendFilePath = path;
                    sendMessage(sendFilePath, CONTENT_TYPE.FILE);
                    refreshAdapter();
                }
            }
        }
            break;
    }
	}

	private void startRecord() {
		
		RECORD_FILENAME = System.currentTimeMillis()
				+ org.sipdroid.sipua.utils.TextUtils.getRandomNumStr(3);

		mAudioRecorder = new AudioRecorderUtils();
		mAudioRecorder.setVoicePath(VOICE_PATH, RECORD_FILENAME);
		mRecordThread = new Thread(recordThread);

		try {
			mAudioRecorder.start();
			recordState = RECORD_ON;    //modify by zyf 必须start audioRecorder 成功，才认为在录音状态
			mRecordThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		showVoiceDialog(0);
	}

	private void stopRecord() {
		recordState = RECORD_OFF;
		try {
			mRecordThread.interrupt();
			mRecordThread = null;
			mAudioRecorder.stop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {   //modify by zyf 捕获stop failed 的异常
			e.printStackTrace();
		}
		voiceValue = 0.0;
	}

	// 录音线程
	private Runnable recordThread = new Runnable() {

		@Override
		public void run() {
			recodeTime = 0.0f;
			while (recordState == RECORD_ON) {
					try {
						// 限制录音时长
						if (recodeTime >= MAX_RECORD_TIME && MAX_RECORD_TIME != 0) {
							 recordHandler.sendEmptyMessage(0);
						 }
						Thread.sleep(200);
						recodeTime += 0.2;
						
						// 获取音量，更新dialog
						if (!isMove) {
							voiceValue = mAudioRecorder.getAmplitude();
							recordHandler.sendEmptyMessage(1);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}

		}
	};

	public Handler recordHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				showWarnToast(R.string.chat_toast_record_longtime);
				sendVoiceData();
				break;
			case 1:
				setDialogImage();
				break;
			default:
				break;
			}
			
		}
	};
	
	@Override
	public void onClick() {
		back();
	}
	
	private void back(){
		if (mLayoutMessagePlusBar.isShown()) {
			hidePlusBar();
		} else if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE) {
			hideKeyBoard();
		} else {
			finish();
		}
	}
	
	
	private void onBottomLayoutChanged(int type){
		switch (type) {
		case TYPE_EDIT:
			mAudioBtn.setVisibility(View.GONE);
			mIbTextDitorKeyBoard.setVisibility(View.GONE);
			meditText.setVisibility(View.VISIBLE);
			mIvTextDitorAudio.setVisibility(View.VISIBLE);
			break;
		case TYPE_AUDIO:
			mAudioBtn.setVisibility(View.VISIBLE);
			mIbTextDitorKeyBoard.setVisibility(View.VISIBLE);
			meditText.setVisibility(View.GONE);
			mIvTextDitorAudio.setVisibility(View.GONE);
			hideKeyBoard();
			break;
		default:
			break;
		}
	}
	
	private void sendVoiceData(){
		if (recordState == RECORD_ON) {

			stopRecord();

			if (mRecordDialog != null && mRecordDialog.isShowing()) {
				mRecordDialog.dismiss();
			}

			if (!isMove) {
				if (recodeTime < MIN_RECORD_TIME) {
					showWarnToast(R.string.chat_toast_record_shorttime);
				} else {
					mVoicePath = mAudioRecorder.getVoicePath();
					sendVoiceMsg(mVoicePath, CONTENT_TYPE.VOICE, recodeTime);
					refreshAdapter();
				}
			}

			isMove = false;
		}
		if (mAudioRecorder != null)
			mAudioRecorder = null;
	}
	
	private void goImageBrowser(String path){
		MyLog.e("zyf", "goImageBrowser path " + path);
		File file = null;
		try {
			file = new File(path);
		} catch (NullPointerException e) {
			showWarnToast(R.string.text_chat_iamge_deleted);
			return;
		}
		
		Intent i = new Intent(Intent.ACTION_VIEW);
		Uri mUri = Uri.parse("file://" + file.getPath());
		i.setDataAndType(mUri, "image/*");
		startActivity(i);
	}
	
	private void goFileBrowser(String path){
		MyLog.e("zyf", "goFileBrowser path " + path);
		File file = null;
		try {
			file = new File(path);
		} catch (NullPointerException e) {
			showWarnToast(R.string.text_chat_file_deleted);
			return;
		}
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		Uri mUri = Uri.parse("file://" + FileUtils.getPathByFullPath(file.getPath()));
		i.setDataAndType(mUri, "*/*");
		startActivity(i);
	}

}
