package com.ryanli.rxdownloader.data.retrofit.download;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.FlowableEmitter;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Auther: RyanLi
 * Data: 2018-06-11 23:17
 * Description:
 */
public class FileHelper {

    public static void saveFile(FlowableEmitter<String> emitter, File saveFile,
                                Response<ResponseBody> resp) {

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            try {
                int readLen;
                int downloadSize = 0;
                byte[] buffer = new byte[8192];

                inputStream = resp.body().byteStream();
                outputStream = new FileOutputStream(saveFile);

                long contentLength = resp.body().contentLength();


                while ((readLen = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, readLen);
                    downloadSize += readLen;
                    emitter.onNext((downloadSize * 100f / contentLength) + "");
                }

                outputStream.flush(); // This is important!!!
                emitter.onComplete();
            } finally {
                closeQuietly(inputStream);
                closeQuietly(outputStream);
                closeQuietly(resp.body());
            }
        } catch (IOException e) {
            emitter.onError(e);
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }
}
