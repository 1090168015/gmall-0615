package com.atguigu.gmall.pms.vo;

import lombok.Data;

@Data//搜索属性Vo
public class SpuAttributeValueVO {//pms_product_attr_value：基本属性值表，跟SPU相关
    private Long productAttributeId; //当前sku对应的属性的attr_id
    private String name;//属性名  电池
    private String value;//3G   3000mah
}
