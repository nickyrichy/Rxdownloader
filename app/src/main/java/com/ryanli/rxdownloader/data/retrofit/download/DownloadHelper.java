package com.ryanli.rxdownloader.data.retrofit.download;

import com.ryanli.rxdownloader.data.retrofit.RetrofitProvider;
import com.ryanli.rxdownloader.data.retrofit.httpapis.DownloadApi;
import com.ryanli.rxdownloader.data.utils.LogUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static com.ryanli.rxdownloader.data.retrofit.download.DownloadUtils.retry;
import static java.lang.String.format;

/**
 * Auther: RyanLi
 * Data: 2018-06-18 23:33
 * Description:
 */
public class DownloadHelper {
    private int maxRetryCount = 3;
    private int maxThreads = 3;

    private String defaultSavePath;
    private DownloadApi downloadApi;
    private TemporaryRecordTable recordTable;


    public DownloadHelper() {
        downloadApi = RetrofitProvider.getInstance().create(DownloadApi.class);
        recordTable = new TemporaryRecordTable();
        defaultSavePath = getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath();
    }

    public void setRetrofit(Retrofit retrofit) {
        downloadApi = retrofit.create(DownloadApi.class);
    }

    public void setDefaultSavePath(String defaultSavePath) {
        this.defaultSavePath = defaultSavePath;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    /**
     * dispatch download
     *
     * @param entity download entity
     * @return DownloadStatus
     */
    public Observable<DownloadStatus> downloadDispatcher(final DownloadEntity entity) {
        return Observable.just(1)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        addTempRecord(entity);
                    }
                })
                .flatMap(new Function<Integer, ObservableSource<DownloadType>>() {
                    @Override
                    public ObservableSource<DownloadType> apply(Integer integer)
                            throws Exception {
                        return getDownloadType(entity.getUrl());
                    }
                })
                .flatMap(new Function<DownloadType, ObservableSource<DownloadStatus>>() {
                    @Override
                    public ObservableSource<DownloadStatus> apply(DownloadType type)
                            throws Exception {
                        return download(type);
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        logError(throwable);
                    }
                })
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        recordTable.delete(entity.getUrl());
                    }
                });
    }

    /**
     * Add a temporary record to the record recordTable.
     *
     * @param entity download entity
     */
    private void addTempRecord(DownloadEntity entity) {
        if (recordTable.contain(entity.getUrl())) {
            throw new IllegalArgumentException(format("The url [%s] already exists.", entity.getUrl()));
        }
        recordTable.add(entity.getUrl(), new TemporaryRecord(entity));
    }

    private ObservableSource<DownloadStatus> download(DownloadType downloadType)
            throws IOException, ParseException {
        downloadType.prepareDownload();
        return downloadType.startDownload();
    }

    private void logError(Throwable throwable) {
        if (throwable instanceof CompositeException) {
            CompositeException realException = (CompositeException) throwable;
            List<Throwable> exceptions = realException.getExceptions();
            for (Throwable each : exceptions) {
                LogUtils.e(each.getMessage());
            }
        } else {
            LogUtils.e(throwable.getMessage());
        }
    }


    /**
     * get download type.
     *
     * @param url url
     * @return download type
     */
    private Observable<DownloadType> getDownloadType(final String url) {
        return Observable.just(1)
                .flatMap(new Function<Integer, ObservableSource<Object>>() {
                    @Override
                    public ObservableSource<Object> apply(Integer integer)
                            throws Exception {
                        return checkUrl(url);
                    }
                })
                .flatMap(new Function<Object, ObservableSource<Object>>() {
                    @Override
                    public ObservableSource<Object> apply(Object o) throws Exception {
                        return checkRange(url);
                    }
                })
                .doOnNext(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        recordTable.init(url, maxThreads, maxRetryCount, defaultSavePath,
                                downloadApi);
                    }
                })
                .flatMap(new Function<Object, ObservableSource<DownloadType>>() {
                    @Override
                    public ObservableSource<DownloadType> apply(Object o) throws Exception {
                        return recordTable.fileExists(url) ? existsType(url) : nonExistsType(url);
                    }
                });
    }

    /**
     * Gets the download type of file non-existence.
     *
     * @param url file url
     * @return Download Type
     */
    private Observable<DownloadType> nonExistsType(final String url) {
        return Observable.just(1)
                .flatMap(new Function<Integer, ObservableSource<DownloadType>>() {
                    @Override
                    public ObservableSource<DownloadType> apply(Integer integer)
                            throws Exception {
                        return Observable.just(recordTable.generateNonExistsType(url));
                    }
                });
    }

    /**
     * Gets the download type of file existence.
     *
     * @param url file url
     * @return Download Type
     */
    private Observable<DownloadType> existsType(final String url) {
        return Observable.just(1)
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer integer) throws Exception {
                        return recordTable.readLastModify(url);
                    }
                })
                .flatMap(new Function<String, ObservableSource<Object>>() {
                    @Override
                    public ObservableSource<Object> apply(String s) throws Exception {
                        return checkFile(url, s);
                    }
                })
                .flatMap(new Function<Object, ObservableSource<DownloadType>>() {
                    @Override
                    public ObservableSource<DownloadType> apply(Object o)
                            throws Exception {
                        return Observable.just(recordTable.generateFileExistsType(url));
                    }
                });
    }

    /**
     * check url
     *
     * @param url url
     * @return empty
     */
    private ObservableSource<Object> checkUrl(final String url) {
        return downloadApi.check(url)
                .doOnNext(new Consumer<Response<Void>>() {
                    @Override
                    public void accept(Response<Void> response) throws Exception {
                        if (!response.isSuccessful()) {
                            throw new IllegalArgumentException(format("The url [%s] is illegal.", url));
                        } else {
                            recordTable.saveFileInfo(url, response);
                        }
                    }
                })
                .map(new Function<Response<Void>, Object>() {
                    @Override
                    public Object apply(Response<Void> response) throws Exception {
                        return new Object();
                    }
                })
                .compose(retry("Request", maxRetryCount));
    }

    /**
     * http checkRangeByHead request,checkRange need info.
     *
     * @param url url
     * @return empty Observable
     */
    private ObservableSource<Object> checkRange(final String url) {
        return downloadApi.checkRangeByHead("bytes=0-", url)
                .doOnNext(new Consumer<Response<Void>>() {
                    @Override
                    public void accept(Response<Void> response) throws Exception {
                        recordTable.saveRangeInfo(url, response);
                    }
                })
                .map(new Function<Response<Void>, Object>() {
                    @Override
                    public Object apply(Response<Void> response) throws Exception {
                        return new Object();
                    }
                })
                .compose(retry("Request", maxRetryCount));
    }

    /**
     * http checkRangeByHead request,checkRange need info, check whether if server file has changed.
     *
     * @param url url
     * @return empty Observable
     */
    private ObservableSource<Object> checkFile(final String url, String lastModify) {
        return downloadApi.checkFileByHead(lastModify, url)
                .doOnNext(new Consumer<Response<Void>>() {
                    @Override
                    public void accept(Response<Void> response) throws Exception {
                        recordTable.saveFileState(url, response);
                    }
                })
                .map(new Function<Response<Void>, Object>() {
                    @Override
                    public Object apply(Response<Void> response) throws Exception {
                        return new Object();
                    }
                })
                .compose(retry("Request", maxRetryCount));
    }

}
