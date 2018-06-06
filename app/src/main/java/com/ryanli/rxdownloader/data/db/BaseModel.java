package com.ryanli.rxdownloader.data.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import java.util.List;
import java.util.Map;


/**
 * Created by Lizhangfeng on 2016/8/8 0008.
 * Description: 基类model
 */
public abstract class BaseModel implements BaseColumns {

    private DBHelper dbHelper;

    public BaseModel() {
        dbHelper = DBHelper.getInstence();
    }

    /**
     * 插入数据库语句
     *
     * @return
     */
    public String getCreateTableSql() {
        return getCreateTable(getTableName(), getParamsMap());
    }

    public String getCreateTable(String tableName, Map<String, String> map) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("create table ").append(tableName).append(" (");

        for (Map.Entry<String, String> entity : map.entrySet()) {
            stringBuilder.append(entity.getKey()).append(" ").append(entity.getValue());
            stringBuilder.append(",");
        }
        String sql = stringBuilder.substring(0, stringBuilder.length() - 1);
        sql = sql + ")";
        return sql;
    }

    /**
     * 插入一条
     */
    public void insert(ContentValues values) {
        dbHelper.getWritableDatabase().insert(getTableName(), null, values);
    }

    /**
     * 插入多条数据（没插入一条数据，数据库会默认开启一个事务）
     *
     * @param values
     */
    public void insertMore(List<ContentValues> values) {
        //事务的开启
        dbHelper.getWritableDatabase().beginTransaction();
        for (int i = 0; i < values.size(); i++) {
            insert(values.get(i));
        }
        dbHelper.getWritableDatabase().endTransaction();//事务的结束

    }

    /**
     * 更新
     */
    public void update(ContentValues values, String where, String[] whereArgs) {
        dbHelper.getWritableDatabase().update(getTableName(), values, where, whereArgs);
    }

    /**
     * 删除数据
     */
    public void delete(String where, String[] whereArgs) {
        dbHelper.getWritableDatabase().delete(getTableName(), where, whereArgs);
    }

    /**
     * 查询
     *
     * @param sql
     */
    public Cursor select(String sql) {
        return dbHelper.getReadableDatabase().rawQuery(sql, null);
    }

    public Cursor selectAll() {
        String sql = "select * from " + getTableName();
        return dbHelper.getWritableDatabase().rawQuery(sql, null);
    }

    public Cursor selectAllDesc(String condition) {
        String sql = "select * from " + getTableName() + " order by " + condition + " desc";
        return dbHelper.getWritableDatabase().rawQuery(sql, null);
    }

    public Cursor selectAllAsc(String condition) {
        String sql = "select * from " + getTableName() + " order by " + condition + " asc";
        return dbHelper.getWritableDatabase().rawQuery(sql, null);
    }

    /**
     * 清空表
     */
    public void clear() {
        dbHelper.getWritableDatabase().execSQL("delete from " + getTableName());
    }

    /**
     * 删除更个表
     */
    public void deleteTable() {
        dbHelper.getWritableDatabase().execSQL("drop table " + getTableName());
    }

    /**
     * 获取数据库数据总条数
     *
     * @return
     */
    public long getCount() {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        SQLiteStatement statement = dbHelper.getWritableDatabase().compileStatement(sql);
        long count = statement.simpleQueryForLong();
        return count;
    }

    protected abstract String getTableName();

    protected abstract Map<String, String> getParamsMap();


}
