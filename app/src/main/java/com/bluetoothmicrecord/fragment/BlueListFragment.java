package com.bluetoothmicrecord.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetoothmicrecord.R;
import com.bluetoothmicrecord.adapter.BlueListAdapter;
import com.bluetoothmicrecord.bean.BluetoothDevices;
import com.bluetoothmicrecord.bean.RecordAudioFile;
import com.bluetoothmicrecord.interfaces.BlueListAdapterInterface;
import com.bluetoothmicrecord.utils.BluetoothUtil;
import com.bluetoothmicrecord.utils.DaoUtils;
import com.bluetoothmicrecord.utils.DividerItemDecoration;
import com.bluetoothmicrecord.utils.PubUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.bluetoothmicrecord.utils.PubUtils.dip2px;
import static com.bluetoothmicrecord.utils.PubUtils.getSavedAudioDirPath;
import static com.bluetoothmicrecord.utils.PubUtils.getSavedAudioFilePath;

/**
 * Created by ${王sir} on 2017/9/7.
 * application
 */

public class BlueListFragment extends Fragment {

    private static final String ARG_IS_HIDDEN ="BlueListFragment" ;
    private Context context;
    private View view;
    private RecyclerView mBluelistRv;
    private DaoUtils greenDaoUtil;
    private List<BluetoothDevices> arrays;
    private BlueListAdapter adapter;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothUtil bluetoothUtil;

    private AudioManager mAudioManager = null;
    private MediaRecorder mRecorder = null;

    private TextView button_tv;
    private Dialog dialog;
    private int time = 0;//连接时长
    private String Tag = "BlueListFragment";
    private String savedPath;
    private boolean isShow = false;//fragment是否显示

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        greenDaoUtil = new DaoUtils(context);
        getDialog();
        initBluetoothDvStatus();
        adapter = new BlueListAdapter(context);
        bluetoothUtil = new BluetoothUtil(context, greenDaoUtil, adapter);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        initAdapterItemBtClickListener();

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

    private void getDialog() {
        dialog = new Dialog(context, R.style.DialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog, null);
        TextView tv = (TextView) view.findViewById(R.id.progress_content_tv);
        tv.setText("正在启动，请稍后...");
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * item右侧的按钮点击事件
     */
    private void initAdapterItemBtClickListener() {
        adapter.setOnBlueListIvClickCallBack(new BlueListAdapterInterface() {

            @Override
            public void OnBlueListIvClickListener(final BluetoothDevices bean, TextView matchStatus, TextView connectStatus, TextView bt) {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent requestBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(requestBluetoothOn,
                            100);
                    return;
                }
                button_tv = bt;
                if (("配  对").equals(bt.getText().toString().trim())) {
                    bluetoothUtil.matchDialogToShow(context);
                    bluetoothUtil.initParameters();
                    PubUtils.deviceName = bean.getDeviceAccount();
                    return;
                }
                if (("连  接").equals(bt.getText().toString().trim())) {
                    Set<BluetoothDevice> localSet = bluetoothAdapter.getBondedDevices();
                    for (BluetoothDevice btDevice : localSet) {
                        if (btDevice.getAddress().equals(bean.getMac())) {
                            bluetoothUtil.connect(btDevice);
                        }
                    }

                    return;
                }
                if (("开始录音").equals(bt.getText().toString().trim())) {
//                   int status =  bluetoothUtil.getDeviceConnectedStatus(getDeviceFromBondDv(bean));
//                    Log.i(Tag,status+"+++++++++++++");
                    bean.setHasRecordAudio(true);
                    greenDaoUtil.updateEntity(bean);
                    savedPath = getSavedAudioFilePath(bean);
                    if (!mAudioManager.isBluetoothScoAvailableOffCall()) {
                        Toast.makeText(context.getApplicationContext(), "系统不支持蓝牙录音", Toast.LENGTH_LONG).show();
                        return;
                    }
                    mAudioManager.stopBluetoothSco();
                    mAudioManager.startBluetoothSco();//蓝牙录音的关键，启动SCO连接，耳机话筒才起作用
                    if (dialog != null && !dialog.isShowing()) {
                        dialog.show();
                    }
                    registSCOReceiver();
                    return;
                }
                if (("停止录音").equals(bt.getText().toString().trim())) {
                    RecordAudioFile recordAudioFile = new RecordAudioFile();
                    recordAudioFile.setRecordTime(PubUtils.getDateToString(System.currentTimeMillis()));
                    recordAudioFile.setFilePath(savedPath);
                    recordAudioFile.setFileName(PubUtils.getAudioFileName(savedPath));
                    recordAudioFile.setFileDir(PubUtils.getAudioFileDirName(getSavedAudioDirPath(bean)));
                    recordAudioFile.setUpLoadStatus("0");
                    recordAudioFile.setUploadProgress(0);
                    greenDaoUtil.insertRecordAudioFile(recordAudioFile);
                    stopRecording();
                    return;
                }
            }

            @Override
            public void blueListItemClickListener() {

            }

            @Override
            public void blueListItemLongClickListener(BluetoothDevices bean) {//删除蓝牙设备
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
                greenDaoUtil.deleteEntity(bean);
                initRecyclerView();
            }
        });

    }

    /**
     * 通过添加蓝牙实体类获取真实的蓝牙设备
     *
     * @param bluetoothdv
     * @return
     */
    private BluetoothDevice getDeviceFromBondDv(BluetoothDevices bluetoothdv) {
        BluetoothDevice device = null;
        Set<BluetoothDevice> localSet = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice btDevice : localSet) {
            String mac = btDevice.getAddress();
            if (mac.equals(bluetoothdv.getMac())) {
                device = btDevice;
            }
        }
        return device;
    }

    /**
     * 开始录制音频
     */
    private void startRecording() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(savedPath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            mRecorder.prepare();
            if (mRecorder != null) {
                mRecorder.start();//开始录音
            }

        } catch (Exception e) {
            Log.e("", "" + e);
        }


    }

    /**
     * 停止录音
     */
    private void stopRecording() {
        //mAudioManager.stopBluetoothSco();


        if (mRecorder != null) {
            try {
                //下面三个参数必须加，不加的话会奔溃，在mediarecorder.stop();
                //报错为：RuntimeException:stop failed
                mRecorder.setOnErrorListener(null);
                mRecorder.setOnInfoListener(null);
                mRecorder.setPreviewDisplay(null);
                mRecorder.stop();
            } catch (IllegalStateException e) {
                // TODO: handle exception
            }catch (RuntimeException e) {
                // TODO: handle exception
            }catch (Exception e) {
                // TODO: handle exception
            }
            mRecorder.release();
            mRecorder = null;
            if (button_tv != null) {
                button_tv.setText("开始录音");
                adapter.setRecordStatus(false);
            }
        }
        if (mAudioManager != null) {
            if (mAudioManager.isBluetoothScoOn()) {
                mAudioManager.setMode(AudioManager.MODE_NORMAL);
                mAudioManager.setBluetoothScoOn(false);
                mAudioManager.stopBluetoothSco();
            }
        }



    }

    /**
     * 注册监听sco状态的广播
     */
    private void registSCOReceiver() {
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                    mAudioManager.setBluetoothScoOn(true);  //打开SCO
                    mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                    startRecording();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    button_tv.setText("停止录音");
                    adapter.setRecordStatus(true);
                    context.unregisterReceiver(this);  //别遗漏
                }
            }
        }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
    }

    /**
     * 初始化存储蓝牙设备的状态
     */
    private void initBluetoothDvStatus() {
        arrays = greenDaoUtil.queryAllBluetoothDevices();
        for (BluetoothDevices array : arrays) {
            array.setMatchStatus("未配对");
            array.setConnectStatus("未连接");
            greenDaoUtil.updateEntity(array);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isShow) {
            initRecyclerView();
        }
        IntentFilter filter = new IntentFilter("MATCHED");
        context.registerReceiver(mBroadCastReciver, filter);

        bluetoothUtil.getBluetoothA2DP();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_blue_list, container, false);
        initView(view);

        return view;
    }

    private void initView(View view) {
        mBluelistRv = (RecyclerView) view.findViewById(R.id.bluelist_rv);
    }

    /**
     * 初始化recyclerview
     */
    private void initRecyclerView() {
        arrays = greenDaoUtil.queryAllBluetoothDevices();
        List<String> bluetoothDvs = new ArrayList<>();
        Set<BluetoothDevice> localSet = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice btDevice : localSet) {
            String mac = btDevice.getAddress();
            bluetoothDvs.add(mac);
        }
        adapter.setDate(arrays, bluetoothDvs);
        LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        mBluelistRv.setLayoutManager(manager);
        mBluelistRv.setAdapter(adapter);
        mBluelistRv.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            switch (resultCode) {
                case Activity.RESULT_OK: {
                    PubUtils.iscancel = true;
                    Toast.makeText(context, "蓝牙已开启", Toast.LENGTH_SHORT).show();
                }
                break;

                case Activity.RESULT_CANCELED: {

                    PubUtils.iscancel = true;
                    showWarnDialog();
                }
                break;
                default:
                    break;
            }
        }
    }

    private void showWarnDialog() {

        View v = LayoutInflater.from(context).inflate(R.layout.warning, null);
        final Dialog dialog_ = new Dialog(context, R.style.DialogStyle);
        dialog_.setCanceledOnTouchOutside(false);
        dialog_.setCancelable(false);
        dialog_.show();
        Window window = dialog_.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        lp.width = dip2px(context, 300);
        lp.height = dip2px(context, 180);
        window.setAttributes(lp);
        window.setContentView(v);
        TextView warn_false_tv = (TextView) v.findViewById(R.id.warn_false_tv);
        TextView warn_true_tv = (TextView) v.findViewById(R.id.warn_true_tv);
        warn_true_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PubUtils.iscancel = false;
                dialog_.dismiss();
            }
        });
        warn_false_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_.dismiss();
                PubUtils.iscancel = false;
                Intent requestBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(requestBluetoothOn,
                        100);

            }
        });
    }

    BroadcastReceiver mBroadCastReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("MATCHED".equals(action)) {
                Toast.makeText(context.getApplicationContext(), "已配对", Toast.LENGTH_LONG).show();
                PubUtils.bluetoothDevices.setMatchStatus("已配对");
                adapter.notifyDataSetChanged();
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
    }
}
