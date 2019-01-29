package com.leadcore.sip.ui;

import java.util.List;
import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.adapter.GroupMemAdapter;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.utils.MyLog;

import com.leadcore.sip.login.AdhocManager;
import com.leadcore.sip.login.LoginPersonAdapter;
import com.leadcore.sip.login.NodeResource;
import com.leadcore.sms.entity.Group;
import com.leadcore.sms.entity.Users;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class TabContactActivity extends TabItemActivity implements OnItemClickListener,OnClickListener{

	private GridView gridView;
	
    private static final int WHAT_HANDLER_SCREEN_REFRESH = 0;
    private static final int WHAT_HANDLER_GROUP_REFRESH = 1;
    private LoginPersonAdapter adapter;
    private List<NodeResource> personList;
    private BroadcastReceiver screenRefresh;
    private Users mPeople; //User Instance
    
    private Button btnNewGroup;	
    private TextView txtTitle;
    private RelativeLayout tabContactTitle;//add by lss for UI
    private Button btn_recent_contact;
    private Button btn_all_contact;
    private EditText search_contact_person;
	private Boolean isBtnRecentSelected=true;
	private Boolean isBtnAllSelected=false;
	private ListView content_lv; 


   
    //add end
    
    private GroupMemAdapter groupMemAdapter;
    private List<Group> groupList;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		init();
		MyLog.e("zhaoyifei", "TabContactActivity onCreate");
	}

	public void init(){
		setContentView(R.layout.activity_tab_contact);
		//add by lss for UI title begin
		tabContactTitle=(RelativeLayout)findViewById(R.id.tab_contact_title);
		txtTitle=(TextView)tabContactTitle.findViewById(R.id.txt_title);
		txtTitle.setText(R.string.call_list);
		btnNewGroup = (Button) tabContactTitle.findViewById(R.id.btn_new_group);
		btnNewGroup.setVisibility(View.VISIBLE);
		btnNewGroup.setOnClickListener(this);
		
		search_contact_person=(EditText)findViewById(R.id.search_contact_person);
		search_contact_person.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent();
				intent.setClass(TabContactActivity.this, SearchPerson.class);
				startActivity(intent);
			}
			
		});
		content_lv=(ListView)findViewById(R.id.content_lv);
		btn_recent_contact=(Button)findViewById(R.id.recent_contact_person);
		btn_all_contact=(Button)findViewById(R.id.all_contact_person);
	
		//set init background
		btn_all_contact.setBackground(null);
		btn_recent_contact.setBackground(getResources().getDrawable(R.drawable.btnsearch_nor));
		btn_recent_contact.setOnClickListener(new OnClickListener(){

			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				btn_all_contact.setBackground(null);
				btn_recent_contact.setBackground(getResources().getDrawable(R.drawable.btnsearch_contacts));
				isBtnRecentSelected=true;
				isBtnAllSelected=false;
				setAdapter();
			}
			
		});
		
		btn_all_contact.setOnClickListener(new OnClickListener(){


			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				btn_recent_contact.setBackground(null);
				btn_all_contact.setBackground(getResources().getDrawable(R.drawable.btnsearch_contacts));
				isBtnRecentSelected=false;
				isBtnAllSelected=true;
				setAdapter();
			}
			
		});
		//add end
		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setOnItemClickListener(this);
		
        screenRefresh = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (AdhocManager.ACTION_SCREEN_REFRESH.equals(action)) {
                    handler.sendEmptyMessage(WHAT_HANDLER_SCREEN_REFRESH);
                }else if (AdhocManager.ACTION_GROUP_REFRESH.equals(action)) {
                	handler.sendEmptyMessage(WHAT_HANDLER_GROUP_REFRESH);
				}
            }
        };
        IntentFilter filter = new IntentFilter(AdhocManager.ACTION_SCREEN_REFRESH);
        filter.addAction(AdhocManager.ACTION_GROUP_REFRESH);
        registerReceiver(screenRefresh, filter);
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		MyLog.e("zhaoyifei", "TabContactActivity onResume");
		setAdapter();
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MyLog.e("zhaoyifei", "TabContactActivity onPause");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(screenRefresh);
		MyLog.e("zhaoyifei", "TabContactActivity onDestroy");
	}

    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_HANDLER_SCREEN_REFRESH:
                    personList = AdhocManager.getInstance(TabContactActivity.this).getList();                   
                    MyLog.i("zhaoyifei", "personList " + personList.size());
                    if ((null != adapter)) {
                    	adapter.setDataList(personList);
                        adapter.notifyDataSetChanged();
                         
					}
                    break;
                case WHAT_HANDLER_GROUP_REFRESH:
                	groupList = BaseActivity.mApplication.getOnlineGroups();
                	MyLog.i("group", "TabContactActivity_handler refresh group size:" + groupList.size());
                	if (null != groupMemAdapter) {
                		groupMemAdapter.setDataList(groupList);
                    	groupMemAdapter.notifyDataSetChanged();
					}
                    break; 
                  
                default:
                    break;
            }
        };
    };
    

 
 
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		if (isBtnRecentSelected) {
			goPerInfo(position);
		}else {
			goChatActivity(position);
		}
		
	}

	private void goChatActivity(int position){
		Group group = groupList.get(position);
		if (group == null) {
			Toast.makeText(this, "null", Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(Group.ENTITY_GROUOP, group);
		startActivity(intent);
	}
	
	private void goPerInfo(int position) {
		NodeResource data = personList.get(position);
		if (null == data) {
			Toast.makeText(this, "null", Toast.LENGTH_LONG).show();
			return;
		}
		mPeople = new Users();
		mPeople.setNickname(data.getDisplayName());
		mPeople.setIMEI(data.getNumber());
		mPeople.setIpaddress(data.getIndex());
		Intent intent = new Intent(this, PersonInfoActivity.class);
		intent.putExtra(Users.ENTITY_PEOPLE, mPeople);
		startActivity(intent);
	}
	
	
	@Override
	public void onClick(View v) {
		Intent i = new Intent(this, ContactsChooseActivity.class);
		startActivity(i);
	}
	
	private void setAdapter(){
        if (isBtnRecentSelected) {
        	adapter = new LoginPersonAdapter(this, personList);        	
            handler.sendEmptyMessage(WHAT_HANDLER_SCREEN_REFRESH);
            gridView.setAdapter(adapter);
            
		}else {
			groupMemAdapter = new GroupMemAdapter(this, groupList);
			handler.sendEmptyMessage(WHAT_HANDLER_GROUP_REFRESH);
			gridView.setAdapter(groupMemAdapter);
		}
	}
	
	public void deleteDB(View view){
		Receiver.engine(this).delGroupDatabase();
	}
	
	public void send(View view){
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				TcpFileServer.getInstance(TabContactActivity.this).newSendThread(
//						BaseApplication.IMAG_PATH + File.separator + "239.0.0.60"
//						+ File.separator + "test.jpg", "239.0.0.60",null);
//				TcpFileServer.getInstance(TabContactActivity.this).startServer();
//				try {
//					Thread.sleep(10000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				TcpFileServer.getInstance(TabContactActivity.this).sendFile();
//			}
//		}).start();
		
	}
	
	public void receive(View view){
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				TcpFileClient.getInstance(TabContactActivity.this).startReceive();
//				TcpFileClient.getInstance(TabContactActivity.this).receiveFile(BaseApplication.IMAG_PATH, "172.20.10.2");
//				
//			}
//		}).start();

	}
	
}
