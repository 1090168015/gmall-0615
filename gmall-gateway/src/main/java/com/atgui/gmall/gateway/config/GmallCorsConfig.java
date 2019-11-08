package com.atgui.gmall.gateway.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GmallCorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter(){
        // 初始化CORS配置对象
        CorsConfiguration config = new CorsConfiguration();//跨域请对象
        // 允许的域,不要写*，否则cookie就无法使用了
        config.addAllowedOrigin("http://localhost:1000");//允许那些域名跨域请求
        config.addAllowedOrigin("http://localhost:2000");
        // 允许的头信息
        config.addAllowedHeader("*");//允许跨域请求携带的头信息
        // 允许的请求方式
        config.addAllowedMethod("*");//允许跨域请求携带的方法
        // 是否允许携带Cookie信息
        config.setAllowCredentials(true);
        // 添加映射路径，我们拦截一切请求
        //cors配置源信息
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**",config);
        return new CorsWebFilter(corsConfigurationSource);

    }
}
