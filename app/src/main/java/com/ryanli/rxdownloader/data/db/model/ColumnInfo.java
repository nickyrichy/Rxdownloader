package com.ryanli.rxdownloader.data.db.model;

/**
 * Auther: RyanLi
 * Data: 2018-06-10 03:16
 * Description: 表列信息
 */
public class ColumnInfo {

    private String columName; //列名
    private String fieldtype; // 字段类型

    private DataType dbtype;
    private boolean isPrimaryKey = false;


    public String getColumName() {
        return columName;
    }

    public void setColumName(String columName) {
        this.columName = columName;
    }

    public String getFieldtype() {
        return fieldtype;
    }

    public void setFieldtype(String fieldtype) {
        this.fieldtype = fieldtype;
    }

    public DataType getDbtype() {
        return dbtype;
    }

    public void setDbtype(DataType dbtype) {
        this.dbtype = dbtype;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    @Override
    public String toString() {
        return "ColumnInfo [columName=" + columName + ", fieldtype=" + fieldtype
                + ", dbtype=" + dbtype + "]";
    }
}
