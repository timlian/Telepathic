
package com.telepathic.finder.app;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.telepathic.finder.R;

public class AboutActivity extends SherlockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

}
