package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.Query;
import com.atguigu.gmall.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.vo.AttrGroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")//AttrGroupServiceImpl继承ServiceImpl并实现AttrGroupService，ServiceImpl实现了IService，所以this.page是调取的父类封装实现的方法，做分页
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {


    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    AttrDao attrDao;



    /*public class ServiceImpl<M extends BaseMapper<T>, T> implements IService<T> {
           public IPage<T> page(IPage<T> page, Wrapper<T> queryWrapper) {

                return this.baseMapper.selectPage(page, queryWrapper);
AttrGroupServiceImpl继承ServiceImpl，ServiceImpl有方法page，返回IPage<T>
        */
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(        //page方法里传两个参数，第一个参数为IPage<T> page，Query对象调取getPage(params)可以获取Page对象
                new Query<AttrGroupEntity>().getPage(params),//分页条件
                new QueryWrapper<AttrGroupEntity>()         //查询条件
        );

        return new PageVo(page);
    }
 /*
------------------------------
        public class Query<T> {
            public IPage<T> getPage(QueryCondition params) {
                return this.getPage(params, null, false);
            }
    }
 *
 * */
    @Override
    public PageVo queryByCatIdPage(Long catId, QueryCondition queryCondition) {
        QueryWrapper<AttrGroupEntity> wapper = new QueryWrapper<>();
        if (catId != null) {
            wapper.eq("catelog_id",catId);
        }

        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(queryCondition),
                wapper);



        return new PageVo(page);
    }






    @Override
    public AttrGroupVO queryById(Long gid) {
        // 查询分组
        AttrGroupVO attrGroupVO = new AttrGroupVO();
        AttrGroupEntity attrGroupEntity = this.getById(gid);
        BeanUtils.copyProperties(attrGroupEntity,attrGroupVO);

        List<AttrAttrgroupRelationEntity> reationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", gid));
        if (CollectionUtils.isEmpty(reationEntities)){// 判断关联关系是否为空，如果为空，直接返回
            return attrGroupVO;
        }
        //將reationEntities设置到AttrGroupVO中供controller层获取，响应
        attrGroupVO.setRelations(reationEntities);
        // 收集分组下的所有规格id
        List<Long> attrids = reationEntities.stream().map(relation -> relation.getAttrId()).collect(Collectors.toList());

        // 查询分组下的所有规格参数
        List<AttrEntity> attrEntities = this.attrDao.selectBatchIds(attrids);
        attrGroupVO.setAttrEntities(attrEntities);


        return attrGroupVO;
    }

    @Override
    public List<AttrGroupVO> queryGroupWithAttrsByCatId(Long catId) {
        //根据分类分类id《catelog_id》查询分类下的所有组
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catId));
        //查询每个组下的所有数据
        List<AttrGroupVO> attrGroupVOS = attrGroupEntities.stream().map(attrGroupEntity -> {
            return this.queryById(attrGroupEntity.getAttrGroupId());
        }).collect(Collectors.toList());


        return attrGroupVOS;
    }

}