package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.QueryCondition;


/**
 * 商品满减信息
 *
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:18:10
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageVo queryPage(QueryCondition params);
}

