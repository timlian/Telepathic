
package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.HashMap;

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
import com.telepathic.finder.adapter.BusCardPageAdapter;
import com.telepathic.finder.adapter.GalleryAdapter;
import com.telepathic.finder.sdk.traffic.BusCard;
import com.telepathic.finder.sdk.traffic.ConsumerRecord;
import com.telepathic.finder.sdk.traffic.ConsumerRecord.ConsumerType;
import com.telepathic.finder.sdk.traffic.CountConsumerRecord;
import com.telepathic.finder.sdk.traffic.EWalletConsumerRecord;
import com.telepathic.finder.util.Utils;

public class BusCardRecordActivity2 extends Activity {

    private ArrayList<BusCard> mBusCardList = new ArrayList<BusCard>();
    private Button mSendButton;
    private AutoCompleteTextView mEditText;
    private ViewPager mViewPager;
    private Gallery mGallery;
    private BusCardPageAdapter mViewPagerAdapter;
    private GalleryAdapter mGalleryAdapter;
    private LinearLayout mNoItemTips;
    private RelativeLayout mConsumptionDetail;

    private static final String KEY_CARD_ID        = "cardID";
    private static final String KEY_LINE_NUMBER    = "lineNumber";
    private static final String KEY_BUS_NUMBER     = "busNumber";
    private static final String KEY_CONSUMER_TIME  = "consumerTime";
    private static final String KEY_CONSUMER_COUNT = "consumerCount";
    private static final String KEY_RESIDUAL_COUNT = "residualCount";
    private static final String KEY_CONSUMER_AMOUNT = "consumerAmount";
    private static final String KEY_RESIDUAL_AMOUNT = "residualAmount";
    private static final String KEY_CONSUMPTION_TYPE = "type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_card_record);
        initTestData();
        initView();
        mViewPagerAdapter = new BusCardPageAdapter(this, mBusCardList);
        mViewPager.setAdapter(mViewPagerAdapter);
    }

    private void initView(){
        mSendButton = (Button)findViewById(R.id.search);
        mEditText = (AutoCompleteTextView)findViewById(R.id.key_card_id);
        mNoItemTips = (LinearLayout)findViewById(R.id.no_item_tips);
        mConsumptionDetail = (RelativeLayout)findViewById(R.id.consumption_detail);
        mGallery = (Gallery)findViewById(R.id.viewpager_tab);
        mViewPager = (ViewPager)findViewById(R.id.viewpager);
        mNoItemTips.setVisibility(View.GONE);
        mConsumptionDetail.setVisibility(View.VISIBLE);
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

    private void initTestData() {
        BusCard card1 = getConsumptionInfo(TEST_DATA_1);
        BusCard card2 = getConsumptionInfo(TEST_DATA_2);
        BusCard card3 = getConsumptionInfo(TEST_DATA_3);
        mBusCardList.add(card1);
        mBusCardList.add(card2);
        mBusCardList.add(card3);
    }

    public BusCard getConsumptionInfo(String[] data) {
        ArrayList<ConsumerRecord> consumerRecords = createTestConsumerRecords(data);
        BusCard card = new BusCard();
        for (ConsumerRecord record : consumerRecords) {
            switch(record.getType()){
                case COUNT:
                    if (card.getResidualCount() == null) {
                        card.setResidualCount(record.getResidual());
                    }
                    break;
                case EWALLET:
                    if (card.getResidualAmount() == null) {
                        card.setResidualAmount(record.getResidual());
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown consumer type !!!");
            }
            if (card.getResidualAmount() != null && card.getResidualCount() != null) {
                break;
            }
        }
        card.setConsumerRecords(consumerRecords);
        card.setCardNumber(consumerRecords.get(0).getCardId().substring(4));
        card.setLastDate(consumerRecords.get(0).getConsumerTime());
        return card;
    }

    private ArrayList<ConsumerRecord> createTestConsumerRecords(String[] testdata) {
        ArrayList<ConsumerRecord> testConsumerRecords = new ArrayList<ConsumerRecord>();
        ConsumerRecord record = null;
        for(int i = 0; i < testdata.length; i++) {
            record = parseConsumerRecord(testdata[i]);
            testConsumerRecords.add(record);
        }
        return testConsumerRecords;
    }

    private ConsumerRecord parseConsumerRecord(String data) {
        String[] properties = data.trim().replaceAll("[{}]", "").split(";");
        HashMap<String, String> propertyHashMap = new HashMap<String, String>();
        for(int i = 0; i < properties.length; i++) {
            String[] itemEntry = properties[i].trim().split("=");
            propertyHashMap.put(itemEntry[0], itemEntry[1]);
        }

        ConsumerRecord record = null;
        String consumptionType = propertyHashMap.get(KEY_CONSUMPTION_TYPE);
        switch (ConsumerType.valueOf(consumptionType)) {
            case COUNT:
                record = new CountConsumerRecord();
                record.setConsumption(propertyHashMap.get(KEY_CONSUMER_COUNT));
                record.setResidual(propertyHashMap.get(KEY_RESIDUAL_COUNT));
                break;
            case EWALLET:
                record = new EWalletConsumerRecord();
                record.setConsumption(propertyHashMap.get(KEY_CONSUMER_AMOUNT));
                record.setResidual(propertyHashMap.get(KEY_RESIDUAL_AMOUNT));
                break;
            default:
                throw new RuntimeException("Unknown consumption type!!!");
        }
        record.setBusNumber(propertyHashMap.get(KEY_BUS_NUMBER));
        record.setLineNumber(propertyHashMap.get(KEY_LINE_NUMBER));
        record.setCardID(propertyHashMap.get(KEY_CARD_ID));
        record.setConsumerTime(Utils.parseDate(propertyHashMap.get(KEY_CONSUMER_TIME)));
        return record;
    }

    private final String[] TEST_DATA_1 =
        {
            "{lineNumber=102; busNumber=031154; cardID=000110808691; consumerTime=2013-1-1 19:23:59; consumerCount=2; residualCount=28;   type=COUNT}",
            "{lineNumber=102; busNumber=031162; cardID=000110808691; consumerTime=2013-1-1 15:39:33; consumerCount=2; residualCount=30;   type=COUNT}" ,
            "{lineNumber=188; busNumber=031186; cardID=000110808691; consumerTime=2012-12-21 18:35:37; consumerCount=2; residualCount=6;  type=COUNT}",
            "{lineNumber=102; busNumber=031149; cardID=000110808691; consumerTime=2012-12-15 20:40:37; consumerCount=2; residualCount=8;  type=COUNT}",
            "{lineNumber=102; busNumber=031153; cardID=000110808691; consumerTime=2012-12-15 17:21:55; consumerCount=0; residualCount=10; type=COUNT}",
            "{lineNumber=185; busNumber=031144; cardID=000110808691; consumerTime=2012-12-15 17:08:52; consumerCount=2; residualCount=10; type=COUNT}",
            "{lineNumber=188; busNumber=031190; cardID=000110808691; consumerTime=2012-12-15 14:07:10; consumerCount=0; residualCount=12; type=COUNT}",
            "{lineNumber=185; busNumber=031195; cardID=000110808691; consumerTime=2012-12-15 12:14:36; consumerCount=2; residualCount=12; type=COUNT}",
            "{lineNumber=102; busNumber=031164; cardID=000110808691; consumerTime=2012-12-9 18:42:06; consumerCount=2; residualCount=14;  type=COUNT}",
            "{lineNumber=102; busNumber=031158; cardID=000110808691; consumerTime=2012-12-2 21:08:27; consumerCount=0; residualCount=16;  type=COUNT}",
            "{lineNumber=112; busNumber=039141; cardID=000110808691; consumerTime=2012-12-2 20:54:23; consumerCount=2; residualCount=16;  type=COUNT}",
            "{lineNumber=102; busNumber=031156; cardID=000110808691; consumerTime=2012-12-2 16:33:48; consumerCount=2; residualCount=18;  type=COUNT}",
            "{lineNumber=185; busNumber=031228; cardID=000110808691; consumerTime=2012-12-2 10:44:08; consumerAmount=1.80; residualAmount=46.40; type=EWALLET}",
            "{lineNumber=185; busNumber=031228; cardID=000110808691; consumerTime=2012-12-2 10:44:06; consumerAmount=1.80; residualAmount=48.20; type=EWALLET}",
            "{lineNumber=102; busNumber=031153; cardID=000110808691; consumerTime=2012-10-14 19:10:33; consumerAmount=1.80; residualAmount=3.20; type=EWALLET}",
            "{lineNumber=50; busNumber=049182; cardID=000110808691; consumerTime=2012-10-3 10:41:41; consumerAmount=1.80; residualAmount=5; type=EWALLET}",
            "{lineNumber=50; busNumber=049543; cardID=000110808691; consumerTime=2012-9-29 9:13:45; consumerAmount=1.80; residualAmount=6.80; type=EWALLET}",
            "{lineNumber=188; busNumber=031181; cardID=000110808691; consumerTime=2012-9-23 11:51:44; consumerAmount=1.80; residualAmount=12.20; type=EWALLET}",
            "{lineNumber=185; busNumber=034006; cardID=000110808691; consumerTime=2012-8-27 10:31:19; consumerAmount=1.80; residualAmount=14; type=EWALLET}",
            "{lineNumber=102; busNumber=031158; cardID=000110808691; consumerTime=2012-8-26 16:47:37; consumerAmount=1.80; residualAmount=23; type=EWALLET}",
        };
    private final String[] TEST_DATA_2 =
        {
            "{lineNumber=102; busNumber=031154; cardID=000110802436; consumerTime=2013-1-1 19:23:59; consumerCount=2; residualCount=28;   type=COUNT}",
            "{lineNumber=102; busNumber=031162; cardID=000110802436; consumerTime=2013-1-1 15:39:33; consumerCount=2; residualCount=30;   type=COUNT}" ,
            "{lineNumber=188; busNumber=031186; cardID=000110802436; consumerTime=2012-12-21 18:35:37; consumerCount=2; residualCount=6;  type=COUNT}",
            "{lineNumber=102; busNumber=031149; cardID=000110802436; consumerTime=2012-12-15 20:40:37; consumerCount=2; residualCount=8;  type=COUNT}",
            "{lineNumber=102; busNumber=031153; cardID=000110802436; consumerTime=2012-12-15 17:21:55; consumerCount=0; residualCount=10; type=COUNT}",
            "{lineNumber=185; busNumber=031144; cardID=000110802436; consumerTime=2012-12-15 17:08:52; consumerCount=2; residualCount=10; type=COUNT}",
            "{lineNumber=188; busNumber=031190; cardID=000110802436; consumerTime=2012-12-15 14:07:10; consumerCount=0; residualCount=12; type=COUNT}",
            "{lineNumber=185; busNumber=031195; cardID=000110802436; consumerTime=2012-12-15 12:14:36; consumerCount=2; residualCount=12; type=COUNT}",
            "{lineNumber=102; busNumber=031164; cardID=000110802436; consumerTime=2012-12-9 18:42:06; consumerCount=2; residualCount=14;  type=COUNT}",
            "{lineNumber=102; busNumber=031158; cardID=000110802436; consumerTime=2012-12-2 21:08:27; consumerCount=0; residualCount=16;  type=COUNT}",
            "{lineNumber=112; busNumber=039141; cardID=000110802436; consumerTime=2012-12-2 20:54:23; consumerCount=2; residualCount=16;  type=COUNT}",
            "{lineNumber=102; busNumber=031156; cardID=000110802436; consumerTime=2012-12-2 16:33:48; consumerCount=2; residualCount=18;  type=COUNT}",
            "{lineNumber=185; busNumber=031228; cardID=000110802436; consumerTime=2012-12-2 10:44:08; consumerAmount=1.80; residualAmount=46.40; type=EWALLET}",
            "{lineNumber=185; busNumber=031228; cardID=000110802436; consumerTime=2012-12-2 10:44:06; consumerAmount=1.80; residualAmount=48.20; type=EWALLET}",
            "{lineNumber=102; busNumber=031153; cardID=000110802436; consumerTime=2012-10-14 19:10:33; consumerAmount=1.80; residualAmount=3.20; type=EWALLET}",
            "{lineNumber=50; busNumber=049182; cardID=000110802436; consumerTime=2012-10-3 10:41:41; consumerAmount=1.80; residualAmount=5; type=EWALLET}",
            "{lineNumber=50; busNumber=049543; cardID=000110802436; consumerTime=2012-9-29 9:13:45; consumerAmount=1.80; residualAmount=6.80; type=EWALLET}",
            "{lineNumber=188; busNumber=031181; cardID=000110802436; consumerTime=2012-9-23 11:51:44; consumerAmount=1.80; residualAmount=12.20; type=EWALLET}",
            "{lineNumber=185; busNumber=034006; cardID=000110802436; consumerTime=2012-8-27 10:31:19; consumerAmount=1.80; residualAmount=14; type=EWALLET}",
            "{lineNumber=102; busNumber=031158; cardID=000110802436; consumerTime=2012-8-26 16:47:37; consumerAmount=1.80; residualAmount=23; type=EWALLET}",
        };
    private final String[] TEST_DATA_3 =
        {
            "{lineNumber=102; busNumber=031154; cardID=000110802455; consumerTime=2013-1-1 19:23:59; consumerCount=2; residualCount=28;   type=COUNT}",
            "{lineNumber=102; busNumber=031162; cardID=000110802455; consumerTime=2013-1-1 15:39:33; consumerCount=2; residualCount=30;   type=COUNT}" ,
            "{lineNumber=188; busNumber=031186; cardID=000110802455; consumerTime=2012-12-21 18:35:37; consumerCount=2; residualCount=6;  type=COUNT}",
            "{lineNumber=102; busNumber=031149; cardID=000110802455; consumerTime=2012-12-15 20:40:37; consumerCount=2; residualCount=8;  type=COUNT}",
            "{lineNumber=102; busNumber=031153; cardID=000110802455; consumerTime=2012-12-15 17:21:55; consumerCount=0; residualCount=10; type=COUNT}",
            "{lineNumber=185; busNumber=031144; cardID=000110802455; consumerTime=2012-12-15 17:08:52; consumerCount=2; residualCount=10; type=COUNT}",
            "{lineNumber=188; busNumber=031190; cardID=000110802455; consumerTime=2012-12-15 14:07:10; consumerCount=0; residualCount=12; type=COUNT}",
            "{lineNumber=185; busNumber=031195; cardID=000110802455; consumerTime=2012-12-15 12:14:36; consumerCount=2; residualCount=12; type=COUNT}",
            "{lineNumber=102; busNumber=031164; cardID=000110802455; consumerTime=2012-12-9 18:42:06; consumerCount=2; residualCount=14;  type=COUNT}",
            "{lineNumber=102; busNumber=031158; cardID=000110802455; consumerTime=2012-12-2 21:08:27; consumerCount=0; residualCount=16;  type=COUNT}",
            "{lineNumber=112; busNumber=039141; cardID=000110802455; consumerTime=2012-12-2 20:54:23; consumerCount=2; residualCount=16;  type=COUNT}",
            "{lineNumber=102; busNumber=031156; cardID=000110802455; consumerTime=2012-12-2 16:33:48; consumerCount=2; residualCount=18;  type=COUNT}",
            "{lineNumber=185; busNumber=031228; cardID=000110802455; consumerTime=2012-12-2 10:44:08; consumerAmount=1.80; residualAmount=46.40; type=EWALLET}",
            "{lineNumber=185; busNumber=031228; cardID=000110802455; consumerTime=2012-12-2 10:44:06; consumerAmount=1.80; residualAmount=48.20; type=EWALLET}",
            "{lineNumber=102; busNumber=031153; cardID=000110802455; consumerTime=2012-10-14 19:10:33; consumerAmount=1.80; residualAmount=3.20; type=EWALLET}",
            "{lineNumber=50; busNumber=049182; cardID=000110802455; consumerTime=2012-10-3 10:41:41; consumerAmount=1.80; residualAmount=5; type=EWALLET}",
            "{lineNumber=50; busNumber=049543; cardID=000110802455; consumerTime=2012-9-29 9:13:45; consumerAmount=1.80; residualAmount=6.80; type=EWALLET}",
            "{lineNumber=188; busNumber=031181; cardID=000110802455; consumerTime=2012-9-23 11:51:44; consumerAmount=1.80; residualAmount=12.20; type=EWALLET}",
            "{lineNumber=185; busNumber=034006; cardID=000110802455; consumerTime=2012-8-27 10:31:19; consumerAmount=1.80; residualAmount=14; type=EWALLET}",
            "{lineNumber=102; busNumber=031158; cardID=000110802455; consumerTime=2012-8-26 16:47:37; consumerAmount=1.80; residualAmount=23; type=EWALLET}",
        };
}
