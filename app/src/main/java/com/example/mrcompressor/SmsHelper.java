package com.example.mrcompressor;

import android.telephony.SmsManager;

public class SmsHelper {

    /**
     * Constants helper class
     */


    public static final String SMS_CONDITION = "Phchksms";
    public static final String SMS_CONDITION2 = "phchksms";

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return android.util.Patterns.PHONE.matcher(phoneNumber).matches();
    }

    public static void sendInfoSms(String number, String smsBody) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, smsBody, null, null);
    }
}
