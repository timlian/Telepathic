
package com.telepathic.finder.app;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import cn.domob.android.ads.DomobUpdater;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.telepathic.finder.R;
import com.telepathic.finder.util.UmengEvent;
import com.umeng.analytics.MobclickAgent;

public class AboutActivity extends SherlockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.onEvent(this, UmengEvent.OTHER_ABOUT);
        setContentView(R.layout.activity_about);

        try {
            PackageManager pm = getPackageManager();
            PackageInfo pkgInfo;
            pkgInfo = pm.getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
            String version = pkgInfo.versionName;
            String displayVersion = getString(R.string.version, version);
            TextView tvVersion = (TextView)findViewById(R.id.version);
            tvVersion.setText(displayVersion);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView tvDescription = (TextView)findViewById(R.id.description);
        tvDescription.setText(Html.fromHtml(getResources().getString(R.string.app_description)));

        TextView tvCopyright = (TextView)findViewById(R.id.copyright);
        tvCopyright.setText(Html.fromHtml(getResources().getString(R.string.copyright)));

        // Use Domob SDK to check update
        DomobUpdater.checkUpdate(AboutActivity.this, getString(R.string.publisher_id));
    }

    @Override
    protected void onStart() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.back_window_forward, R.anim.push_right_out);
    }

}
