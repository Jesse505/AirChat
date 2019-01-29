package com.leadcore.sip.login;

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

public class LogPersonAdapter extends BaseAdapter{

    private static final String TAG = LogPersonAdapter.class.getSimpleName();
    private LayoutInflater layoutInflater;
    private List<NodeResource> dataList;
  

    public LogPersonAdapter(Context context, List<NodeResource> dataList) {
        this.dataList = dataList;
        layoutInflater = LayoutInflater.from(context);
    }
    public void setDataList(List<NodeResource> dataList) {
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
         
        	  convertView = layoutInflater.inflate(R.layout.item_call_status, null);
              viewHolder.iconView = (ImageView) convertView.findViewById(R.id.img_call_status);
              viewHolder.iconView.setVisibility(View.VISIBLE);
              viewHolder.nameView = (TextView) convertView.findViewById(R.id.txt_name);
              viewHolder.totalTimeView=(TextView) convertView.findViewById(R.id.txt_total_time);
              viewHolder.timeView = (TextView) convertView.findViewById(R.id.txt_time);
              viewHolder.timeView.setVisibility(View.VISIBLE);
              viewHolder.totalTimeView.setVisibility(View.VISIBLE);
              viewHolder.nameView.setVisibility(View.VISIBLE);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        NodeResource bean = dataList.get(position);
        setData(viewHolder, bean);
        return convertView;
    }
    
    public void setData(ViewHolder viewHolder, NodeResource data) {
        if (null == data) {
            Log.e(TAG, "err , this person data is null");
            return;
        }
        if (data.getIconId() > 0) {
            viewHolder.iconView.setImageResource(data.getIconId());
        }
        viewHolder.nameView.setText(data.getName());
        viewHolder.totalTimeView.setText(data.getIndex());
    }
    
    class ViewHolder {
        public ImageView iconView;
        public TextView nameView ;
        public TextView totalTimeView;
        public TextView timeView;
    }
    
}
