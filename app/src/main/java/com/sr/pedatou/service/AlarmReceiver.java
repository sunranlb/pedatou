package com.sr.pedatou.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.sr.pedatou.activity.AddActivity;
import com.sr.pedatou.activity.AlarmActivity;
import com.sr.pedatou.activity.MainActivity;
import com.sr.pedatou.dao.NoteDAO;
import com.sr.pedatou.service.AlarmService.AlarmBinder;
import com.sr.pedatou.util.Note;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    private AlarmService alarmService;

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("AR onReceive");
        if (intent.getAction().equals("com.sr.pedatou.ACTION_SET_ALARM")) {
            System.out.println(intent.getAction());

            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> list = am.getRunningTasks(100);
            boolean isRunning = false;
            for (RunningTaskInfo info : list) {
                if (info.topActivity.getPackageName().equals("com.sr.pedatou")
                        || info.baseActivity.getPackageName().equals(
                        "com.sr.pedatou")) {
                    isRunning = true;
                    break;
                }
            }
            if (!isRunning) {
                System.out.println("-----start MA----");
                Intent i = new Intent();
                i.setClass(context, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }

            Intent in = new Intent();
            in.setClass(context, AlarmActivity.class);
            in.putExtra("messageTitle", intent.getStringExtra("messageTitle"));
            in.putExtra("messageContent",
                    intent.getStringExtra("messageContent"));
            in.putExtra("id", intent.getIntExtra("id", 0));
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(in);
        } else if (intent.getAction().equals("com.sr.pedatou.ACTION_TEST")) {
            System.out.println("AR received:" + intent.getAction());


        } else {
            System.out.println("AR received:" + intent.getAction());
            context.startService(new Intent(context, AlarmService.class));
            Intent i = new Intent(context, AlarmService.class);
            i.setAction("com.sr.pedatou.ACTION_BOOT_COMPLETE");
            context.startService(i);
        }
    }


}
