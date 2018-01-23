package com.bluetoothmicrecord.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bluetoothmicrecord.R;
import com.bluetoothmicrecord.activity.RecordAudiosActivity;
import com.bluetoothmicrecord.adapter.RecordAudioAdapter;
import com.bluetoothmicrecord.bean.BluetoothDevices;
import com.bluetoothmicrecord.bean.RecordAudioFile;
import com.bluetoothmicrecord.interfaces.RecordAudioAdapterInterface;
import com.bluetoothmicrecord.utils.DaoUtils;
import com.bluetoothmicrecord.utils.DividerItemDecoration;
import com.bluetoothmicrecord.utils.PubUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.bluetoothmicrecord.utils.PubUtils.dip2px;

/**
 * Created by ${王sir} on 2017/9/8.
 * application
 */

public class RecordAudioListFragment extends Fragment {
    private Context context;
    private DaoUtils greenDaoUtil;
    private RecyclerView mRecordAudioRv;
    private RecordAudioAdapter adapter;
    private boolean isShow = false;//fragment是否显示


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        greenDaoUtil = new DaoUtils(context);
        adapter = new RecordAudioAdapter(context);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.record_audio_list, container, false);
        initView(view);
        return view;

    }

    private void initView(View view) {
        mRecordAudioRv = (RecyclerView) view.findViewById(R.id.record_audio_list_rv);
        adapter.setOnItemClickListener(new RecordAudioAdapterInterface() {
            @Override
            public void itemClick(BluetoothDevices bean) {
                PubUtils.AudiosDevice = bean;
                startActivity(new Intent(context, RecordAudiosActivity.class));
            }

            @Override
            public void itemLongClick(BluetoothDevices bean) {
                showDeleteDialog(bean);

            }
        });
    }


    /**
     * 删除对象的对话框
     */
    private void showDeleteDialog(final BluetoothDevices bean) {
        View v = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
        final Dialog dialog_c = new Dialog(context, R.style.DialogStyle);
        dialog_c.setCanceledOnTouchOutside(false);
        dialog_c.show();
        Window window = dialog_c.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        lp.width = dip2px(context, 300); // 宽度
        lp.height = dip2px(context, 230); // 高度
        // lp.alpha = 0.7f; // 透明度
        window.setAttributes(lp);
        window.setContentView(v);
        TextView confirm_tv = (TextView) v.findViewById(R.id.confirm_tv);
        TextView cancel_tv = (TextView) v.findViewById(R.id.cancel_tv);
        TextView warn_content_tv = (TextView) v.findViewById(R.id.warn_content_tv);
        warn_content_tv.setText("删除后录制音频也将被删除，确定删除吗？");
        cancel_tv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog_c.dismiss();
            }
        });
        confirm_tv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog_c.dismiss();
                bean.setHasRecordAudio(false);
                greenDaoUtil.updateEntity(bean);
                //将录制的音频的数据库存储记录删除
                for (RecordAudioFile file:getDataForAdapter(bean)) {
                    greenDaoUtil.deleteEntity(file);
                }
                //将录制的音频文件删除
                String path = PubUtils.getSavedAudioDirPath(bean);
                File file = new File(path);
                File[] files = file.listFiles();
                for (File file1 : files) {
                    if (file1.getName().contains(PubUtils.AudioFileType)) {
                        file1.delete();
                    }
                }

                initRecyclerView();
            }
        });

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            isShow = true;
            onResume();
        }else{
            isShow = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isShow) {
            initRecyclerView();
        }
    }

    /**
     * 查询单个蓝牙设备所有录制音频文件数据
     *
     * @return
     */
    private List<RecordAudioFile> getDataForAdapter(BluetoothDevices bean) {
        List<RecordAudioFile> arrays = new ArrayList<>();
        String fileDir = bean.getDeviceName().trim() + "-" + bean.getDeviceAccount().trim();
        arrays = greenDaoUtil.queryAllRecordAudioFiles(fileDir);
        return arrays;
    }
    /**
     * 初始化recyclerview
     */
    private void initRecyclerView() {
        List<BluetoothDevices> arrays = new ArrayList<>();
        List<BluetoothDevices> arrays_all = greenDaoUtil.queryAllBluetoothDevices();
        for (BluetoothDevices bluetoothDevices : arrays_all) {
            if (bluetoothDevices.getHasRecordAudio()) {
                arrays.add(bluetoothDevices);
            }
        }
        LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        mRecordAudioRv.setLayoutManager(manager);
        adapter.setData(arrays);
        mRecordAudioRv.setAdapter(adapter);
        mRecordAudioRv.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL));
    }

}
