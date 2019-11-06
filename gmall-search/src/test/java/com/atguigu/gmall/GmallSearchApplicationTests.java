package com.atguigu.gmall;

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
            QueryCondition queryCondition = new QueryCondition();;//分页条件对象
            queryCondition.setPage(pageNum);//给分页条件设置值
            queryCondition.setLimit(pageSize);
            Resp<List<SpuInfoEntity>> listResp = gmallPmsClient.querySpuPage(queryCondition);//根据分页条件获取分页对象
            List<SpuInfoEntity> spuInfoEntities = listResp.getData();//根据分页对象获取分页数据

//遍历spuInfoEntities获取sku数据导入ES索引库中
            for (SpuInfoEntity spuInfoEntity : spuInfoEntities) {
                Resp<List<SkuInfoEntity>> skuResp = gmallPmsClient.querySkuBySpuId(spuInfoEntity.getId());
                List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
                if (CollectionUtils.isEmpty(skuInfoEntities)){
                    continue;
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
                    //设置分类相关
                    Resp<CategoryEntity> categoryEntityResp = gmallPmsClient.queryCategoryBySpuId(skuInfoEntity.getCatalogId());
                    CategoryEntity categoryEntity = categoryEntityResp.getData();
                    if (categoryEntity != null) {
                        goodsVO.setProductCategoryId(skuInfoEntity.getCatalogId());
                        goodsVO.setProductCategoryName(categoryEntity.getName());
                    }
                    //设置搜索属性
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
    void contextLoads() {
    }

}
