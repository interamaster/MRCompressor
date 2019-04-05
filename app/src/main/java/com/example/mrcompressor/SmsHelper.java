package com.example.mrcompressor;


import android.telephony.SmsManager;


public class SmsHelper {

    /**
     * Constants helper class
     */


    public static final String SMS_CONDITION = "Phchksms";
    public static final String SMS_CONDITION2 = "phchksms";
    public static final String SMS_CONDITION3 = "PHSETTRIGGER";
    public static final String INTENTPOWEROFF = "INTENTPOWEROFF";
    public static final String INTENTPOWERON = "INTENTPOWERON";





    //mio para guradr los valores de app to get noti1,noti2,noti3,noti4,trigger,avlor de vibracion,SRN,Hospi name...!!son public y accesibles desde toda la APK


    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String PREF_SRN = "srn";//sera el user name
    public static final String PREF_HOSPNAME = "hospname";
    public static final String PREF_EMAIL = "email";//sera el user name
    public static final String PREF_VALUEVIBRATENOW = "valuevibrationnow";
    public static final String PREF_VALUETRIGGERVIBRATE = "valuetriggervibtrateset";
    public static final String PREF_NAMEAPPTOREADNOTIFICACTIONS = "nameapptoreadnotif";
    public static final String PREF_BOOL_ALARMADEFALLO ="false";
    public static final String PREF_RADIOBUTTONVALUEAPPTOREADNOTIS="radiobuttonapptoreadnotis";

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return android.util.Patterns.PHONE.matcher(phoneNumber).matches();
    }





    public static void sendInfoSms(String number, String smsBody) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, smsBody, null, null);
    }
}
