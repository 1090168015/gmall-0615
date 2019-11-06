package com.atguigu.gmall.pms.service.impl;



import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.Query;
import com.atguigu.gmall.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.CategoryDao;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gmall.pms.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategories(Integer level, Long parentCid) {
        QueryWrapper<CategoryEntity> wapper = new QueryWrapper<>();
        if (level != 0){// //分类所在的级别为0查询所有商品分类
            wapper.eq("cat_level",level);

        }
        if (parentCid != null) {//判断父节点id是否为null
            wapper.eq("parent_cid",parentCid);
        }

        return this.list(wapper);
    }

}