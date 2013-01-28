
package com.telepathic.finder.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.telepathic.finder.R;
import com.telepathic.finder.adapter.ConsumeRecordAdapter;
import com.telepathic.finder.adapter.GalleryAdapter;
import com.telepathic.finder.util.Utils;

public class BusCardRecordActivity2 extends Activity {

    private Button mSendButton;
    private AutoCompleteTextView mEditText;
    private ViewPager mViewPager;
    private Gallery mGallery;
    private ConsumeRecordAdapter mViewPagerAdapter;
    private GalleryAdapter mGalleryAdapter;
    private LinearLayout mNoItemTips;
    private RelativeLayout mConsumptionDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_card_record);
        initView();
    }

    private void initView(){
        mSendButton = (Button)findViewById(R.id.search);
        mEditText = (AutoCompleteTextView)findViewById(R.id.key_card_id);
        mNoItemTips = (LinearLayout)findViewById(R.id.no_item_tips);
        mConsumptionDetail = (RelativeLayout)findViewById(R.id.consumption_detail);
    }

    public void onSearchCardIdClicked(View v){
        if(mSendButton.equals(v)) {
            String cardNumber = mEditText.getText().toString();
            if (Utils.isValidBusCardNumber(cardNumber)) {
                //TODO: Get the consumer record
            } else {
                mEditText.setError(getResources().getString(R.string.card_id_error_notice));
            }
        }
    }

}
