package com.bluetoothmicrecord;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.storage.StorageManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetoothmicrecord.activity.AddBlueToothDevicesActivity;
import com.bluetoothmicrecord.activity.UploadService;
import com.bluetoothmicrecord.adapter.RecordAudiosActivityAdapter;
import com.bluetoothmicrecord.bean.RecordAudioFile;
import com.bluetoothmicrecord.fragment.AppSetFragment;
import com.bluetoothmicrecord.fragment.BlueListFragment;
import com.bluetoothmicrecord.fragment.RecordAudioListFragment;
import com.bluetoothmicrecord.upload.uploadUtil.PreferenceUtil;
import com.bluetoothmicrecord.utils.ActivityManager;
import com.bluetoothmicrecord.utils.BluetoothUtil;
import com.bluetoothmicrecord.utils.DaoUtils;
import com.bluetoothmicrecord.utils.PubUtils;
import com.uploadbugs.utils.BugPublicUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.bluetoothmicrecord.utils.PubUtils.PwdDialogCanBeShow;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTitleNameTv;
    private ImageView mTitleRightPicIv;
    private FrameLayout mMainFl;
    private ImageView mBluetoothListIv;
    private TextView mBluetoothListNameTv;
    private LinearLayout mBluetoothListLl;
    private ImageView mRecordAudioIv;
    private TextView mRecordAudioNameTv;
    private LinearLayout mRecordAudioLl;
    private ImageView mAppSetIv;
    private TextView mAppSetTv;
    private LinearLayout mAppSetLl;
    private DaoUtils greenDaoUtil;


    private int bottomBtPress = 1;//1代表蓝牙列表被点击，2代表录音设备被点击，3代表设置被点击
    private BluetoothUtil bluetoothUtil;
    private ImageView mTitleLeftBackIv;
    private RecordAudiosActivityAdapter adapter;
    private MyConn conn;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private boolean PwdDialogToShow = true;//密码框是否弹出
    private Dialog dialog_input;
    PreferenceUtil mPrefManager = null;
    private AppSetFragment appSetFragment;
    private BlueListFragment blueListFragment;
    private RecordAudioListFragment recordAudioListFragment;
    private FragmentManager fragmentManager;
    private boolean isExit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            appSetFragment = new AppSetFragment();
            blueListFragment = new BlueListFragment();
            recordAudioListFragment = new RecordAudioListFragment();
        } else {
            appSetFragment = (AppSetFragment) fragmentManager.getFragment(savedInstanceState, "appSetFragment");
            blueListFragment = (BlueListFragment) fragmentManager.getFragment(savedInstanceState, "blueListFragment");
            recordAudioListFragment = (RecordAudioListFragment) fragmentManager.getFragment(savedInstanceState, "recordAudioListFragment");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        BugPublicUtils.checkToUploadBugInfos(this,"ZNZT_crash","http://zc.xun365.net/WebService/SoftWare.asmx/SetBugInfo","201801181009","ZNZT");

        ActivityManager.getInstance().addActivity(this);
        initView();
        initBottomButtonStatus(0);
        initFragmentSelected(0);
        bluetoothUtil = new BluetoothUtil();
        mPrefManager = PreferenceUtil.getInstance(this, "APPSET");
        if (mPrefManager.getBoolean("AUTOUPLOAD")) {
            startUploadServiceNow();
            PubUtils.upload = true;
        }
        appSetFragment.setUploadServiceOpenableCallBack(new AppSetFragment.UploadServiceOpenableInterface() {
            @Override
            public void startUploadService() {

                startUploadServiceNow();
                PubUtils.upload = true;
            }

            @Override
            public void closeUploadService() {
                if (conn != null) {
                    unbindService(conn);
                }
                PubUtils.upload = false;
            }
        });
    }


    /**
     * 获取手机内置SD卡和外置SD卡
     *
     * @param context
     * @return
     */
    public String[] getVolumePaths(Context context) {
        StorageManager mStorageManager;
        Method mMethodGetPaths = null;
        mStorageManager = (StorageManager) context
                .getSystemService(Activity.STORAGE_SERVICE);
        try {
            mMethodGetPaths = mStorageManager.getClass()
                    .getMethod("getVolumePaths");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        String[] paths = null;
        try {
            paths = (String[]) mMethodGetPaths.invoke(mStorageManager);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return paths;
    }
//    public static void verifyStoragePermissions(Activity activity) {
//
//        try {
//            //检测是否有写的权限
//            int permission = ActivityCompat.checkSelfPermission(activity,
//                    "android.permission.WRITE_EXTERNAL_STORAGE");
//            int permission2 = ActivityCompat.checkSelfPermission(activity,
//                    "android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
//            if (permission2 != PackageManager.PERMISSION_GRANTED) {
//                // 没有写的权限，去申请写的权限，会弹出对话框
//                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void startUploadServiceNow() {

        conn = new MyConn();
        bindService(new Intent(this, UploadService.class), conn, Context.BIND_AUTO_CREATE);

    }

    private class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            UploadService.MyBinder binder = (UploadService.MyBinder) iBinder;
            UploadService service = binder.getUploadService();
            service.setUploadServiceCallBack(new UploadService.uploadServiceInterface() {
                @Override
                public void warn(String warnStr) {
                    Log.e("TAG", warnStr);
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!PwdDialogCanBeShow) {
            if (PwdDialogToShow) {
                if (dialog_input == null) {
                    showPassWordInput();
                } else {
                    if (!dialog_input.isShowing()) {
                        showPassWordInput();
                    }
                }
            } else {
                PwdDialogToShow = true;
            }
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        PwdDialogCanBeShow = false;
    }

    /**
     * 密码输入提示框
     */
    private void showPassWordInput() {

        View v = LayoutInflater.from(this).inflate(R.layout.password_input, null);
        dialog_input = new Dialog(this, R.style.DialogStyle);
        dialog_input.setCanceledOnTouchOutside(false);
        dialog_input.setCancelable(false);
        dialog_input.show();
        Window window = dialog_input.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        lp.width = PubUtils.dip2px(this, 300); // 宽度
        lp.height = PubUtils.dip2px(this, 240); // 高度
        // lp.alpha = 0.7f; // 透明度
        window.setAttributes(lp);
        window.setContentView(v);
        dialog_input.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if (keyCode == event.KEYCODE_BACK) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        finish();
                    }
                }
                return false;
            }
        });
        final EditText et_password = (EditText) v.findViewById(R.id.et_password);
        ImageButton btn_ok = (ImageButton) v.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String et_pwd = et_password.getText().toString().trim();
                if (et_password == null || TextUtils.isEmpty(et_pwd)) {
                    Toast.makeText(MainActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getPwd().equals(et_pwd)) {
                    dialog_input.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "密码不正确，请重新输入", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 获取设置的验证密码
     *
     * @return
     */
    private String getPwd() {
        PreferenceUtil mPrefManager = PreferenceUtil.getInstance(this, "APPSET");
        String pwd = mPrefManager.getString("PASSWORD");
        if (TextUtils.isEmpty(pwd)) {
            pwd = "8888888";
        }
        return pwd;
    }

    /**
     * 将所有fragment隐藏
     */
    private void hideFrament(FragmentTransaction fragmentTransaction) {
        if (blueListFragment != null) {
            fragmentTransaction.hide(blueListFragment);
        }
        if (recordAudioListFragment != null) {
            fragmentTransaction.hide(recordAudioListFragment);
        }
        if (appSetFragment != null) {
            fragmentTransaction.hide(appSetFragment);
        }
    }


    /**
     * 初始化底部按钮状态
     */

    private void initBottomButtonStatus(int position) {
        mBluetoothListIv.setImageResource(R.drawable.bluetooth_list_normal);
        mBluetoothListNameTv.setTextColor(getResources().getColor(R.color.white));
        mRecordAudioIv.setImageResource(R.drawable.record_his_normal);
        mRecordAudioNameTv.setTextColor(getResources().getColor(R.color.white));
        mAppSetIv.setImageResource(R.drawable.app_set_normal);
        mAppSetTv.setTextColor(getResources().getColor(R.color.white));
        switch (position) {
            case 0://蓝牙列表
                bottomBtPress = 1;
                initTopToolBar(0);
                mBluetoothListIv.setImageResource(R.drawable.bluetooth_list_press);
                mBluetoothListNameTv.setTextColor(getResources().getColor(R.color.bottomBtTextColor));
                break;
            case 1://录制音频
                bottomBtPress = 2;
                initTopToolBar(1);
                mRecordAudioIv.setImageResource(R.drawable.record_his_press);
                mRecordAudioNameTv.setTextColor(getResources().getColor(R.color.bottomBtTextColor));
                break;
            case 2://设置
                bottomBtPress = 3;
                initTopToolBar(2);
                mAppSetIv.setImageResource(R.drawable.app_set_press);
                mAppSetTv.setTextColor(getResources().getColor(R.color.bottomBtTextColor));
                break;
            default:
                break;
        }
    }

    /**
     * 初始化顶部布局
     */
    private void initTopToolBar(int i) {
        switch (i) {
            case 0:
                mTitleNameTv.setText("侦控列表");
                mTitleRightPicIv.setVisibility(View.VISIBLE);
                mTitleLeftBackIv.setVisibility(View.INVISIBLE);
                mTitleRightPicIv.setImageResource(R.drawable.add_device_selector);
                break;
            case 1:
                mTitleNameTv.setText("录制音频");
                mTitleRightPicIv.setVisibility(View.INVISIBLE);
                mTitleLeftBackIv.setVisibility(View.INVISIBLE);
                break;
            case 2:
                mTitleNameTv.setText("设置");
                mTitleRightPicIv.setVisibility(View.INVISIBLE);
                mTitleLeftBackIv.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }

    }

    private void initView() {
        mTitleNameTv = (TextView) findViewById(R.id.title_name_tv);
        mTitleRightPicIv = (ImageView) findViewById(R.id.title_right_add_iv);
        mTitleRightPicIv.setOnClickListener(this);
        mMainFl = (FrameLayout) findViewById(R.id.main_fl);
        mBluetoothListIv = (ImageView) findViewById(R.id.BluetoothList_iv);
        mBluetoothListNameTv = (TextView) findViewById(R.id.BluetoothListName_tv);
        mBluetoothListLl = (LinearLayout) findViewById(R.id.BluetoothList_ll);
        mBluetoothListLl.setOnClickListener(this);
        mRecordAudioIv = (ImageView) findViewById(R.id.recordAudio_iv);
        mRecordAudioNameTv = (TextView) findViewById(R.id.recordAudioName_tv);
        mRecordAudioLl = (LinearLayout) findViewById(R.id.recordAudio_ll);
        mRecordAudioLl.setOnClickListener(this);
        mAppSetIv = (ImageView) findViewById(R.id.appSet_iv);
        mAppSetTv = (TextView) findViewById(R.id.appSet_tv);
        mAppSetLl = (LinearLayout) findViewById(R.id.appSet_ll);
        mAppSetLl.setOnClickListener(this);
        mTitleLeftBackIv = (ImageView) findViewById(R.id.title_left_back_iv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_right_add_iv://添加蓝牙设备
                startActivityForResult(new Intent(this, AddBlueToothDevicesActivity.class), 0);
                break;
            case R.id.BluetoothList_ll://底部蓝牙列表
                initBottomButtonStatus(0);
                initFragmentSelected(0);
                break;
            case R.id.recordAudio_ll://底部录制音频
                initBottomButtonStatus(1);
                initFragmentSelected(1);
                break;
            case R.id.appSet_ll://底部设置
                initBottomButtonStatus(2);
                initFragmentSelected(2);
                break;
        }
    }

    /**
     * 初始化fragment
     *
     * @param i
     */
    private void initFragmentSelected(int i) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        hideFrament(fragmentTransaction);
        switch (i) {
            case 0:
                if (!blueListFragment.isAdded()) {
                    fragmentTransaction.add(R.id.main_fl, blueListFragment, "blueListFragment");
                }
                fragmentTransaction.show(blueListFragment);
                break;
            case 1:
                if (!recordAudioListFragment.isAdded()) {
                    fragmentTransaction.add(R.id.main_fl, recordAudioListFragment, "recordAudioListFragment");
                }
                fragmentTransaction.show(recordAudioListFragment);
                break;
            case 2:
                if (!appSetFragment.isAdded()) {
                    fragmentTransaction.add(R.id.main_fl, appSetFragment, "appSetFragment");
                }
                fragmentTransaction.show(appSetFragment);
                break;
            default:
                break;
        }
        fragmentTransaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PwdDialogToShow = false;
        switch (resultCode) {

            case 0://该结果码要与Fragment中的一致,0代表不同意打开蓝牙
                super.onActivityResult(requestCode, resultCode, data);
                break;
            case -1://该结果码要与Fragment中的一致,-1代表同意打开蓝牙
                super.onActivityResult(requestCode, resultCode, data);
                break;
            case 99:
                Toast.makeText(getApplicationContext(), "设备添加成功", Toast.LENGTH_LONG).show();
                break;
            case 77:
                break;
            default:
                break;
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothUtil.appToFinish();

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100://上传进度
                    Message msg100 = new Message();
                    msg100.what = 1;
                    msg100.obj = msg.obj;
                    mHandler.sendMessage(msg100);
                    break;
                case 1:
                    RecordAudioFile recordAudioFile = (RecordAudioFile) msg.obj;
                    greenDaoUtil.updateEntity(recordAudioFile);
                    adapter.notifyDataSetChanged();
                    break;
                case 6:
                    Toast.makeText(getApplicationContext(), "没有网络", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }


            super.handleMessage(msg);
        }
    };

    /**
     * 测试数据
     *
     * @return
     */
    private List<RecordAudioFile> getDataForAdapter() {

        List<RecordAudioFile> arrays = new ArrayList<>();

        String fileDir = "/storage/emulated/0/qqmusic";
        File savedFile = new File(fileDir);
        File[] files = savedFile.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                RecordAudioFile recordAudioFile = new RecordAudioFile();
                recordAudioFile.setRecordTime(PubUtils.getDateToString(System.currentTimeMillis()));
                recordAudioFile.setFilePath(file.getPath());
                recordAudioFile.setFileName(PubUtils.getAudioFileName(file.getPath()));
                recordAudioFile.setFileDir(fileDir);
                recordAudioFile.setUpLoadStatus("0");
                recordAudioFile.setUploadProgress(0);
                arrays.add(recordAudioFile);
            }


        }
        return arrays;
    }


    @Override
    public void onBackPressed() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    isExit = false;
                }

            }, 2000);
        } else {
            super.onBackPressed();
        }


    }
}
