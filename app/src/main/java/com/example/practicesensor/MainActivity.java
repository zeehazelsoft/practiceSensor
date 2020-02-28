package com.example.practicesensor;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    final String TAG = getClass().getName().toString();
    SensorManager mSensorManager;
    Sensor mAccelerometer;
    TableLayout accTable;
    TextView accl, spd, spd_kmph;
    Button btnStart, btnStop, btnClear;
    Timer updateTimer;
    float []linearAcceleration = new float[3];
    Velocity velocity;
    Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSensor();

        accTable =(TableLayout)findViewById(R.id.country_table);

        //accl = (TextView)findViewById(R.id.accl);
        spd = (TextView)findViewById(R.id.spd);
        spd_kmph = (TextView)findViewById(R.id.spd_kmph);

        btnStart = (Button)findViewById(R.id.buttonStart);
        btnStart.setOnClickListener(this);
        btnStop = (Button)findViewById(R.id.buttonStop);
        btnStop.setOnClickListener(this);
        btnClear= (Button)findViewById(R.id.buttonClear);
        btnClear.setOnClickListener(this);
    }

    private void initSensor() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if(mAccelerometer == null) {
            Toast.makeText(this, "Accelerometer sensor not available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    void fillTable(float values[]) {

        float[] val = values;
        TableRow row;
        TextView t1, t2, t3;
        //Converting to dip unit
        int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 1, getResources().getDisplayMetrics());

        //for (int current = 0; current < CountriesList.abbreviations.length; current++) {
        row = new TableRow(this);

        t1 = new TextView(this);
        t1.setTextColor(Color.WHITE);
        t1.setBackgroundColor(Color.GRAY);
        t2 = new TextView(this);
        t2.setTextColor(Color.WHITE);
        t2.setBackgroundColor(Color.LTGRAY);
        t3 = new TextView(this);
        t3.setTextColor(Color.WHITE);
        t3.setBackgroundColor(Color.GRAY);

        t1.setText(""+val[0]);
        t2.setText(""+val[1]);
        t3.setText(""+val[2]);

        t1.setTypeface(null, Typeface.BOLD);
        t2.setTypeface(null, Typeface.BOLD);
        t3.setTypeface(null, Typeface.BOLD);

        t1.setTextSize(15);
        t2.setTextSize(15);
        t3.setTextSize(15);

        t1.setWidth(150 * dip);
        t2.setWidth(150 * dip);
        t3.setWidth(150 * dip);
        t1.setPadding(20*dip, 0, 0, 0);
        row.addView(t1);
        row.addView(t2);
        row.addView(t3);

        accTable.addView(row, new TableLayout.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT));

    }

    public void onClick(View v) {

        if(v == btnStart) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            velocity = new Velocity();
            updateTimer = new Timer("velocityUpdate");
            handler = new Handler();
            updateTimer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    calculateAndUpdate();
                }
            }, 0, 1200);
        }else  if(v == btnStop) {
            mSensorManager.unregisterListener(this);

            displayVelocityValues();
            displayVelocityTable();
            velocity = null;
            handler = null;
            updateTimer.cancel();


        } else if(v == btnClear) {
            accTable.removeAllViews();
        }
    }

    private void displayVelocityTable() {
        try {
            accTable.removeAllViews();
            double[] vl = velocity.getVlArray();
            for(int i = 0; i<vl.length; i++) {
                /*Log.d(TAG, "v = " + vl[i] + "mps, "+(vl[i] * 3.6)+ " kmph");*/
                //float[] val = values;
                TableRow row;
                TextView t1, t2;
                //Converting to dip unit
                int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        (float) 1, getResources().getDisplayMetrics());

                //for (int current = 0; current < CountriesList.abbreviations.length; current++) {
                row = new TableRow(this);

                t1 = new TextView(this);
                t1.setTextColor(Color.WHITE);
                t1.setBackgroundColor(Color.GRAY);
                t2 = new TextView(this);
                t2.setTextColor(Color.WHITE);
                t2.setBackgroundColor(Color.LTGRAY);
                t1.setText(""+vl[i]);
                t2.setText(""+(vl[i] * 3.6));
                t1.setTypeface(null, Typeface.BOLD);
                t2.setTypeface(null, Typeface.BOLD);
                t1.setTextSize(15);
                t2.setTextSize(15);
                t1.setWidth(200 * dip);
                t2.setWidth(200 * dip);

                t1.setPadding(20*dip, 0, 0, 0);
                row.addView(t1);
                row.addView(t2);


                accTable.addView(row, new TableLayout.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT));
            }
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void displayVelocityValues() {
        try {
            double[] vl = velocity.getVlArray();
            for(int i = 0; i<vl.length; i++) {
                Log.d(TAG, "v = " + vl[i] + "mps, "+(vl[i] * 3.6)+ " kmph");
            }
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void calculateAndUpdate() {

        final double vel = velocity.getVelocity(linearAcceleration, System.currentTimeMillis());
        final double velKmph = vel * 3.6;
        //spd.setText("v = "+ velKmph + " kmph");

        handler.post(new Runnable() {
            public void run() {

                //Log.d(getClass().getName().toString(), "Setting velocity = " + velKmph+ " kmph");
                spd.setText("v = "+ vel + " mps");
                spd_kmph.setText("v = "+ velKmph + " kmph");
            }
        });
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        linearAcceleration[0] = event.values[0];
        linearAcceleration[1] = event.values[1];
        linearAcceleration[2] = event.values[2];

        fillTable(linearAcceleration);
    }
}
class Velocity {

    private final String TAG = getClass().getName().toString();
    int sampleCounter = 0;
    final int totalSamples = 5;
    long time0, nAccel;
    static int i=0;
    double aDelT0 = 0, v0 = 0, v = 0;

    final int totalVelocityValues = 1000;
    double []velocityValues = new double[totalVelocityValues];

    //float []linearAcceleration = new float[3];

    //final int totalAccl = 5;
    double []accel = new double[totalSamples];

    private double getAvg(double[] a) {
        double total = 0;
        for(int i = 0; i<a.length; i++)
            total = total + a[i];
        return (total / a.length);
    }

    private double getAcceleration(float[] linearAcceleration) {
        return Math.sqrt(Math.pow(linearAcceleration[0], 2) + Math.pow(linearAcceleration[0], 2) + Math.pow(linearAcceleration[0], 2));
    }

    public double getVelocity(float[] linearAcceleration, long time1) {

        //this.linearAcceleration = linearAcceleration;

        try {
            if(sampleCounter < (totalSamples-1)) {
                if(sampleCounter == 0)
                    time0 = time1;
                accel[sampleCounter] = getAcceleration(linearAcceleration);
                sampleCounter++;
            } else if(sampleCounter == (totalSamples-1)) {
                accel[sampleCounter] = getAcceleration(linearAcceleration);

                double avgAccel = getAvg(accel);
                long timeDelta = ((time1 - time0) / 1000);
                double aDelT1 = (avgAccel * timeDelta);
                Log.d(TAG, "aDelT1 = "+avgAccel +" * "+timeDelta + " = "+aDelT1 );

                v = calculateVelovity(aDelT1);
                if(i !=totalVelocityValues) {
                    velocityValues[i]=v;
                    i++;
                } else {
                    for(int j=0;j<(totalVelocityValues-1);j++)
                        velocityValues[j]=velocityValues[j+1];
                    velocityValues[totalVelocityValues -1]=v;
                }
                sampleCounter = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    private double calculateVelovity(double aDelT1) {
        double v = v0 + (aDelT1 - aDelT0);
        Log.d(TAG, "v = "+v0+ "+ ("+aDelT1+" - "+aDelT0+") = "+v);
        v0 = v;
        aDelT0 = aDelT1;
        return v;
    }



    public double[] getVlArray() {
        return velocityValues;
    }
}
