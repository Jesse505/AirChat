package com.leadcore.sip.ui;

import java.util.ArrayList;
import java.util.List;

import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.adapter.GroupMemAdapter;
import org.sipdroid.sipua.adapter.OnlineMemAdapter;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.SessionUtils;
import org.sipdroid.sipua.widget.MyGridView;

import android.R.integer;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.leadcore.sip.login.LoginPersonAdapter;
import com.leadcore.sms.entity.Group;
import com.leadcore.sms.entity.Users;
import com.leadcore.sms.socket.udp.IPMSGConst;
import com.leadcore.sms.sql.SqlDBOperate;

public class GroupMembersActivity extends BaseActivity implements OnClickListener, OnItemClickListener {

	private SqlDBOperate mSqlDBOperate;
	private Group mGroup;
	
	private Button btn_back;
	private TextView tv_title;
	private MyGridView gridView_members;
	private OnlineMemAdapter adapter;
	private Button btn_clear_chatting_info;
	private Button btn_quit_group;
	
	private List<Users> members ;
	private int memberNum;
	private ArrayList<String> memberIMEI;       //群组成员的IMEI集合
	private List<Users> onlineMembers ;
	private int onlineMemNum;
	private ArrayList<String> onlineMemIMEI ;   //在线群组成员的IMEI集合
	private boolean isMaster = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_members);
		mSqlDBOperate = new SqlDBOperate(this);
		mGroup = (Group) getIntent().getSerializableExtra(Group.ENTITY_GROUOP);
		initViews();
		initEvents();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		init();
	}
	
	private void init(){
		getMembersInfo();
		setAdapter();
		tv_title.setText(getString(R.string.text_chat_group_info) + "(" + onlineMemNum + "/" + memberNum + ")");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mSqlDBOperate != null) {
			mSqlDBOperate.close();
			mSqlDBOperate = null;
		}
	}
	
	@Override
	protected void initViews() {
		btn_back = (Button) findViewById(R.id.btn_back);
		tv_title = (TextView) findViewById(R.id.tv_title);
		gridView_members = (MyGridView) findViewById(R.id.gridview_members);
		btn_clear_chatting_info = (Button) findViewById(R.id.btn_clear_chatting_info);
		btn_quit_group = (Button) findViewById(R.id.btn_quit_group);
	}

	@Override
	protected void initEvents() {
		btn_back.setOnClickListener(this);
		btn_clear_chatting_info.setOnClickListener(this);
		btn_quit_group.setOnClickListener(this);
		gridView_members.setOnItemClickListener(this);
	}

	@Override
	public void processMessage(Message msg) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * quit the group
	 * @param groupIp   the ip of the group to quit
	 */
	private void quitGroup(String groupIp){
		mApplication.removeOnlineGroup(groupIp);
		mSqlDBOperate.delGroupInfo(groupIp);
		mSqlDBOperate.delGroupMemInfo(groupIp);
		Receiver.engine(mContext).sendMultiSMSdata(IPMSGConst.IPMSG_GROUP_QUIT, groupIp);
		Receiver.engine(mContext).stopMultiProvider(groupIp);
		startActivity(TabHomeActivity.class);
	}
	/**
	 * only master of the group can dismiss group
	 */
	private void dismissGroup(String groupIp){
		mApplication.removeOnlineGroup(groupIp);
		mSqlDBOperate.delGroupInfo(groupIp);
		mSqlDBOperate.delGroupMemInfo(groupIp);
		Receiver.engine(mContext).sendMultiSMSdata(IPMSGConst.IPMSG_GROUP_DISMISS, groupIp);
		Receiver.engine(mContext).stopMultiProvider(groupIp);
		startActivity(TabHomeActivity.class);
	}
	
	private void getMembersInfo(){
		onlineMembers = new ArrayList<Users>();
		onlineMemIMEI = new ArrayList<String>();
		memberIMEI    = new ArrayList<String>();
		//如果本机不是是群主并且群主在线的时候
		if (!mGroup.getMasterID().equals(SessionUtils.getIMEI()) 
				&& mApplication.getOnlineUser(mGroup.getMasterID()) !=null) {
			List<Group> groups = mApplication.getOnlineUser(mGroup.getMasterID()).getmGroups();
			for (Group group : groups) {
				MyLog.i("group", "GroupMembersActivity.getMembersInfo group ip: " + group.getStrIP());
				if (group.getStrIP().equals(mGroup.getStrIP())) {
					members = group.getMembers();
					MyLog.i("group", "GroupMembersActivity.getMembersInfo members.size: " + members.size());
				}
			}
		}else {
			//获取数据库中保存的组员记录
			members = mSqlDBOperate.getMembersByGroupIP(mGroup.getStrIP());
		}
		
		//新建一个List用于保存删除的member
		List<Users> removedMembers = new ArrayList<Users>();
		for (Users member : members) {
			//获取在线组员并且询问他还是否在这个组里面
			Users user = mApplication.getOnlineUser(member.getIMEI());
			if (member.getIMEI().equals(SessionUtils.getIMEI())) {
				onlineMembers.add(user);
			}else {
				if (null != user) {
					if (user.getmGroupIps().contains(mGroup.getStrIP())) {
						onlineMembers.add(user);
					}else {
						removedMembers.add(member);
					}
					
				}
			}
			
		}
		members.removeAll(removedMembers);
		for (Users member : removedMembers) {
			mSqlDBOperate.delMemberInfo(member.getIMEI(), mGroup.getStrIP());
		}
		memberNum = members.size();
		onlineMemNum = onlineMembers.size();
		for (Users user : members) {
			memberIMEI.add(user.getIMEI());
		}
		for (Users user : onlineMembers) {
			onlineMemIMEI.add(user.getIMEI());
		}
		//如果是群主，需要添加删除组员和添加组员按钮
		if (SessionUtils.getIMEI().equals(mGroup.getMasterID())) {
			isMaster = true;
			onlineMembers.add(new Users("", "", ""));
			onlineMembers.add(new Users("", "", ""));
		}
	}
	
	private void setAdapter(){
        	adapter = new OnlineMemAdapter(this, onlineMembers, isMaster);
        	gridView_members.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_clear_chatting_info:
			mSqlDBOperate.deleteGroupChattingInfo(mGroup.getStrIP());
			mSqlDBOperate.deteleChatMessageGroupInfo(mGroup.getStrIP());//add by lss for delete group message when user quit or delete group
			break;
		case R.id.btn_quit_group:
			mSqlDBOperate.deleteGroupChattingInfo(mGroup.getStrIP());
			mSqlDBOperate.deteleChatMessageGroupInfo(mGroup.getStrIP());//add by lss for delete group message when user quit or delete group
			if (mGroup.getMasterID().equals(SessionUtils.getIMEI())) {
				dismissGroup(mGroup.getStrIP());
			}else {
				quitGroup(mGroup.getStrIP());
			}
			break;
		default:
			break;
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position == (onlineMembers.size()-1)) {
			Intent i = new Intent(this, MembersChooseActivity.class);
			i.putStringArrayListExtra("onlineMemIMEI",  onlineMemIMEI);
			i.putStringArrayListExtra("memberIMEI", memberIMEI);
			i.putExtra("type", "delete");
			i.putExtra("groupIP", mGroup.getStrIP());
			startActivity(i);
		}else if (position == (onlineMembers.size()-2)) {
			Intent i = new Intent(this, MembersChooseActivity.class);
			i.putStringArrayListExtra("onlineMemIMEI",  onlineMemIMEI);
			i.putStringArrayListExtra("memberIMEI", memberIMEI);
			i.putExtra("type", "add");
			i.putExtra("groupIP", mGroup.getStrIP());
			startActivity(i);
		}
		
	}

}
