package com.atguigu.gmall.search.vo;

import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsVO {
    private Long id;  //skuId
    private Long brandId; //品牌id
    private String brandName;  //品牌名
    private Long productCategoryId;  //sku的分类id
    private String productCategoryName; //sku的名字


    private String pic; //sku的默认图片
    private String name;//这是需要检索的sku的标题
    private BigDecimal price;//sku-price；
    private Integer sale;//sku-sale 销量
    private Long stock;//sku-stock 库存
    private Integer sort;//排序分 热度分

    //保存当前sku所有需要检索的属性；
    //检索属性来源于spu的基本属性中的search_type=1（销售属性都已经拼接在标题中了）
    private List<SpuAttributeValueVO> attrValueList;//检索属性,
    // (要判断是不是检索属性，要在pms_attr属性表里查看search_type类型，所以要将pms_product_attr_value与pms_attr进行关联查询是否是检索属性，根据spu_id与search_type是否等于1进行查询)
    //由于是属性表与属性值表进行关联查询，所以在AttrController或者ProductAttrValueController添加查询检索属性的controller方法都可以
}

/*
*
*   Long id;  //skuId
    Long brandId; //品牌id
    String brandName;  //品牌名
    Long productCategoryId;  //sku的分类id
    String productCategoryName; //sku的名字

    String pic; //sku的默认图片
    String name;//这是需要检索的sku的标题
    BigDecimal price;//sku-price；
    Integer sale;//sku-sale 销量
    Long stock;//sku-stock 库存
    Integer sort;//排序分 热度分
    //保存当前sku所有需要检索的属性；
    //检索属性来源于spu的基本属性中的search_type=1（销售属性都已经拼接在标题中了）
    List<SpuAttributeValueVO> attrValueList;//检索属性
* */
