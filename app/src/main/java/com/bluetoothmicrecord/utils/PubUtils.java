package com.bluetoothmicrecord.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.TextView;

import com.bluetoothmicrecord.R;
import com.bluetoothmicrecord.bean.BluetoothDevices;
import com.bluetoothmicrecord.bean.RecordAudioFile;
import com.bluetoothmicrecord.upload.uploadUtil.PreferenceUtil;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ${王sir} on 2017/9/8.
 * application
 */

public class PubUtils {
    public static boolean PwdDialogCanBeShow = false;//RecordAudioActivity退出时的状态值
    public static boolean scanResult = false;//二维码扫描结果
    public static boolean iscancel = false;//是否取消打开蓝牙
    public static boolean upload = false;//uploadService中是否轮询未上传文件
    public static boolean status = false;//是否取消打开蓝牙
    public static boolean status2 = false;//是否取消打开蓝牙
    public static String pin_password = "0000";//蓝牙匹配密码
    public static String deviceName = "";//配对蓝牙设备名称
    public static BluetoothDevices bluetoothDevices;//点击的实体类
    public static BluetoothDevices AudiosDevice;//录制音频中点击的实体类
    // 用于格式化日期,作为日志文件名的一部分
    private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    public static String AudioFileType = ".m4a";
    public static List<RecordAudioFile> selectedFiles = new ArrayList<>();//存放选中item的集合

    public static String databaseAddress = "kc.xun365.net";//默认后台ip
    public static int ACTIVITY_FINISH = 77;//添加蓝牙设备不成功Activity退出
    public static int ADDEDBLUEACTIVITY_FINISH = 99;//添加蓝牙设备的成功时Activity退出


    private static String RootPath  = Environment.getExternalStorageDirectory().getAbsolutePath() ;
    private static String AudioFileSavedPath =RootPath + "/SavedAudio/";




    // 将时间戳转成字符串
    public static String getDateToString(long time) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return sf.format(d);
    }

    /**
     * 文字颜色
     */
    public static void setTextViewColor(Context context, TextView tv, String content, boolean sure) {
        ForegroundColorSpan span;
        int start = 0;
        int end = 0;
        if (content != null & !("").equals(content)) {
            start = content.indexOf("：") + 1;
            end = content.length();
        }
        SpannableString spanString = new SpannableString(content);
        if (sure) {
            span = new ForegroundColorSpan(context.getResources().getColor(R.color.green));
        } else {
            span = new ForegroundColorSpan(Color.RED);
        }
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        spanString.setSpan(styleSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(spanString);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 更新本地bean 数据
     */
    public static void updateLocalSqlBean(Context context,RecordAudioFile recordAudioFile){
        DaoUtils  greenDaoUtil = new DaoUtils(context);
        greenDaoUtil.updateEntity(recordAudioFile);
    }
    /**
     * 获取保存录制音频的路径
     *
     * @return
     */
    public static String getSavedAudioFilePath(BluetoothDevices bean) {
        String time = formatter.format(new Date());
        File savedDir = new File( AudioFileSavedPath+ bean.getDeviceName().trim() + "-" + bean.getDeviceAccount().trim());
        if (!savedDir.exists()) {
            savedDir.mkdirs();
        }
        String savedPath = savedDir.getAbsolutePath() + "/" + time + AudioFileType;

        return savedPath;
    }

    /**
     * 获取文件目录的名称
     * @param path
     * @return
     */
    public static String getAudioFileDirName(String path) {
    if (!TextUtils.isEmpty(path)) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = file.getName();
        return fileName;
    }
    return "";
    }
    /**
     * 获取文件的名称
     * @param path
     * @return
     */
    public static String getAudioFileName(String path) {
    if (!TextUtils.isEmpty(path)) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = file.getName();
        String [] fileNames = fileName.split("\\.");
        return fileNames[0];
    }
    return "";
    }

    /**
     * 获取保存录制音频的目录路径
     *
     * @return
     */
    public static String getSavedAudioDirPath(BluetoothDevices bean) {
        File savedDir = new File(AudioFileSavedPath+ bean.getDeviceName().trim() + "-" + bean.getDeviceAccount().trim());
        if (!savedDir.exists()) {
            savedDir.mkdirs();
        }
        return savedDir.getAbsolutePath();
    }

    public static String getRemoteFileName() {
        String fileName = "";
        //注册码
        String registerCode = "2bgz12yp";
        //获取系统时间
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String time = df.format(System.currentTimeMillis());
        String randCode = "";
        randCode += (int) (Math.random() * 9 + 1);
        for (int i = 0; i < 5; i++) {
            randCode += (int) (Math.random() * 10);
        }
        fileName = registerCode + randCode+time+PubUtils.AudioFileType;
        return fileName;
    }

    public static void removeFileName(String oldFile) {
        PreferenceUtil mPrefManager = PreferenceUtil.getInstance(null, null);
        if (mPrefManager != null) {
            mPrefManager.remove(oldFile);
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName
     * @return
     */
    public static String getFileFormat(String fileName) {
        if (TextUtils.isEmpty(fileName))
            return "";

        int point = fileName.lastIndexOf('.');
        return fileName.substring(point + 1);
    }

    /**
     * 获取指定文件大小
     * @return
     * @throws Exception 　　
     */
    public static String  getFileSize(File file) throws Exception {
        long size = 0;
        String fileSize = "";
        DecimalFormat  df = new DecimalFormat("0.0");

        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available()/1024;
            if (size>1000) {
                fileSize = df.format(size/1024)+"MB";
            }else{
                fileSize = df.format(size)+"KB";
            }
        } else {
            file.createNewFile();
            Log.e("获取文件大小", "文件不存在!");
        }
        return fileSize;
    }

    /**
     * ip正则
     * @param ipaddr
     * @return
     */
    public static boolean isIPAddress(String ipaddr) {
        boolean flag = false;
        Pattern pattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
        Matcher m = pattern.matcher(ipaddr);
        flag = m.matches();
        return flag;
    }
}
