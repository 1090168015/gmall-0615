package com.atguigu.gmall.vo;

import lombok.Data;

@Data
public class SkuLockVO {//验库锁库VO

    private Long SkuId;//商品id
    private Integer count;//锁定商品数量
    private Boolean lock;//锁定成功true,锁定失败false
    private Long skuWareId;//锁定库存的id

    private String orderToken;//订单号


}
