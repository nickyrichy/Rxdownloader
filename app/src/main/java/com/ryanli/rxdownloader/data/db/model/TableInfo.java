package com.ryanli.rxdownloader.data.db.model;

import java.util.HashMap;

/**
 * Auther: RyanLi
 * Data: 2018-06-10 02:58
 * Description: 数据库 表信息类
 */
public class TableInfo {

    private String tableName;
    private String primaryKey;
    private HashMap<String, ColumnInfo> colunmMap;//数据库的列名与列信息的映射

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public HashMap<String, ColumnInfo> getColunmMap() {
        return colunmMap;
    }

    public void setColunmMap(HashMap<String, ColumnInfo> colunmMap) {
        this.colunmMap = colunmMap;
    }

    public ColumnInfo getColumnByColunmName(String colunmName) {
        if (colunmMap == null) {
            return null;
        }
        return colunmMap.get(colunmName);
    }

    @Override
    public String toString() {
        return "TableInfo [tableName=" + tableName + ", primaryKey=" + primaryKey + ", colunmMap=" + colunmMap + "]";
    }
}
