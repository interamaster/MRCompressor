package com.example.mrcompressor;

import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.Display;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by joseramondelgado on 19/08/18.
 */

public class NOTIFSService extends NotificationListenerService {


        //para saber si ya esta el sevivio running:
        private boolean ServiceYaRunning;


    //PARA EL LOGGING

    private String TAG = this.getClass().getSimpleName();

    //PARA EL COINTEXT EN SCREEN ON


    private Context mContext;




    //para el valor de la notif a leer


    private SharedPreferences mPrefs;

    private String ApptoreadNotif;
    private String pacakgenamenotif;


    //para evitar duplicados

    long LastWhastsppsbnTime;
    String lastmesagetext="ultimo";
    String Actualmesagetext="actual";




    public void onCreate() {
        super.onCreate();


        //mio:
        ServiceYaRunning=false;

     //   Log.i("INICIO SERVIVIO NOTIFS:", "OK");


        mContext = this;



        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);


        //recupermaos packagename de app a readnumero de usos..por defecto es 0
        ApptoreadNotif=mPrefs.getString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,"hangout");

        Log.d("apks to star reading: ",ApptoreadNotif);






    }

    @Override
    public void onDestroy() {
        super.onDestroy();

      //  Log.i("FINAL SERVIVIO NOTIFS:", "OK");



    }




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////METODO QUE SE EJECUTA CADA VEZ QUE SE RECIBE NOTIFICATION////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

//   /*

        //forma de ver los key del sbn(notific)

            Log.i(TAG, "ID:" + sbn.getId());
            Log.i(TAG, "Posted by:" + sbn.getPackageName());
            Log.i(TAG, "tickerText:" + sbn.getNotification().tickerText);

            for (String key : sbn.getNotification().extras.keySet()) {
                Log.i(TAG, key + "=" + sbn.getNotification().extras.get(key));
            }

/*
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: ID:0
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: Posted by:com.google.android.talk
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: tickerText:jose ramon delgado: Kfkdkd
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.title=jose ramon delgado
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.hiddenConversationTitle=null
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.subText=null
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.car.EXTENSIONS=Bundle[mParcelledData.dataSize=1736]
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.template=android.app.Notification$MessagingStyle
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.showChronometer=false
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.icon=2130839035
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.text=Kfkdkd
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.progress=0
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.progressMax=0
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.selfDisplayName=You
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.appInfo=ApplicationInfo{47534ed com.google.android.talk}
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.messages=[Landroid.os.Parcelable;@855ef22
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.showWhen=true
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.largeIcon=android.graphics.Bitmap@1bf5db3
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.messagingStyleUser=Bundle[mParcelledData.dataSize=148]
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.infoText=null
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.wearable.EXTENSIONS=Bundle[mParcelledData.dataSize=1680]
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.progressIndeterminate=false
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.remoteInputHistory=null
2019-04-05 11:34:24.710 9248-9248/com.example.mrcompressor I/NOTIFSService: android.isGroupConversation=false



 */



//*/



            if (sbn.isOngoing()) {
                return;
            }





         //   mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

            pacakgenamenotif = sbn.getPackageName();


            if (!isNotif4packnamehabilitada(pacakgenamenotif,false)) {


                //no es una de las nuetaras de hangout o otros o sms


                return;
            }



            //ponemos el logging

            LogInfodelSBN(sbn);





            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////PARA PASARLO A MAINACTIVITY//////////////////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


          if (isNotif4packnamehabilitada(pacakgenamenotif,true) && isActivityRunning(MainActivity.class)){//TODO no se si chequear o directamnete lanzarla..





                Intent dialogIntent = new Intent(this, MainActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);


                CharSequence notificationText = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT);

                //y lo ponemos de extra en el intent:
                dialogIntent.putExtra("TEXTORECIBIDO",notificationText);




                startActivity(dialogIntent);


            }
            }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
      //  Log.i(TAG,"********** onNOtificationRemoved");
       // Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());

    }




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////PARA SABER SI MI ACTIVITY ESTA ACTIVA//////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




    protected Boolean isActivityRunning(Class activityClass)


    {
        ActivityManager activityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
                return true;
        }

        return false;
    }



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////PARA SABER SI ES UNA NOTIFIC HABILOTADOA//////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private  boolean isNotif4packnamehabilitada(String packname, boolean anadirestadistica){


        if (mPrefs.getString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,"sms") .equals( packname)) {

           Log.i("INFO","era una notifi a leer!!");





            return true;
        }


        else return false;
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////PARA PONER EN LOG.I TODA LA INFO QUE SE VA A PSASR//////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void LogInfodelSBN(StatusBarNotification sbn ){
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////PARA ER EL LOGGING SOLO//////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        // Log.i(TAG,"**********  onNotificationPosted");
        // Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
/*
            Log.i(TAG, "SBN :" + sbn );

            Log.i(TAG, "ID:" + sbn.getId());
            Log.i(TAG, "Posted by:" + sbn.getPackageName());
            Log.i(TAG, "tickerText:" + sbn.getNotification().tickerText);

            for (String key : sbn.getNotification().extras.keySet()) {
                Log.i(TAG, key + "=" + sbn.getNotification().extras.get(key).toString());
            }

*/



        pacakgenamenotif = sbn.getPackageName();

        /*
        //PASO EL COLOR A MANO EN isNotif4packnamehabilitada

        int colordelanotif=sbn.getNotification().color;//esto da valores extraños ?¿?¿ ej:SBN notif color:-16746281

        colorNotif = String.format("#%06X", (0xFFFFFF & colordelanotif));
        Log.i(TAG, "color en hex:"+colorNotif);
        */

        String ticker = "";
      //  Log.i(TAG, "SBN notification extras :" + sbn.getNotification().extras);

        //esto da: para KIDSTIMER EN EL RELOJ!! CDA SEGUNDO
/*
            SBN :Bundle[{android.title=null,
             android.subText=null,
              android.showChronometer=false,
              android.icon=2131099752,
               android.text=REMAINIG TIME: 01:20:57,
                android.progress=0,
                android.progressMax=0,
                 android.appInfo=ApplicationInfo{c2e75d5 com.sfc.jrdv.kidstimer},
                  android.showWhen=true,
                   android.largeIcon=null,
                    android.infoText=null,
                    android.originatingUserId=0,
                     android.progressIndeterminate=false,
                      android.remoteInputHistory=null}]

   */

//ESTO DA EN UN WTASAPP
/*
            I/NEW: ----------
                    08-19 18:20:32.071 30873-30873/com.mio.jrdv.ambientnotifs I/NOTIFSService: SBN notification extras :
                    Bundle[{android.title=Gustavo Hijo: ​,
                    android.conversationTitle=Gustavo Hijo,
                    android.subText=null,
                     android.car.EXTENSIONS=Bundle[mParcelledData.dataSize=1076],
                      android.template=android.app.Notification$MessagingStyle,
                      android.showChronometer=false,
                      android.icon=2131231581,
                      android.text=😂😂,
                      android.progress=0,
                      android.progressMax=0,
                      android.selfDisplayName=Gustavo Hijo,
                       android.appInfo=ApplicationInfo{6b50372 com.whatsapp},
                       android.messages=[Landroid.os.Parcelable;@e4b63c3,
                        android.showWhen=true,
                         android.largeIcon=android.graphics.Bitmap@a511440,
                          android.infoText=null,
                          android.wearable.EXTENSIONS=Bundle[mParcelledData.dataSize=764],
                           android.progressIndeterminate=false,
                           android.remoteInputHistory=null}]





                    */


     //   Log.i(TAG, "SBN notification  :" + sbn.getNotification());

            /*

             //esto da: para KIDSTIMER EN EL RELOJ!! CDA SEGUNDO
             SBN notification  :Notification(pri=0 contentView=null vibrate=null sound=null defaults=0x0 flags=0xa color=0x00000000 vis=PRIVATE)

             */

            /*
            ESTO PARA WHATASAPP


             08-19 18:20:32.074 30873-30873/com.mio.jrdv.ambientnotifs I/NOTIFSService: SBN notification
            :Notification(channel=group_chat_defaults_2
            pri=0
            contentView=null
            vibrate=null
            sound=null
            defaults=0x0
            flags=0x8
            color=0xff075e54
            groupKey=group_key_messages
            sortKey=3
            actions=1
            number=1
            vis=PRIVATE s
            emFlags=0x0
            semPriority=0
            semMissedCount=0)
             */


        // if (sbn.getNotification().tickerText != null) {
        //  ticker = sbn.getNotification().tickerText.toString();
        //   }
        Bundle extras = sbn.getNotification().extras;
//        try {
//            //sbn.getNotification().contentIntent.send();
//        } catch (PendingIntent.CanceledException e) {
//            e.printStackTrace();
//        }

      //  Log.i("NEW", "----------");

            /*
            if (extras.get("android.title")!=null) {
                //this is the title of the notification
                //algunas veces da error!!!

                //Key android.title expected String but value was a android.text.SpannableString.  The default value <null> was returned.

                String title = extras.getString("android.title");
                Log.i("Title", title);

            }
            */


      //  Log.i(TAG, "SBN APPNAME:" + pacakgenamenotif);//SBN APPNAME:com.whatsapp

      //  Log.i(TAG, "SBN notif color:" +  colorNotif);


        CharSequence bigText = (CharSequence) extras.getCharSequence("android.title");
        if (bigText != null) {
            String title = bigText.toString();
          //  Log.i("Title", title);
        }


        CharSequence bigText2 = (CharSequence) extras.getCharSequence("android.subtext");
        if (bigText2 != null) {
            String SUBTETXT = bigText2.toString();
          //  Log.i("SUBTETXT", SUBTETXT.toString());
        }


        CharSequence bigText3 = (CharSequence) extras.getCharSequence("android.text");
        if (bigText3 != null) {
            //String TEXT = bigText3.toString();
            lastmesagetext = bigText3.toString();
           // Log.i("TEXT", lastmesagetext.toString());
        }



        CharSequence bigText4 = (CharSequence) extras.getCharSequence("android.bigText");
        if (bigText4 != null) {
            String TEXT = bigText4.toString();
          //  Log.i("BIGTEXT", TEXT.toString());
        }


        //this is a bitmap to be used instead of the small icon when showing the  notification


        Drawable appIcon = null;
        try {
            appIcon = getPackageManager().getApplicationIcon(pacakgenamenotif);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        Bitmap largeIcon = null;
        try {
            largeIcon = (Bitmap) sbn.getNotification().extras.getParcelable(Notification.EXTRA_LARGE_ICON);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Bitmap smallIcon = null;
        try {
            int idsmallicon = sbn.getNotification().extras.getInt(Notification.EXTRA_SMALL_ICON);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int id1 = extras.getInt("android.icon");


     //   Log.i("FINAL ", "----------");


            /*
            ESTO DA PARA UN WHATSAPP
             08-19 18:20:32.074 30873-30873/com.mio.jrdv.ambientnotifs I/NEW: ----------
                    08-19 18:20:32.074 30873-30873/com.mio.jrdv.ambientnotifs I/Title: Gustavo Hijo: ​
            08-19 18:20:32.074 30873-30873/com.mio.jrdv.ambientnotifs I/TEXT: 😂😂
            08-19 18:20:32.132 30873-30873/com.mio.jrdv.ambientnotifs I/ApplicationPackageManager: load=com.whatsapp, bg=96-96, dr=144-144, forDefault=false, density=0
            08-19 18:20:32.141 30873-30873/com.mio.jrdv.ambientnotifs I/ApplicationPackageManager: scaled rate=0.59999996, size=144, alpha=2, hold=0
            08-19 18:20:32.142 30873-30873/com.mio.jrdv.ambientnotifs I/ApplicationPackageManager: load=com.whatsapp-theme2, bg=96-96, dr=144-144, tarScale=0.59999996, relScale=0.41142857, mask=false


             */


    }


}
