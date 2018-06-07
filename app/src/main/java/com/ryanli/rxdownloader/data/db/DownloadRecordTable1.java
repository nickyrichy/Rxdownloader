package com.ryanli.rxdownloader.data.db;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Auther: RyanLi
 * Data: 2018-06-07 12:19
 * Description: 下载记录数据库表
 */
public class DownloadRecordTable extends BaseTable {

    public static final String TABLE_NAME = "download_record";//表名

    private static Map<String, String> paramsMap = new HashMap<>();

    static {
        DbOpenHelper.TABLES.add(DownloadRecordTable.class.getName());
        paramsMap.put("save_name", "text");
        paramsMap.put("savePath", "text");
        paramsMap.put("date", "INTEGER NOT NULL");
        paramsMap.put(_ID, "integer primary key autoincrement");
    }

    public DownloadRecordTable() {

    }

    public DownloadRecordTable(Context context) {
        super(context);
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected Map<String, String> getParamsMap() {
        return paramsMap;
    }

    public void test() {
        DbOpenHelper.getInstance(mContext).getReadableDatabase();
    }
}
