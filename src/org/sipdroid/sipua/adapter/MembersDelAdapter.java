package org.sipdroid.sipua.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sipdroid.sipua.R;
import org.sipdroid.sipua.adapter.ContactsChooseAdapter.ViewHolder;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.SessionUtils;

import com.leadcore.sip.login.NodeResource;
import com.leadcore.sms.entity.Users;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class MembersDelAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<Users> dataList;
    public Map<Integer, Boolean> map;
    
    public MembersDelAdapter(Context context, List<Users> data){
    	dataList = data;
    	layoutInflater = LayoutInflater.from(context);
    	map = new HashMap<Integer, Boolean>();
    	for (int i = 0; i < data.size(); i++) {
			map.put(i, false);
		}
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
            viewHolder.miconView = (ImageView) convertView.findViewById(R.id.id_image_icon);
            viewHolder.mnameView = (TextView) convertView.findViewById(R.id.id_text_name);
            viewHolder.mnumberView = (TextView) convertView.findViewById(R.id.id_text_number);
            viewHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.id_checkbox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        
        Users User = dataList.get(position);
        setData(viewHolder, User);
        if (User.getIMEI().equals(SessionUtils.getIMEI())) {
        	viewHolder.mCheckBox.setBackgroundResource(0);
        	viewHolder.mCheckBox.setButtonDrawable(R.drawable.ic_contacts_disenable);
		}else {
			viewHolder.mCheckBox.setChecked(map.get(position));
		}
        return convertView;
	}
	
    public void setData(ViewHolder viewHolder, Users data) {
        if (null == data) {
            return;
        }
        viewHolder.mnameView.setText(data.getNickname());
        viewHolder.mnumberView.setText(data.getIpaddress());
    }
    
    class ViewHolder {
        public ImageView miconView;
        public TextView mnameView ;
        public TextView mnumberView;
        public CheckBox mCheckBox;
    }

}
