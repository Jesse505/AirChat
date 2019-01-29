package org.sipdroid.sipua.adapter;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sipdroid.sipua.R;
import org.sipdroid.sipua.UserAgent;
import org.sipdroid.sipua.utils.MyLog;
import com.leadcore.sms.entity.Call;
import com.leadcore.sms.sql.SqlDBOperate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CallAdapter extends BaseAdapter {

	private static String TAG = "CallAdapter";	
	private List<Call> dataList;
    public Map<Integer, Boolean> map;
    private LayoutInflater layoutInflater;
   

    public CallAdapter(Context context, List<Call> data){

    	dataList = data;
    	layoutInflater = LayoutInflater.from(context);
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
						R.layout.call_template, null);
				holder.mLayoutCall=(LinearLayout)convertView.findViewById(R.id.rel_call_send);
				holder.mtvReceiverName = (TextView) convertView.findViewById(R.id.txt_name);
				holder.calltime=(TextView)convertView.findViewById(R.id.txt_call_time);
				holder.date=(TextView)convertView.findViewById(R.id.txt_date);
				holder.imgCallType=(ImageView)convertView.findViewById(R.id.img_call_type);//add by lss for declear calltype (audio or video) 
				holder.imgCallStatus=(ImageView)convertView.findViewById(R.id.img_status);
				holder.mLayoutCall.setVisibility(View.VISIBLE);
			    convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}	
		Call bean = dataList.get(position);
        setData(holder, bean);
        return convertView;	
	}
	 public void setData(ViewHolder viewHolder, Call data) {
	        if (null == data) {
	            return;
	        }
	        MyLog.d("lss","data"+data+":::data.getCallTime()"+data.getCallTime()
	        		+":::data.getSendTime()"+data.getSendTime()
	        		+":::data.getCallStatus()"+data.getCallStatus()
	        		+":::data.getReceiveIMEI()"+data.getReceiveIMEI());
	        String calltime="0s";
	        if(data.getCallTime()>=60&&data.getCallTime()<3600)
	        {
	        	calltime=String.valueOf(data.getCallTime()/60)+"m"+String.valueOf(data.getCallTime()%60)+"s";
	        }
	        else if(data.getCallTime()<60)
	        {
	        	calltime=String.valueOf(data.getCallTime())+"s";
	        }
	        else if(data.getCallTime()>=3600){
	        	calltime=String.valueOf(data.getCallTime()/3600)+"h";
	        	if(data.getCallTime()%3600>60)
	        		{
	        			calltime+=String.valueOf((data.getCallTime()%3600)/60)+"m"
	        						+String.valueOf((data.getCallTime()%3600)%60)+"s";
	        		}
	        	else 
		        	{
		        		calltime+=String.valueOf((data.getCallTime()%3600)/60)+"s";			
		        	}
	        }
	        viewHolder.calltime.setText(calltime);
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
	        
	       //判断电话接听状态
	        int callstatus=data.getCallStatus();
	        switch(callstatus){
	        case 1:
	        	viewHolder.imgCallStatus.setImageResource(R.drawable.oncallgoing);
	        	break;
	        case 2:
	        	viewHolder.imgCallStatus.setImageResource(R.drawable.incall);
	        	break;
	        case 3:
	        	viewHolder.imgCallStatus.setImageResource(R.drawable.unreceive_call);
	        	break;	        
	        default:
	        		break;
	        }
	        //判断获取接收方号码格式
	        //modify by lss for modifying the activity of calllog show senderName
	       
	        viewHolder.mtvReceiverName.setText(data.getReceiverName());
	        MyLog.d("lss","viewHolder.mtvReceiverName.getText()="+
	        		viewHolder.mtvReceiverName.getText());
	        //modify end
	        //add by lss for declear calltype (audio or video)
	        String calltype=data.getCallType();
	        MyLog.d("lss","calltype="+calltype);
	        if(calltype.equals(UserAgent.VoiceCall)){
	        	//语音
	        	viewHolder.imgCallType.setImageResource(R.drawable.img_audio);
	        }
	        else if (calltype.equals(UserAgent.VideoCall)){
	        	//视频
	        	viewHolder.imgCallType.setImageResource(R.drawable.img_video);
	        }
	        //add end
	    }

	static class ViewHolder {

		private TextView mtvReceiverName;
		private TextView calltime;
		private TextView date;
		private ImageView imgCallType;//add by lss for declear calltype (audio or video)
		private ImageView imgCallStatus;
		private LinearLayout mLayoutCall;
	}

}
