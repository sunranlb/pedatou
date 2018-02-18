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
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sr.pedatou.R;
import com.sr.pedatou.adapter.HeaderAdapterOption;
import com.sr.pedatou.adapter.HeaderRecycleAdapter;
import com.sr.pedatou.adapter.HeaderRecycleViewHolder;
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

//import com.sr.pedatou.adapter.RVAdapter;

public class MainActivity extends AppCompatActivity {
    static final public String TAG = "MA";
    private static boolean isBindService = false;
    private static int screenWidth = 0, screenHeight = 0;
    private static float screenDensity = 0;
    @BindView(R.id.rv)
    RecyclerView rv;
    @BindView(R.id.main_activity_toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_add_btn)
    ImageButton toolbarAddBtn;
    @BindView(R.id.left_drawer)
    RelativeLayout leftDrawer;
    @BindView(R.id.left_drawer_add)
    Button leftDrawerAdd;
    private HeaderRecycleAdapter mHeaderRVAdapter = null;
    private StickHeaderItemDecoration mStickDecoration = null;
    private RVAdapter rvAdapter;
    private NoteDAO dao;
    private MyLinearLayoutManager layoutManager;
    private Typeface noteTypeface;
    private Typeface headerTypeface;
    private AlarmService alarmService;
    private List<List<Note>> mGroupList;
    private static Map<Integer, String> mHeaderMap;
    private int todayPosition;
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

    private HeaderRecycleViewHolder.OnItemClickListener onNoteClickListener = new
            HeaderRecycleViewHolder.OnItemClickListener() {
        @Override
        public void onItemClick(int groupId, int childId, int position, int viewId, boolean
                isHeader, View rootView, HeaderRecycleViewHolder holder) {
            Intent i = new Intent();
            i.setClass(MainActivity.this, AddActivity.class);
            Note n = (Note) mHeaderRVAdapter.getItem(groupId, childId);
            System.out.println("onItemClick: Note's id = " + n.getId());
            i.putExtra("id", n.getId());
            startActivityForResult(i, BIND_AUTO_CREATE);
        }

        @Override
        public void onItemLongClick(final int groupId, final int childId, final int position, int
                viewId, boolean isHeader, View rootView, HeaderRecycleViewHolder holder) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Delete It???").setNegativeButton("CANCEL", null);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    Note n = (Note) mHeaderRVAdapter.getItem(groupId, childId);
                    alarmService.deleteAlarm(n);
                    dao.detele(n.getId());
                    mHeaderRVAdapter.remove(groupId, childId, position, n);
                    Toast.makeText(MainActivity.this, "Deleted note at " + Tools.transDB2RV(n
                            .getTime()), Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
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
        windowInit();
        initSize();
        leftDrawerAdd.setOnTouchListener(addListener);
        initToolbar();
        dao = new NoteDAO(MainActivity.this);

    }

    private void windowInit() {
        WindowManager wm = this.getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        screenDensity = metric.density;
    }

    private void initSize() {
        ViewGroup.LayoutParams l = leftDrawer.getLayoutParams();
        l.width = screenWidth / 3 * 2;
        leftDrawer.setLayoutParams(l);
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
        startIntroAnimation();
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
                Toast.makeText(this, "Added!", Toast.LENGTH_SHORT).show();
            } else if (str.equals("2")) { //change one content
                changeOneNoteContent();
            } else if (str.equals("3")) { //change content but has the same time
                int toDeleteId = Integer.parseInt(bundle.getString("toDelete"));
                removeOneNoteById(toDeleteId);
                changeOneNoteContent();
            } else if (str.equals("4")) { // change content but has new time
                int toDeleteId = Integer.parseInt(bundle.getString("toDelete"));
                removeOneNoteById(toDeleteId);
                addNewFromDB();
            }
        }
    }

    private void removeOneNoteById(int toDeleteId) {
        int groupId = 0, childId = 0, pos = 0;
        int groupSize = mGroupList.size(), childSize;
        for (; groupId < groupSize; ++groupId) {
            List<Note> adapterGroupList = mGroupList.get(groupId);
            childSize = adapterGroupList.size();
            childId = 0;
            for (; childId < childSize; ++childId) {
                if (adapterGroupList.get(childId).getId() == toDeleteId) {
                    pos++;
                    mHeaderRVAdapter.remove(groupId, childId, pos, adapterGroupList.get(childId));
                    return;
                }
                pos++;
            }
            pos++;
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

    private Note findNewNote(List<Note> dbList, List<Note> adapterList) {
        Note addedNote;
        int adapterDatalistSize = adapterList.size(), i = 0;
        for (; i < adapterDatalistSize; ++i) {
            if (!(dbList.get(i).getTime().equals(adapterList.get(i).getTime()))) {
                addedNote = dbList.get(i);
                return addedNote;
            }
        }
        return i == adapterDatalistSize ? null : dbList.get(i);
    }

    private void changeOneNoteContent() {
        List<Note> tmp = dao.getAll();
        int groupId = 0, childId = 0, pos = 0, tmpI = 0;
        int groupSize = mGroupList.size(), childSize;
        for (; groupId < groupSize; ++groupId) {
            List<Note> adapterGroupList = mGroupList.get(groupId);
            childSize = adapterGroupList.size();
            childId = 0;
            for (; childId < childSize; ++childId) {
                if (!(tmp.get(tmpI).getContent().equals(adapterGroupList.get(childId).getContent
                        ()))) {
                    pos++;
                    adapterGroupList.get(childId).setContent(tmp.get(tmpI).getContent());
                    mHeaderRVAdapter.changeOneNoteContent(pos, tmp);
                    return;
                }
                pos++;
                tmpI++;
            }
            pos++;
        }
    }

    private void addNewFromDB() {
        List<Note> daoList = dao.getAll();
        List<Note> adapterList = mHeaderRVAdapter.getList();
        Note addedNote = findNewNote(daoList, adapterList);
        if (addedNote == null) return;

//        setGroupListAndHeaderMap(daoList);
        List<List<Note>> adapterDatalist = mHeaderRVAdapter.getGroupList();

        int groupId = 0, childId = 0, pos = 0;
        int groupSize = adapterDatalist.size(), childSize;
        for (; groupId < groupSize; ++groupId) {
            List<Note> adapterGroupList = adapterDatalist.get(groupId);
            childSize = adapterGroupList.size();
            childId = 0;
            for (; childId < childSize; ++childId) {
                if (addedNote.getTime().equals(adapterGroupList.get(childId).getTime())) {
                    pos++;
                    mHeaderRVAdapter.add(groupId, childId, pos, adapterDatalist, daoList);
                    return;
                }
                pos++;
            }
            pos++;
        }
        pos++;
        mHeaderRVAdapter.add(groupId, childId, pos, adapterDatalist, daoList);
    }

    private void initRV() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                0, 0, 0);

        List<Note> dataList = dao.getFromDay(cal);
        noteTypeface = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Light.ttf");
        headerTypeface = Typeface.createFromAsset(this.getAssets(), "fonts/Pacifico.ttf");
        mGroupList = new LinkedList<>();

        if (mHeaderMap == null) {
            mHeaderMap = new ArrayMap<>();
//            System.out.println("is Empty!!");
//            mHeaderMap.put(0, "History");
            mHeaderMap.put(0, "Today");
            mHeaderMap.put(1, "Tomorrow");
            mHeaderMap.put(2, "Within One Week");
            mHeaderMap.put(3, "Within Two Weeks");
            mHeaderMap.put(4, "Farther Future");
        }
        setGroupListAndHeaderMap(dataList, cal);

        mHeaderRVAdapter = new HeaderRecycleAdapter<Note, String>(this, new HeaderAdapterOption
                (false, true), mGroupList, mHeaderMap, dataList, noteTypeface, headerTypeface,
                onNoteClickListener);
        layoutManager = new MyLinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(mHeaderRVAdapter);
        if (mStickDecoration == null) {
            mStickDecoration = new StickHeaderItemDecoration(mHeaderRVAdapter);
            rv.addItemDecoration(mStickDecoration);
            rv.setItemAnimator(new MyItemAnimator());
        }
        rv.scrollToPosition(todayPosition);
//        oldset(dataList);

    }

    private void oldset(List<Note> dataList) {
        layoutManager = new MyLinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(layoutManager);
        rvAdapter = new RVAdapter(dataList, noteTypeface);

//        rvAdapter.setOnRecyclerViewListener(this);
        rv.setAdapter(rvAdapter);
//        rv.setItemAnimator(new MyItemAnimator());

    }

    // 设置列表数组和header数组，并确定today的位置
    private void setGroupListAndHeaderMap(List<Note> dataList, Calendar cal) {
        mGroupList.clear();

        int listSize = dataList.size();
        int i = 0;
        todayPosition = 0;
//        List<Note> historyList = new ArrayList<Note>();
        List<Note> todayList = new ArrayList<Note>();
        List<Note> tomorrowList = new ArrayList<Note>();
        List<Note> oneweekList = new ArrayList<Note>();
        List<Note> twoweekList = new ArrayList<Note>();
        List<Note> fartherfutureList = new ArrayList<Note>();
//        for (; i < listSize; ++i) {
//            Calendar t = Tools.dbToCalendarAccurateToDay(dataList.get(i).getTime());
//            if (t.compareTo(cal) >= 0) break;
//            historyList.add(dataList.get(i));
//        }
        todayPosition += i + 1;
        cal.add(Calendar.DAY_OF_MONTH, 1);
        for (; i < listSize; ++i) {
            Calendar t = Tools.dbToCalendarAccurateToDay(dataList.get(i).getTime());
            if (t.compareTo(cal) >= 0) break;
            todayList.add(dataList.get(i));
        }
        cal.add(Calendar.DAY_OF_MONTH, 1);
        for (; i < listSize; ++i) {
            Calendar t = Tools.dbToCalendarAccurateToDay(dataList.get(i).getTime());
            if (t.compareTo(cal) >= 0) break;
            tomorrowList.add(dataList.get(i));
        }
        cal.add(Calendar.DAY_OF_MONTH, 5);
        for (; i < listSize; ++i) {
            Calendar t = Tools.dbToCalendarAccurateToDay(dataList.get(i).getTime());
            if (t.compareTo(cal) >= 0) break;
            oneweekList.add(dataList.get(i));
        }
        cal.add(Calendar.DAY_OF_MONTH, 7);
        for (; i < listSize; ++i) {
            Calendar t = Tools.dbToCalendarAccurateToDay(dataList.get(i).getTime());
            if (t.compareTo(cal) >= 0) break;
            twoweekList.add(dataList.get(i));
        }
        for (; i < listSize; ++i) {
            fartherfutureList.add(dataList.get(i));
        }
//        mGroupList.add(historyList);
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

    @OnClick({R.id.toolbar_add_btn, R.id.left_drawer_add})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar_add_btn:
                Intent i = new Intent();
                i.setClass(MainActivity.this, AddActivity.class);
                startActivityForResult(i, BIND_AUTO_CREATE);
                break;
            case R.id.left_drawer_add:
                System.out.println("onClick");
                break;
        }
    }

    private View.OnTouchListener addListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    System.out.println("onTouch-down");
                case MotionEvent.ACTION_UP:
                    System.out.println("onTouch-up");
                    return true;
            }
            return true;
        }
    };
//    @OnTouch({R.id.left_drawer_add})
//    public boolean onTouch(View v, MotionEvent event) {
//        switch (v.getId()) {
//            case R.id.left_drawer_add:
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        System.out.println("onTouch-down");
//                    case MotionEvent.ACTION_UP:
//                        System.out.println("onTouch-up");
//                        return true;
//                }
//                break;
//        }
//
//        return false;
//    }
}
