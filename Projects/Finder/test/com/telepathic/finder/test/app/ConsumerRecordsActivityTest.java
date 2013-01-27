
package com.telepathic.finder.test.app;

import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.telepathic.finder.R;
import com.telepathic.finder.app.BusCardRecordActivity;

public class ConsumerRecordsActivityTest extends
ActivityInstrumentationTestCase2<BusCardRecordActivity> {
    private static final String CARD_ID_CACHE = "card_id_cache";

    private BusCardRecordActivity mActivity;

    private AutoCompleteTextView mEditText;

    private Button mSendButton;

    public ConsumerRecordsActivityTest() {
        super("com.telepathic.finder", BusCardRecordActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mEditText = (AutoCompleteTextView)mActivity.findViewById(R.id.searchkey);
        mSendButton = (Button)mActivity.findViewById(R.id.search);

        SharedPreferences preferences = mActivity.getSharedPreferences(CARD_ID_CACHE,
                mActivity.MODE_PRIVATE);
        preferences.edit().clear().commit();
    }

    public void testPreconditions() {
        assertNotNull(mEditText);
        assertNotNull(mSendButton);
    }

    public void testViewText() {
        assertEquals(mActivity.getResources().getString(R.string.ic_card_hint), mEditText.getHint());
        assertEquals(mActivity.getResources().getString(R.string.find), mSendButton.getText());
    }

    @UiThreadTest
    public void testIllegalInput() {
        // Test input the empty string
        mEditText.setError(null);
        mEditText.setText("");
        mSendButton.performClick();
        assertEquals(mActivity.getResources().getString(R.string.card_id_error_notice), mEditText.getError());

        // Test input null
        mEditText.setError(null);
        mEditText.setText(null);
        mSendButton.performClick();
        assertEquals(mActivity.getResources().getString(R.string.card_id_error_notice), mEditText.getError());

        // Test input string length less than 8
        mEditText.setError(null);
        mEditText.setText("10802");
        mSendButton.performClick();
        assertEquals(mActivity.getResources().getString(R.string.card_id_error_notice), mEditText.getError());

        // Test input string length more than 8
        mEditText.setError(null);
        mEditText.setText("108092345");
        mSendButton.performClick();
        assertEquals(mActivity.getResources().getString(R.string.card_id_error_notice), mEditText.getError());

        // Test a non-existed card ID
        mEditText.setError(null);
        mEditText.setText("12345678");
        mSendButton.performClick();
    }

}
