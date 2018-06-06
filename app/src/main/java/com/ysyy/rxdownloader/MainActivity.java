package com.ysyy.rxdownloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.ysyy.rxdownloader.data.retrofit.download.DownloadType;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Logger.addLogAdapter(new AndroidLogAdapter());
//        String lengthCorrectUrl = "http://image5.tuku.cn/pic/wallpaper/fengjing/qiutiandehu/004.jpg";
//        disposable = RetrofitProvider.getInstance().create(DownloadApi.class).download(null, lengthCorrectUrl)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<Response<ResponseBody>>() {
//                    @Override
//                    public void accept(Response<ResponseBody> responseBodyResponse) throws Exception {
//                        Toast.makeText(MainActivity.this, responseBodyResponse.body().contentLength() + "", Toast.LENGTH_SHORT).show();
//                    }
//                });

        DownloadType.NormalDownload normalDownload = new DownloadType.NormalDownload();
        normalDownload.startDownload()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Response<ResponseBody>>() {
                    @Override
                    public void accept(Response<ResponseBody> responseBodyResponse) throws Exception {
                        Toast.makeText(MainActivity.this, responseBodyResponse.body().contentLength() + "", Toast.LENGTH_SHORT).show();
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
