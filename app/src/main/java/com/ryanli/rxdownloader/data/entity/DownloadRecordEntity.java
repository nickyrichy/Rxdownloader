package com.ryanli.rxdownloader.data.entity;

/**
 * Auther: RyanLi
 * Data: 2018-06-07 12:28
 * Description: 下载记录实体类
 */
public class DownloadRecordEntity {

    private String url;
    private String saveName;
    private String savePath;
    private long date; //格林威治时间,毫秒
    private String missionId;

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

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

}
