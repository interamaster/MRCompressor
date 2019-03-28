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
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.CircleProgress;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //v01 creada logica de enviar y recibir sms y de detectar enchufado y desenchufado esto funiona ok incluso fuera app y hecha auto boot!!
    //v02 añadida detcetaion vibtatio con gauge wue o representa y valor minimo del gauge a elegir en seekbar
    //V03 AÑADIDO ENVIO EN BACKGROUND DE EMAIL Y AÑLGO DE INTERFAZ..PTE


    private static final int SMS_PERMISSION_CODE = 0;
    private static final String TAG = "MainAc sensores";

    //pàra los valores del sms a enviar



    //para el gauge

    private CircleProgress circleProgress;

    //seekbar

   private SeekBar simpleSeekBar;


   private  int ValorMinimoVibration;
    // para el sensor

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    //valores del sensor

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;


    private static final int SHAKE_THRESHOLD = 600;

    //para el intnt Extra info

    public static   String  EXTRA_MESSAGE="mensaje";
    public static   String  EXTRA_TIME="time";
    public static   String  EXTRA_RMNAME="3T CORDOBA";
    public static   String  EXTRA_SMSCONFIGURADO="639689367";


    //PARA PODER HIDE LA PROGRESSBAR DESDE EL ASYNTASK


    private ProgressDialog progressDialog;


    private boolean SendEmailOK=false;//PARASABER SI SE MANDO OK  O NO EL AUTO EMAIL


    //para los edittext


    private EditText srn,email,hospname,otherappname;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //los edittext


        srn = (EditText) findViewById(R.id.srntext);
        email = (EditText) findViewById(R.id.emailtext);
        hospname = (EditText) findViewById(R.id.hospnameedittext);
        otherappname = (EditText) findViewById(R.id.otherapppackagename);



        //el gauge:

        circleProgress = (CircleProgress) findViewById(R.id.circle_progress);
        circleProgress.setMax(150);




        //el seekbar

          simpleSeekBar=(SeekBar) findViewById(R.id.seekBar); // initiate the progress bar
        final TextView ValorSeekBar=(TextView)findViewById(R.id.valorseekbar);


        // perform seek bar change listener event used for getting the progress value
        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

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
                ValorSeekBar.setText(String.valueOf(ValorMinimoVibration)+"%");

            }
        });



          /*Additionally we have two possibilities to keep our application awake.
                First is adding a flag to WindowManager which will keep the screen on with full power:
        */


        Window window = getWindow();

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //CHEQUEO SMS PERMISION

        if (!hasReadSmsPermission()) {
            showRequestPermissionsInfoAlertDialog();
        }



        EXTRA_TIME="mensaje cambiado px si";




        //registro de sensor y variables de sensores

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

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
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
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
                dialog.dismiss();
                requestReadAndSendSmsPermission();
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

                float ValorVibrationclaculada=new Float( Math.abs(difx +dify+difz));



               // absLabel.setText(String.format("ABS: %+2.0f ", (float)Math.round(ValorVibrationclaculada)));

                Log.d( "ValorVibrationcal:",String.format("ABS: %+2.0f ", (float)Math.round(ValorVibrationclaculada)));

                circleProgress.setProgress(Math.round(ValorVibrationclaculada));




                last_x = x;
                last_y = y;
                last_z = z;


                if (ValorVibrationclaculada<ValorMinimoVibration){

                    Log.d(TAG+" MENOR!:",String.valueOf(ValorMinimoVibration));

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
        String otherappnametoreadnotistopass = otherappname.getText().toString();






        //los Valores on correctos asi que los guardamos en le SharedPreference!!!


        SharedPreferences pref = getSharedPreferences(SmsHelper.PREFS_NAME, Context.MODE_PRIVATE);

        // We need an editor object to make changes
        SharedPreferences.Editor edit = pref.edit();

        // Set/Store data
        edit.putString(SmsHelper.PREF_SRN, srntopass);
        edit.putString(SmsHelper.PREF_HOSPNAME, hospitalnametopass);
        edit.putString(SmsHelper.PREF_NAMEAPPTOREADNOTIFICACTIONS, otherappnametoreadnotistopass);
        edit.putString(SmsHelper.PREF_EMAIL, emailtopass);
        edit.putString(SmsHelper.PREF_VALUEVIBRATENOW, "33%");//TODO poner valor real
        edit.putString(SmsHelper.PREF_VALUETRIGGERVIBRATE, "11%");//TODO poner valor real


        edit.putBoolean(SmsHelper.PREF_BOOL_ALARMADEFALLO, false);//TODO  poner valor real



        // Commit the changes
        edit.commit();


        //empieza a girara spinnerr


       // empiezagiraprogressbar();


        Mail m = new Mail("icas.generico@gmail.com", "Sevilla2!");
        //String[] toArr = {"jrdvsoftyopozi@gmail.com"};

        //new Email::

        String[] toArr = {"interamaster@gmail.com"};

        m.setTo(toArr);
        m.setFrom("icas.generico@gmail.com");
        m.setSubject("AVISO COMPRESOR PARADO!!");
        m.setBody("dasdsd");
        /*
        m.setBody("Gracias por enviarnos su email estos son sus datos:\n" + name + "\n" + email + "\n" + telefono + "\n" + comunidad + "\n signature:" + "Start ENCRYPTED:" +
                "%%ADFSDLIFSDLJKHDLKASHDLKHSLKJDHLSDHLKASDKJSKDJJDKJKLDLSKAJDLKAJSDKJLDJLKSJDLKAJSDLKSJADLKJASLDJASKDKJDLKJERIUFH" +
                "KLJHFDKHGJKFHKJHGKJHJKFJKDFHGKJHDFKJGHKJDFHGKJFHJKHJFGKJHFDKJGHKJFDHGJKFHGKJDKHGDFKHGJKFDHGKSHFJGHDFSKGKJFDHGKFH" +
                " DSADASKDJKASJDLKAJDLKJSALKDJLKJKDLJSALKDJASLKDJLKASDJLKSDJKLSJDLKASJDLKJSLKJDLKJDDSJLKADJSLDSJAKSDKLDSLJ" +
                "DNSDJLKAJJKVJKVSDIOUFISODUOIFSJKLKDLSFJLKSDJFLKDSJFKLJSDLKFJKLDSJFLKSJDFLKJSDLKFJKLDJFLKSDJFKLJSDFKLJSDKLFJSDKLF" +"\n \n \n " +
                " el password deberia ser ghfincas+2 utimos digitos del imei"+"\n \n osea: ghfincas");

                */
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

    public void SendEmailtestMio(View view) {




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
