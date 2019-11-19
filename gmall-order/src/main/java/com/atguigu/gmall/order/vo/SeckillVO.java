package com.atguigu.gmall.order.vo;

import lombok.Data;

@Data
public class SeckillVO {//秒杀VO对象

    private Long userId;
    private Long skuId;
    private Integer count;

}
