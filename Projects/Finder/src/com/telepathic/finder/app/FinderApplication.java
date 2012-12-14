package com.telepathic.finder.app;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

public class FinderApplication extends Application {
    static FinderApplication mDemoApp;
    
    //�ٶ�MapAPI�Ĺ�����
    BMapManager mBMapMan = null;
    
    // ��ȨKey
    // �����ַ��http://developer.baidu.com/map/android-mobile-apply-key.htm
    String mStrKey = "A963422DFFFC8530BDDC5FF0063205F9E2D98461";
    boolean m_bKeyRight = true; // ��ȨKey��ȷ����֤ͨ��
    
    // �����¼���������������ͨ�������������Ȩ��֤�����
    static class MyGeneralListener implements MKGeneralListener {
        @Override
        public void onGetNetworkState(int iError) {
            Log.d("MyGeneralListener", "onGetNetworkState error is "+ iError);
            Toast.makeText(FinderApplication.mDemoApp.getApplicationContext(), "���������������",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onGetPermissionState(int iError) {
            Log.d("MyGeneralListener", "onGetPermissionState error is "+ iError);
            if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
                // ��ȨKey����
                Toast.makeText(FinderApplication.mDemoApp.getApplicationContext(), 
                        "����BMapApiDemoApp.java�ļ�������ȷ����ȨKey��",
                        Toast.LENGTH_LONG).show();
                FinderApplication.mDemoApp.m_bKeyRight = false;
            }
        }
    }

    @Override
    public void onCreate() {
        Log.v("BMapApiDemoApp", "onCreate");
        mDemoApp = this;
        mBMapMan = new BMapManager(this);
        boolean isSuccess = mBMapMan.init(this.mStrKey, new MyGeneralListener());
        // ��ʼ����ͼsdk�ɹ������ö�λ����ʱ��
        if (isSuccess) {
            mBMapMan.getLocationManager().setNotifyInternal(10, 5);
        }
        else {
            // ��ͼsdk��ʼ��ʧ�ܣ�����ʹ��sdk
        }
        super.onCreate();
    }

    @Override
    //��������app���˳�֮ǰ����mapadpi��destroy()�����������ظ���ʼ��������ʱ������
    public void onTerminate() {
        // TODO Auto-generated method stub
        if (mBMapMan != null) {
            mBMapMan.destroy();
            mBMapMan = null;
        }
        super.onTerminate();
    }

}
