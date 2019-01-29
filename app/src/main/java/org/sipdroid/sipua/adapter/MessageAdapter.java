package org.sipdroid.sipua.adapter;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.utils.MyLog;

import com.leadcore.sms.entity.Group;
import com.leadcore.sms.entity.Message;
import com.leadcore.sms.entity.Message.CONTENT_TYPE;
import com.leadcore.sms.sql.GroupInfo;
import com.leadcore.sms.sql.SqlDBOperate;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * 
 * @author liushasha
 *
 */
public class MessageAdapter extends BaseAdapter {

	private static String TAG = "MessageAdapter";
	
	private List<Message> dataList;
	protected SqlDBOperate mDBOperate;
    public Map<Integer, Boolean> map;
    public Context mContext;
    private LayoutInflater layoutInflater;
    private String mIMEI;
    
    public MessageAdapter(Context context, List<Message> data) {

    	dataList = data;
    	
    	layoutInflater = LayoutInflater.from(context);
    	map = new HashMap<Integer, Boolean>();
    	for (int i = 0; i < data.size(); i++) {
    		
			map.put(i, false);

		}
    	map.put(0, true);
 
    }
    
    public MessageAdapter(Context context, List<Message> data,String IMEI) {

    	dataList = data;
    	layoutInflater = LayoutInflater.from(context);
    	mIMEI=IMEI;
    	map = new HashMap<Integer, Boolean>();
    	for (int i = 0; i < data.size(); i++) {
    		
			map.put(i, false);

		}
    	map.put(0, true);
 
    }
   

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		
		return dataList == null ? 0 : dataList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		
		return dataList == null ? null : dataList.get(position);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;		
		if (convertView == null) {

			 	holder = new ViewHolder();
				convertView = layoutInflater.inflate(
						R.layout.simple_message_list, null);
				holder.mLayoutCall=(LinearLayout)convertView.findViewById(R.id.rel_message_send);
				holder.mtvReceiverName = (TextView) convertView.findViewById(R.id.txt_name);
				holder.messageContent=(TextView)convertView.findViewById(R.id.txt_message_content);
				holder.date=(TextView)convertView.findViewById(R.id.txt_date);
				holder.readPeople=(ImageView)convertView.findViewById(R.id.txt_read_status);
				holder.mLayoutCall.setVisibility(View.VISIBLE);				
			    convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}	
		Message bean = dataList.get(position);

		setData(holder, bean);
		
        return convertView;	
	}
	
	private void setDataType(ViewHolder viewHolder, Message data){
		
		
		if(data.getContentType()==CONTENT_TYPE.TEXT){
			 viewHolder.messageContent.setText(data.getMsgContent());
		 }
		 else if(data.getContentType()==CONTENT_TYPE.IMAGE){
			 viewHolder.messageContent.setText(R.string.img_set_message);
		 }
		 else if(data.getContentType()==CONTENT_TYPE.FILE){
			 viewHolder.messageContent.setText(R.string.fill_set_message);
		 }
		 else if(data.getContentType()==CONTENT_TYPE.VOICE){
			 viewHolder.messageContent.setText(R.string.voice_set_message);
		 }
   
	}
	//add by lss for adding group message in message list
	private void setDataType(ViewHolder viewHolder, Message data,String group){
		
		String sendIMEI=data.getSenderIMEI();
		String content;
		//add by lss for processing the situation which messageContent is null
		if(data.getContentType() != null){
		if(data.getContentType()==CONTENT_TYPE.TEXT){			
			viewHolder.messageContent.setText(sendIMEI+layoutInflater.getContext().getResources().getString(R.string.logo)+data.getMsgContent());
		 }
		 else if(data.getContentType()==CONTENT_TYPE.IMAGE){			 
			 viewHolder.messageContent.setText(sendIMEI+layoutInflater.getContext().getResources().getString(R.string.logo)
					 +layoutInflater.getContext().getResources().getString(R.string.img_set_message));			 
		 }
		 else if(data.getContentType()==CONTENT_TYPE.FILE){			 
			 viewHolder.messageContent.setText(sendIMEI+layoutInflater.getContext().getResources().getString(R.string.logo)
					 +layoutInflater.getContext().getResources().getString(R.string.fill_set_message));
		 }
		 else if(data.getContentType()==CONTENT_TYPE.VOICE){			
			 viewHolder.messageContent.setText(sendIMEI+layoutInflater.getContext().getResources().getString(R.string.logo)
					 +layoutInflater.getContext().getResources().getString(R.string.voice_set_message));
		 } 
		}
		else{
			viewHolder.messageContent.setText(sendIMEI+layoutInflater.getContext().getResources().getString(R.string.logo)
					 +"null");
		}
		//add end
	}
	//add end
	
	 public void setData(ViewHolder viewHolder, Message data) {
	        if (null == data) {
	            return;
	        }
	        
	        //add by lss for adding group message in message list	             	        
	        if((!"".equals(data.getReceiveIP()))&&(data.getReceiveIP()!=null)){
	        	//group
	        	MyLog.d("lss","this is group");
	        	// add by lss for processing the situation of group equals null
	        	Group group=getGroup(data.getReceiveIP());
	 	        if(null==group){
	 	        	return;
	 	        }
	 	       //add end
	        	int contentLength=data.getMsgContent().length();
	 	        MyLog.d("lss","contentLength="+contentLength);
	 	        setDataType(viewHolder,data,"true");//群组信息显示方式
	  
	 	        String callDateNow=data.getSendTime();
	 	        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd");  
	 	        Date curDate = new Date(System.currentTimeMillis());//获取当前时间     
	 	        String dateNow = formatter.format(curDate);
	 	        if(callDateNow.contains(dateNow)){
	 	        	viewHolder.date.setText(callDateNow.substring(10));
	 	        }
	 	        else{
	 	        	 viewHolder.date.setText(callDateNow);
	 	        }
	 	        
	 	      //根据receiverIP获取群组名称	      
	 	      String groupName=group.getStrName();
		 	  MyLog.d("lss","groupName="+groupName+"::group"+group);
		 	  viewHolder.mtvReceiverName.setText(groupName);	 
	 	       //判断未读已读	 	        
	 	        if(data.getReadStatus()==0){
	 	        	//add by lss for hiding unreading flags which send messages by local 
	 	        	if(data.getSenderIMEI().contains(mIMEI)){
	 	        		viewHolder.readPeople.setVisibility(View.GONE);
	 	        	}else{
	 	        		viewHolder.readPeople.setVisibility(View.VISIBLE);
	 	        	}	 	        		
	 	        }
	 	        else{
	 	        	viewHolder.readPeople.setVisibility(View.GONE);
	 	        }
	        }
	        else{	        	
	 	       MyLog.d("lss","this is p2p");
	        	//not group 
	        	int contentLength=data.getMsgContent().length();
	 	        MyLog.d("lss","contentLength="+contentLength);
	 	        setDataType(viewHolder,data);//设置信息显示方式
	  
	 	        String callDateNow=data.getSendTime();
	 	        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd");  
	 	        Date curDate = new Date(System.currentTimeMillis());//获取当前时间     
	 	        String dateNow = formatter.format(curDate);
	 	        if(callDateNow.contains(dateNow)){
	 	        	viewHolder.date.setText(callDateNow.substring(10));
	 	        }
	 	        else{
	 	        	 viewHolder.date.setText(callDateNow);
	 	        }
	 	        
	 	        //判断获取接收方号码格式
	 	        
	 	        if(data.getSenderIMEI().contains(":"))
	 		       {
	 		        	if(data.getSenderIMEI().substring(5, 8).equals(mIMEI)){
	 		        		viewHolder.mtvReceiverName.setText(data.getReceiverIMEI().substring(5, 8));
	 		        	}
	 		        	else{
	 		        		viewHolder.mtvReceiverName.setText(data.getSenderIMEI().substring(5, 8));
	 		        	}
	 		        }
	 		        else
	 		        {
	 		        	
	 		        	if(data.getSenderIMEI().equals(mIMEI)){
	 		        		viewHolder.mtvReceiverName.setText(data.getReceiverIMEI());
	 		        	}
	 		        	else{
	 		        		viewHolder.mtvReceiverName.setText(data.getSenderIMEI());
	 		        	}
	 		        	
	 		        	
	 		        }	 	        
	 	       //判断未读已读
	 	        MyLog.d("lss","data.getReadStatus()="+data.getReadStatus()
	 	        		+"data.getSenderIMEI()="+data.getSenderIMEI()
	 	        		+"data.getReceiverIMEI()="+data.getReceiverIMEI());
	 	        if(data.getReadStatus()==0){
	 	        	if(data.getSenderIMEI().contains(mIMEI)){
	 	        			viewHolder.readPeople.setVisibility(View.GONE);
	 	        		}
	 	        		else{
	 	        			viewHolder.readPeople.setVisibility(View.VISIBLE);
	 	        		}
	 	        }
	 	        else{
	 	        	viewHolder.readPeople.setVisibility(View.GONE);
	 	        }
	        	
	        }
	        //add end
	    }
	 //add by lss for detecting group is exist or not
	public Group getGroup(String groupIP){
		Group group=Receiver.engine(Receiver.mContext).getGroup(groupIP);
		return group;
	}
	 //add end
	static class ViewHolder {

		private TextView mtvReceiverName;
		private TextView messageContent;
		private TextView date;
		private ImageView readPeople;
		private LinearLayout mLayoutCall;
	}

}
