package com.atguigu.gmall.sms.service;

import VO.ItemSaleVO;
import VO.SaleVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品sku积分设置
 *
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:18:10
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    PageVo queryPage(QueryCondition params);

    void saveSale(SaleVO saleVO);

    List<ItemSaleVO> queryItemSalVOS(Long skuId);
}

