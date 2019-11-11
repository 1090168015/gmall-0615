package com.atguigu.gmall.index.aspectj;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annotation.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
@Component//将切面类注入到spring容器
@Aspect//声明这个类是切面类，切面类中定义前置通知，后置通知，环绕通知等通知，通知的注解方法里定义的方法就是通知需要作用点方法
public class CacheAspect {
/*
* 1.方法的返回值是Object
* 2.方法的参数是ProceedingJoinPoint，代表连接点，目标对象中的方法，课代表目标方法的对象，这里的目标方法是
* 3.方法必须抛出Throwable异常
* 4.通过joinPoint.proceed(args)执行原始方法
*
* 环绕通知 ProceedingJoinPoint 执行proceed方法的作用是让目标方法执行，这也是环绕通知和前置、后置通知方法的一个最大区别。
    简单理解，环绕通知=前置+目标方法执行+后置通知，proceed方法就是用于启动目标方法执行的.
* */
    @Autowired
    private  StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(com.atguigu.gmall.index.annotation.GmallCache)")//这个环绕通知作用在标注有GmallCache注解的方法上
    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {

        //获取方法签名    // 获取连接点方法签名对象  <所谓连接点是指那些可以被拦截到的点（目标对象中的方法）。在spring中，这些点指的是方法，因为spring只支持方法类型的连接点。>
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取方法签名对象
        //获取注解对象,//通过方法签名对象获取标注在方法上的注解对象，  // 获取连接点的GmallCache注解信息
        GmallCache annotation = signature.getMethod().getAnnotation(GmallCache.class);
        String prefix = annotation.prefix();//获取缓存前缀，GmallCache注解定义的缓存前缀，即（String prefix）
        Class returnType = signature.getReturnType();//获取连接点方法的返回值类型
        String args = Arrays.asList(joinPoint.getArgs()).toString();//《个人理解获取连接点参数》获取方法的参数，ProceedingJoinPoint是连接点对象，就是方法对象
        //方法的参数（queryCategoryVO(Long pid)）可以和前缀组成缓存的唯一标志，只要参数一样，说明就会从缓存中命中
        //查询缓存
        String key = prefix + ":" +args;//将连接点参数与定义GmallCache注解前缀拼接，组装成保存到redis中key，
       /* String jsonString = this.redisTemplate.opsForValue().get(key);//从redis中查询的数据，返回的是json字符串，需要反序列化为响应的对象
        //如果缓存中有，直接返回
        if (StringUtils.isNotBlank(jsonString)){
            JSON.parseObject(jsonString,returnType);
        }*/
        //查询缓存

        Object result = this.cacheHit(key, returnType);
        //如果缓存中有，直接返回，
        if (result != null) {
//            System.out.println("-------------------------------");
            return  result;
        }
//        System.out.println("++++++++++++++++++++++++");

        //初始化分布式锁
        // 相同pid的共用同一把锁，即使这个pid的数据缓存中没有不妨碍别的数据的执行，只锁当前资源，相同方法参数的资源共用同一把锁
        // 如果只用lock作为锁，会将所有的数据都锁住，所有的数据共用同一把锁
        RLock lock = this.redissonClient.getLock("lock" + args);
        lock.lock();// 防止缓存穿透 加锁
        //查询缓存  // 再次检查内存是否有，因为高并发下，可能在加锁这段时间内，已有其他线程放入缓存
      result = this.cacheHit(key, returnType);
        //如果缓存中有，直接返回，并且释放分布式锁
        if (result != null) {//判断查询缓存返回对象是否为空
            lock.unlock();
            return  result;
        }
        //放入缓存，释放分布式锁
        result = joinPoint.proceed(joinPoint.getArgs());//拿到目标方法的参数，代替方法的执行
        long timeout = annotation.timeout();
        timeout = (long) (timeout+Math.random()*annotation.random());
        this.redisTemplate.opsForValue().set(key,JSON.toJSONString(result),timeout, TimeUnit.SECONDS);
        lock.unlock();
        return result;
    }

    public Object cacheHit(String key,Class returnType){
        String jsonString = this.redisTemplate.opsForValue().get(key);
        //如果缓存中有，直接返回
        if (StringUtils.isNotBlank(jsonString)){
           return JSON.parseObject(jsonString,returnType);//将在redis中根据key查询到的数据转化为对应的对象
        }
        return  null;

    }

}
/*
* 11.3.5.	环绕通知（@Around）
环绕通知，目标执行前后，都进行增强，甚至可以控制目标方法执行。环绕通知优先于其他通知先执行。
应用场景：日志、缓存、权限、性能监控、事务管理
环绕通知必须满足以下条件：
	接受的参数：ProceedingJoinPoint（可执行的连接点）
	需要通过proceed = joinPoint.proceed(joinPoint.getArgs());执行目标方法，否则目标方法不执行
	返回值：Object返回值，否则返回通知无法执行
	抛出Throwable异常。否则其他通知无法捕获异常
*/