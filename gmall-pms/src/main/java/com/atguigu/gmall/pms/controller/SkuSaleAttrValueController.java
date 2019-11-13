package com.atguigu.gmall.pms.controller;

import java.util.Arrays;
import java.util.List;


import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuSaleAttrValueService;
import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.QueryCondition;
import com.atguigu.gmall.core.bean.Resp;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * sku销售属性&值
 *
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:04:41
 */
@Api(tags = "sku销售属性&值 管理")
@RestController
@RequestMapping("pms/skusaleattrvalue")
public class SkuSaleAttrValueController {
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @GetMapping("sku/{skuId}")//根据sku查询销售属性
    public Resp<List<SkuSaleAttrValueEntity>> querSaleAttrBySkuId(@PathVariable("skuId")Long skuId){

        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities= this.skuSaleAttrValueService.list(new QueryWrapper<SkuSaleAttrValueEntity>().eq("sku_id",skuId));
                ;
        return Resp.ok(skuSaleAttrValueEntities) ;
    }

    @GetMapping("{spuId}")//根据spuId查询Sku，在根据sku查询销售属性
    public Resp<List<SkuSaleAttrValueEntity>> querSaleAttrValues(@PathVariable("spuId")Long spuId){

        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities= this.skuSaleAttrValueService.querySaleValues(spuId);
        return Resp.ok(skuSaleAttrValueEntities) ;
    }

    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:skusaleattrvalue:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = skuSaleAttrValueService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{id}")
    @PreAuthorize("hasAuthority('pms:skusaleattrvalue:info')")
    public Resp<SkuSaleAttrValueEntity> info(@PathVariable("id") Long id){
		SkuSaleAttrValueEntity skuSaleAttrValue = skuSaleAttrValueService.getById(id);

        return Resp.ok(skuSaleAttrValue);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:skusaleattrvalue:save')")
    public Resp<Object> save(@RequestBody SkuSaleAttrValueEntity skuSaleAttrValue){
		skuSaleAttrValueService.save(skuSaleAttrValue);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:skusaleattrvalue:update')")
    public Resp<Object> update(@RequestBody SkuSaleAttrValueEntity skuSaleAttrValue){
		skuSaleAttrValueService.updateById(skuSaleAttrValue);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:skusaleattrvalue:delete')")
    public Resp<Object> delete(@RequestBody Long[] ids){
		skuSaleAttrValueService.removeByIds(Arrays.asList(ids));

        return Resp.ok(null);
    }

}
