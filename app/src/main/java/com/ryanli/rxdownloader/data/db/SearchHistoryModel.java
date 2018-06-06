package com.ryanli.rxdownloader.data.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.bwf.framwork.base.BaseModel;
import com.bwf.framwork.bean.SearchHistoryBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lizhangfeng on 2016/8/31 0031.
 * Description: 搜索历史表
 */
public class SearchHistoryModel extends BaseModel {

    public static final String TABLE_NAME = "search_history";//表名

    private static Map<String, String> paramsMap = new HashMap<>();

    static {
        paramsMap.put(_ID, "integer primary key autoincrement");//
        paramsMap.put("content", "TEXT NOT NULL");//搜索内容
        paramsMap.put("time", "TEXT NOT NULL");//时间
    }


    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected Map<String, String> getParamsMap() {
        return paramsMap;
    }

    /**
     * 插入搜索历史
     *
     * @param searchHistoryBean
     */
    public void insertSearchHistory(SearchHistoryBean searchHistoryBean) {
        if (searchHistoryBean == null)
            return;

        ContentValues values = new ContentValues();
        values.put("content", searchHistoryBean.content);
        values.put("time", searchHistoryBean.time);

        if (isExist(searchHistoryBean.content)) {//存在内容
            update(values, "content=?", new String[]{searchHistoryBean.content});
        } else {//如果没有则插入数据

            if (getCount() == 6) {
                deleteLast();
                insert(values);
            } else {
                insert(values);
            }

        }

    }

    public boolean isExist(String content) {

        Cursor cursor = select("select * from " + TABLE_NAME + " where content = '" + content + "'");
        if (cursor != null && cursor.moveToNext())
            return true;
        return false;
    }

    /**
     * 删除最后一条
     */
    public void deleteLast() {
        String content = "";
        Cursor cursor = selectAllAsc("time");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                content = cursor.getString(cursor.getColumnIndex("content"));
            }
            cursor.close();
        }

        delete("content=?", new String[]{content});

    }

    public List<SearchHistoryBean> getAll() {
        List<SearchHistoryBean> searchHistoryBeen = null;
        Cursor cursor = selectAllDesc("time");
        if (cursor != null) {
            searchHistoryBeen = new ArrayList<>();
            while (cursor.moveToNext()) {
                SearchHistoryBean searchHistoryBean = new SearchHistoryBean();
                searchHistoryBean.content = cursor.getString(cursor.getColumnIndex("content"));
                searchHistoryBean.time = cursor.getString(cursor.getColumnIndex("time"));
                searchHistoryBean.id = cursor.getString(cursor.getColumnIndex(_ID));
                searchHistoryBeen.add(searchHistoryBean);
            }
            cursor.close();
        }
        return searchHistoryBeen;
    }

}
