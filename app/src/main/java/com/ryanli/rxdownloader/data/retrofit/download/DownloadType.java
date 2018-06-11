package com.ryanli.rxdownloader.data.retrofit.download;

import android.os.Environment;

import com.ryanli.rxdownloader.data.retrofit.RetrofitProvider;
import com.ryanli.rxdownloader.data.retrofit.httpapis.DownloadApi;
import com.ryanli.rxdownloader.data.utils.LogUtils;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.io.File;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 * Auther: RyanLi
 * Data: 2018-06-06 16:40
 * Description: 文件下载类型 普通下载、断点下载、多线程下载、已完成下载
 */
public abstract class DownloadType {

    protected abstract Flowable<String> download();

    protected void prepareDownload() {
        LogUtils.i(this.getClass().getSimpleName() + " preparing...");
    }

    public Flowable<String> startDownload() {
        //使用flowable背压支持
        return Flowable.just(1)
                .doOnSubscribe(new Consumer<Subscription>() {
                    @Override
                    public void accept(Subscription subscription) throws Exception {
                        LogUtils.i(this.getClass().getSimpleName() + " start download!!!");
                    }
                })
                .flatMap(new Function<Integer, Publisher<String>>() {
                    @Override
                    public Publisher<String> apply(Integer integer) throws Exception {
                        return download();
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtils.e(this.getClass().getSimpleName() + " download error:" + throwable.getMessage());
                    }
                })
                .doOnCancel(new Action() {
                    @Override
                    public void run() throws Exception {
                        LogUtils.i(this.getClass().getSimpleName() + " cancel download!!!");
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        LogUtils.i(this.getClass().getSimpleName() + "finish download!!!");
                    }
                });
    }


    public static class NormalDownload extends DownloadType {

        @Override
        public Flowable<String> download() {
            return RetrofitProvider.getInstance().create(DownloadApi.class).download(null,
                    "http://www.desktx.com/d/file/wallpaper/movie/20180427/fd884aed6e4e8b7ca3f1db5105631bff.jpg")
                    .flatMap(new Function<Response<ResponseBody>, Publisher<String>>() {
                        @Override
                        public Publisher<String> apply(final Response<ResponseBody> responseBodyResponse) throws Exception {

                            return Flowable.create(new FlowableOnSubscribe<String>() {
                                @Override
                                public void subscribe(FlowableEmitter<String> emitter) throws Exception {
                                    FileHelper.saveFile(emitter, new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()+File.separator+"test.jpg"), responseBodyResponse);
                                }
                            }, BackpressureStrategy.LATEST);
                        }
                    });
        }
    }

    public static class ContinueDownload extends DownloadType {

        @Override
        protected Flowable<String> download() {
            return null;
        }
    }

    public static class MultiThreadDownload extends DownloadType {

        @Override
        protected Flowable<String> download() {
            return null;
        }
    }

    public static class FinishedDownload extends DownloadType {

        @Override
        protected Flowable<String> download() {
            return null;
        }
    }

}
