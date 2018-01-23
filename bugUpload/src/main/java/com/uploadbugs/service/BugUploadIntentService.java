package com.uploadbugs.service;

import android.app.IntentService;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uploadbugs.utils.BugPublicUtils;

import static com.uploadbugs.utils.BugPublicUtils.arrays;

public class BugUploadIntentService extends IntentService {
    private BugPublicUtils publicUtils;
    private Gson mGson;

    public BugUploadIntentService() {
        super("");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        publicUtils = new BugPublicUtils(this);
        mGson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String str = GetDeleteShort(mGson.toJson(arrays));
        //TOdo 将bugInfo信息上传到平台
        BugPublicUtils.uploadBugsToService(str);

    }
    /**
     * 去掉String字符串中的\u0027
     * @param str
     * @return
     */
    public  String GetDeleteShort(String str){
        String s = str.replace("\\u0027", "");
        return s;
    }

}
