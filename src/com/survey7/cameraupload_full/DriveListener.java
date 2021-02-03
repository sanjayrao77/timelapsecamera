package com.survey7.cameraupload_full;

import android.app.Service;
import android.util.Log;
import com.google.android.gms.drive.events.DriveEventService;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.CompletionEvent;
import java.util.List;
import android.os.IBinder;
import android.content.Intent;
import android.os.Binder;


public class DriveListener extends DriveEventService {
 private Globals globals;

 @Override
 public void onCreate() {
  super.onCreate();
  globalcontainer gc=(globalcontainer)getApplicationContext();
  if (gc!=null) globals=gc.globals;
  if (globals!=null) do {} while (false);
 }

 @Override
 public void onChange(ChangeEvent event) {
  super.onChange(event);
  do {} while (false);
 }

 @Override
 public void onCompletion(CompletionEvent event) {
  do {} while (false);
  List<String> tags=event.getTrackingTags();
  if ((tags!=null)&&(!tags.isEmpty())) {

   if (globals!=null) globals.checkin_completiontag(tags.get(0));
  }
  event.dismiss();
 }
}
