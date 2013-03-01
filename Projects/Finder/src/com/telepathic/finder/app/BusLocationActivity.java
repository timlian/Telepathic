package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

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

public class BusLocationActivity extends Activity {
    private static final String TAG = BusLocationActivity.class.getSimpleName();

    private static final int CUSTOM_DIALOG_ID_START = 100;

    private static final int BUS_LINE_SEARCH_DLG  = CUSTOM_DIALOG_ID_START + 1;

    private static final int DOWN_VOICE_SEARCH_DLG = CUSTOM_DIALOG_ID_START + 2;

    private static final int DOWN_VOICE_SEARCH_THROUGH_BROWSER_DLG = CUSTOM_DIALOG_ID_START + 3;

    private static final int EXIT_CONFIRM_DIALOG = CUSTOM_DIALOG_ID_START + 4;

    private static final int CUSTOM_INTENT_REQUEST_CODE_START = 0x1000;

    private static final int START_SPEECH_RECOGNIZE = CUSTOM_INTENT_REQUEST_CODE_START + 1;

    private static final int MAP_ZOOM_LEVEL = 14;

    private ImageButton mBtnSearch;

    private AutoCompleteTextView mTvSearchKey;

    private ImageView mIvSpeak;

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

    private IMessageHandler mSearchBusLineDoneHandler;
    private IMessageHandler mSearchBusRouteDoneHandler;
    private IMessageHandler mGetBusLocationUpdateHandler;
    private IMessageHandler mGetBusLocationDoneHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_location_view);

        Utils.copyAppDatabaseFiles(getPackageName());

        mBtnSearch = (ImageButton) findViewById(R.id.search);

        mTvSearchKey = (AutoCompleteTextView) findViewById(R.id.search_key);

        mIvSpeak = (ImageView)findViewById(R.id.iv_speak);

        // init map service
        FinderApplication app = (FinderApplication) getApplication();
        mMapManager = app.getMapManager();
        mMapManager.start();
        //        super.initMapActivity(mMapManager);

        // init traffic service
        mTrafficService = app.getTrafficService();
        mMessageDispatcher = app.getMessageDispatcher();
        initMessageHandlers();

        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapController = mMapView.getController();

        initMapView();

        mLocClient = new LocationClient(getApplicationContext());
        mLocClient.registerLocationListener(mLocationListener);
        //        mMapView.setBuiltInZoomControls(true);

        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//打开gps
        option.setCoorType("bd09ll");     //设置坐标类型
        mLocClient.setLocOption(option);
        mLocClient.start();
        mMapController.setZoom(14);
        mMapController.enableClick(true);

        mMapView.displayZoomControls(true);

        Drawable marker = getResources().getDrawable(R.drawable.bus_location_marker);
        marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
        /**
         * 创建自定义的ItemizedOverlay
         */
        mBusLocationOverlay = new CustomItemizedOverlay(marker, this);
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
                    removeDialog(BUS_LINE_SEARCH_DLG);
                    showBusRoutesDlg(mLineNumber, busPois);
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
                RouteOverlay routeOverlay = new RouteOverlay(BusLocationActivity.this, mMapView);
                routeOverlay.setData(route);
                mMapView.getOverlays().clear();
                mMapView.getOverlays().add(routeOverlay);
                mMapView.getOverlays().add(mLocationOverlay);
                mMapView.refresh();
                mMapView.getController().animateTo(route.getStart());
                mBtnSearch.setEnabled(true);
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
                if (msg.arg2 != 0) {
                    String errorMessage = (String) msg.obj;
                    showErrorMessage(errorMessage);
                }
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

    @Override
    protected void onDestroy() {
        clearMessageHandlers();
        super.onDestroy();
    }

    private void initMapView() {
        mMapView.setLongClickable(true);
        //mMapController.setMapClickEnable(true);
        //mMapView.setSatellite(false);
    }

    private void showErrorMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
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

    public void onSearchClicked(View v) {
        if (!mBtnSearch.equals(v)) {
            return ;
        }
        String lineNumber = mTvSearchKey.getText().toString();
        if (Utils.isValidBusLineNumber(lineNumber)) {
            String city = getResources().getString(R.string.default_city);
            Utils.hideSoftKeyboard(this, mTvSearchKey);
            mBtnSearch.setEnabled(false);
            showDialog(BUS_LINE_SEARCH_DLG);
            mLineNumber = lineNumber;
            mTrafficService.searchBusLine(city, mLineNumber);
            Utils.debug(TAG, "UI Thread: " + Thread.currentThread().toString());
        } else {
            Toast.makeText(this, R.string.invalid_input_hint,Toast.LENGTH_LONG).show();
        }
        //        Intent intent = new Intent(this, BusRouteHistoryActivity.class);
        //        startActivity(intent);
    }

    public void onSpeakClicked(View v){
        if (mIvSpeak.equals(v)){
            mTvSearchKey.setText("");
            startSpeechRecognize();
        }
    }

    private void startSpeechRecognize() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.speak_busline_number));
        try {
            startActivityForResult(intent, START_SPEECH_RECOGNIZE);
        } catch (ActivityNotFoundException ex) {
            showDialog(DOWN_VOICE_SEARCH_DLG);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_SPEECH_RECOGNIZE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            try {
                new recognizeResultTask().execute(matches);
            } catch (RejectedExecutionException ex) {
                ex.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        //        mMapManager.getLocationManager().removeUpdates(mLocationListener);
        //        mLocationOverlay.disableMyLocation();
        //        mMapManager.stop();
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mLocClient.requestLocation();
        //        mMapManager.getLocationManager().requestLocationUpdates(mLocationListener);
        //        mLocationOverlay.enableMyLocation();
        //        mMapManager.start();
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mMapView.onRestoreInstanceState(savedInstanceState);
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
                prgDlg.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mBtnSearch.setEnabled(true);
                    }
                });
                retDialog = prgDlg;
                break;
            case DOWN_VOICE_SEARCH_DLG:
                AlertDialog.Builder vsDlg = new AlertDialog.Builder(BusLocationActivity.this)
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
                retDialog = vsDlg.create();
                break;
            case DOWN_VOICE_SEARCH_THROUGH_BROWSER_DLG:
                AlertDialog.Builder vsBrowserDlg = new AlertDialog.Builder(BusLocationActivity.this)
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
                retDialog = vsBrowserDlg.create();
                break;
            case EXIT_CONFIRM_DIALOG:
                Builder exitDlgBuilder = new Builder(BusLocationActivity.this)
                .setTitle(R.string.confirm_exit_title)
                .setMessage(R.string.confirm_exit_message)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BusLocationActivity.this.finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
                retDialog = exitDlgBuilder.create();
                break;
            default:
                break;
        }
        return retDialog;
    }

    private void showBusRoutesDlg(String busLineNumber, final ArrayList<MKPoiInfo> busRoutePois) {
        final String[] busRoutes = new String[busRoutePois.size()];
        for (int idx = 0; idx < busRoutePois.size(); idx++) {
            int startPos = busRoutePois.get(idx).name.indexOf('(');
            int endPos   = busRoutePois.get(idx).name.indexOf(')');
            busRoutes[idx] = busRoutePois.get(idx).name.substring(startPos+1, endPos);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String titleText = String.format(getResources().getString(R.string.select_bus_route), busLineNumber);
        builder.setTitle(titleText).setSingleChoiceItems(busRoutes, 0, null)
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

    private class recognizeResultTask extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... params) {
            ArrayList<String> result = new ArrayList<String>();
            for (String recognize : params[0]) {
                String busLineNo = Utils.formatRecognizeData(recognize);
                if (busLineNo != null) {
                    result.add(busLineNo);
                }
            }
            return Utils.removeDuplicateWithOrder(result);
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            // mTvSearchKey.setAdapter(null);
            if (result.size() > 1) {
                mTvSearchKey.setText(result.get(0));
                result.remove(0);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        BusLocationActivity.this, android.R.layout.simple_dropdown_item_1line,
                        result);
                mTvSearchKey.setAdapter(adapter);
                mTvSearchKey.showDropDown();
            } else if (result.size() == 1) {
                mTvSearchKey.setText(result.get(0));
                onSearchClicked(mBtnSearch);
            } else {
                Toast.makeText(BusLocationActivity.this, R.string.no_matches_busline,
                        Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(result);
        }
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
