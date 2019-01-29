package com.leadcore.sip.ui;

import java.util.HashMap;
import java.util.List;

import org.sipdroid.sipua.R;
import org.sipdroid.sipua.UserAgent;
import org.sipdroid.sipua.adapter.CallAdapter;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.SessionUtils;
import com.leadcore.sip.login.AdhocManager;
import com.leadcore.sip.login.LogPersonAdapter;
import com.leadcore.sip.login.NodeResource;

import com.leadcore.sms.entity.Call;
import com.leadcore.sms.entity.Users;
import com.leadcore.sms.sql.SqlDBOperate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TabCallActivity extends TabItemActivity implements OnClickListener, OnItemClickListener {
	 private TextView txtTitle;
	 private RelativeLayout tabCallTitle;
	 private Button keyground;
	 LinearLayout keyground_relative;
	 private int key_visible=0x8;
	 Context mContext = this;
	 private static final int WHAT_HANDLER_SCREEN_REFRESH = 0;
	 private BroadcastReceiver screenRefresh;
	 private Users mPeople; //User Instance
	 protected SqlDBOperate mDBOperate;
	 protected List<Call> mCallList; // ͨ���б�
	 protected CallAdapter mAdapter;
	 EditText input_number;
	private static final HashMap<Integer, Character> mDisplayMap =new HashMap<Integer, Character>();
	private static final HashMap<Character, Integer> mToneMap =new HashMap<Character, Integer>();
	RelativeLayout top_layout;
	RelativeLayout dtmf_display;
	RelativeLayout dialpad;
	EditText mDigits;
	Thread t;
	Button call;
	Button video_call;
	private List<NodeResource> personList;
	private ListView callist;
	private LogPersonAdapter logAdapter;
	public int count_user;
	private String name;
	private TextView displayName;
    protected String mNickName;
    protected String mIMEI;
    protected String mLocalIP;
    protected int mID;
    protected int mSenderID;
    protected ListView mCalList;

   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		init();// add by lss for UI
		updateListview();
	}
	
	private void updateListview(){
		mCallList = mDBOperate.getScrollCallOfCallInfo(0, 50);
		mAdapter = new CallAdapter(this,mCallList);
		mAdapter.notifyDataSetChanged();
		mCalList.setAdapter(mAdapter);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		MyLog.i("tab", "TabCallActivity onResume!");
		mDigits.setText(mDigits.getText().toString().trim());
		updateListview();
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mDigits.setText(mDigits.getText().toString().trim());
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(screenRefresh);
		
		MyLog.d("lss","TabCallActivity onDestroy!");
		mDigits.setText(mDigits.getText().toString().trim());
	}
	
    
	//add by lss for UI begin
	public void init(){
		setContentView(R.layout.activity_tab_call);
		
		//add by lss for UI title begin
		tabCallTitle=(RelativeLayout)findViewById(R.id.tab_call_title);
		txtTitle=(TextView)tabCallTitle.findViewById(R.id.txt_title);
		txtTitle.setText(R.string.call_recent_list);
		//for keyground begin
		keyground=(Button)findViewById(R.id.keyground);
		keyground_relative=(LinearLayout)findViewById(R.id.dtmf_call);
	
		top_layout=(RelativeLayout)keyground_relative.findViewById(R.id.top);
		mDigits = (EditText) findViewById(R.id.digits);
        mDisplayMap.put(R.id.one, '1');
        mDisplayMap.put(R.id.two, '2');
        mDisplayMap.put(R.id.three, '3');
        mDisplayMap.put(R.id.four, '4');
        mDisplayMap.put(R.id.five, '5');
        mDisplayMap.put(R.id.six, '6');
        mDisplayMap.put(R.id.seven, '7');
        mDisplayMap.put(R.id.eight, '8');
        mDisplayMap.put(R.id.nine, '9');
        mDisplayMap.put(R.id.zero, '0');
        mDisplayMap.put(R.id.pound, '#');
        mDisplayMap.put(R.id.star, '*');
               
        mToneMap.put('1', ToneGenerator.TONE_DTMF_1);
        mToneMap.put('2', ToneGenerator.TONE_DTMF_2);
        mToneMap.put('3', ToneGenerator.TONE_DTMF_3);
        mToneMap.put('4', ToneGenerator.TONE_DTMF_4);
        mToneMap.put('5', ToneGenerator.TONE_DTMF_5);
        mToneMap.put('6', ToneGenerator.TONE_DTMF_6);
        mToneMap.put('7', ToneGenerator.TONE_DTMF_7);
        mToneMap.put('8', ToneGenerator.TONE_DTMF_8);
        mToneMap.put('9', ToneGenerator.TONE_DTMF_9);
        mToneMap.put('0', ToneGenerator.TONE_DTMF_0);
        mToneMap.put('#', ToneGenerator.TONE_DTMF_P);
        mToneMap.put('*', ToneGenerator.TONE_DTMF_S);        
        View button;
        for (int viewId : mDisplayMap.keySet()) {
            button = findViewById(viewId);
            button.setOnClickListener(this);
            
        }
             
        call=(Button)findViewById(R.id.call);
        video_call=(Button)findViewById(R.id.vedio);
       
        	
		keyground.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(key_visible==0x8)
				{
					keyground_relative.setVisibility(View.VISIBLE);
					key_visible=0x0;
					mCalList.setVisibility(View.GONE);
					
				}
				else if(key_visible==0x0)        
				{
					keyground_relative.setVisibility(View.GONE);
					key_visible=0x8;
					mCalList.setVisibility(View.VISIBLE);
					
				}	
			}
		});
		
		callist=(ListView)findViewById(R.id.search_callist);
		callist.setOnItemClickListener(this);
		logAdapter = new LogPersonAdapter(this, personList);
		handler.sendEmptyMessage(WHAT_HANDLER_SCREEN_REFRESH);
		callist.setAdapter(logAdapter);
		callist.setVisibility(View.GONE);
		set_eSearch_TextChanged();
		screenRefresh = new BroadcastReceiver(){
	            @Override
	            public void onReceive(Context context, Intent intent) {
	                String action = intent.getAction();
	                if (AdhocManager.ACTION_SCREEN_REFRESH.equals(action)) {
	                    handler.sendEmptyMessage(WHAT_HANDLER_SCREEN_REFRESH);
	                }
	            }
	        };
	        IntentFilter filter = new IntentFilter(AdhocManager.ACTION_SCREEN_REFRESH);
	        registerReceiver(screenRefresh, filter);
	        call.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					for(int info=0;info<count;info++){
						person(info);
						MyLog.d("lss","info="+info+":::count="+count);
					}
					//modify by lss for vedio call bug when number equals null
					if((!"".equals(mDigits.getText().toString().trim()))&&
							(mDigits.getText().toString().trim()!=null)){						
						if(!"".equals(name) && name!=null)
						{
							Receiver.engine(mContext).call(mDigits.getText().toString().trim(),ip,UserAgent.VoiceCall);
						}
						else{
							showShortToast(R.string.notification_phone_number);						
						}	
					}else{
						showShortToast(R.string.screen_login_number);	
					}
					//modify end								
					ip="127.0.0.1";	
					mAdapter.notifyDataSetChanged();	
				}	
	        });
	        
	        video_call.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					for(int info=0;info<count;info++){
						person(info);
						MyLog.d("lss","info="+info+":::count="+count);
					}
					//modify by lss for vedio call bug when number equals null
					if((!"".equals(mDigits.getText().toString().trim()))&&
							(mDigits.getText().toString().trim()!=null)){
						Receiver.engine(mContext).call(mDigits.getText().toString().trim(),ip,UserAgent.VideoCall);
					}else{
						showShortToast(R.string.screen_login_number);	
					}
					//modify end					
					ip="127.0.0.1";			
					mAdapter.notifyDataSetChanged();	
				}	        	
	        });	        
	        //��ȡ������Ϣ	        	    	
			//get call ListView info
			mCalList = (ListView) findViewById(R.id.call_clv_list);
			mDBOperate = new SqlDBOperate(this);
			mCallList = mDBOperate.getScrollCallOfCallInfo(0, 50);
			//modify by lss for modifying the activity of calllog show senderName
			mAdapter = new CallAdapter(this,mCallList);
			mCalList.setAdapter(mAdapter);
			mCalList.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					Call callItem = mCallList.get((int) id);					
					Receiver.engine(mContext).call(callItem.getReceiveIMEI(),callItem.getReceiveIP(),UserAgent.VoiceCall);
				}
				
			});
			//add by lss for delete single call info
			mCalList.setOnItemLongClickListener(new OnItemLongClickListener(){

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					final Call callItem = mCallList.get((int) id);
					final long callId = callItem.getId();
					CustomDialog.Builder builder = new CustomDialog.Builder(TabCallActivity.this);
					builder.setMessage(R.string.alert_clear_single);
					builder.setTitle(R.string.clear_single_call);
					builder.setPositiveButton(R.string.clear_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							
							mDBOperate.deteleCallInfo(mDBOperate.getCallInfoByID(callId));
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
				
			});
	}
	
	
    public void setLvSelection(int position) {
        mCalList.setSelection(position);
    }
	String number;
	private void person(int info){
		NodeResource data = personList.get(info);
		if (null == data) {
			Toast.makeText(this, "null", Toast.LENGTH_LONG).show();
			return;
		}
		if(data.getNumber().equals(mDigits.getText().toString().trim()))
		{ 	
			ip=data.getIndex();
			number=data.getNumber();
			name=data.getDisplayName();			
		}
		
	}
	private String ip="127.0.0.1";
	private int count;
	private Handler handler = new Handler(){
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	                case WHAT_HANDLER_SCREEN_REFRESH:
	                   personList = AdhocManager.getInstance(TabCallActivity.this).getList();
	                   logAdapter.setDataList(personList);
	                   logAdapter.notifyDataSetChanged();	                   
	                   count=callist.getCount();
	                   break;

	                default:
	                    break;
	            }
	        };
	    };
	   
 
	Button ivDeleteText;
	private void set_eSearch_TextChanged()  
	{  
	     
		ivDeleteText=(Button)findViewById(R.id.ivDeleteText);
		ivDeleteText.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub				
				 String str = mDigits.getText().toString();
				 
			      if (str!= null && !str.equals("")){   
			    String string = new String();   
			     if (str.length() > 1){     
			   string =   str.substring(0, str.length() - 1);    
			    }else {    
			      string =  null;   
			   }   
			   mDigits.setText(string);
			   displayName.setText("");}
			}
			
		});
	    mDigits.addTextChangedListener(new TextWatcher() {  
	              
	         @Override  
	         public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {  
	               // TODO Auto-generated method stub  
	               
	         }  
	              
	         @Override  
	         public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,  
	                        int arg3) {  
	               // TODO Auto-generated method stub  
	               //?????????????  
	         }  
	              
	         @Override  
	         public void afterTextChanged(Editable s) {  
	               // TODO Auto-generated method stub  
	              
	               if(s.length() == 0){  
	                     ivDeleteText.setVisibility(View.GONE);//???????,?????  
	               }  
	               else {  
	                     ivDeleteText.setVisibility(View.VISIBLE);//????????,????  
	                     for(int info=0;info<count;info++){
	             			person(info);
	             			MyLog.d("lss"," displayName info="+info+":::displayName count="+count);
	             		}
	             		displayName=(TextView)findViewById(R.id.txt_name);
	             		displayName.setText(name);
	             		displayName.setVisibility(View.VISIBLE);
	             		name="";
	               }  
	         }  
	    });  
	          
	}  
	
	
	public void onClick(View v) {
        int viewId = v.getId();

        // if the button is recognized
        if (mDisplayMap.containsKey(viewId)) {                    
        	appendDigit(mDisplayMap.get(viewId));                    
        }
    }
	private boolean isContains=false;
	
    void appendDigit(final char c) {
        mDigits.getText().append(c);      
    } 

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		goPerInfo(position);
	}
	
	private void goPerInfo(int position) {
		NodeResource data = personList.get(position);
		if (null == data) {
			Toast.makeText(this, "null", Toast.LENGTH_LONG).show();
			return;
		}
		if(data.getNumber().equals(mDigits.getText().toString().trim()))
		{		
			mPeople = new Users();
			mPeople.setNickname(data.getDisplayName());
			mPeople.setIMEI(data.getNumber());
			mPeople.setIpaddress(data.getIndex());						
		}
	}
	
	@Override
	public void finish() {
		if (null != mDBOperate) {
			mDBOperate.close();
			mDBOperate = null;
		}
		super.finish();
	}
}
