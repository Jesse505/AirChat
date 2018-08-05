package org.sipdroid.sipua.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sipdroid.sipua.R;
import com.leadcore.sip.login.NodeResource;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactsChooseAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<NodeResource> dataList;
    public Map<Integer, Boolean> map;
    
    public ContactsChooseAdapter(Context context, List<NodeResource> data){
//    	if (data.size() >= 2) {
//			dataList = data.subList(1, data.size());    //delete my item
//		}
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
            convertView = layoutInflater.inflate(R.layout.list_item_contacts_choose, null);
            viewHolder.mMasterView = (TextView) convertView.findViewById(R.id.tv_group_master);
            viewHolder.miconView = (ImageView) convertView.findViewById(R.id.id_image_icon);
            viewHolder.mnameView = (TextView) convertView.findViewById(R.id.id_text_name);
            viewHolder.mnumberView = (TextView) convertView.findViewById(R.id.id_text_number);
            viewHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.id_checkbox);
            if (position == 0) {
    			viewHolder.mMasterView.setVisibility(View.VISIBLE);
    			viewHolder.mCheckBox.setVisibility(View.GONE);
    		}
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        
        NodeResource bean = dataList.get(position);
        setData(viewHolder, bean);
        viewHolder.mCheckBox.setChecked(map.get(position));
        return convertView;
	}
	
    public void setData(ViewHolder viewHolder, NodeResource data) {
        if (null == data) {
            return;
        }
        viewHolder.mnameView.setText(data.getName());
        viewHolder.mnumberView.setText(data.getIndex());
    }
    
    class ViewHolder {
        public ImageView miconView;
        public TextView mMasterView;
        public TextView mnameView ;
        public TextView mnumberView;
        public CheckBox mCheckBox;
    }

}
