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
    
    private Button mBtnSearch = null;   // 搜索按钮

    private MapView mMapView = null;    // 地图View
    private MKSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用
    private String  mCityName = "成都";
    private BMapManager mMapManager;
    private MyLocationOverlay mLocationOverlay = null;  //定位图层
    private LocationListener mLocationListener = null; //onResume时注册此listener，onPause时需要Remove

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buslinesearch);

        mMapManager = new BMapManager(getApplication());
        mMapManager.init(DEV_KEY, new MyGeneralListener());
        mMapManager.start();
        mMapManager.start(); // 如果使用地图SDK，请初始化地图
        super.initMapActivity(mMapManager);

        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.setBuiltInZoomControls(true);
        // 设置在缩放动画过程中也显示overlay,默认为不绘制
        mMapView.setDrawOverlayWhenZooming(true);

        // 添加定位图层
        mLocationOverlay = new MyLocationOverlay(this, mMapView);
        mMapView.getOverlays().add(mLocationOverlay);

        // 初始化搜索模块，注册事件监听
        mSearch = new MKSearch();
        mSearch.init(mMapManager, new MKSearchListener() {

            @Override
            public void onGetPoiDetailSearchResult(int type, int error) {
            }

            public void onGetPoiResult(MKPoiResult res, int type, int error) {
                // 错误号可参考MKEvent中的定义
                if (error != 0 || res == null) {
                    Toast.makeText(BusLocationActivity.this, "抱歉，未找到结果",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // 找到公交路线poi node
                MKPoiInfo curPoi = null;
                int totalPoiNum = res.getNumPois();
                for (int idx = 0; idx < totalPoiNum; idx++) {
                    curPoi = res.getPoi(idx);
                    Log.d(TAG, curPoi.toString());
                    if (2 == curPoi.ePoiType) {
                        // poi类型，0：普通点，1：公交站，2：公交线路，3：地铁站，4：地铁线路
                        mSearch.busLineSearch(mCityName, curPoi.uid);
                        break;
                    }
                }

                // 没有找到公交信息
                if (curPoi == null) {
                    Toast.makeText(BusLocationActivity.this, "抱歉，未找到结果",
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
                    Toast.makeText(BusLocationActivity.this, "抱歉，未找到结果",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                MKRoute myRoute = result.getBusRoute();
                RouteOverlay routeOverlay = new RouteOverlay(BusLocationActivity.this, mMapView);
                // 此处仅展示一个方案作为示例
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

        // 注册定位事件
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

        // 设定搜索按钮的响应
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
        mLocationOverlay.disableCompass(); // 关闭指南针
        mMapManager.stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapManager.getLocationManager().requestLocationUpdates(mLocationListener);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableCompass(); // 打开指南针
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

 // 常用事件监听，用来处理通常的网络错误，授权验证错误等
    static class MyGeneralListener implements MKGeneralListener {
        @Override
        public void onGetNetworkState(int iError) {
            Log.d("MyGeneralListener", "onGetNetworkState error is "+ iError);
//            Toast.makeText(BMapApiDemoApp.mDemoApp.getApplicationContext(), "您的网络出错啦！",
//                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onGetPermissionState(int iError) {
            Log.d("MyGeneralListener", "onGetPermissionState error is "+ iError);
//            if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
//                // 授权Key错误：
//                Toast.makeText(BMapApiDemoApp.mDemoApp.getApplicationContext(),
//                        "请在BMapApiDemoApp.java文件输入正确的授权Key！",
//                        Toast.LENGTH_LONG).show();
//                BMapApiDemoApp.mDemoApp.m_bKeyRight = false;
//            }
        }
    }


}
