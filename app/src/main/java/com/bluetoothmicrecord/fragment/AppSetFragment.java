package com.bluetoothmicrecord.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetoothmicrecord.R;
import com.bluetoothmicrecord.upload.uploadUtil.PreferenceUtil;
import com.bluetoothmicrecord.utils.PubUtils;

import java.io.OutputStream;

import static android.app.Activity.RESULT_OK;

/**
 * Created by ${王sir} on 2017/9/8.
 * application
 */

public class AppSetFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_IS_HIDDEN = "AppSetFragment";
    PreferenceUtil mPrefManager = null;
    private Context context;
    private View view;
    /**
     * /storage/0/sdcard/
     */
    private TextView mAudioFileSavedPathDisplay;
    private RelativeLayout mAudioFileSavedPathRl;
    /**
     * 开启
     */
    private RadioButton mOpenAutoUpload;
    /**
     * 关闭
     */
    private RadioButton mCloseAutoUpload;
    private RadioGroup mAutoUploadRg;
    /**
     * 移动网络
     */
    private RadioButton mUploadNetworkMobile;
    /**
     * WIFI
     */
    private RadioButton mUploadNetworkWifi;
    private RadioGroup mAutoUploadNetWorkRg;
    /**
     * 是
     */
    private RadioButton mDeleteOriginalFile;
    /**
     * 否
     */
    private RadioButton mNotDeleteOriginalFile;
    private RadioGroup mDeleteLocalAudioFileRg;
    private RelativeLayout mChangePwdRl;
    private RelativeLayout mServerIpRl;

    private int clickTimes = 0;//点击次数

    //用于判断跳转Intent类型
    private static final int DIRECTORY_CHOOSE_REQ_CODE = 1;


    private UploadServiceOpenableInterface uploadServiceOpenableInterface;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        mPrefManager = PreferenceUtil.getInstance(context, "APPSET");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.app_set, container, false);
        initView(view);
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        initRadioButtonsStatus();

    }

    //初始化view
    private void initView(View view) {
        mAudioFileSavedPathDisplay = (TextView) view.findViewById(R.id.audioFile_savedPath_display);
        mAudioFileSavedPathRl = (RelativeLayout) view.findViewById(R.id.audioFile_savedPath_rl);
        mAudioFileSavedPathRl.setOnClickListener(this);
        mOpenAutoUpload = (RadioButton) view.findViewById(R.id.open_autoUpload);
        mCloseAutoUpload = (RadioButton) view.findViewById(R.id.close_autoUpload);
        mAutoUploadRg = (RadioGroup) view.findViewById(R.id.autoUpload_rg);
        mUploadNetworkMobile = (RadioButton) view.findViewById(R.id.upload_network_mobile);
        mUploadNetworkWifi = (RadioButton) view.findViewById(R.id.upload_network_wifi);
        mAutoUploadNetWorkRg = (RadioGroup) view.findViewById(R.id.autoUpload_netWork_rg);
        mDeleteOriginalFile = (RadioButton) view.findViewById(R.id.delete_original_file);
        mNotDeleteOriginalFile = (RadioButton) view.findViewById(R.id.not_delete_original_file);
        mDeleteLocalAudioFileRg = (RadioGroup) view.findViewById(R.id.deleteLocalAudioFile_rg);
        mChangePwdRl = (RelativeLayout) view.findViewById(R.id.change_pwd_rl);
        mChangePwdRl.setOnClickListener(this);
        mServerIpRl = (RelativeLayout) view.findViewById(R.id.serverIp_rl);
        mServerIpRl.setOnClickListener(this);
        mOpenAutoUpload.setOnClickListener(this);
        mCloseAutoUpload.setOnClickListener(this);
        mUploadNetworkMobile.setOnClickListener(this);
        mUploadNetworkWifi.setOnClickListener(this);
        mDeleteOriginalFile.setOnClickListener(this);
        mNotDeleteOriginalFile.setOnClickListener(this);
    }

    /**
     * 初始化所有的RadioButton的选择状态
     */
    private void initRadioButtonsStatus() {

        boolean autoUpload = mPrefManager.getBoolean("AUTOUPLOAD");
        boolean deleteFile = mPrefManager.getBoolean("DELETEFILE");
        String netType = mPrefManager.getString("NETWORKTYPE");
        if (autoUpload) {
            mAutoUploadRg.check(R.id.open_autoUpload);
        } else {
            mAutoUploadRg.check(R.id.close_autoUpload);
        }
        if (deleteFile) {
            mDeleteLocalAudioFileRg.check(R.id.delete_original_file);
        } else {
            mDeleteLocalAudioFileRg.check(R.id.not_delete_original_file);
        }
        if ("mobile".equals(netType)) {
            mAutoUploadNetWorkRg.check(R.id.upload_network_mobile);
        } else {
            mAutoUploadNetWorkRg.check(R.id.upload_network_wifi);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audioFile_savedPath_rl://文件保存路径
//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//                startActivityForResult(intent, DIRECTORY_CHOOSE_REQ_CODE);
                break;
            case R.id.change_pwd_rl://更改密码
                showChangePwdDialog();
                break;
            case R.id.serverIp_rl://服务器IP
                clickTimes++;
                if (clickTimes > 9) {
                    clickTimes = 0;
                    showSetServerIpDialog();
                } else {
                    Toast.makeText(context.getApplicationContext(), "如果想设置服务器IP，请联系管理员", Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.open_autoUpload://开启自动上传
                mPrefManager.put("AUTOUPLOAD", true);
                if (uploadServiceOpenableInterface != null) {
                    uploadServiceOpenableInterface.startUploadService();
                }
                break;
            case R.id.close_autoUpload://关闭自动上传

                mPrefManager.put("AUTOUPLOAD", false);
                if (uploadServiceOpenableInterface != null) {
                    uploadServiceOpenableInterface.closeUploadService();
                }
                break;
            case R.id.upload_network_mobile:
                mPrefManager.put("NETWORKTYPE", "mobile");
                break;
            case R.id.upload_network_wifi:
                mPrefManager.put("NETWORKTYPE", "wifi");

                break;
            case R.id.delete_original_file:
                mPrefManager.put("DELETEFILE", true);
                break;
            case R.id.not_delete_original_file:
                mPrefManager.put("DELETEFILE", false);
                break;
        }
    }

    /**
     * 更改密码的对话框
     */
    private void showChangePwdDialog() {
        View v = LayoutInflater.from(context).inflate(R.layout.change_pwd, null);
        final EditText et1 = (EditText) v.findViewById(R.id.pwd_et1);
        final EditText et2 = (EditText) v.findViewById(R.id.pwd_et2);
        TextView tv1 = (TextView) v.findViewById(R.id.pwd_tv1);
        TextView tv2 = (TextView) v.findViewById(R.id.pwd_tv2);
        final Dialog dialog_c = new Dialog(context, R.style.DialogStyle);
        dialog_c.setCanceledOnTouchOutside(false);
        dialog_c.show();
        Window window = dialog_c.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER);
        lp.width = PubUtils.dip2px(context, 300); // 宽度
        lp.height = PubUtils.dip2px(context, 225); // 高度
        //lp.dimAmount = 0f;//去掉对话框自带背景色
        window.setAttributes(lp);
        window.setContentView(v);
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pwd = mPrefManager.getString("PASSWORD");
                if (TextUtils.isEmpty(pwd)) {
                    pwd = "8888888";
                }
                String pwd1 = et1.getText().toString().trim();
                String pwd2 = et2.getText().toString().trim();
                if (TextUtils.isEmpty(pwd1)) {
                    Toast.makeText(context, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(pwd2)) {
                    Toast.makeText(context, "请再输入一次密码", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!pwd1.equals(pwd2)) {
                    Toast.makeText(context, "两次输入的密码不一致，请重新输入", Toast.LENGTH_SHORT).show();
                    return;
                } else if (pwd1.equals(pwd2)) {
                    if (pwd.equals(pwd1)) {
                        Toast.makeText(context, "新密码与原密码一致，请重新输入", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        mPrefManager.put("PASSWORD", pwd1);
                        Toast.makeText(context, "密码更改成功", Toast.LENGTH_SHORT).show();
                        dialog_c.dismiss();
                    }

                }


            }
        });
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_c.dismiss();
            }
        });
    }

    /**
     * 更改服务器ip的对话框
     */
    private void showSetServerIpDialog() {
        View v = LayoutInflater.from(context).inflate(R.layout.setting_server_ip, null);
        final EditText ftpIP = (EditText) v.findViewById(R.id.ftp_ip);
        final EditText ftpPort = (EditText) v.findViewById(R.id.ftp_port);
        final EditText dataBaseAddress = (EditText) v.findViewById(R.id.database);

        Button settingOK = (Button) v.findViewById(R.id.setting_ok);
        Button settingCancle = (Button) v.findViewById(R.id.setting_cancle);
        final Dialog dialog_c = new Dialog(context, R.style.DialogStyle);
        dialog_c.setCanceledOnTouchOutside(false);
        dialog_c.show();
        Window window = dialog_c.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER);
//        lp.width = PubUtils.dip2px(context, 300); // 宽度
//        lp.height = PubUtils.dip2px(context, 225); // 高度
        //lp.dimAmount = 0f;//去掉对话框自带背景色
        window.setAttributes(lp);
        window.setContentView(v);
        initServerSetInfo(ftpIP, ftpPort, dataBaseAddress);
        settingOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ftpIP.getText().toString().trim();
                String port = ftpPort.getText().toString().trim();
                String dbAddr = dataBaseAddress.getText().toString().trim();
                if (TextUtils.isEmpty(ip)) {
                    Toast.makeText(context.getApplicationContext(), "请输入FTP服务器IP地址", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    if (!PubUtils.isIPAddress(ip)) {
                        Toast.makeText(context.getApplicationContext(), "FTP服务器IP地址格式有误，请重新输入", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                if (TextUtils.isEmpty(port)) {
                    Toast.makeText(context.getApplicationContext(), "请输入FTP服务器端口号", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(dbAddr)) {
                    Toast.makeText(context.getApplicationContext(), "请输入数据库地址", Toast.LENGTH_LONG).show();
                    return;
                }
                mPrefManager.put("FTP_IP", ip);
                mPrefManager.put("FTP_PORT", port);
                mPrefManager.put("DB_ADDRESS", dbAddr);
                dialog_c.dismiss();

            }
        });
        settingCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_c.dismiss();
            }
        });
    }

    /**
     * 初始化设置服务器的信息
     */
    private void initServerSetInfo(EditText ftpIP, EditText ftpPort, EditText dataBaseAddress) {
        String ip = mPrefManager.getString("FTP_IP");
        String port = mPrefManager.getString("FTP_PORT");
        String dbAddr = mPrefManager.getString("DB_ADDRESS");
        if (!TextUtils.isEmpty(ip)) {
            ftpIP.setText(ip);
        }
        if (!TextUtils.isEmpty(port)) {
            ftpPort.setText(port);
        }
        if (!TextUtils.isEmpty(dbAddr)) {
            dataBaseAddress.setText(dbAddr);
        }

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            clickTimes = 0;
        } else {
            onResume();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == DIRECTORY_CHOOSE_REQ_CODE) {
            //获取返回的Uri
            Uri uri = data.getData();
            mkdirsOnTFCard(uri);
        }
    }

    /**
     * DocumentFile外置SD卡创建文件夹
     */
    public void mkdirsOnTFCard(Uri uri) {
        //创建DocumentFile
        DocumentFile pickedDir = DocumentFile.fromTreeUri(context, uri);
        /**
         * 如果没有该文件夹,则创建一个新的文件并写入内容
         * 查询文件是否存在时,假如文件存在,则返回true;不存在时不会返回false,而是返回null
         * 所以这里应该用try-catch来判断,出现异常则不存在此文件
         */
        boolean ishasDirectory;
        try {
            ishasDirectory = pickedDir.findFile("WENWENWEN888").exists();
        } catch (Exception e) {
            ishasDirectory = false;
        }
        if (!ishasDirectory) {
            try {
                //创建新的一个文件夹
                pickedDir.createDirectory("WENWENWEN888");
                //找到新文件夹的路径
                pickedDir = pickedDir.findFile("WENWENWEN888");
                //创建新的文件
                DocumentFile newFile = pickedDir.createFile("text/plain", "new_file");
                //写入内容到新建文件
                OutputStream out = context.getContentResolver().openOutputStream(newFile.getUri());
                if (out != null) {
                    out.write("测试".getBytes());
                    out.close();
                }
                Toast.makeText(context, "创建成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, "创建失败", Toast.LENGTH_SHORT).show();
                Log.e("Exception", "DocumentFile创建失败:" + e);
            }
        }
    }


    public void setUploadServiceOpenableCallBack(UploadServiceOpenableInterface uploadServiceOpenableInterface) {
        this.uploadServiceOpenableInterface = uploadServiceOpenableInterface;
    }

    public interface UploadServiceOpenableInterface {
        void startUploadService();

        void closeUploadService();
    }
}
