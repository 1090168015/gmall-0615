package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.SeckillSkuRelationEntity;
import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.QueryCondition;


/**
 * 秒杀活动商品关联
 *
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:18:10
 */
public interface SeckillSkuRelationService extends IService<SeckillSkuRelationEntity> {

    PageVo queryPage(QueryCondition params);
}

