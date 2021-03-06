package com.atguigu.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.usm.entity.MemberCollectSpuEntity;
import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.QueryCondition;


/**
 * 会员收藏的商品
 *
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:21:33
 */
public interface MemberCollectSpuService extends IService<MemberCollectSpuEntity> {

    PageVo queryPage(QueryCondition params);
}

