package com.leadcore.sip.ui;

import java.util.ArrayList;
import java.util.List;

import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.adapter.MembersAddAdapter;
import org.sipdroid.sipua.adapter.MembersDelAdapter;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.NetworkUtil;
import org.sipdroid.sipua.utils.SessionUtils;

import com.dt.adhoc.service.IGroupInfoCfgRspListener;
import com.leadcore.sip.ui.ContactsChooseActivity.MyGroupInfoListener;
import com.leadcore.sms.entity.Group;
import com.leadcore.sms.entity.Users;
import com.leadcore.sms.socket.udp.IPMSGConst;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

public class MembersChooseActivity extends BaseActivity implements OnClickListener, OnItemClickListener{

	private Button btn_cancel, btn_ok;
	private ListView lv_contacts;
	private ArrayList<String> onlineMemIMEI;       //在线组员的IMEI集合
	private ArrayList<Users>  onlineMembers;	   //在线组员集合
	private ArrayList<String> memberIMEI;		   //旧组员的IMEI集合
	private ArrayList<Users>  newMembers;		   //新组员集合
	private ArrayList<Users>  onlineContacts;	   //在线成员集合
	private MembersAddAdapter membersAddAdapter;
	private MembersDelAdapter membersDelAdapter;
	private String type;
	private String mGroupIP;
	private List<Integer> del_select_index = new ArrayList<Integer>();
	private List<Integer> add_select_index = new ArrayList<Integer>();
	
	private static Object newGroupLock = new Object();
	private boolean isCreateGroupSuccess = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		initEvents();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		init();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void initViews() {
		setContentView(R.layout.activity_memberschoose);
		btn_cancel = (Button) findViewById(R.id.btn_cancel);
		btn_ok = (Button) findViewById(R.id.btn_ok);
		lv_contacts = (ListView) findViewById(R.id.lv_contacts);
		onlineMemIMEI = getIntent().getStringArrayListExtra("onlineMemIMEI");
		memberIMEI    = getIntent().getStringArrayListExtra("memberIMEI");
		type = getIntent().getStringExtra("type");
		mGroupIP = getIntent().getStringExtra("groupIP");
	}

	@Override
	protected void initEvents() {
		btn_cancel.setOnClickListener(this);
		btn_ok.setOnClickListener(this);
		lv_contacts.setOnItemClickListener(this);
	}
	
	private void init(){
		onlineMembers = new ArrayList<Users>();
		for (String imei : onlineMemIMEI) {
			if (BaseActivity.mApplication.getOnlineUser(imei) != null) {
				onlineMembers.add(BaseActivity.mApplication.getOnlineUser(imei));
			}
		}
		onlineContacts = (ArrayList<Users>) BaseActivity.mApplication.getOnlineUsers();
		membersDelAdapter = new MembersDelAdapter(this, onlineMembers);
		membersAddAdapter = new MembersAddAdapter(this, onlineMembers, onlineContacts);
		if (type.equals("delete")) {
			lv_contacts.setAdapter(membersDelAdapter);
		}else if(type.equals("add")){
			lv_contacts.setAdapter(membersAddAdapter);
		}
	}

	@Override
	public void processMessage(Message msg) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_cancel:
			finish();
			break;
		case R.id.btn_ok:
			if (type.equals("delete")) {
				for (int i = 0; i < del_select_index.size(); i++) {
					MyLog.i("zhaoyf", "del_select_index :"+del_select_index.get(i));
					Receiver.engine(mContext).sendSMSdata(IPMSGConst.IPMSG_GROUP_DEL_MEM,
							onlineMembers.get(del_select_index.get(i)).getIpaddress(),mGroupIP);
					Receiver.engine(mContext).delMemInfo(onlineMembers.get(del_select_index.get(i)).getIMEI(),
							mGroupIP);
				}
				finish();
			}else if (type.equals("add")) {
				addGroupMem();
			}
			break;
		default:
			break;
		}
	}
	
	private void addGroupMem(){
		if (NetworkUtil.network_type == NetworkUtil.ADHOC_NETWORK) {
			requestGroupInfo();
		}else if (NetworkUtil.network_type == NetworkUtil.WIFI) {
			addMember();
			finish();
		}
	}
	//配置自组网模块信息
	private void requestGroupInfo(){
		MyLog.i("jesse", "onPostExecute requestGroupInfo()");
		putAsyncTask(new AsyncTask<Void, Void, Boolean>(){

			@Override
			protected Boolean doInBackground(Void... params) {
				
				Receiver.engine(mContext).requestGroupInfoCfg(mGroupIP, getNewMemIpList(), new MyGroupInfoListener());
				synchronized (newGroupLock) {
					try {
						newGroupLock.wait(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
				return true;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showLoadingDialog(getString(R.string.dialog_creating_group));
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				dismissLoadingDialog();
				if (!isCreateGroupSuccess) {
					showShortToast(getString(R.string.create_group_failed));
					return;
				}
				MyLog.i("jesse", "onPostExecute addMember()");
				addMember();
				finish();
			}
		});
	}
	//获取新成员ip地址集合
	private List<String> getNewMemIpList() {
		List<Users> newMembers = new ArrayList<Users>();
		List<String> newMemIpList = new ArrayList<String>();
		// 获取旧成员集合
		for (String imei : memberIMEI) {
			Users member = Receiver.engine(mContext).getMemberInfo(imei,
					mGroupIP);
			if (null != member)
				newMembers.add(member);
		}
		// 获取新成员集合并更新本地数据库当前组成员
		for (int i = 0; i < add_select_index.size(); i++) {
			MyLog.i("zhaoyf", "add_select_index :" + add_select_index.get(i));
			Users member = onlineContacts.get(add_select_index.get(i));
			newMembers.add(member);
		}
		for (Users user : newMembers) {
			newMemIpList.add(user.getIpaddress());
		}
		return newMemIpList;
	}
	
	private void addMember(){
		newMembers       = new ArrayList<Users>();
		//获取旧成员集合
		for (String imei : memberIMEI) {
			Users member = Receiver.engine(mContext).getMemberInfo(imei, mGroupIP);
			if(null != member)
				newMembers.add(member);
		}
		//获取新成员集合并更新本地数据库当前组成员
		for (int i = 0; i < add_select_index.size(); i++) {
			MyLog.i("zhaoyf", "add_select_index :"+add_select_index.get(i));
			Users member = onlineContacts.get(add_select_index.get(i));
			member.setGroupIP(mGroupIP);
			Receiver.engine(mContext).addMemberInfo(member);
			newMembers.add(member);
			//更新在线成员所携带的群组列表信息      modify by zyf
			Users onlineMember = BaseActivity.mApplication.getOnlineUser(member.getIMEI());
			if (null != onlineMember) {
				onlineMember.getmGroupIps().add(mGroupIP);
			}
		}
		String mGroupName = BaseActivity.mApplication.getOnlineGroup(mGroupIP).getStrName();
		Group group = new Group(mGroupIP, mGroupName, SessionUtils.getIMEI());
		group.setMembers(newMembers);
		group.setMemberNum(newMembers.size());
		//通知新成员集合中的每个成员
		for (int i = 0; i < newMembers.size(); i++) {
			Users member = newMembers.get(i);
			Receiver.engine(mContext).sendSMSdata(IPMSGConst.IPMSG_GROUP_ADD_MEM,
					member.getIpaddress(),group);
			MyLog.i("jesse", "MembersChooseActivity_addMember member.ip : " + member.getIpaddress());
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		MyLog.i("zhaoyf", "onItemClick position:" + position);
		if (type.equals("delete")) {
			if (onlineMembers.get(position).getIMEI().equals(SessionUtils.getIMEI())) {
				return;
			}
			CheckBox checkBox = (CheckBox) view
					.findViewById(R.id.id_checkbox);

			checkBox.toggle();
			
			membersDelAdapter.map.put(position, checkBox.isChecked());
			
			if (checkBox.isChecked()) {
				del_select_index.add(position);
			}else {
				for (int i = 0; i < del_select_index.size(); i++) {
					if (position == del_select_index.get(i)) {
						del_select_index.remove(i);
					}
				}
				
			}
		}else if(type.equals("add")){
			if (onlineMembers.contains(onlineContacts.get(position))) {
				return;
			}
			CheckBox checkBox = (CheckBox) view
					.findViewById(R.id.id_checkbox);

			checkBox.toggle();
			
			membersAddAdapter.map.put(position, checkBox.isChecked());
			
			if (checkBox.isChecked()) {
				add_select_index.add(position);
			}else {
				for (int i = 0; i < add_select_index.size(); i++) {
					if (position == add_select_index.get(i)) {
						add_select_index.remove(i);
					}
				}
				
			}
		}
	}

	//the callback of create group
	public class MyGroupInfoListener extends IGroupInfoCfgRspListener.Stub{

		@Override
		public void onGroupInfoCfgRsp(boolean isSuccessful, String groupIP)
				throws RemoteException {
			isCreateGroupSuccess = isSuccessful;
			synchronized (newGroupLock) {
				newGroupLock.notifyAll();
			}
			
		}
		
	}
}
