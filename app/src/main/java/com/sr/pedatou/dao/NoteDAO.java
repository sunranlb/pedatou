package com.sr.pedatou.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sr.pedatou.util.*;

public class NoteDAO {
    private static final String TAG = "StudentDAO";
    private DBOpenHelper helper;
    private SQLiteDatabase db;

    public NoteDAO(Context context) {
        helper = new DBOpenHelper(context);
    }

    public void add(Note n) {
        db = helper.getWritableDatabase();
        try {
            db.execSQL("insert into note (content,time) values (?,?)",
                    new Object[]{n.getContent(), n.getTime()});
        } catch (SQLException e) {
            Log.v(TAG, "" + e.getMessage());
            if (e.getMessage().contains("no such table")) {
                helper.onCreate(db);
                db.execSQL("insert into note (content,time) values (?,?)",
                        new Object[]{n.getContent(), n.getTime()});
            } else {
                e.printStackTrace();

            }
        }

    }

    public void update(int id, String content, String time) {
        db = helper.getWritableDatabase();
        db.execSQL("update note set content = ?,time = ? where id = ?",
                new Object[]{content, time, id});
    }

    public Note findById(int id) {
        db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "select id,content,time from note where id = ?",
                new String[]{String.valueOf(id)});
        if (cursor.moveToNext()) {
            return new Note(cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("content")),
                    cursor.getString(cursor.getColumnIndex("time")));
        }
        return null;
    }

    public Note findByTime(String time) {
        db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "select id,content,time from note where time = ?",
                new String[]{time});
        if (cursor.moveToNext()) {
            return new Note(cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("content")),
                    cursor.getString(cursor.getColumnIndex("time")));
        }
        return null;
    }

    public ArrayList<Note> getAll() {
        ArrayList<Note> r = new ArrayList<Note>();
        db = helper.getWritableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from note order by time", new String[]{});
            while (cursor.moveToNext()) {
                r.add(new Note(cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("content")),
                        cursor.getString(cursor.getColumnIndex("time"))));
            }
        } catch (SQLException e) {
            Log.v(TAG, e.getMessage());
        }

        return r;
    }

    public ArrayList<Note> getFromDay(Calendar cal) {
        ArrayList<Note> r = new ArrayList<Note>();
        db = helper.getWritableDatabase();
        String dbTime = Tools.calendarToDb(cal);
        try {
            Cursor cursor = db.rawQuery(
                    "select * from note where time >= " + dbTime + " order by time",
                    new String[]{});
            while (cursor.moveToNext()) {
                r.add(new Note(cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("content")),
                        cursor.getString(cursor.getColumnIndex("time"))));
            }
        } catch (SQLException e) {
            Log.v(TAG, e.getMessage());
        }

        return r;
    }

    public void detele(Integer... ids) {
        if (ids.length > 0) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < ids.length; i++) {
                sb.append('?').append(',');
            }
            sb.deleteCharAt(sb.length() - 1);
            SQLiteDatabase database = helper.getWritableDatabase();
            database.execSQL("delete from note where id in (" + sb + ")",
                    (Object[]) ids);
        }
    }

    // public List<Student> getScrollData(int start, int count)
    // {
    // List<Student> students = new ArrayList<Student>();
    // db = helper.getWritableDatabase();
    // Cursor cursor = db.rawQuery("select * from t_student limit ?,?", new
    // String[]{ String.valueOf(start), String.valueOf(count) });
    // while (cursor.moveToNext())
    // {
    // students.add(new Student(cursor.getInt(cursor.getColumnIndex("sid")),
    // cursor.getString(cursor.getColumnIndex("name")),
    // cursor.getShort(cursor.getColumnIndex("age"))));
    // }
    // return students;
    // }
    // public long getCount()
    // {
    // db = helper.getWritableDatabase();
    // Cursor cursor = db.rawQuery("select count(sid) from t_student", null);
    // if (cursor.moveToNext())
    // {
    // return cursor.getLong(0);
    // }
    // return 0;
    // }
}
