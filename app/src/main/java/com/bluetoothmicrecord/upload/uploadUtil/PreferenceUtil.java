/**
 * @Title: PreferenceManager.java
 * @Package com.hutu.localfile.manager
 * @Description: TODO
 * @author Long Li
 * @date 2015-5-13 下午5:45:40
 * @version V1.0
 */
package com.bluetoothmicrecord.upload.uploadUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * TODO<Preference基本操作>
 *
 * @author Long Li
 * @data: 2015-5-13 下午5:45:40
 * @version: V1.0
 */
public class PreferenceUtil {

    // private Context mContext;
    private SharedPreferences prefs;
    private static PreferenceUtil mPrefManager = null;

    public static PreferenceUtil getInstance(Context mContext, String filename) {
        if (mPrefManager == null) {
            mPrefManager = new PreferenceUtil(mContext, filename);
        }
        return mPrefManager;
    }

    public PreferenceUtil(Context mContext, String filename) {
        // this.mContext = mContext;
        prefs = mContext.getSharedPreferences(filename, mContext.MODE_PRIVATE);
    }

    /**
     * 存储String类型
     * @param key
     * @param vaule
     */
    public void put(String key, String vaule) {
        if ((null != key) && (key.length() != 0)) {
            Editor editor = prefs.edit();
            editor.putString(key, vaule);
            editor.commit();
        }
    }

    /**
     * 存储boolean类型
     * @param key
     * @param vaule
     */
    public void put(String key, boolean vaule) {
        if ((null != key) && (key.length() != 0)) {
            Editor editor = prefs.edit();
            editor.putBoolean(key, vaule);
            editor.commit();
        }
    }

    public String getString(String key) {
        String mdata = prefs.getString(key, "");
        return mdata;
    }

    public boolean getBoolean(String key) {
        boolean mdata = prefs.getBoolean(key, false);
        return mdata;
    }

    public void remove(String key) {
        if ((null != key) && (key.length() != 0)) {
            Editor editor = prefs.edit();
            editor.remove(key);
            editor.commit();
        }
    }
}
