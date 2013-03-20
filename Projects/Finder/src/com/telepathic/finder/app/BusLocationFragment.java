
package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.actionbarsherlock.widget.SearchView.OnSuggestionListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKStep;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.telepathic.finder.R;
import com.telepathic.finder.app.MessageDispatcher.IMessageHandler;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.ITrafficeMessage;
import com.telepathic.finder.sdk.traffic.entity.baidu.BDBusLine;
import com.telepathic.finder.sdk.traffic.entity.baidu.BDBusRoute;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.util.Utils;

public class BusLocationFragment extends SherlockFragment {
    private static final String TAG = BusLocationFragment.class.getSimpleName();

    private static final int CUSTOM_DIALOG_ID_START = 100;

    private static final int BUS_LINE_SEARCH_DLG = CUSTOM_DIALOG_ID_START + 1;

    private static final int CLEAN_CACHE_CONFIRM_DLG = CUSTOM_DIALOG_ID_START + 2;

    private static final int MAP_ZOOM_LEVEL = 14;

    private MainActivity mActivity;

    private SearchView mSearchView;

    private MapView mMapView;

    private LinearLayout mUpdateLocation;

    private ImageView mUpdateIcon;

    private ProgressBar mProgress;

    private BMapManager mMapManager;

    private MapController mMapController = null;

    private LocationClient mLocClient;

    private LocationData mLocData = null;

    private CustomItemizedOverlay mBusLocationOverlay;

    private MyLocationOverlay mLocationOverlay; // 定位图层

    private MyLocationListenner mLocationListener;

    private ITrafficService mTrafficService;

    private MessageDispatcher mMessageDispatcher;

    private MKRoute mBusRoute;

    private String mBusRouteUid;

    private Dialog mDialog;

    private IMessageHandler mGetBusLocationUpdateHandler;

    private IMessageHandler mGetBusLocationDoneHandler;

    private boolean mIsFirstUpdate = true;

    private BaiDuDataCache mDataCache;

    private static final String[] BUS_LINE_PROJECTION = {
            ITrafficData.BaiDuData.BusLine._ID, ITrafficData.BaiDuData.BusLine.LINE_NUMBER,
            ITrafficData.BaiDuData.BusLine.START_STATION,
            ITrafficData.BaiDuData.BusLine.END_STATION
    };

    private static final int IDX_BUS_LINE_ID = 0;

    private static final int IDX_BUS_LINE_NUMBER = 1;

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
        mDataCache = new BaiDuDataCache(mActivity);
        // init map service
        FinderApplication app = (FinderApplication)mActivity.getApplication();
        mMapManager = app.getMapManager();
        mMapManager.start();

        // init traffic service
        mTrafficService = app.getTrafficService();
        mMessageDispatcher = app.getMessageDispatcher();

        mUpdateLocation = (LinearLayout)getView().findViewById(R.id.update_location);
        mUpdateIcon = (ImageView)getView().findViewById(R.id.update_icon);
        mProgress = (ProgressBar)getView().findViewById(R.id.progress_circle);
        mUpdateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRouteLocation(mBusRoute, mBusRouteUid);
                mUpdateIcon.setVisibility(View.GONE);
                mProgress.setVisibility(View.VISIBLE);
                mUpdateLocation.setEnabled(false);
                mUpdateIcon.setEnabled(false);
            }
        });
        if (mBusRoute != null && mBusRouteUid != null) {
            mUpdateLocation.setEnabled(true);
            mUpdateIcon.setEnabled(true);
        } else {
            mUpdateLocation.setEnabled(false);
            mUpdateIcon.setEnabled(false);
        }

        if (mMapView == null) {
            mMapView = (MapView)getView().findViewById(R.id.bmapView);
        }
        if (mMapController == null) {
            mMapController = mMapView.getController();
            GeoPoint point = new GeoPoint((int)(30.6633 * 1e6), (int)(104.0723 * 1e6));// Set
                                                                                       // the
                                                                                       // map
                                                                                       // center
                                                                                       // in
                                                                                       // Tianfu
                                                                                       // Square
            mMapController.setCenter(point);
            mMapController.setZoom(MAP_ZOOM_LEVEL);
            mMapController.enableClick(true);
        }
        if (mLocationListener == null) {
            mLocationListener = new MyLocationListenner();
        }

        initMapView();

        if (mLocClient == null) {
            mLocClient = new LocationClient(mActivity.getApplicationContext());
            mLocClient.registerLocationListener(mLocationListener);

            LocationClientOption option = new LocationClientOption();
            option.setOpenGps(true);// 打开gps
            option.setCoorType("bd09ll"); // 设置坐标类型
            mLocClient.setLocOption(option);
            mLocClient.start();
        }

        if (mBusLocationOverlay == null) {
            Drawable marker = getResources().getDrawable(R.drawable.bus_location_marker);
            marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
            /**
             * 创建自定义的ItemizedOverlay
             */
            mBusLocationOverlay = new CustomItemizedOverlay(marker, mActivity);
        }
        if (mLocationOverlay == null) {
            mLocationOverlay = new MyLocationOverlay(mMapView);
            if (mLocData == null) {
                mLocData = new LocationData();
                mLocationOverlay.setData(mLocData);
            }
            mMapView.getOverlays().add(mLocationOverlay);
            mLocationOverlay.enableCompass();
        }
        mMapView.refresh();
    }

    private void initMessageHandlers() {
        mGetBusLocationUpdateHandler = new IMessageHandler() {

            @Override
            public int what() {
                return ITrafficeMessage.GET_BUS_LOCATION_UPDATED;
            }

            @Override
            public void handleMessage(Message msg) {
                Integer index = (Integer)msg.obj;
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
                Toast.makeText(mActivity, getString(R.string.get_location_finished),
                        Toast.LENGTH_SHORT).show();
                mIsFirstUpdate = true;
                if (msg.arg2 != 0) {
                    String errorMessage = (String)msg.obj;
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
                mUpdateLocation.setEnabled(true);
                mUpdateIcon.setEnabled(true);
                mUpdateIcon.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
            }
        };
        mMessageDispatcher.add(mGetBusLocationUpdateHandler);
        mMessageDispatcher.add(mGetBusLocationDoneHandler);
    }

    private void clearMessageHandlers() {
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
        for (int idx = 0; idx < totalNum; idx++) {
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
        initMessageHandlers();
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
            case CLEAN_CACHE_CONFIRM_DLG:
                Builder build = new AlertDialog.Builder(mActivity);
                build.setTitle(R.string.confirm_clean_cache_title)
                        .setMessage(R.string.confirm_clean_bus_line_cache)
                        .setPositiveButton(R.string.ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Utils.copyAppDatabaseFiles(mActivity.getPackageName());
                                int rows = mDataCache.deleteAllBusLines();
                                Utils.debug(TAG, "deleted rows: " + rows);
                                getSuggestions(""); // reset the suggestions
                            }
                        }).setNegativeButton(R.string.cancel, null);
                mDialog = build.create();
                break;
            default:
                break;
        }
        if (mDialog != null) {
            mDialog.show();
        }
    }

    private void dismissWaittingDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    private void showBusLineDlg(final BDBusLine line) {
        final int routeCount = line.getRouteCount();
        final String[] busRoutes = new String[routeCount];
        for (int idx = 0; idx < routeCount; idx++) {
            String firstStation = line.getRoute(idx).getFirstStation();
            String lastStation = line.getRoute(idx).getLastStation();
            busRoutes[idx] = firstStation + " - " + lastStation;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final String titleText = String.format(getResources().getString(R.string.select_bus_route),
                line.getLineNumber());
        builder.setTitle(titleText).setSingleChoiceItems(busRoutes, 0, null)
                .setOnCancelListener(null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        final int selectedPosition = ((AlertDialog)dialog).getListView()
                                .getCheckedItemPosition();
                        final BDBusRoute route = line.getRoute(selectedPosition);
                        searchBusRoute(route.getCity(), route.getUid());
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    private void searchBusLine(final String city, final String lineNumber) {
        BDBusLine line = mDataCache.getBusLine(lineNumber);
        if (line != null) {
            dismissWaittingDialog();
            showBusLineDlg(line);
            mUpdateLocation.requestFocusFromTouch();
            return;
        }
        mTrafficService.searchBusLine(city, lineNumber, new ICompletionListener() {
            @Override
            public void onSuccess(Object result) {
                dismissWaittingDialog();
                BDBusLine line = mDataCache.getBusLine(lineNumber);
                if (line != null) {
                    showBusLineDlg(line);
                    mUpdateLocation.requestFocusFromTouch();
                }
            }

            @Override
            public void onFailure(int errorCode, String errorText) {
                Utils.debug(TAG, "Search bus line failed: " + errorText);
                dismissWaittingDialog();
                String reason = Utils.getErrorMessage(getResources(), errorCode, errorText);
                String description = getString(R.string.search_bus_line_failed, reason);
                Toast.makeText(mActivity, description, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchBusRoute(final String city, final String uid) {
        MKRoute route = mDataCache.getRoute(uid);
        if (route != null) {
            updateRoute(route, uid);
            return;
        }
        mTrafficService.searchBusRoute(city, uid, new ICompletionListener() {
            @Override
            public void onSuccess(Object result) {
                MKRoute route = (MKRoute)result;
                if (route != null) {
                    updateRoute(route, uid);
                }
            }

            @Override
            public void onFailure(int errorCode, String errorText) {
                Utils.debug(TAG, "Search bus route failed: " + errorText);
                String reason = Utils.getErrorMessage(getResources(), errorCode, errorText);
                String description = getString(R.string.search_bus_route_failed, reason);
                Toast.makeText(mActivity, description, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getRouteLocation(MKRoute route, String uid) {
        if (route == null) {
            return;
        }
        String lineNumber = mDataCache.getRouteLineNumber(uid);
        if (Utils.isValidBusLineNumber(lineNumber)) {
            Toast.makeText(mActivity, getString(R.string.start_get_location), Toast.LENGTH_SHORT)
                    .show();
            mTrafficService.getBusLocation(lineNumber, getRouteStationNames(route));
        }
    }

    private void drawRoute(MKRoute route) {
        RouteOverlay routeOverlay = new RouteOverlay(mActivity, mMapView);
        routeOverlay.setData(route);
        mMapView.getOverlays().clear();
        mMapView.getOverlays().add(routeOverlay);
        mMapView.getOverlays().add(mLocationOverlay);
        mMapView.refresh();
        mMapView.getController().animateTo(route.getStart());
        mBusLocationOverlay.removeAllOverlay();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_cache:
                showDialog(CLEAN_CACHE_CONFIRM_DLG);
                return true;
            case R.id.about:
                startActivity(new Intent(mActivity, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the options menu from XML
        inflater.inflate(R.menu.menu_bus_location, menu);

        // Get the SearchView and set the searchable configuration
        mSearchView = (SearchView)menu.findItem(R.id.search_bus_location).getActionView();
        Utils.debug(TAG, mSearchView.getClass().getName());

        SearchManager manager = (SearchManager)this.getSherlockActivity().getSystemService(
                Context.SEARCH_SERVICE);
        SearchableInfo info = manager.getSearchableInfo(this.getSherlockActivity()
                .getComponentName());
        mSearchView.setSearchableInfo(info);
        mSearchView.setQueryHint(getResources().getText(R.string.bus_number_hint));

        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String lineNumber = query.toUpperCase();
                if (Utils.isValidBusLineNumber(lineNumber)) {
                    String city = getResources().getString(R.string.default_city);
                    Utils.hideSoftKeyboard(mActivity, mSearchView);
                    showDialog(BUS_LINE_SEARCH_DLG);
                    searchBusLine(city, lineNumber);
                } else {
                    Toast.makeText(mActivity, R.string.invalid_line_number, Toast.LENGTH_LONG)
                            .show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mActivity == null) {
                    return false;
                }
                getSuggestions(newText);
                return true;
            }
        });
        mSearchView.setOnSuggestionListener(new OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor)mSearchView.getSuggestionsAdapter().getItem(position);
                int suggestionIndex = cursor
                        .getColumnIndex(ITrafficData.BaiDuData.BusLine.LINE_NUMBER);
                mSearchView.setQuery(cursor.getString(suggestionIndex), true);
                return true;
            }
        });

        EditText searchEditText = (EditText)mSearchView.findViewById(R.id.abs__search_src_text);
        if (searchEditText != null) {
            searchEditText.setEms(10);
            searchEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
            searchEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    String queryText = mSearchView.getQuery().toString();
                    if (hasFocus) {
                        getSuggestions(queryText);
                        mSearchView.setQuery(queryText, false);
                    }
                }
            });
        } else {
            mSearchView.setInputType(InputType.TYPE_CLASS_TEXT);
            mSearchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    String queryText = mSearchView.getQuery().toString();
                    if (hasFocus) {
                        getSuggestions(queryText);
                        mSearchView.setQuery(queryText, false);
                    }
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void getSuggestions(String queryText) {
        Cursor cursor = queryBusLines(queryText);
        String[] from = new String[] {
                ITrafficData.BaiDuData.BusLine.LINE_NUMBER,
                ITrafficData.BaiDuData.BusLine.START_STATION,
                ITrafficData.BaiDuData.BusLine.END_STATION
        };
        int[] to = new int[] {
                R.id.line_number, R.id.start_station, R.id.end_station
        };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(mActivity,
                R.layout.bus_line_suggestion_item, cursor, from, to, 0);
        mSearchView.setSuggestionsAdapter(adapter);
    }

    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return;

            mLocData.latitude = location.getLatitude();
            mLocData.longitude = location.getLongitude();
            mLocData.direction = 2.0f;
            mLocData.accuracy = location.getRadius();
            mLocData.direction = location.getDerect();
            Log.d("loctest",
                    String.format("before: lat: %f lon: %f", location.getLatitude(),
                            location.getLongitude()));
            // GeoPoint p = CoordinateConver.fromGcjToBaidu(new
            // GeoPoint((int)(locData.latitude* 1e6), (int)(locData.longitude *
            // 1e6)));
            // Log.d("loctest",String.format("before: lat: %d lon: %d",
            // p.getLatitudeE6(),p.getLongitudeE6()));
            mLocationOverlay.setData(mLocData);
            mMapView.refresh();
            mMapController.animateTo(new GeoPoint((int)(mLocData.latitude * 1e6),
                    (int)(mLocData.longitude * 1e6)), null);
        }

        @Override
        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null) {
                return;
            }
        }

    }

    private Cursor queryBusLines(String lineNumber) {
        ContentResolver resolver = mActivity.getContentResolver();
        String sortOrder = ITrafficData.BaiDuData.BusLine.LAST_UPDATE_TIME + " DESC "
                + "LIMIT 0,30";
        String selection = null, selectionArgs[] = null;
        if (!TextUtils.isEmpty(lineNumber)) {
            selection = ITrafficData.BaiDuData.BusLine.LINE_NUMBER + " LIKE ?";
            selectionArgs = new String[] {
                lineNumber + "%"
            };
        }
        Cursor cursor = resolver.query(ITrafficData.BaiDuData.BusLine.CONTENT_URI,
                BUS_LINE_PROJECTION, selection, selectionArgs, sortOrder);
        return cursor;
    }

    private void updateRoute(MKRoute route, String uid) {
        if (route != null && !TextUtils.isEmpty(uid)) {
            mBusRoute = route;
            mBusRouteUid = uid;
            mUpdateLocation.setEnabled(true);
            mUpdateIcon.setEnabled(true);
            drawRoute(route);
        }
    }

}
