package com.survey7.cameraupload_full;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import java.io.OutputStream;
import java.io.IOException;
import java.lang.Runnable;
import android.os.Handler;
import java.util.LinkedList;
import android.app.ActivityManager;
import android.content.ServiceConnection;
import android.content.Context;
import android.content.ComponentName;
import android.os.IBinder;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.Query.Builder;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.ExecutionOptions;

public class DriveUpload extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {
 private Globals globals;
 private boolean isdrawn;
 private GoogleApiClient GAC;
 private boolean iserror=false;
 private boolean needcreatefolder=false;
 private boolean needfolderid=true;
 private boolean needcreatefile=false;
 private boolean needfoldertrim=false;
 private boolean needdeletes=false;
 private boolean needwaitforcomplete=false;
 private Handler handler=new Handler();
 private Runnable runnable_pulse=new Runnable() { public void run() { pulse(); } };
 private Runnable runnable_redraw=new Runnable() { public void run() { redraw(); }};
 private DriveId folderid;
 private String lasterror=null;
 private String logmessage="";
 private String lastmessage="";
 private String lastlogmessage="";
 private int lastmessagecount=0;
 private LinkedList<DriveId> deletes;

 @Override
 public void onConfigurationChanged(Configuration newConfig) {
  super.onConfigurationChanged(newConfig);
 }
 @Override
 public void onCreate(Bundle savedInstanceState) {
  globals=(Globals)((globalcontainer)getApplicationContext()).globals;
  super.onCreate(savedInstanceState);
  setContentView(R.layout.upload);

  if (globals.uf.folderdriveid!=null) folderid=DriveId.decodeFromString(globals.uf.folderdriveid);
  if (folderid==null) needfolderid=true;
  if (globals.trimnumber>0) needfoldertrim=true;
  needcreatefile=true;
 }
 @Override
 public void onDestroy() {
  do {} while (false);
  handler.removeCallbacksAndMessages(null);
  super.onDestroy();
 }
 @Override
 public void onResume() {
  super.onResume();
  do {} while (false);
  isdrawn=true;
  redraw();
  if (GAC==null) {
   do {} while (false);
   GAC=new GoogleApiClient.Builder(this)
     .addApi(Drive.API)
     .addScope(Drive.SCOPE_FILE)
     .addConnectionCallbacks(this)
     .addOnConnectionFailedListener(this)
     .build();
  }
  do {} while (false);
  GAC.connect();
 }
 @Override
 protected void onPause() {
  isdrawn=false;
  if (GAC!=null) {
   do {} while (false);
   GAC.disconnect();
  }
  super.onPause();
 }
 @Override
 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  do {} while (false);
  if (requestCode==1) {
   if (resultCode==RESULT_OK) {
   } else {
    lasterror="DriveUpload, failed to associate with account";
    do {} while (false);
    iserror=true;
    handler.post(runnable_pulse);
   }
  }
 }
 @Override
 public void onConnectionFailed(ConnectionResult result) {
  if (!result.hasResolution()) {
   lasterror="DriveUpload.onConnectionFailed, no resolution";
   do {} while (false);
   iserror=true;
   handler.post(runnable_pulse);
   return;
  }
  do {} while (false);
  try {
   result.startResolutionForResult(this,1);
  } catch(SendIntentException e) {
   lasterror="DriveUpload.onConnectionFailed, sendintentexception";
   iserror=true;
   do {} while (false);
   handler.post(runnable_pulse);
  }
 }
 @Override
 public void onConnected(Bundle connectionHint) {
  do {} while (false);
  handler.post(runnable_pulse);
 }
 @Override
 public void onConnectionSuspended(int cause) {
  do {} while (false);
 }
 private void addlogmessage(String message) {
  if (message.equals(lastmessage)) {
   lastmessagecount++;
   logmessage=lastlogmessage+" ("+lastmessagecount+")";
  } else {
   lastmessagecount=1;
   lastlogmessage=logmessage=logmessage+"\n"+message;
   lastmessage=message;
  }
  handler.post(runnable_redraw);
 }
 private void redraw() {
  if (!isdrawn) return;
  ((TextView)findViewById(R.id.status_textview)).setText(lastmessage);
  ((TextView)findViewById(R.id.log_textview)).setText(logmessage);
 }
 private void pulse() {
  handler.removeCallbacksAndMessages(null);
  if (!iserror) {
   if (needcreatefolder) { addlogmessage("Creating folder"); createfolder(); return; }
   if (needfolderid) { addlogmessage("Looking for folder"); findfolder(); return; }
   if (needcreatefile) { addlogmessage("Creating file"); createfile(); return; }
   if (needfoldertrim) { addlogmessage("Trimming folder"); trimfolder(); return; }
   if (needdeletes) { addlogmessage("Deleting file"); deletefile(); return; }
   if (needwaitforcomplete) { addlogmessage("Waiting for upload confirmation"); waitforcomplete(); return; }
   addlogmessage("Save to Google Drive finished");
  }
  Intent intent=getIntent();
  if (lasterror!=null) {
   intent.putExtra("message",lasterror);
   setResult(Activity.RESULT_FIRST_USER,intent);
  } else {
   intent.putExtra("message",lastmessage);
   setResult(Activity.RESULT_OK,intent);
  }
  finish();
 }
 final private ResultCallback<DriveFileResult> createfilecb2=new ResultCallback<DriveFileResult>() {
  @Override
  public void onResult(DriveFileResult result) {
   if (!result.getStatus().isSuccess()) {
    lasterror="DriveUpload.createfilecb2 failure in getstatus";
    do {} while (false);
    iserror=true;
    handler.post(runnable_pulse);
    return;
   }
   do {} while (false);
   needcreatefile=false;
   needwaitforcomplete=globals.waitforcomplete;
   handler.post(runnable_pulse);
  }
 };
 final private ResultCallback<DriveContentsResult> createfilecb1=new ResultCallback<DriveContentsResult>() {
  @Override
  public void onResult(DriveContentsResult result) {
   final UploadFile uf=globals.uf;
   if (!result.getStatus().isSuccess()) {
    lasterror="DriveUpload.createfilecb1 failure in getstatus";
    do {} while (false);
    iserror=true;
    handler.post(runnable_pulse);
    return;
   }
   final DriveContents dc=result.getDriveContents();
   new Thread() {
    @Override
    public void run() {
     OutputStream os=dc.getOutputStream();
     try {
      os.write(uf.data);
     } catch(IOException e) {
      lasterror="DriveUpload.createfilecb1 IO error";
      do {} while (false);
      iserror=true;
      handler.post(runnable_pulse);
      return;
     }
     MetadataChangeSet mdcs=new MetadataChangeSet.Builder().setMimeType("image/jpeg").setTitle(uf.longfilename).build();


     ExecutionOptions eo;
     ExecutionOptions.Builder eob=new ExecutionOptions.Builder();

     eo=eob.setNotifyOnCompletion(true).setTrackingTag(uf.longfilename).build();
     globals.init_completiontag(uf.longfilename);

     folderid.asDriveFolder()
       .createFile(GAC,mdcs,dc,eo)
       .setResultCallback(createfilecb2);
    }
   }.start();
  }
 };
 private void createfile() {
  do {} while (false);
  Drive.DriveApi.newDriveContents(GAC).setResultCallback(createfilecb1);
 }
 final private ResultCallback<MetadataBufferResult> querycb=new ResultCallback<MetadataBufferResult>() {
  @Override
  public void onResult(MetadataBufferResult result) {
   final MetadataBuffer mdb;
   int i,n;
   boolean found=false;
   if (!result.getStatus().isSuccess()) {
    lasterror="DriveUpload.querycb failure in getstatus";
    do {} while (false);
    iserror=true;
    handler.post(runnable_pulse);
    return;
   }
   mdb=result.getMetadataBuffer();
   n=mdb.getCount();
   for (i=0;i<n;i++) {
    Metadata m=mdb.get(i);
    if (!m.isFolder()) continue;
    if (m.isTrashed()) continue;
    if (!m.isEditable()) continue;
    found=true;
    do {} while (false);
    do {} while (false);
    folderid=m.getDriveId();
    globals.uf.folderdriveid=folderid.encodeToString();
    needfolderid=false;
    break;
   }
   if (!found) {
    do {} while (false);
    needcreatefolder=true;
   }
   handler.post(runnable_pulse);
   mdb.release();
  }
 };
 private void findfolder() {
  do {} while (false);
  Query query=new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE,globals.uf.foldername)).build();
  Drive.DriveApi.getRootFolder(GAC).queryChildren(GAC,query).setResultCallback(querycb);
 }
 final private ResultCallback<DriveFolderResult> createfoldercb2=new ResultCallback<DriveFolderResult>() {
  @Override
  public void onResult(DriveFolderResult result) {
   if (!result.getStatus().isSuccess()) {
    lasterror="DriveUpload.createfoldercb2 failure in getstatus";
    do {} while (false);
    iserror=true;
    handler.post(runnable_pulse);
    return;
   }
   do {} while (false);

   folderid=result.getDriveFolder().getDriveId();
   globals.uf.folderdriveid=folderid.encodeToString();
   needcreatefolder=false;
   needfolderid=false;
   handler.post(runnable_pulse);
  }
 };
 final private ResultCallback<DriveContentsResult> createfoldercb1=new ResultCallback<DriveContentsResult>() {
  @Override
  public void onResult(DriveContentsResult result) {
   if (!result.getStatus().isSuccess()) {
    lasterror="DriveUpload.createfoldercb1 failure in getstatus";
    do {} while (false);
    iserror=true;
    handler.post(runnable_pulse);
    return;
   }
   final DriveContents dc=result.getDriveContents();
   MetadataChangeSet mdcs=new MetadataChangeSet.Builder().setTitle(globals.uf.foldername).build();
   Drive.DriveApi.getRootFolder(GAC)
     .createFolder(GAC,mdcs)
     .setResultCallback(createfoldercb2);
  }
 };
 private void createfolder() {
  do {} while (false);
  Drive.DriveApi.newDriveContents(GAC).setResultCallback(createfoldercb1);
 }
 final private ResultCallback<MetadataBufferResult> trimcb=new ResultCallback<MetadataBufferResult>() {
  @Override
  public void onResult(MetadataBufferResult result) {
   final MetadataBuffer mdb;
   Metadata m;
   int i,n,count=0;

   if (!result.getStatus().isSuccess()) {
    lasterror="DriveUpload.trimcb failure in getstatus";
    do {} while (false);
    iserror=true;
    handler.post(runnable_pulse);
    return;
   }
   mdb=result.getMetadataBuffer();
   n=mdb.getCount();
   for (i=0;i<n;i++) {
    final String s;
    m=mdb.get(i);
    if (m.isTrashed()) continue;
    s=m.getTitle();
    if (s.endsWith(".autodelete.jpg")) count++;
   }
   do {} while (false);
   if (count>globals.trimnumber) {
    deletes=new LinkedList<DriveId>();
    count-=globals.trimnumber;
    needdeletes=true;
    for (i=0;i<n;i++) {
     final String s;
     m=mdb.get(i);
     if (m.isTrashed()) continue;
     s=m.getTitle();
     if (s.endsWith(".autodelete.jpg")) {
      do {} while (false);
      deletes.add(m.getDriveId());
      count--;
      if (count==0) break;
     }
    }
   }
   needfoldertrim=false;
   handler.post(runnable_pulse);
   mdb.release();
  }
 };
 private void trimfolder() {
  do {} while (false);
  SortOrder so=new SortOrder.Builder().addSortAscending(SortableField.CREATED_DATE).build();
  Query query=new Query.Builder()
    .addFilter(Filters.eq(SearchableField.MIME_TYPE,"image/jpeg"))
    .setSortOrder(so)
    .build();
  folderid.asDriveFolder().queryChildren(GAC,query).setResultCallback(trimcb);
 }
 final private ResultCallback<Status> deletecb=new ResultCallback<Status>() {
  @Override
  public void onResult(Status result) {
   if (!result.isSuccess()) {
    lasterror="DriveUpload.deletecb failure in getstatus";
    do {} while (false);
    iserror=true;
    handler.post(runnable_pulse);
    return;
   }

   if (null==deletes.peekFirst()) needdeletes=false;
   do {} while (false);
   handler.post(runnable_pulse);
  }
 };
 private void deletefile() {
  do {} while (false);
  DriveId id;
  id=deletes.pollFirst();
  if (id==null) {
   do {} while (false);
   return;
  }
  if (globals.deletetrash) id.asDriveResource().delete(GAC).setResultCallback(deletecb);
  else id.asDriveResource().trash(GAC).setResultCallback(deletecb);
 }

 private void waitforcomplete() {
  do {} while (false);
  if (globals.fetch_completiontag()) {
   do {} while (false);
   needwaitforcomplete=false;
   handler.post(runnable_pulse);
   return;
  }
  handler.postDelayed(runnable_pulse,1000);
 }
}
