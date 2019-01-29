package com.leadcore.sip.login;

import java.util.List;
import org.sipdroid.sipua.R;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SearchLoginPersonAdapter extends BaseAdapter{

    private static final String TAG = SearchLoginPersonAdapter.class.getSimpleName();
    private LayoutInflater layoutInflater;
    private List<NodeResource> dataList;


    public SearchLoginPersonAdapter(Context context, List<NodeResource> dataList) {
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
            convertView = layoutInflater.inflate(R.layout.list_search_person, null);
           
            viewHolder.nameView = (TextView) convertView.findViewById(R.id.id_text_name);
            viewHolder.numberView = (TextView) convertView.findViewById(R.id.id_text_number);
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
        viewHolder.nameView.setText(data.getName());
        viewHolder.numberView.setText(data.getNumber());
    }
    
    class ViewHolder {
        
        public TextView nameView ;
        public TextView numberView;
    }
}
