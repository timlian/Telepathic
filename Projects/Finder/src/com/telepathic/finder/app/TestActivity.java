package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
import com.telepathic.finder.util.Utils;

import android.R.integer;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TestActivity extends Activity {
    private static final String TAG = "TestActivity";
    
    private Button mSendButton;
    private MessageSender mMessageSender;
    private EditText mEditText;
    private TextView mTextView;
    
    private TrafficService mTrafficService;
    private BusLineRoute mBusLineRoute;
    private Timer mTimer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        
        mTimer = new Timer();
        mTrafficService = TrafficService.getTrafficService();
        
        mEditText = (EditText) findViewById(R.id.enter_busline);
        mTextView = (TextView) findViewById(R.id.busline_info);
        
        mSendButton = (Button) findViewById(R.id.get_busline);
        mSendButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                String lineName = mEditText.getText().toString();
                mTrafficService.getBusLineRoute(lineName, new MyBusLineListener());
                mSendButton.setEnabled(false);
                mTextView.setText("Waiting...");
                Utils.hideSoftKeyboard(getApplicationContext(), mEditText);
                mTrafficService.getBusLocation(lineName, "新会展中心公交站", "新会展中心公交站", null);
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
                    buffer.append(mBusLineRoute.getLineName() + "路 ");
                    buffer.append("方向: " + mBusLineRoute.getType());
                    buffer.append(" 首车: "+ mBusLineRoute.getDepartureTime());
                    buffer.append(" 末车: " + mBusLineRoute.getCloseoffTime());
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
                    mTextView.setText(buffer.toString());
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
                    mTextView.setText(paramServerMessage.content.toString());
                    mSendButton.setEnabled(true);
                }
            });
            
            Log.d(TAG, paramServerMessage.toString());
        }
    }
}
