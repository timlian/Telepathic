package com.telepathic.finder.test.app;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import com.telepathic.finder.R;
import com.telepathic.finder.app.BusLocationActivity;

public class BusLocationActivityTest extends ActivityInstrumentationTestCase2<BusLocationActivity> {

    public BusLocationActivity mActivity;

    private ImageButton mBtnSearch;

    private AutoCompleteTextView mTvSearchKey;

    public BusLocationActivityTest(){
        super("com.telepathic.finder", BusLocationActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();

        mBtnSearch = (ImageButton) mActivity.findViewById(R.id.search);

        mTvSearchKey = (AutoCompleteTextView) mActivity.findViewById(R.id.search_key);
    }

    public void testPreconditions() {
        assertNotNull(mActivity);
        assertNotNull(mBtnSearch);
        assertNotNull(mTvSearchKey);
    }

    public void testViewText() {
        assertEquals(mActivity.getResources().getString(R.string.bus_number_hint), mTvSearchKey.getHint());
    }

    @UiThreadTest
    public void testSearchBusline() {
        // Test the legal data
        mTvSearchKey.setText("501");
        mBtnSearch.performClick();

        mTvSearchKey.setText("504A");
        mBtnSearch.performClick();

        // Test the illegal data
        mTvSearchKey.setText("50123");
        mBtnSearch.performClick();

        mTvSearchKey.setText("501AB");
        mBtnSearch.performClick();

        // Test the legal but invalid data
        mTvSearchKey.setText("501A");
        mBtnSearch.performClick();
    }

}
