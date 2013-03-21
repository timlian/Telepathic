package com.telepathic.finder.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.baidu.mapapi.BMapManager;
import com.telepathic.finder.R;

public class SplashActivity extends Activity {

    private static final int SPLASH_DISPLAY_LENGHT = 1000 * 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_view);
        // New Handler to start the HomeActivity and close this SplashActivity after some seconds.
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
            	Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGHT);
        // init map service
        BMapManager mapManager = ((FinderApplication)getApplication()).getMapManager();
        if (mapManager != null) {
        	mapManager.start();
        }
    }
}
