package com.atguigu.gmall.pms.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.service.SkuImagesService;
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
 * sku图片
 *
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:04:41
 */
@Api(tags = "sku图片 管理")
@RestController
@RequestMapping("pms/skuimages")
public class SkuImagesController {
    @Autowired
    private SkuImagesService skuImagesService;

    @GetMapping("{skuId}")//根据skuId获取sku图片地址
    public Resp<List<String>> queryPicsBySkuId(@PathVariable("skuId") Long skuId){
        List<SkuImagesEntity> skuImagesEntities = this.skuImagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
        List<String> stringList = skuImagesEntities.stream().map(skuImagesEntity -> skuImagesEntity.getImgUrl()).collect(Collectors.toList());
        return Resp.ok(stringList);


    }

    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:skuimages:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = skuImagesService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{id}")
    @PreAuthorize("hasAuthority('pms:skuimages:info')")
    public Resp<SkuImagesEntity> info(@PathVariable("id") Long id){
		SkuImagesEntity skuImages = skuImagesService.getById(id);

        return Resp.ok(skuImages);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:skuimages:save')")
    public Resp<Object> save(@RequestBody SkuImagesEntity skuImages){
		skuImagesService.save(skuImages);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:skuimages:update')")
    public Resp<Object> update(@RequestBody SkuImagesEntity skuImages){
		skuImagesService.updateById(skuImages);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:skuimages:delete')")
    public Resp<Object> delete(@RequestBody Long[] ids){
		skuImagesService.removeByIds(Arrays.asList(ids));

        return Resp.ok(null);
    }

}
