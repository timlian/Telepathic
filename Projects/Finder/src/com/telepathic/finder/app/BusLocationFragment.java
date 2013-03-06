package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKStep;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.telepathic.finder.R;
import com.telepathic.finder.app.MessageDispatcher.IMessageHandler;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.ITrafficeMessage;
import com.telepathic.finder.util.Utils;

public class BusLocationFragment extends SherlockFragment {

    private static final String TAG = BusLocationFragment.class.getSimpleName();

    private static final int CUSTOM_DIALOG_ID_START = 100;

    private static final int BUS_LINE_SEARCH_DLG  = CUSTOM_DIALOG_ID_START + 1;

    private static final int DOWN_VOICE_SEARCH_DLG = CUSTOM_DIALOG_ID_START + 2;

    private static final int DOWN_VOICE_SEARCH_THROUGH_BROWSER_DLG = CUSTOM_DIALOG_ID_START + 3;

    private static final int EXIT_CONFIRM_DIALOG = CUSTOM_DIALOG_ID_START + 4;

    private static final int CUSTOM_INTENT_REQUEST_CODE_START = 0x1000;

    private static final int START_SPEECH_RECOGNIZE = CUSTOM_INTENT_REQUEST_CODE_START + 1;

    private static final int MAP_ZOOM_LEVEL = 14;

    private MainActivity mActivity;

    private MapView mMapView;

    private BMapManager mMapManager;

    private MapController mMapController = null;
    private LocationClient mLocClient;
    private LocationData mLocData = null;
    private MKMapViewListener mMapListener = null;
    private CustomItemizedOverlay mBusLocationOverlay;
    private MyLocationOverlay mLocationOverlay;  //定位图层
    private MyLocationListenner mLocationListener = new MyLocationListenner();
    private ITrafficService mTrafficService;
    private MessageDispatcher mMessageDispatcher;
    private MKRoute mBusRoute;
    private String mLineNumber;

    private Dialog mDialog;

    private IMessageHandler mSearchBusLineDoneHandler;
    private IMessageHandler mSearchBusRouteDoneHandler;
    private IMessageHandler mGetBusLocationUpdateHandler;
    private IMessageHandler mGetBusLocationDoneHandler;

    private boolean mIsFirstUpdate = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bus_location, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity)getSherlockActivity();

        Utils.copyAppDatabaseFiles(mActivity.getPackageName());

        // init map service
        FinderApplication app = (FinderApplication) mActivity.getApplication();
        mMapManager = app.getMapManager();
        mMapManager.start();

        // init traffic service
        mTrafficService = app.getTrafficService();
        mMessageDispatcher = app.getMessageDispatcher();
        initMessageHandlers();

        mMapView = (MapView)getView().findViewById(R.id.bmapView);
        mMapController = mMapView.getController();

        initMapView();

        mLocClient = new LocationClient(mActivity.getApplicationContext());
        mLocClient.registerLocationListener(mLocationListener);

        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//打开gps
        option.setCoorType("bd09ll");     //设置坐标类型
        mLocClient.setLocOption(option);
        mLocClient.start();
        mMapController.setZoom(MAP_ZOOM_LEVEL);
        mMapController.enableClick(true);

        mMapView.displayZoomControls(true);

        Drawable marker = getResources().getDrawable(R.drawable.bus_location_marker);
        marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
        /**
         * 创建自定义的ItemizedOverlay
         */
        mBusLocationOverlay = new CustomItemizedOverlay(marker, mActivity);
        mLocationOverlay = new MyLocationOverlay(mMapView);
        mLocData = new LocationData();
        mLocationOverlay.setData(mLocData);
        mMapView.getOverlays().add(mLocationOverlay);
        mLocationOverlay.enableCompass();
        mMapView.refresh();
    }

    private void initMessageHandlers() {
        mSearchBusLineDoneHandler = new IMessageHandler() {
            @Override
            public int what() {
                return ITrafficeMessage.SEARCH_BUS_LINE_DONE;
            }

            @Override
            public void handleMessage(Message msg) {
                ArrayList<MKPoiInfo> busPois = (ArrayList<MKPoiInfo>)msg.obj;
                if (busPois != null && busPois.size() > 0) {
                    removeDialog();
                    showBusRoutesDlg(mLineNumber, busPois);
                    mMapView.requestFocusFromTouch();
                }
            }
        };
        mSearchBusRouteDoneHandler = new IMessageHandler() {
            @Override
            public int what() {
                return ITrafficeMessage.SEARCH_BUS_ROUTE_DONE;
            }

            @Override
            public void handleMessage(Message msg) {
                MKRoute route = (MKRoute) msg.obj;
                RouteOverlay routeOverlay = new RouteOverlay(mActivity, mMapView);
                routeOverlay.setData(route);
                mMapView.getOverlays().clear();
                mMapView.getOverlays().add(routeOverlay);
                mMapView.getOverlays().add(mLocationOverlay);
                mMapView.refresh();
                mMapView.getController().animateTo(route.getStart());
                mBusRoute = route;
                mBusLocationOverlay.removeAllOverlay();
                mTrafficService.getBusLocation(mLineNumber, getRouteStationNames(route));
            }
        };
        mGetBusLocationUpdateHandler = new IMessageHandler() {

            @Override
            public int what() {
                return ITrafficeMessage.GET_BUS_LOCATION_UPDATED;
            }

            @Override
            public void handleMessage(Message msg) {
                Integer index = (Integer) msg.obj;
                if (mBusRoute != null) {
                    if (mIsFirstUpdate == true) {
                        RouteOverlay routeOverlay = new RouteOverlay(mActivity, mMapView);
                        routeOverlay.setData(mBusRoute);
                        mMapView.getOverlays().clear();
                        mMapView.getOverlays().add(routeOverlay);
                        mMapView.getOverlays().add(mLocationOverlay);
                        mMapView.invalidate();
                        mIsFirstUpdate = false;
                    }
                    MKStep station = mBusRoute.getStep(index);
                    updateBusLocation(station);
                }
            }
        };
        mGetBusLocationDoneHandler = new IMessageHandler() {
            @Override
            public int what() {
                return ITrafficeMessage.GET_BUS_LOCATION_DONE;
            }

            @Override
            public void handleMessage(Message msg) {
                mIsFirstUpdate = true;
                if (msg.arg2 != 0) {
                    String errorMessage = (String) msg.obj;
                    showErrorMessage(errorMessage);
                }
                int midStationIndex = mBusRoute.getNumSteps() / 2;
                MKStep midStation = mBusRoute.getStep(midStationIndex);
                if (midStation != null) {
                    mMapView.getController().animateTo(midStation.getPoint());
                }
                Drawable marker = getResources().getDrawable(R.drawable.bus_location_marker);
                marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
                /**
                 * 创建一个新的自定义的ItemizedOverlay，以便更新时使用
                 */
                mBusLocationOverlay = new CustomItemizedOverlay(marker, mActivity);
            }
        };

        mMessageDispatcher.add(mSearchBusLineDoneHandler);
        mMessageDispatcher.add(mSearchBusRouteDoneHandler);
        mMessageDispatcher.add(mGetBusLocationUpdateHandler);
        mMessageDispatcher.add(mGetBusLocationDoneHandler);
    }

    private void clearMessageHandlers() {
        mMessageDispatcher.remove(mSearchBusLineDoneHandler);
        mMessageDispatcher.remove(mSearchBusRouteDoneHandler);
        mMessageDispatcher.remove(mGetBusLocationUpdateHandler);
        mMessageDispatcher.remove(mGetBusLocationDoneHandler);
    }

    private void initMapView() {
        mMapView.setLongClickable(true);
    }

    private void showErrorMessage(String errorMessage) {
        Toast.makeText(mActivity, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private static ArrayList<String> getRouteStationNames(MKRoute route) {
        ArrayList<String> result = new ArrayList<String>();
        final int totalNum = route.getNumSteps();
        for(int idx = 0; idx < totalNum; idx++) {
            MKStep station = route.getStep(idx);
            String stationName = station.getContent();
            if (stationName != null && stationName.length() != 0) {
                if (stationName.charAt(stationName.length() - 1) != '\u7AD9') {
                    stationName += '\u7AD9';
                }
            }
            result.add(stationName);
        }
        return result;
    }

    @Override
    public void onStop() {
        clearMessageHandlers();
        super.onStop();
    }

    @Override
    public void onStart() {
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setTitle(R.string.bus_location);
        super.onStart();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mLocClient.requestLocation();
        mMapView.onResume();
        super.onResume();
    }

    private void showDialog(int id) {
        mDialog = null;
        switch (id) {
            case BUS_LINE_SEARCH_DLG:
                ProgressDialog prgDlg = new ProgressDialog(mActivity);
                prgDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                prgDlg.setMessage(getResources().getString(R.string.find_bus_route));
                prgDlg.setIndeterminate(true);
                prgDlg.setOnCancelListener(null);
                mDialog = prgDlg;
                break;
            case DOWN_VOICE_SEARCH_DLG:
                AlertDialog.Builder vsDlg = new AlertDialog.Builder(mActivity)
                .setTitle(R.string.no_voice_search_title)
                .setMessage(R.string.no_voice_search_msg)
                .setNegativeButton(R.string.no_voice_search_cancel,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNeutralButton(R.string.no_voice_search_browser, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Intent.ACTION_VIEW);
                        intent.setData(Uri
                                .parse("http://m.wandoujia.com/apps/com.google.android.voicesearch"));
                        startActivity(intent);
                    }
                })
                .setPositiveButton(R.string.no_voice_search_download,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent installIntent = new Intent(Intent.ACTION_VIEW);
                        installIntent.setData(Uri
                                .parse("market://details?id=com.google.android.voicesearch"));
                        try {
                            startActivity(installIntent);
                        } catch (ActivityNotFoundException ex) {
                            dialog.dismiss();
                            showDialog(DOWN_VOICE_SEARCH_THROUGH_BROWSER_DLG);
                        }
                    }
                });
                mDialog = vsDlg.create();
                break;
            case DOWN_VOICE_SEARCH_THROUGH_BROWSER_DLG:
                AlertDialog.Builder vsBrowserDlg = new AlertDialog.Builder(mActivity)
                .setTitle(R.string.no_market_title)
                .setMessage(R.string.no_market_msg)
                .setNegativeButton(R.string.no_market_cancel,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {
                    }
                })
                .setPositiveButton(R.string.no_market_download,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {
                        Intent intent = new Intent(
                                Intent.ACTION_VIEW);
                        intent.setData(Uri
                                .parse("http://m.wandoujia.com/apps/com.google.android.voicesearch"));
                        startActivity(intent);
                    }
                });
                mDialog = vsBrowserDlg.create();
                break;
            case EXIT_CONFIRM_DIALOG:
                Builder exitDlgBuilder = new Builder(mActivity)
                .setTitle(R.string.confirm_exit_title)
                .setMessage(R.string.confirm_exit_message)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mActivity.finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
                mDialog = exitDlgBuilder.create();
                break;
            default:
                break;
        }
        if (mDialog != null) {
            mDialog.show();
        }
    }

    private void removeDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    private void showBusRoutesDlg(String busLineNumber, final ArrayList<MKPoiInfo> busRoutePois) {
        final String[] busRoutes = new String[busRoutePois.size()];
        for (int idx = 0; idx < busRoutePois.size(); idx++) {
            int startPos = busRoutePois.get(idx).name.indexOf('(');
            int endPos   = busRoutePois.get(idx).name.indexOf(')');
            busRoutes[idx] = busRoutePois.get(idx).name.substring(startPos+1, endPos);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final String titleText = String.format(getResources().getString(R.string.select_bus_route), busLineNumber);
        builder.setTitle(titleText).setSingleChoiceItems(busRoutes, 0, null)
        .setOnCancelListener(null).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
                dialog.dismiss();
            }
        }).create().show();
    }

    private void searchBusRoute(String city, String uid) {
        mTrafficService.searchBusRoute(city, uid);
    }

    private void updateBusLocation(MKStep station) {
        /**
         * 创建并添加第一个标记：
         */
        OverlayItem overlayItem = new OverlayItem(station.getPoint(), "", station.getContent());
        mBusLocationOverlay.addOverlay(overlayItem);
        /**
         * 往地图上添加自定义的ItemizedOverlay
         */
        List<Overlay> mapOverlays = mMapView.getOverlays();
        for (Overlay ol : mapOverlays) {
            if (ol instanceof CustomItemizedOverlay) {
                mapOverlays.remove(ol);
            }
        }
        mapOverlays.add(mBusLocationOverlay);
        mMapView.getController().animateTo(station.getPoint());
        mMapView.refresh();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the options menu from XML
        inflater.inflate(R.menu.menu_bus_location, menu);

        // Get the SearchView and set the searchable configuration
        final SearchView searchView = (SearchView) menu.findItem(R.id.search_bus_location).getActionView();
        searchView.setQueryHint(getResources().getText(R.string.bus_number_hint));
        searchView.setInputType(InputType.TYPE_CLASS_TEXT);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                String lineNumber = query;
                if (Utils.isValidBusLineNumber(lineNumber)) {
                    String city = getResources().getString(R.string.default_city);
                    Utils.hideSoftKeyboard(mActivity, searchView);
                    showDialog(BUS_LINE_SEARCH_DLG);
                    mLineNumber = lineNumber;
                    mTrafficService.searchBusLine(city, mLineNumber);
                    Utils.debug(TAG, "UI Thread: " + Thread.currentThread().toString());
                } else {
                    Toast.makeText(mActivity, R.string.invalid_input_hint,Toast.LENGTH_LONG).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO Auto-generated method stub
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return ;

            mLocData.latitude = location.getLatitude();
            mLocData.longitude = location.getLongitude();
            mLocData.direction = 2.0f;
            mLocData.accuracy = location.getRadius();
            mLocData.direction = location.getDerect();
            Log.d("loctest",String.format("before: lat: %f lon: %f", location.getLatitude(),location.getLongitude()));
            // GeoPoint p = CoordinateConver.fromGcjToBaidu(new GeoPoint((int)(locData.latitude* 1e6), (int)(locData.longitude *  1e6)));
            //  Log.d("loctest",String.format("before: lat: %d lon: %d", p.getLatitudeE6(),p.getLongitudeE6()));
            mLocationOverlay.setData(mLocData);
            mMapView.refresh();
            mMapController.animateTo(new GeoPoint((int)(mLocData.latitude* 1e6), (int)(mLocData.longitude *  1e6)), null);
        }

        @Override
        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null){
                return ;
            }
        }

    }

}
