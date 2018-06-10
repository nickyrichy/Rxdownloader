package com.ryanli.rxdownloader.data.db.parse;

import com.ryanli.rxdownloader.data.db.annotation.Column;
import com.ryanli.rxdownloader.data.db.annotation.PrimaryKey;
import com.ryanli.rxdownloader.data.db.annotation.Table;
import com.ryanli.rxdownloader.data.db.model.ColumnInfo;
import com.ryanli.rxdownloader.data.db.model.DataType;
import com.ryanli.rxdownloader.data.db.model.TableInfo;
import com.ryanli.rxdownloader.data.utils.LogUtils;

import org.reactivestreams.Publisher;

import java.lang.reflect.Field;
import java.util.HashMap;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

/**
 * Auther: RyanLi
 * Data: 2018-06-10 02:51
 * Description: 注解解析得到表信息
 */
public class AnnoParse {

    public static TableInfo initTableInfo(final Class<?> clazz) {

        final Table table = clazz.getAnnotation(Table.class);

        if (table == null) {
            LogUtils.e("table is null, please check your" + clazz.getName());
            return null;
        }

        final TableInfo tableInfo = new TableInfo();

        tableInfo.setTableName(clazz.getSimpleName());

        final HashMap<String, ColumnInfo> columnMaps = new HashMap<>();

        Flowable.fromArray(clazz.getDeclaredFields())
                .flatMap(new Function<Field, Publisher<?>>() {
                    @Override
                    public Publisher<?> apply(Field field) throws Exception {
                        field.setAccessible(true);
                        Column column = field.getAnnotation(Column.class);
                        if (column != null) {

                            // 列信息
                            ColumnInfo columnInfo = new ColumnInfo();
                            columnInfo.setFieldtype(field.getType().getName());

                            columnInfo.setColumName(field.getName());

                            //数据库 对应的列类型
                            columnInfo.setDbtype(getType(field.getType(), columnInfo.getFieldtype()));

                            columnMaps.put(columnInfo.getColumName(), columnInfo);

                            PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
                            if (primaryKey != null) {
                                tableInfo.setPrimaryKey(columnInfo.getColumName());
                                columnInfo.setPrimaryKey(true);
                            }
                        }

                        return Flowable.just(1);
                    }
                })
                .subscribe();

        tableInfo.setColunmMap(columnMaps);

        return tableInfo;

    }

    /**
     * 根据类获取表名
     *
     * @param clazz
     * @return
     */
    public static String getTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            return null;
        }
        return clazz.getName();
    }


    /**
     * 通过field的type类型名字，得到存到数据库里对应的数据类型
     *
     * @param typeName
     * @return
     */
    private static DataType getType(Class<?> clazz, String typeName) {
        if (clazz.isEnum()) {
            return DataType.ENUM;
        } else if (long.class.getName().equals(typeName) || Long.class.getName().equals(typeName)) {
            return DataType.LONG;
        } else if (Integer.class.getName().equals(typeName) || int.class.getName().equals(typeName)) {
            return DataType.INTEGER;
        } else if (String.class.getName() == typeName) {
            return DataType.STRING;
        } else if (float.class.getName().equals(typeName) || Float.class.getName().equals(typeName)) {
            return DataType.FLOAT;
        } else if (boolean.class.getName().equals(typeName) || Boolean.class.getName().equals(typeName)) {
            return DataType.BOOLEAN;
        } else if (short.class.getName().equals(typeName) || Short.class.getName().equals(typeName)) {
            return DataType.SHORT;
        } else {
            return DataType.UNKOWN;
        }
    }
}
