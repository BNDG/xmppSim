package com.hjq.demo.chat.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 搜索历史
 *
 * @author zhou
 */
@Entity(tableName = "searchHistory")
public class SearchHistory{
    @PrimaryKey(autoGenerate = true)
    public int id;
    private String userId;
    private String keyword;
    private String createTime;
    private Integer count;

    public SearchHistory() {

    }

    public SearchHistory(String keyword) {
        this.keyword = keyword;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
