package com.atguigu.gmall.ums.dao;

import com.atguigu.gmall.usm.entity.MemberLoginLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员登录记录
 * 
 * @author sx
 * @email sx@atguigu.com
 * @date 2019-10-28 20:21:33
 */
@Mapper
public interface MemberLoginLogDao extends BaseMapper<MemberLoginLogEntity> {
	
}
