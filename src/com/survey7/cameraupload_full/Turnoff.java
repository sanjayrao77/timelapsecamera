package com.survey7.cameraupload_full;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.os.Vibrator;
import android.os.Handler;


public class Turnoff extends Activity {
 private Globals globals;
 private Handler handler;

 @Override
 public void onConfigurationChanged(Configuration newConfig) {
  super.onConfigurationChanged(newConfig);
 }

 @Override
 public void onCreate(Bundle savedInstanceState) {
  ComponentName CN;
  DevicePolicyManager DPM;
  globals=(Globals)((globalcontainer)getApplicationContext()).globals;
  super.onCreate(savedInstanceState);

  CN=new ComponentName(Turnoff.this,Darclass.class);
  DPM=(DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);


  if (!DPM.isAdminActive(CN)) {
   Intent intent=new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
   intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, CN);
   startActivityForResult(intent,1);
  } else {
   DPM.lockNow();




  }

  finish();
 }

 @Override
 protected void onActivityResult(int requestcode, int resultcode, Intent data) {
  if (requestcode==1) {
  }
 }
}
