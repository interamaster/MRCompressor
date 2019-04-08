package com.example.mrcompressor;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mrcompressor.helpers.NotificationServiceHelper;
import com.github.lzyzsd.circleprogress.CircleProgress;

public class MainActivity extends AppCompatActivity implements SensorEventListener,CountDownAnimation.CountDownListener {

    //v01 creada logica de enviar y recibir sms y de detectar enchufado y desenchufado esto funiona ok incluso fuera app y hecha auto boot!!
    //v02 añadida detcetaion vibtatio con gauge wue o representa y valor minimo del gauge a elegir en seekbar
    //V03 AÑADIDO ENVIO EN BACKGROUND DE EMAIL Y ALGO DE INTERFAZ..PTE
    //v035 CREASDOS ERVICIO DE LEER NPOTIS PTE DE IMPLEMNETAR Y AÑADIDAS SHAREDPREF DE LA MAIN
    //V05 LEE YA LOS HANGOUT PERO DA CRASH..
    //v06 funciona detecion de no vinration y alos 5 min MANDA EMAIL DE FALLO, YA LEE OK LOS HANGOUT Y CAMBIA EL TRIGGER CON MENSAJE PHSETTRIGGER XX
    //v08 ENVIO EMAIL SI POWEROFF O ON Y SMS Y DETECTAR SI TIENE SIM O NO Y SI NO TIENE RED AVISA DE QUE NO MANDARA NADA
    //V085 AÑADIDO AJUSTES CON EMAIL Y PASS PARA ENVIAR POR SI EN UN FUTURO CAMBIA....Y TEMPORIZADORES DE REENVIO EMAIL A LA HORA Y A LAS 24H
    //v099 AÑADIDO ICONO Y TERMINDAD..PASA A MODO PRUEBA
    //v1,0 AÑADIDA VERSION NUMBER EN TITLE Y AÑADIDO ENVIO DE SMS EN CASO DE TEST,FALLO ,POWER OFF Y POWER ON ,Y RESPONDE A ENVIO SMS.. LISTA A PROBAR IN SITE




    //PARA EL LOGGING
    private static final int REQUESTSMS_PERMISSION_CODE = 0;
    private static final String TAG = "MainAc sensores";

    //pàra los valores del sms a enviar


    //para saber si hay q reenviar email en 1h 12h o 24h


    private boolean reenviaren1h=false;
    private boolean reenviaren12h=false;
    private  boolean reenviaren24h=false;

    private int timeranimacion;


    //para el countdown animation

    TextView textCountdown;

    CountDownAnimation countDownAnimation;

    //Declare timer del teimpo para avisar
    CountDownTimer cTimer = null;

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
    float ValorVibrationclaculadaANTERIOR;
   private  int ValorMinimoVibration;
    // para el sensor

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    //valores del sensor

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;


    private static final int SHAKE_THRESHOLD = 600;

    //para el intent Extra info

    private String  TextorecibidoService ="";



    //PARA PODER HIDE LA PROGRESSBAR DESDE EL ASYNTASK


    private ProgressDialog progressDialog;


    private boolean SendEmailOK=false;//PARASABER SI SE MANDO OK  O NO EL AUTO EMAIL


    //para los edittext


    private EditText srn,email,hospname,otherappname;


    private EditText emailtosend,passemailtosend;

    private  EditText numtelefono;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //poner la version en el titulo

        this.setTitle("MRCOMPRESSOR V."+BuildConfig.VERSION_NAME);




        //las pref

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);



        //recuperar alarmas de reenvio


        reenviaren1h=mPrefs.getBoolean(SmsHelper.PREF_BOOL_REENVIAREMAIL1H,false);
        reenviaren12h=mPrefs.getBoolean(SmsHelper.PREF_BOOL_REENVIAREMAIL12H,false);
        reenviaren24h=mPrefs.getBoolean(SmsHelper.PREF_BOOL_REENVIAREMAIL24H,false);


        //y el valor del timer de aniamcion de avisod e reenvio email


        timeranimacion=mPrefs.getInt(SmsHelper.PREF_INT_TIEMPOANIMACIONTIMER,300);//por defecto 300 segs


        //CHEQUEO ACCESO  A EER NOTIFIS


        initializeService();



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

        emailtosend=(EditText) findViewById(R.id.emailsender);
        passemailtosend=(EditText) findViewById(R.id.emailpasssender);

        numtelefono=(EditText) findViewById(R.id.telfnumber);


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



        //email sender

        ((EditText)findViewById(R.id.emailsender)).setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                /* When focus is lost check that the text field
                 * has valid values.
                 */
                if (!hasFocus) {

                    mPrefs.edit().putString(SmsHelper.PREF_EMAILSENDER,emailtosend.getText().toString()).commit();
                }
            }
        });

        emailtosend.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do your stuff here
                    mPrefs.edit().putString(SmsHelper.PREF_EMAILSENDER,emailtosend.getText().toString()).commit();

                }
                return false;
            }
        });



        //passemailsender


        ((EditText)findViewById(R.id.emailpasssender)).setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                /* When focus is lost check that the text field
                 * has valid values.
                 */
                if (!hasFocus) {

                    mPrefs.edit().putString(SmsHelper.PREF_PASSEMAILSENDER,passemailtosend.getText().toString()).commit();
                }
            }
        });

        passemailtosend.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do your stuff here
                    mPrefs.edit().putString(SmsHelper.PREF_PASSEMAILSENDER,passemailtosend.getText().toString()).commit();

                }
                return false;
            }
        });



        //telfnumber


        ((EditText)findViewById(R.id.telfnumber)).setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                /* When focus is lost check that the text field
                 * has valid values.
                 */
                if (!hasFocus) {

                    mPrefs.edit().putString(SmsHelper.PREF_TELF_NUMBER,numtelefono.getText().toString()).commit();
                }
            }
        });

        numtelefono.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do your stuff here
                    mPrefs.edit().putString(SmsHelper.PREF_TELF_NUMBER,numtelefono.getText().toString()).commit();

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

                SendEmailYSMStestMio();

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

        numtelefono.setText(mPrefs.getString(SmsHelper.PREF_TELF_NUMBER,null));

        ValorMinimoVibration=mPrefs.getInt(SmsHelper.PREF_VALUETRIGGERVIBRATE,25);


        emailtosend.setText(mPrefs.getString(SmsHelper.PREF_EMAILSENDER,"icas.generico@gmail.com"));
        passemailtosend.setText(mPrefs.getString(SmsHelper.PREF_PASSEMAILSENDER,"Sevilla2!"));

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


            //ANULAMOS EL TIMER SI VIENE DE UNA NOTIFICACION

            cTimer = null;




            //METODO SACANDO EL EXTRA DE CADA COSA



            //CharSequence notificationText = extrasfromService.getCharSequence(Notification.EXTRA_TEXT);

            CharSequence notificationText = extrasfromService.getCharSequence("TEXTORECIBIDO");
            // CharSequence notificationSubText = extrasfromService.getCharSequence(Notification.EXTRA_SUB_TEXT);//no manda anda en whastapp akl menos



            if (notificationText !=null ){


                //TextorecibidoService.equals(notificationText);
                TextorecibidoService=notificationText.toString();

              //  Log.d(TAG , "notificacion recibida de intent: "+TextorecibidoService);



                //primero separamops le texto por espacio

                String[] TextoSeparadodelService = TextorecibidoService.trim().split("\\s+");

                String Texto1delservice=TextoSeparadodelService[0];

                if (TextoSeparadodelService.length>1){
                String Texto2delService=TextoSeparadodelService[1];
                }





                //enviamos email con los datos si cumple Phchksms o phchksms

                if (Texto1delservice.equals(SmsHelper.SMS_CONDITION)|| Texto1delservice.equals(SmsHelper.SMS_CONDITION2)){

                   // Log.d(TAG , "notificacion recibida de intent: ERA phchksms!! y lo ejecuto"  );

                    SendEmailYSMStestMio();
                }


                //si es PHSETTTRIGER XX


                if (Texto1delservice.equals(SmsHelper.SMS_CONDITION3)){

                 //   Log.d(TAG , "notificacion recibida de intent: ERA PHSETTRIGER!! y lo ejecuto"  );



                    //sacamos el valor:


                        String Texto2delService=TextoSeparadodelService[1];


                   // String textfiltrado=removeWord(TextorecibidoService,"PHSETTTRIGER");



                  //  Log.d(" texto filtrado es: ",Texto2delService);

                    int myNum = 0;

                    try {
                        myNum = Integer.parseInt(Texto2delService );
                    } catch(NumberFormatException nfe) {
                        System.out.println("Could not parse " + nfe);
                    }


                    ValorMinimoVibration=myNum;
                    //y lo guardamos

                    mPrefs.edit().putInt(SmsHelper.PREF_VALUETRIGGERVIBRATE,myNum).commit();

                    ValorSeekBar.setText(String.valueOf(ValorMinimoVibration)+"%");
                    simpleSeekBar.setProgress(ValorMinimoVibration);



                    // SendEmailInBackgroundMioOK();//

                }


                //si es POWEROFF desenchufado


                if (Texto1delservice.equals(SmsHelper.INTENTPOWEROFF)){

                  //  Log.d(TAG , "notificacion recibida de intent: ERA POWEROFF!! y lo ejecuto"  );



                    //TIENE SIM?


                    if (isSimAvailable()){

                        if (numtelefono!=null) {


                            String textoSMS="SRN:"+srn.getText().toString()+"\nHOSPITAL:"+hospname.getText().toString()+"\nMRCOMPRESSOR DESENCHUFADO!!";

                            SmsHelper.sendInfoSms(numtelefono.getText().toString(),textoSMS);

                            Toast.makeText(MainActivity.this, "SENDING SMS TO:"+numtelefono.getText().toString(), Toast.LENGTH_SHORT).show();

                        }


                    }


                    // MANDA EMAIL SIMEPRE



                    new SendMailPOWEROFF().execute("");



                }



                //si es POWERON vuelto a enchufar


                if (Texto1delservice.equals(SmsHelper.INTENTPOWERON)){

                  //  Log.d(TAG , "notificacion recibida de intent: ERA POWERON!! y lo ejecuto"  );


                    //TIENE SIM?

                    if (isSimAvailable()){

                        if (numtelefono!=null) {


                            String textoSMS="SRN:"+srn.getText().toString()+"\nHOSPITAL:"+hospname.getText().toString()+"\nMRCOMPRESSOR VUELTO A ENCHUFAR!!";

                            SmsHelper.sendInfoSms(numtelefono.getText().toString(),textoSMS);

                            Toast.makeText(MainActivity.this, "SENDING SMS TO:"+numtelefono.getText().toString(), Toast.LENGTH_SHORT).show();

                        }


                    }


                    // MANDA EMAIL SIMEPRE



                    new SendMailPOWERON().execute("");



                }


                //SI ES REBOOT TAMBIEN


                if (Texto1delservice.equals(SmsHelper.INTENTREBOOT)){

                 //   Log.d(TAG , "notificacion recibida de intent: ERA REBOOT!! y lo ejecuto"  );



                    //ENVIO SMS Y EMAIL

                    SendEmailYSMSTrasReboot();


                }



            }








        }








    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////CHEQUEO network//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        //https://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////CHEQUEO SIM//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




    public boolean isSimAvailable() {
        boolean isAvailable = false;
        TelephonyManager telMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT: //SimState = “No Sim Found!”;
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED: //SimState = “Network Locked!”;
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED: //SimState = “PIN Required to access SIM!”;
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED: //SimState = “PUK Required to access SIM!”; // Personal Unblocking Code
                break;
            case TelephonyManager.SIM_STATE_READY:
                isAvailable = true;
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN: //SimState = “Unknown SIM State!”;
                break;
        }
        return isAvailable;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////CHEQUEO ACCESOS A NOTFICACIOENS!!!//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void initializeService() {

        checkForRunningService();


                if (mServiceActive) {
                    // showServiceDialog(R.string.notification_listener_launch);
                   // showServiceDialog(R.string.notification_listener_launch); //SI ESTA OK NO HACE NADA
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



                    //chequea app esta instalada

                    if (!isAppInstalled("com.google.android.talk")){


                        //avisa que no esta instalada

                        Toast.makeText(MainActivity.this, "QUIZAS DEBERIAS INSTALAR HANGOUT ARTISTA!!!!", Toast.LENGTH_SHORT).show();

                        //y manda a google play



                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.talk&hl=es"));
                        startActivity(intent);
                    }



                    //CHEQUEO ACCESOA ALEER NOTIFIS


                    initializeService();

                    //mostrarParticular(false);
                }
                break;

            case R.id.radioButtonsms:
                if (marcado) {


                    //chequeo si tinen SIM!!!

                    if (isSimAvailable()){


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

                    showSMSStatePermission();}


                    else {

                        //no tiene SIM asi q no se puede cambiar



                        Toast.makeText(MainActivity.this, "NO TIENES SIM..SOLO HANGOUT!", Toast.LENGTH_SHORT).show();


                        RadioButton rbhangout=(RadioButton)radiogroupchooseapktonotify.getChildAt(1);
                        rbhangout.setChecked(true);

                    }


                }
                break;

            case R.id.radioButtonotros:
                if (marcado) {

                    mPrefs.edit().putString(SmsHelper.PREF_RADIOBUTTONVALUEAPPTOREADNOTIS,"otro").commit();

                    //y el valor de al pp a read notis

                    mPrefs.edit().putString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,otherappname.getText().toString()).commit();




                    //CHEQUEO ACCESOA ALEER NOTIFIS



                    //chequea app esta instalada

                    if (!isAppInstalled(mPrefs.getString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,"none"))){


                        //avisa que no esta instalada

                        Toast.makeText(MainActivity.this, "QUIZAS DEBERIAS INSTALAR ESA APK ARTISTA..!!!!", Toast.LENGTH_SHORT).show();

                        //y manda a google play



                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+mPrefs.getString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,"none")));
                        startActivity(intent);
                    }


                    initializeService();

                }
                break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////chequea app is installed//////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////




    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
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



                ValorVibrationclaculadaANTERIOR=ValorVibrationclaculada;

                ////////////

                float difx=50*Math.abs(Math.abs(x)-Math.abs(last_x));
                float dify=50*Math.abs(Math.abs(y)-Math.abs(last_y));
                float difz=50*Math.abs(Math.abs(z)-Math.abs(last_z));

                ValorVibrationclaculada=new Float( Math.abs(difx +dify+difz));




                /*
                if ((ValorVibrationclaculada > ValorVibrationclaculadaANTERIOR)&&(ValorVibrationclaculada < 100)){

                    ValorVibrationclaculada=ValorVibrationclaculadaANTERIOR+1;

                }
                else {

                    ValorVibrationclaculada=ValorVibrationclaculadaANTERIOR-1;
                }

                */

               // absLabel.setText(String.format("ABS: %+2.0f ", (float)Math.round(ValorVibrationclaculada)));

                //Log.d( "ValorVibrationcal:",String.format("ABS: %+2.0f ", (float)Math.round(ValorVibrationclaculada)));

                circleProgress.setProgress(Math.round(ValorVibrationclaculada));




                last_x = x;
                last_y = y;
                last_z = z;


                if (ValorVibrationclaculada<ValorMinimoVibration){

                   //  Log.d(TAG+" MENOR!:",String.valueOf(ValorMinimoVibration)+"anterior:"+String.valueOf(ValorVibrationclaculadaANTERIOR)+" actual:"+String.valueOf(ValorVibrationclaculada));

                    circleProgress.setFinishedColor(Color.RED);
                   // circleProgress.setUnfinishedColor(Color.RED);//asi se reelna entero verde o rojo...


                    //si ya tenia que reenviar por una hora



                    if (reenviaren1h && timeranimacion==300){

                    //asi solo se ejecyut6a 1 vez!!!


                        timeranimacion=3600;
                        mPrefs.edit().putInt(SmsHelper.PREF_INT_TIEMPOANIMACIONTIMER,3600).commit();

                        //y reininio timer
                        //cancel timer

                        if(cTimer!=null){
                            cTimer.cancel();
                            cTimer=null;

                        }

                        //y la animation

                        if (countDownAnimation!=null) {


                            countDownAnimation.cancel();
                            countDownAnimation=null;



                        }




                    }

                    if (reenviaren24h && timeranimacion==3600){

                        //solo se ejecuta 1 vez

                        //y reininio timer
                        //cancel timer

                        if(cTimer!=null){
                            cTimer.cancel();
                            cTimer=null;

                        }

                        //no la animacion!!! pues seria 360*24 y  o cabe

                        timeranimacion=3600*24;
                        mPrefs.edit().putInt(SmsHelper.PREF_INT_TIEMPOANIMACIONTIMER,3600*24).commit();



                    }

                    //iniciamos el timer si no existia ya!!!
                    if (cTimer==null){





                        if (countDownAnimation==null) {


                            //////////////////countdown animation///////////////////////

                            textCountdown = (TextView) findViewById(R.id.textcountdown);
                            countDownAnimation = new CountDownAnimation(textCountdown, timeranimacion);//300 SEGUNDOS por defecto

                            //elegimnoms una niamacionmas chula:
                            Animation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                            AnimationSet animationSet = new AnimationSet(false);
                            animationSet.addAnimation(scaleAnimation);
                            animationSet.addAnimation(alphaAnimation);


                            countDownAnimation.setAnimation(animationSet);


                            //añadimos el listener a nosotros
                            countDownAnimation.setCountDownListener(this);
                            //y empezamos!!!
                             countDownAnimation.start();
                        }


                        ////////////////////////////////////////////////



                            cTimer = new CountDownTimer(timeranimacion*1000, 1000) {//300 seg= 5min!! hay que onerlo en milisecs por eso x1000
                                public void onTick(long millisUntilFinished) {



                                    Log.d("seconds remaining: " ,Long.toString(millisUntilFinished / 1000));

                                    //CADA  5 SEGUNDO UN BIP
                                    //http://lineadecodigo.com/java/multiplo-de-un-numero-en-java/


                                    if ((millisUntilFinished/1000)%5==0) {

                                        //https://stackoverflow.com/questions/13463691/error-generating-beep-using-tonegenerator-class

                                        final ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);



                                        //que espere 2 segundos a borrar el sonido o a aveces no suena

                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                //Do something after 2s para que ñpueda leer el sensor

                                                toneG.release();
                                            }
                                        }, 2000);




                                        //si no tiene red que avise!!!!


                                        if (!isNetworkAvailable()) {
                                            Toast.makeText(MainActivity.this, "OJO NO HAY RED NO MANDARE NADA!!!!", Toast.LENGTH_SHORT).show();

                                        }





                                    }


                                }
                                public void onFinish() {

                                    cTimer.cancel();

                                    //si llega al final manda EMAIL!!!
                                    SendEmailySMSFallo();
                                    //Y SMS SI PUEDE


                                    //activo reenvio email en 1h


                                    if (!reenviaren1h) {

                                        reenviaren1h = true;

                                        reenviaren1h = mPrefs.edit().putBoolean(SmsHelper.PREF_BOOL_REENVIAREMAIL1H, true).commit();

                                    }

                                    else {

                                        //si ya estaba el 1 hora activo el 24h

                                        reenviaren24h=true;



                                        reenviaren1h = mPrefs.edit().putBoolean(SmsHelper.PREF_BOOL_REENVIAREMAIL24H, true).commit();


                                    }


                                }
                            };
                            cTimer.start();
                        }





                }


                else {

                    circleProgress.setFinishedColor(Color.GREEN);
                    //circleProgress.setUnfinishedColor(Color.GREEN);//asi se reelna entero verde o rojo...


                    //CANCELO LA CUNETA ATRAS



                    //cancel timer

                        if(cTimer!=null){
                            cTimer.cancel();
                            cTimer=null;



                        }

                        //y la animation

                    if (countDownAnimation!=null) {


                        countDownAnimation.cancel();
                        countDownAnimation=null;



                    }

                    //y quitar lo de repetir en 1h

                    reenviaren1h=false;

                   mPrefs.edit().putBoolean(SmsHelper.PREF_BOOL_REENVIAREMAIL1H,false).commit();

                   // y en 24h

                    reenviaren24h=false;

                    mPrefs.edit().putBoolean(SmsHelper.PREF_BOOL_REENVIAREMAIL24H,false).commit();

                    // y pongo timerrep de nuevo a 300 sgs


                    timeranimacion=300;
                    mPrefs.edit().putInt(SmsHelper.PREF_INT_TIEMPOANIMACIONTIMER,300).commit();


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


        if (cTimer!=null) {

            cTimer.cancel();
            cTimer = null;
        }

        if (countDownAnimation!=null) {


            countDownAnimation.cancel();
            countDownAnimation=null;



        }

    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {


        if (cTimer!=null) {

            cTimer.cancel();
            cTimer = null;
        }

        if (countDownAnimation!=null) {


            countDownAnimation.cancel();
            countDownAnimation=null;



        }


        super.onDestroy();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////aniamotr listener//////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////





    @Override
    public void onCountDownEnd(CountDownAnimation animation) {



    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////auto email//////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void empiezagiraprogressbar(){


        progressDialog = new ProgressDialog(MainActivity.this,  R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Generando  email encriptado para enviar a servidor,por favor envie el email a continuacion... en 24h recibira por email su contraseña.");
        progressDialog.show();


    }


    public void SendEmailInBackgroundMioOK(){




        String srntopass = srn.getText().toString();
        String emailtopass = email.getText().toString();
        String hospitalnametopass = hospname.getText().toString();
        String appnametoreadnotistopass = mPrefs.getString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,"none");








        //empieza a girara spinnerr

        //recupermaosn valores de email envio

        String emailtosend2=emailtosend.getText().toString();
        String passemailtosend2=passemailtosend.getText().toString();

        // empiezagiraprogressbar();


        //Mail m = new Mail("icas.generico@gmail.com", "Sevilla2!");

        Mail m = new Mail(emailtosend2, passemailtosend2);

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

    public void SendEmailInBackgroundMioOKTRASREBOOT(){




        String srntopass = srn.getText().toString();
        String emailtopass = email.getText().toString();
        String hospitalnametopass = hospname.getText().toString();
        String appnametoreadnotistopass = mPrefs.getString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,"none");








        //empieza a girara spinnerr

        //recupermaosn valores de email envio

        String emailtosend2=emailtosend.getText().toString();
        String passemailtosend2=passemailtosend.getText().toString();

        // empiezagiraprogressbar();


        //Mail m = new Mail("icas.generico@gmail.com", "Sevilla2!");

        Mail m = new Mail(emailtosend2, passemailtosend2);

        //String[] toArr = {"jrdvsoftyopozi@gmail.com"};

        //new Email::

        //String[] toArr = {"interamaster@gmail.com"};

        String[] toArr = {emailtopass};

        m.setTo(toArr);
        m.setFrom("icas.generico@gmail.com");
        m.setSubject("MRCOMPRESSOR "+hospitalnametopass);
        // m.setBody("dasdsd");

        m.setBody("MRCOMPRESSOR SE HA REINICIADO!!:\n \nESTO QUIERE DECIR QUE SEGURAMENTE NO ESTA FUNCIONADO HASTA QUE SE DESBLOQUEE EL MOVIL!!!!\n\n" +"EQUIPO SRN: "+ srntopass + "\nNOMBRE: " + hospitalnametopass + "\n LEYENDO APK: " + appnametoreadnotistopass +
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


    public void SendEmailInBackgroundMioMAL(){




        String srntopass = srn.getText().toString();
        String emailtopass = email.getText().toString();
        String hospitalnametopass = hospname.getText().toString();
        String appnametoreadnotistopass = mPrefs.getString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,"none");








        //empieza a girara spinnerr

        //recupermaosn valores de email envio
        String emailtosend2=emailtosend.getText().toString();
        String passemailtosend2=passemailtosend.getText().toString();

        // empiezagiraprogressbar();


        //Mail m = new Mail("icas.generico@gmail.com", "Sevilla2!");

        Mail m = new Mail(emailtosend2, passemailtosend2);


        //String[] toArr = {"jrdvsoftyopozi@gmail.com"};

        //new Email::

        //String[] toArr = {"interamaster@gmail.com"};

        String[] toArr = {emailtopass};

        m.setTo(toArr);
        m.setFrom("icas.generico@gmail.com");
        m.setSubject("MRCOMPRESSOR  STOPPED!!! "+hospitalnametopass);
        // m.setBody("dasdsd");

        m.setBody("MRCOMPRESSOR IS STOPPED:\n" +"EQUIPO SRN: "+ srntopass + "\nNOMBRE: " + hospitalnametopass + "\n LEYENDO APK: " + appnametoreadnotistopass +
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


    public void SendEmailInBackgroundMioPOWEROFF(){




        String srntopass = srn.getText().toString();
        String emailtopass = email.getText().toString();
        String hospitalnametopass = hospname.getText().toString();
        String appnametoreadnotistopass = mPrefs.getString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,"none");







        //recupermaosn valores de email envio


        String emailtosend2=emailtosend.getText().toString();
        String passemailtosend2=passemailtosend.getText().toString();

        // empiezagiraprogressbar();


        //Mail m = new Mail("icas.generico@gmail.com", "Sevilla2!");

        Mail m = new Mail(emailtosend2, passemailtosend2);


        //String[] toArr = {"jrdvsoftyopozi@gmail.com"};

        //new Email::

        //String[] toArr = {"interamaster@gmail.com"};

        String[] toArr = {emailtopass};

        m.setTo(toArr);
        m.setFrom("icas.generico@gmail.com");
        m.setSubject("MRCOMPRESSOR "+hospitalnametopass+" DESENCHUFADO");
        // m.setBody("dasdsd");

        m.setBody("MRCOMPRESSOR IS RUNNING PERO SE HA DESENCHUFADO:\n" +"EQUIPO SRN: "+ srntopass + "\nNOMBRE: " + hospitalnametopass + "\n LEYENDO APK: " + appnametoreadnotistopass +
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





    public void SendEmailInBackgroundMioPOWERON(){




        String srntopass = srn.getText().toString();
        String emailtopass = email.getText().toString();
        String hospitalnametopass = hospname.getText().toString();
        String appnametoreadnotistopass = mPrefs.getString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS,"none");






        //recupermaosn valores de email envio

        String emailtosend2=emailtosend.getText().toString();
        String passemailtosend2=passemailtosend.getText().toString();

        // empiezagiraprogressbar();


        //Mail m = new Mail("icas.generico@gmail.com", "Sevilla2!");

        Mail m = new Mail(emailtosend2, passemailtosend2);



        //empieza a girara spinnerr


        // empiezagiraprogressbar();



        //String[] toArr = {"jrdvsoftyopozi@gmail.com"};

        //new Email::

        //String[] toArr = {"interamaster@gmail.com"};

        String[] toArr = {emailtopass};

        m.setTo(toArr);
        m.setFrom("icas.generico@gmail.com");
        m.setSubject("MRCOMPRESSOR "+hospitalnametopass+" VUELTO A ENCHUFAR");
        // m.setBody("dasdsd");

        m.setBody("MRCOMPRESSOR IS RUNNING Y SE HA VUELTO A ENCHUFAR:\n" +"EQUIPO SRN: "+ srntopass + "\nNOMBRE: " + hospitalnametopass + "\n LEYENDO APK: " + appnametoreadnotistopass +
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




    public void SendEmailYSMStestMio() {


        //TIENE SIM?

        if (isSimAvailable()){

            if (numtelefono!=null) {


                String textoSMS="SRN:"+srn.getText().toString()+"\nHOSPITAL:"+hospname.getText().toString()+"\nMRCOMPRESSOR TEST!! \nACTUAL VIBRATION:"+ ValorVibrationclaculada+"\nTRIGGER VALUE:"+ValorMinimoVibration;

                SmsHelper.sendInfoSms(numtelefono.getText().toString(),textoSMS);

                Toast.makeText(MainActivity.this, "SENDING SMS TO:"+numtelefono.getText().toString(), Toast.LENGTH_SHORT).show();

            }


        }



        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 2s para que ñpueda leer el sensor

                new SendMailOK().execute("");
            }
        }, 2000);



       // new SendMailOK().execute("");

    }


    public void SendEmailYSMSTrasReboot() {




        //lo miramos a laos 20 segundpos pues la sim aun no arranca
        //y a parte esta mal el telfnumer!!lo cogemo s de la pref
        //pero no s eporque pero no lo manda..quizas depende del movil..asi se queda el email va bien





        final Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 20s para que ñpueda leer el sensor

                //TIENE SIM?

                if (isSimAvailable()){

                  //  Log.d(TAG , " notificacion recibida de si tiene sim tras reboot"  );


                    String numtelffrompref=mPrefs.getString(SmsHelper.PREF_TELF_NUMBER,null);



                    if (numtelffrompref!=null) {

                    //    Log.d(TAG , "notificacion recibida de si tiene numero de telefono tras reboot:"+numtelffrompref + " y numteldeedittext ees:"+numtelefono  );


                        String textoSMS="SRN:"+srn.getText().toString()+"\nHOSPITAL:"+hospname.getText().toString()+"\nMRCOMPRESSOR SE HA REINICIADO!!! \n ESTO  QUIERE DECIR QUE SEGURAMENTE NO ESTA FUNCIONANDO NI LO HARA HASTA QUE SE  DESBLOQUEE EL TERMINAL!! ";

                        //SmsHelper.sendInfoSms(numtelefono.getText().toString(),textoSMS);
                        SmsHelper.sendInfoSms(numtelffrompref,textoSMS);

                      //  Log.d(TAG , "notificacion recibida de si ENVIO  SMS  tras reboot"  );

                        Toast.makeText(MainActivity.this, "SENDING SMS TO:"+numtelffrompref, Toast.LENGTH_SHORT).show();

                    }

                   // else Log.d(TAG , "notificacion recibida de NO TIENE  numero de telefono tras reboot:"+numtelffrompref  );


                }

             //  else  Log.d(TAG , "notificacion recibida de NO TIENE tras reboot y  40 segs"  );



            }
        }, 20000);





        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 2s para que ñpueda leer el sensor

                new SendMailTRASREBBOT().execute("");



            }
        }, 2000);



        // new SendMailOK().execute("");

    }



    private void SendEmailySMSFallo() {




        if (isSimAvailable()){

            if (numtelefono!=null) {


                String textoSMS="SRN:"+srn.getText().toString()+"\nHOSPITAL:"+hospname.getText().toString()+"\nTHE COMPRESSOR HAS STOPPED!! \nACTUAL VIBRATION:"+ ValorVibrationclaculada+"\nTRIGGER VALUE:"+ValorMinimoVibration;

                SmsHelper.sendInfoSms(numtelefono.getText().toString(),textoSMS);

                Toast.makeText(MainActivity.this, "SENDING SMS TO:"+numtelefono.getText().toString(), Toast.LENGTH_SHORT).show();

            }


        }



        new SendMailFALLO().execute("");


    }



    private class SendMailFALLO extends AsyncTask<String, Integer, Void> {


        protected void onProgressUpdate() {
            //called when the background task makes any progress
        }

        @Override
        protected Void doInBackground(String... params) {






            SendEmailInBackgroundMioMAL();
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





    private class SendMailPOWEROFF extends AsyncTask<String, Integer, Void> {


        protected void onProgressUpdate() {
            //called when the background task makes any progress
        }

        @Override
        protected Void doInBackground(String... params) {






            SendEmailInBackgroundMioPOWEROFF();
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



    private class SendMailPOWERON extends AsyncTask<String, Integer, Void> {


        protected void onProgressUpdate() {
            //called when the background task makes any progress
        }

        @Override
        protected Void doInBackground(String... params) {






            SendEmailInBackgroundMioPOWERON();
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




    private class SendMailOK extends AsyncTask<String, Integer, Void> {


        protected void onProgressUpdate() {
            //called when the background task makes any progress
        }

        @Override
        protected Void doInBackground(String... params) {






            SendEmailInBackgroundMioOK();
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








    private class SendMailTRASREBBOT extends AsyncTask<String, Integer, Void> {


        protected void onProgressUpdate() {
            //called when the background task makes any progress
        }

        @Override
        protected Void doInBackground(String... params) {






            SendEmailInBackgroundMioOKTRASREBOOT();
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
