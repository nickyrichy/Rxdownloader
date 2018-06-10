package com.ryanli.rxdownloader.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.CancellationSignal;

import com.ryanli.rxdownloader.data.db.model.ColumnInfo;
import com.ryanli.rxdownloader.data.db.model.DataType;
import com.ryanli.rxdownloader.data.db.model.OrderBy;
import com.ryanli.rxdownloader.data.db.model.TableInfo;
import com.ryanli.rxdownloader.data.db.parse.AnnoParse;
import com.ryanli.rxdownloader.data.utils.LogUtils;

import org.reactivestreams.Publisher;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Auther: RyanLi
 * Data: 2018-06-10 12:09
 * Description: 数据库管理 单例
 */
public class DBManager {

    private Map<String, TableInfo> mTablesInfo;

    private SqlCreator mSqlCreator;

    private DbOpenHelper mDbOpenHelper;

    private static DBManager instance;

    private DBManager(Context context) {
        this.mSqlCreator = new SqlCreator();
        this.mDbOpenHelper = new DbOpenHelper(context);
    }

    public static DBManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DBManager.class) {
                if (instance == null) {
                    instance = new DBManager(context);
                }
            }
        }
        return instance;

    }

    /**
     * 插入单条数据
     *
     * @param obj
     * @param <T>
     * @return
     */
    public <T> boolean insert(T obj) {
        List<T> list = new ArrayList<>();
        list.add(obj);
        return insertList(list);
    }

    /**
     * 插入多条数据
     *
     * @param objs
     * @param <T>
     * @return
     */
    public <T> boolean insertList(List<T> objs) {
        if (objs != null && objs.size() != 0) {
            //获取表信息
            TableInfo tableInfo = createAndGetTableInfo(objs.get(0).getClass());
            if (tableInfo == null) {
                return false;
            }

            List<ContentValues> valuesList = getContentValueList(objs, tableInfo);
            return mDbOpenHelper.executeInsert(mSqlCreator.getInsertSql(tableInfo, valuesList)) != 0;
        }
        return false;
    }

    /**
     * 查询表中数据条数
     *
     * @param clazz
     * @return
     */
    public long queryDataCount(Class<?> clazz) {
        TableInfo tableInfo = createAndGetTableInfo(clazz);
        if (tableInfo != null) {
            String sql = mSqlCreator.queryDataCount(tableInfo);
            return mDbOpenHelper.queryCount(sql);
        }
        return 0;
    }

    public <T> boolean isExist(T obj) {

        Class<T> clazz = (Class<T>) obj.getClass();
        final TableInfo tableInfo = createAndGetTableInfo(obj.getClass());

        ContentValues contentValues = getContentValue(obj, tableInfo);

        String sql = mSqlCreator.findObjectById(tableInfo, contentValues);

        List<T> find = mDbOpenHelper.query(clazz, sql, null, new DbOpenHelper.OnQueryResultListener<T>() {

            @Override
            public List<T> onResult(Class<T> clazz, Cursor cursor) {
                try {
                    return packageData(clazz, tableInfo, cursor);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        return find != null && find.size() > 0;
    }


    public <T> List<T> query(Class<T> clazz, OrderBy orderBy, int limitStart, int limitSize) {
        final TableInfo tableInfo = createAndGetTableInfo(clazz);
        String sql = mSqlCreator.getQuerySql(tableInfo, orderBy, limitStart, limitSize);

        return mDbOpenHelper.query(clazz, sql, null, new DbOpenHelper.OnQueryResultListener<T>() {

            @Override
            public List<T> onResult(Class<T> clazz, Cursor cursor) {
                try {
                    return packageData(clazz, tableInfo, cursor);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    public <T> List<T> query(Class<T> clazz, boolean distinct, String[] columns, String selection,
                             String[] selectionArgs, String groupBy, String having, String orderBy, String limit,
                             CancellationSignal cancellationSignal) {
        final TableInfo tableInfo = createAndGetTableInfo(clazz);
        return mDbOpenHelper.query(clazz, tableInfo.getTableName(), distinct, columns, selection, selectionArgs, groupBy, having, orderBy,
                limit, cancellationSignal, new DbOpenHelper.OnQueryResultListener<T>() {
                    public List<T> onResult(Class<T> clazz, Cursor cursor) {
                        try {
                            return packageData(clazz, tableInfo, cursor);
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
    }

    public <T> boolean update(T t) {
        List<T> list = new ArrayList<>();
        list.add(t);
        return update(list);
    }


    public <T> boolean update(List<T> objs) {
        TableInfo tableInfo = createAndGetTableInfo(objs.get(0).getClass());
        if (tableInfo == null) {
            return false;
        }

        List<ContentValues> valuesList = getContentValueList(objs, tableInfo);

        return mDbOpenHelper.executeUpdateDelete(mSqlCreator.getUpdateSql(tableInfo, valuesList)) > 0;
    }

    public <T> boolean delete(T t) {
        List<T> list = new ArrayList<>();
        list.add(t);
        return delete(list);
    }

    public <T> boolean delete(List<T> objs) {
        TableInfo tableInfo = createAndGetTableInfo(objs.get(0).getClass());
        if (tableInfo == null) {
            return false;
        }

        List<ContentValues> valuesList = getContentValueList(objs, tableInfo);
        return mDbOpenHelper.executeUpdateDelete(mSqlCreator.getDeleteSql(tableInfo, valuesList)) > 0;
    }


    private <T> List<ContentValues> getContentValueList(List<T> objs, final TableInfo tableInfo) {

        final List<ContentValues> contentValuesList = new ArrayList<>();
        Flowable.fromIterable(objs)
                .flatMap(new Function<T, Publisher<ContentValues>>() {
                    @Override
                    public Publisher<ContentValues> apply(T t) throws Exception {
                        return Flowable.just(getContentValue(t, tableInfo));
                    }
                })
                .flatMap(new Function<ContentValues, Publisher<?>>() {
                    @Override
                    public Publisher<?> apply(ContentValues contentValues) throws Exception {
                        contentValuesList.add(contentValues);
                        return Flowable.just(1);
                    }
                }).subscribe();
        return contentValuesList;

    }


    /**
     * 通过属性得到列的值
     *
     * @param obj
     * @param tableInfo
     * @param <T>
     * @return
     */
    private <T> ContentValues getContentValue(final T obj, final TableInfo tableInfo) {

        final ContentValues contentValues = new ContentValues();

        Flowable.fromArray(obj.getClass().getDeclaredFields())
                .flatMap(new Function<Field, Publisher<?>>() {
                    @Override
                    public Publisher<?> apply(Field field) throws IllegalAccessException {

                        field.setAccessible(true);
                        ColumnInfo column = tableInfo.getColumnByColunmName(field.getName());
                        if (column == null) {
                            return Flowable.just(1);
                        }

                        Object object = field.get(obj);
                        if (object == null) {
                            contentValues.put(column.getColumName(), "");
                            return Flowable.just(1);
                        }
                        switch (column.getDbtype()) {
                            case INTEGER:
                                contentValues.put(column.getColumName(), (Integer) object);
                                break;

                            case STRING:
                                contentValues.put(column.getColumName(), (String) object);
                                break;
                            case LONG:
                                contentValues.put(column.getColumName(), (Long) object);
                                break;
                            case DOUBLE:
                            case FLOAT:
                            case SHORT:
                            case ENUM:
                            case BOOLEAN:
                                contentValues.put(column.getColumName(), object.toString());
                                break;
                            default:
                                break;
                        }
                        return Flowable.just(1);
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtils.e(throwable.getMessage());
                    }
                })
                .subscribe();

        return contentValues;
    }

    /**
     * 根据注解解析得到表信息，并在数据库里创建表（如果不存在）
     *
     * @param clazz
     * @return
     */
    private TableInfo createAndGetTableInfo(Class<?> clazz) {
        if (mTablesInfo == null) {
            mTablesInfo = new HashMap<>();
        }

        String tableName = AnnoParse.getTableName(clazz);

        String key = clazz.getName() + tableName;
        if (mTablesInfo.containsKey(key)) {
            return mTablesInfo.get(key);
        }
        TableInfo tableInfo = AnnoParse.initTableInfo(clazz);

        if (tableInfo == null) {
            return null;
        }

        mTablesInfo.put(key, tableInfo);


        if (!mDbOpenHelper.create(mSqlCreator.getCreateTableSql(tableInfo))) {
            return null;
        }

        return tableInfo;
    }

    public <T> List<T> packageData(Class<T> clazz, TableInfo tableInfo, Cursor cursor)
            throws InstantiationException, IllegalAccessException {
        ArrayList<T> list = null;
        if (cursor != null && cursor.getCount() > 0) {
            if (list == null) {
                list = new ArrayList<>();
            }
            while (cursor.moveToNext()) {
                T obj = clazz.newInstance();
                Set<String> keySet = tableInfo.getColunmMap().keySet();
                for (String key : keySet) {
                    fillValue(obj, tableInfo.getColunmMap().get(key), cursor);
                }
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * 把字段数据填充到对象中
     *
     * @param obj
     * @param columnInfo
     * @param cursor
     */
    private void fillValue(Object obj, ColumnInfo columnInfo, Cursor cursor) {
        try {
            int columnIndex = cursor.getColumnIndex(columnInfo.getColumName());
            int type = cursor.getType(columnIndex);
            switch (type) {
                case Cursor.FIELD_TYPE_INTEGER:
                    if (columnInfo.getDbtype() == DataType.STRING) {
                        setFieldValue(obj, columnInfo.getColumName(), String.valueOf(cursor.getInt(columnIndex)));
                        break;
                    }
                    setFieldValue(obj, columnInfo.getColumName(), cursor.getInt(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    setFieldValue(obj, columnInfo.getColumName(), cursor.getFloat(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    String tmpValue = cursor.getString(columnIndex);
                    if (tmpValue == null) {
                        setFieldValue(obj, columnInfo.getColumName(), null);
                        break;
                    }
                    switch (columnInfo.getDbtype()) {
                        case ENUM:
                            Class<Enum> clazz = (Class<Enum>) Class.forName(columnInfo.getFieldtype());
                            Field[] fields = clazz.getDeclaredFields();
                            if (fields == null) {
                                setFieldValue(obj, columnInfo.getColumName(), null);
                                break;
                            }
                            for (Field field : fields) {
                                if (field.getName().equals(tmpValue)) {
                                    Enum value = Enum.valueOf(clazz, tmpValue);
                                    setFieldValue(obj, columnInfo.getColumName(), value);
                                    return;
                                }
                            }
                            setFieldValue(obj, columnInfo.getColumName(), null);
                            break;
                        case BOOLEAN:
                            if ("true".equals(tmpValue)) {
                                setFieldValue(obj, columnInfo.getColumName(), true);
                            } else {
                                setFieldValue(obj, columnInfo.getColumName(), false);
                            }
                            break;
                        case LONG:
                            setFieldValue(obj, columnInfo.getColumName(), Long.valueOf(tmpValue));
                            break;
                        case DOUBLE:
                            setFieldValue(obj, columnInfo.getColumName(), Double.valueOf(tmpValue));
                            break;
                        case FLOAT:
                            setFieldValue(obj, columnInfo.getColumName(), Float.valueOf(tmpValue));
                            break;
                        default:
                            setFieldValue(obj, columnInfo.getColumName(), tmpValue);
                            break;
                    }
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    setFieldValue(obj, columnInfo.getColumName(), cursor.getBlob(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_NULL:

                    break;
                default:
                    break;
            }
        } catch (Exception e) {

        }
    }

    private void setFieldValue(Object obj, String propertyName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(propertyName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
