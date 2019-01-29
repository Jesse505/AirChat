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

public class LoginPersonAdapter extends BaseAdapter{

    private static final String TAG = LoginPersonAdapter.class.getSimpleName();
    private LayoutInflater layoutInflater;
    private List<NodeResource> dataList;
//    private OnPersonItemClickListener onItemClickListener ;
//
//    public interface OnPersonItemClickListener {
//        public boolean onLongClick(View v, int pos);
//        public void onClick(View v, int pos) ;
//    }
//    
//    public void setOnItemClickListener(OnPersonItemClickListener onItemClickListener) {
//        this.onItemClickListener = onItemClickListener;
//    }

    public LoginPersonAdapter(Context context, List<NodeResource> dataList) {
        this.dataList = dataList;
        layoutInflater = LayoutInflater.from(context);
    }
    public void setDataList(List<NodeResource> dataList) {
        this.dataList = dataList;
    }
    
//    @Override
//    public void onBindViewHolder(ViewHolder arg0, int pos) {
//        NodeResource data = dataList.get(pos);
//        if (null == data) {
//            Log.e(TAG, "err, this data =null, pos=" + pos);
//        }
//        arg0.setData(data);
//    }
//
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
//        View view = LayoutInflater.from(arg0.getContext()).inflate(11, arg0, false);
//        ViewHolder viewHolder = new ViewHolder(view);
//        return viewHolder;
//    }
//    
//    
//    
//    public class ViewHolder111 extends RecyclerView.ViewHolder implements OnClickListener, OnLongClickListener {
//
//        private View rootView;
//        private ImageView iconView;
//        private TextView nameView ;
//        private TextView ipView;
//        public ViewHolder(View itemView) {
//            super(itemView);
//            rootView = itemView.findViewById(R.id.id_layout_persons);
//            iconView = (ImageView) itemView.findViewById(R.id.id_image_icon);
//            nameView = (TextView) itemView.findViewById(R.id.id_text_name);
//            ipView = (TextView) itemView.findViewById(R.id.id_text_ip);
//            rootView.setOnClickListener(this);
//            rootView.setOnLongClickListener(this);
//        }
//        
//        public void setData(NodeResource data) {
//            if (null == data) {
//                Log.e(TAG, "err , this person data is null");
//                return;
//            }
//            if (data.getIconId() > 0) {
//                iconView.setImageResource(data.getIconId());
//            }
//            nameView.setText(data.getName());
//            ipView.setText(data.getIndex());
//        }
//
//        @Override
//        public boolean onLongClick(View v) {
//            onItemClickListener.onLongClick(v, getPosition());
//            return false;
//        }
//
//        @Override
//        public void onClick(View v) {
//            onItemClickListener.onClick(v, getPosition());
//        }
//    }

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
        viewHolder.ipView.setText(data.getIndex());
    }
    
    class ViewHolder {
        public ImageView iconView;
        public TextView nameView ;
        public TextView ipView;
    }
}
