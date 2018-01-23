package com.bluetoothmicrecord.utils;

import android.app.Dialog;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetoothmicrecord.R;
import com.bluetoothmicrecord.adapter.BlueListAdapter;
import com.bluetoothmicrecord.bean.BluetoothDevices;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ${王sir} on 2017/9/11.
 * application
 */

public class BluetoothUtil {


    private static final String TAG = "BluetoothA2DPTest";

    private BluetoothA2dp mBluetoothA2dp;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private Context context;
    private DaoUtils greendaoUtil;
    private BlueListAdapter adapter;
    private Dialog dialog;
    private boolean isFound = false;//是否已经被发现

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device;
            switch (intent.getAction()) {
                case BluetoothDevice.ACTION_FOUND:
                    //<editor-fold>
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int deviceClassType = device.getBluetoothClass().getDeviceClass();
                    //找到指定的蓝牙设备
                    if ((deviceClassType == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET
                            || deviceClassType == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES)
                            && device.getName().equals(PubUtils.deviceName)) {
                        Log.i(TAG, "Found device:" + device.getName());
                        mBluetoothDevice = device;
                        isFound = true;
                        if (dialog_match != null && dialog_match.isShowing()) {
                            dialog_match.dismiss();
                        }
                        //start bond，开始配对
                        startMarth();
                    }else{
                        isFound = false;
                    }

                    //</editor-fold>
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED ://搜索结束
                    if (!isFound) {
                        if (dialog_match != null && dialog_match.isShowing()) {
                            dialog_match.dismiss();
                        }
                        Toast.makeText(context.getApplicationContext(), "找不到设备，请确保设备开启被搜寻模式", Toast.LENGTH_LONG).show();

                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED://配对状态
                    //<editor-fold>
                    int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    switch (bondState) {
                        case BluetoothDevice.BOND_BONDED:  //配对成功
                            Log.i(TAG, "Device:" + device.getName() + " bonded.");
                            mBluetoothAdapter.cancelDiscovery();  //取消搜索
                            PubUtils.bluetoothDevices.setMatchStatus("已配对");
                            greendaoUtil.updateEntity(PubUtils.bluetoothDevices);
                            List<BluetoothDevices> arrays = greendaoUtil.queryAllBluetoothDevices();
                            adapter.setDate(arrays);
                            connect();  //连接蓝牙设备
                            break;
                        //正在配对
                        case BluetoothDevice.BOND_BONDING:
                            Log.i(TAG, "Device:" + device.getName() + " bonding.");
                            break;
                        //配对不成功
                        case BluetoothDevice.BOND_NONE:
                            Log.i(TAG, "Device:" + device.getName() + " not bonded.");
                            //不知道是蓝牙耳机的关系还是什么原因，经常配对不成功
                            //配对不成功的话，重新尝试配对
                            startMarth();
                            break;
                        default:
                            break;

                    }

                    //</editor-fold>
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    //<editor-fold>
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    switch (state) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.i(TAG, "BluetoothAdapter is turning on.");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Log.i(TAG, "BluetoothAdapter is on.");
                            //蓝牙已打开，开始搜索并连接service
                            startDiscovery();
                            getBluetoothA2DP();
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.i(TAG, "BluetoothAdapter is turning off.");
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            Log.i(TAG, "BluetoothAdapter is off.");
                            break;
                    }
                    //</editor-fold>
                    break;
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                    state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothDevice.ERROR);

                    if (state == BluetoothA2dp.STATE_CONNECTED) {
                        // connected
                        PubUtils.bluetoothDevices.setConnectStatus("已连接");
                        greendaoUtil.updateEntity(PubUtils.bluetoothDevices);
                        List<BluetoothDevices> arrays = greendaoUtil.queryAllBluetoothDevices();
                        adapter.setDate(arrays);
                        Log.i(TAG, "BluetoothDevice is Connected.");
                    } else if (state == BluetoothA2dp.STATE_DISCONNECTED) {
                        // disconnect
                    }
                    mhandler.sendEmptyMessage(0);
                    break;
                default:
                    break;
            }
        }
    };
    public Dialog dialog_match;

    public BluetoothUtil(Context context, DaoUtils greendaoUtil, BlueListAdapter adapter) {
        this.context = context;
        this.greendaoUtil = greendaoUtil;
        this.adapter = adapter;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        getBluetoothA2DP();
        dialog = new Dialog(context, R.style.DialogStyle);
        dialog.setContentView(R.layout.dialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        mhandler.sendEmptyMessage(0);
        registBluetoothBroadcastReceiver();

    }

    public BluetoothUtil() {

    }

    /**
     * 获取配对时的dialog
     */
    public void matchDialogToShow(Context context) {
        if (dialog_match == null) {
            dialog_match = new Dialog(context, R.style.DialogStyle);
            View view = LayoutInflater.from(context).inflate(R.layout.dialog, null);
            TextView tv = (TextView) view.findViewById(R.id.progress_content_tv);
            tv.setText("正在查找设备配对，请稍后...");
            dialog_match.setContentView(view);
            dialog_match.setCancelable(true);
            dialog_match.setCanceledOnTouchOutside(false);

        }
        dialog_match.show();
    }

    public void appToFinish() {
        mhandler.sendEmptyMessage(1);
        disableAdapter();
    }

    private Handler mhandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://检测蓝牙配对设备
                    List<String> matchStatuss = new ArrayList<>();//已配对蓝牙设备
                    final List<String> connectedStatues = new ArrayList<>();
                    Set<BluetoothDevice> localSet = mBluetoothAdapter.getBondedDevices();
                    for (BluetoothDevice btDevice : localSet) {
                        String mac = btDevice.getAddress();
                        matchStatuss.add(mac);
                        if (mBluetoothA2dp != null) {
                            int a = mBluetoothA2dp.getConnectionState(btDevice);
                            Log.i(TAG, mac + "+++++++++++++" + a);
                            if (a > 1) {
                                connectedStatues.add(mac);
                            }

//                            if (mBluetoothA2dp.getConnectionState(btDevice) == BluetoothA2dp.STATE_CONNECTED || mBluetoothA2dp.getConnectionState(btDevice) == BluetoothA2dp.STATE_PLAYING) {
//                                Log.i(TAG,"已连接+++++++++++++"+mBluetoothA2dp.getConnectionState(btDevice));
//                                connectedStatues.add(mac);
//                            }
                        }

                    }
                    adapter.setMatchAndConnectStatus(matchStatuss, connectedStatues);


                    break;
                case 1://移除任务
                    mhandler.removeMessages(0);
                    break;
                case 2:
                    BluetoothDevice device = (BluetoothDevice) msg.obj;
                    if (mBluetoothA2dp != null) {
                        int status = mBluetoothA2dp.getConnectionState(device);
                        Log.i(TAG, device.getName() + "+++++++++++++" + status);
                        if (status == 0) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            Toast.makeText(context.getApplicationContext(), "无法连接设备，请确保设备开机正常", Toast.LENGTH_LONG).show();
                            mhandler.removeMessages(2);
                        } else if (status == 1) {
                            Message mess = new Message();
                            mess.obj = device;
                            mess.what = 2;
                            mhandler.sendMessageDelayed(mess, 1000);
                        } else if (status == 2) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            mhandler.removeMessages(2);
                        }
                    }
                    break;
                case 3:
                    break;
                default:
                    break;
            }
        }
    };

    public void initParameters() {

        if (mBluetoothAdapter == null) {
            Log.e(TAG, "have no bluetooth adapter.");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        } else {
            //开始搜索附近蓝牙
            startDiscovery();

        }


    }


    /**
     * 注册蓝牙广播接收者
     */
    private void registBluetoothBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);//开始搜索
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//搜索结束
        context.registerReceiver(mBroadcastReceiver, filter);
    }

    private void startDiscovery() {
        Log.i(TAG, "mBluetoothAdapter startDiscovery.");
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {

            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.startDiscovery();
        }
    }

    public int getDeviceConnectedStatus(BluetoothDevice device) {
        int status = mBluetoothA2dp.getConnectionState(device);
        return status;
    }

    /**
     * //绑定BluetoothA2DP，获得service
     */
    public void getBluetoothA2DP() {

        Log.i(TAG, "getBluetoothA2DP");
        if (mBluetoothAdapter == null) {
            return;
        }

        if (mBluetoothA2dp != null) {
            initAdapterData();
            return;
        }

        mBluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.A2DP) {
                    //Service连接成功，获得BluetoothA2DP
                    mBluetoothA2dp = (BluetoothA2dp) proxy;
                    initAdapterData();

                }
            }

            @Override
            public void onServiceDisconnected(int profile) {

            }
        }, BluetoothProfile.A2DP);

    }

    /**
     * 初始化适配器数据
     */
    private void initAdapterData() {

        Set<BluetoothDevice> localSet = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice btDevice : localSet) {
            String mac = btDevice.getAddress();
            int connected = mBluetoothA2dp.getConnectionState(btDevice);
            if (connected > 0) {//已连接
                List<BluetoothDevices> arrays = greendaoUtil.queryAllBluetoothDevices();
                for (BluetoothDevices array : arrays) {
                    if (array.getMac().equals(mac)) {
                        array.setConnectStatus("已连接");
                    }
                }
                adapter.setDate(arrays);
            } else {
                List<BluetoothDevices> arrays = greendaoUtil.queryAllBluetoothDevices();
                for (BluetoothDevices array : arrays) {
                    if (array.getMac().equals(mac)) {
                        array.setConnectStatus("未连接");
                    }
                }
                adapter.setDate(arrays);
            }

        }
    }

    /**
     * 初始化适配器数据
     */
    private void initAdapterData(BluetoothDevice bluetoothDevice) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int connected = mBluetoothA2dp.getConnectionState(bluetoothDevice);
        if (connected > 0) {//已连接
            List<BluetoothDevices> arrays = greendaoUtil.queryAllBluetoothDevices();
            for (BluetoothDevices array : arrays) {
                if (array.getMac().equals(bluetoothDevice.getAddress())) {
                    array.setConnectStatus("已连接");
                } else {
                    array.setConnectStatus("未连接");
                }
            }
            adapter.setDate(arrays);
        } else {
            List<BluetoothDevices> arrays = greendaoUtil.queryAllBluetoothDevices();
            for (BluetoothDevices array : arrays) {
                if (array.getMac().equals(bluetoothDevice.getAddress())) {
                    array.setConnectStatus("未连接");
                }
            }
            adapter.setDate(arrays);
        }

    }

    /**
     * 开始配对
     */
    private void startMarth() {
        Log.i(TAG, "createBond");
        mBluetoothDevice.createBond();
    }

    //connect和disconnect都是hide方法，普通应用只能通过反射机制来调用该方法
    private void connect() {
        if (mBluetoothA2dp == null) {
            return;
        }

        try {
            Method connect = mBluetoothA2dp.getClass().getDeclaredMethod("connect", BluetoothDevice.class);
            connect.setAccessible(true);
            connect.invoke(mBluetoothA2dp, mBluetoothDevice);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Log.e(TAG, "connect exception:" + e);
            e.printStackTrace();
        }
    }

    //connect和disconnect都是hide方法，普通应用只能通过反射机制来调用该方法
    public void connect(final BluetoothDevice bluetoothDevice) {
        if (mBluetoothA2dp == null) {
            mBluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    if (profile == BluetoothProfile.A2DP) {
                        //Service连接成功，获得BluetoothA2DP
                        mBluetoothA2dp = (BluetoothA2dp) proxy;
                        try {
                            Method connect = mBluetoothA2dp.getClass().getDeclaredMethod("connect", BluetoothDevice.class);
                            connect.setAccessible(true);
                            connect.invoke(mBluetoothA2dp, bluetoothDevice);
                            if (dialog != null && !dialog.isShowing()) {
                                dialog.show();
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Message msg = new Message();
                            msg.what = 2;
                            msg.obj = bluetoothDevice;
                            mhandler.sendMessage(msg);
                        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                            Log.e(TAG, "connect exception:" + e);
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onServiceDisconnected(int profile) {

                }
            }, BluetoothProfile.A2DP);
        } else {
            try {
                Method connect = mBluetoothA2dp.getClass().getDeclaredMethod("connect", BluetoothDevice.class);
                connect.setAccessible(true);
                connect.invoke(mBluetoothA2dp, bluetoothDevice);
                if (dialog != null && !dialog.isShowing()) {
                    dialog.show();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message msg = new Message();
                msg.what = 2;
                msg.obj = bluetoothDevice;
                mhandler.sendMessage(msg);

            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                Log.e(TAG, "connect exception:" + e);
                e.printStackTrace();
            }
        }

    }


    /**
     * 断开连接
     */
    private void disconnect() {
        Log.i(TAG, "disconnect");
        if (mBluetoothA2dp == null) {
            return;
        }
        if (mBluetoothDevice == null) {
            return;
        }

        try {
            Method disconnect = mBluetoothA2dp.getClass().getDeclaredMethod("disconnect", BluetoothDevice.class);
            disconnect.setAccessible(true);
            disconnect.invoke(mBluetoothA2dp, mBluetoothDevice);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Log.e(TAG, "connect exception:" + e);
            e.printStackTrace();
        }
    }

    /**
     * 断开连接
     */
    public void disconnect(final BluetoothDevice bluetoothDevice) {
        Log.i(TAG, "disconnect");
        if (mBluetoothA2dp == null) {
            return;
        }

        try {
            Method disconnect = mBluetoothA2dp.getClass().getDeclaredMethod("disconnect", BluetoothDevice.class);
            disconnect.setAccessible(true);
            disconnect.invoke(mBluetoothA2dp, bluetoothDevice);
            initAdapterData(bluetoothDevice);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Log.e(TAG, "connect exception:" + e);
            e.printStackTrace();
        }
    }

    //取消配对
    private void unPairAllDevices() {
        Log.i(TAG, "unPairAllDevices");
        for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
            try {
                Method removeBond = device.getClass().getDeclaredMethod("removeBond");
                removeBond.invoke(device);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    //注意，在程序退出之前（OnDestroy），需要断开蓝牙相关的Service
    //否则，程序会报异常：service leaks
    public void disableAdapter() {
        Log.i(TAG, "disableAdapter");
        if (mBluetoothAdapter == null) {
            return;
        }

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        //关闭ProfileProxy，也就是断开service连接
        mBluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, mBluetoothA2dp);
        if (mBluetoothAdapter.isEnabled()) {
            boolean ret = mBluetoothAdapter.disable();
            Log.i(TAG, "disable adapter:" + ret);
        }
    }
}
