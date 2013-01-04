package com.telepathic.finder.app;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;

import android.app.Application;
import android.widget.Toast;

public class FinderApplication extends Application {
    static FinderApplication mApp;

    private BMapManager mBMapManager = null;

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
        mApp = this;
        mBMapManager = new BMapManager(this);
        boolean isSuccess = mBMapManager.init(this.mStrKey, new MyGeneralListener());
        // 初始化地图sdk成功，设置定位监听时间
        if (isSuccess) {
            mBMapManager.getLocationManager().setNotifyInternal(10, 5);
        } else {
            // 地图sdk初始化失败，不能使用sdk
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

}
