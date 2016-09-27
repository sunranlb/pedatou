package com.sr.pedatou.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sr.pedatou.R;
import com.sr.pedatou.service.AlarmService;
import com.sr.pedatou.service.AlarmService.AlarmBinder;
import com.sr.pedatou.util.Note;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.sr.pedatou.util.Tools.transDB2RV;

public class AlarmActivity extends Activity implements View.OnClickListener {

    @BindView(R.id.al_title)
    TextView titleTV;
    @BindView(R.id.al_context)
    TextView contentTV;
    @BindView(R.id.ad_btn_2)
    Button d2mBtn;
    @BindView(R.id.ad_btn_5)
    Button d5mBtn;
    @BindView(R.id.ad_btn_10)
    Button d10mBtn;
    @BindView(R.id.ad_btn_30)
    Button d30mBtn;
    @BindView(R.id.ad_btn_1h)
    Button d1hBtn;
    @BindView(R.id.ad_btn_ok)
    Button okBtn;
    private NotificationManager manager;
    private boolean isBindService = false;
    private String time;
    private String content;
    private int id = 0;

    private AlarmService alarmService;
    private PendingIntent pi;
    private AlarmManager alm;

    private ServiceConnection alarmServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AlarmBinder b = (AlarmBinder) service;
            alarmService = b.getService();
            isBindService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("AlarmA onServiceDisconnected!");
            isBindService = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm);
        ButterKnife.bind(this);
        System.out.println("AlarmA onCreate!");
        initView();

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent = getIntent();
        time = intent.getStringExtra("messageTitle");
        content = intent.getStringExtra("messageContent");
        id = intent.getIntExtra("id", 0);
        System.out.println("AlarmA:id = " + id + ",time = " + time + ", c = "
                + content);
        titleTV.setText(transDB2RV(time));
        contentTV.setText(content);

        setNotification(transDB2RV(time), content, id);

    }

    @Override
    protected void onStart() {
        System.out.println("AlarmA onStart!");
        if (!isBindService) {
            Intent i = new Intent(AlarmActivity.this, AlarmService.class);
            bindService(i, alarmServiceConnection, Context.BIND_AUTO_CREATE);
            isBindService = true;
            System.out.println("AlarmA onStart:isBindService = "
                    + isBindService);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        System.out.println("AlarmA onStop!");
        if (isBindService) {
            unbindService(alarmServiceConnection);
            isBindService = false;
            System.out.println("AlarmA onStop:isBind = " + isBindService);
        }
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                return true;
            case KeyEvent.KEYCODE_BACK:
                return true;
            case KeyEvent.KEYCODE_MENU:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @OnClick({R.id.ad_btn_2, R.id.ad_btn_5, R.id.ad_btn_10, R.id.ad_btn_30, R.id.ad_btn_1h, R.id.ad_btn_ok})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ad_btn_2:
                System.out.println("2m btn: id = " + id);
                btnSetDelayAlarm(2, id);
                break;
            case R.id.ad_btn_5:
                System.out.println("5m btn: id = " + id);
                btnSetDelayAlarm(5, id);
                break;
            case R.id.ad_btn_10:
                System.out.println("10m btn: id = " + id);
                btnSetDelayAlarm(10, id);
                break;
            case R.id.ad_btn_30:
                System.out.println("30m btn: id = " + id);
                btnSetDelayAlarm(30, id);
                break;
            case R.id.ad_btn_1h:
                System.out.println("1h btn: id = " + id);
                btnSetDelayAlarm(60, id);
                break;
            case R.id.ad_btn_ok:
                System.out.println("OK btn: id = " + id);
                manager.cancel(id);
                finish();
                break;
        }
    }

    // TODO
    private void btnSetDelayAlarm(int delayMin, int id) {
        Note n = new Note();
        n.setContent(content);
        n.setTime(time);
        n.setId(id);
        alarmService.setDelayAlarm(delayMin, n);
        manager.cancel(id);
        finish();
    }

    private void initView() {
        titleTV = (TextView) findViewById(R.id.al_title);
        contentTV = (TextView) findViewById(R.id.al_context);
        okBtn = (Button) findViewById(R.id.ad_btn_ok);
        d2mBtn = (Button) findViewById(R.id.ad_btn_2);
        d5mBtn = (Button) findViewById(R.id.ad_btn_5);
        d10mBtn = (Button) findViewById(R.id.ad_btn_10);
        d30mBtn = (Button) findViewById(R.id.ad_btn_30);
        d1hBtn = (Button) findViewById(R.id.ad_btn_1h);

        okBtn.setOnClickListener(this);
        d2mBtn.setOnClickListener(this);
        d5mBtn.setOnClickListener(this);
        d10mBtn.setOnClickListener(this);
        d30mBtn.setOnClickListener(this);
        d1hBtn.setOnClickListener(this);
    }

    private void setNotification(String title, String content, int id) {

        Notification n;

        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        i.setClass(this, AlarmActivity.class);
        i.putExtra("messageTitle", title);
        i.putExtra("messageContent", content);
        i.putExtra("id", id);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pi = PendingIntent.getActivity(this, id, i,
                PendingIntent.FLAG_UPDATE_CURRENT);
        n = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pi)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();
//        n = new Notification(R.drawable.red_cross24, content,
//                System.currentTimeMillis());
//        n.setLatestEventInfo(this, title, content, pi);
//        n.defaults = Notification.DEFAULT_ALL;

        manager.notify(id, n);
    }


}