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
import android.widget.LinearLayout;
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


public class Main extends Activity {
 private Globals globals;
 private Handler handler;
 private Runnable pulse_runnable=new Runnable() { public void run() { pulse(); } };
 private int autostartfuse;
 private boolean inwakelock=false;
 private PowerManager PM;
 private PowerManager.WakeLock wl;

 private void startwakelock() {
  if (!globals.autostart && !globals.fullscreen) return;
  if (inwakelock) return;
  inwakelock=true;
  wl.acquire();
 }
 private void stopwakelock() {
  if (!inwakelock) return;
  if (globals.fullscreen) return;
  inwakelock=false;
  wl.release();
 }
 private void reconsiderwakelock() {
  startwakelock();
  stopwakelock();
 }
 private void startintent(Class cl) {
    Intent intent=new Intent();
    intent.setClass(this,cl);
    startActivity(intent);
  }

 @Override
 public void onConfigurationChanged(Configuration newConfig) {
  super.onConfigurationChanged(newConfig);
 }
 @Override
 public void onCreate(Bundle savedInstanceState) {
  globals=(Globals)((globalcontainer)getApplicationContext()).globals;
  super.onCreate(savedInstanceState);
  if (!isTaskRoot()) { finish(); return; }
  PM=(PowerManager)getSystemService(POWER_SERVICE);
  wl=PM.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.ON_AFTER_RELEASE, Main.class.getPackage().getName());
  handler=new Handler();
  globals.loadpreferences(this);
  setandfilllayout();
 }
 private void setupcheckedtextview(CheckedTextView ctv, View.OnClickListener ocl, boolean isselected) {
  ctv.setOnClickListener(ocl);
  ctv.setChecked(isselected);
 }
 private void pulse() {

  if (!globals.autostart) return;
  autostartfuse--;
  if (0==autostartfuse) {
   startintent(Waiting.class);
   finish();
  } else {
   ((Button)findViewById(R.id.start_btn)).setText("Cancel Autostart in "+autostartfuse+" seconds");
   handler.postDelayed(pulse_runnable,1000);
  }
 }
 private void saveoptions(Context context) {
  switch ( ((Spinner)findViewById(R.id.whatrepeat_spinner)).getSelectedItemPosition() ) {
   case 0: globals.periodseconds=0; break;
   case 1: globals.periodseconds=60; break;
   case 2: globals.periodseconds=5*60; break;
   case 3: globals.periodseconds=10*60; break;
   case 4: globals.periodseconds=15*60; break;
   case 5: globals.periodseconds=20*60; break;
   case 6: globals.periodseconds=30*60; break;
   case 7: globals.periodseconds=60*60; break;
   case 8: globals.periodseconds=2*60*60; break;
   case 9: globals.periodseconds=4*60*60; break;
   case 10: globals.periodseconds=8*60*60; break;
   case 11: globals.periodseconds=12*60*60; break;
   case 12: globals.periodseconds=24*60*60; break;
  }

  switch ( ((Spinner)findViewById(R.id.whichcamera_spinner)).getSelectedItemPosition() ) {
   case 0: globals.takemainshot=true; globals.takefrontshot=false; break;
   case 1: globals.takemainshot=false; globals.takefrontshot=true; break;
   case 2: globals.takemainshot=true; globals.takefrontshot=true; break;
  }

  globals.maxpixels_front=((Spinner)findViewById(R.id.maxpixels_front_spinner)).getSelectedItemPosition();
  globals.maxpixels_main=((Spinner)findViewById(R.id.maxpixels_main_spinner)).getSelectedItemPosition();

  globals.uploadurl=((EditText)findViewById(R.id.url_edittext)).getText().toString();
  globals.basicauthorization=((EditText)findViewById(R.id.authorization_edittext)).getText().toString();
  globals.msec_focustime=Integer.parseInt(((EditText)findViewById(R.id.focustime_edittext)).getText().toString());
  globals.sec_initialdelay=Integer.parseInt(((EditText)findViewById(R.id.initialdelay_edittext)).getText().toString());
  globals.foldername=((EditText)findViewById(R.id.foldername_edittext)).getText().toString();
  if (globals.foldername.equals("")) globals.foldername=globals.default_foldername;
  globals.trimnumber=Integer.parseInt(((EditText)findViewById(R.id.trimnumber_edittext)).getText().toString());
  globals.cameraname=((EditText)findViewById(R.id.cameraname_edittext)).getText().toString();

  if (!globals.uploadurl.startsWith("http://") && !globals.uploadurl.startsWith("https://"))
    globals.uploadurl="http://"+globals.uploadurl;
  globals.cameraname=globals.cameraname.replaceAll("[^a-zA-Z0-9]","_");

  globals.waitforwifi=((CheckedTextView)findViewById(R.id.waitforwifi_checkedtext)).isChecked();
  globals.autostartwifi=((CheckedTextView)findViewById(R.id.autostartwifi_checkedtext)).isChecked();
  globals.autostopwifi=((CheckedTextView)findViewById(R.id.autostopwifi_checkedtext)).isChecked();
  globals.fullscreen=((CheckedTextView)findViewById(R.id.fullscreen_checkedtext)).isChecked();
  globals.fullscreen=((CheckedTextView)findViewById(R.id.fullscreen_checkedtext)).isChecked();
  globals.dimscreen=((CheckedTextView)findViewById(R.id.dimscreen_checkedtext)).isChecked();
  globals.turnoff=((CheckedTextView)findViewById(R.id.turnoff_checkedtext)).isChecked();
  globals.uploadpost=((CheckedTextView)findViewById(R.id.uploadpost_checkedtext)).isChecked();
  globals.uploaddrive=((CheckedTextView)findViewById(R.id.uploaddrive_checkedtext)).isChecked();
  globals.deletetrash=((CheckedTextView)findViewById(R.id.deletetrash_checkedtext)).isChecked();
  globals.forceflash=((CheckedTextView)findViewById(R.id.forceflash_checkedtext)).isChecked();
  globals.uploadstorage=((CheckedTextView)findViewById(R.id.uploadstorage_checkedtext)).isChecked();
  globals.waitforcomplete=((CheckedTextView)findViewById(R.id.waitforcomplete_checkedtext)).isChecked();
  globals.autostart=((CheckedTextView)findViewById(R.id.autostart_checkedtext)).isChecked();

  globals.savepreferences(context);

 }

 private void setandfilllayout() {
  final Context context=this;
  int i;
  setContentView(R.layout.options);

  ((EditText)findViewById(R.id.url_edittext)).setText(globals.uploadurl);
  ((EditText)findViewById(R.id.authorization_edittext)).setText(globals.basicauthorization);
  ((EditText)findViewById(R.id.focustime_edittext)).setText(String.valueOf(globals.msec_focustime));
  ((EditText)findViewById(R.id.initialdelay_edittext)).setText(String.valueOf(globals.sec_initialdelay));
  ((EditText)findViewById(R.id.foldername_edittext)).setText(globals.foldername);
  ((EditText)findViewById(R.id.trimnumber_edittext)).setText(String.valueOf(globals.trimnumber));
  ((EditText)findViewById(R.id.cameraname_edittext)).setText(globals.cameraname);

  switch (globals.periodseconds) {
   case 60: i=1; break;
   case 5*60: i=2; break;
   case 10*60: i=3; break;
   case 15*60: i=4; break;
   case 20*60: i=5; break;
   case 30*60: i=6; break;
   case 60*60: i=7; break;
   case 2*60*60: i=8; break;
   case 4*60*60: i=9; break;
   case 8*60*60: i=10; break;
   case 12*60*60: i=11; break;
   case 24*60*60: i=12; break;
   default: i=0; break;
  }
  ((Spinner)findViewById(R.id.whatrepeat_spinner)).setSelection(i);

  i=2; if (globals.takemainshot && !globals.takefrontshot) i=0; else if (!globals.takemainshot && globals.takefrontshot) i=1;
  ((Spinner)findViewById(R.id.whichcamera_spinner)).setSelection(i);

  ((Spinner)findViewById(R.id.maxpixels_front_spinner)).setSelection(globals.maxpixels_front);
  ((Spinner)findViewById(R.id.maxpixels_main_spinner)).setSelection(globals.maxpixels_main);


  ((EditText)findViewById(R.id.url_edittext)).setText(globals.uploadurl);
  ((EditText)findViewById(R.id.authorization_edittext)).setText(globals.basicauthorization);
  View.OnClickListener ctvocl=new View.OnClickListener() {
    @Override
    public void onClick(View v) {
     final CheckedTextView ctv=(CheckedTextView)v;
     ctv.toggle();
     switch (v.getId()) {
      case R.id.uploadpost_checkedtext:
       ((LinearLayout)findViewById(R.id.httpoptions_linearlayout)).setVisibility(ctv.isChecked()?View.VISIBLE:View.GONE);
      break;
      case R.id.uploaddrive_checkedtext:
       ((LinearLayout)findViewById(R.id.driveoptions_linearlayout)).setVisibility(ctv.isChecked()?View.VISIBLE:View.GONE);
      break;
      case R.id.uploadstorage_checkedtext:
       ((LinearLayout)findViewById(R.id.storageoptions_linearlayout)).setVisibility(ctv.isChecked()?View.VISIBLE:View.GONE);
      break;
     }
    }
    };
  setupcheckedtextview(((CheckedTextView)findViewById(R.id.waitforwifi_checkedtext)),ctvocl,globals.waitforwifi);
  setupcheckedtextview(((CheckedTextView)findViewById(R.id.autostartwifi_checkedtext)),ctvocl,globals.autostartwifi);
  setupcheckedtextview(((CheckedTextView)findViewById(R.id.autostopwifi_checkedtext)),ctvocl,globals.autostopwifi);
  setupcheckedtextview(((CheckedTextView)findViewById(R.id.fullscreen_checkedtext)),ctvocl,globals.fullscreen);
  setupcheckedtextview(((CheckedTextView)findViewById(R.id.dimscreen_checkedtext)),ctvocl,globals.dimscreen);
  setupcheckedtextview(((CheckedTextView)findViewById(R.id.turnoff_checkedtext)),ctvocl,globals.turnoff);
  setupcheckedtextview(((CheckedTextView)findViewById(R.id.uploadpost_checkedtext)),ctvocl,globals.uploadpost);
  setupcheckedtextview(((CheckedTextView)findViewById(R.id.uploaddrive_checkedtext)),ctvocl,globals.uploaddrive);
  setupcheckedtextview(((CheckedTextView)findViewById(R.id.uploadstorage_checkedtext)),ctvocl,globals.uploadstorage);
  setupcheckedtextview(((CheckedTextView)findViewById(R.id.deletetrash_checkedtext)),ctvocl,globals.deletetrash);
  setupcheckedtextview(((CheckedTextView)findViewById(R.id.forceflash_checkedtext)),ctvocl,globals.forceflash);

  setupcheckedtextview(((CheckedTextView)findViewById(R.id.waitforcomplete_checkedtext)),ctvocl,globals.waitforcomplete);
  setupcheckedtextview(((CheckedTextView)findViewById(R.id.autostart_checkedtext)),ctvocl,globals.autostart);

  if (!globals.uploadpost) ((LinearLayout)findViewById(R.id.httpoptions_linearlayout)).setVisibility(View.GONE);
  if (!globals.uploaddrive) ((LinearLayout)findViewById(R.id.driveoptions_linearlayout)).setVisibility(View.GONE);
  if (!globals.uploadstorage) ((LinearLayout)findViewById(R.id.storageoptions_linearlayout)).setVisibility(View.GONE);

  if (globals.autostart) {
   ((Button)findViewById(R.id.start_btn)).setText("Cancel Autostart");
  } else {
   ((Button)findViewById(R.id.start_btn)).setText("Start");
  }

  ((Button)findViewById(R.id.start_btn)).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
     if (!globals.autostart) {
      saveoptions(context);
      reconsiderwakelock();
      startintent(Waiting.class);
     } else {
      handler.removeCallbacksAndMessages(null);
      ((Button)findViewById(R.id.start_btn)).setText("Autostart disabled");
     }
    }
  });
  ((Button)findViewById(R.id.save_btn)).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
     saveoptions(context);
     reconsiderwakelock();
     handler.post(new Runnable() { public void run() { setandfilllayout(); }});
    }
  });

 }
 @Override
 public void onPause() {
  stopwakelock();
  handler.removeCallbacksAndMessages(null);
  super.onPause();
 }
 @Override
 public void onResume() {
  startwakelock();
  autostartfuse=11;
  handler.post(pulse_runnable);
  super.onResume();
 }
}
