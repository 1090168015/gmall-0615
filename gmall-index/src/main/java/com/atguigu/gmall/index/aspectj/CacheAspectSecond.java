package com.atguigu.gmall.index.aspectj;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annotation.GmallCache;
import com.fasterxml.jackson.core.sym.NameN;
import net.bytebuddy.asm.Advice;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class CacheAspectSecond {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;

    /* 1.方法的返回值是Object
     * 2.方法的参数ProceedingJoinPoint
     * 3.方法必须抛出Throwable异常
     * 4.通过joinPoint.proceed(args)执行原始方法*/

    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint) throws  Throwable{
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        GmallCache annotation = signature.getMethod().getAnnotation(GmallCache.class);
        Class returnType = signature.getReturnType();
        String prefix = annotation.prefix();
        List<String> args = Arrays.asList(joinPoint.getArgs().toString());
        String key =prefix+":"+args;

        Object result = this.cacheHit(key, returnType);
        if (result != null) {
            return  result;
        }
        RLock lock = this.redissonClient.getLock("lock" + args);
        lock.lock();
        result = this.cacheHit(key, returnType);
        if (result != null) {
            lock.unlock();
            return  result;
        }
        result = joinPoint.proceed(joinPoint.getArgs());

        long timeout = annotation.timeout();
        timeout= (long) (timeout+Math.random()+annotation.random());
        this.redisTemplate.opsForValue().set(key,JSON.toJSONString(result),timeout, TimeUnit.SECONDS);
        lock.unlock();
        return  result;



    }

    public Object cacheHit(String key,Class returnType){
        String jsonString = this.redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(jsonString)){
            return JSON.parseObject(jsonString);
        }
        return  null;
    }

}
