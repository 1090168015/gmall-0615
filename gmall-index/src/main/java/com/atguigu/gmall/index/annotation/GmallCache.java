package com.atguigu.gmall.index.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD})//注解所用在方法上
@Retention(RetentionPolicy.RUNTIME)//运行时注解
@Documented
public @interface GmallCache {

    /*
    * 缓存前缀
    * */
    @AliasFor("value")
    String prefix() default "cache";
    @AliasFor("prefix")
    String value() default "cache";
    /*
    * 缓存的过期时间，单位是秒
    * */
    long timeout() default 300L;
    /*
    * 为防止缓存雪崩，而设置的过期时间的随机值范围
    * */
    long random() default 300L;


}
