package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.QueryCondition;
import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.CategoryVO;
import com.atguigu.gmall.pms.vo.GroupVO;
import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;


public interface GmallPmsApi {//用于远程调用pmscontroller方法
    @GetMapping("pms/skusaleattrvalue/sku/{skuId}")//根据sku查询销售属性
    public Resp<List<SkuSaleAttrValueEntity>> querSaleAttrBySkuId(@PathVariable("skuId")Long skuId);
    @GetMapping("pms/attrgroup/item/group/{cid}/{spuId}")
    public Resp<List<GroupVO>> queryGroupVOByCid(@PathVariable("cid") Long cid, @PathVariable("spuId") Long spuId);
    @GetMapping("pms/spuinfodesc/info/{spuId}")//根据spuId查询商品描述信息
    public Resp<SpuInfoDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);
    @GetMapping("pms/skusaleattrvalue/{spuId}")//根据spuId查询SkuId，再根据skuId查询销售属性
    public Resp<List<SkuSaleAttrValueEntity>> querSaleAttrValues(@PathVariable("spuId")Long spuId);
    @GetMapping("pms/skuimages/{skuId}")//根据skuId获取sku图片地址
    public Resp<List<String>> queryPicsBySkuId(@PathVariable("skuId") Long skuId);
    @GetMapping("pms/spuinfo/info/{id}")//根据spuId查询spu信息
    public Resp<SpuInfoEntity> querySpuById(@PathVariable("id") Long id);
    @GetMapping("pms/skuinfo/info/{skuId}")//根据skuId查询sku信息
    public Resp<SkuInfoEntity> querySkuById(@PathVariable("skuId") Long skuId);


    @PostMapping("pms/spuinfo/list")
    public Resp<List<SpuInfoEntity>> querySpuPage(@RequestBody QueryCondition queryCondition);
    /*@GetMapping("pms/spuinfo/list")
    public Resp<PageVo> list(QueryCondition queryCondition);*/

    @GetMapping("pms/skuinfo/{spuId}")//库存管理->商品库存->库存维护
    public Resp<List<SkuInfoEntity>> querySkuBySpuId(@PathVariable("spuId") Long  spuId);

    @GetMapping("pms/brand/info/{brandId}")
    public Resp<BrandEntity> queryBrandBySpuId(@PathVariable("brandId") Long brandId);//由于是根据路径调用，所以可以将方法名改掉

    @ApiOperation("详情查询")
    @GetMapping("pms/category/info/{catId}")
    public Resp<CategoryEntity> queryCategoryBySpuId(@PathVariable("catId") Long catId);

    @GetMapping("pms/productattrvalue/{spuId}")//根据spuId获取ProductAttrValueVO对象对应pms_product_attr_value，用于gmall-search工程远程调用分词
    public Resp<List<SpuAttributeValueVO>> querySearchAttrValue(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/category")                      //分类所在的级别为0查询所有商品分类,用作Index门户工程三级分类
    public Resp<List<CategoryEntity>> queryCategories(@RequestParam(value = "level",defaultValue = "0")Integer level, @RequestParam(value = "parentCid" ,required = false)Long parentCid);

    @GetMapping("pms/category/{pid}")//根据一级分类id查询二级分类及三级分类
    public Resp<List<CategoryVO>> queryCateGoryWithSub(@PathVariable("pid") Long pid);

}
