package com.hjq.demo.chat.entity;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 地区
 *
 * @author zhou
 */
@Entity(tableName = "region")
public class RegionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    private String parentId;
    private String level;
    private String name;
    private String code;
    private Float seq;


    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Float getSeq() {
        return seq;
    }

    public void setSeq(Float seq) {
        this.seq = seq;
    }
}
