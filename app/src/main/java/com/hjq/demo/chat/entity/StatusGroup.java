package com.hjq.demo.chat.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.List;

/**
 * 用户状态组
 *
 * @author zhou
 */
@Entity(tableName = "statusGroup")
public class StatusGroup implements Serializable {
    @PrimaryKey
    public int id;
    private String name;
    private List<Status> statusList;
    private String statusListJson;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Status> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<Status> statusList) {
        this.statusList = statusList;
    }

    public String getStatusListJson() {
        return statusListJson;
    }

    public void setStatusListJson(String statusListJson) {
        this.statusListJson = statusListJson;
    }

}