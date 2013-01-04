package com.telepathic.finder.app;

import com.telepathic.finder.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends Activity {
    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
    }

    public void onBusLocationClicked(View view) {
        Intent intent = new Intent(this, BusLocationActivity.class);
        startActivity(intent);
    }

    public void onBusTestClicked(View view) {
        Intent intent = new Intent(this, ConsumerRecordsActivity.class);
        startActivity(intent);
    }
}
