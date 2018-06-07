package com.ryanli.rxdownloader.data.retrofit.download;

import com.ysyy.rxdownloader.data.retrofit.RetrofitProvider;
import com.ysyy.rxdownloader.data.retrofit.httpapis.DownloadApi;
import com.ysyy.rxdownloader.data.utils.LogUtils;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import io.reactivex.Flowable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Auther: RyanLi
 * Data: 2018-06-06 16:40
 * Description: 文件下载类型 普通下载、断点下载、多线程下载、已完成下载
 */
public abstract class DownloadType {

    protected abstract Flowable<Response<ResponseBody>> download();

    protected void prepareDownload() {
        LogUtils.i(this.getClass().getSimpleName() + " preparing...");
    }

    public Flowable<Response<ResponseBody>> startDownload() {
        //使用flowable背压支持
        return Flowable.just(1)
                .doOnSubscribe(new Consumer<Subscription>() {
                    @Override
                    public void accept(Subscription subscription) throws Exception {
                        LogUtils.i(this.getClass().getSimpleName() + " start download!!!");
                    }
                })
                .flatMap(new Function<Integer, Publisher<Response<ResponseBody>>>() {
                    @Override
                    public Publisher<Response<ResponseBody>> apply(Integer integer) throws Exception {
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
        public Flowable<Response<ResponseBody>> download() {
            return RetrofitProvider.getInstance().create(DownloadApi.class).download(null, "http://image5.tuku.cn/pic/wallpaper/fengjing/qiutiandehu/004.jpg");
        }
    }

    public static class ContinueDownload extends DownloadType {

        @Override
        protected Flowable<Response<ResponseBody>> download() {
            return null;
        }
    }

    public static class MultiThreadDownload extends DownloadType {

        @Override
        protected Flowable<Response<ResponseBody>> download() {
            return null;
        }
    }

    public static class FinishedDownload extends DownloadType {

        @Override
        protected Flowable<Response<ResponseBody>> download() {
            return null;
        }
    }

}
