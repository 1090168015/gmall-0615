package com.atguigu.gmall.auth.service.impl;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.core.exception.GmallException;
import com.atguigu.gmall.core.utils.JwtUtils;
import com.atguigu.gmall.usm.entity.MemberEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@EnableConfigurationProperties({JwtProperties.class})
public class AuthServiceImpl implements AuthService {
    @Autowired
    private GmallUmsClient gmallUmsClient;
    @Autowired
    private JwtProperties jwtProperties;
    @Override
    public String accredit(String username, String password) {
        try {  //1.远程调用用户中心数据接口，查询用户信息
        Resp<MemberEntity> memberEntityResp = gmallUmsClient.queryUser(username, password);
        MemberEntity memberEntity = memberEntityResp.getData();
        //2.判断用户是否存在，不存在直接返回
        if (memberEntity == null) {
            return username;
        }
        //3.存在生成jwt
        Map<String, Object> map = new HashMap<>(); //map           载荷中的数据
        map.put("id",memberEntity.getId());
        map.put("username",memberEntity.getUsername());
            return JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
            throw new GmallException("jwt认证失败");
        }
        //4.把生成对的jwt放入cookie中(在controller里实现)
    }
}
