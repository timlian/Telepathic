package com.telepathic.finder.app;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.ActivityManager;
import android.app.Application;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.traffic.TrafficManager;
import com.telepathic.finder.util.Logger;
import com.telepathic.finder.util.Utils;

public class FinderApplication extends Application {
	private static final String TAG = FinderApplication.class.getName();
	
	private static final String MAIN_PROCESS_NAME = "com.telepathic.finder";
	
    static FinderApplication mApp;

    private BMapManager mBMapManager = null;
    private TrafficManager mTrafficManager = null;
    private MessageDispatcher mMessageDispatcher = new MessageDispatcher();

    // Authentication Key
    private String mStrKey = "A963422DFFFC8530BDDC5FF0063205F9E2D98461";
    boolean m_bKeyRight = true;

    // 常用事件监听，用来处理通常的网络错误，授权验证错误等
    private static class MyGeneralListener implements MKGeneralListener {
        @Override
        public void onGetNetworkState(int iError) {
            Toast.makeText(FinderApplication.mApp.getApplicationContext(), "您的网络出错啦！",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onGetPermissionState(int iError) {
            if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
                Toast.makeText(FinderApplication.mApp.getApplicationContext(),
                        "请在FinderApplication.java文件输入正确的授权Key！",
                        Toast.LENGTH_LONG).show();
                FinderApplication.mApp.m_bKeyRight = false;
            }
        }
    }

    @Override
    public void onCreate() {
    	if (MAIN_PROCESS_NAME.equals(getCurrentProcessName())) {
    		//setUncatchedExceptionHandler();
    		mApp = this;
	        mBMapManager = new BMapManager(this);
	        boolean isSuccess = mBMapManager.init(this.mStrKey, new MyGeneralListener());
	        Handler msgHandler = mMessageDispatcher.getMessageHandler(getMainLooper());
	        mTrafficManager = TrafficManager.getTrafficManager(mBMapManager, getApplicationContext(), msgHandler);
	        // 初始化地图sdk成功，设置定位监听时间
	        if (isSuccess) {
	            // mBMapManager.getLocationManager().setNotifyInternal(10, 5);
	        } else {
	            // 地图sdk初始化失败，不能使用sdk
	        }
    	}
       
        super.onCreate();
    }

    //建议在您app的退出之前调用mapadpi的destroy()函数，避免重复初始化带来的时间消耗
    @Override
    public void onTerminate() {
        if (mBMapManager != null) {
            mBMapManager.destroy();
            mBMapManager = null;
        }
        super.onTerminate();
    }

    public BMapManager getMapManager() {
        return mBMapManager;
    }

    public ITrafficService getTrafficService() {
        return mTrafficManager.getTrafficService();
    }

    public MessageDispatcher getMessageDispatcher() {
        return mMessageDispatcher;
    }

    private void setUncatchedExceptionHandler() {
    	Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				Logger.logTrace(thread, ex);
			}
		});
    }
    
    private String getCurrentProcessName() {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
    
}
