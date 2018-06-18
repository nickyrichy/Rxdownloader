package com.ryanli.rxdownloader.data.retrofit.download;

import com.ryanli.rxdownloader.data.db.annotation.Column;
import com.ryanli.rxdownloader.data.db.annotation.PrimaryKey;
import com.ryanli.rxdownloader.data.db.annotation.Table;

/**
 * Auther: RyanLi
 * Data: 2018-06-18 21:47
 * Description:
 */
@Table
public class DownloadEntity {

    @Column
    @PrimaryKey
    private String url;
    @Column
    private String saveName;
    @Column
    private String savePath;
    @Column
    private int flag;
    @Column
    public boolean isChunked = false;
    @Column
    private long totalSize;
    @Column
    private long downloadSize;

    public boolean isChunked() {
        return isChunked;
    }

    public void setChunked(boolean chunked) {
        isChunked = chunked;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getDownloadSize() {
        return downloadSize;
    }

    public void setDownloadSize(long downloadSize) {
        this.downloadSize = downloadSize;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSaveName() {
        return saveName;
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public static class Builder {
        private String url;
        private String saveName;
        private String savePath;
        private int flag;

        public Builder setFlag(int flag) {
            this.flag = flag;
            return this;
        }

        public Builder(String url) {
            this.url = url;
        }

        public Builder setSaveName(String saveName) {
            this.saveName = saveName;
            return this;
        }

        public Builder setSavePath(String savePath) {
            this.savePath = savePath;
            return this;
        }

        public DownloadEntity build() {
            DownloadEntity entity = new DownloadEntity();
            entity.url = this.url;
            entity.saveName = this.saveName;
            entity.savePath = this.savePath;
            entity.flag = this.flag;
            return entity;
        }
    }
}
