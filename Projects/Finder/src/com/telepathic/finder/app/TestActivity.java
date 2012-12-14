package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.Timer;

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
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.RouteOverlay;

import com.telepathic.finder.R;
import com.telepathic.finder.network.ksoap.message.ClientMessage;
import com.telepathic.finder.network.ksoap.message.listener.MessageSendListener;
import com.telepathic.finder.network.ksoap.message.server.ServerMessage;
import com.telepathic.finder.network.ksoap.sender.MessageSender;
import com.telepathic.finder.sdk.BusLineListener;
import com.telepathic.finder.sdk.BusLineRoute;
import com.telepathic.finder.sdk.BusLocationListener;
import com.telepathic.finder.sdk.BusStation;
import com.telepathic.finder.sdk.TrafficService;

public class TestActivity extends MapActivity {
    private static final String TAG = "TestActivity";
    
    private static final String MAP_DEV_KEY = "A963422DFFFC8530BDDC5FF0063205F9E2D98461";
    
    private Button mSendButton;
    private MessageSender mMessageSender;
    private EditText mEditText;
    
    private TrafficService mTrafficService;
    private BusLineRoute mBusLineRoute;
    
    private MapView mMapView = null;    // 地图View
    private MKSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用
    private String  mCityName = null;
    private BMapManager mMapManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        
        FinderApplication app = (FinderApplication)this.getApplication();
        if (app.mBMapMan == null) {
            app.mBMapMan = new BMapManager(getApplication());
            app.mBMapMan.init(app.mStrKey, new FinderApplication.MyGeneralListener());
        }
        app.mBMapMan.start();
        // 如果使用地图SDK，请初始化地图Activity
        super.initMapActivity(app.mBMapMan);
        
        mMapView = (MapView)findViewById(R.id.bmapView);
        mMapView.setBuiltInZoomControls(true);
        //设置在缩放动画过程中也显示overlay,默认为不绘制
        mMapView.setDrawOverlayWhenZooming(true);
        
        // 初始化搜索模块，注册事件监听
        mSearch = new MKSearch();
        mSearch.init(app.mBMapMan, new MKSearchListener(){

            @Override
            public void onGetPoiDetailSearchResult(int type, int error) {
            }
            
            public void onGetPoiResult(MKPoiResult res, int type, int error) {
                // 错误号可参考MKEvent中的定义
                if (error != 0 || res == null) {
                    Toast.makeText(TestActivity.this, "抱歉，未找到结果", Toast.LENGTH_LONG).show();
                    return;
                }
                
                // 找到公交路线poi node
                MKPoiInfo curPoi = null;
                int totalPoiNum  = res.getNumPois();
                for( int idx = 0; idx < totalPoiNum; idx++ ) {
                    curPoi = res.getPoi(idx);
                    if ( 2 == curPoi.ePoiType ) {
                        // poi类型，0：普通点，1：公交站，2：公交线路，3：地铁站，4：地铁线路
                        mSearch.busLineSearch(mCityName, curPoi.uid);
                        break;
                    }
                }
                
                // 没有找到公交信息
                if (curPoi == null) {
                    Toast.makeText(TestActivity.this, "抱歉，未找到结果", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(TestActivity.this, "抱歉，未找到结果", Toast.LENGTH_LONG).show();
                    return;
                }

                RouteOverlay routeOverlay = new RouteOverlay(TestActivity.this, mMapView);
                // 此处仅展示一个方案作为示例
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
        
       // mTrafficService = TrafficService.getTrafficService();
        
        mEditText = (EditText) findViewById(R.id.searchkey);
        
        mSendButton = (Button) findViewById(R.id.search);
        mSendButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                String lineName = mEditText.getText().toString();
//                mTrafficService.getBusLineRoute(lineName, new MyBusLineListener());
//                mSendButton.setEnabled(false);
//                Utils.hideSoftKeyboard(getApplicationContext(), mEditText);
//                mTrafficService.getBusLocation(lineName, "鏂颁細灞曚腑蹇冨叕浜ょ珯", "鏂颁細灞曚腑蹇冨叕浜ょ珯", null);
                mSearch.poiSearchInCity("成都", lineName);
            }
        });
        
        
    }
    
    private MessageSender getMessageSender() {
        if (mMessageSender != null) {
            mMessageSender.cancel();
        }
        mMessageSender = new MessageSender();
        mMessageSender.setListener(new MyMessageSenderListener());
        return mMessageSender;
    }
    
    private class MyBusLineListener implements BusLineListener {

        @Override
        public void onSuccess(final BusLineRoute route) {
            mBusLineRoute = route;
            ArrayList<BusStation> stations = mBusLineRoute.getStations();
            final String lastStation = stations.get(stations.size() - 1).getName();
            mTrafficService.getBusLocation(mBusLineRoute.getLineName(), lastStation, lastStation, new MyBusLocationListener());
//            mTimer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    mTrafficService.getBusLocation(mBusLineRoute.getLineName(), lastStation, lastStation, new MyBusLocationListener());
//                }
//            }, 0, 5000);
            
        }

        @Override
        public void onError(String errorMessage) {
            // TODO Auto-generated method stub
            
        }
    }
    
    private class MyBusLocationListener implements BusLocationListener {

        @Override
        public void onSuccess(String lineNumber, final String distance) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(mBusLineRoute.getLineName() + "璺�");
                    buffer.append("鏂瑰悜: " + mBusLineRoute.getType());
                    buffer.append(" 棣栬溅: "+ mBusLineRoute.getDepartureTime());
                    buffer.append(" 鏈溅: " + mBusLineRoute.getCloseoffTime());
                    buffer.append("\n\n");
                    ArrayList<BusStation> stations = mBusLineRoute.getStations();
                    int currentPosition = stations.size() - Integer.parseInt(distance) - 1;
                    for(int i = 0; i < stations.size(); i++) {
                        buffer.append(i + 1 + ".");
                        buffer.append(stations.get(i).getName());
                        if (currentPosition == i) {
                            buffer.append("     Here!!!");
                        }
                        buffer.append("\n");
                    }
                    mSendButton.setEnabled(true);
                }
            });
            
        }

        @Override
        public void onError(String errorMessage) {
            // TODO Auto-generated method stub
            
        }
        
    }
    private class MyMessageSenderListener implements MessageSendListener {
        private MyMessageSenderListener() {
        }

        public final boolean onError(Exception paramException,
                MessageSender paramMessageSender,
                ClientMessage paramClientMessage) {
            return false;
        }

        public final void onMessageRecieved(final ServerMessage paramServerMessage,
                MessageSender paramMessageSender,
                ClientMessage paramClientMessage) {
            // NetworkUI2.this.onSuccessResult(paramServerMessage,
            // paramMessageSender, paramClientMessage);
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    mSendButton.setEnabled(true);
                }
            });
            
            Log.d(TAG, paramServerMessage.toString());
        }
    }
    
    @Override
    protected void onPause() {
        FinderApplication app = (FinderApplication)this.getApplication();
        app.mBMapMan.stop();
        super.onPause();
    }
    @Override
    protected void onResume() {
        FinderApplication app = (FinderApplication)this.getApplication();
        app.mBMapMan.start();
        super.onResume();
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }
    
    static class MyGeneralListener implements MKGeneralListener {
        @Override
        public void onGetNetworkState(int iError) {
            Log.d("MyGeneralListener", "onGetNetworkState error is "+ iError);
//            Toast.makeText(getApplicationContext(), "��������������",
//                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onGetPermissionState(int iError) {
            Log.d("MyGeneralListener", "onGetPermissionState error is "+ iError);
//            if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
//                // ��ȨKey����
//                Toast.makeText(getApplicationContext(), 
//                        "����BMapApiDemoApp.java�ļ�������ȷ����ȨKey��",
//                        Toast.LENGTH_LONG).show();
//                BMapApiDemoApp.mDemoApp.m_bKeyRight = false;
//            }
        }
    }
}
