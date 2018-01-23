package com.bluetoothmicrecord.utils;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ${王sir} on 2017/10/10.
 * application
 */

public class ActivityManager {

    private List<Activity> activityList = new LinkedList<Activity>();
    private static ActivityManager instance;

    private ActivityManager() {
    }

    /**
     * 单例模式中获取唯一的AbActivityManager实例.
     * @return
     */
    public static ActivityManager getInstance() {
        if (null == instance) {
            instance = new ActivityManager();
        }
        return instance;
    }

    /**
     * 添加Activity到容器中.
     * @param activity
     */
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    /**
     * 遍历所有Activity并finish.
     */
    public void clearAllActivity() {
        for (Activity activity : activityList) {
            if(activity!=null){
                activity.finish();
            }
        }
        System.exit(0);
    }
}
