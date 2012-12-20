package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
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
    private static final String TAG = "BusLocationActivity";
    
    private static final int CUSTOM_DIALOG_ID_START = 100;
    
    private static final int BUS_LINE_SEARCH_DLG  = CUSTOM_DIALOG_ID_START + 1;
    
    private static final int MAP_ZOOM_LEVEL = 14;
    
    private Button mBtnSearch;  

    private MapView mMapView;    
    private MKSearch mSearch;    
    
    private BMapManager mMapManager;
    
    private MyLocationOverlay mLocationOverlay;  //定位图层
    private LocationListener mLocationListener; //onResume时注册此listener，onPause时需要Remove
    private TrafficService mTrafficService;
    private MKRoute mBusRoute;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_location_view);
        
        // init map service 
        FinderApplication app = (FinderApplication) getApplication();
        mMapManager = app.getMapManager();
        mMapManager.start();
        super.initMapActivity(mMapManager);
        
        // init traffic service
        mTrafficService = TrafficService.getTrafficService(mMapManager);

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
                    Toast.makeText(BusLocationActivity.this, "Sorry, there is no corresponding bus line.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                debugMKPoiResult(res, false);
                
                ArrayList<MKPoiInfo> allPois = res.getAllPoi();
                if (allPois != null && allPois.size() > 0) {
                    ArrayList<MKPoiInfo> busRoutePois = new ArrayList<MKPoiInfo>();
                    for(final MKPoiInfo poiInfo : allPois) {
                        // poi类型，0：普通点，1：公交站，2：公交线路，3：地铁站，4：地铁线路
                        if (poiInfo.ePoiType == 2) {
                            busRoutePois.add(poiInfo);
                        }
                    }
                    if (busRoutePois.size() != 0) {
                        dismissDialog(BUS_LINE_SEARCH_DLG);
                        showBusRoutesDlg(busRoutePois);
                    }
                    
                } else {
                    Toast.makeText(BusLocationActivity.this, "Sorry, there is no corresponding bus line.",
                            Toast.LENGTH_LONG).show();
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
                Log.d("Test", "find a new bus route: " + result.getBusName());
                debugMKBusLine(result, false);
                mBusRoute = result.getBusRoute();
                MKRoute route = result.getBusRoute();
                int lastStationIdx = route.getNumSteps() - 1;
                final String lastStation = route.getStep(lastStationIdx).getContent();
                final String lineNumber = Utils.getBusLineNumber(result.getBusName()).get(0);
                mTrafficService.getBusLocation(lineNumber, lastStation, lastStation, new MyBusLocationListener());
                
                RouteOverlay routeOverlay = new RouteOverlay(BusLocationActivity.this, mMapView);
                // 此处仅展示一个方案作为示例
                routeOverlay.setData(result.getBusRoute());
                mMapView.getOverlays().clear();
                mMapView.getOverlays().add(routeOverlay);
                mMapView.invalidate();
                mMapView.getController().animateTo(result.getBusRoute().getStart());
                mBtnSearch.setEnabled(true);
            }

            @Override
            public void onGetSuggestionResult(MKSuggestionResult res, int arg1) {

            }

            @Override
            public void onGetRGCShareUrlResult(String arg0, int arg1) {

            }

        });

        // 注册定位事件
        mLocationListener = new LocationListener(){
            @Override
            public void onLocationChanged(Location location) {
                if (location != null){
                    GeoPoint pt = new GeoPoint((int)(location.getLatitude()*1e6), (int)(location.getLongitude()*1e6));
                    mMapView.getController().animateTo(pt);
                    mMapView.getController().setZoom(MAP_ZOOM_LEVEL);
                    mMapManager.getLocationManager().removeUpdates(this);
                }
            }
        };

        // 设定搜索按钮的响应
        mBtnSearch = (Button) findViewById(R.id.search);
    }

    public void onSearchClicked(View v) {
        if (mBtnSearch.equals(v)) {
            EditText editSearchKey = (EditText) findViewById(R.id.search_key);
            String busNumber = editSearchKey.getText().toString();
            String city = getResources().getString(R.string.default_city);
            mSearch.poiSearchInCity(city, busNumber);
            Utils.hideSoftKeyboard(this, editSearchKey);
            mBtnSearch.setEnabled(false);
            showDialog(BUS_LINE_SEARCH_DLG);
        }
    }
    
    private void debugMKBusLine(MKBusLineResult result, boolean flag) {
        if (flag) {
            Log.d("Test", "bus name: " + result.getBusName());
            debugMkRoute(result.getBusRoute(), true);
        }
    }
    
    /*
     * MKRoute.ROUTE_TYPE_UNKNOW   = 0
     * MKRoute.ROUTE_TYPE_DRIVING  = 1
     * MKRoute.ROUTE_TYPE_WALKING  = 2
     * MKRoute.ROUTE_TYPE_BUS_LINE = 3
     */
    private void debugMkRoute(MKRoute route, boolean flag) {
        if (flag) {
//            ArrayList<ArrayList<GeoPoint>> pointList = route.getArrayPoints();
//            ArrayList<GeoPoint> points = null; 
//            for(int i = 0; i < pointList.size(); i++) {
//                Log.d("Test", "#" + i);
//                points = pointList.get(i);
//                for(int j = 0; j < points.size(); j++) {
//                    Log.d("Test", "##" + j + " " + points.get(j).toString());
//                }
//            }
            Log.d("Test", "index: " + route.getIndex());
            Log.d("Test", "steps: " + route.getNumSteps());
            MKStep step = null;
            for (int idx = 0; idx < route.getNumSteps(); idx++) {
                step = route.getStep(idx);
                Log.d("Test", "station: " + step.getContent() + "location: " + step.getPoint());
            }
        }
    }
    
    private void debugMKPoiResult(MKPoiResult result, boolean flag) {
        if (flag) {
            ArrayList<MKPoiInfo> infos = result.getAllPoi();
            Log.d("Test", "all poi number: " + infos.size());
            //MKCityListInfo firstCity = result.getCityListInfo(0);
           // Log.d("Test", "city: " + firstCity.city + ", num: " + firstCity.num); 
            //Log.d("Test", "city list number: " + result.getCityListNum()); // 0
            
            Log.d("Test", "current number pois: " + result.getCurrentNumPois()); //12
            ArrayList<MKPoiResult> results = result.getMultiPoiResult();
            Log.d("Test", "num page: " + result.getNumPages());
            Log.d("Test", "num pois: " + result.getNumPois());
            Log.d("Test", "page index: " + result.getPageIndex());
            for(int i = 0; i < result.getCurrentNumPois(); i++) {
                printMKPoiInfo(result.getPoi(i));
            }
        }
    }
    
    private void printMKPoiInfo(MKPoiInfo info){
        Log.d("Test", "############ start ##############");
        Log.d("Test", "Address: " + info.address);
        Log.d("Test", "City: " + info.city);
        Log.d("Test", "Type: " + info.ePoiType);
        Log.d("Test", "hasCaterDetails: " + info.hasCaterDetails);
        Log.d("Test", "name: " + info.name);
        Log.d("Test", "phoneNum: " + info.phoneNum);
        Log.d("Test", "postCode: " + info.postCode);
        Log.d("Test", "GeoPoint: " + info.pt);
        Log.d("Test", "uid: " + info.uid);
        Log.d("Test", "############ end ##############");
    }
    
    private void debugThread(String msg, boolean flag) {
        if (flag) {
            Thread curThread = Thread.currentThread();
            Log.d(TAG, msg + " Thread id: " + curThread.getId());
            Log.d(TAG, msg + " Thread info : " + curThread.toString());
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
    protected Dialog onCreateDialog(int id, Bundle args) {
        Dialog retDialog = null;
        switch (id) {
        case BUS_LINE_SEARCH_DLG:
            ProgressDialog prgDlg = new ProgressDialog(this);
            prgDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            prgDlg.setMessage(getResources().getString(R.string.find_bus_route));
            prgDlg.setIndeterminate(true);
            prgDlg.setCancelable(false);
            retDialog = prgDlg;
            break;
            
        default:
            break;
        }
        return retDialog;
    }
    
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    private void showBusRoutesDlg(final ArrayList<MKPoiInfo> busRoutePois) {
        final String[] busRoutes = new String[busRoutePois.size()];
        for (int idx = 0; idx < busRoutePois.size(); idx++) {
            busRoutes[idx] = busRoutePois.get(idx).name;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_bus_route)
                .setSingleChoiceItems(busRoutes, 0, null)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                dialog.dismiss();
                                final int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                                final MKPoiInfo busRouteInfo = busRoutePois.get(selectedPosition);
                                mSearch.busLineSearch(busRouteInfo.city, busRouteInfo.uid);
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                dialog.dismiss();
                            }
                        }).create().show();
    }

    private void addMarker(MKStep station) {
     // 创建标记maker  
        Drawable marker = getResources().getDrawable(R.drawable.bus_location_marker);  
        // 为maker定义位置和边界  
        marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());  
        
        /** 
         * 创建自定义的ItemizedOverlay 
         */  
        CustomItemizedOverlay overlay = new CustomItemizedOverlay(marker, this);  
        
        /** 
         * 创建并添加第一个标记：
         */  
        OverlayItem overlayItem = new OverlayItem(station.getPoint(), "", station.getContent());  
        overlay.addOverlay(overlayItem);  
        /** 
         * 往地图上添加自定义的ItemizedOverlay 
         */  
        List<Overlay> mapOverlays = mMapView.getOverlays();  
        mapOverlays.add(overlay);  
        mMapView.getController().animateTo(station.getPoint());
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
                    Toast.makeText(BusLocationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
