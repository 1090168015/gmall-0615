package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.vo.SkuLockVO;
import com.atguigu.gmall.wms.dao.WareSkuDao;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WareListener {

    @Autowired
    private WareSkuDao wareSkuDao;
    @Autowired
    StringRedisTemplate redisTemplate;

    @RabbitListener(queues = "WMS-DEAD-QUEUE")
    public void unlock(String orderToken){
        //获取锁锁定商品的库存
        String stockJson = this.redisTemplate.opsForValue().get("order:stock:" + orderToken);
        //反序列化
        List<SkuLockVO> skuLockVOS = JSON.parseArray(stockJson, SkuLockVO.class);
        //遍历解锁库存
        skuLockVOS.forEach(skuLockVO -> {
            wareSkuDao.unlock(skuLockVO.getSkuWareId(),skuLockVO.getCount());
        });


    }


}
