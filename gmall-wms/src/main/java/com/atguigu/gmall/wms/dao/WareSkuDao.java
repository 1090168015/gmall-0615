package com.atguigu.gmall.wms.dao;

import com.atguigu.gmall.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:11:19
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    List<WareSkuEntity> checkStore(@Param("skuId") Long skuId, @Param("count")Integer count);//商品id
    int lock(@Param("id")Long id ,@Param("count")Integer count);//，锁定库存，库存id,和锁定数量
    int unlock(@Param("id")Long id ,@Param("count")Integer count);//解锁库存，库存id,和锁定数量
}
