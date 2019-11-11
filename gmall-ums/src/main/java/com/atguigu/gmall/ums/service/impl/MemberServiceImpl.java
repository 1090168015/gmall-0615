package com.atguigu.gmall.ums.service.impl;

import com.atguigu.gmall.ums.service.MemberService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.Query;
import com.atguigu.gmall.core.bean.QueryCondition;

import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.entity.MemberEntity;

import java.util.Date;
import java.util.UUID;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<MemberEntity> wrapper = new QueryWrapper<>();

        switch (type){//选择封装查询条件
            case 1:wrapper.eq("username",data);break;
            case 2:wrapper.eq("mobile",data);break;
            case 3:wrapper.eq("email",data);break;
            default:return false;
        }
        return  this.count(wrapper) ==0;//如果查询返回为0表示数据库中没有，可以用于注册


    }

    @Override
    public void register(MemberEntity memberEntity, String code) {
        //1.校验验证码
        //2.生成盐
        String salt = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        memberEntity.setSalt(salt);
        //3.加盐加密
        memberEntity.setPassword(DigestUtils.md5Hex(memberEntity.getPassword()+salt));
        //4.注册成功
        memberEntity.setStatus(1);
        memberEntity.setLevelId(1L);
        memberEntity.setCreateTime(new Date());
        memberEntity.setIntegration(0);//积分
        memberEntity.setGrowth(0);
        this.save(memberEntity);//保存用户
        //5.删除验证码

    }

    @Override
    public MemberEntity queryUser(String username, String password) {
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("username", username));
        if (memberEntity==null){
            throw new IllegalArgumentException("用户名或密码错误");
        }
        password =  DigestUtils.md5Hex(password+memberEntity.getSalt());//将客户输入的密码，与数据库加的盐进行加密
        if (!StringUtils.equals(password,memberEntity.getPassword())){//数据库里的密码是加盐后的密码，将用户输入的密码重新加盐加密生成新的密码字符串，与数据库中客户的获取的加盐加密的密码进行比较
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return memberEntity;
    }

}