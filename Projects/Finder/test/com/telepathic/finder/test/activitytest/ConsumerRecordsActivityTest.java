
package com.telepathic.finder.test.activitytest;

import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.telepathic.finder.R;
import com.telepathic.finder.app.ConsumerRecordsActivity;

public class ConsumerRecordsActivityTest extends
ActivityInstrumentationTestCase2<ConsumerRecordsActivity> {
    private static final String CARD_ID_CACHE = "card_id_cache";

    private ConsumerRecordsActivity mActivity;

    private AutoCompleteTextView mEditText;

    private Button mSendButton;

    public ConsumerRecordsActivityTest() {
        super("com.telepathic.finder", ConsumerRecordsActivity.class);
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
    public void testSaveCardID() {
        mEditText.setText("10802436");
        mSendButton.performClick();
        mEditText.setText("10808691");
        mSendButton.performClick();
        assertEquals(2, mEditText.getAdapter().getCount());
        String item1 = (String)mEditText.getAdapter().getItem(0);
        String item2 = (String)mEditText.getAdapter().getItem(1);
        assertEquals(true,
                ((item1.equals("10802436") && item2.equals("10808691")) ||
                        (item2.equals("10802436") && item1.equals("10808691"))));
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
    }

}
