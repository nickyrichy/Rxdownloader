package com.ryanli.rxdownloader.data.retrofit;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.ryanli.rxdownloader.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.ryanli.rxdownloader.BuildConfig.BASE_URL;

/**
 * Auther: RyanLi
 * Data: 2018-06-06 15:52
 * Description: 提供线程安全的Retrofit单例，饿汉模式
 */
public class RetrofitProvider {


    public static Retrofit getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {

        private static Retrofit INSTANCE = create();

        private SingletonHolder() {

        }

        private static Retrofit create() {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.readTimeout(10, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true);

            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(interceptor);
            }

            return new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(builder.build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
    }
}
