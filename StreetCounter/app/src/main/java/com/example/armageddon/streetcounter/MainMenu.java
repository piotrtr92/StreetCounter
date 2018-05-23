package com.example.armageddon.streetcounter;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainMenu extends AppCompatActivity {

    DBSQLite dbsqLite;
    private StopWatch_Service mService;
    private BroadcastReceiver broadcastReceiver,broadcastReceiver2,bcSaved;
    TextView tx1;
    boolean isRunned;
    Button bt1,bt2,bt3,bt4;
    ArrayList<String> dates;
    EditText mEtLaps; //laps text view
    ScrollView mSvLaps;
    boolean mBound = false;
    int mLapCounter = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dates=new ArrayList<String>();
        dbsqLite=new DBSQLite(this);
        tx1=(TextView)findViewById(R.id.StopWatch);
        bt1=(Button)findViewById(R.id.start);
        bt2=(Button)findViewById(R.id.stop);
        bt3=(Button)findViewById(R.id.laps);
        tx1.setText("00:00:00:00");
        bt2.setEnabled(false);
        bt2.setTextColor(Color.RED);
        bt3.setEnabled(false);
        bt3.setTextColor(Color.RED);
        mEtLaps = (EditText) findViewById(R.id.saveResult);
        mEtLaps.setEnabled(false); //prevent the et_laps to be editable

        mSvLaps = (ScrollView) findViewById(R.id.scrolled);

        if(bcSaved== null){
            bcSaved = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String text= intent.getExtras().get("getText").toString();
                    dates=intent.getStringArrayListExtra("getArr");
                    for(int i =0;i<dates.size();i++){
                        mEtLaps.append("LAP " + String.valueOf(mLapCounter++)
                                + "   " + dates.get(i) + "\n");
                        mSvLaps.post(new Runnable() {
                            @Override
                            public void run() {
                                mSvLaps.smoothScrollTo(0, mEtLaps.getBottom());
                            }
                        });
                    }
                    tx1.setText(text);
                }
            };
        }
        registerReceiver(bcSaved,new IntentFilter("textView"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.saveAs) {
            return true;
        }else if(id==R.id.saveMeasure){
            return true;
        }else if(id==R.id.MeasureList)
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean isRunning= intent.getExtras().getBoolean("mIsRun");
                    isRunned=isRunning;
                    if(isRunning==true){
                        String dane=intent.getExtras().get("stopwatch").toString();
                        tx1.setText(dane);
                        bt1.setEnabled(false);
                        bt1.setTextColor(Color.RED);
                        bt2.setEnabled(true);
                        bt2.setTextColor(Color.WHITE);
                        bt3.setEnabled(true);
                        bt3.setTextColor(Color.WHITE);
                    }else{
                        bt1.setEnabled(true);
                        bt1.setTextColor(Color.WHITE);
                        bt2.setEnabled(false);
                        bt2.setTextColor(Color.RED);
                        if(tx1.getText().toString()!="00:00:00:00"){
                            bt3.setEnabled(true);
                            bt3.setTextColor(Color.WHITE);
                        }else {
                            bt3.setEnabled(false);
                            bt3.setTextColor(Color.RED);
                        }
                    }
                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("timer"));


        Intent intent=new Intent(this,SaveData_Service.class);
        stopService(intent);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, StopWatch_Service.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

   /* @Override
    protected void onStop() {
        super.onStop();
        //unregisterReceiver(cashbackReciver);
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Write your message here.");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        System.exit(0);
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }*/

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            StopWatch_Service.LocalBinder binder = (StopWatch_Service.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void reseted(View view) {
        tx1.setText("00:00:00:00");
        if (mBound) {
            mService.restartTime();
        }
        mEtLaps.setText("");
        dates.clear();
    }
    public void stoped(View view) {

        if (mBound) {
            mService.stopTime();
            bt1.setEnabled(true);
            bt1.setTextColor(Color.WHITE);
            bt2.setEnabled(false);
            bt2.setTextColor(Color.RED);
            if(tx1.getText().toString()!="00:00:00:00"){
                bt3.setEnabled(true);
                bt3.setTextColor(Color.WHITE);
            }else {
                bt3.setEnabled(false);
                bt3.setTextColor(Color.RED);
            }
        }
    }
    public void started(View view) {
        if (mBound) {
            mService.startTime();
            bt1.setEnabled(false);
            bt1.setTextColor(Color.RED);
            bt2.setEnabled(true);
            bt2.setTextColor(Color.WHITE);
            bt3.setEnabled(true);
            bt3.setTextColor(Color.WHITE);
        }
    }
    public void lap(View view) {
        String dane=tx1.getText().toString();
        dates.add(dane);
        mEtLaps.append("LAP " + String.valueOf(mLapCounter++)
                + "   " + dane + "\n");
        mSvLaps.post(new Runnable() {
            @Override
            public void run() {
                mSvLaps.smoothScrollTo(0, mEtLaps.getBottom());
            }
        });
        tx1.setText("00:00:00:00");
        if (mBound) {
            mService.restartTime();
        }
    }


    public void GetCamera(View view) {
        Cursor cur=dbsqLite.getAllData();
        if(cur.getCount()>0) {
            String dane = tx1.getText().toString();
            Intent intent = new Intent();
            intent.setClass(this, SaveData_Service.class);
            intent.putExtra("TextView", dane);
            intent.putStringArrayListExtra("arrDate", dates);
            startService(intent);

            Intent inten = new Intent(MainMenu.this, CamImg.class);
            startActivity(inten);
        }else
            Toast.makeText(this,"Utwórz projekt",Toast.LENGTH_LONG).show();
    }

    public void saveAs(MenuItem item) {
        String dane=tx1.getText().toString();
        Intent intent =  new Intent();
        intent.setClass(this, SaveData_Service.class);
        intent.putExtra("TextView",dane);
        intent.putStringArrayListExtra("arrDate",dates);
        startService(intent);
        Intent i=new Intent(MainMenu.this,SaveAs.class);
        startActivity(i);
    }

    public void saveList(MenuItem item) {
        Cursor cur=dbsqLite.getAllData();
        if(dates.isEmpty()){
            Toast.makeText(this,"Nie masz żadnych pomiarów",Toast.LENGTH_LONG).show();
        }else if(cur.getCount()==0){
            Toast.makeText(this,"Utwórz projekt",Toast.LENGTH_LONG).show();
        }else if(!dates.isEmpty() && cur.getCount()!=0){
            if(dbsqLite.insertValues(dates)){
                Toast.makeText(this,"Dodano pomiary",Toast.LENGTH_LONG).show();
                dates.clear();
                mEtLaps.setText("");
                mSvLaps.post(new Runnable() {
                    @Override
                    public void run() {
                        mSvLaps.smoothScrollTo(0, mEtLaps.getBottom());
                    }
                });

            }else{
                Toast.makeText(this,"Błąd dodawania",Toast.LENGTH_LONG).show();
            }
        }
    }

    public void showList(MenuItem item) {
        String dane=tx1.getText().toString();
        Intent intent =  new Intent();
        intent.setClass(this, SaveData_Service.class);
        intent.putExtra("TextView",dane);
        intent.putStringArrayListExtra("arrDate",dates);
        startService(intent);
        Intent inten=new Intent(this,Measure_list.class);
        startActivity(inten);
    }

    public void getGallery(View view) {
        String dane=tx1.getText().toString();
        Intent intent =  new Intent();
        intent.setClass(this, SaveData_Service.class);
        intent.putExtra("TextView",dane);
        intent.putStringArrayListExtra("arrDate",dates);
        startService(intent);
        Intent i=new Intent(MainMenu.this,GalleryImages.class);
        startActivity(i);
    }
}