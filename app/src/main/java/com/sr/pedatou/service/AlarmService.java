package com.sr.pedatou.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.sr.pedatou.dao.NoteDAO;
import com.sr.pedatou.util.Note;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class AlarmService extends Service {
    private final static String TAG = "AlarmService";
    private PendingIntent pi;
    private AlarmManager alm;
    private AlarmBinder ab = new AlarmBinder();

    @Override
    public void onCreate() {
        System.out.println("AS onCreate!");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("AS onStartCommand!");
        try {
//            System.out.println("AS onStartCommand: " + intent.getAction());

            if (intent.getAction().equals("com.sr.pedatou.ACTION_BOOT_COMPLETE")) {
                bootSet();
            }

        } catch (Exception e) {

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("AS onBind!");
        return ab;
    }

    @Override
    public void onDestroy() {
        System.out.println("AS onDestry!!!");
        super.onDestroy();
    }

    // XXX
    /*
     * When device completes booting, set future alarms
	 */
    public void bootSet() {

        NoteDAO dao = new NoteDAO(this);
        List<Note> allNotes = dao.getAll();

        int setBeginIndex = findFutureBeginIndex(allNotes);

        if (setBeginIndex != -1) {
            System.out.println("notes number:" + (allNotes.size() - setBeginIndex));

            while (setBeginIndex < allNotes.size()) {
                String t = allNotes.get(setBeginIndex).getTime();
                int year = Integer.parseInt(t.substring(0, 4));
                int month = Integer.parseInt(t.substring(4, 6));
                int day = Integer.parseInt(t.substring(6, 8));
                int hour = Integer.parseInt(t.substring(8, 10));
                int min = Integer.parseInt(t.substring(10, 12));

                setAlarm(year, month, day, hour, min,
                        allNotes.get(setBeginIndex));
                setBeginIndex++;

            }

        }
    }

    private int findFutureBeginIndex(List<Note> notes) {
        Date now = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        String nowDate = formatter.format(now);
        long nowLong = Long.parseLong(nowDate) - 1000000l;
        System.out.println("now long = " + nowLong);
        // if all notes are before now, return -1
        if (Long.parseLong(notes.get(notes.size() - 1).getTime()) < nowLong) {
            System.out.println("AS:All notes are before now!");
            return -1;
        }
        int l = 0, r = notes.size() - 1;
        while (l < r) {
            int m = (r + l) / 2;
            if (Long.parseLong(notes.get(m).getTime()) < nowLong)
                l = m + 1;
            else
                r = m;
        }
        return r;
    }

    public void setAlarm(int year, int month, int day, int hour, int min, Note n) {
        Calendar c2 = Calendar.getInstance();
        c2.set(year, month, day, hour, min);
        Calendar c = Calendar.getInstance();
        System.out.println("" + (c2.getTimeInMillis() - c.getTimeInMillis()));

        Intent i = new Intent();
        i.setClass(this, AlarmReceiver.class);
        i.setAction("com.sr.pedatou.ACTION_SET_ALARM");
        i.putExtra("messageTitle", n.getTime());
        i.putExtra("messageContent", n.getContent());
        i.putExtra("id", n.getId());
        System.out.println("AS:id = " + n.getId() + ",time = " + n.getTime()
                + ", c =" + n.getContent());
        // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // pi = PendingIntent.getActivity(this, n.getId(), i,
        // 0);
        pi = PendingIntent.getBroadcast(this, n.getId(), i,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alm.set(AlarmManager.RTC_WAKEUP, c2.getTimeInMillis(), pi);
    }

    public void deleteAlarm(Note n) {
        System.out.println("Service.deleteAlarm : id = " + n.getId());
        Intent i = new Intent();
        i.setClass(this, AlarmReceiver.class);
        i.setAction("com.sr.pedatou.ACTION_SET_ALARM");
        i.putExtra("messageTitle", n.getTime());
        i.putExtra("messageContent", n.getContent());
        i.putExtra("id", n.getId());
        pi = PendingIntent.getBroadcast(this, n.getId(), i,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alm.cancel(pi);
    }

    public void setDelayAlarm(int delayMin, Note n) {
        Calendar c = Calendar.getInstance();
        long delayMillis = delayMin * 60000l;
        System.out.println("AS:setDelayAlarm:delayMillis = " + delayMillis);

        Intent i = new Intent();
        i.setClass(this, AlarmReceiver.class);
        i.setAction("com.sr.pedatou.ACTION_SET_ALARM");
        i.putExtra("messageTitle", n.getTime());
        i.putExtra("messageContent", "[Delay " + delayMin + " min] " + n.getContent());
        i.putExtra("id", n.getId());
        System.out.println("AS:id = " + n.getId() + ",time = " + n.getTime()
                + ", c =" + n.getContent());
        pi = PendingIntent.getBroadcast(this, n.getId(), i,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alm.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis() + delayMillis, pi);
    }

    // service通过binder暴露给可以使用的函数
    public class AlarmBinder extends Binder {
        public AlarmService getService() {
            return AlarmService.this;
        }
    }

}
