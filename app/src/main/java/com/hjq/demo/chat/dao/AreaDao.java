package com.hjq.demo.chat.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hjq.demo.chat.entity.AreaEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

/**
 * 地区
 *
 * @author zhou
 */
@Dao
public interface AreaDao {

    /**
     * 获取所有省份列表
     *
     * @return 所有省份列表
     */
    @Query("select * from areas where type = (:type) order by seq asc")
    Single<List<AreaEntity>> getProvinceList(String type);

    /**
     * 获取某个省所有市列表
     *
     * @param provinceName 省
     * @return 某个省所有市列表
     */
    @Query("select * from areas where type = (:type) and parentName = (:provinceName) order by seq asc")
    Single<List<AreaEntity>> getCityListByProvinceName(String type, String provinceName);

    /**
     * 获取某个市所有区县列表
     *
     * @param cityName 市
     * @return 某个市所有区县列表
     */
    @Query("select * from areas where type = (:type) and parentName = (:cityName) order by seq asc")
    Single<List<AreaEntity>> getDistrictListByCityName(String type, String cityName);

    /**
     * 根据市和区获取某个区
     * 某些区会重复
     *
     * @param cityName     市
     * @param districtName 区
     * @return 区
     */
    @Query("select * from areas where type = (:type) and parentName = (:cityName) and name = (:districtName) order by seq asc")
    Single<List<AreaEntity>> getDistrictByCityNameAndDistrictName(String type, String cityName, String districtName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveAreas(List<AreaEntity> area);
}