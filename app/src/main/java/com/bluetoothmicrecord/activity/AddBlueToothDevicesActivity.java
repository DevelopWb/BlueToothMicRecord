package com.bluetoothmicrecord.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetoothmicrecord.R;
import com.bluetoothmicrecord.bean.BluetoothDevices;
import com.bluetoothmicrecord.utils.ActivityManager;
import com.bluetoothmicrecord.utils.DaoUtils;
import com.bluetoothmicrecord.utils.PubUtils;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

public class AddBlueToothDevicesActivity extends BaseActivity implements View.OnClickListener {

    /**
     * 蓝牙列表
     */
    private TextView mTitleNameTv;
    private ImageView mTitleRightPicIv;
    /**
     * 布控名称
     */
    private EditText mDeviceNameEt;
    /**
     * 设备账号
     */
    private EditText mDeviceAccountEt;
    /**
     * 设备密码
     */
    private EditText mDevicePasswordEt;
    /**
     * 保存
     */
    private TextView mSaveDeviceInfoTv;
    private ImageView mScanIv;
    /**
     * 设备标识号
     */
    private EditText mDeviceIdEt;
    private DaoUtils greenDaoUtil;
    private ImageView mTitleLeftBackIv;
    private int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_device);
        ActivityManager.getInstance().addActivity(this);
        initView();
        greenDaoUtil = new DaoUtils(this);
    }

    private void initView() {
        mTitleNameTv = (TextView) findViewById(R.id.title_name_tv);
        mTitleNameTv.setText("添加蓝牙设备");
        mTitleRightPicIv = (ImageView) findViewById(R.id.title_right_add_iv);
        mTitleRightPicIv.setVisibility(View.INVISIBLE);
        mDeviceNameEt = (EditText) findViewById(R.id.device_name_et);
        mDeviceAccountEt = (EditText) findViewById(R.id.device_account_et);
        mDevicePasswordEt = (EditText) findViewById(R.id.device_password_et);
        mSaveDeviceInfoTv = (TextView) findViewById(R.id.save_device_info_tv);
        mSaveDeviceInfoTv.setOnClickListener(this);
        mScanIv = (ImageView) findViewById(R.id.scan_iv);
        mScanIv.setOnClickListener(this);
        mDeviceIdEt = (EditText) findViewById(R.id.device_id_et);
        mTitleLeftBackIv = (ImageView) findViewById(R.id.title_left_back_iv);
        mTitleLeftBackIv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_device_info_tv://保存添加设备信息
                String name = mDeviceNameEt.getText().toString().trim();
                String id = mDeviceIdEt.getText().toString().trim();
                String account = mDeviceAccountEt.getText().toString().trim();
                String password = mDevicePasswordEt.getText().toString().trim();


                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getApplicationContext(), "布控名称不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(id)) {
                    Toast.makeText(getApplicationContext(), "标识号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(account)) {
                    Toast.makeText(getApplicationContext(), "设备账号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "设备密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (greenDaoUtil.checkDeviceWhetherSaved(id)) {
                    Toast.makeText(getApplicationContext(), "设备已添加", Toast.LENGTH_SHORT).show();
                    return;
                }
                Long time = System.currentTimeMillis();
                BluetoothDevices bean = new BluetoothDevices();
                bean.setDeviceName(name);
                bean.setMac(id);
                bean.setDeviceAccount(account);
                bean.setDevicePassword(password);
                bean.setAddTime(PubUtils.getDateToString(time));
                bean.setMatchStatus("未配对");
                bean.setConnectStatus("未连接");
                bean.setHasRecordAudio(false);
                greenDaoUtil.insertBluetoothDevices(bean);
                Intent intent = new Intent();
                setResult(PubUtils.ADDEDBLUEACTIVITY_FINISH, intent);
                finish();

                break;
            case R.id.scan_iv://二维码扫描的按钮
                Intent intent_scan = new Intent(AddBlueToothDevicesActivity.this, CaptureActivity.class);
                startActivityForResult(intent_scan, REQUEST_CODE);
                break;
            case R.id.title_left_back_iv:
                Intent intentBack = new Intent();
                setResult(PubUtils.ACTIVITY_FINISH,intentBack);
                finish();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * 处理二维码扫描结果
         */


        if (requestCode == REQUEST_CODE) {
            PubUtils.scanResult = true;
            //处理扫描结果（在界面上显示）
            if (null != data) {

                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    if (!TextUtils.isEmpty(result)) {
                        String[] results = result.split(",");
                        mDeviceIdEt.setText(results[0]);
                        mDeviceAccountEt.setText(results[1]);
                        mDevicePasswordEt.setText(results[2]);
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(AddBlueToothDevicesActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }

    }


}
