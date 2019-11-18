package com.atguigu.gmall.cart.listener;


import com.atguigu.gmall.cart.fegin.GmallPmsClient;
import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CartListener {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GmallPmsClient gmallPmsClient;

    private static final String  KEY_PREFIX="cart:key:";//购物车保存到redis中的前缀

    private static final String CURRENT_PRICE_PRFIX="cart:price:";
    //MQ消息监听,用于修改商品修改时获取获取MQ发送修改商品的Id，然后根据id查询具体的商品，
    // 将数据库中商品的最新价格设置到redis缓存中，用于在购物车显示时，购物车根据最新商品价格的redis键获取最新价格的redis值
    @RabbitListener(bindings = @QueueBinding(//队列绑定
            value =@Queue(value = "GMALL-CART-QUEUE",durable = "true"),//自定义消息队列名
            exchange = @Exchange(value = "GMALL-ITEM-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),//交换机类型
            key = {"item.update"}
    ))
    public void listener(Map<String,Object> map){//监听pms工程里的SpuInfoServiceImpl方法bigSave发出的MQ消息
        Long spuId = (Long) map.get("id");
        Resp<List<SkuInfoEntity>> listResp = this.gmallPmsClient.querySkuBySpuId(spuId);
        List<SkuInfoEntity> skuInfoEntities = listResp.getData();
        skuInfoEntities.forEach(skuInfoEntity -> {
            this.redisTemplate.opsForValue().set(CURRENT_PRICE_PRFIX+skuInfoEntity.getSkuId(),skuInfoEntity.getPrice().toString());
        });


    }

    //监听gmall-order工程OrderService里的submit方法
    @RabbitListener(bindings = @QueueBinding(//监听订单提交成功，删除购物车
            value =@Queue(value = "GMALL-CART-DELETE-QUEUE",durable = "true"),//自定义消息队列名
            exchange = @Exchange(value = "GMALL-ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),//交换机类型
            key = {"cart.delete"}
    ))      //Message message,Channel channel《手动ACK需要通过管道》
    public void deleteListener(Map<String,Object> map, Message message, Channel channel) throws IOException {
        String userId = map.get("userId").toString();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        List<Long> skuIds =(List<Long>) map.get("skuIds");
        List<String> skuIdString = skuIds.stream().map(skuId -> skuId.toString()).collect(Collectors.toList());
        hashOps.delete(skuIdString.toArray());
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);//手动ACK是否确认多个，false只确认当前这一条
        } catch (IOException e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);//重新入队
            e.printStackTrace();
        }


    }

}
