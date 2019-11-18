package com.atguigu.gmall.oms.vo;

import VO.ItemSaleVO;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVO {
    private Long skuId;//商品id
    private String title;//标题
    private BigDecimal price;//图片
    private String defaultImage;//价格
    private Integer count;//购买数量
    private Boolean store;//是否有库存
    private List<SkuSaleAttrValueEntity> skuSaleAttrValue;//商品规格参数
    private List<ItemSaleVO> sales;//营销信息
    private BigDecimal weight;//商品重量
}
