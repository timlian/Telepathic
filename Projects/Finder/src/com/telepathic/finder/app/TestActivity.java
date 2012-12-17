package com.telepathic.finder.app;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.telepathic.finder.R;
import com.telepathic.finder.sdk.BusLineListener;
import com.telepathic.finder.sdk.BusLineRoute;
import com.telepathic.finder.sdk.BusLocationListener;
import com.telepathic.finder.sdk.BusStation;
import com.telepathic.finder.sdk.ChargeRecordsListener;
import com.telepathic.finder.sdk.TrafficService;
import com.telepathic.finder.util.Utils;

public class TestActivity extends Activity {
    private static final String TAG = "TestActivity";

    private Button mSendButton;
    private EditText mEditText;
    private TextView mTextBusInfo;

    private TrafficService mTrafficService;
    private BusLineRoute mBusLineRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);

        mTrafficService = TrafficService.getTrafficService();

        mEditText = (EditText) findViewById(R.id.searchkey);
        mTextBusInfo = (TextView) findViewById(R.id.bus_info);
        mSendButton = (Button) findViewById(R.id.search);
        mSendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String number = mEditText.getText().toString();
                if (number.length()> 0 && number.length() < 4) {
                    mTrafficService.getBusLineRoute(number, new MyBusLineListener());
                    //mTrafficService.getBusLocation(number, "新会展中心公交站", "新会展中心公交站", new MyBusLocationListener());
                    mSendButton.setEnabled(false);
                    mTextBusInfo.setText("fetching bus line route for " + number + " ... ");
                    Utils.hideSoftKeyboard(getApplicationContext(), mEditText);
                } else if (number.length() == 8) {
                    mTrafficService.getChargeRecords(number, 10, new MyChargeRecordsListener());
                    mSendButton.setEnabled(false);
                    mTextBusInfo.setText("fetching charge records for " + number + " ... ");
                    Utils.hideSoftKeyboard(getApplicationContext(), mEditText);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter the correct parameter.",
                            Toast.LENGTH_SHORT).show();
                }
                
            }
        });
    }

    private class MyBusLineListener implements BusLineListener {

        @Override
        public void onSuccess(final BusLineRoute route) {
            mBusLineRoute = route;
            ArrayList<BusStation> stations = mBusLineRoute.getStations();
            final String lastStation = stations.get(stations.size() - 1).getName();
            mTrafficService.getBusLocation(mBusLineRoute.getLineName(), lastStation, lastStation, new MyBusLocationListener());
        }

        @Override
        public void onError(final String errorMessage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSendButton.setEnabled(true);
                    mTextBusInfo.setText("");
                    Toast.makeText(TestActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private class MyBusLocationListener implements BusLocationListener {

        @Override
        public void onSuccess(String lineNumber, final String distance) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(mBusLineRoute.getLineName() + "路");
                    buffer.append(" 方向: " + mBusLineRoute.getType());
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
                    mSendButton.setEnabled(true);
                    mTextBusInfo.setText(buffer.toString());
                }
            });

        }

        @Override
        public void onError(final String errorMessage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSendButton.setEnabled(true);
                    mTextBusInfo.setText("");
                    Toast.makeText(TestActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    
    private class MyChargeRecordsListener implements ChargeRecordsListener {

        @Override
        public void onSuccess(final String result) {
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    mSendButton.setEnabled(true);
                    mTextBusInfo.setText(result);
                }
            });
            
        }

        @Override
        public void onError(final String errorMessage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSendButton.setEnabled(true);
                    mTextBusInfo.setText("");
                    Toast.makeText(TestActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
            
        }
        
    }
}
