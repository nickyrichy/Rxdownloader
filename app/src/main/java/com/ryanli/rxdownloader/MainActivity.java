package com.ryanli.rxdownloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.ryanli.rxdownloader.data.retrofit.download.DownloadType;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.reactivestreams.Publisher;

import io.reactivex.BackpressureStrategy;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Logger.addLogAdapter(new AndroidLogAdapter());


        new RxPermissions(this).request(WRITE_EXTERNAL_STORAGE)
                .toFlowable(BackpressureStrategy.LATEST)
                .flatMap(new Function<Boolean, Publisher<String>>() {
                    @Override
                    public Publisher<String> apply(Boolean aBoolean) throws Exception {
                        return new DownloadType.NormalDownload().download()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread());
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Toast.makeText(MainActivity.this, s + "", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    protected void onDestroy() {
        if (disposable != null) {
            disposable.dispose();
        }
        super.onDestroy();
    }
}
