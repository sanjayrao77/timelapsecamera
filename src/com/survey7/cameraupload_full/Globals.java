package com.survey7.cameraupload_full;
import android.util.Log;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Globals {
 public static final String DBG="DEBUG";
 public boolean isloaded=false;
 public UploadFile uf;

 public String laststatus="",lasterror="";
 public int errorcount=0;

 public String basicauthorization="";
 public String uploadurl="http://";
 public int periodseconds=0;
 public boolean takefrontshot=false;
 public boolean takemainshot=true;
 public boolean waitforwifi=true;
 public boolean autostartwifi=false;
 public boolean autostopwifi=false;
 public boolean fullscreen=false;
 public boolean dimscreen=false;
 public int msec_focustime=2000;
 public int sec_initialdelay=1;
 public boolean turnoff=false;
 public int maxpixels_front=0;
 public int maxpixels_main=0;
 public boolean uploadpost=false;
 public boolean uploaddrive=true;
 public String default_foldername="TimeLapseCameraUpload";
 public String foldername=default_foldername;
 public int trimnumber=50;
 public boolean deletetrash=false;
 public String cameraname="";
 public boolean forceflash=false;
 public boolean uploadstorage=false;
 public boolean removable=true;
 public boolean waitforcomplete=true;
 public boolean autostart=false;

 public final static void debugout(String msg) {
  Log.v(DBG,msg);
 }

 public void setstatus(String status) {
  Globals.debugout("cameraupload.globals status change: "+status);
  laststatus=status;
 }
 public void seterror(String error) {
  lasterror=laststatus=error;
  Log.e(DBG,error);
  errorcount+=1;
 }
 public String getlaststatus() {
  Globals.debugout("cameraupload.globals status request: "+laststatus);
  return laststatus;
 }

 public void logoptions() {
  Log.v(DBG,"Options: ");
  Log.v(DBG,"basicauthization: "+basicauthorization);
  Log.v(DBG,"uploadurl: "+uploadurl);
  Log.v(DBG,"periodseconds: "+periodseconds);
  Log.v(DBG,"takefrontshot: "+takefrontshot);
  Log.v(DBG,"takemainshot: "+takemainshot);
 }
 public void loadpreferences(Context context) {
  if (isloaded) return;
  SharedPreferences settings;
  settings=PreferenceManager.getDefaultSharedPreferences(context);
  basicauthorization=settings.getString("basicauthorization",basicauthorization);
  uploadurl=settings.getString("uploadurl",uploadurl);
  periodseconds=settings.getInt("periodseconds",periodseconds);
  takefrontshot=settings.getBoolean("takefrontshot",takefrontshot);
  takemainshot=settings.getBoolean("takemainshot",takemainshot);
  waitforwifi=settings.getBoolean("waitforwifi",waitforwifi);
  autostartwifi=settings.getBoolean("autostartwifi",autostartwifi);
  autostopwifi=settings.getBoolean("autostopwifi",autostopwifi);
  fullscreen=settings.getBoolean("fullscreen",fullscreen);
  dimscreen=settings.getBoolean("dimscreen",dimscreen);
  msec_focustime=settings.getInt("msec_focustime",msec_focustime);
  sec_initialdelay=settings.getInt("sec_initialdelay",sec_initialdelay);
  turnoff=settings.getBoolean("turnoff",turnoff);
  maxpixels_front=settings.getInt("maxpixels_front",maxpixels_front);
  maxpixels_main=settings.getInt("maxpixels_main",maxpixels_main);
  uploadpost=settings.getBoolean("uploadpost",uploadpost);
  uploaddrive=settings.getBoolean("uploaddrive",uploaddrive);
  foldername=settings.getString("foldername",foldername);
  trimnumber=settings.getInt("trimnumber",trimnumber);
  deletetrash=settings.getBoolean("deletetrash",deletetrash);
  cameraname=settings.getString("cameraname",cameraname);
  forceflash=settings.getBoolean("forceflash",forceflash);
  uploadstorage=settings.getBoolean("uploadstorage",uploadstorage);
  removable=settings.getBoolean("removable",removable);
  waitforcomplete=settings.getBoolean("waitforcomplete",waitforcomplete);
  autostart=settings.getBoolean("autostart",autostart);
  isloaded=true;
 }

 public void savepreferences(Context context) {
  SharedPreferences settings;
  SharedPreferences.Editor editor;
  settings=PreferenceManager.getDefaultSharedPreferences(context);
  editor=settings.edit();
  editor.putString("basicauthorization",basicauthorization);
  editor.putString("uploadurl",uploadurl);
  editor.putInt("periodseconds",periodseconds);
  editor.putBoolean("takefrontshot",takefrontshot);
  editor.putBoolean("takemainshot",takemainshot);
  editor.putBoolean("waitforwifi",waitforwifi);
  editor.putBoolean("autostartwifi",autostartwifi);
  editor.putBoolean("autostopwifi",autostopwifi);
  editor.putBoolean("fullscreen",fullscreen);
  editor.putBoolean("dimscreen",dimscreen);
  editor.putInt("msec_focustime",msec_focustime);
  editor.putInt("sec_initialdelay",sec_initialdelay);
  editor.putBoolean("turnoff",turnoff);
  editor.putInt("maxpixels_front",maxpixels_front);
  editor.putInt("maxpixels_main",maxpixels_main);
  editor.putBoolean("uploadpost",uploadpost);
  editor.putBoolean("uploaddrive",uploaddrive);
  editor.putString("foldername",foldername);
  editor.putInt("trimnumber",trimnumber);
  editor.putBoolean("deletetrash",deletetrash);
  editor.putString("cameraname",cameraname);
  editor.putBoolean("forceflash",forceflash);
  editor.putBoolean("uploadstorage",uploadstorage);
  editor.putBoolean("removable",removable);
  editor.putBoolean("waitforcomplete",waitforcomplete);
  editor.putBoolean("autostart",autostart);
  editor.apply();
 }

 private String completiontag;
 private boolean status_completiontag;

 public synchronized void init_completiontag(String tag) {
  completiontag=tag;
  status_completiontag=false;
 }
 public synchronized void checkin_completiontag(String tag) {
  if (tag.equals(completiontag)) status_completiontag=true;
 }
 public synchronized boolean fetch_completiontag() {
  return status_completiontag;
 }
}
