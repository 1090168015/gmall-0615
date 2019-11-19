package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.vo.SkuLockVO;
import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
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
        if (StringUtils.isEmpty(stockJson)){
            return;
        }
        //反序列化
        List<SkuLockVO> skuLockVOS = JSON.parseArray(stockJson, SkuLockVO.class);
        //遍历解锁库存
        skuLockVOS.forEach(skuLockVO -> {
            wareSkuDao.unlock(skuLockVO.getSkuWareId(),skuLockVO.getCount());
        });
        this.redisTemplate.delete("order:stock:"+orderToken);//将保存在redis中的需要解锁的商品信息删除
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "WMS-STOCK-QUEUE",durable = "true"),
            exchange = @Exchange(value ="WMS-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"stock.minus"}
    ))
    public void minusStock(String orderToken){//减库存
        String stockJson = this.redisTemplate.opsForValue().get("order:stock:" + orderToken);
        if (StringUtils.isEmpty(stockJson)){
            return;
        }
        //反序列化
        List<SkuLockVO> skuLockVOS = JSON.parseArray(stockJson, SkuLockVO.class);

        //遍历解锁库存
        skuLockVOS.forEach(skuLockVO -> {
            wareSkuDao.minus(skuLockVO.getSkuWareId(),skuLockVO.getCount());
        });
        this.redisTemplate.delete("order:stock:"+orderToken);

    }


}
