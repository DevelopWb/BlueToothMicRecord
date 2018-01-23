package com.bluetoothmicrecord.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bluetoothmicrecord.R;
import com.bluetoothmicrecord.bean.BluetoothDevices;
import com.bluetoothmicrecord.interfaces.RecordAudioAdapterInterface;

import java.util.List;

/**
 * Created by ${王sir} on 2017/9/7.
 * application
 */

public class RecordAudioAdapter extends RecyclerView.Adapter<RecordAudioAdapter.ViewHolder> {
    private List<BluetoothDevices> arrays;
    private Context context;
    private RecordAudioAdapterInterface itemClickListener;

    public RecordAudioAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<BluetoothDevices> arrays) {
        this.arrays = arrays;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_audio_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final BluetoothDevices bean = arrays.get(position);
        holder.record_audio_name.setText("布控名称：" + bean.getDeviceName());
        holder.record_audio_id.setText("设备标识：" + bean.getMac());
        holder.record_audio_time.setText("添加时间：" + bean.getAddTime());
        holder.record_audio_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener!=null) {
                    itemClickListener.itemClick(bean);
                }
            }
        });
        holder.record_audio_ll.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (itemClickListener!=null) {
                    itemClickListener.itemLongClick(bean);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrays == null ? 0 : arrays.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView record_audio_name;//设备名称
        private final TextView record_audio_id;//设备标识
        private final TextView record_audio_time;//添加时间
        private final LinearLayout record_audio_ll;//item父控件

        public ViewHolder(View itemView) {
            super(itemView);
            record_audio_name = (TextView) itemView.findViewById(R.id.record_audio_name);
            record_audio_id = (TextView) itemView.findViewById(R.id.record_audio_id);
            record_audio_time = (TextView) itemView.findViewById(R.id.record_audio_time);
            record_audio_ll = (LinearLayout) itemView.findViewById(R.id.record_audio_ll);
        }
    }

    public void setOnItemClickListener(RecordAudioAdapterInterface itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
