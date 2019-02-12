package com.example.mrcompressor;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipSession;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
     * A broadcast receiver who listens for incoming SMS
     */
    public class SmsBroadcastReceiver extends BroadcastReceiver {

        private static final String TAG = "SmsBroadcastReceiver";

        /*
        //estas var y el listener solo lo usa en medium ..
        https://android.jlelse.eu/detecting-sending-sms-on-android-8a154562597f


        no el github: https://github.com/JoaquimLey/sms-parsing/blob/master/app/src/main/java/com/joaquimley/smsparsing/SmsBroadcastReceiver.java


           private final String serviceProviderNumber;
         private final String serviceProviderSmsCondition;

         private SipSession.Listener listener;

    public SmsBroadcastReceiver(String serviceProviderNumber, String serviceProviderSmsCondition) {
        this.serviceProviderNumber = serviceProviderNumber;
        this.serviceProviderSmsCondition = serviceProviderSmsCondition;
    }

        */

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
                String smsSender = "";
                String smsBody = "";

                /*
                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    smsBody += smsMessage.getMessageBody();
                }*/


                /////////////////////////////////
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                        smsSender = smsMessage.getDisplayOriginatingAddress();
                        smsBody += smsMessage.getMessageBody();
                    }
                } else {
                    Bundle smsBundle = intent.getExtras();
                    if (smsBundle != null) {
                        Object[] pdus = (Object[]) smsBundle.get("pdus");
                        if (pdus == null) {
                            // Display some error to the user
                            Log.e(TAG, "SmsBundle had no pdus key");
                            return;
                        }
                        SmsMessage[] messages = new SmsMessage[pdus.length];
                        for (int i = 0; i < messages.length; i++) {
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                            smsBody += messages[i].getMessageBody();
                        }
                        smsSender = messages[0].getOriginatingAddress();
                    }
                }




                /////////////////////////////////////////////



                if ((smsBody.startsWith(SmsHelper.SMS_CONDITION)) || (smsBody.startsWith(SmsHelper.SMS_CONDITION2)) ) {
                    Log.d(TAG, "Sms with condition detected");
                    Toast.makeText(context, "BroadcastReceiver caught conditional SMS: " + smsBody, Toast.LENGTH_LONG).show();

                    //solo para coger valores de mainactivity!!

                    SmsHelper.sendInfoSms(smsSender,MainActivity.EXTRA_TIME);
                }


                Log.d(TAG, "SMS detected: From " + smsSender + " With text " + smsBody);
            }
        }
}
