package com.leadcore.sip.ui;

import java.util.ArrayList;
import java.util.List;

import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.adapter.MessageAdapter;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.SessionUtils;

import com.leadcore.sip.login.AdhocManager;
import com.leadcore.sms.entity.Group;
import com.leadcore.sms.entity.Message;
import com.leadcore.sms.entity.Users;
import com.leadcore.sms.sql.ChattingInfo;
import com.leadcore.sms.sql.SqlDBOperate;
import com.leadcore.sms.sql.UserInfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * 
 * @author liushasha
 *
 */
public class TabMessageActivity extends TabItemActivity {

	 private TextView txtTitle;//add by lss for UI
	 private RelativeLayout tabMessageTitle;//add by lss for UI 
	 protected List<Message> mMessageList; //�����б�	 
	 protected List<Message> mReadPeople;
	 protected MessageAdapter mAdapter;
	 protected MessageAdapter mess;
	 private ListView messageList;
	 protected SqlDBOperate mDBOperate;
	 private BroadcastReceiver broadcastReceiver;
	 public static final String ACTION_MESSAGELIST_REFRESH = "com.example.message_list.screen.refresh";
	 private Users mPeople;
	 public static int flag=0;
	 private ChattingInfo info ;
	 protected String mNickName;
     protected String mIMEI;
     protected String mLocalIP;
     protected int mID;
     protected  static int mSenderID=0;
     List<Message> mM = new ArrayList<Message>();//add by lss for processing group null
     private static final int WHAT_HANDLER_GROUP_REFRESH = 1;//add by lss for processing group null
     
	 @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		init();
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		
		updateListview();
		super.onResume();
		MyLog.i("tab", "TabMessageActivity onResume");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
		
	}
	@Override
	public void finish(){
		if (null != mDBOperate) {// �ر���ݿ�l��
			mDBOperate.close();
			mDBOperate = null;
		}
		super.finish();
		
	}

	
	private void updateListview(){
		
		mMessageList = mDBOperate. getScrollChatMessageOfChattingInfo(1,mID);
		//add by lss for group null		
		mAdapter = new MessageAdapter(this,mMessageList,mIMEI);//modify  by lss for group null
		//add end
		mAdapter.notifyDataSetChanged();
		messageList.setAdapter(mAdapter);
		MyLog.d("lss","mSender="+mSenderID);
	}
	
	//add by lss for UI begin
	public void init(){
		setContentView(R.layout.activity_tab_message);
		//add by lss for UI title begin
		tabMessageTitle=(RelativeLayout)findViewById(R.id.tab_message_title);
		txtTitle=(TextView)tabMessageTitle.findViewById(R.id.txt_title);
		txtTitle.setText(R.string.message_list);
		//add end
	
		mID = SessionUtils.getLocalUserID();
		mNickName = SessionUtils.getNickname();
		mIMEI = SessionUtils.getIMEI();
		mLocalIP = SessionUtils.getLocalIPaddress();

		messageList = (ListView) findViewById(R.id.message_clv_list);
		mDBOperate = new SqlDBOperate(this);
		mMessageList = mDBOperate. getScrollChatMessageOfChattingInfo(1,mID);	 
		mAdapter = new MessageAdapter(this,mMessageList,mIMEI);//modify  by lss for group null
		MyLog.d("lss"," myh mMessageList==="+mMessageList);
		messageList.setAdapter(mAdapter);
		messageList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Message MessageItem;
				MessageItem = mMessageList.get((int) id);
				
				//add by lss for adding group message in message list					
				if((!"".equals(MessageItem.getReceiveIP()))&&(MessageItem.getReceiveIP()!=null)){
					//group
					MyLog.d("lss","group MessageItem"+MessageItem/*+"::MessageItem.getReceiverIMEI()"+MessageItem.getReceiverIMEI()*/);
					//modify by lss for message activity die
					int groupId=mDBOperate.getGroupIDByIP(MessageItem.getReceiveIP());//根据ID获取IP
					Group group=mDBOperate.getGroupById(groupId);//根据IP获取Group
					String strName=group.getStrName();//获取group name
                   /* mGroup.setStrName(BaseActivity.mApplication.getOnlineGroup(MessageItem.getReceiveIP()).getStrName()); */
					//modify end
					info=mDBOperate.getChatMessageInfoByID(MessageItem.getId());
					MyLog.d("lss","info="+info);
					info.setReadStatus(1);
					mDBOperate.updateChatReadStatusInfo(info);	
					
					Intent intent = new Intent(TabMessageActivity.this, ChatActivity.class);
			        intent.putExtra(Group.ENTITY_GROUOP, group);
			        startActivity(intent);
				}
				else{
					
			      //not group
					mPeople = new Users();
					UserInfo user;
					if(MessageItem.getSenderIMEI().contains(mIMEI)){
						
						user=mDBOperate.getUserInfoByIMEI(MessageItem.getReceiverIMEI());
						mPeople.setNickname(getResources().getString(R.string.local));
					}
					else{ 
						
						user=mDBOperate.getUserInfoByIMEI(MessageItem.getSenderIMEI());
						mPeople.setNickname(user.getName());
					}
					
					mPeople.setNickname(user.getName());
					mPeople.setIMEI(user.getIMEI());
					mPeople.setIpaddress(user.getIPAddr());	
					info=mDBOperate.getChatMessageInfoByID(MessageItem.getId());
					info.setReadStatus(1);
					mDBOperate.updateChatReadStatusInfo(info);
					
					Intent intent = new Intent(TabMessageActivity.this, ChatActivity.class);
			        intent.putExtra(Users.ENTITY_PEOPLE, mPeople);
			        startActivity(intent);
				}	
			}			
		});
		//add by lss for long click delete message info
		messageList.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Message MessageItem = mMessageList.get((int) id);
				final long messageId=MessageItem.getId();
				//add by lss for adding group message in message list
				if((!"".equals(MessageItem.getReceiveIP()))&&(MessageItem.getReceiveIP()!=null)){
					//group
					CustomDialog.Builder builder = new CustomDialog.Builder(TabMessageActivity.this);
					builder.setMessage(R.string.alert_clear_single);
					builder.setTitle(R.string.clear_single_info);
					builder.setPositiveButton(R.string.clear_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {				
							mDBOperate.deteleChatMessageInfo(mDBOperate.getChatMessageInfoByID(messageId));
							updateListview();
							dialog.dismiss();  
						}
					});

					builder.setNegativeButton(R.string.clear_cancel,
							new android.content.DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();  
								}
							});

					builder.create().show();
					return false;
				}
				else{
					//not group
					CustomDialog.Builder builder = new CustomDialog.Builder(TabMessageActivity.this);
					builder.setMessage(R.string.alert_clear_single);
					builder.setTitle(R.string.clear_single_info);
					builder.setPositiveButton(R.string.clear_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {							
							mDBOperate.deteleChatMessageInfo(mDBOperate.getChatMessageInfoByID(messageId));
							updateListview();
							dialog.dismiss();  
						}
					});

					builder.setNegativeButton(R.string.clear_cancel,
							new android.content.DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();  
								}
							});

					builder.create().show();
					return false;
				}
			}
			
		});
	
		//���ն��ŷ��͹㲥����MessageList
		broadcastReceiver=new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action=intent.getAction();
				if(action.equals(ACTION_MESSAGELIST_REFRESH)){					
					updateListview();					
					MyLog.d("lss","updateListMessage!");
				}else if(AdhocManager.ACTION_GROUP_REFRESH.equals(action)){
					handler.sendEmptyMessage(WHAT_HANDLER_GROUP_REFRESH);
				}
			}			
		};
		 IntentFilter localIntentFilter = new IntentFilter();
	     localIntentFilter.addAction(ACTION_MESSAGELIST_REFRESH);
	     localIntentFilter.addAction(AdhocManager.ACTION_GROUP_REFRESH);//add by lss for group null
	     registerReceiver(this.broadcastReceiver, localIntentFilter);    
	}
	//add by lss for group null
	 private List<Group> groupList;
	private Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {               
                case WHAT_HANDLER_GROUP_REFRESH:
                	groupList = BaseActivity.mApplication.getOnlineGroups();
                	MyLog.i("group", "TabContactActivity_handler refresh group size:" + groupList.size());
                	if (null != mAdapter) {
                		mMessageList = mDBOperate. getScrollChatMessageOfChattingInfo(1,mID);	                 		
                		mAdapter = new MessageAdapter(TabMessageActivity.this,mMessageList,mIMEI);//modify  by lss for group null               
                		MyLog.d("lss"," myh mMessageList==="+mMessageList);
                		messageList.setAdapter(mAdapter);
					}
                    break;                   
                default:
                    break;
            }
        };
    };
  //add end
    
}
