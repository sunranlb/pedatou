package com.sr.pedatou.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TimePicker;

import com.sr.pedatou.R;
import com.sr.pedatou.dao.NoteDAO;
import com.sr.pedatou.service.AlarmService;
import com.sr.pedatou.service.AlarmService.AlarmBinder;
import com.sr.pedatou.util.Note;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.sr.pedatou.util.Tools.transDB2RV;

public class AddActivity extends AppCompatActivity {
    private static final String TAG = "AddActivity";
    private static int screenWidth = 0, screenHeight = 0;
    private static float screenDensity = 0;
    private static int usualBtnWidth = 0, usualBtnHeight = 0;
    private static boolean redCrossShown = false;
    private static boolean isBindService = false;
    private static ArrayList<Button> usualBtnSet;
    private static ArrayList<ImageButton> redCrossSet;
    @BindView(R.id.add_datePicker)
    DatePicker datePicker;
    @BindView(R.id.add_timePicker)
    TimePicker timePicker;
    @BindView(R.id.add_rlDTPicker)
    RelativeLayout rlDTPicker;
    @BindView(R.id.add_gl_addBtn)
    ImageButton addUsualBtn;
    @BindView(R.id.add_gl)
    GridLayout gl;
    @BindView(R.id.add_flRC)
    FrameLayout flRC;
    @BindView(R.id.add_content)
    EditText editContent;
    @BindView(R.id.add_clear_text)
    ImageButton clearTextImgBtn;
    @BindView(R.id.add)
    RelativeLayout addActivityLayout;
    @BindView(R.id.add_activity_toolbar)
    Toolbar toolbar;
    @BindView(R.id.add_activity_scroll_view)
    ScrollView scrollView;
    private int globalT = 0; // detect which usualBtn is select to delete
    private int year, month, day, hour, min;
    private Note noteToUpdate;
    private NoteDAO dao = new NoteDAO(AddActivity.this);
    private boolean isToChangeNote = false;
    private Note noteToChange;

    private AlarmService alarmService;

    private OnTouchListener rmRCOTL = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (redCrossShown)
                        removeRedCross();
                case MotionEvent.ACTION_UP:
                    ;
            }
            if (!(v instanceof EditText && editContent.isFocused())) {
                editContent.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }

            return false;
        }
    };
    private OnTouchListener rmAUBOTL = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x = event.getRawX();
            float y = event.getRawY();
            int gx = (int) (x / (screenWidth / 4) - 0.5);
            int gy = (int) ((y - gl.getY()) / (screenHeight / 10));
            globalT = gx + (gy - 1) * 4;
            return false;
        }
    };

    private ServiceConnection alarmServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AlarmBinder b = (AlarmBinder) service;
            alarmService = b.getService();
            isBindService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("AddA onServiceDisconnected!");
            isBindService = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        ButterKnife.bind(this);
        windowInit();
        initToolBar();

        usualBtnSet = new ArrayList<Button>();
        redCrossSet = new ArrayList<ImageButton>();

        initDPTP();
        setRMRCOTL();
        initUsualBtn();

        Intent i = getIntent();
        if (i.getIntExtra("id", 0) != 0) {
            isToChangeNote = true;
            int changeNoteId = i.getIntExtra("id", 0);
            noteToChange = dao.findById(changeNoteId);
            String time = noteToChange.getTime();
            int year = Integer.parseInt(time.substring(0, 4));
            int month = Integer.parseInt(time.substring(4, 6));
            int day = Integer.parseInt(time.substring(6, 8));
            int hour = Integer.parseInt(time.substring(8, 10));
            int min = Integer.parseInt(time.substring(10, 12));
            datePicker.init(year, month, day, null);
            timePicker.setCurrentHour(hour);
            timePicker.setCurrentMinute(min);
            editContent.setText(noteToChange.getContent());

        }
    }

    @Override
    protected void onStart() {
//        System.out.println("AddA onStart!");
        if (!isBindService) {
            Intent i = new Intent(AddActivity.this, AlarmService.class);
            bindService(i, alarmServiceConnection, Context.BIND_AUTO_CREATE);
            isBindService = true;
//            System.out.println("AddA onStart:isBindService = " + isBindService);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
//        System.out.println("AddA onStop!");
        if (isBindService) {
            unbindService(alarmServiceConnection);
            isBindService = false;
//            System.out.println("AddA onStop:isBind = " + isBindService);
        }
        super.onStop();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (redCrossShown) {
                removeRedCross();
            } else {

                android.support.v7.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Back without Saving???").setNegativeButton(
                        "CANCEL", null);
                builder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Intent it = new Intent();
                                it.putExtra("needRefreshLV", "0");
                                setResult(CONTEXT_RESTRICTED, it);
                                finish();
                            }
                        });
                builder.show();
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_activity_tool_bar_menu, menu);
        return true;
    }

    // XXX
    private boolean containSameUsualBtn(String s) {
        for (int i = 0; i < usualBtnSet.size(); ++i) {
            if (usualBtnSet.get(i).getText().toString().equals(s))
                return true;
        }
        return false;
    }

    private void getTime() {
        year = datePicker.getYear();
        month = datePicker.getMonth();
        day = datePicker.getDayOfMonth();
        hour = timePicker.getCurrentHour();
        min = timePicker.getCurrentMinute();
    }

    private void setAlarm(Note n) {
        alarmService.setAlarm(year, month, day, hour, min, n);
    }

    private void deleteAlarm(Note n) {
        alarmService.deleteAlarm(n);
    }

    /*
    对已有的Note的Content进行追加，情况是用户在点击确定按钮后并不知道该时间已经有事件了
     */
    private void appendContent2DBAndFinish(String time) {
        String transContent = noteToUpdate.getContent() + " "
                + editContent.getText().toString();
        dao.update(noteToUpdate.getId(), transContent, time);
        Note n = new Note();
        n.setId(noteToUpdate.getId());
        n.setContent(transContent);
        n.setTime(noteToUpdate.getTime());
        setAlarm(n);

        Intent it = new Intent();
        if (isToChangeNote) {
            dao.detele(noteToChange.getId());
            it.putExtra("needRefreshLV", "3");
            it.putExtra("toDelete", "" + noteToChange.getId());
            deleteAlarm(noteToChange);
        } else {
            it.putExtra("needRefreshLV", "2");
        }
        setResult(CONTEXT_RESTRICTED, it);
        finish();
    }

    /*
    单纯增加一项新的事件
     */
    private void save2DBAndFinish(String time) {
        Note note = new Note();

        note.setContent(editContent.getText().toString());
        note.setTime(time);
        dao.add(note);
        note = dao.findByTime(time); // 重新寻找的作用是填充id项
        setAlarm(note);
        Intent it = new Intent();
        if (isToChangeNote) { // 进入这里的条件是用户点击RV修改了时间并且DB里没有该时间
            dao.detele(noteToChange.getId());
            it.putExtra("needRefreshLV", "4");
            it.putExtra("toDelete", "" + noteToChange.getId());
            deleteAlarm(noteToChange);
        } else {
            it.putExtra("needRefreshLV", "1");
        }
        setResult(CONTEXT_RESTRICTED, it);
        finish();
    }

    /*
    用户点击LV的一项并提出更改
     */
    private void changeContentAndFinish(String time) {
        noteToChange.setContent(editContent.getText().toString());
        noteToChange.setTime(time);
        dao.update(noteToChange.getId(), noteToChange.getContent(), time);
        setAlarm(noteToChange);

        Intent it = new Intent();
        it.putExtra("needRefreshLV", "2");
        setResult(CONTEXT_RESTRICTED, it);
        finish();
    }

    private void showHasSameTimeDialog(final String time) {

        noteToUpdate = dao.findByTime(time);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("There is something at this time. Update it???")
                .setMessage(
                        "Time:" + transDB2RV(time)
                                + "\nContent:" + noteToUpdate.getContent())
                .setNegativeButton("CANCEL", null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                appendContent2DBAndFinish(time);
            }
        });
        builder.show();

    }

    private String transTime2DB() {
        String tMonth;
        if (month < 10)
            tMonth = "0" + month;
        else
            tMonth = "" + month;

        String tDay;
        if (day < 10)
            tDay = "0" + day;
        else
            tDay = "" + day;

        String tHour;
        if (hour < 10)
            tHour = "0" + hour;
        else
            tHour = "" + hour;

        String tMin;
        if (min < 10)
            tMin = "0" + min;
        else
            tMin = "" + min;

        return "" + year + tMonth + tDay + tHour + tMin;
    }

    private void saveUsualBtn() {
        Editor sd = getSharedPreferences("UsualBtn", 0).edit();
        sd.clear();
        for (int i = 0; i < usualBtnSet.size(); ++i) {
            sd.putString("" + i, usualBtnSet.get(i).getText().toString());
        }
        sd.commit();
    }

    private void initUsualBtn() {
        SharedPreferences sd = getSharedPreferences("UsualBtn", 0);
        Map<String, ?> btns = sd.getAll();
        int num = btns.size();
        if (num != 0) {
            for (int i = num - 1; i >= 0; --i) {
                addUsualBtn(sd.getString("" + i, ""), false);
            }
        }
    }

    private void setRMRCOTL() {
        addActivityLayout.setOnTouchListener(rmRCOTL);
        gl.setOnTouchListener(rmRCOTL);
        rlDTPicker.setOnTouchListener(rmRCOTL);
        editContent.setOnTouchListener(rmRCOTL);
        addUsualBtn.setOnTouchListener(rmRCOTL);
        toolbar.setOnTouchListener(rmRCOTL);
    }

    private void reachedMaxUsualBtnNumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cannot add usual button anymore!!!")
                .setPositiveButton("OK", null);
        builder.show();
    }

    private void addUsualAlertDialog(String title) {

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        addUsualDialog();
                    }
                });
        builder.show();
    }

    private void initDPTP() {
        resizeDatePicker(datePicker);// 璋冩暣datepicker澶у皬
        resizeTimePicker(timePicker);// 璋冩暣timepicker澶у皬
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(Calendar.getInstance().get(
                Calendar.HOUR_OF_DAY));
        datePicker
                .setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        timePicker
                .setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
    }

    private void initToolBar() {
        toolbar.setTitle("Add Note");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                getTime();
                String transTime = transTime2DB();
                if (isToChangeNote) { //用户点击RV项进入的AddActivity
                    if (noteToChange.getTime().equals(transTime)) { //用户并没有修改时间，只修改了内容
                        changeContentAndFinish(transTime);
                    } else { //用户修改了时间
                        if (dao.findByTime(transTime) != null) {
                            //如果用户修改的时间已经有事件项目了，由于需要保证数据库时间的唯一性
                            //需要提醒用户已经有该时间的时间，弹出提示框询问是否添加
                            showHasSameTimeDialog(transTime);
                        } else //如果用户修改的时间在数据库中并没有
                            save2DBAndFinish(transTime);
                    }
                } else { //用户点击添加按钮进入的AddActivity
                    if (dao.findByTime(transTime) != null) { //如果添加的新事件的时间在数据库中已经有了
                        showHasSameTimeDialog(transTime);
                    } else
                        save2DBAndFinish(transTime);
                }
                return false;
            }
        });

        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (redCrossShown) {
                    removeRedCross();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            AddActivity.this);
                    builder.setTitle("Back without Saving???")
                            .setNegativeButton("CANCEL", null);
                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    Intent it = new Intent();
                                    it.putExtra("needRefreshLV", "0");
                                    setResult(CONTEXT_RESTRICTED, it);
                                    finish();
                                }
                            });
                    builder.show();
                }
            }
        });
    }

    private void windowInit() {
        WindowManager wm = this.getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        screenDensity = metric.density;
        usualBtnWidth = (int) ((screenWidth - 32 * screenDensity) / 4);
        usualBtnHeight = (int) (screenHeight / 10);

        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setHorizontalScrollBarEnabled(false);
    }

    private void removeRedCross() {
        flRC.removeAllViews();
        redCrossSet.clear();
        redCrossShown = false;
    }

    private void addRedCross() {
        GridLayout gl = (GridLayout) findViewById(R.id.add_gl);
        float gly = gl.getY();
        // Log.v(TAG, "x:"+glx+",y:"+gly);
        int btnNum = usualBtnSet.size();
        // Log.v(TAG, ""+screenDensity);
        for (int i = 0; i < btnNum; ++i) {
            globalT = i;
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            ImageButton imb = new ImageButton(AddActivity.this);
            imb.setBackgroundResource(R.drawable.red_cross24);
            imb.setLayoutParams(lp);
            imb.setX(usualBtnWidth * (i % 4 + 1) + (16 - 4 - 12 - 1)
                    * screenDensity);
            imb.setY(gly - 10 + usualBtnHeight * ((i - i % 4) / 4));

            imb.setOnTouchListener(rmAUBOTL);
            imb.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmDeleteDialog();
                }
            });

            redCrossSet.add(imb);
            flRC.addView(imb);
        }
        redCrossShown = true;
    }

    private void confirmDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
        builder.setTitle("Remove???").setNegativeButton("CANCEL", null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                removeRedCross();
                usualBtnSet.remove(globalT);
                saveUsualBtn();
                refreshGridLayout();
                addRedCross();
            }
        });
        builder.show();

    }

    private void addUsualDialog() {

        final EditText inputServer = new EditText(this);
        inputServer.setSingleLine();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a Usual Thing").setView(inputServer)
                .setNegativeButton("CANCEL", null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                String inputName = inputServer.getText().toString();
                if (inputName.equals("")) {
                    addUsualAlertDialog("You Can't Input Nothing!!!");
                } else if (containSameUsualBtn(inputName)) {
                    addUsualAlertDialog("\"" + inputName
                            + "\" already exists!!!");
                } else {
                    addUsualBtn(inputName, true);
                }

            }
        });
        builder.show();

        // 寤舵椂鏄剧ず閿洏
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm.isActive())
                    imm.showSoftInput(inputServer, 0); // 鏄剧ず杞敭鐩�
            }

        }, 200);
    }

    private void addUsualBtn(final String n, boolean needSave) {

        Button nbt = new Button(AddActivity.this);
        nbt.setText(n);
        nbt.setSingleLine();
        nbt.setWidth(usualBtnWidth);
        nbt.setHeight(usualBtnHeight);
        nbt.setOnTouchListener(rmRCOTL);
        nbt.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!redCrossShown) {
                    addRedCross();
                }
                return true;
            }

        });
        nbt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editContent.append(" " + n);
            }
        });
        usualBtnSet.add(0, nbt);
        if (needSave)
            saveUsualBtn();
        refreshGridLayout();

    }

    private void refreshGridLayout() {
        GridLayout gl = (GridLayout) findViewById(R.id.add_gl);
        gl.removeAllViews();
        // System.out.println(usualBtnSet);
        for (int i = 0; i < usualBtnSet.size(); ++i) {
            gl.addView(usualBtnSet.get(i));
        }
        gl.addView(addUsualBtn);
    }

    // private void setNumberPickerTextSize(ViewGroup viewGroup) {
    // List<NumberPicker> npList = findNumberPicker(viewGroup);
    // if (null != npList) {
    // for (NumberPicker np : npList) {
    // EditText et = findEditText(np);
    // et.setFocusable(false);
    // et.setGravity(Gravity.CENTER);
    // et.setTextSize(screenWidth / 25);
    //
    // }
    // }
    // }
    //
    // private EditText findEditText(NumberPicker np) {
    // if (null != np) {
    // for (int i = 0; i < np.getChildCount(); i++) {
    // View child = np.getChildAt(i);
    //
    // if (child instanceof EditText) {
    // return (EditText) child;
    // }
    // }
    // }
    //
    // return null;
    // }

    private void resizeTimePicker(FrameLayout tp) {
        List<NumberPicker> npList = findNumberPicker(tp);
        for (NumberPicker np : npList) {
            resizeNumberPickerOfTimePicker(np);
        }
    }

    private void resizeDatePicker(FrameLayout tp) {
        List<NumberPicker> npList = findNumberPicker(tp);
        for (NumberPicker np : npList) {
            resizeNumberPickerOfDatePicker(np);
        }
    }

    private List<NumberPicker> findNumberPicker(ViewGroup viewGroup) {
        List<NumberPicker> npList = new ArrayList<NumberPicker>();
        View child = null;
        if (null != viewGroup) {
            // Log.v(TAG,viewGroup.toString()+","+ viewGroup.getChildCount());
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                child = viewGroup.getChildAt(i);
                // Log.v(TAG,
                // viewGroup.toString()+":"+viewGroup.getChildCount()+"/"+i+":"+child.toString());
                child.setOnTouchListener(rmRCOTL);
                if (child instanceof NumberPicker) {
                    npList.add((NumberPicker) child);
                } else if (child instanceof LinearLayout) {
                    List<NumberPicker> result = findNumberPicker((ViewGroup) child);
                    if (result.size() > 0) {
                        return result;
                    }
                }
            }
        }
        return npList;
    }

    private void resizeNumberPickerOfTimePicker(NumberPicker np) {
        LayoutParams params = new LayoutParams(
                screenWidth / 7, screenHeight / 4);
        params.setMargins(-screenWidth / 100, 0, 0, 0);
        // System.out.println(""+screenWidth/6+","+screenHeight/4);
        np.setLayoutParams(params);
    }

    private void resizeNumberPickerOfDatePicker(NumberPicker np) {
        LayoutParams params = new LayoutParams(
                screenWidth / 7, screenHeight / 4);
        params.setMargins(screenWidth / 100, 0, 0, 0);
        // System.out.println(""+screenWidth/6+","+screenHeight/4);
        np.setLayoutParams(params);
    }

    @OnClick({R.id.add_gl_addBtn, R.id.add_clear_text})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_gl_addBtn:
                if (usualBtnSet.size() >= 15) {
                    reachedMaxUsualBtnNumDialog();
                } else {
                    addUsualDialog();
                }
                break;
            case R.id.add_clear_text:
                editContent.setText("");
                break;
        }
    }
}
