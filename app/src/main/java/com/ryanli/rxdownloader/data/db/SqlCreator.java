package com.ryanli.rxdownloader.data.db;

import android.content.ContentValues;

import com.ryanli.rxdownloader.data.db.model.ColumnInfo;
import com.ryanli.rxdownloader.data.db.model.OrderBy;
import com.ryanli.rxdownloader.data.db.model.TableInfo;
import com.ryanli.rxdownloader.data.utils.LogUtils;

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
 * Description: sql语句生成类
 */
public class SqlCreator {

    /**
     * 获取建表sql语句
     *
     * @param tableInfo 表信息
     * @return
     */
    public String getCreateTableSql(final TableInfo tableInfo) {

        final StringBuilder sql = new StringBuilder();

        Flowable.just(tableInfo)
                .doOnSubscribe(new Consumer<Subscription>() {
                    @Override
                    public void accept(Subscription subscription) throws Exception {
                        sql.append("CREATE TABLE IF NOT EXISTS ");
                        sql.append(tableInfo.getTableName());
                        sql.append(" (");
                    }
                })
                .flatMap(new Function<TableInfo, Publisher<Map.Entry<String, ColumnInfo>>>() {
                    @Override
                    public Publisher<Map.Entry<String, ColumnInfo>> apply(TableInfo tableInfo) throws Exception {
                        if (tableInfo == null || tableInfo.getColunmMap() == null || tableInfo.getColunmMap().keySet().size() == 0) {
                            throw new Exception();
                        }
                        return Flowable.fromIterable(tableInfo.getColunmMap().entrySet());
                    }
                }).flatMap(new Function<Map.Entry<String, ColumnInfo>, Publisher<?>>() {
            @Override
            public Publisher<?> apply(Map.Entry<String, ColumnInfo> entrySet) throws Exception {
                ColumnInfo columnInfo = entrySet.getValue();
                sql.append(" ");
                sql.append(columnInfo.getColumName());
                sql.append(" ");
                sql.append(columnInfo.getDbtype().getSqlType());

                if (columnInfo.isPrimaryKey()) {
                    sql.append(" PRIMARY KEY");
                }
                sql.append(",");
                return Flowable.just(1);
            }
        })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        sql.deleteCharAt(sql.length() - 1);
                        sql.append(");");
                        LogUtils.i("Create tables sql -> " + sql.toString());
                    }
                })
                .subscribe();

        return sql.toString();

    }

    /**
     * 获取插入数据aql语句
     *
     * @param tableInfo
     * @param values
     * @return
     */
    public String getInsertSql(final TableInfo tableInfo, List<ContentValues> values) {

        final StringBuilder sql = new StringBuilder();
        Flowable.merge(Flowable.fromIterable(values.get(0).keySet())
                        .doOnSubscribe(new Consumer<Subscription>() {
                            @Override
                            public void accept(Subscription subscription) throws Exception {
                                sql.append("INSERT INTO ");
                                sql.append(tableInfo.getTableName());
                                sql.append("(");
                            }
                        })
                        .map(new Function<String, Object>() {
                            @Override
                            public Object apply(String key) throws Exception {
                                sql.append(key);
                                sql.append(",");
                                return sql;
                            }
                        })
                        .doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                sql.deleteCharAt(sql.length() - 1);
                                sql.append(")");
                                sql.append(" VALUES");
                            }
                        })
                , Flowable.fromIterable(values)
                        .flatMap(new Function<ContentValues, Publisher<Object>>() {
                            @Override
                            public Publisher<Object> apply(final ContentValues contentValues) throws Exception {
                                sql.append("(");
                                return Flowable.fromIterable(contentValues.keySet())
                                        .flatMap(new Function<String, Publisher<Object>>() {
                                            @Override
                                            public Publisher<Object> apply(String key) throws Exception {
                                                return Flowable.just(contentValues.get(key));
                                            }
                                        })
                                        .doFinally(new Action() {
                                            @Override
                                            public void run() throws Exception {
                                                sql.deleteCharAt(sql.length() - 1);
                                                sql.append(" ),");
                                            }
                                        })
                                        ;
                            }
                        })
                        .doOnNext(new Consumer<Object>() {
                            @Override
                            public void accept(Object value) throws Exception {
                                sql.append("'");
                                sql.append(value);
                                sql.append("'");
                                sql.append(",");
                            }
                        }))
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        sql.deleteCharAt(sql.length() - 1);
                        sql.append(";");
                        LogUtils.i("Insert sql -> " + sql.toString());
                    }
                })
                .subscribe();
        return sql.toString();
    }

    /**
     * 获取查询表中数据总条数sql语句
     *
     * @param tableInfo
     * @return
     */
    public String queryDataCount(TableInfo tableInfo) {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(*) from ");
        String tableName = tableInfo.getTableName();
        sql.append(tableName);
        LogUtils.i("Query count sql -> " + sql.toString());
        return sql.toString();
    }

    /**
     * 主键查询
     *
     * @param tableInfo
     * @param contentValues
     * @return
     */
    public String findObjectById(TableInfo tableInfo, ContentValues contentValues) {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from ");
        sql.append(tableInfo.getTableName());
        sql.append(" where ");
        sql.append(tableInfo.getPrimaryKey());
        sql.append(" = ");
        sql.append(contentValues.get(tableInfo.getPrimaryKey()));
        sql.append(";");
        LogUtils.i("Find obj sql -> " + sql.toString());
        return sql.toString();
    }

    public String getQuerySql(TableInfo tableInfo, OrderBy orderBy, int limitStart, int limitSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from ");
        String tableName = tableInfo.getTableName();
        sql.append(tableName);
        if (orderBy != null) {
            sql.append(" order by ");
            String keyName = tableInfo.getPrimaryKey();
            sql.append(keyName);
            if (orderBy == OrderBy.ASC) {
                sql.append(" asc");
            } else {
                sql.append(" desc");
            }
        }

        if (limitStart >= 0 && limitSize >= 0) {
            sql.append(" limit ");
            sql.append(limitStart);
            sql.append(",");
            sql.append(limitSize);
        }
        sql.append(";");
        LogUtils.i("find sql==>>" + sql.toString());
        return sql.toString();
    }


    public String getUpdateSql(final TableInfo tableInfo, List<ContentValues> values) {
//		replace into test_tbl (id,dr) values (1,'2'),(2,'3'),...(x,'y');

        final StringBuilder sql = new StringBuilder();
        Flowable.merge(Flowable.fromIterable(values.get(0).keySet())
                        .doOnSubscribe(new Consumer<Subscription>() {
                            @Override
                            public void accept(Subscription subscription) throws Exception {
                                sql.append("REPLACE INTO ");
                                sql.append(tableInfo.getTableName());
                                sql.append("(");
                            }
                        })
                        .map(new Function<String, Object>() {
                            @Override
                            public Object apply(String key) throws Exception {
                                sql.append(key);
                                sql.append(",");
                                return sql;
                            }
                        })
                        .doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                sql.deleteCharAt(sql.length() - 1);
                                sql.append(")");
                                sql.append(" VALUES");
                            }
                        })
                , Flowable.fromIterable(values)
                        .flatMap(new Function<ContentValues, Publisher<Object>>() {
                            @Override
                            public Publisher<Object> apply(final ContentValues contentValues) throws Exception {
                                sql.append("(");
                                return Flowable.fromIterable(contentValues.keySet())
                                        .flatMap(new Function<String, Publisher<Object>>() {
                                            @Override
                                            public Publisher<Object> apply(String key) throws Exception {
                                                return Flowable.just(contentValues.get(key));
                                            }
                                        })
                                        .doFinally(new Action() {
                                            @Override
                                            public void run() throws Exception {
                                                sql.deleteCharAt(sql.length() - 1);
                                                sql.append(" ),");
                                            }
                                        })
                                        ;
                            }
                        })
                        .doOnNext(new Consumer<Object>() {
                            @Override
                            public void accept(Object value) throws Exception {
                                sql.append("'");
                                sql.append(value);
                                sql.append("'");
                                sql.append(",");
                            }
                        }))
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        sql.deleteCharAt(sql.length() - 1);
                        sql.append(";");
                        LogUtils.i("Insert sql -> " + sql.toString());
                    }
                })
                .subscribe();
        return sql.toString();
    }

    public String getDeleteSql(final TableInfo tableInfo, List<ContentValues> values) {
        final StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ");
        sql.append(tableInfo.getTableName());
        sql.append(" WHERE ");
        sql.append(tableInfo.getPrimaryKey());
        sql.append(" IN (");
        Flowable.fromIterable(values)
                .map(new Function<ContentValues, Object>() {
                    @Override
                    public Object apply(ContentValues contentValues) throws Exception {
                        sql.append(contentValues.get(tableInfo.getPrimaryKey()));
                        sql.append(",");
                        return "";
                    }
                }).subscribe();

        sql.deleteCharAt(sql.length() - 1);
        sql.append(");");

        LogUtils.i("Delete sql -> " + sql.toString());

        return sql.toString();
    }

}
