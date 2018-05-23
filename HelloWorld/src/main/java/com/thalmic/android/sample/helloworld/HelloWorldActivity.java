/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.thalmic.android.sample.helloworld;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.scanner.ScanActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class HelloWorldActivity extends Activity {

    File dir = null;
    String[] juzi={"请拒绝打架","我们组织了聚会","大家请起立","大家组织了聚会","请组织聚会","请大家起立","大家起立"};
    String[] wenzi1={"打架","冲压","兰花","组织","点击",
            "冲压","我们","请","椅子","未知",
            "兰花","请","拒绝","未知","兰花",
            "组织","椅子","未知","聚会","兰花",
            "点击","未知","兰花","兰花","点击","大家","起立"};
    int[][][] par={{{11,7},{12},{0}},{{6},{3,15},{18}},{{25},{11,7},{26}},{{25},{3,15},{18}},{{11,7},{3,15},{18}},{{11,7},{25},{26}},{{25},{26}}};
    int[] a;
    static public int h1,h2,v1,v2,p1,p2,pc1,pc2;
    static  public boolean s1,s2,z1=true,z2;
    TextToSpeech speech ;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    Boolean f=false,yes=false;
    Button start,stop;
    public void match(int c){
        boolean bo=false;
        for (int i=0;i<a.length;i++)
        {
            if (bo) break;
            for (int j=0;j<par[i][a[i]].length;j++)
            {
                if (par[i][a[i]][j]==c)
                {
                    a[i]++;
                    if (a[i]==par[i].length)
                    {
                        speech.speak(juzi[i], TextToSpeech.QUEUE_FLUSH, null);
                        bo=true;
                        for (int k=0;k<a.length;k++) a[k]=0;
                    }
                    break;
                }
            }
        }
    }
    private String gettime() {
        Calendar ca = Calendar.getInstance();
        String timeData = DateFormat.format("yyyyMMddkkmmss",ca.getTime()).toString()+" ";
        return timeData;
    }
    private void createDir(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);// 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            Log.d("sd!!!","I an in write"+sdDir);
        }
        String path_str=sdDir.getPath()+"/MYO";
        dir = new File(path_str);
        if (!dir.exists()) {
            //若不存在，创建目录，可以在应用启动的时候创建
            Log.d("no", "createDir: ");
                boolean ismake = dir.mkdirs();
        }
    }
    private TextView myo1name,myo2name;
    private TextView myo1ori,myo2ori,myo1acc,myo1gyr,myo2acc,myo2gyr,pos;
    private ArrayList<Myo> mKnownMyos = new ArrayList<Myo>();

    private MyoAdapter mAdapter;
    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {

        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void  onPose (Myo myo, long timestamp, Pose pose)
        {
            int aa=0;
            if (pose==Pose.FIST) aa=1;
            if (pose==Pose.WAVE_IN) aa=2;
            if (pose==Pose.WAVE_OUT) aa=3;
            if (pose==Pose.FINGERS_SPREAD) aa=4;
            if (pose==Pose.DOUBLE_TAP) aa=5;
            if (identifyMyo(myo)==1){
                myo1name.setText(pose.toString());

                if (!s1&&aa>0)
                {
                    if (s2)
                    {

                            s2 = false;
                            pc1 = -50;
                            pc2 = -50;
                            pos.setText(wenzi1[aa * 5 + p2 - 6]);
                            h1 = 50;
                            if (f) match(aa * 5 + p2 - 6);
                            else
                            if (z1) {
                                speech.speak(wenzi1[aa * 5 + p2 - 6], TextToSpeech.QUEUE_FLUSH, null);
                        }
                        z1 = false;
                    }
                    else {
                        s1 = true;
                        p1 = aa;
                        pc1=50;
                    }
                }

            }
            else
            {
                myo2name.setText(pose.toString());
                if (!s2&&aa>0)
                {
                    if (s1)
                    {

                            h1 = 50;
                            s1 = false;
                            pc1 = -50;
                            pc2 = -50;
                            pos.setText(wenzi1[p1 * 5 + aa - 6]);
                            if (f) match(p1 * 5 + aa - 6);
                            else
                            if (z1) {
                                speech.speak(wenzi1[p1 * 5 + aa - 6], TextToSpeech.QUEUE_FLUSH, null);
                        }
                        z1 = false;
                    }
                    else {
                        s2 = true;
                        p2 = aa;
                        pc2=50;
                    }
                }
            }
        }
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            mKnownMyos.add(myo);

            // Now that we've added it to our list, get our short ID for it and print it out.
            Log.i("hhh", "Attached to " + myo.getMacAddress() + ", now known as Myo " + identifyMyo(myo) + ".");
            if (identifyMyo(myo)==1) {
                myo1name.setTextColor(Color.CYAN);
                myo1name.setText(myo.getName());
            }
            else {
                yes=true;
                myo2name.setTextColor(Color.CYAN);
                myo2name.setText(myo.getName());
            }
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            if (identifyMyo(myo)==1)
                myo1name.setTextColor(Color.RED);
            else
                myo2name.setTextColor(Color.RED);
        }

        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.


        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.


        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onAccelerometerData (Myo myo, long timestamp, Vector3 accel)
        {
            float x=(float) Math.toDegrees(accel.x());
            float y=(float) Math.toDegrees(accel.y());
            float z=(float) Math.toDegrees(accel.z());
            String s="acc:\nx:"+x+"\ny:"+y+"\nz:"+z;
            String s1=x+" "+y+" "+z+'\n';
            String s2;
            h1=h1-1;
            if (h1==0) z1=true;
            if (identifyMyo(myo)==1) {
                myo1acc.setText(s);
                s2 = "myo1acc";
                pc1=pc1-1;
                if (pc1==0) {
                    if (p1 == 4) {
                        if (f) match(25);
                        else speech.speak(wenzi1[25], TextToSpeech.QUEUE_FLUSH, null);
                        pos.setText(wenzi1[25]);
                    } else if (p1 == 1) {
                        if (f) match(26);
                        else speech.speak(wenzi1[26], TextToSpeech.QUEUE_FLUSH, null);
                        pos.setText(wenzi1[26]);
                    }
                }
                if (pc1==0) HelloWorldActivity.s1=false;
            }
            else {
                myo2acc.setText(s);
                s2 = "myo2acc";
                pc2=pc2-1;
                if (pc2==0) {
                    if (p2 == 4) {
                        if (f) match(25);
                        else speech.speak(wenzi1[25], TextToSpeech.QUEUE_FLUSH, null);
                        pos.setText(wenzi1[25]);
                    } else if (p2 == 1) {
                        if (f) match(26);
                        else speech.speak(wenzi1[26], TextToSpeech.QUEUE_FLUSH, null);
                        pos.setText(wenzi1[26]);
                    }
                }
                if (pc2==0) HelloWorldActivity.s2=false;
            }
            if (f && yes) writedata(s1,s2);
        }
        @Override
        public void onGyroscopeData (Myo myo, long timestamp, Vector3 gyro)
        {
            float x=(float) Math.toDegrees(gyro.x());
            float y=(float) Math.toDegrees(gyro.y());
            float z=(float) Math.toDegrees(gyro.z());
            String s="gyr:\nx:"+x+"\ny:"+y+"\nz:"+z;
            String s1=x+" "+y+" "+z+'\n';
            String s2;
            if (identifyMyo(myo)==1) {
                myo1gyr.setText(s);
                s2 = "myo1gyr";
            }
            else {
                myo2gyr.setText(s);
                s2 = "myo2gyr";
            }
            if (f && yes)
            {
                writedata(s1,s2);
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if we have write permission
        Log.d(par.length+"", "match: ");
        a=new int[par.length];
        int permission = ActivityCompat.checkSelfPermission(HelloWorldActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(HelloWorldActivity.this, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
        createDir();
        setContentView(R.layout.activity_hello_world);
        speech= new TextToSpeech(HelloWorldActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d(status+"", "onInit: ");
                if (status == TextToSpeech.SUCCESS) {
                    int result = speech.setLanguage(Locale.CHINESE);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        if (BuildConfig.DEBUG) Log.d("MainActivity", "朗读出现错误...");
                    } else {
                        speech.speak("您有新的美团外卖订单，请及时处理！", TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
        });
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        stop.setEnabled(false);
        pos = (TextView) findViewById(R.id.pos);
        myo1name = (TextView) findViewById(R.id.myo1name);
        myo1acc = (TextView) findViewById(R.id.myo1acc);
        myo1gyr = (TextView) findViewById(R.id.myo1gyr);
        myo2name = (TextView) findViewById(R.id.myo2name);
        myo2acc = (TextView) findViewById(R.id.myo2acc);
        myo2gyr = (TextView) findViewById(R.id.myo2gyr);
        start.setBackgroundColor(Color.BLUE);
        stop.setBackgroundColor(Color.GRAY);
        start.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast toast=Toast.makeText(HelloWorldActivity.this,"start!",Toast.LENGTH_SHORT);
                toast.show();
                f=true;
                start.setBackgroundColor(Color.GRAY);
                stop.setBackgroundColor(Color.BLUE);
                stop.setEnabled(true);
                start.setEnabled(false);
            }
        });
        stop.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Toast toast=Toast.makeText(HelloWorldActivity.this,"stop!",Toast.LENGTH_SHORT);
                toast.show();
                // TODO Auto-generated method stub
                f=false;
                start.setBackgroundColor(Color.BLUE);
                stop.setBackgroundColor(Color.GRAY);
                stop.setEnabled(false);
                start.setEnabled(true);
            }
        });
        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        final int attachingCount=2;
        hub.setLockingPolicy(Hub.LockingPolicy.NONE);
        hub.setMyoAttachAllowance(attachingCount);
        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Activity is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);

        if (isFinishing()) {
            // The Activity is finishing, so shutdown the Hub. This will disconnect from the Myo.
            Hub.getInstance().shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.action_scan == id) {
            onScanActionSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onScanActionSelected() {
        // Launch the ScanActivity to scan for Myos to connect to.
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }
    private void writedata(String string,String s2){
        Log.d("SensorWrite","I an in write");
        String fileName = null;
        fileName = s2+".txt";

        if (!dir.exists())
            dir.mkdir();

        if (dir.exists() && dir.canWrite()) {
            Log.d("can","can write");
            File newFile = new File(dir.getAbsolutePath() + "/" + fileName);
            FileOutputStream fos = null;
            try {
                newFile.createNewFile();
                if (newFile.exists() && newFile.canWrite()) {
                    fos = new FileOutputStream(newFile,true);
                    fos.write((string+"\n").getBytes());

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try{
                        fos.flush();
                        fos.close();
                    }
                    catch (IOException e) { }
                }
            }
        }
    }
    private int identifyMyo(Myo myo) {
        return mKnownMyos.indexOf(myo) + 1;
    }
    private class MyoAdapter extends ArrayAdapter<String> {

        public MyoAdapter(Context context, int count) {
            super(context, android.R.layout.simple_list_item_1);

            // Initialize adapter with items for each expected Myo.
            for (int i = 0; i < count; i++) {
                add(getString(R.string.waiting_message));
            }
        }

        public void setMessage(Myo myo, String message) {
            // identifyMyo returns IDs starting at 1, but the adapter indices start at 0.
            int index = identifyMyo(myo) - 1;

            // Replace the message.
            remove(getItem(index));
            insert(message, index);
        }
    }
}
