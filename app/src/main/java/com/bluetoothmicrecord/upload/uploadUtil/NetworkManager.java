/**   
 * @Title: NetworkManager.java 
 * @Package com.example.androidnetwork 
 * @Description: TODO
 * @author Long Li  
 * @date 2015-5-20 上午10:30:46 
 * @version V1.0   
 */
package com.bluetoothmicrecord.upload.uploadUtil;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

/**
 * TODO<请描述这个类是干什么的>
 * 
 * @author Long Li
 * @data: 2015-5-20 上午10:30:46
 * @version: V1.0
 */



public class NetworkManager {
	private Context mContext = null;

	public NetworkManager(Context mContext) {
		this.mContext = mContext;
	}


	/**
	 * 1代表wifi，2代表移动网络
	 * @return
	 */
	public  String getNetworkType() {
		String netowkrType = "";
		ConnectivityManager connectivity = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo mobNetInfo = connectivity
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		NetworkInfo wifiNetInfo = connectivity
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		

		
		if ((mobNetInfo != null) && (wifiNetInfo != null)) {			
			
			if (wifiNetInfo.getState() == State.CONNECTED) { //1
				netowkrType = "wifi";
			} else {
				if (mobNetInfo.getState() == State.CONNECTED) { // 2
					netowkrType = "mobile";
				} else {
					netowkrType = "unConnect"; //网络已经断开
				}
			}
		} 
		
		return netowkrType;
		
	}
	
//	public boolean CheckNetworkPermisson() {
//		SharedPreferences prefs  = PreferenceManager.getDefaultSharedPreferences(mContext);
//		boolean setWifi = prefs.getBoolean("wifi_network", true);
//		boolean setGPS = prefs.getBoolean("gsm_network", false);
//		int mNetType = getNetworkType();
//		if ( mNetType != 0) { //网络连接正常
//			//判断网络类型，根据配置来判断是否需要上传
//			if (mNetType != 1) { //非wifi网络都默认为GPS
//				if (!setGPS) {
//					Log.d("debug", "用户设置GPS网络下不能上传");
//					return false;
//				}
//			} else { //wifi网络
//				if (!setWifi) {
//					Log.d("debug", "用户设置wifi情况下不能上传");
//					return false;
//				}
//			}
//		} else {
//
//			return false;
//		}
//		return true;
//	}
	// 判断网络是否正常

	public static boolean isConnected(Context context) {
		boolean isOk = true;
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mobNetInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			NetworkInfo wifiNetInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (wifiNetInfo != null && !wifiNetInfo.isConnectedOrConnecting()) {
				if (mobNetInfo != null && !mobNetInfo.isConnectedOrConnecting()) {
					NetworkInfo info = connectivityManager
							.getActiveNetworkInfo();
					if (info == null) {
						isOk = false;
					}
				}
			}
			mobNetInfo = null;
			wifiNetInfo = null;
			connectivityManager = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isOk;
	}



}
