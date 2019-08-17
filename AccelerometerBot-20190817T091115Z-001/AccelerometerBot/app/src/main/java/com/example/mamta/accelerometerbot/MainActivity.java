package com.example.mamta.accelerometerbot;

import static com.example.mamta.accelerometerbot.R.id.*;
import static com.example.mamta.accelerometerbot.R.drawable.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // EditText editTextAddress, editTextPort;
    Button btnconnection;
    TextView textViewRx;
    ImageView img;
    RelativeLayout rlayout;
    SensorManager sm = null;
    boolean connectState = false;

    ClientHandler clientHandler;
    ClientThread clientThread;
    float[] values;
    Boolean datasend = false;
    SensorManager sensorManager;
    Sensor senAcc;


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            values = event.values;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, senAcc, SensorManager.SENSOR_DELAY_NORMAL);
        textViewRx = (TextView) findViewById(received);

        img = (ImageView) findViewById(R.id.logo);
        rlayout = (RelativeLayout) findViewById(layout);
        rlayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.e("screen HEIGHT", "onCreate: " + rlayout.getHeight());
                rlayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (rlayout.getLayoutParams().height > 700) {
                        textViewRx.setTextAppearance(R.style.TextAppearance_AppCompat_Display3);
                    }

                }
            }
        });

        btnconnection = (Button) findViewById(CONNECTION);
        btnconnection.setText("START");
        btnconnection.setBackgroundResource(round_button_green);


        btnconnection.setOnClickListener(buttonConnectOnClickListener);


        clientHandler = new ClientHandler(this);
    }

    View.OnClickListener buttonConnectOnClickListener =
            new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (connectState == false) {
                        connectState = true;
                        btnconnection.setText("STOP");
                        btnconnection.setBackgroundResource(round_button_red);
                        clientThread = new ClientThread(
                                "192.168.4.1",
                                80,
                                clientHandler);

                        clientThread.start();

                        datasend = true;
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                while (datasend) {
                                    if (clientThread != null) {
                                        String msgToSend = "" + (int) values[0] + "," + (int) values[1] + "," + (int) values[2]+ "\n";

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                textViewRx.setText("x: " + (int) values[0] + "\ny: " + (int) values[1] + "\nz: " + (int) values[2]);
                                            }
                                        });


                                        clientThread.txMsg(msgToSend);
                                        try {
                                            Thread.sleep(150);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            }
                        });
                    } else {
                        if (clientThread != null) {
                            datasend = false;

                            clientThread.setRunning(false);
                            clientThread.closeConnection();
                            clientThread = null;

                        }
                    }


                }
            };


    private void updateState(String state) {
    }

    private void updateRxMsg(String rxmsg) {
        textViewRx.append(rxmsg + "\n");
    }


    private void clientEnd() {
        connectState = false;
        btnconnection.setBackgroundResource(round_button_green);
        btnconnection.setText("START");
        clientThread = null;

    }

    public static class ClientHandler extends Handler {
        public static final int UPDATE_STATE = 0;
        public static final int UPDATE_MSG = 1;
        public static final int UPDATE_END = 2;
        private MainActivity parent;

        public ClientHandler(MainActivity parent) {
            super();
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_STATE:
                    parent.updateState((String) msg.obj);
                    break;
                case UPDATE_MSG:
                    parent.updateRxMsg((String) msg.obj);
                    break;
                case UPDATE_END:
                    parent.clientEnd();
                    break;
                default:
                    super.handleMessage(msg);
            }

        }

    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(this);
        super.onStop();

    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, senAcc, SensorManager.SENSOR_DELAY_NORMAL);
    }
}




