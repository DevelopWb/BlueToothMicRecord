package com.bluetoothmicrecord.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bluetoothmicrecord.R;
import com.bluetoothmicrecord.bean.RecordAudioFile;
import com.bluetoothmicrecord.interfaces.RecordAudiosActivityAdapterInterface;
import com.bluetoothmicrecord.upload.uploadUtil.HProgressBar;
import com.bluetoothmicrecord.upload.uploadUtil.PreferenceUtil;
import com.bluetoothmicrecord.utils.DaoUtils;
import com.bluetoothmicrecord.utils.PubUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ${王sir} on 2017/9/7.
 * application
 */

public class RecordAudiosActivityAdapter extends RecyclerView.Adapter<RecordAudiosActivityAdapter.ViewHolder> {
    private List<RecordAudioFile> arrays;
    private Context context;
    private RecordAudiosActivityAdapterInterface itemClickListener;
    private boolean selected = false;//item是否需要被选择
    private PreferenceUtil mPreferenceUtil;
    private DaoUtils greenDaoUtil;
    private RecordAudioFile playingRecordAudio;//正在播放的音频文件


    public RecordAudiosActivityAdapter(Context context, DaoUtils greenDaoUtil) {
        this.context = context;
        this.greenDaoUtil = greenDaoUtil;
        mPreferenceUtil = PreferenceUtil.getInstance(context, "APPSET");
    }

    public void setData(List<RecordAudioFile> arrays) {
        this.arrays = arrays;
        notifyDataSetChanged();
    }

    public void setPlayingBean(RecordAudioFile bean) {
        this.playingRecordAudio = bean;
        notifyDataSetChanged();
    }

    public void setItemSelected(boolean selected) {
        this.selected = selected;
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_audio_activity_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        final RecordAudioFile bean = arrays.get(position);
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            if (selected) {
                if (PubUtils.selectedFiles.contains(bean)) {
                    holder.selecter_checkbox.setChecked(true);
                } else {
                    holder.selecter_checkbox.setChecked(false);
                }

            } else {

                if (bean.getUpLoadStatus().equals("0")) {//未上传
                    holder.upload_progressbar.setVisibility(View.GONE);
                } else if (bean.getUpLoadStatus().equals("1")) {//正在上传
                    holder.upload_tv.setText("暂停上传");
                    holder.upload_progressbar.setVisibility(View.VISIBLE);
                    holder.upload_progressbar.setProgress(bean.getUploadProgress());
                } else if (bean.getUpLoadStatus().equals("2")) {//已暂停
                    if (bean.getUploadProgress() > 99) {
                        holder.upload_progressbar.setVisibility(View.GONE);
                        holder.upload_tv.setText(" 已上传 ");
                    } else {
                        holder.upload_tv.setText("继续上传");
                        holder.upload_progressbar.setVisibility(View.VISIBLE);
                        holder.upload_progressbar.setProgress(bean.getUploadProgress());
                    }

                } else  if (bean.getUpLoadStatus().equals("3")) {//已上传
                    holder.upload_progressbar.setVisibility(View.GONE);
                    holder.upload_tv.setText(" 已上传 ");
//            if (itemClickListener != null) {
//                itemClickListener.deleteAudio(bean);
//            }
//            if (mPreferenceUtil.getBoolean("DELETEFILE")) {
//                String path = bean.getFilePath();
//                File file = new File(path);
//                file.delete();
//                greenDaoUtil.deleteEntity(bean);
//
////                arrays = getDataForAdapter();
////                notifyDataSetChanged();
//            }
                }else{
                    holder.upload_tv.setText("无网络");
                }
            }

        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final RecordAudioFile bean = arrays.get(position);
        bean.setPosition(position);
        String fileSize = "";
        if (selected) {
            if (PubUtils.selectedFiles.contains(bean)) {
                holder.selecter_checkbox.setChecked(true);
            } else {
                holder.selecter_checkbox.setChecked(false);
            }
            holder.selecter_checkbox.setVisibility(View.VISIBLE);
            holder.startPlay_audio_iv.setVisibility(View.GONE);
            holder.upload_tv.setVisibility(View.INVISIBLE);

        } else {
            holder.selecter_checkbox.setVisibility(View.GONE);
            holder.startPlay_audio_iv.setVisibility(View.VISIBLE);
            holder.upload_tv.setVisibility(View.VISIBLE);

        }
        try {
            fileSize = PubUtils.getFileSize(new File(bean.getFilePath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.audio_file_name.setText(bean.getFileName() + PubUtils.AudioFileType);
        holder.audio_file_size_tv.setText("文件大小：" + fileSize);
        holder.record_time_tv.setText("录制时间：" + bean.getRecordTime());
        if (bean.getUpLoadStatus().equals("0")) {//未上传
            holder.upload_progressbar.setVisibility(View.GONE);
            holder.upload_tv.setText("开始上传");
        } else if (bean.getUpLoadStatus().equals("1")) {//正在上传
            holder.upload_tv.setText("暂停上传");
            holder.upload_progressbar.setVisibility(View.VISIBLE);
            holder.upload_progressbar.setProgress(bean.getUploadProgress());
        } else if (bean.getUpLoadStatus().equals("2")) {//已暂停
            if (bean.getUploadProgress() > 99) {
                holder.upload_progressbar.setVisibility(View.GONE);
                holder.upload_tv.setText(" 已上传 ");
            } else {
                holder.upload_tv.setText("继续上传");
                holder.upload_progressbar.setVisibility(View.VISIBLE);
                holder.upload_progressbar.setProgress(bean.getUploadProgress());
            }

        } else  if (bean.getUpLoadStatus().equals("3")) {//已上传
            holder.upload_progressbar.setVisibility(View.GONE);
            holder.upload_tv.setText(" 已上传 ");
//            if (itemClickListener != null) {
//                itemClickListener.deleteAudio(bean);
//            }
//            if (mPreferenceUtil.getBoolean("DELETEFILE")) {
//                String path = bean.getFilePath();
//                File file = new File(path);
//                file.delete();
//                greenDaoUtil.deleteEntity(bean);
//
////                arrays = getDataForAdapter();
////                notifyDataSetChanged();
//            }
        }else{
            holder.upload_tv.setText("无网络");
        }
        holder.audios_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.itemClick(bean);
                }
            }
        });
        holder.audios_ll.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.itemLongClick(bean);
                }
                return false;
            }
        });

        holder.upload_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.uploadFile(bean, position);
                }


            }
        });
        holder.startPlay_audio_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.startPlayAudio(bean, holder.startPlay_audio_iv);
                }
            }
        });
        if (bean.equals(playingRecordAudio)) {
            holder.startPlay_audio_iv.setImageResource(R.drawable.pause_normal);
        } else {
            holder.startPlay_audio_iv.setImageResource(R.drawable.start_normal);

        }


    }

    @Override
    public int getItemCount() {
        return arrays == null ? 0 : arrays.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView audio_file_name;//音频文件名称
        private final TextView upload_tv;//上传按钮
        private final TextView record_time_tv;//录制时间
        private final TextView audio_file_size_tv;//文件大小
        private final LinearLayout audios_ll;//item父控件
        private final HProgressBar upload_progressbar;//上传进度
        private final ImageView startPlay_audio_iv;//播放音频的按钮
        private final CheckBox selecter_checkbox;//item的选择框

        public ViewHolder(View itemView) {
            super(itemView);
            audio_file_name = (TextView) itemView.findViewById(R.id.audio_file_name);
            upload_tv = (TextView) itemView.findViewById(R.id.upload_tv);
            record_time_tv = (TextView) itemView.findViewById(R.id.record_time_tv);
            audio_file_size_tv = (TextView) itemView.findViewById(R.id.audio_file_size_tv);
            audios_ll = (LinearLayout) itemView.findViewById(R.id.audios_ll);
            upload_progressbar = (HProgressBar) itemView.findViewById(R.id.upload_progressbar);
            startPlay_audio_iv = (ImageView) itemView.findViewById(R.id.startPlay_audio_iv);
            selecter_checkbox = (CheckBox) itemView.findViewById(R.id.selecter_checkbox);
        }
    }

    public void setOnItemClickListener(RecordAudiosActivityAdapterInterface itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    /**
     * 查询单个蓝牙设备所有录制音频文件数据
     *
     * @return
     */
    private List<RecordAudioFile> getDataForAdapter() {
        List<RecordAudioFile> arrays = new ArrayList<>();
        String fileDir = PubUtils.AudiosDevice.getDeviceName().trim() + "-" + PubUtils.AudiosDevice.getDeviceAccount().trim();
        arrays = greenDaoUtil.queryAllRecordAudioFiles(fileDir);
        return arrays;
    }
}
