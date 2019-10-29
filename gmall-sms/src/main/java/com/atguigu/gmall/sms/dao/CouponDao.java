package com.atguigu.gmall.sms.dao;

import com.atguigu.gmall.sms.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:18:10
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
