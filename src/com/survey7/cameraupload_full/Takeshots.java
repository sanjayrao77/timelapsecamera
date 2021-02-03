package com.survey7.cameraupload_full;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.hardware.Camera;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import java.lang.Runnable;
import android.os.Handler;
import android.os.PowerManager;
import java.util.List;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Intent;
import java.util.Calendar;
import java.lang.StringBuilder;
import android.view.Surface;

public class Takeshots extends Activity {
 private Globals globals;
 private Handler handler=new Handler();
 private Runnable runnable_pulse=new Runnable() { public void run() { pulse(); } };
 private PowerManager PM;
 private Camera camera=null;

 private ConnectivityManager CM;

 private boolean needmainshot=false;
 private boolean needfrontshot=false;
 private boolean isdrawn=false;
 private boolean incamera=false;
 private Preview preview;
 private String directionstring;
 private boolean needpreviewready=false;
 private boolean needautofocus=false;
 private boolean needtakeshot=false;
 private Calendar shotdate;
 private boolean needpicture=false;
 private byte[] picturedata;
 private boolean needstorageupload=false;
 private boolean needwifi=false;
 private boolean needhttpupload=false;
 private boolean needdriveupload=false;
 private int wififuse=0;
 private boolean needactivityreturn=false;
 private int cameraorientation=0;
 private int displayrotation=0;
 private int imagerotation=0;

 private void cameracleanup() {
  if (preview!=null) preview.cleanup();
  if (incamera) {
   camera.release();
   incamera=false;
   do {} while (false);
  }
 }
 private void nextcamera() {
  cameracleanup();
  if (needmainshot) needmainshot=false;
  else needfrontshot=false;
  handler.post(runnable_pulse);
 }
 private boolean inlist(List<String> list, String needle) {
  if (list==null) return false;
  for (String s : list) {
   if (s.equals(needle)) return true;
  }
  return false;
 }
 private boolean setparameters(int maxpixels) {
  List<Camera.Size> sizes;
  Camera.Parameters params;
  int highest=0;
  int besth=0,bestw=0;
  int ceiling;

  switch (maxpixels) {
   case 1: ceiling=800*600; break;
   case 2: ceiling=1280*960; break;
   case 3: ceiling=1600*1200; break;
   case 4: ceiling=2048*1536; break;
   case 5: ceiling=2560*1920; break;
   case 6: ceiling=3264*2448; break;
   case 7: ceiling=4000*3000; break;
   default: ceiling=Integer.MAX_VALUE;
  }

  params=camera.getParameters();
  sizes=params.getSupportedPictureSizes();
  for (Camera.Size cs : sizes) {
   int area;
   area=cs.width*cs.height;
   if ((area<=ceiling)&&(area>highest)) { highest=area; bestw=cs.width; besth=cs.height; }
  }
  if (highest==0) return true;
  params.setPictureSize(bestw,besth);
  do {} while (false);


  List<String> list;

  list=params.getSupportedFlashModes();
  if (globals.forceflash) {
   if (inlist(list,Camera.Parameters.FLASH_MODE_TORCH)) params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
   else if (inlist(list,Camera.Parameters.FLASH_MODE_ON)) params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
   else if (inlist(list,Camera.Parameters.FLASH_MODE_AUTO)) params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
  } else {
   if (inlist(list,Camera.Parameters.FLASH_MODE_AUTO)) params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
  }

  list=params.getSupportedFocusModes();
  if (inlist(list,Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
   params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
  } else if (inlist(list,Camera.Parameters.FOCUS_MODE_AUTO)) {
   params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
  }

  list=params.getSupportedWhiteBalance();
  if (inlist(list,Camera.Parameters.WHITE_BALANCE_AUTO)) params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

  list=params.getSupportedSceneModes();
  if (inlist(list,Camera.Parameters.SCENE_MODE_AUTO)) params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

  params.setRotation(imagerotation);
  camera.setParameters(params);
  return false;
 }
 private void setimagerotation(boolean isfront, int degrees) {
  int result;
  if (isfront) {
   result = (cameraorientation + degrees) % 360;
  } else {
   result = (cameraorientation - degrees + 360) % 360;
  }
  imagerotation=result;
 }
 private void setdisplayrotation(boolean isfront) {

  int degrees=0;
  switch (getWindowManager().getDefaultDisplay().getRotation()) {
   case Surface.ROTATION_90: degrees=90; break;
   case Surface.ROTATION_180: degrees=180; break;
   case Surface.ROTATION_270: degrees=270; break;
  }

  int result;
  if (isfront) {
   result = (cameraorientation + degrees) % 360;
   result = (360 - result) % 360;
  } else {
   result = (cameraorientation - degrees + 360) % 360;
  }
  displayrotation=result;
  camera.setDisplayOrientation(result);
  preview.setorientation(result);
  setimagerotation(isfront,degrees);
 }
 private boolean opencamera(boolean onlyfront) {
  Camera.CameraInfo info=new Camera.CameraInfo();
  final int n=Camera.getNumberOfCameras();
  int i;
  for (i=0;i<n;i++) {
   Camera.getCameraInfo(i,info);
   if (onlyfront) {
    if (info.facing==Camera.CameraInfo.CAMERA_FACING_BACK) continue;
   } else {
    if (info.facing==Camera.CameraInfo.CAMERA_FACING_FRONT) continue;
   }
   camera=Camera.open(i);
   cameraorientation=info.orientation;
   if (camera!=null) return false;
  }
  return true;
 }
 private void readyshot(boolean onlyfront) {
  int maxpixels;

  if (onlyfront) {
   globals.setstatus("Readying front shot");
   directionstring="front";
   maxpixels=globals.maxpixels_front;
  } else {
   globals.setstatus("Readying main shot");
   directionstring="main";
   maxpixels=globals.maxpixels_main;
  }
  if (opencamera(onlyfront)) {
   globals.seterror("No camera found");
   nextcamera();
   return;
  }
  incamera=true;
  setdisplayrotation(onlyfront);
  if (setparameters(maxpixels)) {
   globals.seterror("Error setting parameters, no acceptable resolution found.");
   nextcamera();
   return;
  }
  preview.setcamera(camera);
  needpreviewready=true;
 }
 private Camera.PictureCallback picturecallback=new Camera.PictureCallback() {
  @Override
  public void onPictureTaken(byte[] data, Camera camera) {
   needpicture=false;
   do {} while (false);
   preview.stopautofocus();
   globals.setstatus("received picture");
   globals.uf.shortfilename=null;
   globals.uf.longfilename=null;
   if (globals.uploaddrive) needdriveupload=true;
   if (globals.uploadstorage) {
    do {} while (false);
    needstorageupload=true;
   }

   if (globals.uploadpost) needhttpupload=true;
   if (globals.waitforwifi) {
    wififuse=30;
    needwifi=true;
   }

   picturedata=data;
   nextcamera();
  }
 };
 private void autofocus() {
  globals.setstatus("calling autofocus");
  needautofocus=false;
  preview.startautofocus(new Camera.AutoFocusCallback() {
   @Override
   public void onAutoFocus(boolean success, Camera camera) {
    needtakeshot=true;
    handler.removeCallbacksAndMessages(null);
    handler.postDelayed(runnable_pulse,globals.msec_focustime);
   }
  });
 }
 private void takeshot() {
  globals.setstatus("calling takePicture");
  needtakeshot=false;
  shotdate=Calendar.getInstance();
  try {
   camera.takePicture(null,null,picturecallback);
   needpicture=true;
  } catch (java.lang.RuntimeException e) {
   globals.seterror("takepicture exception");
   nextcamera();
  }
 }
 private String makefilename(boolean isfull) {
  StringBuilder sb=new StringBuilder(100);
  sb.append(shotdate.get(Calendar.YEAR));
  sb.append("-");
  final int month=1+shotdate.get(Calendar.MONTH);
  if (month<10) sb.append("0");
  sb.append(month);
  sb.append("-");
  final int day=shotdate.get(Calendar.DAY_OF_MONTH);
  if (day<10) sb.append("0");
  sb.append(day);
  sb.append("-");
  final int hour=shotdate.get(Calendar.HOUR_OF_DAY);
  if (hour<10) sb.append("0");
  sb.append(hour);
  if (isfull) sb.append(":");
  final int minutes=shotdate.get(Calendar.MINUTE);
  if (minutes<10) sb.append("0");
  sb.append(minutes);
  if (isfull) sb.append(":");
  final int seconds=shotdate.get(Calendar.SECOND);
  if (seconds<10) sb.append("0");
  sb.append(seconds);
  if (isfull) {
   if (!globals.cameraname.isEmpty()) {
    sb.append("-");
    sb.append(globals.cameraname);
   }
  }
  sb.append("-");
  sb.append(directionstring);

  sb.append(".autodelete.jpg");

  return sb.toString();
 }
 private void storageuploaddata() {
  do {} while (false);
  needstorageupload=false;
  globals.uf.shortfilename=makefilename(false);
  globals.uf.data=picturedata;

  Intent intent=new Intent(this,StorageUpload.class);
  needactivityreturn=true;
  startActivityForResult(intent,41);
 }

 private void waitforwifi() {
  NetworkInfo ni;
  ni=CM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
  if (ni.isConnected()) {
   do {} while (false);
   needwifi=false;
   handler.post(runnable_pulse);
   return;
  }
  wififuse--;
  if (0==wififuse) {
   globals.seterror("Timeout waiting for wifi");
   finish();
   return;
  }
  handler.postDelayed(runnable_pulse,1000);
 }


 private void httpuploaddata() {
  needhttpupload=false;
  String delim="?";
  if (globals.uploadurl.indexOf('?')>=0) delim="&";
  globals.uf.url=globals.uploadurl+delim+"direction="+directionstring;
  globals.uf.data=picturedata;
  globals.uf.basicauthorization=globals.basicauthorization;

  Intent intent=new Intent(this,HttpUpload.class);
  needactivityreturn=true;
  startActivityForResult(intent,42);
 }

 private void driveuploaddata() {
  do {} while (false);
  needdriveupload=false;
  globals.uf.longfilename=makefilename(true);
  globals.uf.data=picturedata;

  Intent intent=new Intent(this,DriveUpload.class);
  needactivityreturn=true;
  startActivityForResult(intent,43);
 }
 private void pulse() {
  do {} while (false);
  handler.removeCallbacksAndMessages(null);
  if (!isdrawn) return;
  if (!incamera) {
   if (needactivityreturn) {
    do {} while (false);
    return;
   }
   if (needstorageupload) {
    do {} while (false);
    storageuploaddata();
    return;
   }

   if (needwifi) {
    do {} while (false);
    waitforwifi();
    return;
   }
   if (needhttpupload) {
    do {} while (false);
    httpuploaddata();
    return;
   }

   if (needdriveupload) {
    do {} while (false);
    driveuploaddata();
    return;
   }
   if (needmainshot) {
    do {} while (false);
    readyshot(false);
    return;
   }
   if (needfrontshot) {
    do {} while (false);
    readyshot(true);
    return;
   }
  } else {
   if (needpreviewready) {
    do {} while (false);
    return;
   }
   if (needautofocus) {
    do {} while (false);
    autofocus();
    return;
   }
   if (needtakeshot) {
    do {} while (false);
    takeshot();
    return;
   }
   if (needpicture) {
    do {} while (false);
    return;
   }
   do {} while (false);
   nextcamera();
   return;
  }
  setResult(Activity.RESULT_OK);
  finish();
 }
 @Override
 public void onConfigurationChanged(Configuration newConfig) {
  super.onConfigurationChanged(newConfig);
 }
 @Override
 public void onCreate(Bundle savedInstanceState) {
  globals=(Globals)((globalcontainer)getApplicationContext()).globals;
  super.onCreate(savedInstanceState);
  PM=(PowerManager)getSystemService(POWER_SERVICE);

  CM=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

  preview=new Preview(globals,this,this);
  setContentView(preview);
  needmainshot=globals.takemainshot;
  needfrontshot=globals.takefrontshot;
  handler.post(runnable_pulse);
  do {} while (false);
 }
 @Override
 public void onResume() {
  super.onResume();
  do {} while (false);
  isdrawn=true;
  pulse();
 }
 @Override
 public void onPause() {
  super.onPause();
  do {} while (false);
  isdrawn=false;
  cameracleanup();
  handler.removeCallbacksAndMessages(null);
 }
 @Override
 public void onDestroy() {
  do {} while (false);
  handler.removeCallbacksAndMessages(null);
  cameracleanup();
  super.onDestroy();
 }
 @Override
 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  Bundle b=null;
  String message="";
  if (data!=null) b=data.getExtras();
  if (b!=null) {
   message=b.getString("message");
   if (message==null) message="";
  }

  do {} while (false);
  switch (requestCode) {
   case 41:
   case 42:
   case 43:
    if (resultCode==Activity.RESULT_OK) {
     globals.setstatus(message);
    } else if (resultCode==Activity.RESULT_FIRST_USER) {
     globals.seterror(message);
    }
    needactivityreturn=false;
    handler.post(runnable_pulse);
    break;
  }
 }
 public void errorout() {
  finish();
 }
 public void onPreview() {
  Camera.Parameters params;
  String fm;
  do {} while (false);
  needpreviewready=false;
  params=camera.getParameters();
  fm=params.getFocusMode();
  if (fm.equals(params.FOCUS_MODE_AUTO)) {
   needautofocus=true;
   handler.removeCallbacksAndMessages(null);
   handler.post(runnable_pulse);
  } else {
   needtakeshot=true;
   handler.removeCallbacksAndMessages(null);
   handler.postDelayed(runnable_pulse,globals.msec_focustime);
  }
 }
}

class Preview extends ViewGroup implements SurfaceHolder.Callback {
 private SurfaceView sv;
 private SurfaceHolder sh;
 private Camera camera;
 private boolean inpreview=false;
 private boolean inautofocus=false;
 private Globals globals;
 private Takeshots takeshots;
 private int wpreview=0,hpreview=0;
 private int rotation=0;

 Preview(Globals globals_in, Context context, Takeshots takeshots_in) {
  super(context);
  globals=globals_in;
  takeshots=takeshots_in;
  sv=new SurfaceView(context);
  addView(sv);
  sh=sv.getHolder();
  sh.addCallback(this);
  sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  do {} while (false);
 }
 private void setpreviewsize(int width, int height) {
  List<Camera.Size> sizes;
  Camera.Parameters params;
  int highest=0;
  int bestw=0,besth=0;

  params=camera.getParameters();
  sizes=params.getSupportedPreviewSizes();
  for (Camera.Size cs : sizes) {
   if (cs.width > width) continue;
   if (cs.height > height) continue;
   int area=cs.width*cs.height;
   if (area>highest) {
    highest=area; bestw=cs.width; besth=cs.height;
    do {} while (false);
   }
  }
  switch (rotation) {
   case 90: case 270: wpreview=besth; hpreview=bestw; break;
   default: wpreview=bestw; hpreview=besth; break;
  }
  if (highest==0) {
   globals.seterror("Couldn't find any preview dimensions to fit screen");
   takeshots.errorout();
   return;
  }
  params.setPreviewSize(bestw,besth);
  camera.setParameters(params);
 }
 @Override
 protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
  final int width=resolveSize(getSuggestedMinimumWidth(),widthMeasureSpec);
  final int height=resolveSize(getSuggestedMinimumHeight(),heightMeasureSpec);
  setMeasuredDimension(width,height);
  do {} while (false);
 }
 @Override
 protected void onLayout(boolean changed, int l, int t, int r, int b) {
  if (getChildCount()!=1) return;
  View child=getChildAt(0);
  int width=r-l,height=b-t;
  if (camera!=null) {
   int pl,pt;
   setpreviewsize(width,height);
   pl=(width-wpreview)/2;
   pt=(height-hpreview)/2;
   if (pl<0) pl=0; if (pt<0) pt=0;

   child.layout(pl,pt,pl+wpreview,pt+hpreview);
  } else {
   child.layout(0,0,width,height);
  }
  do {} while (false);
 }
 @Override
 public void surfaceCreated(SurfaceHolder holder) {
  do {} while (false);
 }
 @Override
 public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
  do {} while (false);
  if (camera==null) return;
  try {
   camera.setPreviewDisplay(holder);
  } catch (java.io.IOException err) {
   globals.seterror("setPreviewDisplay exception");
   takeshots.errorout();
  }
  camera.startPreview();
  inpreview=true;
  takeshots.onPreview();
 }
 @Override
 public void surfaceDestroyed(SurfaceHolder holder) {
  do {} while (false);
  cleanup();
 }
 public void setcamera(Camera camera_in) {
  cleanup();
  if (camera_in==null) return;
  camera=camera_in;
  requestLayout();
 }
 public void startautofocus(Camera.AutoFocusCallback cb) {
  do {} while (false);
  inautofocus=true;
  camera.autoFocus(cb);
 }
 public void stopautofocus() {
  if (inautofocus) {
   do {} while (false);
   camera.cancelAutoFocus();
   inautofocus=false;
  }
 }
 public void cleanup() {
  if (inpreview) {
   stopautofocus();
   camera.stopPreview();
   inpreview=false;
  }
  camera=null;
 }
 public void setorientation(int rotation_in) {
  rotation=rotation_in;
 }
}
