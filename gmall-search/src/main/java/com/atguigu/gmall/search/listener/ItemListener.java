package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.entity.WareSkuEntity;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import com.atguigu.gmall.search.fegin.GmallPmsClient;
import com.atguigu.gmall.search.fegin.GmallWmsClient;
import com.atguigu.gmall.search.vo.GoodsVO;
import io.searchbox.client.JestClient;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class ItemListener {
    @Autowired
    private JestClient jestClient;
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "GMALL-SEARCH-QUEUE",durable = "true"),
        exchange = @Exchange(value = "GMALL-ITEM-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
        key = {"item.*"}))
    public void listener(Map<String,Object> map){
        if (CollectionUtils.isEmpty(map)){
                return;
            }
        Long spuId = (Long) map.get("id");
        String type = map.get("type").toString();
        if (StringUtils.equals( "insert",type) ||StringUtils.equals("update",type)){
            Resp<List<SkuInfoEntity>> skuResp = gmallPmsClient.querySkuBySpuId(spuId);//获取SKU对象，一个SPU可以对应多个SKU
            List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
            if (CollectionUtils.isEmpty(skuInfoEntities)){//skuInfoEntities判断SKU是否为空，如果为空，结束这次遍历到的SKU对象，继续下次遍历
                return;   //SPU可以有多个SKU也可以没有SKU
            }
            skuInfoEntities.forEach(skuInfoEntity -> {
                GoodsVO goodsVO = new GoodsVO();
                //设置sku相关数据
                goodsVO.setName(skuInfoEntity.getSkuTitle());
                goodsVO.setId(skuInfoEntity.getSkuId());
                goodsVO.setPrice(skuInfoEntity.getPrice());
                goodsVO.setPic(skuInfoEntity.getSkuDefaultImg());
                goodsVO.setSale(100);//设置销量
                goodsVO.setSort(0);//设置总和排序
                //设置品牌相关   Resp里封装了响应信息
                Resp<BrandEntity> brandEntityResp = gmallPmsClient.queryBrandBySpuId(skuInfoEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResp.getData();//获取品牌对象brandEntity
                if (brandEntity != null) {
                    goodsVO.setBrandId(skuInfoEntity.getBrandId());
                    goodsVO.setBrandName(brandEntity.getName());
                }
                //设置分类相关数据
                Resp<CategoryEntity> categoryEntityResp = gmallPmsClient.queryCategoryBySpuId(skuInfoEntity.getCatalogId());
                CategoryEntity categoryEntity = categoryEntityResp.getData();
                if (categoryEntity != null) {
                    goodsVO.setProductCategoryId(skuInfoEntity.getCatalogId());
                    goodsVO.setProductCategoryName(categoryEntity.getName());
                }
                //设置搜索属性相关数据
                Resp<List<SpuAttributeValueVO>> searchAttrValueResp = gmallPmsClient.querySearchAttrValue(spuId);
                List<SpuAttributeValueVO> spuAttributeValueVOList = searchAttrValueResp.getData();
                goodsVO.setAttrValueList(spuAttributeValueVOList);
                //库存相关
                Resp<List<WareSkuEntity>> wareResp = gmallWmsClient.queryWareBySkuId(skuInfoEntity.getSkuId());
                List<WareSkuEntity> wareSkuEntities = wareResp.getData();
                if (wareSkuEntities.stream().anyMatch(t -> t.getStock() > 0)) {
                    goodsVO.setStock(1L);
                } else {
                    goodsVO.setStock(0L);
                }
//传入数据源goodsVO，创建index(索引库名)，type(数据类型，相当于数据库里的表)，id(数据id)
                Index index = new Index.Builder(goodsVO).index("goods").type("info").id(skuInfoEntity.getSkuId().toString()).build();
                try {
                    this.jestClient.execute(index);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }else if (StringUtils.equals("delete",type)){
            Resp<List<SkuInfoEntity>> skuResp = gmallPmsClient.querySkuBySpuId(spuId);
            List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
            if (CollectionUtils.isEmpty(skuInfoEntities)){
                return;
            }
            skuInfoEntities.forEach(skuInfoEntity -> {
                Delete delete = new Delete.Builder(skuInfoEntity.getSkuId().toString()).index("goods").type("info").build();
                try {
                    jestClient.execute(delete);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "GMALL-SEARCH-QUEUE",durable = "true"),
                exchange = @Exchange(value = "GMALL-ITEM-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
                key = {"item.*"}))
    public void lisetnerSecond(Map<String,Object> map){
        if (CollectionUtils.isEmpty(map)){
            return;
        }
        Long spuId = (Long) map.get("id");
        String type = map.get("type").toString();
        if (StringUtils.equals("insert",type)||StringUtils.equals("update",type)){
            Resp<List<SkuInfoEntity>> skuResp = gmallPmsClient.querySkuBySpuId(spuId);
            List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
            if (CollectionUtils.isEmpty(skuInfoEntities)){
                return;
            }


            skuInfoEntities.forEach(skuInfoEntity -> {
                GoodsVO goodsVO = new GoodsVO();
                //设置sku相关数据
                goodsVO.setName(skuInfoEntity.getSkuTitle());
                goodsVO.setId(skuInfoEntity.getSkuId());
                goodsVO.setPic(skuInfoEntity.getSkuDefaultImg());
                goodsVO.setPrice(skuInfoEntity.getPrice());
                goodsVO.setSale(100);
                goodsVO.setSort(0);
                //设置品牌相关数据
                Resp<BrandEntity> brandEntityResp = gmallPmsClient.queryBrandBySpuId(skuInfoEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResp.getData();
                if (brandEntity!=null){
                    goodsVO.setBrandId(skuInfoEntity.getBrandId());
                    goodsVO.setBrandName(brandEntity.getName());
                }
                //设置分类相关数据
                Resp<CategoryEntity> categoryEntityResp = gmallPmsClient.queryCategoryBySpuId(skuInfoEntity.getCatalogId());
                CategoryEntity categoryEntityRespData = categoryEntityResp.getData();
                if (categoryEntityResp != null) {
                    goodsVO.setProductCategoryId(skuInfoEntity.getCatalogId());
                    goodsVO.setProductCategoryName(categoryEntityRespData.getName());
                }
                //设置搜索属性相关数据
                Resp<List<SpuAttributeValueVO>> searchAttrValue = gmallPmsClient.querySearchAttrValue(skuInfoEntity.getSpuId());
                List<SpuAttributeValueVO> spuAttributeValueVOS = searchAttrValue.getData();
                goodsVO.setAttrValueList(spuAttributeValueVOS);
                //库存相关
                Resp<List<WareSkuEntity>> wareResp = gmallWmsClient.queryWareBySkuId(skuInfoEntity.getSkuId());
                List<WareSkuEntity> wareSkuEntities = wareResp.getData();
                if (wareSkuEntities.stream().anyMatch(t->t.getStock()>0)){
                    goodsVO.setStock(1L);
                }else {
                    goodsVO.setStock(0L);
                }
                Index index = new Index.Builder(goodsVO).index("goods").type("info").id(skuInfoEntity.getSkuId().toString()).build();
                try {
                    jestClient.execute(index);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }else if (StringUtils.equals("delete",type)){
            Resp<List<SkuInfoEntity>> skuResp = gmallPmsClient.querySkuBySpuId(spuId);
            List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
            if (CollectionUtils.isEmpty(skuInfoEntities)){
                return;
            }
            skuInfoEntities.forEach(skuInfoEntity -> {

                Delete delete = new Delete.Builder(skuInfoEntity.getSkuId().toString()).index("goods").type("info").build();

                try {
                    jestClient.execute(delete);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            });


        }
    }
}
/*
    private Long stock;//sku-stock 库存
    //保存当前sku所有需要检索的属性；
    //检索属性来源于spu的基本属性中的search_type=1（销售属性都已经拼接在标题中了）
    private List<SpuAttributeValueVO> attrValueList;//检索属性,
    // (要判断是不是检索属性，要在pms_attr属性表里查看search_type类型，所以要将pms_product_attr_value与pms_attr进行关联查询是否是检索属性，根据spu_id与search_type是否等于1进行查询)
    //由于是属性表与属性值表进行关联查询，所以在AttrController或者ProductAttrValueController添加查询检索属性的controller方法都*/