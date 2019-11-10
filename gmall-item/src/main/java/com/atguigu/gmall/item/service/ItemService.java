package com.atguigu.gmall.item.service;

import VO.ItemSaleVO;
import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.entity.WareSkuEntity;
import com.atguigu.gmall.item.fegin.GmallPmsClient;
import com.atguigu.gmall.item.fegin.GmallSmsClient;
import com.atguigu.gmall.item.fegin.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVO;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;

    public ItemVO item(Long skuId) {
        ItemVO itemVO = new ItemVO();
        // 1. 查询sku信息
        Resp<SkuInfoEntity> skuInfoEntityResp = gmallPmsClient.querySkuById(skuId);
        SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
        BeanUtils.copyProperties(skuInfoEntity,itemVO);
        Long spuId = skuInfoEntity.getSpuId();

        // 2.品牌
        Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandBySpuId(skuInfoEntity.getBrandId());
        itemVO.setBrand(brandEntityResp.getData());
        // 3.分类
        Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCategoryBySpuId(skuInfoEntity.getCatalogId());
        itemVO.setCategory(categoryEntityResp.getData());
        // 4.spu信息
        Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsClient.querySpuById(spuId);
        itemVO.setSpuInfo(spuInfoEntityResp.getData());
        // 5.设置图片信息
        Resp<List<String>> picsResp = this.gmallPmsClient.queryPicsBySkuId(skuId);
        itemVO.setPic(picsResp.getData());
        // 6.营销信息
        Resp<List<ItemSaleVO>> itemSalveResp = this.gmallSmsClient.queryItemSalveVOs(skuId);
        itemVO.setSales(itemSalveResp.getData());

        // 7.是否有货
        Resp<List<WareSkuEntity>> wareResp = this.gmallWmsClient.queryWareBySkuId(skuId);
        List<WareSkuEntity> wareSkuEntities = wareResp.getData();
        itemVO.setStore(wareSkuEntities.stream().anyMatch(t ->t.getStock()>0));
        // 8.spu所有的销售属性
        Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = this.gmallPmsClient.querSaleAttrValues(spuId);
        itemVO.setSkuSales(saleAttrValueResp.getData());
        // 9.spu的描述信息
        Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsClient.querySpuDescById(spuId);
        itemVO.setDesc(spuInfoDescEntityResp.getData());
        // 10.规格属性分组及组下的规格参数及值
        Resp<List<GroupVO>> listResp = this.gmallPmsClient.queryGroupVOByCid(skuInfoEntity.getCatalogId(), spuId);
        itemVO.setGroups(listResp.getData());
        return itemVO;

    }
}
