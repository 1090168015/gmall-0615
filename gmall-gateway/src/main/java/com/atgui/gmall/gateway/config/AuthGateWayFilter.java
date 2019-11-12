package com.atgui.gmall.gateway.config;

import com.atguigu.gmall.core.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@EnableConfigurationProperties({JwtProperties.class})
public class AuthGateWayFilter implements GatewayFilter, Ordered {
    //用于过滤请求，验证客户是否登录，授权情况，如果没有登录转至登录页面，如果登录了，执行客户请求逻辑
    @Autowired
    private  JwtProperties properties;//
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //获取cookie中token信息
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();//获取请求时发过来的cookie数据
        //判断是否存在，不存在响应相关信息（也可以重定向登陆页面，但是这里重定向由于jar包不支持重定向，所以响应数据）
        if(cookies==null||!cookies.containsKey(properties.getCookieName())){//判断获取的cookies是否为空，且获取的cookies里是否有需要的键
            response.setStatusCode(HttpStatus.UNAUTHORIZED);//如果为空或者没有需要的cookie，响应状态码为认证，结束请求
            return response.setComplete();//Complete完整的，彻底的
        }
        //存在，解析cookies数据
        HttpCookie cookie = cookies.getFirst(properties.getCookieName());
        try {
        /*String value = cookie.getValue();//获取对象cookName对应的cookie的value值
        PublicKey publicKey = properties.getPublicKey();//获取公钥，用于解析私钥加签后生成的密文*/
            //获取token中的用户信息
            JwtUtils.getInfoFromToken(cookie.getValue(),properties.getPublicKey());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        return chain.filter(exchange);//过滤器放行
    }

    @Override
    public int getOrder(){
        return 0;
    }
}
