package com.example.mrcompressor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PhoneChargerConnectedListener extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {


         /*   context.startService(
                    new Intent(MyService.ACTION_POWER_CONNECTED));*/

            SmsHelper.sendInfoSms(MainActivity.EXTRA_SMSCONFIGURADO,"OJO MRCompresor de :"+MainActivity.EXTRA_RMNAME + " DESENCHUFADO!!");


        } else if (Intent.ACTION_POWER_CONNECTED.equals(action)) {

            SmsHelper.sendInfoSms(MainActivity.EXTRA_SMSCONFIGURADO,"OJO MRCompresor de :"+MainActivity.EXTRA_RMNAME+" VUELTO A  ENCHUFAR!!");

          /* context.startService(
                    new Intent(MyService.ACTION_POWER_DISCONNECTED));*/


        }
    }
}