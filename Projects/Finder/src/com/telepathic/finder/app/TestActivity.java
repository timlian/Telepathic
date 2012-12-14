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
                String lineName = mEditText.getText().toString();
                mTrafficService.getBusLineRoute(lineName, new MyBusLineListener());
                mSendButton.setEnabled(false);
                mTextBusInfo.setText("Waiting...");
                Utils.hideSoftKeyboard(getApplicationContext(), mEditText);
//                mTrafficService.getBusLocation(lineName, "鏂颁細灞曚腑蹇冨叕浜ょ珯", "鏂颁細灞曚腑蹇冨叕浜ょ珯", null);
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
}
