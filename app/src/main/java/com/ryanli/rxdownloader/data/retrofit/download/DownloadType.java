package com.ryanli.rxdownloader.data.retrofit.download;

import com.ryanli.rxdownloader.data.utils.LogUtils;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static java.lang.String.format;

/**
 * Auther: RyanLi
 * Data: 2018-06-06 16:40
 * Description: 文件下载类型 普通下载、断点下载、多线程下载、已完成下载
 */
public abstract class DownloadType {

    protected TemporaryRecord record;

    private DownloadType(TemporaryRecord record) {
        this.record = record;
    }

    protected void prepareDownload() throws IOException, ParseException {
        LogUtils.i(this.getClass().getSimpleName() + " preparing...");
    }

    protected abstract Publisher<DownloadStatus> download();

    public io.reactivex.Observable<DownloadStatus> startDownload() {
        //使用flowable背压支持
        return Flowable.just(1)
                .doOnSubscribe(new Consumer<Subscription>() {
                    @Override
                    public void accept(Subscription subscription) throws Exception {
//                        record.start();
                        LogUtils.i(this.getClass().getSimpleName() + " start download!!!");
                    }
                })
                .flatMap(new Function<Integer, Publisher<DownloadStatus>>() {
                    @Override
                    public Publisher<DownloadStatus> apply(Integer integer) throws Exception {
                        return download();
                    }
                })
//                .doOnNext(new Consumer<DownloadStatus>() {
//                    @Override
//                    public void accept(DownloadStatus status) throws Exception {
//                        record.update(status);
//                    }
//                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
//                        record.error();
                        LogUtils.e(this.getClass().getSimpleName() + " download error:" + throwable.getMessage());
                    }
                })
                .doOnCancel(new Action() {
                    @Override
                    public void run() throws Exception {
//                        record.cancel();
                        LogUtils.i(this.getClass().getSimpleName() + " cancel download!!!");
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
//                        record.complete();
                        LogUtils.i(this.getClass().getSimpleName() + "finish download!!!");
                    }
                }).toObservable();
    }


    public static class NormalDownload extends DownloadType {

        public NormalDownload(TemporaryRecord record) {
            super(record);
        }

        @Override
        public void prepareDownload() throws IOException, ParseException {
            super.prepareDownload();
            record.prepareNormalDownload();
        }

        @Override
        protected Publisher<DownloadStatus> download() {
            return record.download()
                    .flatMap(new Function<Response<ResponseBody>, Publisher<DownloadStatus>>() {
                        @Override
                        public Publisher<DownloadStatus> apply(Response<ResponseBody> response) throws Exception {
                            return save(response);
                        }
                    })
                    .compose(DownloadUtils.<DownloadStatus>retry2(this.getClass().getSimpleName(), record.getMaxRetryCount()));
        }

        private Publisher<DownloadStatus> save(final Response<ResponseBody> response) {
            return Flowable.create(new FlowableOnSubscribe<DownloadStatus>() {
                @Override
                public void subscribe(FlowableEmitter<DownloadStatus> e) throws Exception {
                    record.save(e, response);
                }
            }, BackpressureStrategy.LATEST);
        }
    }

    public static class ContinueDownload extends DownloadType {

        public ContinueDownload(TemporaryRecord record) {
            super(record);
        }

        @Override
        protected Flowable<DownloadStatus> download() {
            List<Publisher<DownloadStatus>> tasks = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                tasks.add(rangeDownload(i));
            }
            return Flowable.mergeDelayError(tasks);
        }

        /**
         * 分段下载任务
         *
         * @param index 下载编号
         * @return Observable
         */
        private Publisher<DownloadStatus> rangeDownload(final int index) {
            return record.rangeDownload(index)
                    .subscribeOn(Schedulers.io())  //Important!
                    .flatMap(new Function<Response<ResponseBody>, Publisher<DownloadStatus>>() {
                        @Override
                        public Publisher<DownloadStatus> apply(Response<ResponseBody> response) throws Exception {
                            return save(index, response.body());
                        }
                    })
                    .compose(DownloadUtils.<DownloadStatus>retry2(format(Locale.getDefault(), "Range %d", index), record.getMaxRetryCount()));
        }

        /**
         * 保存断点下载的文件,以及下载进度
         *
         * @param index    下载编号
         * @param response 响应值
         * @return Flowable
         */
        private Publisher<DownloadStatus> save(final int index, final ResponseBody response) {

            return Flowable.create(new FlowableOnSubscribe<DownloadStatus>() {
                @Override
                public void subscribe(FlowableEmitter<DownloadStatus> emitter) throws Exception {
                    record.save(emitter, index, response);
                }
            }, BackpressureStrategy.LATEST);
        }
    }

    public static class MultiThreadDownload extends ContinueDownload {

        public MultiThreadDownload(TemporaryRecord record) {
            super(record);
        }

        @Override
        public void prepareDownload() throws IOException, ParseException {
            super.prepareDownload();
            record.prepareRangeDownload();
        }

    }

    public static class FinishedDownload extends DownloadType {

        public FinishedDownload(TemporaryRecord record) {
            super(record);
        }

        @Override
        protected Flowable<DownloadStatus> download() {
            return Flowable.just(new DownloadStatus(record.getContentLength(), record.getContentLength()));
        }
    }

}
