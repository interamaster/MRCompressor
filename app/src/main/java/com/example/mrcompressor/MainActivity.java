package com.example.mrcompressor;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mrcompressor.helpers.NotificationServiceHelper;
import com.github.lzyzsd.circleprogress.CircleProgress;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //v01 creada logica de enviar y recibir sms y de detectar enchufado y desenchufado esto funiona ok incluso fuera app y hecha auto boot!!
    //v02 añadida detcetaion vibtatio con gauge wue o representa y valor minimo del gauge a elegir en seekbar
    //V03 AÑADIDO ENVIO EN BACKGROUND DE EMAIL Y ALGO DE INTERFAZ..PTE
    //v035 CREASDOS ERVICIO DE LEER NPOTIS PTE DE IMPLEMNETAR Y AÑADIDAS SHAREDPREF DE LA MAIN
    //V05 LEE YA LOS HANGOUT PERO DA CRASH..



    //PARA EL LOGGING
    private static final int REQUESTSMS_PERMISSION_CODE = 0;
    private static final String TAG = "MainAc sensores";

    //pàra los valores del sms a enviar



    //para ssaber sie le notif servcie esta runnning y habilitado

    private boolean mServiceActive;

    //sharedprefs

    private SharedPreferences mPrefs;


    //para el intnt Extra info

    public static   String  EXTRA_MESSAGE="mensaje";
    public static   String  EXTRA_TIME="time";
    public static   String  EXTRA_RMNAME="3T CORDOBA";
    public static   String  EXTRA_SMSCONFIGURADO="639689367";



    //para el gauge

    private CircleProgress circleProgress;

    //seekbar

   private SeekBar simpleSeekBar;

   //radiogroup

    private RadioGroup radiogroupchooseapktonotify;




    float ValorVibrationclaculada;
   private  int ValorMinimoVibration;
    // para el sensor

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    //valores del sensor

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;


    private static final int SHAKE_THRESHOLD = 600;

    //para el intent Extra info

    private String  TextorecibidoService ;



    //PARA PODER HIDE LA PROGRESSBAR DESDE EL ASYNTASK


    private ProgressDialog progressDialog;


    private boolean SendEmailOK=false;//PARASABER SI SE MANDO OK  O NO EL AUTO EMAIL


    //para los edittext


    private EditText srn,email,hospname,otherappname;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //las pref

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);




        //radiogroup

        radiogroupchooseapktonotify = (RadioGroup) findViewById(R.id.radiogrupochooseapk);


        if( mPrefs.getString(SmsHelper.PREF_RADIOBUTTONVALUEAPPTOREADNOTIS,"hangout").equals("sms")){

            RadioButton rbmsms=(RadioButton)radiogroupchooseapktonotify.getChildAt(0);
            rbmsms.setChecked(true);

            //CHEQUEO SMS PERMISION

            /*
            if (!hasReadSmsPermission()) {
                showRequestPermissionsInfoAlertDialog();
            }
            */

            //OPCION 1
            showSMSStatePermission();



            //CHEQUEO ACCESOA ALEER NOTIFIS


            initializeService();




        }
        else if  ( mPrefs.getString(SmsHelper.PREF_RADIOBUTTONVALUEAPPTOREADNOTIS,"hangout").equals("hangout")){
            RadioButton rbhangout=(RadioButton)radiogroupchooseapktonotify.getChildAt(1);
            rbhangout.setChecked(true);

        }
        else if  ( mPrefs.getString(SmsHelper.PREF_RADIOBUTTONVALUEAPPTOREADNOTIS,"hangout").equals("otro")){
            RadioButton rbmotro=(RadioButton)radiogroupchooseapktonotify.getChildAt(2);
            rbmotro.setChecked(true);

        }



        //los edittext


        srn = (EditText) findViewById(R.id.srntext);
        email = (EditText) findViewById(R.id.emailtext);
        hospname = (EditText) findViewById(R.id.hospnameedittext);
        otherappname = (EditText) findViewById(R.id.otherapppackagename);


        //mejor los hag focusabel apra que guarden el valor despues de darle al teclado done o de salir:

        //other app nmae



        ((EditText)findViewById(R.id.otherapppackagename)).setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                /* When focus is lost check that the text field
                 * has valid values.
                 */
                if (!hasFocus) {

                    mPrefs.edit().putString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,otherappname.getText().toString()).commit();
                }
            }
        });

        otherappname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do your stuff here
                    mPrefs.edit().putString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,otherappname.getText().toString()).commit();

                }
                return false;
            }
        });


        //hosp name:



        ((EditText)findViewById(R.id.hospnameedittext)).setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                /* When focus is lost check that the text field
                 * has valid values.
                 */
                if (!hasFocus) {

                    mPrefs.edit().putString(SmsHelper.PREF_HOSPNAME,hospname.getText().toString()).commit();
                }
            }
        });

        hospname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do your stuff here
                    mPrefs.edit().putString(SmsHelper.PREF_HOSPNAME,hospname.getText().toString()).commit();

                }
                return false;
            }
        });



        //email;



        ((EditText)findViewById(R.id.emailtext)).setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                /* When focus is lost check that the text field
                 * has valid values.
                 */
                if (!hasFocus) {

                    mPrefs.edit().putString(SmsHelper.PREF_EMAIL,email.getText().toString()).commit();
                }
            }
        });

        email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do your stuff here
                    mPrefs.edit().putString(SmsHelper.PREF_EMAIL,email.getText().toString()).commit();

                }
                return false;
            }
        });









        //srn:


        ((EditText)findViewById(R.id.srntext)).setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                /* When focus is lost check that the text field
                 * has valid values.
                 */
                if (!hasFocus) {

                    mPrefs.edit().putString(SmsHelper.PREF_SRN,srn.getText().toString()).commit();
                }
            }
        });

        srn.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do your stuff here
                    mPrefs.edit().putString(SmsHelper.PREF_SRN,srn.getText().toString()).commit();

                }
                return false;
            }
        });


        //el gauge:

        circleProgress = (CircleProgress) findViewById(R.id.circle_progress);
        circleProgress.setMax(150);
        circleProgress.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                SendEmailtestMio();

                return true;
            }
        }) ;





        //el seekbar

        //initial value =25%..no que lo recupere de la pref mas abjo

       // ValorMinimoVibration=25;

          simpleSeekBar=(SeekBar) findViewById(R.id.seekBar); // initiate the progress bar
        final TextView ValorSeekBar=(TextView)findViewById(R.id.valorseekbar);

        //le damos su valor guardado

        //recuperamos vzlores de campos si ya existian


        srn.setText(mPrefs.getString(SmsHelper.PREF_SRN,null));
        email.setText(mPrefs.getString(SmsHelper.PREF_EMAIL,null));
        hospname.setText(mPrefs.getString(SmsHelper.PREF_HOSPNAME,null));
        otherappname.setText(mPrefs.getString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,null));

        ValorMinimoVibration=mPrefs.getInt(SmsHelper.PREF_VALUETRIGGERVIBRATE,25);

        Log.d("trigger en: ",String.valueOf( ValorMinimoVibration));





        ValorSeekBar.setText(String.valueOf(ValorMinimoVibration)+"%");
        simpleSeekBar.setProgress(ValorMinimoVibration);



        // perform seek bar change listener event used for getting the progress value
        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 25;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                /*
                Toast.makeText(MainActivity.this, "Seek bar progress is :" + progressChangedValue,
                        Toast.LENGTH_SHORT).show();*/

                ValorMinimoVibration=progressChangedValue;
                //y lo guardamos

                mPrefs.edit().putInt(SmsHelper.PREF_VALUETRIGGERVIBRATE,progressChangedValue).commit();

                ValorSeekBar.setText(String.valueOf(ValorMinimoVibration)+"%");

            }
        });



          /*Additionally we have two possibilities to keep our application awake.
                First is adding a flag to WindowManager which will keep the screen on with full power:
        */


        Window window = getWindow();

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        //registro de sensor y variables de sensores

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);




        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////RECIBIR TEXT DESDE EL  SERVICE DE LEER NOTIS//////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        //FORMA 1 PASADO EN EL INTENT EXTRAS

        Bundle extrasfromService = getIntent().getExtras();
        if(extrasfromService !=null){





            //METODO SACANDO EL EXTRA DE CADA COSA



            //CharSequence notificationText = extrasfromService.getCharSequence(Notification.EXTRA_TEXT);

            CharSequence notificationText = extrasfromService.getCharSequence("TEXTORECIBIDO");
            // CharSequence notificationSubText = extrasfromService.getCharSequence(Notification.EXTRA_SUB_TEXT);//no manda anda en whastapp akl menos



            if (notificationText !=null ){

                TextorecibidoService.equals(notificationText);

                //enviamos email con los datos si cumple Phchksms o phchksms

                if (TextorecibidoService.equals(SmsHelper.SMS_CONDITION)|| TextorecibidoService.equals(SmsHelper.SMS_CONDITION2)){

                    SendEmailInBackgroundMio();
                }


                //si es PHSETTTRIGER XX


                if (TextorecibidoService.equals(SmsHelper.SMS_CONDITION3)){



                    //sacamos el valor:

                    String textfiltrado=removeWord(TextorecibidoService,"PHSETTTRIGER");

                    Log.d(" texto filtrado es: ",textfiltrado);

                    int myNum = 0;

                    try {
                        myNum = Integer.parseInt(textfiltrado );
                    } catch(NumberFormatException nfe) {
                        System.out.println("Could not parse " + nfe);
                    }


                    ValorMinimoVibration=myNum;
                    //y lo guardamos

                    mPrefs.edit().putInt(SmsHelper.PREF_VALUETRIGGERVIBRATE,myNum).commit();

                    ValorSeekBar.setText(String.valueOf(ValorMinimoVibration)+"%");


                    SendEmailInBackgroundMio();//

                }



            }








        }








    }



    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////CHEQUEO ACCESOS A NOTFICACIOENS!!!//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void initializeService() {

        checkForRunningService();


                if (mServiceActive) {
                    // showServiceDialog(R.string.notification_listener_launch);
                    showServiceDialog(R.string.notification_listener_launch);
                } else {
                    // showServiceDialog(R.string.notification_listener_warning);
                    showServiceDialog(R.string.notification_listener_warning);
                }


            }





    private boolean checkForRunningService() {
        mServiceActive = NotificationServiceHelper.isServiceRunning(this);
        if (mServiceActive) {
            return true;

        } else {

            return false;

        }
    }

    private void showServiceDialog(int message) {
        new android.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface alertDialog, int id) {
                        alertDialog.cancel();
                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    }
                })
                .show();
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////filtrar el texto recibido//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




    public static String removeWord(String string, String word)
    {

        // Check if the word is present in string
        // If found, remove it using removeAll()
        if (string.contains(word)) {

            // To cover the case
            // if the word is at the
            // beginning of the string
            // or anywhere in the middle
            String tempWord = word + " ";
            string = string.replaceAll(tempWord, "");

            // To cover the edge case
            // if the word is at the
            // end of the string
            tempWord = " " + word;
            string = string.replaceAll(tempWord, "");
        }

        // Return the resultant string
        return string;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////radiobutton listener//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void onRadioButtonClicked(View view) {

        boolean marcado = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radioButtonhangout:
                if (marcado) {
                    //actualizamos el valor guardado:

                    mPrefs.edit().putString(SmsHelper.PREF_RADIOBUTTONVALUEAPPTOREADNOTIS,"hangout").commit();

                    //y el valor de al pp a read notis

                    mPrefs.edit().putString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,"com.google.android.talk").commit();




                    //mostrarParticular(false);
                }
                break;

            case R.id.radioButtonsms:
                if (marcado) {


                    mPrefs.edit().putString(SmsHelper.PREF_RADIOBUTTONVALUEAPPTOREADNOTIS,"sms").commit();

                    //y el valor de al pp a read notis

                    mPrefs.edit().putString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,"none").commit();

                    //CHEQUEO SMS PERMISION


                    /*
                    if (!hasReadSmsPermission()) {
                        showRequestPermissionsInfoAlertDialog();
                    }
                        */

                    //OPCION 1

                    showSMSStatePermission();


                }
                break;

            case R.id.radioButtonotros:
                if (marcado) {

                    mPrefs.edit().putString(SmsHelper.PREF_RADIOBUTTONVALUEAPPTOREADNOTIS,"otro").commit();

                    //y el valor de al pp a read notis

                    mPrefs.edit().putString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,otherappname.getText().toString()).commit();



                }
                break;
        }
    }


    /**
     * Check if we have SMS permission
     */
    public boolean isSmsPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Runtime permission
     */

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////OPCION 1//////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void showSMSStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECEIVE_SMS);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECEIVE_SMS)) {
                showExplanation("Permission Needed", "SMS", Manifest.permission.RECEIVE_SMS, REQUESTSMS_PERMISSION_CODE);
            } else {
                requestPermission(Manifest.permission.RECEIVE_SMS, RECEIVER_VISIBLE_TO_INSTANT_APPS);
            }
        } else {
            Toast.makeText(MainActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case REQUESTSMS_PERMISSION_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////OPCION 2 NO FUNCIONA////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    private boolean hasReadSmsPermission() {




        return ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request runtime SMS permission
     */
    private void requestReadAndSendSmsPermission() {


        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
            // You may display a non-blocking explanation here, read more in the documentation:
            // https://developer.android.com/training/permissions/requesting.html
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, REQUESTSMS_PERMISSION_CODE);


        }





    /**
     * Displays an AlertDialog explaining the user why the SMS permission is going to be requests
     *
     * Optional informative alert dialog to explain the user why the app needs the Read/Send SMS permission
     */
    private void showRequestPermissionsInfoAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permission_alert_dialog_title);
        builder.setMessage(R.string.permission_dialog_message);
        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                requestReadAndSendSmsPermission();
                dialog.dismiss();
            }
        });
        builder.show();
    }



        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////sensor listener//////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //https://code.tutsplus.com/tutorials/using-the-accelerometer-on-android--mobile-22125

        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];




            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 500) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

              //  Log.d(TAG+" x: ",String.valueOf(x));
               // Log.d(TAG+" y: ",String.valueOf(y));
               // Log.d(TAG+" z: ",String.valueOf(z));

               // float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

               // double total = Math.sqrt(x * x + y * y + z * z);//graviotation





                ////////////

                float difx=50*Math.abs(Math.abs(x)-Math.abs(last_x));
                float dify=50*Math.abs(Math.abs(y)-Math.abs(last_y));
                float difz=50*Math.abs(Math.abs(z)-Math.abs(last_z));

                ValorVibrationclaculada=new Float( Math.abs(difx +dify+difz));



               // absLabel.setText(String.format("ABS: %+2.0f ", (float)Math.round(ValorVibrationclaculada)));

                //Log.d( "ValorVibrationcal:",String.format("ABS: %+2.0f ", (float)Math.round(ValorVibrationclaculada)));

                circleProgress.setProgress(Math.round(ValorVibrationclaculada));




                last_x = x;
                last_y = y;
                last_z = z;


                if (ValorVibrationclaculada<ValorMinimoVibration){

                  //  Log.d(TAG+" MENOR!:",String.valueOf(ValorMinimoVibration));

                    circleProgress.setFinishedColor(Color.RED);


                }


                else {

                    circleProgress.setFinishedColor(Color.GREEN);
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////sensor listener//////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////



    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////auto email//////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void empiezagiraprogressbar(){


        progressDialog = new ProgressDialog(MainActivity.this,  R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Generando  email encriptado para enviar a servidor,por favor envie el email a continuacion... en 24h recibira por email su contraseña.");
        progressDialog.show();


    }


    public void SendEmailInBackgroundMio(){


        String srntopass = srn.getText().toString();
        String emailtopass = email.getText().toString();
        String hospitalnametopass = hospname.getText().toString();
        String appnametoreadnotistopass = mPrefs.getString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,"none");








        //empieza a girara spinnerr


       // empiezagiraprogressbar();


        Mail m = new Mail("icas.generico@gmail.com", "Sevilla2!");
        //String[] toArr = {"jrdvsoftyopozi@gmail.com"};

        //new Email::

        //String[] toArr = {"interamaster@gmail.com"};

        String[] toArr = {emailtopass};

        m.setTo(toArr);
        m.setFrom("icas.generico@gmail.com");
        m.setSubject("MRCOMPRESSOR "+hospitalnametopass);
       // m.setBody("dasdsd");

        m.setBody("MRCOMPRESSOR IS RUNNING:\n" +"EQUIPO SRN: "+ srntopass + "\nNOMBRE: " + hospitalnametopass + "\n LEYENDO APK: " + appnametoreadnotistopass +
                "\n VALOR DE TRIGGER:" + ValorMinimoVibration + "\n VALOR ACTUAL VIBRACION:" +ValorVibrationclaculada+ " \n Start ENCRYPTED:" + "\n \n \n " +
                " (C) JOSE RAMON DELGADO 2019");


        try {
            // m.addAttachment("/sdcard/bday.jpg");
            if(m.send()) {
                //Toast.makeText(this, "Email was sent successfully.", Toast.LENGTH_LONG).show();
                //al hacerlo en backgrousd nos e pueden poner Toast!!!
                //asin que pongo la ivar a true y ya en el postexecute del asyntask que lo ponga!!!

                SendEmailOK=true;

            } else {

                //al hacerlo en backgrousd nos e pueden poner Toast!!!
                //asin que pongo la ivar a false  y ya en el postexecute del asyntask que lo ponga!!!

                SendEmailOK=false;


                //Toast.makeText(this, "Email was not sent.", Toast.LENGTH_LONG).show();
                // Toast.makeText(this, "Lo siento su movil no esta preparado para mandar emails de manera autoamtica vams ahcerlo de manera manual" +
                //   "", Toast.LENGTH_SHORT).show();

              //  ManualEmailSiFallaAutomatico();

            }
        } catch(Exception e) {
            Log.e("MailApp", "Could not send email", e);

            //lo mandamos manual

            SendEmailOK=false;


           // ManualEmailSiFallaAutomatico();
        }





    }

    public void SendEmailtestMio() {




        new SendMail().execute("");

    }



    private class SendMail extends AsyncTask<String, Integer, Void> {


        protected void onProgressUpdate() {
            //called when the background task makes any progress
        }

        @Override
        protected Void doInBackground(String... params) {
            SendEmailInBackgroundMio();
            return null;
        }

        protected void onPreExecute() {
            //called before doInBackground() is started
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (SendEmailOK) {
                //funciono ok

               // progressDialog.hide();
                Toast.makeText(MainActivity.this, "Su email se envio correctamente!!", Toast.LENGTH_LONG).show();

                //finish();


            }

            else {

                //ha fallado

                Toast.makeText(MainActivity.this, "Email fallo!!!", Toast.LENGTH_LONG).show();
            }


        }
    }

}
