package com.atguigu.gmall.wms.config;

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
    public Exchange exchange(){//路由交换机
        return new TopicExchange("WMS-EXCHANGE", true, false, null);
    }
    @Bean
    public Queue queue(){//延时队列
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "WMS-EXCHANGE");//死信路由
        arguments.put("x-dead-letter-routing-key", "wms.ttl");//死信队列
        arguments.put("x-message-ttl", 60000);//延时队列延时时间
        return new Queue("WMS-TTL-QUEUE",true,false,false,arguments);
    }
    @Bean
    public Binding binding(){//交换机与延时队列绑定

        return new Binding("WMS-TTL-QUEUE",Binding.DestinationType.QUEUE,"WMS-EXCHANGE","wms.unlock",null);
    }

    @Bean
    public Queue bing(){//死信队列
        return new Queue("WMS-DEAD-QUEUE",true,false,false,null);

    }

    @Bean
    public Binding deadBinding(){//交换机与死信队列绑定

        return new Binding("WMS-DEAD-QUEUE",Binding.DestinationType.QUEUE,"WMS-EXCHANGE","wms.ttl",null);
    }



}
