package com.telepathic.finder.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKRoute;
import com.baidu.mapapi.MKStep;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.RouteOverlay;
import com.telepathic.finder.R;
import com.telepathic.finder.app.MessageDispatcher.IMessageHandler;
import com.telepathic.finder.sdk.ITrafficListeners;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.ITrafficeMessage;
import com.telepathic.finder.sdk.traffic.entity.BusRoute;
import com.telepathic.finder.util.Utils;

public class BusLocationActivity extends MapActivity {
	private static final String TAG = BusLocationActivity.class.getSimpleName();

    private static final int CUSTOM_DIALOG_ID_START = 100;

    private static final int BUS_LINE_SEARCH_DLG  = CUSTOM_DIALOG_ID_START + 1;

    private static final int DOWN_VOICE_SEARCH_DLG = CUSTOM_DIALOG_ID_START + 2;

    private static final int DOWN_VOICE_SEARCH_THROUGH_BROWSER_DLG = CUSTOM_DIALOG_ID_START + 3;

    private static final int CUSTOM_INTENT_REQUEST_CODE_START = 0x1000;

    private static final int START_SPEECH_RECOGNIZE = CUSTOM_INTENT_REQUEST_CODE_START + 1;

    private static final int MAP_ZOOM_LEVEL = 14;

    private ImageButton mBtnSearch;

    private AutoCompleteTextView mTvSearchKey;

    private ImageView mIvSpeak;

    private MapView mMapView;
    private BMapManager mMapManager;

    private MyLocationOverlay mLocationOverlay;  //定位图层
    private LocationListener mLocationListener; //onResume时注册此listener，onPause时需要Remove
    private ITrafficService mTrafficService;
    private MessageDispatcher mMessageDispatcher;
    
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
        super.initMapActivity(mMapManager);

        // init traffic service
        mTrafficService = app.getTrafficService();
        mMessageDispatcher = app.getMessageDispatcher();
        initMessageHandlers();

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

    private void initMessageHandlers() {
    	mMessageDispatcher.add(new IMessageHandler() {
			@Override
			public int what() {
				return ITrafficeMessage.SEARCH_BUS_LINE_DONE;
			}
			
			@Override
			public void handleMessage(Message msg) {
				ArrayList<MKPoiInfo> busPois = (ArrayList<MKPoiInfo>)msg.obj;
				if (busPois != null && busPois.size() > 0) {
					removeDialog(BUS_LINE_SEARCH_DLG);
					showBusRoutesDlg("102", busPois);
				}
			}
		});
    	
    	mMessageDispatcher.add(new IMessageHandler() {
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
	            mMapView.invalidate();
	            mMapView.getController().animateTo(route.getStart());
	            mBtnSearch.setEnabled(true);
	            //mTrafficService.getBusLocation(getRouteStationNames(route));
			}
		});
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
            mTrafficService.searchBusLine(city, lineNumber);
            Utils.debug(TAG, "UI Thread: " + Thread.currentThread().toString());
        } else {
            Toast.makeText(this, R.string.invalid_input_hint,Toast.LENGTH_LONG).show();
        }
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
        mMapManager.getLocationManager().removeUpdates(mLocationListener);
        mLocationOverlay.disableMyLocation();
        mMapManager.stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapManager.getLocationManager().requestLocationUpdates(mLocationListener);
        mLocationOverlay.enableMyLocation();
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
            default:
                break;
        }
        return retDialog;
    }


    @Override
    protected boolean isRouteDisplayed() {
        return false;
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
    
	private ITrafficListeners.BusLocationListener mBusLocationListener = new ITrafficListeners.BusLocationListener() {
		@Override
		public void onReceived(final MKStep station) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (station != null) {
						updateBusLocation(station);
					}
				}
			});
		}
	};

}
