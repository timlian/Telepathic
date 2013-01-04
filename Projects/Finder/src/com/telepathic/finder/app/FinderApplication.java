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

    // �����¼���������������ͨ�������������Ȩ��֤�����
    private static class MyGeneralListener implements MKGeneralListener {
        @Override
        public void onGetNetworkState(int iError) {
            Toast.makeText(FinderApplication.mApp.getApplicationContext(), "���������������",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onGetPermissionState(int iError) {
            if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
                Toast.makeText(FinderApplication.mApp.getApplicationContext(),
                        "����FinderApplication.java�ļ�������ȷ����ȨKey��",
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
        // ��ʼ����ͼsdk�ɹ������ö�λ����ʱ��
        if (isSuccess) {
            mBMapManager.getLocationManager().setNotifyInternal(10, 5);
        } else {
            // ��ͼsdk��ʼ��ʧ�ܣ�����ʹ��sdk
        }
        super.onCreate();
    }

    //��������app���˳�֮ǰ����mapadpi��destroy()�����������ظ���ʼ��������ʱ������
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
