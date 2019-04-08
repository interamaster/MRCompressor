package com.example.mrcompressor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by esq00931 on 31/10/2016.
 * PARA EL AUTOBOOT!!!
 * HAYQ UE AÑADIRLO EN MANIFEST TAMBIEN Y QUE FILTER POR EL INTENT android.intent.action.BOOT_COMPLETED
 */

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, MainActivity.class);
           //  pushIntent.putExtra(LockService.EXTRA_MESSAGE,"REBOOT");
              pushIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


            //y lo ponemos de extra en el intent:
            pushIntent.putExtra("TEXTORECIBIDO","REBOOT");
            context.startActivity(pushIntent);


        }
    }
}