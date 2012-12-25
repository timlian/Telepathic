package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKStep;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.RouteOverlay;
import com.telepathic.finder.R;
import com.telepathic.finder.sdk.BusRoute;
import com.telepathic.finder.sdk.TrafficListener.BusLineListener;
import com.telepathic.finder.sdk.TrafficListener.BusLocationListener;
import com.telepathic.finder.sdk.TrafficListener.BusRouteListener;
import com.telepathic.finder.sdk.TrafficService;
import com.telepathic.finder.util.Utils;

public class BusLocationActivity extends MapActivity {
    private static final String TAG = "BusLocationActivity";

    private static final int CUSTOM_DIALOG_ID_START = 100;

    private static final int BUS_LINE_SEARCH_DLG  = CUSTOM_DIALOG_ID_START + 1;

    private static final int MAP_ZOOM_LEVEL = 14;

    private Button mBtnSearch;

    private MapView mMapView;
    private BMapManager mMapManager;

    private MyLocationOverlay mLocationOverlay;  //定位图层
    private LocationListener mLocationListener; //onResume时注册此listener，onPause时需要Remove
    private TrafficService mTrafficService;
    private MyBusLocationListener mBusLocationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_location_view);

        mBtnSearch = (Button) findViewById(R.id.search);

        mBusLocationListener = new MyBusLocationListener();
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
    }

    public void onSearchClicked(View v) {
        if (mBtnSearch.equals(v)) {
            EditText editSearchKey = (EditText) findViewById(R.id.search_key);
            String busNumber = editSearchKey.getText().toString();
            if (Utils.isValidBusLineNumber(busNumber)) {
                String city = getResources().getString(R.string.default_city);
                Utils.hideSoftKeyboard(this, editSearchKey);
                mBtnSearch.setEnabled(false);
                // showDialog(BUS_LINE_SEARCH_DLG);
                mTrafficService.searchBusLine(city, busNumber,
                        new BusLineListener() {
                    @Override
                    public void done(ArrayList<MKPoiInfo> busPois,
                            int error) {
                        showBusRoutesDlg(busPois);
                    }
                });
            } else {
                Toast.makeText(this, R.string.invalid_input_hint,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        mMapManager.getLocationManager().removeUpdates(mLocationListener);
        mLocationOverlay.disableMyLocation();
        mMapManager.stop();
        mTrafficService.unregisterBusLocationListener(mBusLocationListener);
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapManager.getLocationManager().requestLocationUpdates(mLocationListener);
        mLocationOverlay.enableMyLocation();
        mMapManager.start();
        mTrafficService.registerBusLocationListener(mBusLocationListener);
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
        builder.setTitle(R.string.select_bus_route).setSingleChoiceItems(busRoutes, 0, null)
        .setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mBtnSearch.setEnabled(true);
            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                final int selectedPosition = ((AlertDialog)dialog).getListView()
                        .getCheckedItemPosition();
                final MKPoiInfo busRouteInfo = busRoutePois.get(selectedPosition);
                searchBusRoute(busRouteInfo.city, busRouteInfo.uid);
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                mBtnSearch.setEnabled(true);
                dialog.dismiss();
            }
        }).create().show();
    }

    private void searchBusRoute(String city, String uid) {
        mTrafficService.searchBusRoute(city, uid, new BusRouteListener() {
            @Override
            public void done(BusRoute route, int error) {
                RouteOverlay routeOverlay = new RouteOverlay(BusLocationActivity.this, mMapView);
                routeOverlay.setData(route.getRoute());
                mMapView.getOverlays().clear();
                mMapView.getOverlays().add(routeOverlay);
                mMapView.getOverlays().add(mLocationOverlay);
                mMapView.invalidate();
                mMapView.getController().animateTo(route.getRoute().getStart());
                mBtnSearch.setEnabled(true);
                mTrafficService.retrieveBusLocation(route);
            }
        });
    }

    private void updateBusLocation(MKStep station) {
        Drawable marker = getResources().getDrawable(R.drawable.bus_location_marker);
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
        public void onLocationUpdated(final MKStep busLocation) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (busLocation != null) {
                        updateBusLocation(busLocation);
                    }
                }
            });
        }

        @Override
        public void onError(final String errorMessage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BusLocationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

}
