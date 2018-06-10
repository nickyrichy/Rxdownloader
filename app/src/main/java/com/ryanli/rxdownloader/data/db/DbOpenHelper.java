package com.ryanli.rxdownloader.data.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.CancellationSignal;

import com.ryanli.rxdownloader.data.utils.LogUtils;

import java.util.List;

import static com.ryanli.rxdownloader.BuildConfig.DB_NAME;
import static com.ryanli.rxdownloader.BuildConfig.DB_VERSION;

/**
 * Auther: RyanLi
 * Data: 2018-06-07 09:32
 * Description: 数据库帮助类
 */
public class DbOpenHelper extends SQLiteOpenHelper {


    public DbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(final SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean create(String sql) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            SQLiteStatement state = db.compileStatement(sql);
            state.execute();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }

        return true;
    }

    public long executeInsert(String sql) {
        SQLiteDatabase db = getWritableDatabase();
        long count = -1;
        try {
            db.beginTransaction();
            SQLiteStatement stat = db.compileStatement(sql);
            count = stat.executeInsert();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
        return count;
    }

    protected long queryCount(String sql) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        long count = cursor.getLong(0);
        cursor.close();
        db.close();
        return count;
    }

    public interface OnQueryResultListener<T> {
        List<T> onResult(Class<T> clazz, Cursor cursor);
    }

    protected <T> List<T> query(Class<T> clazz, String sql, String[] selectionArgs, OnQueryResultListener<T> callback) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        List<T> list = null;
        try {
            list = callback.onResult(clazz, cursor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            db.close();
        }
        return list;
    }

    protected <T> List<T> query(Class<T> clazz, String tableName, boolean distinct, String[] columns, String selection,
                                String[] selectionArgs, String groupBy, String having, String orderBy, String limit,
                                CancellationSignal cancellationSignal, OnQueryResultListener<T> callback) {
        SQLiteDatabase db = getReadableDatabase();
        List<T> list = null;
        Cursor cursor;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            cursor = db.query(distinct, tableName, columns, selection, selectionArgs, groupBy, having, orderBy, limit,
                    cancellationSignal);
        } else {
            cursor = db.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        }
        try {
            list = callback.onResult(clazz, cursor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            db.close();
        }
        return list;
    }

    public int executeUpdateDelete(String sql) {
        SQLiteDatabase db = getReadableDatabase();
        int count = -1;
        try {
            db.beginTransaction();
            SQLiteStatement stat = db.compileStatement(sql);
            count = stat.executeUpdateDelete();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
        return count;
    }

}
