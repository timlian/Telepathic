package com.telepathic.finder.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKRoute;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.RouteOverlay;
import com.telepathic.finder.R;
import com.telepathic.finder.util.Utils;

public class MainActivity extends MapActivity {
    private static final String TAG = "MainActivity"; 
    private static final String DEV_KEY = "A963422DFFFC8530BDDC5FF0063205F9E2D98461";
    
    private Button mBtnSearch = null;   // ������ť
    
    private MapView mMapView = null;    // ��ͼView
    private MKSearch mSearch = null;    // ����ģ�飬Ҳ��ȥ����ͼģ�����ʹ��
    private String  mCityName = "�ɶ�";
    private BMapManager mMapManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buslinesearch);

        mMapManager = new BMapManager(getApplication());
        mMapManager.init(DEV_KEY, new MyGeneralListener());
        mMapManager.start();
        mMapManager.start(); // ���ʹ�õ�ͼSDK�����ʼ����ͼ
        super.initMapActivity(mMapManager);

        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.setBuiltInZoomControls(true);
        // ���������Ŷ���������Ҳ��ʾoverlay,Ĭ��Ϊ������
        mMapView.setDrawOverlayWhenZooming(true);

        // ��ʼ������ģ�飬ע���¼�����
        mSearch = new MKSearch();
        mSearch.init(mMapManager, new MKSearchListener() {

            @Override
            public void onGetPoiDetailSearchResult(int type, int error) {
            }

            public void onGetPoiResult(MKPoiResult res, int type, int error) {
                // ����ſɲο�MKEvent�еĶ���
                if (error != 0 || res == null) {
                    Toast.makeText(MainActivity.this, "��Ǹ��δ�ҵ����",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // �ҵ�����·��poi node
                MKPoiInfo curPoi = null;
                int totalPoiNum = res.getNumPois();
                for (int idx = 0; idx < totalPoiNum; idx++) {
                    curPoi = res.getPoi(idx);
                    if (2 == curPoi.ePoiType) {
                        // poi���ͣ�0����ͨ�㣬1������վ��2��������·��3������վ��4��������·
                        mSearch.busLineSearch(mCityName, curPoi.uid);
                        break;
                    }
                }

                // û���ҵ�������Ϣ
                if (curPoi == null) {
                    Toast.makeText(MainActivity.this, "��Ǹ��δ�ҵ����",
                            Toast.LENGTH_LONG).show();
                    return;
                }

            }

            public void onGetDrivingRouteResult(MKDrivingRouteResult res,
                    int error) {
            }

            public void onGetTransitRouteResult(MKTransitRouteResult res,
                    int error) {
            }

            public void onGetWalkingRouteResult(MKWalkingRouteResult res,
                    int error) {
            }

            public void onGetAddrResult(MKAddrInfo res, int error) {
            }

            public void onGetBusDetailResult(MKBusLineResult result, int iError) {
                if (iError != 0 || result == null) {
                    Toast.makeText(MainActivity.this, "��Ǹ��δ�ҵ����",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                RouteOverlay routeOverlay = new RouteOverlay(MainActivity.this, mMapView);
                // �˴���չʾһ��������Ϊʾ��
                routeOverlay.setData(result.getBusRoute());
                mMapView.getOverlays().clear();
                mMapView.getOverlays().add(routeOverlay);
                mMapView.invalidate();

                mMapView.getController().animateTo(result.getBusRoute().getStart());
            }

            @Override
            public void onGetSuggestionResult(MKSuggestionResult res, int arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onGetRGCShareUrlResult(String arg0, int arg1) {
                // TODO Auto-generated method stub

            }

        });

        // �趨������ť����Ӧ
        mBtnSearch = (Button) findViewById(R.id.search);

        OnClickListener clickListener = new OnClickListener() {
            public void onClick(View v) {
                SearchButtonProcess(v);
            }
        };

        mBtnSearch.setOnClickListener(clickListener);
    }

    void SearchButtonProcess(View v) {
        if (mBtnSearch.equals(v)) {
            EditText editSearchKey = (EditText) findViewById(R.id.searchkey);
            String searchKey = editSearchKey.getText().toString();
            mSearch.poiSearchInCity(mCityName, editSearchKey.getText().toString());
            Utils.hideSoftKeyboard(this, editSearchKey);
        }
    }

    @Override
    protected void onPause() {
        mMapManager.stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapManager.start();
        super.onResume();
    }

    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }
    
 // �����¼���������������ͨ�������������Ȩ��֤�����
    static class MyGeneralListener implements MKGeneralListener {
        @Override
        public void onGetNetworkState(int iError) {
            Log.d("MyGeneralListener", "onGetNetworkState error is "+ iError);
//            Toast.makeText(BMapApiDemoApp.mDemoApp.getApplicationContext(), "���������������",
//                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onGetPermissionState(int iError) {
            Log.d("MyGeneralListener", "onGetPermissionState error is "+ iError);
//            if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
//                // ��ȨKey����
//                Toast.makeText(BMapApiDemoApp.mDemoApp.getApplicationContext(), 
//                        "����BMapApiDemoApp.java�ļ�������ȷ����ȨKey��",
//                        Toast.LENGTH_LONG).show();
//                BMapApiDemoApp.mDemoApp.m_bKeyRight = false;
//            }
        }
    }


}
