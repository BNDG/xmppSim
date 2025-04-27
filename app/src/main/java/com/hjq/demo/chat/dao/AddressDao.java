package com.hjq.demo.chat.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.hjq.demo.chat.entity.Address;

import java.util.List;

/**
 * 地址
 *
 * @author zhou
 */
@Dao
public interface AddressDao {

    /**
     * 获取地址列表
     *
     * @return 地址列表
     */
    @Query("SELECT * FROM address")
    List<Address> getAddressList();

    /**
     * 保存地址
     * 如果插入的数据在数据库表中已经存在，就会抛出异常
     *
     * @param address 地址
     */
    @Insert
    void saveAddress(Address address);

    //  如果通过 Entity 来删除数据，传进来的参数需要包含主键
    @Delete
    void delete(Address user);

    /**
     * 清除地址
     */
    @Query("DELETE FROM address")
    void clearAddress();

    /**
     * 根据地址ID获取地址详情
     *
     * @param addressId 地址ID
     * @return 地址详情
     */
    @Query("SELECT * FROM address WHERE addressId=(:addressId)")
    Address getAddressByAddressId(String addressId);

    /**
     * 根据地址ID删除地址
     *
     * @param addressId 地址ID
     */
    @Query("delete from address where addressId =(:addressId)")
    void deleteAddressByAddressId(String addressId);

}