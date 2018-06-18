package com.ryanli.rxdownloader.data.retrofit.download;

import android.annotation.SuppressLint;
import android.content.Context;

import com.ryanli.rxdownloader.data.utils.LogUtils;

import java.io.InterruptedIOException;
import java.net.SocketException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Auther: RyanLi
 * Data: 2018-06-19 00:02
 * Description:
 */
public class RxDownloader {

    private static final Object object = new Object();
    @SuppressLint("StaticFieldLeak")
    private volatile static RxDownloader instance;

    private int maxDownloadNumber = 5;

    private DownloadHelper downloadHelper;

    static {
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (throwable instanceof InterruptedException) {
                    LogUtils.i("Thread interrupted");
                } else if (throwable instanceof InterruptedIOException) {
                    LogUtils.i("Io interrupted");
                } else if (throwable instanceof SocketException) {
                    LogUtils.i("Socket error");
                }
            }
        });
    }

    private RxDownloader(Context context) {
        downloadHelper = new DownloadHelper();
    }

    /**
     * Return RxDownload Instance
     *
     * @param context context
     * @return RxDownload
     */
    public static RxDownloader getInstance(Context context) {
        if (instance == null) {
            synchronized (RxDownloader.class) {
                if (instance == null) {
                    instance = new RxDownloader(context);
                }
            }
        }
        return instance;
    }

    /**
     * set default save path.
     *
     * @param savePath default save path.
     * @return instance.
     */
    public RxDownloader defaultSavePath(String savePath) {
        downloadHelper.setDefaultSavePath(savePath);
        return this;
    }

    /**
     * set max thread to download file.
     *
     * @param max max threads
     * @return instance
     */
    public RxDownloader maxThread(int max) {
        downloadHelper.setMaxThreads(max);
        return this;
    }

    /**
     * set max retry count when download failed
     *
     * @param max max retry count
     * @return instance
     */
    public RxDownloader maxRetryCount(int max) {
        downloadHelper.setMaxRetryCount(max);
        return this;
    }

    /**
     * set max download number when service download
     *
     * @param max max download number
     * @return instance
     */
    public RxDownloader maxDownloadNumber(int max) {
        this.maxDownloadNumber = max;
        return this;
    }


    /**
     * Normal download.
     * <p>
     * Will save the download records in the database.
     * <p>
     * Un subscribe will pause download.
     *
     * @param url Url
     * @return Observable<DownloadStatus>
     */
    public Observable<DownloadStatus> download(String url) {
        return download(url, null);
    }

    /**
     * Normal download with assigned Name.
     *
     * @param url      url
     * @param saveName SaveName
     * @return Observable<DownloadStatus>
     */
    public Observable<DownloadStatus> download(String url, String saveName) {
        return download(url, saveName, null);
    }

    /**
     * Normal download with assigned name and path.
     *
     * @param url      url
     * @param saveName SaveName
     * @param savePath SavePath
     * @return Observable<DownloadStatus>
     */
    public Observable<DownloadStatus> download(String url, String saveName, String savePath) {
        return download(new DownloadEntity.Builder(url).setSaveName(saveName)
                .setSavePath(savePath).build());
    }

    /**
     * Normal download.
     * <p>
     * You can construct a DownloadBean to save extra data to the database.
     *
     * @param downloadEntity download bean.
     * @return Observable<DownloadStatus>
     */
    public Observable<DownloadStatus> download(DownloadEntity downloadEntity) {
        return downloadHelper.downloadDispatcher(downloadEntity);
    }

    /**
     * Normal download for Transformer.
     *
     * @param url        url
     * @param <Upstream> Upstream
     * @return Transformer
     */
    public <Upstream> ObservableTransformer<Upstream, DownloadStatus> transform(String url) {
        return transform(url, null);
    }

    /**
     * Normal download for Transformer.
     *
     * @param url        url
     * @param saveName   saveName
     * @param <Upstream> Upstream
     * @return Transformer
     */
    public <Upstream> ObservableTransformer<Upstream, DownloadStatus> transform(
            String url, String saveName) {
        return transform(url, saveName, null);
    }

    /**
     * Normal download for Transformer.
     *
     * @param url        url
     * @param saveName   saveName
     * @param <Upstream> Upstream
     * @return Transformer
     */
    public <Upstream> ObservableTransformer<Upstream, DownloadStatus> transform(
            String url, String saveName, String savePath) {

        return transform(new DownloadEntity.Builder(url)
                .setSaveName(saveName).setSavePath(savePath).build());
    }

    /**
     * Normal download version of the Transformer.
     *
     * @param downloadEntity download bean
     * @param <Upstream>     Upstream
     * @return Transformer
     */
    public <Upstream> ObservableTransformer<Upstream, DownloadStatus> transform(
            final DownloadEntity downloadEntity) {
        return new ObservableTransformer<Upstream, DownloadStatus>() {
            @Override
            public ObservableSource<DownloadStatus> apply(Observable<Upstream> upstream) {
                return upstream.flatMap(new Function<Upstream, ObservableSource<DownloadStatus>>() {
                    @Override
                    public ObservableSource<DownloadStatus> apply(Upstream upstream) throws Exception {
                        return download(downloadEntity);
                    }
                });
            }
        };
    }
}
