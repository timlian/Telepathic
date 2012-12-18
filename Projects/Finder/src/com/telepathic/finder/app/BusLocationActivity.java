package com.telepathic.finder.app;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
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
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.RouteOverlay;
import com.telepathic.finder.R;
import com.telepathic.finder.util.Utils;

public class BusLocationActivity extends MapActivity {
    private static final String TAG = "MainActivity";
    private static final String DEV_KEY = "A963422DFFFC8530BDDC5FF0063205F9E2D98461";
    private static final int DIALOG_WAITING = 1;
    
    private Button mBtnSearch = null;   // ������ť

    private MapView mMapView = null;    // ��ͼView
    private MKSearch mSearch = null;    // ����ģ�飬Ҳ��ȥ����ͼģ�����ʹ��
    private String  mCityName = "�ɶ�";
    private BMapManager mMapManager;
    private MyLocationOverlay mLocationOverlay = null;  //��λͼ��
    private LocationListener mLocationListener = null; //onResumeʱע���listener��onPauseʱ��ҪRemove

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

        // ��Ӷ�λͼ��
        mLocationOverlay = new MyLocationOverlay(this, mMapView);
        mMapView.getOverlays().add(mLocationOverlay);

        // ��ʼ������ģ�飬ע���¼�����
        mSearch = new MKSearch();
        mSearch.init(mMapManager, new MKSearchListener() {

            @Override
            public void onGetPoiDetailSearchResult(int type, int error) {
            }

            public void onGetPoiResult(MKPoiResult res, int type, int error) {
                // ����ſɲο�MKEvent�еĶ���
                if (error != 0 || res == null) {
                    Toast.makeText(BusLocationActivity.this, "��Ǹ��δ�ҵ����",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // �ҵ�����·��poi node
                MKPoiInfo curPoi = null;
                int totalPoiNum = res.getNumPois();
                for (int idx = 0; idx < totalPoiNum; idx++) {
                    curPoi = res.getPoi(idx);
                    Log.d(TAG, curPoi.toString());
                    if (2 == curPoi.ePoiType) {
                        // poi���ͣ�0����ͨ�㣬1������վ��2��������·��3������վ��4��������·
                        mSearch.busLineSearch(mCityName, curPoi.uid);
                        break;
                    }
                }

                // û���ҵ�������Ϣ
                if (curPoi == null) {
                    Toast.makeText(BusLocationActivity.this, "��Ǹ��δ�ҵ����",
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
                    Toast.makeText(BusLocationActivity.this, "��Ǹ��δ�ҵ����",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                MKRoute myRoute = result.getBusRoute();
                RouteOverlay routeOverlay = new RouteOverlay(BusLocationActivity.this, mMapView);
                // �˴���չʾһ��������Ϊʾ��
                routeOverlay.setData(result.getBusRoute());
                mMapView.getOverlays().clear();
                mMapView.getOverlays().add(routeOverlay);
                mMapView.invalidate();

                mMapView.getController().animateTo(result.getBusRoute().getStart());
                mBtnSearch.setEnabled(true);
                dismissDialog(DIALOG_WAITING);
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

        // ע�ᶨλ�¼�
        mLocationListener = new LocationListener(){
            @Override
            public void onLocationChanged(Location location) {
                if (location != null){
                    GeoPoint pt = new GeoPoint((int)(location.getLatitude()*1e6), (int)(location.getLongitude()*1e6));
                    mMapView.getController().animateTo(pt);
                    mMapManager.getLocationManager().removeUpdates(this);
                }
            }
        };

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
            mBtnSearch.setEnabled(false);
            showDialog(DIALOG_WAITING);
        }
    }

    @Override
    protected void onPause() {
        mMapManager.getLocationManager().removeUpdates(mLocationListener);
        mLocationOverlay.disableMyLocation();
        mLocationOverlay.disableCompass(); // �ر�ָ����
        mMapManager.stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapManager.getLocationManager().requestLocationUpdates(mLocationListener);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableCompass(); // ��ָ����
        mMapManager.start();
        super.onResume();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_WAITING) {
            ProgressDialog prgDlg = new ProgressDialog(this);
            prgDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            prgDlg.setMessage(getResources().getString(R.string.find_bus_route));
            prgDlg.setIndeterminate(true);
            prgDlg.setCancelable(false);
            return prgDlg;
        }
        return null;
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
