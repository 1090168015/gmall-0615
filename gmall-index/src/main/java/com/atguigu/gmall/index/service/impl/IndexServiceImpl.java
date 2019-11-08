package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.index.fengin.GmallPmsClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {
    
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    JedisPool jedisPool;

    @Autowired
    RedissonClient redissonClient;
    
    private static final String KEY_PREFIX ="index:category";
    
    @Override//查询一级分类
    public List<CategoryEntity> queryLevel1Category() {
        Resp<List<CategoryEntity>> resp = gmallPmsClient.queryCategories(1, 0L);
        return resp.getData();
    }

    @Override//根据父分类id查询所有子分类（根据一级分类id查询所有自分类）
    public List<CategoryVO> queryCategoryVO(Long pid) {
        //1.查询缓存，缓存中有点化直接返回
        String cache = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(cache)){
            return JSON.parseArray(cache,CategoryVO.class);
        }
        //2.若果缓存中没有，查询数据库
        Resp<List<CategoryVO>> listResp = gmallPmsClient.queryCateGoryWithSub(pid);
        List<CategoryVO> categoryVOS = listResp.getData();
        //3.查询完成之后，放入缓存
        this.stringRedisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(categoryVOS));
        //响应数据
        return categoryVOS;
    }

    /*
    * Boolean setIfAbsent(K key, V value, long timeout, TimeUnit unit);
    * 参数1：锁对应的键
    * 参数2：锁对应的值
    * 参数3：锁过期时间
    * 参数4：时间单位
    * */


    public String testLock2(){
     //   Boolean lock = this.stringRedisTemplate.opsForValue().setIfAbsent("lock", "111");
        //所有请求竞争锁
        String uuid = UUID.randomUUID().toString();
        //每个线程请求获取一个唯一的子字符串，代表唯一的锁，用于后面删除比较删除，只能删除自己的锁
        Boolean lock = this.stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 10, TimeUnit.SECONDS);//为防止死锁设置过期时间
        //获取到锁执行业务逻辑
        if (lock){
            String numString = this.stringRedisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(numString)){
                return null;
            }
            int num = Integer.parseInt(numString);
            this.stringRedisTemplate.opsForValue().set("num",String.valueOf(++num));
            //释放锁
            Jedis jedis =null;
            try {
                jedis = this.jedisPool.getResource();
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                jedis.eval(script,Arrays.asList("lock"),Arrays.asList(uuid));
            } finally {
                jedis.close();//注意释放jedis资源
            }
         }else {
            try {
                //没有获取到锁的请求进行重试
                TimeUnit.SECONDS.sleep(1);//在没有获取锁的情况下，请求重试，调取原方法
                testLock();//请求重试就是重新调取所要请求的方法
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "新增成功";
    }
    public String testLock1(){
        //所有请求竞争锁
        String uuid = UUID.randomUUID().toString();//每个线程请求获取一个唯一的子字符串，代表唯一的锁，用于后面删除比较删除，只能删除自己的锁
        //为防止死锁设置过期时间
        Boolean lock = this.stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid,10,TimeUnit.SECONDS);
        //获取到锁执行业务逻辑
        if (lock){
            String numString = this.stringRedisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(numString)){
                return null;
            }
            int num = Integer.parseInt(numString);
            this.stringRedisTemplate.opsForValue().set("num",String.valueOf(++num));
            //释放锁

            //this.stringRedisTemplate.execute(new DefaultRedisScript<>(script), Arrays.asList("lock"),uuid);
            if (StringUtils.equals(uuid,this.stringRedisTemplate.opsForValue().get("lock"))){
                this.stringRedisTemplate.delete("lock");
            }
        }else {
            //没有获取到锁的请求进行重试
            try {
                TimeUnit.SECONDS.sleep(1);//一秒后重试，重新请求获取锁
                testLock();//请求重试就是重新调取所要请求的方法
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "新增成功";
    }


    public String testLock3(){//使用Redisson实现分布式锁
        RLock lock = this.redissonClient.getLock("lock");
        lock.lock();
        String numString = this.stringRedisTemplate.opsForValue().get("num");
        if (StringUtils.isBlank(numString)){
            return  null;
        }

        int num = Integer.parseInt(numString);
        this.stringRedisTemplate.opsForValue().set("num",String.valueOf(++num));

        lock.unlock();
        return  "已经增加成功";
    }

    public String testLock(){
        RLock lock = this.redissonClient.getLock("lock");
        lock.lock();
        String numString = this.stringRedisTemplate.opsForValue().get("num");
        if (StringUtils.isBlank(numString)){
            return  null;
        }
        int num = Integer.parseInt(numString);
        this.stringRedisTemplate.opsForValue().set("num",String.valueOf(++num));
        lock.unlock();
        return  "已经增加成功";
    }

    @Override
    public String testRead() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readWriteLock");
        readWriteLock.readLock().lock(10L,TimeUnit.SECONDS);
        String msg = this.stringRedisTemplate.opsForValue().get("msg");
        return msg;
    }

    @Override
    public String testWrite() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readWriteLock");
        readWriteLock.writeLock().lock(10L,TimeUnit.SECONDS);
        String msg = UUID.randomUUID().toString();
        this.stringRedisTemplate.opsForValue().set("msg",msg);
        return "数据写入成功---"+msg;
    }

}

