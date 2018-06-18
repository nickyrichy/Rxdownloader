package com.ryanli.rxdownloader.data.retrofit.download;

/**
 * Auther: RyanLi
 * Data: 2018-06-18 21:52
 * Description:
 */
public class DownloadRangeEntity {

    public long start;
    public long end;
    public long size;

    public DownloadRangeEntity(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public boolean legal() {
        return start <= end;
    }
}
