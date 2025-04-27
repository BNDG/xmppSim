package com.hjq.demo.chat.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hjq.demo.chat.entity.RegionEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface RegionDao {
    @Query("select * from region order by seq asc")
    Single<List<RegionEntity>> getRegionList();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveRegions(List<RegionEntity> region);
}
