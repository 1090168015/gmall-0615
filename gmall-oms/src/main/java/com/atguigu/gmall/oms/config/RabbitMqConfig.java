package com.atguigu.gmall.oms.config;


import com.sun.org.apache.bcel.internal.generic.NEW;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {
    @Bean
    public Exchange exchange(){//参数依次是：交换机名称，不否持久化，是否自动删除，参数
        return new TopicExchange("OMS-EXCHANGE",true,false,null);
    }
    @Bean
    public Queue queue(){//延时队列
        Map<String, Object> arguments  = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "OMS-EXCHANGE");//死信路由
        arguments.put("x-dead-letter-routing-key", "oms.dead");//死信队列routingKey
        arguments.put("x-message-ttl", 110000);//延时时间
        return new Queue("OMS-TTL-QUEUE", true, false, false, arguments);
    }
    @Bean
    public Binding binging(){//将交换机与延时对列绑定
        return new Binding("OMS-TTL-QUEUE",Binding.DestinationType.QUEUE,"OMS-EXCHANGE","oms.close",null);
    }
    @Bean
    public Queue deadQueue(){//声明死信队列
        return new Queue("OMS-DEAD-QUEUE",true,false,false,null);
    }
    @Bean
    public Binding deadBinding(){//将死信队列与交换机绑定
        return new Binding("OMS-DEAD-QUEUE",Binding.DestinationType.QUEUE,"OMS-EXCHANGE","oms.dead",null);
    }

}
