package com.hjq.demo.chat.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 地区
 *
 * @author zhou
 */
@Entity(tableName = "areas")
public class AreaEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    /**
     * 地区名
     */
    private String name;

    /**
     * 父地区名
     */
    private String parentName;

    /**
     * 类型
     * "省" "市" "区"
     */
    private String type;

    /**
     * 排序
     */
    private Integer seq;

    private String postCode;

    public AreaEntity() {

    }

    @Ignore
    public AreaEntity(String name, String parentName, String type, Integer seq) {
        this.name = name;
        this.parentName = parentName;
        this.type = type;
        this.seq = seq;
    }

    @Ignore
    public AreaEntity(String name, String parentName, String type, Integer seq, String postCode) {
        this.name = name;
        this.parentName = parentName;
        this.type = type;
        this.seq = seq;
        this.postCode = postCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }
}
