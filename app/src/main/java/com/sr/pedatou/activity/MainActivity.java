package com.sr.pedatou.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sr.pedatou.R;
import com.sr.pedatou.dao.NoteDAO;
import com.sr.pedatou.service.AlarmService;
import com.sr.pedatou.util.Note;
import com.sr.pedatou.util.Tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements RVAdapter.OnRecyclerViewListener {
    static final public String TAG = "MA";
    @BindView(R.id.rv)
    RecyclerView rv;
    @BindView(R.id.main_activity_toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_add_btn)
    ImageButton toolbarAddBtn;
    private RVAdapter rvAdapter;
    private NoteDAO dao;
    private MyLinearLayoutManager layoutManager;
    private static boolean isBindService = false;
    private AlarmService alarmService;

    private ServiceConnection alarmServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AlarmService.AlarmBinder b = (AlarmService.AlarmBinder) service;
            alarmService = b.getService();
            isBindService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
//            System.out.println("AddA onServiceDisconnected!");
            isBindService = false;
        }
    };


    @Override
    public void onStop() {
        if (isBindService) {
            unbindService(alarmServiceConnection);
            isBindService = false;
//            System.out.println("AddA onStop:isBind = " + isBindService);
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initToolbar();
        dao = new NoteDAO(MainActivity.this);

        layoutManager = new MyLinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(layoutManager);
        startIntroAnimation();

    }

    @Override
    protected void onStart() {
        startService(new Intent(MainActivity.this, AlarmService.class));
        if (!isBindService) {
            Intent i = new Intent(MainActivity.this, AlarmService.class);
            bindService(i, alarmServiceConnection, Context.BIND_AUTO_CREATE);
            isBindService = true;
        }
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main_activity_tool_bar_menu, menu);
//        toolbarAddBtn = menu.findItem(R.id.toolbar_add);
//        toolbarAddBtn.setActionView(R.layout.toolbar_menu_item_view);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        String str = bundle.getString("needRefreshLV");
        if (str != null) {
            if (str.equals("1")) { // added
                addNewFromDB();
            } else if (str.equals("2")) { //change one content
                changeOneNoteContent();
            } else if (str.equals("3")) { //change content but has the same time
                int toDeleteId = Integer.parseInt(bundle.getString("toDelete"));
                rvAdapter.removeById(toDeleteId);
                changeOneNoteContent();
            } else if (str.equals("4")) { // change content but has new time
                int toDeleteId = Integer.parseInt(bundle.getString("toDelete"));
                rvAdapter.removeById(toDeleteId);
                addNewFromDB();
            }
        }
    }

    private void startIntroAnimation() {

        int actionbarSize = (int) (56 * Resources.getSystem().getDisplayMetrics().density);
        toolbar.setTranslationY(-actionbarSize);
        toolbarTitle.setTranslationY(-actionbarSize);
        toolbarAddBtn.setTranslationY(-actionbarSize);
        toolbar.animate().translationY(0).setDuration(600).setStartDelay(0);
        toolbarTitle.animate().translationY(0).setDuration(300).setStartDelay(300);
        toolbarAddBtn.animate().translationY(0).setDuration(300).setStartDelay(600).setListener
                (new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                initRV();
            }
        }).start();
    }

    private void initToolbar() {
        toolbarTitle.setText("pedatou");
        setSupportActionBar(toolbar);
    }

    private void changeOneNoteContent() {
        List<Note> tmp = dao.findAll();
        List<Note> adapterDataList = rvAdapter.getDataList();
        int i = 0, s = adapterDataList.size();
        for (; i < s; ++i) {
            if (!tmp.get(i).getContent().equals(adapterDataList.get(i).getContent())) {
                break;
            }
        }
        if (i != s) rvAdapter.changeOneNoteContent(i, tmp.get(i).getContent());
    }

    private void addNewFromDB() {
        List<Note> tmp = dao.findAll();
        List<Note> adapterDataList = rvAdapter.getDataList();
        int i = 0, s = adapterDataList.size();
        for (; i < s; ++i) {
            if (!(tmp.get(i).getTime().equals(adapterDataList.get(i).getTime()))) break;
        }
        rvAdapter.add(i, tmp.get(i));
    }

    private int getTodayPosition(List<Note> dataList) {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        int listSize = dataList.size();
        int i = 0;
        for (; i < listSize; ++i) {
            Calendar t = Tools.dbToCalendar(dataList.get(i).getTime());
            if (t.compareTo(cal) >= 0) break;
        }
        return i;
    }
    private void initRV() {
        List<Note> dataList = dao.findAll();
        int todayPosition = getTodayPosition(dataList);
//        System.out.println("" + todayPosition);
        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Light.ttf");
        rvAdapter = new RVAdapter(typeface);
        rvAdapter.setOnRecyclerViewListener(this);
        rv.setAdapter(rvAdapter);
        rvAdapter.addList(dataList);
        rv.setItemAnimator(new MyItemAnimator());
        rv.scrollToPosition(todayPosition);
    }

    @Override
    public void onItemClick(int position) {
        Intent i = new Intent();
        i.setClass(MainActivity.this, AddActivity.class);
        i.putExtra("id", rvAdapter.getItem(position).getId());
        startActivityForResult(i, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onItemLongClick(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Delete It???").setNegativeButton("CANCEL", null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                alarmService.deleteAlarm(rvAdapter.getDataList().get(position));
                dao.detele(rvAdapter.getDataList().get(position).getId());
                rvAdapter.remove(position);
            }
        });
        builder.show();
        return true;
    }

    @OnClick({R.id.toolbar_add_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar_add_btn:
                Intent i = new Intent();
                i.setClass(MainActivity.this, AddActivity.class);
                startActivityForResult(i, BIND_AUTO_CREATE);
                break;
        }
    }


}
