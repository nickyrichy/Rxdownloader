package com.ryanli.rxdownloader.data.db.model;

/**
 * Auther: RyanLi
 * Data: 2018-06-10 03:20
 * Description: 数据库 表列的数据类型
 */
public enum DataType {

    INTEGER("int"),
    LONG("int"),
    STRING("varchar"),
    BOOLEAN("varchar"),
    SHORT("varchar"),
    FLOAT("varchar"),
    DOUBLE("varchar"),
    ENUM("varchar"),
    UNKOWN("varchar");

    String sqlType;
    DataType(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getSqlType() {
        return this.sqlType;
    }
}
