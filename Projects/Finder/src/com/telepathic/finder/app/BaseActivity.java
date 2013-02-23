package com.telepathic.finder.app;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.telepathic.finder.R;

public class BaseActivity extends FragmentActivity{

    private static final int BASE_ACTIVITY_CUSTOM_DIALOG_START = 1000;

    private static final int EXIT_CONFIRM_DIALOG = BASE_ACTIVITY_CUSTOM_DIALOG_START + 1;

    Context getContext() {
        return this;
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//    	super.onCreate(savedInstanceState);
//    	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 注意顺序   
//        setContentView(R.layout.main); // 注意顺序   
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,      // 注意顺序   
//        R.layout.title);   
//    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog retDlg = null;
        switch (id) {
            case EXIT_CONFIRM_DIALOG:
                Builder exitDlgBuilder = new Builder(getContext())
                .setTitle(R.string.confirm_exit_title)
                .setMessage(R.string.confirm_exit_message)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BaseActivity.this.finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
                retDlg = exitDlgBuilder.create();
                break;
            default:
                break;
        }
        return retDlg;
    }


}
