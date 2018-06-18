package com.ryanli.rxdownloader.data.retrofit.download;

import android.text.TextUtils;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.ryanli.rxdownloader.data.utils.LogUtils;

import org.reactivestreams.Publisher;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiPredicate;
import okhttp3.internal.http.HttpHeaders;
import retrofit2.Response;

import static android.text.TextUtils.concat;
import static java.io.File.separator;
import static java.lang.String.format;
import static java.util.Locale.getDefault;
import static java.util.TimeZone.getTimeZone;

/**
 * Auther: RyanLi
 * Data: 2018-06-18 19:12
 * Description:
 */
public class DownloadUtils {

    public static boolean empty(String string) {
        return TextUtils.isEmpty(string);
    }

    /**
     * convert long to GMT string
     *
     * @param lastModify long
     * @return String
     */
    public static String longToGMT(long lastModify) {
        Date d = new Date(lastModify);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(getTimeZone("GMT"));
        return sdf.format(d);
    }

    /**
     * convert GMT string to long
     *
     * @param GMT String
     * @return long
     * @throws ParseException
     */
    public static long GMTToLong(String GMT) throws ParseException {
        if (GMT == null || "".equals(GMT)) {
            return new Date().getTime();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(getTimeZone("GMT"));
        Date date = sdf.parse(GMT);
        return date.getTime();
    }

    public static void close(Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
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

    public static <U> ObservableTransformer<U, U> retry(final String hint, final int retryCount) {
        return new ObservableTransformer<U, U>() {
            @Override
            public ObservableSource<U> apply(Observable<U> upstream) {
                return upstream.retry(new BiPredicate<Integer, Throwable>() {
                    @Override
                    public boolean test(Integer integer, Throwable throwable) throws Exception {
                        return retry(hint, retryCount, integer, throwable);
                    }
                });
            }
        };
    }

    public static <U> FlowableTransformer<U, U> retry2(final String hint, final int retryCount) {
        return new FlowableTransformer<U, U>() {
            @Override
            public Publisher<U> apply(Flowable<U> upstream) {
                return upstream.retry(new BiPredicate<Integer, Throwable>() {
                    @Override
                    public boolean test(Integer integer, Throwable throwable) throws Exception {
                        return retry(hint, retryCount, integer, throwable);
                    }
                });
            }
        };
    }

    public static void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public static String lastModify(Response<?> response) {
        return response.headers().get("Last-Modified");
    }

    public static long contentLength(Response<?> response) {
        return HttpHeaders.contentLength(response.headers());
    }

    public static String fileName(String url, Response<?> response) {
        String fileName = contentDisposition(response);
        if (empty(fileName)) {
            fileName = url.substring(url.lastIndexOf('/') + 1);
        }
        if (fileName.startsWith("\"")) {
            fileName = fileName.substring(1);
        }
        if (fileName.endsWith("\"")) {
            fileName = fileName.substring(0, fileName.length() - 1);
        }
        return fileName;
    }

    public static String contentDisposition(Response<?> response) {
        String disposition = response.headers().get("Content-Disposition");
        if (empty(disposition)) {
            return "";
        }
        Matcher m = Pattern.compile(".*filename=(.*)").matcher(disposition.toLowerCase());
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }
    }

    public static boolean isChunked(Response<?> response) {
        return "chunked".equals(transferEncoding(response));
    }

    public static boolean notSupportRange(Response<?> response) {
        return TextUtils.isEmpty(contentRange(response)) || contentLength(response) == -1 ||
                isChunked(response);
    }

    /**
     * Format file size to String
     *
     * @param size long
     * @return String
     */
    public static String formatSize(long size) {
        String hrSize;
        double b = size;
        double k = size / 1024.0;
        double m = ((size / 1024.0) / 1024.0);
        double g = (((size / 1024.0) / 1024.0) / 1024.0);
        double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);
        DecimalFormat dec = new DecimalFormat("0.00");
        if (t > 1) {
            hrSize = dec.format(t).concat(" TB");
        } else if (g > 1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else if (k > 1) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" B");
        }
        return hrSize;
    }

    public static Boolean retry(String hint, int maxRetryCount, Integer integer, Throwable throwable) {
        if (throwable instanceof ProtocolException) {
            if (integer < maxRetryCount + 1) {
                LogUtils.i(format(getDefault(), "%s get [%s] error, now retry [%d] times", hint, "ProtocolException", integer));
                return true;
            }
            return false;
        } else if (throwable instanceof UnknownHostException) {
            if (integer < maxRetryCount + 1) {
                LogUtils.i(format(getDefault(), "%s get [%s] error, now retry [%d] times", hint, "UnknownHostException", integer));
                return true;
            }
            return false;
        } else if (throwable instanceof HttpException) {
            if (integer < maxRetryCount + 1) {
                LogUtils.i(format(getDefault(), "%s get [%s] error, now retry [%d] times", "HttpException", integer));
                return true;
            }
            return false;
        } else if (throwable instanceof SocketTimeoutException) {
            if (integer < maxRetryCount + 1) {
                LogUtils.i(format(getDefault(), "%s get [%s] error, now retry [%d] times", "SocketTimeoutException", integer));
                return true;
            }
            return false;
        } else if (throwable instanceof ConnectException) {
            if (integer < maxRetryCount + 1) {
                LogUtils.i(format(getDefault(), "%s get [%s] error, now retry [%d] times", hint, "ConnectException", integer));
                return true;
            }
            return false;
        } else if (throwable instanceof SocketException) {
            if (integer < maxRetryCount + 1) {
                LogUtils.i(format(getDefault(), "%s get [%s] error, now retry [%d] times", hint, "SocketException", integer));
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     * return file paths
     *
     * @param saveName saveName
     * @param savePath savePath
     * @return filePath, tempPath, lmfPath
     */
    public static String[] getPaths(String saveName, String savePath) {
        String cachePath = concat(savePath, separator, ".cache").toString();
        String filePath = concat(savePath, separator, saveName).toString();
        String tempPath = concat(cachePath, separator, saveName, ".tmp").toString();
        String lmfPath = concat(cachePath, separator, saveName, ".lmf").toString();
        return new String[]{filePath, tempPath, lmfPath};
    }

    /**
     * return files
     *
     * @param saveName saveName
     * @param savePath savePath
     * @return file, tempFile, lmfFile
     */
    public static File[] getFiles(String saveName, String savePath) {
        String[] paths = getPaths(saveName, savePath);
        return new File[]{new File(paths[0]), new File(paths[1]), new File(paths[2])};
    }

    /**
     * create dirs with params path
     *
     * @param paths paths
     */
    public static void mkdirs(String... paths) {
        for (String each : paths) {
            File file = new File(each);
            if (file.exists() && file.isDirectory()) {
                LogUtils.i(format(getDefault(), "Path [%s] exists.", each));
            } else {
                LogUtils.i(format(getDefault(), "Path [%s] not exists, so create.", each));
                boolean flag = file.mkdirs();
                if (flag) {
                    LogUtils.i(format(getDefault(), "Path [%s] create success.", each));
                } else {
                    LogUtils.i(format(getDefault(), "Path [%s] create failed.", each));
                }
            }
        }
    }

    /**
     * delete files
     *
     * @param files files
     */
    public static void deleteFiles(File... files) {
        for (File each : files) {
            if (each.exists()) {
                boolean flag = each.delete();
                if (flag) {
                    LogUtils.i(format(getDefault(), "File [%s] delete success.", each.getName()));
                } else {
                    LogUtils.i(format(getDefault(), "File [%s] delete failed.", each.getName()));
                }
            }
        }
    }

    private static String transferEncoding(Response<?> response) {
        return response.headers().get("Transfer-Encoding");
    }

    private static String contentRange(Response<?> response) {
        return response.headers().get("Content-Range");
    }
}

