package com.bluetoothmicrecord.activity;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.bluetoothmicrecord.adapter.RecordAudiosActivityAdapter;
import com.bluetoothmicrecord.bean.RecordAudioFile;
import com.bluetoothmicrecord.interfaces.UpdateProgressInterface;
import com.bluetoothmicrecord.upload.UploadFileManager;
import com.bluetoothmicrecord.upload.uploadUtil.NetworkManager;
import com.bluetoothmicrecord.upload.uploadUtil.PreferenceUtil;
import com.bluetoothmicrecord.utils.DaoUtils;
import com.bluetoothmicrecord.utils.PubUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadService extends Service {
    private DaoUtils greenDaoUtil;
    private PreferenceUtil spUtil;
    private NetworkManager networkManager;
    private UploadFileManager uploadFileManager;
    private RecordAudiosActivityAdapter adapter;
    private final IBinder mBinder = new MyBinder();

    private uploadServiceInterface uploadServiceInterface;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }


    public class MyBinder extends Binder {
        public UploadService getUploadService() {
            return UploadService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        greenDaoUtil = new DaoUtils(this);
        networkManager = new NetworkManager(this);

        uploadFileManager = new UploadFileManager(this, new UpdateProgressInterface() {
            @Override
            public void sendMsg(RecordAudioFile bean) {
                greenDaoUtil.updateEntity(bean);
                if (spUtil.getBoolean("DELETEFILE")) {
                    if (bean.getUpLoadStatus().equals("3")) {
                        String path = bean.getFilePath();
                        File file = new File(path);
                        file.delete();
                        greenDaoUtil.deleteEntity(bean);
                    }
                }

            }
        });
        adapter = new RecordAudiosActivityAdapter(this, greenDaoUtil);
        spUtil = new PreferenceUtil(this, "APPSET");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (PubUtils.upload) {

                    if (spUtil.getBoolean("AUTOUPLOAD")) {//开启自动上传
                        List<RecordAudioFile> toUploadFiles = getIsToUploadFiles();
                        if (toUploadFiles.size() > 0) {//上传未上传和暂停的文件

                            if (checkUploadNetWork()) { //开始上传
                                for (RecordAudioFile toUploadFile : toUploadFiles) {
                                    if (toUploadFile.getUpLoadStatus().equals("0")) {//未上传，开始上传
                                        uploadFileManager.startUpLoad(toUploadFile);
                                        toUploadFile.setFileNameFromFtp(PubUtils.getRemoteFileName());
                                        toUploadFile.setUpLoadStatus("1");
                                        greenDaoUtil.updateEntity(toUploadFile);
                                    }
                                }

                            }

                        }
                    }
                }
            }
        }).start();
    }

    public void setUploadServiceCallBack(uploadServiceInterface uploadServiceInterface) {
        this.uploadServiceInterface = uploadServiceInterface;
    }

    public interface uploadServiceInterface {
        void warn(String warnStr);
    }
    /**
     * 检查上传网络环境
     * @return
     */
    private boolean checkUploadNetWork(){
        if (!NetworkManager.isConnected(this)) {
            return false;
        }
        String netTypeNow =  networkManager.getNetworkType();
        String setedNetType = spUtil.getString("NETWORKTYPE");
        if (TextUtils.isEmpty(setedNetType)) {
            setedNetType = "wifi";
        }
        if (netTypeNow.equals("wifi")) {
            return  true;

        }else if (netTypeNow.equals("mobile")) {
            if ("mobile".equals(setedNetType)) {
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    /**
     * 获取需要上传的文件
     *
     * @return
     */
    private List<RecordAudioFile> getIsToUploadFiles() {

        List<RecordAudioFile> arraysToUpload = new ArrayList<>();
        List<RecordAudioFile> arrays = greenDaoUtil.queryAllRecordAudioFiles();
        for (RecordAudioFile array : arrays) {
            if ("0".equals(array.getUpLoadStatus()) || "2".equals(array.getUpLoadStatus()) || "1".equals(array.getUpLoadStatus())) {
                arraysToUpload.add(array);
            }
        }
        return arraysToUpload;
    }

//

//
//    /**
//     * 测试数据
//     *
//     * @return
//     */
//    private List<RecordAudioFile> getDataForAdapter() {
//
//        List<RecordAudioFile> arrays = new ArrayList<>();
//
//        String fileDir = "/storage/emulated/0/qqmusic";
//        File savedFile = new File(fileDir);
//        File[] files = savedFile.listFiles();
//        if (files != null && files.length > 0) {
//            for (File file : files) {
//                RecordAudioFile recordAudioFile = new RecordAudioFile();
//                recordAudioFile.setRecordTime(PubUtils.getDateToString(System.currentTimeMillis()));
//                recordAudioFile.setFilePath(file.getPath());
//                recordAudioFile.setFileName(PubUtils.getAudioFileName(file.getPath()));
//                recordAudioFile.setFileDir(fileDir);
//                recordAudioFile.setUpLoadStatus("0");
//                recordAudioFile.setUploadProgress(0);
//                arrays.add(recordAudioFile);
//            }
//
//
//        }
//        return arrays;
//    }


}
