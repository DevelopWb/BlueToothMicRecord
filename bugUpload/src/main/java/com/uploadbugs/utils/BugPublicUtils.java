package com.uploadbugs.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.uploadbugs.bean.BugBean;
import com.uploadbugs.service.BugUploadIntentService;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * Created by ${王sir} on 2017/10/27.
 * application
 */

public class BugPublicUtils {
    public static String bugSavedPath = "";//bug本地保存地址
    public static String url = "";//bug上传地址
    private Context context;
    private String regcode;//注册码
    public static String softwaremark;//软件标识
    public static List<BugBean> arrays;//本地保存的bug信息

    public static void checkToUploadBugInfos(Context context,String savedDirName, String uploadUrl, String regCode, String softMark) {
        String path = Environment.getExternalStorageDirectory()+"/"+savedDirName;
        BugPublicUtils bugUtil = new BugPublicUtils(context).setBugSavedPath(path);
        if (BugPublicUtils.checkBugs(path)) {
            bugUtil.setUrl(uploadUrl).setRegCode(regCode).setSoftwareMark(softMark).searchFileDataToUpload();
        }
    }

    public BugPublicUtils(Context context) {
        this.context = context;

    }

    public BugPublicUtils setRegCode(String regCode) {
        this.regcode = regCode;
        return this;
    }

    public BugPublicUtils setUrl(String url) {
        BugPublicUtils.url = url;
        return this;
    }

    public BugPublicUtils setSoftwareMark(String softwareMark) {
        BugPublicUtils.softwaremark = softwareMark;
        return this;
    }

    public BugPublicUtils setBugSavedPath(String bugSavedPath) {
        BugPublicUtils.bugSavedPath = bugSavedPath;
        return this;
    }

    /**
     * 查看本地存放bug文件夹里面有没有保存的bug并上传
     *
     * @return
     */
    public void searchFileDataToUpload() {
        List<BugBean> arrays = new ArrayList<>();
        File file = new File(bugSavedPath);
        File[] files = file.listFiles();
        String mobileType = android.os.Build.MODEL;
        String mobileOS = android.os.Build.VERSION.RELEASE;
        String mobileImei = getImei(context);
        String softwareVersion = getAPPVersion(context);
        String netWorkType = getNetworkType(context);
        String regCode = regcode;
        String softwareMark = softwaremark;
        for (File file1 : files) {
            BugBean bean = new BugBean();
            bean.setMobileType(mobileType);
            bean.setMobileImei(mobileImei);
            bean.setMobileOS(mobileOS);
            bean.setSoftwareVersion(softwareVersion);
            bean.setNetWorkType(netWorkType);
            bean.setRegCode(regCode);
            bean.setSoftwareMark(softwareMark);
            bean.setAppearTime(getBugAppearTime(file1.getName()));
            try {
                bean.setBugInfo(encodeFile(file1));
            } catch (Exception e) {
                e.printStackTrace();
            }

            arrays.add(bean);
        }
        BugPublicUtils.arrays = arrays;
        //开启服务上传bug
        Intent intent = new Intent(context, BugUploadIntentService.class);
        context.startService(intent);
    }

    /**
     * 从bug文件的名称中提取bug出现的时间
     *
     * @param fileName
     * @return
     */
    private String getBugAppearTime(String fileName) {
        String appearTime = fileName.substring(0, fileName.lastIndexOf("-"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.format(formatter.parse(appearTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 检查本地文件是否有存储的bug信息
     *
     * @return
     */
    public static boolean checkBugs(String bugSavedPath) {
        File file = new File(bugSavedPath);
        if (!file.exists()) {
            file.mkdirs();
            return false;
        } else {
            File[] files = file.listFiles();
            if (files.length > 0) {
                return true;
            } else {
                return false;
            }
        }
    }


    /**
     * 获取网络类型
     *
     * @param context
     * @return
     */
    private String getNetworkType(Context context) {
        String netWorkType = "";
        int netWorkType_Integer = IntenetUtil.getNetworkState(context);
        switch (netWorkType_Integer) {
            case 0:
                netWorkType = "NoNetwork";
                break;
            case 1:
                netWorkType = "wifi";
                break;
            case 2:
                netWorkType = "2G";
                break;
            case 3:
                netWorkType = "3G";
                break;
            case 4:
                netWorkType = "4G";
                break;
            case 5:
                netWorkType = "";
                break;
            default:
                netWorkType = "";
                break;
        }
        return netWorkType;

    }

    /**
     * 获取软件版本号
     */
    private String getAPPVersion(Context context) {
        PackageManager pm = context.getPackageManager();//得到PackageManager对象
        String version_app = "";
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);//得到PackageInfo对象，封装了一些软件包的信息在里面
            version_app = pi.versionName;//获取清单文件中versionCode节点的值
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version_app;
    }

    /**
     * 获取手机IMEI
     *
     * @return
     */
    private String getImei(Context context) {

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        return imei;
    }

    /**
     * 将文件内容转换为字符串
     *
     * @return
     * @throws Exception
     */
    public String encodeFile(File file) throws Exception {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line = br.readLine()) != null) {
            sb.append(line + "<br />");//"<br />"网页中换行的字符
        }
        br.close();
        return sb.toString();
    }

    /**
     * 上传申请信息
     *
     * @param noteInfo
     */
    public static void uploadBugsToService(String noteInfo) {

        OkHttpUtils
                .post()
                .url(BugPublicUtils.url)
                .addParams("regisCode", BugPublicUtils.softwaremark)
                .addParams("buginfo", noteInfo)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
//                        Toast.makeText(c, "", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if (!TextUtils.isEmpty(response)) {
                            try {
                                JSONObject obj = new JSONObject(response);
                                String result = obj.getString("Result");

                                if ("Ok".equals(result)) {
                                    String message = obj.getString("message");
                                    if ("bug已上传".equals(message)) {

                                        //将本地的bug信息删除
                                        File file = new File(bugSavedPath);
                                        if (file.exists()) {
                                            deleteDir(bugSavedPath);
                                        }

                                    }
//                                    Toast.makeText(this, "您已成功提交申请", Toast.LENGTH_LONG).show();
                                } else {
//                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                });
    }


    //删除文件夹和文件夹里面的文件
    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWihtFile(dir);
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                file.delete(); // 删除所有文件
            } else if (file.isDirectory()) {
                deleteDirWihtFile(file); // 递规的方式删除文件夹
            }
        }
        dir.delete();// 删除目录本身
    }
}
