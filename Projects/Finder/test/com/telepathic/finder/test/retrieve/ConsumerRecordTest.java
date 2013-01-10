package com.telepathic.finder.test.retrieve;

import java.text.ParseException;

import com.telepathic.finder.sdk.ConsumerRecord;
import com.telepathic.finder.sdk.ConsumerRecord.ConsumerType;
import com.telepathic.finder.util.Utils;

import android.test.AndroidTestCase;

public class ConsumerRecordTest extends AndroidTestCase {

    /**
     * x.equals(x) should return true
     */
    public void test_reflexive() throws ParseException {
        ConsumerRecord xRecord = new ConsumerRecord();
        xRecord.setLineNumber("102");
        xRecord.setBusNumber("031162");
        xRecord.setCardID("000110808691");
        xRecord.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        xRecord.setConsumerCount("2");
        xRecord.setResidualCount("30");
        xRecord.setResidualAmount("46.40");
        xRecord.setConsumerType(ConsumerType.COUNT);

        assertTrue(xRecord.equals(xRecord));
    }

    /**
     * x.equals(y) must return true if and only if y.equals(x) returns true
     */
    public void test_symmetric() throws ParseException {
        ConsumerRecord xRecord = new ConsumerRecord();
        xRecord.setLineNumber("102");
        xRecord.setBusNumber("031162");
        xRecord.setCardID("000110808691");
        xRecord.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        xRecord.setConsumerCount("2");
        xRecord.setResidualCount("30");
        xRecord.setResidualAmount("46.40");
        xRecord.setConsumerType(ConsumerType.COUNT);

        ConsumerRecord yRecord = new ConsumerRecord();
        yRecord.setLineNumber("102");
        yRecord.setBusNumber("031162");
        yRecord.setCardID("000110808691");
        yRecord.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        yRecord.setConsumerCount("2");
        yRecord.setResidualCount("30");
        yRecord.setResidualAmount("46.40");
        yRecord.setConsumerType(ConsumerType.COUNT);

        if (yRecord.equals(xRecord)) {
            assertTrue(xRecord.equals(yRecord));
        }
    }

    /**
     *  if x.equals(y) returns true and y.equals(z) returns true, then x.equals(z) must return true.
     */
    public void test_transitive() throws ParseException {
        ConsumerRecord xRecord = new ConsumerRecord();
        xRecord.setLineNumber("102");
        xRecord.setBusNumber("031162");
        xRecord.setCardID("000110808691");
        xRecord.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        xRecord.setConsumerCount("2");
        xRecord.setResidualCount("30");
        xRecord.setResidualAmount("46.40");
        xRecord.setConsumerType(ConsumerType.COUNT);

        ConsumerRecord yRecord = new ConsumerRecord();
        yRecord.setLineNumber("102");
        yRecord.setBusNumber("031162");
        yRecord.setCardID("000110808691");
        yRecord.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        yRecord.setConsumerCount("2");
        yRecord.setResidualCount("30");
        yRecord.setResidualAmount("46.40");
        yRecord.setConsumerType(ConsumerType.COUNT);

        ConsumerRecord zRecord = new ConsumerRecord();
        zRecord.setLineNumber("102");
        zRecord.setBusNumber("031162");
        zRecord.setCardID("000110808691");
        zRecord.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        zRecord.setConsumerCount("2");
        zRecord.setResidualCount("30");
        zRecord.setResidualAmount("46.40");
        zRecord.setConsumerType(ConsumerType.COUNT);

        if(xRecord.equals(yRecord) && yRecord.equals(zRecord)) {
            assertEquals(xRecord, zRecord);
        }
    }

    /**
     * multiple invocations of x.equals(y)  consistently return  true or consistently return false
     */
    public void test_consistent() throws ParseException {
        ConsumerRecord xRecord = new ConsumerRecord();
        xRecord.setLineNumber("102");
        xRecord.setBusNumber("031162");
        xRecord.setCardID("000110808691");
        xRecord.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        xRecord.setConsumerCount("2");
        xRecord.setResidualCount("30");
        xRecord.setResidualAmount("46.40");
        xRecord.setConsumerType(ConsumerType.COUNT);

        ConsumerRecord yRecord = new ConsumerRecord();
        yRecord.setLineNumber("102");
        yRecord.setBusNumber("031162");
        yRecord.setCardID("000110808691");
        yRecord.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        yRecord.setConsumerCount("2");
        yRecord.setResidualCount("30");
        yRecord.setResidualAmount("46.40");
        yRecord.setConsumerType(ConsumerType.COUNT);

        if (xRecord.equals(yRecord)) {
            for(int i = 0; i < 10000; i++) {
                assertTrue(xRecord.equals(yRecord));
            }
        }

        ConsumerRecord zRecord = new ConsumerRecord();
        zRecord.setLineNumber("103");
        zRecord.setBusNumber("031162");
        zRecord.setCardID("000110808691");
        zRecord.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        zRecord.setConsumerCount("2");
        zRecord.setResidualCount("30");
        zRecord.setResidualAmount("46.40");
        zRecord.setConsumerType(ConsumerType.COUNT);

        if (!xRecord.equals(zRecord)) {
            for(int i = 0; i < 10000; i++) {
                assertFalse(xRecord.equals(zRecord));
            }
        }
    }

    public void test_null() {
        ConsumerRecord record = new ConsumerRecord();
        assertFalse(record.equals(null));
    }

    public void test_hashcode1() throws ParseException {
        ConsumerRecord xRecord = new ConsumerRecord();
        xRecord.setLineNumber("102");
        xRecord.setBusNumber("031162");
        xRecord.setCardID("000110808691");
        xRecord.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        xRecord.setConsumerCount("2");
        xRecord.setResidualCount("30");
        xRecord.setResidualAmount("46.40");
        xRecord.setConsumerType(ConsumerType.COUNT);

        assertEquals(xRecord.hashCode(), xRecord.hashCode());

        ConsumerRecord yRecord = new ConsumerRecord();
        yRecord.setLineNumber("102");
        yRecord.setBusNumber("031162");
        yRecord.setCardID("000110808691");
        yRecord.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        yRecord.setConsumerCount("2");
        yRecord.setResidualCount("30");
        yRecord.setResidualAmount("46.40");
        yRecord.setConsumerType(ConsumerType.COUNT);

        assertEquals(yRecord.hashCode(), yRecord.hashCode());
        assertEquals(xRecord.hashCode(), yRecord.hashCode());

        ConsumerRecord zRecord = new ConsumerRecord();
        zRecord.setLineNumber("103");
        zRecord.setBusNumber("031162");
        zRecord.setCardID("000110808691");
        zRecord.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        zRecord.setConsumerCount("2");
        zRecord.setResidualCount("30");
        zRecord.setResidualAmount("46.40");
        zRecord.setConsumerType(ConsumerType.COUNT);
        assertEquals(zRecord.hashCode(), zRecord.hashCode());

        assertEquals(xRecord.hashCode(), yRecord.hashCode());
        assertTrue(xRecord.hashCode() != zRecord.hashCode());

    }

    public void test_hashcode2() throws ParseException {
        ConsumerRecord xRecord = new ConsumerRecord();
        xRecord.setLineNumber("185");
        xRecord.setBusNumber("031228");
        xRecord.setCardID("000110808691");
        xRecord.setConsumerTime(Utils.parseDate("2012-12-2 10:44:08"));
        xRecord.setConsumerAmount("1.80");
        xRecord.setResidualCount("20");
        xRecord.setResidualAmount("46.40");
        xRecord.setConsumerType(ConsumerType.ELECTRONIC_WALLET);
        assertEquals(xRecord.hashCode(), xRecord.hashCode());

        ConsumerRecord yRecord = new ConsumerRecord();
        yRecord.setLineNumber("185");
        yRecord.setBusNumber("031228");
        yRecord.setCardID("000110808691");
        yRecord.setConsumerTime(Utils.parseDate("2012-12-2 10:44:08"));
        yRecord.setConsumerAmount("1.80");
        yRecord.setResidualCount("20");
        yRecord.setResidualAmount("46.40");
        yRecord.setConsumerType(ConsumerType.ELECTRONIC_WALLET);
        assertEquals(yRecord.hashCode(), yRecord.hashCode());

        assertEquals(xRecord.hashCode(), yRecord.hashCode());
    }

}
