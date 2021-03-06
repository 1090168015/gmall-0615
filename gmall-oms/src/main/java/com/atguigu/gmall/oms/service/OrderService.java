package com.atguigu.gmall.oms.service;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.QueryCondition;


/**
 * 订单
 *
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:14:24
 */
public interface OrderService extends IService<OrderEntity> {

    PageVo queryPage(QueryCondition params);

    OrderEntity createOrder(OrderSubmitVO submitVO);

    int closeOrder(String orderToken);

    int success(String orderToken);
}

