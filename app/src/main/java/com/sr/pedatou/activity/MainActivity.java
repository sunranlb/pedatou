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
import android.support.v4.util.ArrayMap;
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
import com.sr.pedatou.adapter.HeaderAdapterOption;
import com.sr.pedatou.adapter.HeaderRecycleAdapter;
//import com.sr.pedatou.adapter.RVAdapter;
import com.sr.pedatou.adapter.RVAdapter;
import com.sr.pedatou.dao.NoteDAO;
import com.sr.pedatou.others.MyItemAnimator;
import com.sr.pedatou.others.MyLinearLayoutManager;
import com.sr.pedatou.others.StickHeaderItemDecoration;
import com.sr.pedatou.service.AlarmService;
import com.sr.pedatou.util.Note;
import com.sr.pedatou.util.Tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    static final public String TAG = "MA";
    private static boolean isBindService = false;
    @BindView(R.id.rv)
    RecyclerView rv;
    @BindView(R.id.main_activity_toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_add_btn)
    ImageButton toolbarAddBtn;
    HeaderRecycleAdapter mColorAdapter = null;
    StickHeaderItemDecoration mStickDecoration = null;
    private RVAdapter rvAdapter;
    private NoteDAO dao;
    private MyLinearLayoutManager layoutManager;
    private Typeface typeface;
    private AlarmService alarmService;
    private List<List<Note>> mGroupList = null;
    private Map<Integer, String> mHeaderMap = new ArrayMap<Integer, String>();
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
//                addNewFromDB();
            } else if (str.equals("2")) { //change one content
//                changeOneNoteContent();
            } else if (str.equals("3")) { //change content but has the same time
                int toDeleteId = Integer.parseInt(bundle.getString("toDelete"));
//                rvAdapter.removeById(toDeleteId);
//                changeOneNoteContent();
            } else if (str.equals("4")) { // change content but has new time
                int toDeleteId = Integer.parseInt(bundle.getString("toDelete"));
//                rvAdapter.removeById(toDeleteId);
//                addNewFromDB();
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

//    private void changeOneNoteContent() {
//        List<Note> tmp = dao.findAll();
//        List<Note> adapterDataList = rvAdapter.getDataList();
//        int i = 0, s = adapterDataList.size();
//        for (; i < s; ++i) {
//            if (!tmp.get(i).getContent().equals(adapterDataList.get(i).getContent())) {
//                break;
//            }
//        }
//        if (i != s) rvAdapter.changeOneNoteContent(i, tmp.get(i).getContent());
//    }

//    private void addNewFromDB() {
//        List<Note> tmp = dao.findAll();
//        List<Note> adapterDataList = rvAdapter.getDataList();
//        int i = 0, s = adapterDataList.size();
//        for (; i < s; ++i) {
//            if (!(tmp.get(i).getTime().equals(adapterDataList.get(i).getTime()))) break;
//        }
//        rvAdapter.add(i, tmp.get(i));
//    }

    private void initRV() {
        List<Note> dataList = dao.findAll();
        typeface = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Light.ttf");
        mGroupList = new LinkedList<List<Note>>();
        mHeaderMap = new ArrayMap<Integer, String>();
        initGroupListAndHeaderMap(dataList);

        for (int i = 0; i < mGroupList.size(); ++i) {
            System.out.println(mHeaderMap.get(i));
            System.out.println(mGroupList.get(i));
        }

        mColorAdapter = new HeaderRecycleAdapter<Note, String>(this, new HeaderAdapterOption
                (false, true), mGroupList, mHeaderMap, typeface);
        layoutManager = new MyLinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(mColorAdapter);
        mStickDecoration = new StickHeaderItemDecoration(mColorAdapter);
        rv.addItemDecoration(mStickDecoration);

        rv.setItemAnimator(new MyItemAnimator());
//        oldset(dataList);

    }

    private void oldset(List<Note> dataList) {
        layoutManager = new MyLinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(layoutManager);
        rvAdapter = new RVAdapter(dataList, typeface);

//        rvAdapter.setOnRecyclerViewListener(this);
        rv.setAdapter(rvAdapter);
        rv.setItemAnimator(new MyItemAnimator());

    }

    private void initGroupListAndHeaderMap(List<Note> dataList) {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                0, 0, 0);

        mHeaderMap.put(0, "History");
        mHeaderMap.put(1, "Today");
        mHeaderMap.put(2, "Tomorrow");
        mHeaderMap.put(3, "Within One Week");
        mHeaderMap.put(4, "Within Two Weeks");
        mHeaderMap.put(5, "Farther Future");

        int listSize = dataList.size();
        int i = 0;
        List<Note> historyList = new ArrayList<Note>();
        List<Note> todayList = new ArrayList<Note>();
        List<Note> tomorrowList = new ArrayList<Note>();
        List<Note> oneweekList = new ArrayList<Note>();
        List<Note> twoweekList = new ArrayList<Note>();
        List<Note> fartherfutureList = new ArrayList<Note>();
        for (; i < listSize; ++i) {
            Calendar t = Tools.dbToCalendar(dataList.get(i).getTime());
            if (t.compareTo(cal) >= 0) break;
            historyList.add(dataList.get(i));
        }
        cal.add(Calendar.DAY_OF_MONTH, 1);
        for (; i < listSize; ++i) {
            Calendar t = Tools.dbToCalendar(dataList.get(i).getTime());
            if (t.compareTo(cal) >= 0) break;
            todayList.add(dataList.get(i));
        }
        cal.add(Calendar.DAY_OF_MONTH, 1);
        for (; i < listSize; ++i) {
            Calendar t = Tools.dbToCalendar(dataList.get(i).getTime());
            if (t.compareTo(cal) >= 0) break;
            tomorrowList.add(dataList.get(i));
        }
        cal.add(Calendar.DAY_OF_MONTH, 5);
        for (; i < listSize; ++i) {
            Calendar t = Tools.dbToCalendar(dataList.get(i).getTime());
            if (t.compareTo(cal) >= 0) break;
            oneweekList.add(dataList.get(i));
        }
        cal.add(Calendar.DAY_OF_MONTH, 7);
        for (; i < listSize; ++i) {
            Calendar t = Tools.dbToCalendar(dataList.get(i).getTime());
            if (t.compareTo(cal) >= 0) break;
            twoweekList.add(dataList.get(i));
        }
        cal.add(Calendar.DAY_OF_MONTH, 1);
        for (; i < listSize; ++i) {
            Calendar t = Tools.dbToCalendar(dataList.get(i).getTime());
            if (t.compareTo(cal) >= 0) break;
            fartherfutureList.add(dataList.get(i));
        }
        mGroupList.add(historyList);
        mGroupList.add(todayList);
        mGroupList.add(tomorrowList);
        mGroupList.add(oneweekList);
        mGroupList.add(twoweekList);
        mGroupList.add(fartherfutureList);

    }

//    @Override
//    public void onItemClick(int position) {
//        Intent i = new Intent();
//        i.setClass(MainActivity.this, AddActivity.class);
//        i.putExtra("id", rvAdapter.getItem(position).getId());
//        startActivityForResult(i, BIND_AUTO_CREATE);
//    }
//
//    @Override
//    public boolean onItemLongClick(final int position) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        builder.setTitle("Delete It???").setNegativeButton("CANCEL", null);
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//
//            public void onClick(DialogInterface dialog, int which) {
//                alarmService.deleteAlarm(rvAdapter.getDataList().get(position));
//                dao.detele(rvAdapter.getDataList().get(position).getId());
//                rvAdapter.remove(position);
//            }
//        });
//        builder.show();
//        return true;
//    }

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
