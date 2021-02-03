package com.survey7.cameraupload_full;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.hardware.Camera;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import java.lang.Runnable;
import android.os.Handler;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import android.util.Base64;
import android.os.PowerManager;
import android.net.wifi.WifiManager;


public class Waiting extends Activity {
 private Globals globals;
 private long count=0;
 private Intent intentpulse=null;
 private boolean ischildrunning;

 private WifiManager WM;

 private int savedbrightness=0;
 private boolean oktosleep;
 private boolean inwakelock=false;
 private PowerManager PM;
 private PowerManager.WakeLock wl;
 private Handler handler;

 private void startwakelock() {
  if (inwakelock) return;
  inwakelock=true;
  wl.acquire();
 }
 private void stopwakelock() {
  if (!inwakelock) return;
  inwakelock=false;
  wl.release();
 }

 @Override
 public void onConfigurationChanged(Configuration newConfig) {
  super.onConfigurationChanged(newConfig);
 }
 @Override
 public void onCreate(Bundle savedInstanceState) {
  globals=(Globals)((globalcontainer)getApplicationContext()).globals;
  globals.loadpreferences(this);
  super.onCreate(savedInstanceState);
  handler=new Handler();
  setContentView(R.layout.waiting);
  filllayout();
  Alarmreceiver.startalarm(this,1000*globals.sec_initialdelay,1000*globals.periodseconds);

  WM=(WifiManager)getSystemService(WIFI_SERVICE);
  PM=(PowerManager)getSystemService(POWER_SERVICE);
  wl=PM.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, Waiting.class.getPackage().getName());

  if (globals.dimscreen) {
   try {
    android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    savedbrightness=android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
    android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, 0);
   } catch (android.provider.Settings.SettingNotFoundException err) { }
  }


  globals.uf=new UploadFile();
  globals.uf.foldername=globals.foldername;
  globals.uf.folderdriveid=null;

  final Context ctx=this;
  ((Button)findViewById(R.id.stop_btn)).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
     Alarmreceiver.stopalarm(ctx);
     finish();
    }
  });
  do {} while (false);
 }
 @Override
 public void onDestroy() {
  Alarmreceiver.stopalarm(this);
  handler.removeCallbacksAndMessages(null);

  restorebrightness();

  super.onDestroy();
 }
 @Override
 public void onNewIntent(Intent intent) {
  do {} while (false);
  if (intent.getLongExtra("count",0L)>count) {
   intentpulse=intent;
   startwakelock();
  }
  super.onNewIntent(intent);
 }
 @Override
 public void onResume() {
  do {} while (false);
  Intent intent=intentpulse;
  super.onResume();

  filllayout();
  intentpulse=null;
  if (intent==null) {

   if (globals.turnoff) sleepscreen();

   return;
  }
  count=intent.getLongExtra("count",0L);
  if (ischildrunning) return;

  do {} while (false);
  intent=new Intent(this,Takeshots.class);
  ischildrunning=true;

  if (globals.autostartwifi) {
   if (!WM.isWifiEnabled()) WM.setWifiEnabled(true);
  }

  startActivityForResult(intent,42);
 }
 @Override
 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  do {} while (false);
  if (requestCode!=42) return;

  if (globals.autostopwifi) {
   if (WM.isWifiEnabled()) WM.setWifiEnabled(false);
  }

  if (resultCode==Activity.RESULT_OK) {
   oktosleep=true;
   ischildrunning=false;
   if (0==globals.periodseconds) {
    if (globals.autostart) {
     handler.postDelayed(new Runnable() { public void run() { finish(); }},2000);
     ((TextView)findViewById(R.id.waiting_textview)).setText("Finished and auto-closing");
    } else {
     ((TextView)findViewById(R.id.waiting_textview)).setText("Finished");
    }
   }
  } else if (resultCode==Activity.RESULT_CANCELED) {
   ischildrunning=false;
   Log.e(globals.DBG,"cameraupload.waiting.onActivityResult child was canceled");
  }
  stopwakelock();
 }

 private void filllayout() {
  String s;
  s=globals.getlaststatus();
  if (ischildrunning) s="Child is still running";
  ((TextView)findViewById(R.id.status_textview)).setText(s);
  ((TextView)findViewById(R.id.count_textview)).setText("Count so far: "+count);
 }

 private void restorebrightness() {
  if (!globals.dimscreen) return;
  android.provider.Settings.System.putInt(getContentResolver(),android.provider.Settings.System.SCREEN_BRIGHTNESS,savedbrightness);
 }


 private void sleepscreen() {
  if (!oktosleep) return;
  oktosleep=false;
  startActivity((new Intent(this,Turnoff.class)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
 }

}
