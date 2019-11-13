package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration//标记为配置类。项目启动时，自动初始化
public class CartWebMvcConfigurer implements WebMvcConfigurer {
    //配置拦截器拦截路径 ，如果不配置拦截路径，编写拦截器不起作用
    @Autowired
    private LoginInterceptor loginInterceptor;
    public  void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**");
    }

}
