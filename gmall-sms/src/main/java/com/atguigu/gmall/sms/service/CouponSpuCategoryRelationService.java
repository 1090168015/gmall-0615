package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.CouponSpuCategoryRelationEntity;
import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.QueryCondition;


/**
 * 优惠券分类关联
 *
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:18:10
 */
public interface CouponSpuCategoryRelationService extends IService<CouponSpuCategoryRelationEntity> {

    PageVo queryPage(QueryCondition params);
}

