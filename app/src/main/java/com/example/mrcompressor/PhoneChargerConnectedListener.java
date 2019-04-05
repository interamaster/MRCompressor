package com.example.mrcompressor;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class PhoneChargerConnectedListener extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();



        if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {


         /*   context.startService(
                    new Intent(MyService.ACTION_POWER_CONNECTED));*/


         //si tiene SIM manda sms
           // SmsHelper.sendInfoSms(MainActivity.EXTRA_SMSCONFIGURADO,"OJO MRCompresor de :"+MainActivity.EXTRA_RMNAME + " DESENCHUFADO!!");



            //O MEJOR INTENT A MAIN


            Intent dialogIntent = new Intent(context, MainActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            //y lo ponemos de extra en el intent:
            dialogIntent.putExtra("TEXTORECIBIDO","INTENTPOWEROFF");
            context.startActivity(dialogIntent);






        } else if (Intent.ACTION_POWER_CONNECTED.equals(action)) {

           // SmsHelper.sendInfoSms(MainActivity.EXTRA_SMSCONFIGURADO,"OJO MRCompresor de :"+MainActivity.EXTRA_RMNAME+" VUELTO A  ENCHUFAR!!");

          /* context.startService(
                    new Intent(MyService.ACTION_POWER_DISCONNECTED));*/

            //O MEJOR INTENT A MAIN


            Intent dialogIntent = new Intent(context, MainActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            //y lo ponemos de extra en el intent:
            dialogIntent.putExtra("TEXTORECIBIDO","INTENTPOWERON");
            context.startActivity(dialogIntent);



        }
    }



}