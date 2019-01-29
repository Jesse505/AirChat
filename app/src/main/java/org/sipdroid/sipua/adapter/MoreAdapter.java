package org.sipdroid.sipua.adapter;


import org.sipdroid.sipua.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MoreAdapter extends BaseAdapter {

	private static String TAG = "MessageAdapter";
	
	
    private LayoutInflater layoutInflater;
 
    
    public MoreAdapter(Context context) {
   	
    	layoutInflater = LayoutInflater.from(context);	
    	
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;		
		if (convertView == null) {

			 	holder = new ViewHolder();
				convertView = layoutInflater.inflate(
						R.layout.simple_more_list, null);
				holder.mLayoutMore=(LinearLayout)convertView.findViewById(R.id.line_more_list);
				holder.mtvPersonalInfo = (TextView) convertView.findViewById(R.id.txt_personal_info);
				holder.mtvAbout=(TextView)convertView.findViewById(R.id.txt_about);
				holder.mtvSetting=(TextView)convertView.findViewById(R.id.txt_settting);
				holder.mtvExit=(TextView)convertView.findViewById(R.id.txt_exit);
				holder.imgPersonalInfo = (ImageButton) convertView.findViewById(R.id.img_personal_info);
				holder.imgAbout=(ImageButton)convertView.findViewById(R.id.img_about);
				holder.imgSetting=(ImageButton)convertView.findViewById(R.id.img_setting);
				holder.imgExit=(ImageButton)convertView.findViewById(R.id.img_exit);
		
				holder.mLayoutMore.setVisibility(View.VISIBLE);
				
				
			    convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}	
		
        return convertView;	
	}
	
	
	static class ViewHolder {

		private TextView mtvPersonalInfo;
		private TextView mtvAbout;
		private TextView mtvSetting;
		private TextView mtvExit;
		private ImageButton imgPersonalInfo;
		private ImageButton imgAbout;
		private ImageButton imgSetting;
		private ImageButton imgExit;
		private LinearLayout mLayoutMore;
		
	}


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 1;
	}




	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	

	
	
	

}
