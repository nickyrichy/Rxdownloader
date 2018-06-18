package com.ryanli.rxdownloader.data.retrofit.httpapis;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Auther: RyanLi
 * Data: 2018-06-06 16:14
 * Description: 文件下载api
 */
public interface DownloadApi {

    @GET
    @Streaming
    Flowable<Response<ResponseBody>> download(@Header("range") String range, @Url String url);


    @HEAD
    Observable<Response<Void>> check(@Url String url);

    @HEAD
    Observable<Response<Void>> checkRangeByHead(@Header("Range") String range,
                                                @Url String url);

    @HEAD
    Observable<Response<Void>> checkFileByHead(@Header("If-Modified-Since") String lastModify,
                                               @Url String url);

}
