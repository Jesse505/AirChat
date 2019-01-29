package com.leadcore.sip.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.SessionUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.leadcore.sip.login.AdhocManager;
import com.leadcore.sip.login.NodeResource;
import com.leadcore.sip.login.SearchLoginPersonAdapter;
import com.leadcore.sms.entity.Users;

public class SearchPerson extends BaseActivity   {

	private EditText search_contact_person;
	private SearchLoginPersonAdapter adapter1;
	private ListView content_lv; 
    List<NodeResource> tmpList = new ArrayList();
    private static final int MSG_SINGAL = 1001;  
    private static final String MSG_KEY = "SearchPerson.MSG_KEY";  
    private static final int WHAT_HANDLER_SCREEN_REFRESH = 0;
    private BroadcastReceiver screenRefresh;
    private Users mPeople; //User Instance
    private List<NodeResource> personList;
    boolean isInclude=false;
    boolean isIncludeName=false;
    private Button back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		
	}
	private void init(){
		setContentView(R.layout.activity_search_person);
		back=(Button)findViewById(R.id.btn_back);
		back.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
				
			}
			
		});
		search_contact_person=(EditText)findViewById(R.id.search_contact_person);
		content_lv=(ListView)findViewById(R.id.content_lv);
		content_lv.setVisibility(View.GONE);
		content_lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				goTmpInfo(position);
			}
			
		});
		search_contact_person.addTextChangedListener(new TextWatcher() {  
            @Override  
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  
            	
            }  
  
            @Override  
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	
            		Message msg = new Message();  
                    msg.what = MSG_SINGAL;  
                    Bundle data = new Bundle();  
                    data.putString(MSG_KEY, s.toString());  
                    msg.setData(data);  
                    handler.sendMessage(msg);  
                    MyLog.d("lss", "send:" + s.toString()); 
  
            }  
  
            @Override  
            public void afterTextChanged(Editable s) {  
            	
            		tmpList.clear();
            	
            }  
        });
		
         
		
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
		
	}
	 private Handler handler = new Handler(){
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case WHAT_HANDLER_SCREEN_REFRESH:
                    personList = AdhocManager.getInstance(SearchPerson.this).getList();
                    refreshListView(msg.getData().getString(MSG_KEY));    
                	/*tmpList.clear() ;*/
                    break;
                case MSG_SINGAL: 
                    	refreshListView(msg.getData().getString(MSG_KEY));                    	
                    break; 	                 
	                default:
	                    break;
	            }
	        };
	    };

	    private void refreshListView(String searchString) {  
	    	
	        MyLog.d("lss", "get searchString=" + searchString);  	        
	        if (searchString == null || searchString.trim().length() == 0) {
	            	           
	        	content_lv.setVisibility(View.GONE);
	        	
	        }
	        else{
	        	
	        	content_lv.setVisibility(View.VISIBLE);		        
		        for (NodeResource data : personList) {
		        	MyLog.d("lss","search person list"+personList+":::data="+data);
		        	isInclude=include(data.getNumber(),searchString);
		        	if((searchString.equals(R.string.local))||
		        			(searchString.equals(SessionUtils.getIMEI()))){
		        		isIncludeName=true;
		        	}else{
		        		
		        		isIncludeName=include(data.getDisplayName(),searchString);
		        	}
		        	
		        	MyLog.d("lss","isInclude="+isInclude+"searchString="+searchString
		        			+"isIncludeName="+isIncludeName);		        	
		            if (isInclude||isIncludeName) {
		            	
		            	tmpList.add(data); 	
		            }
		            else{
		            	
		            	tmpList.clear();
		            }
		           
		        }   
		        MyLog.d("lss","tmpList"+tmpList.toString());		      
		        adapter1=new SearchLoginPersonAdapter(this,tmpList);
		        adapter1.setDataList(tmpList);
		        content_lv.setAdapter(adapter1);
		        adapter1.notifyDataSetChanged();
		        content_lv.invalidateViews(); 
		        
	        }
  
	    }  
	    
	    /**
		    * 
		    * 
		    * @param str1 原字符串
		    * @param str2输入字符串
		    * @return
		    */
		public static boolean include(String str1,String str2){
	       char c[] = str2.toCharArray();
	       int j=0;
	       int a=0;
	       if (str2.length()>str1.length()) {
	           return false;
	       }else {
	           for (int i = 0; i < c.length; i++) {

	              while (j < str1.length()) {
	                  String string = str1.substring(j, str1.length());
	                  if (string.indexOf(c[i]) != -1) {
	                     j += string.indexOf(c[i]);
	                     j++;
	                     a++;
	                     break;
	                  }
	                  j++;
	              }
	              if (j >= str1.length() && a < c.length) {
	                  break;
	              }
	              
	           }

	           if (a<c.length) {
	              return false;
	           }
	       }

	       return true;
	    }
	 
	private void goTmpInfo(int position) {
		NodeResource data = tmpList.get(position);
		if (null == data) {
			Toast.makeText(this, "null", Toast.LENGTH_LONG).show();
			return;
		}
		String uri = data.getUri();
		Toast.makeText(this, uri, Toast.LENGTH_LONG).show();
		MyLog.e("item", "ip " + data.getIndex());
		MyLog.e("item", "number " + data.getNumber());
		MyLog.e("item", "name " + data.getDisplayName());
		mPeople = new Users();
		mPeople.setNickname(data.getDisplayName());
		mPeople.setIMEI(data.getNumber());
		mPeople.setIpaddress(data.getIndex());
		Intent intent = new Intent(this, PersonInfoActivity.class);
		intent.putExtra(Users.ENTITY_PEOPLE, mPeople);
		startActivity(intent);
	}
	
	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void initEvents() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void processMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}
	private void setAdapter(){
		
		adapter1=new SearchLoginPersonAdapter(this,tmpList);
        handler.sendEmptyMessage(WHAT_HANDLER_SCREEN_REFRESH);
        adapter1.setDataList(tmpList);
        content_lv.setAdapter(adapter1);
        adapter1.notifyDataSetChanged();
        refreshListView(search_contact_person.getText().toString());    
     /*   content_lv.invalidateViews(); */
	
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		MyLog.d("lss", "SearchPerson onResume");
		setAdapter();
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(screenRefresh);
		MyLog.d("lss", "SearchPerson onDestroy");
	}
}
