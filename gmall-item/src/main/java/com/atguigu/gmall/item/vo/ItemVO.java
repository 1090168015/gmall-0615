package com.atguigu.gmall.item.vo;

import VO.ItemSaleVO;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVO;
import lombok.Data;

import java.util.List;
@Data
public class ItemVO extends SkuInfoEntity {
    private BrandEntity brand;  //品牌对象
    private CategoryEntity category;    //分类对象
    private SpuInfoEntity spuInfo;  //spu对象
    private List<String> pic;//sku的图片列表
    private List<ItemSaleVO> sales;//营销属性
    private Boolean store;//是否有货
    private List<SkuSaleAttrValueEntity> skuSales;//spu下所有的销售属性
    private SpuInfoDescEntity desc;//描述信息
    private List<GroupVO> groups;//组及组下的规格属性
}
