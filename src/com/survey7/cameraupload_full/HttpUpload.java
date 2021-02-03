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


public class HttpUpload extends Activity {
 private Globals globals;
 private boolean isdrawn;
 private String lastmessage="";
 private String logmessage="";
 private Handler handler=new Handler();
 private Runnable runnable_redraw=new Runnable() { public void run() { redraw(); }};

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
  addlogmessage("Starting upload");
  new UploadTask().execute(new HttpUploadParameters(uf.url,uf.basicauthorization,uf.data));
 }
 @Override
 public void onResume() {
  super.onResume();
  do {} while (false);
  isdrawn=true;
  redraw();
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
 private String inputstream_readall(InputStream is) {
  byte[] bytes=new byte[4096];
  ByteArrayOutputStream os=new ByteArrayOutputStream();
  try {
   while (true) {
    int k;
    k=is.read(bytes,0,4096);
    if (k<0) break;
    os.write(bytes,0,k);
   }
  } catch(java.io.IOException e) { return ""; }
  return os.toString();
 }

 private class HttpResult {
  String s;
  boolean b;
  boolean doretry;
  HttpResult(boolean b_in, String s_in) {
   b=b_in; s=s_in; doretry=false;
  }
  HttpResult(boolean b_in, String s_in, boolean doretry_in) {
   b=b_in; s=s_in; doretry=doretry_in;
  }
 };

 private HttpResult httpsupload(String url, String basicauthorization, byte[] post) {
  byte[] resp=null;
  HttpsURLConnection huc=null;
  URL u;
  try {
   u=new URL(url);
   addlogmessage("Opening connection");
   huc=(HttpsURLConnection)u.openConnection();

   try {
    SSLContext ssl=SSLContext.getInstance("TLS");
    ssl.init(null,null,new SecureRandom());
    huc.setSSLSocketFactory(ssl.getSocketFactory());
   }
   catch(java.security.NoSuchAlgorithmException e) { return new HttpResult(true,"httpsupload:nosuchalgorithm exception"); }
   catch(java.security.KeyManagementException e) { return new HttpResult(true,"httpsupload:keymanagement exception"); }

   huc.setConnectTimeout(1000*10);
   huc.setReadTimeout(1000*10);
   huc.setDoOutput(true);
   huc.setDoInput(true);
   huc.setUseCaches(false);
   if ((basicauthorization!=null)&&(!basicauthorization.isEmpty())) {
    huc.setRequestProperty("Authorization", "Basic "+Base64.encodeToString(basicauthorization.getBytes(),Base64.NO_WRAP));
   }
   huc.setRequestMethod("POST");
   huc.setRequestProperty("Content-Length",""+post.length);

   OutputStream os=huc.getOutputStream();
   addlogmessage("Sending POST");
   os.write(post);

   InputStream is=huc.getInputStream();
   addlogmessage("Reading reply");
   String response=inputstream_readall(is);
   do {} while (false);
   if (response.equals("{\"success\":1}")) return new HttpResult(false,"Upload confirmed");
   else return new HttpResult(false,"Upload complete");
  }
  catch(java.net.MalformedURLException e) { return new HttpResult(true,"httpsupload:malformedurl exception"); }
  catch(java.io.IOException e) { return new HttpResult(true,"httpsupload:io exception",true); }

  finally { if (huc!=null) huc.disconnect(); }
 }
 private HttpResult httpupload(String url, String basicauthorization, byte[] post) {
  if (url.startsWith("https://")) return httpsupload(url,basicauthorization,post);

  byte[] resp=null;
  HttpURLConnection huc=null;
  URL u;
  try {
   u=new URL(url);
   addlogmessage("Opening connection");
   huc=(HttpURLConnection)u.openConnection();

   huc.setConnectTimeout(1000*10);
   huc.setReadTimeout(1000*10);
   huc.setDoOutput(true);
   huc.setDoInput(true);
   huc.setUseCaches(false);
   if ((basicauthorization!=null)&&(!basicauthorization.isEmpty())) {

    huc.setRequestProperty("Authorization", "Basic "+Base64.encodeToString(basicauthorization.getBytes(),Base64.NO_WRAP));
   }
   huc.setRequestMethod("POST");
   huc.setRequestProperty("Content-Length",""+post.length);

   OutputStream os=huc.getOutputStream();
   addlogmessage("Sending POST");
   os.write(post);

   InputStream is=huc.getInputStream();
   addlogmessage("Reading reply");
   String response=inputstream_readall(is);
   do {} while (false);
   if (response.equals("{\"success\":1}")) return new HttpResult(false,"Upload confirmed");
   else return new HttpResult(false,"Upload complete");
  }
  catch(java.net.MalformedURLException e) { return new HttpResult(true,"httpupload:malformedurl exception"); }
  catch(java.io.IOException e) { return new HttpResult(true,"httpupload:io exception",true); }

  finally { if (huc!=null) huc.disconnect(); }
 }

 private class HttpUploadParameters {
  String url,basicauthorization;
  byte[] post;
  HttpUploadParameters(String url_in, String basicauthorization_in, byte[] post_in) {
   url=url_in; basicauthorization=basicauthorization_in; post=post_in;
  }
 };

 private class UploadTask extends AsyncTask<HttpUploadParameters, Integer, HttpResult> {
  @Override
  protected HttpResult doInBackground(HttpUploadParameters... params) {
   String url,basicauthorization;
   byte[] post;
   HttpResult bs;
   int loop=3;
   url=params[0].url;
   basicauthorization=params[0].basicauthorization;
   post=params[0].post;
   while (true) {
    bs=httpupload(url,basicauthorization,post);
    if (!bs.b) break;
    if (!bs.doretry) break;
    loop--;
    if (loop==0) break;
    try {
     Thread.sleep(1000);
    } catch(InterruptedException e) {}
   }
   return bs;
  }
  @Override
  protected void onPostExecute(HttpResult result) {
   Intent intent=getIntent();
   intent.putExtra("message",result.s);
   if (result.b) {
    addlogmessage("Error uploading");
    addlogmessage(result.s);

    setResult(RESULT_FIRST_USER,intent);
   } else {
    addlogmessage("Finished uploading");
    addlogmessage(result.s);
    setResult(RESULT_OK,intent);
   }
   finish();
  }
  @Override
  protected void onPreExecute() {
  }
  @Override
  protected void onProgressUpdate(Integer... values) {
  }
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
}
