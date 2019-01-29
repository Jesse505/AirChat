package com.leadcore.sip.ui;

import java.util.ArrayList;
import java.util.List;

import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.adapter.ContactsChooseAdapter;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.NetworkUtil;
import org.sipdroid.sipua.utils.SessionUtils;

import android.content.Intent;
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

import com.dt.adhoc.service.IGroupInfoCfgRspListener;
import com.leadcore.sip.login.AdhocManager;
import com.leadcore.sip.login.NodeResource;
import com.leadcore.sms.entity.Group;
import com.leadcore.sms.entity.Message.CONTENT_TYPE;
import com.leadcore.sms.entity.Users;
import com.leadcore.sms.socket.udp.IPMSGConst;
import com.leadcore.sms.sql.SqlDBOperate;

public class ContactsChooseActivity extends BaseActivity implements OnClickListener, OnItemClickListener{

	private Button btn_cancel, btn_ok;
	private EditText et_groupname,et_groupIP;
	private ListView lv_contacts;
	private ContactsChooseAdapter adapter;
	private List<NodeResource> datalist;
	private List<Integer> list_select_index;
	List<Users> members = new ArrayList<Users>();
	protected SqlDBOperate mDBOperate;
	
	private static Object newGroupLock = new Object();
	private boolean isCreateGroupSuccess = false;
	private static final String TAG = "ContactsChooseActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		initEvents();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != mDBOperate) {// 关闭数据库连接
			mDBOperate.close();
			mDBOperate = null;
		}
	}

	@Override
	protected void initViews() {
		setContentView(R.layout.activity_contactschoose);
		btn_cancel = (Button) findViewById(R.id.btn_cancel);
		btn_ok = (Button) findViewById(R.id.btn_ok);
		lv_contacts = (ListView) findViewById(R.id.lv_contacts);
		et_groupname = (EditText) findViewById(R.id.et_groupname);
		et_groupIP = (EditText) findViewById(R.id.et_groupip);
		datalist = AdhocManager.getInstance(this).getList();
		adapter = new ContactsChooseAdapter(this,datalist);
		list_select_index = new ArrayList<Integer>();
		list_select_index.add(0);
	}

	@Override
	protected void initEvents() {
		btn_cancel.setOnClickListener(this);
		btn_ok.setOnClickListener(this);
		lv_contacts.setAdapter(adapter);
		lv_contacts.setOnItemClickListener(this);
		mDBOperate = new SqlDBOperate(this);
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
			newGroup();
			break;
		default:
			break;
		}
		
	}
	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position == 0) {
			return;
		}
		CheckBox checkBox = (CheckBox) view
				.findViewById(R.id.id_checkbox);

		checkBox.toggle();
		
		adapter.map.put(position, checkBox.isChecked());
		
		if (checkBox.isChecked()) {
			list_select_index.add(position);
		}else {
			for (int i = 0; i < list_select_index.size(); i++) {
				if (position == list_select_index.get(i)) {
					list_select_index.remove(i);
				}
			}
			
		}
	}
	
	public void test(View view){
		MyLog.e("group", "handleMessage");
		com.leadcore.sms.entity.Message message = new com.leadcore.sms.entity.Message();
		message.setMsgContent("hello everyone");
		message.setReceiveIP(et_groupIP.getText().toString());
		message.setContentType(CONTENT_TYPE.TEXT);
		message.setSenderName(SessionUtils.getNickname());
		Receiver.engine(mContext).sendMultiSMSdata(IPMSGConst.IPMSG_SENDMSG, et_groupIP.getText().toString(), message);
	}
	
	private boolean isValidated(){
		if (groupName != null && !groupName.equals("") && groupIP != null && !groupIP.equals("")) {
			//nothing to do
		}else {
			showShortToast(R.string.toast_new_group_null);
			return false;
		}
		
		if (!NetworkUtil.isIpv4(groupIP)) {
			showShortToast(R.string.toast_new_group_ip_error);
			return false;
		}
		for (String ip : mApplication.getOnlineGroupIPs()) {
			if (groupIP.equals(ip)) {
				//groupIP is in use
				showShortToast(R.string.toast_new_group_ip_used);
				return false;
			}
		}
		return true;
	}
	

	
	String groupName;
	String groupIP;
	private void newGroup(){
		groupName = et_groupname.getText().toString();
		groupIP = et_groupIP.getText().toString();
		if (!isValidated()) {
			return;
		}
		if (NetworkUtil.network_type == NetworkUtil.ADHOC_NETWORK) {
			putAsyncTask(new AsyncTask<Void, Void, Boolean>(){

				@Override
				protected Boolean doInBackground(Void... params) {
					
					List<String> memberList = new ArrayList<String>();
					for (int i = 0; i < list_select_index.size(); i++) {
						if (null != datalist.get(list_select_index.get(i))) {
							memberList.add(datalist.get(list_select_index.get(i)).getIndex());
						}
					}
					Receiver.engine(mContext).requestGroupInfoCfg(groupIP, memberList, new MyGroupInfoListener());
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
						MyLog.i(TAG, "zhaoyifei_group requestGroupInfoCfg failed");
						return;
					}
					createGroup();
				}
			});
		}else if (NetworkUtil.network_type == NetworkUtil.WIFI) {
			createGroup();
		}

	}
	
	private void createGroup(){
		MyLog.i("group", "newGroup");
		Group group = new Group(groupIP, groupName, SessionUtils.getIMEI());
		group.setMemberNum(list_select_index.size());
		
		for (int i = 0; i < list_select_index.size(); i++) {
			Users member = new Users();
			member.setGroupIP(groupIP);
			if (null != datalist.get(list_select_index.get(i))) {
				member.setNickname(datalist.get(list_select_index.get(i)).getDisplayName());
				member.setIMEI(datalist.get(list_select_index.get(i)).getNumber());
				member.setIpaddress(datalist.get(list_select_index.get(i)).getIndex());
			}
			mDBOperate.addMemberInfo(member);
			members.add(member);
			//更新在线成员所携带的群组列表信息      modify by zyf
			Users onlineMember = BaseActivity.mApplication.getOnlineUser(member.getIMEI());
			if (null != onlineMember) {
				onlineMember.getmGroupIps().add(group.getStrIP());
			}
		}
		group.setMembers(members);
		for (int i = 0; i < list_select_index.size(); i++) {
			String ip = null;
			if (null != datalist.get(list_select_index.get(i))) {
				ip = datalist.get(list_select_index.get(i)).getIndex();
			}
			MyLog.i("group", "index>>> "+ list_select_index.get(i) + " ip>>> " + ip);
			Receiver.engine(mContext).sendSMSdata(IPMSGConst.IPMSG_NEW_GROUP, ip, group);
		}
		mDBOperate.addGroupInfo(group);
		BaseActivity.mApplication.addOnlineGroup(group);
		sendBroadcast(new Intent(AdhocManager.ACTION_GROUP_REFRESH));
		Intent i = new Intent(ContactsChooseActivity.this, ChatActivity.class);
		i.putExtra(Group.ENTITY_GROUOP, group);
		startActivity(i);
		finish();
	}
	//the callback of create group
	public class MyGroupInfoListener extends IGroupInfoCfgRspListener.Stub{

		@Override
		public void onGroupInfoCfgRsp(boolean isSuccessful, String groupIP)
				throws RemoteException {
			MyLog.i(TAG, "zhaoyifei_group onGroupInfoCfgRsp isSuccessful : " + isSuccessful);
			isCreateGroupSuccess = isSuccessful;
			synchronized (newGroupLock) {
				newGroupLock.notifyAll();
			}
			
		}
		
	}
}
