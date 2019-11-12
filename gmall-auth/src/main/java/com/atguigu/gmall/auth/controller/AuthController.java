package com.atguigu.gmall.auth.controller;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.core.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties jwtProperties;
    @RequestMapping("accredit")
    public Resp<Object> accredit(@RequestParam("username")String username,
                                 @RequestParam("password")String password,
                                 HttpServletRequest request,
                                 HttpServletResponse response){
        String jwtToken =  this.authService.accredit(username,password);//认证登录授权，授权中心方法
        if(StringUtils.isEmpty(jwtToken)){
            return  Resp.fail("xxxxx");
        }
        //4.把生成对的jwt放入cookie中(在controller里实现)
        CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),jwtToken,
                this.jwtProperties.getExpire()*60);
        return Resp.ok(null);
    }
}

