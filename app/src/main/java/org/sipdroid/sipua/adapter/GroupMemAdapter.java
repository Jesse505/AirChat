package org.sipdroid.sipua.adapter;

import java.util.List;

import org.sipdroid.sipua.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leadcore.sip.login.LoginPersonAdapter;
import com.leadcore.sip.login.NodeResource;
import com.leadcore.sms.entity.Group;

public class GroupMemAdapter extends BaseAdapter{

    private static final String TAG = "GroupMemAdapter";
    private LayoutInflater layoutInflater;
    private List<Group> dataList;

    public GroupMemAdapter(Context context, List<Group> dataList) {
        this.dataList = dataList;
        layoutInflater = LayoutInflater.from(context);
    }
    public void setDataList(List<Group> dataList) {
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
        Group group = dataList.get(position);
        setData(viewHolder, group);
        return convertView;
    }
    
    public void setData(ViewHolder viewHolder, Group data) {
        if (null == data) {
            Log.e(TAG, "err , this person data is null");
            return;
        }
        viewHolder.nameView.setText(data.getStrName());
        viewHolder.ipView.setText(data.getStrIP());
        viewHolder.iconView.setImageResource(R.drawable.ic_group_avatar);
    }
    
    class ViewHolder {
        public ImageView iconView;
        public TextView nameView ;
        public TextView ipView;
    }
}
