package com.telepathic.finder.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.baidu.mapapi.BMapManager;
import com.telepathic.finder.R;
import com.telepathic.finder.util.UmengEvent;
import com.telepathic.finder.util.Utils;
import com.umeng.analytics.MobclickAgent;

public class SplashActivity extends Activity {

    private static final int SPLASH_DISPLAY_LENGHT = 1000 * 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.onError(this);
        MobclickAgent.onEvent(this, UmengEvent.OTHER_PHONE_NUMBER, Utils.getPhoneNumber(this));
        MobclickAgent.updateOnlineConfig(this);
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

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
}
