package com.atguigu.gmall.oms.dao;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:14:24
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    int closeOrder(String orderToken);//update oms_order set `status`=4  where order_sn=#{orderToken} and `status`=0

    int success(String orderToken);
}
