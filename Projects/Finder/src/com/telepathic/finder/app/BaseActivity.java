package com.telepathic.finder.app;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v4.app.FragmentActivity;

import com.telepathic.finder.R;

public class BaseActivity extends FragmentActivity{

    private static final int BASE_ACTIVITY_CUSTOM_DIALOG_START = 1000;

    private static final int EXIT_CONFIRM_DIALOG = BASE_ACTIVITY_CUSTOM_DIALOG_START + 1;

    Context getContext() {
        return this;
    }

    @Override
    public void onBackPressed() {
        showDialog(EXIT_CONFIRM_DIALOG);
    }

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
