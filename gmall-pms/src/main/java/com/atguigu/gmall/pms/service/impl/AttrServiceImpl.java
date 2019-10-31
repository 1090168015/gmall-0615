package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryByCidTypePage(QueryCondition queryCondition, Long cid, Integer type) {
        // 构建查询条件
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("catelog_id",cid);
        if (type != null) {
            wrapper.eq("value_type",type);
        }
// 构建分页条件
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(queryCondition),
                wrapper
        );


        return new PageVo(page);
    }


    @Autowired
    AttrDao attrDao;
    @Autowired
    AttrAttrgroupRelationDao relationDao;


    @Transactional
    @Override
    public void saveAttrVoAndRelation(AttrVo attrVo) {//AttrVo继承了AttrEntity，添加了Long attrGroupId属性值
        // 新增规格参数
         this.attrDao.insert(attrVo);
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrId(attrVo.getAttrId());
        relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
        relationEntity.setAttrSort(0);
        this.relationDao.insert(relationEntity);

    }
}