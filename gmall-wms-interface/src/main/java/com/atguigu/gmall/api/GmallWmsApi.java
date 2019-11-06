package com.atguigu.gmall.api;

import com.atguigu.gmall.entity.WareSkuEntity;
import com.atguigu.gmall.core.bean.Resp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface GmallWmsApi {
//    根据skuId查询库存
    @GetMapping("wms/waresku/{skuId}")//库存管理->商品库存->库存维护->库存维护
    public Resp<List<WareSkuEntity>> queryWareBySkuId(@PathVariable("skuId") Long skuId);

}
