package com.ryanli.rxdownloader.data.entity;

import com.ryanli.rxdownloader.data.db.annotation.Column;
import com.ryanli.rxdownloader.data.db.annotation.PrimaryKey;
import com.ryanli.rxdownloader.data.db.annotation.Table;

/**
 * Auther: RyanLi
 * Data: 2018-06-07 12:28
 * Description: 下载记录实体类
 */
@Table
public class DownloadRecordEntity {

    @Column
    private String url = "地址";
    @Column
    private String saveName = "名字";
    @Column
    private String savePath = "路径";
    @PrimaryKey
    @Column
    private long date; //格林威治时间,毫秒
    private String missionId;

    public DownloadRecordEntity(long date) {
        this.date = date;
    }

    public DownloadRecordEntity() {
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
