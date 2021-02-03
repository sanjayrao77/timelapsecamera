package com.survey7.cameraupload_full;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
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
import java.util.List;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.SupplicantState;
import android.os.AsyncTask;
import android.content.Intent;
import javax.net.ssl.SSLContext;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import android.media.MediaScannerConnection;
import java.io.File;
import android.os.Environment;
import java.io.FileOutputStream;
import android.net.Uri;


public class StorageUpload extends Activity {
 private Globals globals;
 private boolean isdrawn;
 private String lastmessage="";
 private String logmessage="";
 private Handler handler=new Handler();
 private Runnable runnable_redraw=new Runnable() { public void run() { redraw(); }};
 private boolean needsave=false;

 @Override
 public void onConfigurationChanged(Configuration newConfig) {
  super.onConfigurationChanged(newConfig);
 }
 @Override
 public void onCreate(Bundle savedInstanceState) {
  globals=(Globals)((globalcontainer)getApplicationContext()).globals;
  final UploadFile uf=globals.uf;
  super.onCreate(savedInstanceState);
  setContentView(R.layout.upload);
  do {} while (false);
  addlogmessage("Starting save");
  needsave=true;
 }
 @Override
 public void onResume() {
  super.onResume();
  do {} while (false);
  isdrawn=true;
  redraw();
  if (needsave) {
   needsave=false;
   savefile();
  }
 }
 @Override
 public void onPause() {
  super.onPause();
  do {} while (false);
  isdrawn=false;
 }
 @Override
 public void onDestroy() {
  do {} while (false);
  super.onDestroy();
 }
 @Override
 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  do {} while (false);
 }

 private void addlogmessage(String message) {
  logmessage=logmessage+"\n"+message;
  lastmessage=message;
  handler.post(runnable_redraw);
 }
 private void redraw() {
  if (!isdrawn) return;
  ((TextView)findViewById(R.id.status_textview)).setText(lastmessage);
  ((TextView)findViewById(R.id.log_textview)).setText(logmessage);
 }
 private void callfinish(String message, boolean isfailure) {
  Intent intent=getIntent();
  if (message!=null) lastmessage=message;
  intent.putExtra("message",lastmessage);
  if (isfailure) {
   setResult(RESULT_FIRST_USER,intent);
  } else {
   setResult(RESULT_OK,intent);
  }
  finish();
 }

 private File getdcimdirectory() {
  File path;
  path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
  if (path==null) return null;
  path=new File(path,"timelapsecamera/");
  if (path.exists()) return path;
  if (path.mkdirs()) return path;
  return null;
 }
 private File getexternaldirectory() {
  File path;
  path=Environment.getExternalStorageDirectory();
  if (path==null) return null;
  path=new File(path,"timelapsecamera/");
  if (path.exists()) return path;
  if (path.mkdirs()) return path;
  return null;
 }

 private void savefile() {
  File f,path;
  path=getdcimdirectory();
  if (path==null) {
   path=getexternaldirectory();
   if (path==null) {
    callfinish("Couldn't make workable external directory",true);
    return;
   }
  }

  f=new File(path,globals.uf.shortfilename);
  try {
   if (!f.createNewFile()) { callfinish("Unable to create file: \""+f.getAbsoluteFile()+"\"",true); return; }
  } catch (java.io.IOException e) { callfinish("Error while creating file: \""+f.getAbsoluteFile()+"\"",true); return; }
  try {
   OutputStream os=new FileOutputStream(f);
   os.write(globals.uf.data);
   os.close();
  }
  catch(java.io.FileNotFoundException e) { callfinish("Filenotfoundexception",true); return; }
  catch(java.io.IOException e) { callfinish("IO error while saving picture",true); return; }
  MediaScannerConnection.scanFile(this,new String[] { f.toString() }, null, null);



  do {} while (false);
  callfinish("Save complete",false);
 }
}
