package com.atguigu.gmall.order.interceptor;

import com.atguigu.gmall.core.bean.UserInfo;
import com.atguigu.gmall.core.utils.CookieUtils;
import com.atguigu.gmall.core.utils.JwtUtils;
import com.atguigu.gmall.order.config.Jwtproperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component//编写拦截器
@EnableConfigurationProperties(Jwtproperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {//继承拦截器适配器
    //如果编辑好拦截器之后直接使用，没有效果，是因为没有配置拦截路径，需要配置拦截路径，这里不适用配置文件的形式，使用配置类的形式
    @Autowired
    Jwtproperties jwtproperties;
   private static final ThreadLocal<UserInfo> THREAD_LOCAL= new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfo userInfo = new UserInfo();//ThreadLocal的载荷信息
        String token = CookieUtils.getCookieValue(request, jwtproperties.getCookieName()); //获取cookie信息（GMALL_TOKEN，UserKey）//获取用户登录token，用于判断用户是否登录，jwt里保存cookie信息，cookie对应cookieName，和cookieVlue
        if (StringUtils.isEmpty(token)){//用户登录token为空，直接将保存有游客信息的userInfo保存到ThreadLocal中
            return  false;
        }
        try {
            //解析gmall_token         userInfoMap存放用户信息       token保存有加密后的用户信息，经过解析，能够得到用户信息，map集合的键是属性名，值是属性对应的值
            Map<String, Object> userInfoMap = JwtUtils.getInfoFromToken(token, jwtproperties.getPublicKey());//token内保存用户信息，以键值对的形式保存
            userInfo.setUserId(Long.valueOf(userInfoMap.get("id").toString()));//生成jwt时，将用户id与用户名保存到了载荷中，生成token，解析后可以直接直接获取用户id值
        } catch (Exception e) {
            e.printStackTrace();
            return  false;
        }
        THREAD_LOCAL.set(userInfo);
        return true;
    }
    public static UserInfo get(){
        return  THREAD_LOCAL.get();//将保存到线程变量里额userInfo对象取出
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        THREAD_LOCAL.remove();
    }
}
/*
因为使用的是tomcat线程池，请求结束不代表线程结束，只是将线程放回线程池，
所以我们要手动结束线程，不然下次请求线程，有可能拿到上次的线程，
获取上次的ThreadLocal。（ThreadLocal是线程变量，获取当前线程的内容，
每个线程都会有ThreadLocal，互不影响）*/

/*
*  //3.存在生成jwt      生成jwt时，将用户id与用户名保存到了载荷中，生成token，解析后可以直接直接获取用户id值
        Map<String, Object> map = new HashMap<>(); //map           载荷中的数据
        map.put("id",memberEntity.getId());
        map.put("username",memberEntity.getUsername());
            return JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
            throw new GmallException("jwt认证失败");
        }
* */