package com.telepathic.finder.app;

import java.util.ArrayList;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.telepathic.finder.R;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.ITrafficService;

public class BusTransferFragment extends SherlockFragment {
    private MainActivity mActivity;

    private AutoCompleteTextView mStartStation;

    private ITrafficService mTrafficService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bus_transfer, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity)getSherlockActivity();

        FinderApplication app = (FinderApplication)mActivity.getApplication();
        mTrafficService = app.getTrafficService();

        mStartStation = (AutoCompleteTextView)getView().findViewById(R.id.start_station);
        mStartStation.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTrafficService.queryStationName(s.toString(), new ICompletionListener() {

                    @Override
                    public void onSuccess(Object result) {
                        ArrayList<String> stations = (ArrayList<String>)result;
                        if (stations != null && stations.size() > 0) {
                            for (String station : stations) {
                                Log.d("debug", "station====" + station);
                            }
                        }

                    }

                    @Override
                    public void onFailure(int errorCode, String errorText) {
                        // TODO Auto-generated method stub

                    }
                });

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
    }



}
