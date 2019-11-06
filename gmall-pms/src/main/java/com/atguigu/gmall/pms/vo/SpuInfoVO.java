package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import lombok.Data;

import java.util.List;
@Data
public class SpuInfoVO extends SpuInfoEntity {//对SpuInfoEntity属性进行扩展

    private List<String> spuImages;

    private List<ProductAttrValueVO> baseAttrs;

    public List<SkuInfoVO> skus;

}
