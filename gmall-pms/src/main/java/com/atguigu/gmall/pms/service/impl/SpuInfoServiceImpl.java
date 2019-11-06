package com.atguigu.gmall.pms.service.impl;

import VO.SaleVO;
import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.Query;
import com.atguigu.gmall.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.*;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.service.SpuInfoDescService;
import com.atguigu.gmall.pms.vo.ProductAttrValueVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;



import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gmall.pms.service.SpuInfoService;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    SpuInfoDescDao spuInfoDescDao;
   /* @Autowired
    ProductAttrValueService productAttrValueService;*/
   @Autowired
   ProductAttrValueDao productAttrValueDao;
   @Autowired
   SkuInfoDao skuInfoDao;
   @Autowired
   private SkuImagesDao skuImagesDao;
   @Autowired
   private SkuSaleAttrValueDao skuSaleAttrValueDao;

   @Autowired
   private GmallSmsClient gmallSmsClient;
    @Autowired
    SpuInfoDescService spuInfoDescService;



    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuInfoByKeyPage(Long catId, QueryCondition queryCondition) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        if (catId != 0) {//判断catId是否为0

            wrapper.eq("catalog_id", catId);

        }

        String key = queryCondition.getKey();//key可以是id或者spu_name，用户可以输入的值
        if (StringUtils.isNotBlank(key)) {//判断key是否为空
            wrapper.and(t -> t.eq("id", key).or().like("spu_name", key));//根据spu_name或者id值查询


        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(queryCondition),
                wrapper
        );

        return new PageVo(page);
    }

    /*九张表:
    SPUX相关：3张表      pms_product_attr_value,pms_spu_info,pms_spu_info_desc
    sku相关：3张表        pms_sku_info,pms_sku_images,
    营销相关：3张表*/
    @GlobalTransactional
    @Override   //注：主键类型在nacos配置文件里设置的自增模式，所示数据库表会自己增加，我们在程序里无法set，所以要在实体类中主键属性添加@TableId(type = IdType.INPUT)
    public void bigSave(SpuInfoVO spuInfoVO) {//SpuInfoVO已经将传进的参数接收，保存到了对象中
        // 1.保存spu相关3张表
        Long spuId = saveSpuInfo(spuInfoVO);
        // 1.2. 保存spu的描述信息 spu_info_desc
        this.spuInfoDescService.saveSpuDesc(spuInfoVO, spuId);
       // int i =1/0;
        // 1.3. 保存spu的规格参数信息productAttrValue：#### baseAttrs  //对应的表是pms_product_attr_value，对应的实体类是ProductAttrValueEntity
        saveBaseAttr(spuInfoVO, spuId);
        /// 2. 保存sku相关信息相关3张表，新增sku必须要有spu，所以sku与spu顺序不能变
        saveSku(spuInfoVO, spuId);
     //   int i =1/0;
    }

    private void saveSku(SpuInfoVO spuInfoVO, Long spuId) {
        List<SkuInfoVO> skuInfoVOS = spuInfoVO.getSkus();//获取sku信息
        if (CollectionUtils.isEmpty(skuInfoVOS)){//如果为空，直接返回,不用执行以下代码
            return;
        }
        // 2.1. 保存sku基本信息skuInfo
        skuInfoVOS.forEach(skuInfoVO -> {
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(skuInfoVO,skuInfoEntity);
            // 品牌和分类的id需要从spuInfo中获取
            skuInfoEntity.setBrandId(spuInfoVO.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoVO.getCatalogId());
            // 获取随机的uuid作为sku的编码
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString().substring(0,10));
            skuInfoEntity.setSpuId(spuId);
            // 获取图片列表
            List<String> images = skuInfoVO.getImages();
            // 如果图片列表不为null，则设置默认图片
            if (!CollectionUtils.isEmpty(images)){
                // 设置第一张图片作为默认图片
                skuInfoEntity.setSkuDefaultImg(StringUtils.isNotBlank(skuInfoEntity.getSkuDefaultImg()) ? skuInfoEntity.getSkuDefaultImg():images.get(0));
            }
            skuInfoDao.insert(skuInfoEntity);

            Long skuId = skuInfoEntity.getSkuId();
            // 2.2. 保存sku图片信息
            if (!CollectionUtils.isEmpty(images)){
                images.forEach(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setDefaultImg(StringUtils.equals(image,skuInfoEntity.getSkuDefaultImg())?1:0);
                    skuImagesEntity.setImgSort(0);
                    skuImagesEntity.setImgSort(0);
                    skuImagesEntity.setImgUrl(image);
                   this.skuImagesDao.insert(skuImagesEntity);
                });
            }
            // 2.3. 保存sku的规格参数（销售属性）
            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVO.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(saleAttr ->{
                    saleAttr.setSkuId(skuId);
                    saleAttr.setAttrSort(0);
                    this.skuSaleAttrValueDao.insert(saleAttr);
                });
            }
            // 3. 保存营销相关信息，需要远程调用gmall-sms相关3张表，新增营销相关必须要有sku信息，所以顺序也不能改变
            // 3.1. 积分优惠，新增积分，skuBounds
            // 3.2. 数量折扣，新增打折信息skuLadder
            // 3.3. 满减优惠，新增满减信息skuReduction
            SaleVO saleVO = new SaleVO();
            BeanUtils.copyProperties(skuInfoVO,saleVO);
            saleVO.setSkuId(skuId);
            gmallSmsClient.saveSale(saleVO);
        });
    }

    private void saveBaseAttr(SpuInfoVO spuInfoVO, Long spuId) {
        List<ProductAttrValueVO> baseAttrs = spuInfoVO.getBaseAttrs();//将接收的参数取出
        /*spuInfoDescEntity.setSpuId(spuId);*/
        System.out.println(baseAttrs);
        baseAttrs.forEach(baseAttr ->{
            baseAttr.setSpuId(spuId);
            this.productAttrValueDao.insert(baseAttr);
        });

      /*  if (!CollectionUtils.isEmpty(bassAttrs)){
            List<ProductAttrValueEntity> productAttrValueEntities = bassAttrs.stream().map(productAttrValueVO -> {
                productAttrValueVO.setSpuId(spuId);
                productAttrValueVO.setAttrSort(0);
                productAttrValueVO.setQuickShow(0);
                return productAttrValueVO;
            }).collect(Collectors.toList());
            this.productAttrValueService.saveBatch(productAttrValueEntities);
        }*/
    }


    private Long saveSpuInfo(SpuInfoVO spuInfoVO) {
        // 1.1. 保存spu基本信息 spu_info
        //SpuInfoVO继承SpuInfoEntity，保存方法可以直接用
        spuInfoVO.setCreateTime(new Date());// 新增时，更新时间和创建时间一致
        spuInfoVO.setUodateTime(spuInfoVO.getCreateTime());
        spuInfoVO .setPublishStatus(1);
        this.save(spuInfoVO);
        return spuInfoVO.getId();
    }

}