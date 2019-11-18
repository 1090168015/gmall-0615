package com.atguigu.gmall.search;

import com.atguigu.gmall.entity.WareSkuEntity;
import com.atguigu.gmall.search.fegin.GmallPmsClient;
import com.atguigu.gmall.search.fegin.GmallWmsClient;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.core.bean.QueryCondition;
import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private JestClient jestClient;
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;



    @Test
    public void importData(){
        Long pageNum =1L;
        Long pageSize =100L;
        do{ //1.分页SPU查询数据
            QueryCondition queryCondition = new QueryCondition();;//分页条件对象，为什么要分页，是为了将数据分批导入ES
            queryCondition.setPage(pageNum);//给分页条件设置值
            queryCondition.setLimit(pageSize);
            Resp<List<SpuInfoEntity>> listResp = gmallPmsClient.querySpuPage(queryCondition);//根据分页条件获取分页对象
            List<SpuInfoEntity> spuInfoEntities = listResp.getData();//根据分页对象获取分页数据

            //遍历spuInfoEntities获取sku数据用于导入ES索引库中
            for (SpuInfoEntity spuInfoEntity : spuInfoEntities) {
                Resp<List<SkuInfoEntity>> skuResp = gmallPmsClient.querySkuBySpuId(spuInfoEntity.getId());//获取SKU对象，一个SPU可以对应多个SKU
                List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
                if (CollectionUtils.isEmpty(skuInfoEntities)){//skuInfoEntities判断SKU是否为空，如果为空，结束这次遍历到的SKU对象，继续下次遍历
                    continue;   //SPU可以有多个SKU也可以没有SKU
                }
                skuInfoEntities.forEach(skuInfoEntity -> {
                    GoodsVO goodsVO = new GoodsVO();
                    //设置sku相关数据
                    goodsVO.setName(skuInfoEntity.getSkuTitle());
                    goodsVO.setId(skuInfoEntity.getSkuId());
                    goodsVO.setPrice(skuInfoEntity.getPrice());
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
                    Resp<List<SpuAttributeValueVO>> searchAttrValueResp = gmallPmsClient.querySearchAttrValue(spuInfoEntity.getId());
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
            }
            pageSize =Long.valueOf(spuInfoEntities.size());//获取当天也记录数，用于与while里100进行比较，判断是否继续循环
            pageNum++;//下一页
        }while (pageSize ==100);//循环条件
    }



    @Test
    void importDataSecond() {
        Long pageNum=1L;
        Long pageSize=100L;
        do {
            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setPage(pageNum);
            queryCondition.setLimit(pageSize);
            Resp<List<SpuInfoEntity>> listResp = gmallPmsClient.querySpuPage(queryCondition);
            List<SpuInfoEntity> spuInfoEntities = listResp.getData();
            for (SpuInfoEntity spuInfoEntity : spuInfoEntities) {
                Resp<List<SkuInfoEntity>> skuResp = gmallPmsClient.querySkuBySpuId(spuInfoEntity.getId());
                List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
                if (CollectionUtils.isEmpty(skuInfoEntities)){
                    continue;
                }
                skuInfoEntities.forEach(skuInfoEntity -> {
                    GoodsVO goodsVO = new GoodsVO();
                    goodsVO.setName(skuInfoEntity.getSkuTitle());
                    goodsVO.setId(skuInfoEntity.getSkuId());
                    goodsVO.setPrice(skuInfoEntity.getPrice());
                    goodsVO.setSale(100);
                    goodsVO.setSort(0);
                    Resp<BrandEntity> brandEntityResp = gmallPmsClient.queryBrandBySpuId(skuInfoEntity.getBrandId());
                    BrandEntity brandEntity = brandEntityResp.getData();
                    if (brandEntity != null) {
                        goodsVO.setBrandId(brandEntity.getBrandId());
                        goodsVO.setBrandName(brandEntity.getName());
                    }
                    Resp<CategoryEntity> categoryEntityResp = gmallPmsClient.queryCategoryBySpuId(skuInfoEntity.getCatalogId());
                    CategoryEntity categoryEntity = categoryEntityResp.getData();
                    if (categoryEntity != null) {
                        goodsVO.setProductCategoryId(skuInfoEntity.getCatalogId());
                        goodsVO.setProductCategoryName(categoryEntity.getName());
                    }
//设置搜索属性相关数据
                    Resp<List<SpuAttributeValueVO>> searchAttrValue = gmallPmsClient.querySearchAttrValue(spuInfoEntity.getId());
                    List<SpuAttributeValueVO> searchAttrValueData = searchAttrValue.getData();
                    goodsVO.setAttrValueList(searchAttrValueData);
                    //库存相关
                    Resp<List<WareSkuEntity>> wareResp = gmallWmsClient.queryWareBySkuId(skuInfoEntity.getSkuId());
                    List<WareSkuEntity> wareSkuEntities = wareResp.getData();
                    if (wareSkuEntities.stream().anyMatch(t->t.getStock()>0)){
                        goodsVO.setStock(1L);
                    }else{
                        goodsVO.setStock(0L);
                    }
                    Index build = new Index.Builder(goodsVO).index("goods").type("info").id(skuInfoEntity.getSkuId().toString()).build();

                    try {
                        this.jestClient.execute(build);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }




                });
            }
            pageSize = Long.valueOf(spuInfoEntities.size());
            pageNum++;

        }while (pageSize==100);
    }

    @Test
    void contextLoads() {
    }


}
/*
*  private Long id;  //skuId
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
    //由于是属性表与属性值表进行关联查询，所以在AttrController或者ProductAttrValueController添加查询检索属性的controller方法都可以*/