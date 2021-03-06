package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.SeckillSkuNoticeEntity;
import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.QueryCondition;


/**
 * 秒杀商品通知订阅
 *
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:18:10
 */
public interface SeckillSkuNoticeService extends IService<SeckillSkuNoticeEntity> {

    PageVo queryPage(QueryCondition params);
}

