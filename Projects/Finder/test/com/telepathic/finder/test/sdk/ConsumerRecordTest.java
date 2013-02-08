package com.telepathic.finder.test.sdk;

import android.test.AndroidTestCase;

import com.telepathic.finder.sdk.traffic.entity.ConsumerRecord;
import com.telepathic.finder.sdk.traffic.entity.CountConsumerRecord;
import com.telepathic.finder.sdk.traffic.entity.EWalletConsumerRecord;
import com.telepathic.finder.util.Utils;

public class ConsumerRecordTest extends AndroidTestCase {

    /**
     * x.equals(x) should return true
     */
    public void test_reflexive_1() {
        ConsumerRecord record = new CountConsumerRecord();
        record.setLineNumber("102");
        record.setBusNumber("031162");
        record.setCardID("000110808691");
        record.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record.setConsumption("2");
        record.setResidual("30");
        assertEquals(record, record);
    }
    
    /**
     * x.equals(x) should return true
     */
    public void test_reflexive_2() {
        ConsumerRecord record = new EWalletConsumerRecord();
        record.setLineNumber("102");
        record.setBusNumber("031162");
        record.setCardID("000110808691");
        record.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record.setConsumption("1.8");
        record.setResidual("46.40");
        assertEquals(record, record);
    }

    /**
     * x.equals(y) must return true if and only if y.equals(x) returns true
     */
    public void test_symmetric_1() {
        ConsumerRecord record1 = new CountConsumerRecord();
        ConsumerRecord record2 = new CountConsumerRecord();
        record1.setLineNumber("102");
        record1.setBusNumber("031162");
        record1.setCardID("000110808691");
        record1.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record1.setConsumption("2");
        record1.setResidual("30");

        record2.setLineNumber("102");
        record2.setBusNumber("031162");
        record2.setCardID("000110808691");
        record2.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record2.setConsumption("2");
        record2.setResidual("30");
        assertEquals(record1, record2);
        assertEquals(record2, record1);
    }
    
    /**
     * x.equals(y) must return true if and only if y.equals(x) returns true
     */
    public void test_symmetric_2() {
        ConsumerRecord record1 = new EWalletConsumerRecord();
        ConsumerRecord record2 = new EWalletConsumerRecord();
        record1.setLineNumber("102");
        record1.setBusNumber("031162");
        record1.setCardID("000110808691");
        record1.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record1.setConsumption("1.80");
        record1.setResidual("46.40");

        record2.setLineNumber("102");
        record2.setBusNumber("031162");
        record2.setCardID("000110808691");
        record2.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record2.setConsumption("1.80");
        record2.setResidual("46.40");
        assertEquals(record1, record2);
        assertEquals(record2, record1);
    }

    /**
     *  if x.equals(y) returns true and y.equals(z) returns true, then x.equals(z) must return true.
     */
    public void test_transitive_1() {
        ConsumerRecord record1 = new CountConsumerRecord();
        ConsumerRecord record2 = new CountConsumerRecord();
        ConsumerRecord record3 = new CountConsumerRecord();
        
        record1.setLineNumber("102");
        record1.setBusNumber("031162");
        record1.setCardID("000110808691");
        record1.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record1.setConsumption("2");
        record1.setResidual("30");
        
        record2.setLineNumber("102");
        record2.setBusNumber("031162");
        record2.setCardID("000110808691");
        record2.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record2.setConsumption("2");
        record2.setResidual("30");
        
        record3.setLineNumber("102");
        record3.setBusNumber("031162");
        record3.setCardID("000110808691");
        record3.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record3.setConsumption("2");
        record3.setResidual("30");

        assertEquals(record1, record2);
        assertEquals(record2, record3);
        assertEquals(record1, record3);

    }
    
    /**
     *  if x.equals(y) returns true and y.equals(z) returns true, then x.equals(z) must return true.
     */
    public void test_transitive_2() {
        ConsumerRecord record1 = new EWalletConsumerRecord();
        ConsumerRecord record2 = new EWalletConsumerRecord();
        ConsumerRecord record3 = new EWalletConsumerRecord();
        
        record1.setLineNumber("102");
        record1.setBusNumber("031162");
        record1.setCardID("000110808691");
        record1.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record1.setConsumption("1.80");
        record1.setResidual("46.40");
        
        record2.setLineNumber("102");
        record2.setBusNumber("031162");
        record2.setCardID("000110808691");
        record2.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record2.setConsumption("1.80");
        record2.setResidual("46.40");
        
        record3.setLineNumber("102");
        record3.setBusNumber("031162");
        record3.setCardID("000110808691");
        record3.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record3.setConsumption("1.80");
        record3.setResidual("46.40");

        assertEquals(record1, record2);
        assertEquals(record2, record3);
        assertEquals(record1, record3);

    }

    /**
     * multiple invocations of x.equals(y)  consistently return  true or consistently return false
     */
    public void test_consistent_1() {
        ConsumerRecord record1 = new CountConsumerRecord();
        ConsumerRecord record2 = new CountConsumerRecord();
        ConsumerRecord record3 = new CountConsumerRecord();
        
        record1.setLineNumber("102");
        record1.setBusNumber("031162");
        record1.setCardID("000110808691");
        record1.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record1.setConsumption("2");
        record1.setResidual("30");
        
        record2.setLineNumber("102");
        record2.setBusNumber("031162");
        record2.setCardID("000110808691");
        record2.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record2.setConsumption("2");
        record2.setResidual("30");
        
        record3.setLineNumber("103");
        record3.setBusNumber("031162");
        record3.setCardID("000110808691");
        record3.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record3.setConsumption("2");
		record3.setResidual("30");

		for (int i = 0; i < 10000; i++) {
			assertTrue(record1.equals(record2));
			assertFalse(record1.equals(record3));
		}
    }
    
    /**
     * multiple invocations of x.equals(y)  consistently return  true or consistently return false
     */
    public void test_consistent_2() {
        ConsumerRecord record1 = new EWalletConsumerRecord();
        ConsumerRecord record2 = new EWalletConsumerRecord();
        ConsumerRecord record3 = new EWalletConsumerRecord();
        
        record1.setLineNumber("102");
        record1.setBusNumber("031162");
        record1.setCardID("000110808691");
        record1.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record1.setConsumption("1.80");
        record1.setResidual("46.40");
        
        record2.setLineNumber("102");
        record2.setBusNumber("031162");
        record2.setCardID("000110808691");
        record2.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record2.setConsumption("1.80");
        record2.setResidual("46.40");
        
        record3.setLineNumber("103");
        record3.setBusNumber("031162");
        record3.setCardID("000110808691");
        record3.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record3.setConsumption("1.80");
		record3.setResidual("46.40");

		for (int i = 0; i < 10000; i++) {
			assertTrue(record1.equals(record2));
			assertFalse(record1.equals(record3));
		}
    }
    

    public void test_null() {
        ConsumerRecord record1 = new CountConsumerRecord();
        ConsumerRecord record2 = new EWalletConsumerRecord();
        assertFalse(record1.equals(null));
        assertFalse(record2.equals(null));
    }

    public void test_hashcode_1() {
        ConsumerRecord record1 = new CountConsumerRecord();
        ConsumerRecord record2 = new CountConsumerRecord();
        ConsumerRecord record3 = new CountConsumerRecord();
        
        record1.setLineNumber("102");
        record1.setBusNumber("031162");
        record1.setCardID("000110808691");
        record1.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record1.setConsumption("2");
        record1.setResidual("30");
        
        record2.setLineNumber("102");
        record2.setBusNumber("031162");
        record2.setCardID("000110808691");
        record2.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record2.setConsumption("2");
        record2.setResidual("30");
        
        record3.setLineNumber("103");
        record3.setBusNumber("031162");
        record3.setCardID("000110808691");
        record3.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record3.setConsumption("2");
        record3.setResidual("30");
        

        assertEquals(record1.hashCode(), record1.hashCode());
        assertEquals(record2.hashCode(), record2.hashCode());
        assertEquals(record3.hashCode(), record3.hashCode());
        
        assertEquals(record1.hashCode(), record2.hashCode());
        assertTrue(record1.hashCode() != record3.hashCode());
    }
    
    public void test_hashcode_2() {
        ConsumerRecord record1 = new EWalletConsumerRecord();
        ConsumerRecord record2 = new EWalletConsumerRecord();
        ConsumerRecord record3 = new EWalletConsumerRecord();
        
        record1.setLineNumber("102");
        record1.setBusNumber("031162");
        record1.setCardID("000110808691");
        record1.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record1.setConsumption("1.80");
        record1.setResidual("46.40");
        
        record2.setLineNumber("102");
        record2.setBusNumber("031162");
        record2.setCardID("000110808691");
        record2.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record2.setConsumption("1.80");
        record2.setResidual("46.40");
        
        record3.setLineNumber("103");
        record3.setBusNumber("031162");
        record3.setCardID("000110808691");
        record3.setConsumerTime(Utils.parseDate("2013-1-1 15:39:33"));
        record3.setConsumption("1.80");
        record3.setResidual("46.40");
        

        assertEquals(record1.hashCode(), record1.hashCode());
        assertEquals(record2.hashCode(), record2.hashCode());
        assertEquals(record3.hashCode(), record3.hashCode());
        
        assertEquals(record1.hashCode(), record2.hashCode());
        assertTrue(record1.hashCode() != record3.hashCode());
    }
}
