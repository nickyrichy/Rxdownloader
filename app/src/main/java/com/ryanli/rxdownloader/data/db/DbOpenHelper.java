package com.ryanli.rxdownloader.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

import static com.ryanli.rxdownloader.BuildConfig.DB_NAME;
import static com.ryanli.rxdownloader.BuildConfig.DB_VERSION;

/**
 * Auther: RyanLi
 * Data: 2018-06-07 09:32
 * Description: 数据库帮助类 单例 注意：要创建的表需要添加到TABLES集合里才会被创建
 */
public class DbOpenHelper extends SQLiteOpenHelper {

    private static volatile DbOpenHelper singleton;

    public static String[] TABLES = new String[]{DownloadRecordTable.class.getName()};

    public DbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DbOpenHelper getInstance(Context context) {
        if (singleton == null) {
            synchronized (DbOpenHelper.class) {
                if (singleton == null) {
                    singleton = new DbOpenHelper(context);
                }
            }
        }
        return singleton;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        //通过反射创建表
        Flowable.fromArray(TABLES)
                .flatMap(new Function<String, Publisher<?>>() {
                    @Override
                    public Publisher<?> apply(String table) throws Exception {
                        Class<BaseTable> baseModelClass = (Class<BaseTable>) Class.forName(table);
                        BaseTable baseTable = baseModelClass.newInstance();//根据Class拿到对象
                        db.execSQL(baseTable.getCreateTableSql());
                        return Flowable.just(1);
                    }
                })
                .subscribe();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
