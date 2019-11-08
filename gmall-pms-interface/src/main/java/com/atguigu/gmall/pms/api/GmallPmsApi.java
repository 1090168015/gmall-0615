package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.QueryCondition;
import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;
import com.atguigu.gmall.pms.vo.SpuAttributeValueVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;


public interface GmallPmsApi {//用于远程调用pmscontroller方法

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
