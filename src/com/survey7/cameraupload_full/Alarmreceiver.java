package com.survey7.cameraupload_full;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;
import android.util.Log;
import java.text.DateFormat;
import java.util.Date;

public class Alarmreceiver extends BroadcastReceiver {
 public static void stopalarm(Context context) {
  AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
  Intent intent=new Intent(context,Alarmreceiver.class);
  PendingIntent pi=PendingIntent.getBroadcast(context,0,intent,0);
  am.cancel(pi);
 }
 public static void startalarm(Context context, int firstdelay, int repeatdelay) {
  AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
  Intent intent=new Intent(context,Alarmreceiver.class);
  intent.putExtra("count",0L);
  long firststart=System.currentTimeMillis()+firstdelay;
  intent.putExtra("target",firststart);
  intent.putExtra("period",repeatdelay);
  PendingIntent pi=PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
  am.set(AlarmManager.RTC_WAKEUP,firststart,pi);
 }
 @Override
 public void onReceive(Context context, Intent intent) {
  PowerManager pm=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
  AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);


  PowerManager.WakeLock wl=pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK


    |PowerManager.ACQUIRE_CAUSES_WAKEUP
    |PowerManager.ON_AFTER_RELEASE, Alarmreceiver.class.getPackage().getName());
  wl.acquire();

  long count=1+intent.getLongExtra("count",0L);
  long target=intent.getLongExtra("target",0L);
  if (target==0) { Log.e("DEBUG","Couldn't find intent.target, giving up"); return; }
  int period=intent.getIntExtra("period",0);
  target+=period;

  intent.putExtra("count",count);
  intent.putExtra("target",target);

  PendingIntent pi=PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
  if (period>0) am.set(AlarmManager.RTC_WAKEUP,target,pi);

  startintent(context,Waiting.class,count);
  wl.release();
    }
 private void startintent(Context context, Class cl, long count) {
  Intent intent=new Intent();
  intent.setClass(context,cl);
  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  intent.putExtra("count",count);
  context.startActivity(intent);
 }
}
