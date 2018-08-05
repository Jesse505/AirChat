package org.sipdroid.sipua.adapter;

import java.io.File;
import java.util.List;

import org.sipdroid.sipua.BaseApplication;
import org.sipdroid.sipua.BaseObjectListAdapter;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.utils.FileUtils;
import org.sipdroid.sipua.utils.ImageUtils;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.SessionUtils;
import org.sipdroid.sipua.widget.HandyTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.leadcore.sms.BaseMessageActivity;
import com.leadcore.sms.entity.Entity;
import com.leadcore.sms.entity.Message;
import com.leadcore.sms.file.FileState;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChatAdapter extends BaseObjectListAdapter {

	private static String TAG = "ChatAdapter";

	private static final int TYPE_COUNT = 8;

	private static final int TYPE_LEFT_TEXT = 0;
	private static final int TYPE_LEFT_IMAGE = 1;
	private static final int TYPE_LEFT_VOICE = 2;
	private static final int TYPE_LEFT_FILE = 3;

	private static final int TYPE_RIGHT_TEXT = 4;
	private static final int TYPE_RIGHT_IMAGE = 5;
	private static final int TYPE_RIGHT_VOICE = 6;
	private static final int TYPE_RIGHT_FILE = 7;


	private static final int ITEM_POSITION = -1;

	private ListView mListView;
	private Bitmap mImageContentBitmap;
	
	private Boolean  isGroup = false;
	private static long lastTime;//add by lss
	private static int AVAILABLE_TIME = 300000;//add by lss
	public ChatAdapter(BaseApplication application, Context context,
			List<? extends Entity> datas) {
		super(application, context, datas);
	}
	
	public ChatAdapter(BaseApplication application,Context context,
			List<? extends Entity> datas,Boolean isGroup){
		super(application, context, datas);
		this.isGroup = isGroup;
	}

	public void setData(List<? extends Entity> datas) {
		super.setData(datas);
	}
	

	public void setListView(ListView view) {
		this.mListView = view;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Message msg = (Message) getItem(position);
		int messageType = getItemViewType(position);

		ViewHolder holder = null;

		if (convertView == null) {

			holder = new ViewHolder();

			switch (messageType) {
			case TYPE_LEFT_TEXT:
				convertView = mInflater.inflate(
						R.layout.message_group_receive_template, null);
				holder.mtvSenderName = (TextView) convertView.findViewById(R.id.left_tv_senderName);
				holder.mHtvTimeStampTime = (HandyTextView) convertView
						.findViewById(R.id.message_timestamp_htv_time);
				holder.mLayoutMessageContainer = (LinearLayout) convertView
						.findViewById(R.id.left_message_layout_messagecontainer);
				holder.mLayoutTimeContainer = (RelativeLayout) convertView
						.findViewById(R.id.message_layout_timecontainer);
				holder.mView = mInflater.inflate(R.layout.message_text, null);
				holder.mEtvTextContent = (HandyTextView) holder.mView
						.findViewById(R.id.message_etv_msgtext);
				holder.mEtvTextContent.setTextColor(0xff607455);//add by lss
				holder.mLayoutMessageContainer.addView(holder.mView);
				break;

			case TYPE_LEFT_IMAGE:
				convertView = mInflater.inflate(
						R.layout.message_group_receive_template, null);
				holder.mtvSenderName = (TextView) convertView.findViewById(R.id.left_tv_senderName);
				holder.mHtvTimeStampTime = (HandyTextView) convertView
						.findViewById(R.id.message_timestamp_htv_time);
				holder.mLayoutMessageContainer = (LinearLayout) convertView
						.findViewById(R.id.left_message_layout_messagecontainer);
				holder.mLayoutTimeContainer = (RelativeLayout) convertView
						.findViewById(R.id.message_layout_timecontainer);
				holder.mView = mInflater.inflate(R.layout.message_image, null);

				holder.mIvImageContent = (ImageView) holder.mView
						.findViewById(R.id.message_iv_msgimage);
				holder.mLayoutMessageContainer.addView(holder.mView);
				break;

			case TYPE_LEFT_VOICE:
				convertView = mInflater.inflate(
						R.layout.message_group_receive_template, null);
				holder.mtvSenderName = (TextView) convertView.findViewById(R.id.left_tv_senderName);
				holder.mHtvTimeStampTime = (HandyTextView) convertView
						.findViewById(R.id.message_timestamp_htv_time);
				holder.mLayoutMessageContainer = (LinearLayout) convertView
						.findViewById(R.id.left_message_layout_messagecontainer);
				holder.mLayoutTimeContainer = (RelativeLayout) convertView
						.findViewById(R.id.message_layout_timecontainer);
				holder.mTvRecordTime = (TextView) convertView
						.findViewById(R.id.left_tv_recordtime);
				holder.mView = mInflater.inflate(R.layout.message_voice, null);

				holder.mIvVoiceImage = (ImageView) holder.mView
						.findViewById(R.id.voice_message_iv_msgimage);
				holder.mLayoutMessageContainer.addView(holder.mView);
				break;

			case TYPE_LEFT_FILE:
				convertView = mInflater.inflate(
						R.layout.message_group_receive_template, null);
				holder.mtvSenderName = (TextView) convertView.findViewById(R.id.left_tv_senderName);
				holder.mHtvTimeStampTime = (HandyTextView) convertView
						.findViewById(R.id.message_timestamp_htv_time);
				holder.mLayoutMessageContainer = (LinearLayout) convertView
						.findViewById(R.id.left_message_layout_messagecontainer);
				holder.mLayoutTimeContainer = (RelativeLayout) convertView
						.findViewById(R.id.message_layout_timecontainer);
				
				holder.mView = mInflater.inflate(R.layout.message_file, null);
				holder.mtvFileName = (TextView) holder.mView
						.findViewById(R.id.message_file_name);
				holder.mtvFileState = (TextView) holder.mView
						.findViewById(R.id.message_file_state);
				holder.mFileStateProgressBar = (ProgressBar) holder.mView
						.findViewById(R.id.message_file_progressbar);
				holder.mLayoutMessageContainer.addView(holder.mView);
				break;

			case TYPE_RIGHT_TEXT:
				convertView = mInflater.inflate(
						R.layout.message_group_send_template, null);
				holder.mtvSenderName = (TextView) convertView.findViewById(R.id.right_tv_senderName);
				holder.mHtvTimeStampTime = (HandyTextView) convertView
						.findViewById(R.id.message_timestamp_htv_time);
				holder.mLayoutMessageContainer = (LinearLayout) convertView
						.findViewById(R.id.right_message_layout_messagecontainer);
				holder.mLayoutTimeContainer = (RelativeLayout) convertView
						.findViewById(R.id.message_layout_timecontainer);
				holder.mView = mInflater.inflate(R.layout.message_text, null);

				holder.mEtvTextContent = (HandyTextView) holder.mView
						.findViewById(R.id.message_etv_msgtext);
				holder.mEtvTextContent.setTextColor(Color.WHITE);//add by lss
				holder.mLayoutMessageContainer.addView(holder.mView);
				break;

			case TYPE_RIGHT_IMAGE:
				convertView = mInflater.inflate(
						R.layout.message_group_send_template, null);
				holder.mtvSenderName = (TextView) convertView.findViewById(R.id.right_tv_senderName);
				holder.mHtvTimeStampTime = (HandyTextView) convertView
						.findViewById(R.id.message_timestamp_htv_time);
				holder.mLayoutMessageContainer = (LinearLayout) convertView
						.findViewById(R.id.right_message_layout_messagecontainer);
				holder.mLayoutTimeContainer = (RelativeLayout) convertView
						.findViewById(R.id.message_layout_timecontainer);
				holder.mView = mInflater.inflate(R.layout.message_image, null);
				holder.mIvImageContent = (ImageView) holder.mView
						.findViewById(R.id.message_iv_msgimage);
				holder.mLayoutMessageContainer.addView(holder.mView);
				break;

			case TYPE_RIGHT_VOICE:
				convertView = mInflater.inflate(
						R.layout.message_group_send_template, null);
				holder.mtvSenderName = (TextView) convertView.findViewById(R.id.right_tv_senderName);
				holder.mHtvTimeStampTime = (HandyTextView) convertView
						.findViewById(R.id.message_timestamp_htv_time);
				holder.mLayoutMessageContainer = (LinearLayout) convertView
						.findViewById(R.id.right_message_layout_messagecontainer);
				holder.mLayoutTimeContainer = (RelativeLayout) convertView
						.findViewById(R.id.message_layout_timecontainer);
				holder.mTvRecordTime = (TextView) convertView
						.findViewById(R.id.right_tv_recordtime);
				holder.mView = mInflater.inflate(R.layout.message_voice, null);

				holder.mIvVoiceImage = (ImageView) holder.mView
						.findViewById(R.id.voice_message_iv_msgimage);
				holder.mLayoutMessageContainer.addView(holder.mView);
				break;

			case TYPE_RIGHT_FILE:
				convertView = mInflater.inflate(
						R.layout.message_group_send_template, null);
				holder.mtvSenderName = (TextView) convertView.findViewById(R.id.right_tv_senderName);
				holder.mHtvTimeStampTime = (HandyTextView) convertView
						.findViewById(R.id.message_timestamp_htv_time);
				holder.mLayoutMessageContainer = (LinearLayout) convertView
						.findViewById(R.id.right_message_layout_messagecontainer);
				holder.mLayoutTimeContainer = (RelativeLayout) convertView
						.findViewById(R.id.message_layout_timecontainer);
				
				holder.mView = mInflater.inflate(R.layout.message_file, null);
				holder.mtvFileName = (TextView) holder.mView
						.findViewById(R.id.message_file_name);
				holder.mtvFileState = (TextView) holder.mView
						.findViewById(R.id.message_file_state);
				holder.mFileStateProgressBar = (ProgressBar) holder.mView
						.findViewById(R.id.message_file_progressbar);

				holder.mLayoutMessageContainer.addView(holder.mView);
				break;
			}
			
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if ((msg.getSendTime()).equals("")) {
			holder.mLayoutTimeContainer.setVisibility(View.GONE);
		} else {
			holder.mLayoutTimeContainer.setVisibility(View.VISIBLE);
		}
		
		if (isGroup) {
			holder.mtvSenderName.setText(msg.getSenderName());
			holder.mtvSenderName.setVisibility(View.VISIBLE);
		}
		
		switch (messageType) {
		case TYPE_LEFT_TEXT:
			dateShow(holder,msg);
			//holder.mHtvTimeStampTime.setText(msg.getSendTime());
			holder.mEtvTextContent.setText(msg.getMsgContent());
			break;

		case TYPE_LEFT_IMAGE:
			dateShow(holder,msg);
			showImage(msg, holder.mIvImageContent);
			break;

		case TYPE_LEFT_VOICE:
			holder.mTvRecordTime.setVisibility(View.VISIBLE);
			holder.mTvRecordTime.setText(msg.getRecordTime()+"''");
			dateShow(holder,msg);
			//holder.mHtvTimeStampTime.setText(msg.getSendTime());
			holder.mIvVoiceImage.setImageResource(R.drawable.voicerecord_left);
			break;

		case TYPE_LEFT_FILE:
			holder.mtvFileName.setText(FileUtils.getNameByPath(msg.getMsgContent()));
			holder.mFileStateProgressBar.setTag(msg.getId());
			holder.mFileStateProgressBar.setTag(ITEM_POSITION, position);
			holder.mtvFileState.setTag(msg.getId()+"state");
			if (msg.getPercent() != 100) {
				holder.mFileStateProgressBar.setVisibility(View.VISIBLE);
				holder.mtvFileState.setText(R.string.text_chat_file_receiving);
			}else {
				holder.mFileStateProgressBar.setVisibility(View.GONE);
				holder.mtvFileState.setText(R.string.text_chat_file_received);
			}
			
			if (msg.getPercent() == 100) {
				holder.mFileStateProgressBar.setVisibility(View.GONE);
				holder.mtvFileState.setText(R.string.text_chat_file_received);
			}else if (msg.getPercent() == -1) {
				holder.mFileStateProgressBar.setVisibility(View.GONE);
				holder.mtvFileState.setText(R.string.text_chat_file_receive_fail);
			}else {
				holder.mFileStateProgressBar.setVisibility(View.VISIBLE);
				holder.mtvFileState.setText(R.string.text_chat_file_receiving);
			}

			dateShow(holder,msg);
			//holder.mHtvTimeStampTime.setText(msg.getSendTime());

			break;

		case TYPE_RIGHT_TEXT:
			dateShow(holder,msg);
			//holder.mHtvTimeStampTime.setText(msg.getSendTime());
			holder.mEtvTextContent.setText(msg.getMsgContent());
			break;

		case TYPE_RIGHT_IMAGE:
			dateShow(holder,msg);
			showImage(msg, holder.mIvImageContent);
			break;

		case TYPE_RIGHT_VOICE:
			holder.mTvRecordTime.setVisibility(View.VISIBLE);
			holder.mTvRecordTime.setText(msg.getRecordTime()+"''");
			dateShow(holder,msg);
			//holder.mHtvTimeStampTime.setText(msg.getSendTime());
			holder.mIvVoiceImage.setImageResource(R.drawable.voicerecord_right);
			break;

		case TYPE_RIGHT_FILE:
			holder.mtvFileName.setText(FileUtils.getNameByPath(msg.getMsgContent()));
			holder.mFileStateProgressBar.setTag(msg.getId());
			holder.mFileStateProgressBar.setTag(ITEM_POSITION, position);
			holder.mtvFileState.setTag(msg.getId()+"state");
			
			if (msg.getPercent() == 100) {
				holder.mFileStateProgressBar.setVisibility(View.GONE);
				holder.mtvFileState.setText(R.string.text_chat_file_sent);
			}else if (msg.getPercent() == -1) {
				holder.mFileStateProgressBar.setVisibility(View.GONE);
				holder.mtvFileState.setText(R.string.text_chat_file_send_fail);
			}else {
				holder.mFileStateProgressBar.setVisibility(View.VISIBLE);
				holder.mtvFileState.setText(R.string.text_chat_file_sending);
			}
			
            dateShow(holder,msg);
            //holder.mHtvTimeStampTime.setText(msg.getSendTime());

			break;
		}

		return convertView;
	}

	/**
	 * 根据数据源的position返回需要显示的的layout的type
	 * 
	 * */
	@Override
	public int getItemViewType(int position) {

		Message msg = (Message) getItem(position);
		int type = -1;
		if (isGroup) {
			if (SessionUtils.isItGroupSelf(msg.getSenderIMEI())) {
				switch (msg.getContentType()) {
				case TEXT:
					type = TYPE_RIGHT_TEXT;
					break;

				case IMAGE:
					type = TYPE_RIGHT_IMAGE;
					break;

				case VOICE:
					type = TYPE_RIGHT_VOICE;
					break;

				case FILE:
					type = TYPE_RIGHT_FILE;
					break;
				}
			} else {
				switch (msg.getContentType()) {
				case TEXT:
					type = TYPE_LEFT_TEXT;
					break;

				case IMAGE:
					type = TYPE_LEFT_IMAGE;
					break;

				case VOICE:
					type = TYPE_LEFT_VOICE;
					break;

				case FILE:
					type = TYPE_LEFT_FILE;
					break;
				}
			}
		} else {
			if (SessionUtils.isItself(msg.getSenderIMEI())) {
				switch (msg.getContentType()) {
				case TEXT:
					type = TYPE_RIGHT_TEXT;
					break;

				case IMAGE:
					type = TYPE_RIGHT_IMAGE;
					break;

				case VOICE:
					type = TYPE_RIGHT_VOICE;
					break;

				case FILE:
					type = TYPE_RIGHT_FILE;
					break;
				}
			} else {
				switch (msg.getContentType()) {
				case TEXT:
					type = TYPE_LEFT_TEXT;
					break;

				case IMAGE:
					type = TYPE_LEFT_IMAGE;
					break;

				case VOICE:
					type = TYPE_LEFT_VOICE;
					break;

				case FILE:
					type = TYPE_LEFT_FILE;
					break;
				}
			}
		}

		return type;
	}

	public void updateFileState(android.os.Message msg){
		mHandler.sendMessage(msg);
	}
	
	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message paramMsg) {
			updateView(paramMsg);
		}
	};

	private void updateView(android.os.Message paramMsg) {
		FileState file = (FileState) paramMsg.obj;

		if (mListView != null) {
			ProgressBar FileStateProgressBar = (ProgressBar) mListView
					.findViewWithTag(file.id);
			TextView tvFileState = (TextView) mListView
					.findViewWithTag(file.id+"state");

			if (FileStateProgressBar != null) {
				int itemPosition = (Integer) FileStateProgressBar.getTag(ITEM_POSITION);
				int visiblePos = mListView.getFirstVisiblePosition();
				int offset = itemPosition - visiblePos;

				Message itemMessage = (Message) getItem(itemPosition);
				itemMessage.setPercent(file.percent);
				MyLog.e(TAG, "updateView file.percent" + file.percent);
				// 只有在可见区域才更新
				if (offset < 0)
					return;
				else {
					if (file.percent == 100) {
						if (SessionUtils.isItself(itemMessage.getSenderIMEI())) {
							tvFileState.setText(R.string.text_chat_file_sent);
						}else {
							tvFileState.setText(R.string.text_chat_file_received);
						}
						FileStateProgressBar.setVisibility(View.GONE);
					}else if (file.percent == -1) {
						if (SessionUtils.isItself(itemMessage.getSenderIMEI())) {
							tvFileState.setText(R.string.text_chat_file_send_fail);
						}else {
							tvFileState.setText(R.string.text_chat_file_receive_fail);
						}
						FileStateProgressBar.setVisibility(View.GONE);
					}else {
						tvFileState.setText(file.percent+"");
						FileStateProgressBar.setProgress(file.percent);
					}
				}

			}
		}
	}

	private Bitmap getImageBitmap(Message msg) {
		String imagePath = BaseMessageActivity.THUMBNAIL_PATH + File.separator
				+ FileUtils.getNameByPath(msg.getMsgContent());
		Bitmap bitmap = ImageUtils.getBitmapFromPath(imagePath);

		if (mImageContentBitmap == null)
			mImageContentBitmap = ImageUtils.getBitmapFromPath(msg
					.getMsgContent());
		return bitmap;
	}
	/**
	 * 显示日期
	 */
	public String dateShow(ViewHolder holder,Message msg){	
		holder.mHtvTimeStampTime.setText(msg.getSendTime());
		return null;	
	}
	

	/**
	 * 返回所有的layout的数量
	 * 
	 * */
	@Override
	public int getViewTypeCount() {
		return TYPE_COUNT;
	}

	static class ViewHolder {

		private TextView mtvSenderName;
		
		private HandyTextView mHtvTimeStampTime; // 时间
		private RelativeLayout mLayoutTimeContainer; // 时间容器
		private LinearLayout mLayoutMessageContainer; // 消息容器
		private View mView;

		private HandyTextView mEtvTextContent; // 文本内容
		private ImageView mIvImageContent; // 图像内容
		private ImageView mIvVoiceImage; // 声音图像
		private TextView mtvFileName; 
		private TextView mtvFileState;
		private ProgressBar mFileStateProgressBar;
		private TextView mTvRecordTime;
	}

	
	private RequestListener<String, GlideDrawable> requestListener = new RequestListener<String, GlideDrawable>() {

		@Override
		public boolean onException(Exception e, String mode,
				Target<GlideDrawable> target, boolean isFirstResource) {
			MyLog.i("glide", "Exception :" + e.toString());
			return false;
		}

		@Override
		public boolean onResourceReady(GlideDrawable arg0, String arg1,
				Target<GlideDrawable> arg2, boolean arg3, boolean arg4) {
			MyLog.i("glide", "onResourceReady");
			return false;
		}
		
	};
	
	private void showImage(Message msg, ImageView imageView){
		Glide.with(mContext)
		.load(msg.getMsgContent())
		.centerCrop()
		.override(200, 200).listener(requestListener)
		.placeholder(R.drawable.ic_loading)
		.error(R.drawable.ic_load_fail)
		.crossFade()
		.into(imageView);
	}
}
