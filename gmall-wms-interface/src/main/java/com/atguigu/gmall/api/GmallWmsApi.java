package com.atguigu.gmall.api;

import com.atguigu.gmall.entity.WareSkuEntity;
import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.vo.SkuLockVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallWmsApi {
//    根据skuId查询库存
    @GetMapping("wms/waresku/{skuId}")//库存管理->商品库存->库存维护->库存维护
    public Resp<List<WareSkuEntity>> queryWareBySkuId(@PathVariable("skuId") Long skuId);

    @PostMapping("wms/waresku/check/lock")//验证和锁库存
    public Resp<Object> checkAndLock(@RequestBody List<SkuLockVO> skuLockVOS);

}
