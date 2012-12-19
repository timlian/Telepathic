package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
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
import com.baidu.mapapi.MKStep;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.RouteOverlay;
import com.telepathic.finder.R;
import com.telepathic.finder.sdk.BusLocationListener;
import com.telepathic.finder.sdk.TrafficService;
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
    private TrafficService mTrafficService;
    private String mBusLine;
    private MKRoute mBusRoute;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buslinesearch);

        mTrafficService = TrafficService.getTrafficService();
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
                mBusRoute = result.getBusRoute();
                debugMKBusLine(result);
                //debugMkRoute(result.getBusRoute());
                MKRoute route = result.getBusRoute();
                int lastStationIdx = route.getNumSteps() - 1;
                final String lastStation = route.getStep(lastStationIdx).getContent();
                RouteOverlay routeOverlay = new RouteOverlay(BusLocationActivity.this, mMapView);
                // �˴���չʾһ��������Ϊʾ��
                routeOverlay.setData(result.getBusRoute());
                mMapView.getOverlays().clear();
                mMapView.getOverlays().add(routeOverlay);
                mMapView.invalidate();

                mMapView.getController().animateTo(result.getBusRoute().getStart());
                
                mTrafficService.getBusLocation(mBusLine, lastStation, lastStation, new MyBusLocationListener());
                
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
            mBusLine = editSearchKey.getText().toString();
            mSearch.poiSearchInCity(mCityName, mBusLine);
            Utils.hideSoftKeyboard(this, editSearchKey);
            mBtnSearch.setEnabled(false);
            showDialog(DIALOG_WAITING);
        }
    }
    
    private void debugMKBusLine(MKBusLineResult result) {
        Log.d("Test", "bus name: " + result.getBusName());
        MKStep firstStation = result.getStation(0);
        Log.d("Test", "startion 0: " + firstStation.getContent() + ", " + firstStation.getPoint());
    }
    
    /*
     * MKRoute.ROUTE_TYPE_UNKNOW   = 0
     * MKRoute.ROUTE_TYPE_DRIVING  = 1
     * MKRoute.ROUTE_TYPE_WALKING  = 2
     * MKRoute.ROUTE_TYPE_BUS_LINE = 3
     */
    private void debugMkRoute(MKRoute route) {
//        ArrayList<ArrayList<GeoPoint>> pointList = route.getArrayPoints();
//        ArrayList<GeoPoint> points = null; 
//        for(int i = 0; i < pointList.size(); i++) {
//            Log.d("Test", "#" + i);
//            points = pointList.get(i);
//            for(int j = 0; j < points.size(); j++) {
//                Log.d("Test", "##" + j + " " + points.get(j).toString());
//            }
//        }
//        Log.d("Test", "index: " + route.getIndex());
//        Log.d("Test", "steps: " + route.getNumSteps());
        
        MKStep step = null;
        for (int idx = 0; idx < route.getNumSteps(); idx++) {
            step = route.getStep(idx);
            Log.d("Test", "station: " + step.getContent() + "location: " + step.getPoint());
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
    
    private void addMarker(MKStep station) {
     // �������maker  
        Drawable marker = getResources().getDrawable(R.drawable.icon);  
        // Ϊmaker����λ�úͱ߽�  
        marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());  
        
        /** 
         * �����Զ����ItemizedOverlay 
         */  
        CustomItemizedOverlay overlay = new CustomItemizedOverlay(marker, this);  
        
        /** 
         * ��������ӵ�һ����ǣ�����ļ��磨���ȣ�87.493147 γ�ȣ�47.118440�� 
         */  
        // ����һ����γ�ȵ�   
        // ������ǣ��½������أ�   
        OverlayItem overlayItem = new OverlayItem(station.getPoint(), mBusLine, station.getContent());  
        // �������ӵ�ͼ���У�����Ӷ��OverlayItem��   
        overlay.addOverlay(overlayItem);  
        
        /** 
         * ����ͼ������Զ����ItemizedOverlay 
         */  
        List<Overlay> mapOverlays = mMapView.getOverlays();  
        mapOverlays.add(overlay);  
  
        /** 
         * ȡ�õ�ͼ�������������ڿ���MapView 
         */  
        //mMapView.getController().setCenter(markPoint);  
        //mMapView.getController().setZoom(9);
 
    }
    
    private class MyBusLocationListener implements BusLocationListener {

        @Override
        public void onSuccess(String lineNumber, final String distance) {
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                 // TODO Auto-generated method stub
                    Toast.makeText(BusLocationActivity.this, "success: " + distance, Toast.LENGTH_SHORT).show();
                    //mMapView.getController().animateTo(result.getBusRoute().getStart());
                    MKStep curStation = mBusRoute.getStep(mBusRoute.getNumSteps() - Integer.parseInt(distance));
                    addMarker(curStation);
                    
                }
            });
            
        }

        @Override
        public void onError(final String errorMessage) {
            runOnUiThread( new Runnable() {
                
                @Override
                public void run() {
                 // TODO Auto-generated method stub
                    Toast.makeText(BusLocationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    
                }
            });
            
        }
        
    }


}
