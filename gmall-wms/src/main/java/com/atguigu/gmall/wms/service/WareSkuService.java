package com.atguigu.gmall.wms.service;

import com.atguigu.gmall.vo.SkuLockVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.entity.WareSkuEntity;
import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品库存
 *
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:11:19
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageVo queryPage(QueryCondition params);

    String checkAndLock(List<SkuLockVO> skuLockVOS);
}

