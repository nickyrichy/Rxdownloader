package com.ryanli.rxdownloader.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Auther: RyanLi
 * Data: 2018-06-07 10:54
 * Description: 数据库表的操作 基类
 */
public abstract class BaseTable implements BaseColumns {

    protected abstract String getTableName();

    protected abstract Map<String, String> getParamsMap();

    protected Context mContext;

    public BaseTable() {
    }

    public BaseTable(Context context) {
        this.mContext = context;
    }

    /**
     * 插入数据库语句
     *
     * @return
     */
    public String getCreateTableSql() {
        return getCreateTable(getTableName(), getParamsMap());
    }

    /**
     * 插入表语句
     *
     * @param tableName
     * @param map
     * @return
     */
    public String getCreateTable(String tableName, Map<String, String> map) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("create table ").append(tableName).append(" (");
        Flowable.fromIterable(map.entrySet())
                .flatMap(new Function<Map.Entry<String, String>, Publisher<?>>() {
                    @Override
                    public Publisher<?> apply(Map.Entry<String, String> entry) throws Exception {
                        stringBuilder.append(entry.getKey()).append(" ").append(entry.getValue());
                        stringBuilder.append(",");
                        return Flowable.just(1);
                    }
                }).subscribe();
        String sql = stringBuilder.substring(0, stringBuilder.length() - 1) + ")";
        return sql;
    }

    /**
     * 插入一条
     */
    public void insert(ContentValues values) {
        DbOpenHelper.getInstance(mContext).getWritableDatabase().insert(getTableName(), null, values);
    }

    /**
     * 插入多条数据（没插入一条数据，数据库会默认开启一个事务）
     *
     * @param values
     */
    public void insertList(final List<ContentValues> values) {
        Flowable.fromIterable(values)
                .doOnSubscribe(new Consumer<Subscription>() {
                    @Override
                    public void accept(Subscription subscription) throws Exception {
                        //事务的开启
                        DbOpenHelper.getInstance(mContext).getWritableDatabase().beginTransaction();
                    }
                })
                .flatMap(new Function<ContentValues, Publisher<?>>() {
                    @Override
                    public Publisher<?> apply(ContentValues contentValues) throws Exception {
                        insert(contentValues);
                        return Flowable.just(1);
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        DbOpenHelper.getInstance(mContext).getWritableDatabase().endTransaction();//事务的结束
                    }
                })
                .subscribe();
    }

    /**
     * sql语句查询
     *
     * @param sql
     */
    public Cursor query(String sql) {
        return DbOpenHelper.getInstance(mContext).getReadableDatabase().rawQuery(sql, null);
    }

    /**
     * 条件查询
     *
     * @param selection
     * @param selectionArgs
     * @return
     */
    public Cursor query(String selection, String[] selectionArgs) {
        return DbOpenHelper.getInstance(mContext).getReadableDatabase().query(getTableName(), new String[]{_ID},
                selection + "=?", selectionArgs, null, null, null);
    }

    /**
     * sql语句查询所有数据
     *
     * @return
     */
    public Cursor queryAll() {
        String sql = "select * from " + getTableName();
        return DbOpenHelper.getInstance(mContext).getWritableDatabase().rawQuery(sql, null);
    }


    /**
     * 更新
     */
    public void update(ContentValues values, String where, String[] whereArgs) {
        DbOpenHelper.getInstance(mContext).getWritableDatabase().update(getTableName(), values, where, whereArgs);
    }

    /**
     * 删除数据
     */
    public void delete(String where, String[] whereArgs) {
        DbOpenHelper.getInstance(mContext).getWritableDatabase().delete(getTableName(), where, whereArgs);
    }

    /**
     * 清空表
     */
    public void clear() {
        DbOpenHelper.getInstance(mContext).getWritableDatabase().execSQL("delete from " + getTableName());
    }

    /**
     * 删除整个表
     */
    public void deleteTable() {
        DbOpenHelper.getInstance(mContext).getWritableDatabase().execSQL("drop table " + getTableName());
    }

    /**
     * 获取数据库数据总条数
     *
     * @return
     */
    public long getCount() {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        SQLiteStatement statement = DbOpenHelper.getInstance(mContext).getWritableDatabase().compileStatement(sql);
        long count = statement.simpleQueryForLong();
        return count;
    }

}
