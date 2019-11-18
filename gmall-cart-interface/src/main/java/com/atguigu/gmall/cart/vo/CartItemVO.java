package com.atguigu.gmall.cart.vo;

import lombok.Data;

@Data
public class CartItemVO {
    private  Long skuId;//购物车对应的商品id
    private Integer count;//购物车对应的商品数量

}
