package org.sipdroid.sipua.adapter;

import java.util.List;

import org.sipdroid.sipua.R;
import org.sipdroid.sipua.adapter.GroupMemAdapter.ViewHolder;

import com.leadcore.sms.entity.Group;
import com.leadcore.sms.entity.Users;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OnlineMemAdapter extends BaseAdapter {

    private static final String TAG = "GroupMemAdapter";
    private LayoutInflater layoutInflater;
    private List<Users> dataList;
    private boolean isMaster;

    public OnlineMemAdapter(Context context, List<Users> dataList, boolean isMaster) {
        this.dataList = dataList;
        this.isMaster = isMaster;
        layoutInflater = LayoutInflater.from(context);
    }
    public void setDataList(List<Users> dataList) {
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList == null ? null : dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.list_item_person, null);
            viewHolder.iconView = (ImageView) convertView.findViewById(R.id.id_image_icon);
            viewHolder.nameView = (TextView) convertView.findViewById(R.id.id_text_name);
            viewHolder.ipView = (TextView) convertView.findViewById(R.id.id_text_ip);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Users User = dataList.get(position);
        setData(viewHolder, User);
        if (isMaster) {
        	if (position == (getCount()-1)) {
            	viewHolder.iconView.setImageResource(R.drawable.btn_member_del);
    		}else if (position == (getCount()-2)) {
    			viewHolder.iconView.setImageResource(R.drawable.btn_member_add);
    		}
		}
        return convertView;
    }
    
    public void setData(ViewHolder viewHolder, Users data) {
        if (null == data) {
            Log.e(TAG, "err , this person data is null");
            return;
        }
        viewHolder.nameView.setText(data.getNickname());
        viewHolder.ipView.setText(data.getIpaddress());
    }
    
    class ViewHolder {
        public ImageView iconView;
        public TextView nameView ;
        public TextView ipView;
    }

}
