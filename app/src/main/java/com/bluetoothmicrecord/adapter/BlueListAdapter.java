package com.bluetoothmicrecord.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bluetoothmicrecord.R;
import com.bluetoothmicrecord.bean.BluetoothDevices;
import com.bluetoothmicrecord.interfaces.BlueListAdapterInterface;
import com.bluetoothmicrecord.utils.PubUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ${王sir} on 2017/9/7.
 * application
 */

public class BlueListAdapter extends RecyclerView.Adapter<BlueListAdapter.ViewHolder> {
    private List<BluetoothDevices> arrays;
    private BlueListAdapterInterface blueListAdapterInterface;
    private Context context;
    private List<String> matchStrs= new ArrayList<>();
    private List<String> connectStrs = new ArrayList<>();
    private boolean recordStatus = false;//录制状态
    private String TAG = "888888";

    public BlueListAdapter(Context context) {
        this.context = context;
    }

    public void setDate(List<BluetoothDevices> arrays, List<String> strs) {
        this.arrays = arrays;
        this.matchStrs = strs;
        notifyDataSetChanged();
    }

    public void setMatchAndConnectStatus(List<String> matchStrs, List<String> connectStrs) {
        this.matchStrs = matchStrs;
        this.connectStrs = connectStrs;
        notifyDataSetChanged();
    }

    public void setDate(List<BluetoothDevices> arrays) {
        this.arrays = arrays;
        notifyDataSetChanged();
    }
    public void setRecordStatus(boolean recordStatus) {
        this.recordStatus = recordStatus;
        notifyDataSetChanged();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blue_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
      final  BluetoothDevices bean = arrays.get(position);
        String matchStr = "";
        String connectStr = "";
        holder.bluelist_item_name.setText("布控名称：" + bean.getDeviceName());
        holder.bluelist_item_id.setText("设备标识：" + bean.getMac());
        holder.bluelist_item_time.setText("添加时间：" + bean.getAddTime());
        if (matchStrs.contains(bean.getMac())) {
            matchStr = "匹配状态：已配对";
        } else {
            matchStr = "匹配状态：未配对";
        }
        if (connectStrs.contains(bean.getMac())) {
            connectStr = "连接状态：已连接";
        } else {
            connectStr = "连接状态：未连接";
        }

        if (matchStr.contains("未配对")) {
            PubUtils.setTextViewColor(context, holder.bluelist_match_status, matchStr, false);
        } else {
            PubUtils.setTextViewColor(context, holder.bluelist_match_status, matchStr, true);
        }
        if (connectStr.contains("未连接")) {
            PubUtils.setTextViewColor(context, holder.bluelist_connect_status, connectStr, false);
        } else {
            PubUtils.setTextViewColor(context, holder.bluelist_connect_status, connectStr, true);
        }
        if (matchStr.contains("未配对")) {
            holder.bluelist_item_tv.setText("配  对");
        } else {
            if (connectStr.contains("未连接")) {
                holder.bluelist_item_tv.setText("连  接");
            } else {
                if (recordStatus) {
                    holder.bluelist_item_tv.setText("停止录音");
                } else {
                    holder.bluelist_item_tv.setText("开始录音");
                }

            }
        }
        holder.bluelist_item_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blueListAdapterInterface != null) {
                    PubUtils.bluetoothDevices = bean;
                    blueListAdapterInterface.OnBlueListIvClickListener(bean, holder.bluelist_match_status, holder.bluelist_connect_status, holder.bluelist_item_tv);
                }
            }
        });
        holder.blue_list_item_ll.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PubUtils.bluetoothDevices = bean;
                Log.i(TAG,"long+++++++++++++++++++++");
                if (blueListAdapterInterface != null) {
                    blueListAdapterInterface.blueListItemLongClickListener(bean);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrays == null ? 0 : arrays.size();
    }



    public void updateAdapter() {
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView bluelist_item_name;//设备名称
        private final TextView bluelist_item_id;//设备标识
        private final TextView bluelist_item_time;//添加时间
        private final TextView bluelist_match_status;//匹配状态
        private final TextView bluelist_connect_status;//连接状态
        private final TextView bluelist_item_tv;//匹配或者连接的按钮
        private final RelativeLayout blue_list_item_ll;//item父布局

        public ViewHolder(View itemView) {
            super(itemView);
            bluelist_item_name = (TextView) itemView.findViewById(R.id.bluelist_item_name);
            bluelist_item_id = (TextView) itemView.findViewById(R.id.bluelist_item_id);
            bluelist_item_time = (TextView) itemView.findViewById(R.id.bluelist_item_time);
            bluelist_match_status = (TextView) itemView.findViewById(R.id.bluelist_match_status);
            bluelist_connect_status = (TextView) itemView.findViewById(R.id.bluelist_connect_status);
            bluelist_item_tv = (TextView) itemView.findViewById(R.id.bluelist_item_tv);
            blue_list_item_ll = (RelativeLayout) itemView.findViewById(R.id.blue_list_item_rl);
        }
    }

    public void setOnBlueListIvClickCallBack(BlueListAdapterInterface blueListAdapterInterface) {
        this.blueListAdapterInterface = blueListAdapterInterface;
    }

}
