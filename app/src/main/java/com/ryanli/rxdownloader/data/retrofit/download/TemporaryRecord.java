package com.ryanli.rxdownloader.data.retrofit.download;

import com.ryanli.rxdownloader.data.retrofit.httpapis.DownloadApi;

import org.reactivestreams.Publisher;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static android.text.TextUtils.concat;
import static com.ryanli.rxdownloader.data.retrofit.download.DownloadUtils.empty;
import static com.ryanli.rxdownloader.data.retrofit.download.DownloadUtils.getPaths;
import static com.ryanli.rxdownloader.data.retrofit.download.DownloadUtils.mkdirs;
import static java.io.File.separator;
import static java.lang.String.format;
import static java.util.Locale.getDefault;

/**
 * Auther: RyanLi
 * Data: 2018-06-18 21:47
 * Description:
 */
public class TemporaryRecord {

    private DownloadEntity entity;

    private String filePath;
    private String tempPath;
    private String lmfPath;

    private int maxRetryCount;
    private int maxThreads;

    private long contentLength;
    private String lastModify;

    private boolean rangeSupport = false;
    private boolean serverFileChanged = false;

    private FileHelper fileHelper;
    private DownloadApi downloadApi;


    public TemporaryRecord(DownloadEntity entity) {
        this.entity = entity;
    }

    /**
     * init needs info
     *
     * @param maxThreads      Max download threads
     * @param maxRetryCount   Max retry times
     * @param defaultSavePath Default save path;
     * @param downloadApi     API
     */
    public void init(int maxThreads, int maxRetryCount, String defaultSavePath,
                     DownloadApi downloadApi) {
        this.maxThreads = maxThreads;
        this.maxRetryCount = maxRetryCount;
        this.downloadApi = downloadApi;
        this.fileHelper = new FileHelper(maxThreads);

        String realSavePath;
        if (empty(entity.getSavePath())) {
            realSavePath = defaultSavePath;
            entity.setSavePath(defaultSavePath);
        } else {
            realSavePath = entity.getSavePath();
        }
        String cachePath = concat(realSavePath, separator, ".cache").toString();
        mkdirs(realSavePath, cachePath);

        String[] paths = getPaths(entity.getSaveName(), realSavePath);
        filePath = paths[0];
        tempPath = paths[1];
        lmfPath = paths[2];
    }


    /**
     * prepare normal download, create files and save last-modify.
     *
     * @throws IOException
     * @throws ParseException
     */
    public void prepareNormalDownload() throws IOException, ParseException {
        fileHelper.prepareDownload(lastModifyFile(), file(), contentLength, lastModify);
    }

    /**
     * prepare range download, create necessary files and save last-modify.
     *
     * @throws IOException
     * @throws ParseException
     */
    public void prepareRangeDownload() throws IOException, ParseException {
        fileHelper.prepareDownload(lastModifyFile(), tempFile(), file(), contentLength, lastModify);
    }

    /**
     * Read download range from record file.
     *
     * @param index index
     * @return
     * @throws IOException
     */
    public DownloadRangeEntity readDownloadRange(int index) throws IOException {
        return fileHelper.readDownloadRange(tempFile(), index);
    }

    /**
     * Normal download save.
     *
     * @param e        emitter
     * @param response response
     */
    public void save(FlowableEmitter<DownloadStatus> e, Response<ResponseBody> response) {
        fileHelper.saveFile(e, file(), response);
    }

    /**
     * Range download save
     *
     * @param emitter  emitter
     * @param index    download index
     * @param response response
     * @throws IOException
     */
    public void save(FlowableEmitter<DownloadStatus> emitter, int index, ResponseBody response)
            throws IOException {
        fileHelper.saveFile(emitter, index, tempFile(), file(), response);
    }

    /**
     * Normal download request.
     *
     * @return response
     */
    public Flowable<Response<ResponseBody>> download() {
        return downloadApi.download(null, entity.getUrl());
    }

    /**
     * Range download request
     *
     * @param index download index
     * @return response
     */
    public Flowable<Response<ResponseBody>> rangeDownload(final int index) {
        return Flowable
                .create(new FlowableOnSubscribe<DownloadRangeEntity>() {
                    @Override
                    public void subscribe(FlowableEmitter<DownloadRangeEntity> e) throws Exception {
                        DownloadRangeEntity range = readDownloadRange(index);
                        if (range.legal()) {
                            e.onNext(range);
                        }
                        e.onComplete();
                    }
                }, BackpressureStrategy.ERROR)
                .flatMap(new Function<DownloadRangeEntity, Publisher<Response<ResponseBody>>>() {
                    @Override
                    public Publisher<Response<ResponseBody>> apply(DownloadRangeEntity range)
                            throws Exception {
                        format(getDefault(), "Range %d start download from [%d] to [%d]", index, range.start, range.end);
                        String rangeStr = "bytes=" + range.start + "-" + range.end;
                        return downloadApi.download(rangeStr, entity.getUrl());
                    }
                });
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public boolean isSupportRange() {
        return rangeSupport;
    }

    public void setRangeSupport(boolean rangeSupport) {
        this.rangeSupport = rangeSupport;
    }

    public boolean isFileChanged() {
        return serverFileChanged;
    }

    public void setFileChanged(boolean serverFileChanged) {
        this.serverFileChanged = serverFileChanged;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public void setLastModify(String lastModify) {
        this.lastModify = lastModify;
    }

    public String getSaveName() {
        return entity.getSaveName();
    }

    public void setSaveName(String saveName) {
        entity.setSaveName(saveName);
    }

    public File file() {
        return new File(filePath);
    }

    public File tempFile() {
        return new File(tempPath);
    }

    public File lastModifyFile() {
        return new File(lmfPath);
    }

    public boolean fileComplete() {
        return file().length() == contentLength;
    }

    public boolean tempFileDamaged() throws IOException {
        return fileHelper.tempFileDamaged(tempFile(), contentLength);
    }

    public String readLastModify() throws IOException {
        return fileHelper.readLastModify(lastModifyFile());
    }

    public boolean fileNotComplete() throws IOException {
        return fileHelper.fileNotComplete(tempFile());
    }

    public File[] getFiles() {
        return new File[]{file(), tempFile(), lastModifyFile()};
    }


//    public void start() {
//        entity.setFlag(DownloadFlag.STARTED);
//        if (!dbManager.isExist(entity)) {
//            dbManager.insert(entity);
//        } else {
//            dbManager.update(entity);
//        }
//    }
//
//    public void update(DownloadStatus status) {
//        entity.setChunked(status.isChunked);
//        entity.setDownloadSize(status.getDownloadSize());
//        entity.setTotalSize(status.getTotalSize());
//        dbManager.update(entity);
//    }
//
//    public void error() {
//        entity.setFlag(DownloadFlag.FAILED);
//        dbManager.update(entity);
//    }
//
//    public void complete() {
//        entity.setFlag(DownloadFlag.COMPLETED);
//        dbManager.update(entity.getUrl());
//    }
//
//    public void cancel() {
//        entity.setFlag(DownloadFlag.PAUSED);
//        dbManager.update(entity);
//    }


}
