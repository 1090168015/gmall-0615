package com.atguigu.gmall.oms.service;

import com.atguigu.gmall.oms.entity.RefundInfoEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.QueryCondition;


/**
 * 退款信息
 *
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:14:24
 */
public interface RefundInfoService extends IService<RefundInfoEntity> {

    PageVo queryPage(QueryCondition params);
}

