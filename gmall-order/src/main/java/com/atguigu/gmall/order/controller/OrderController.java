package com.atguigu.gmall.order.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.core.bean.UserInfo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.config.AlipayTemplate;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.order.vo.PayAsyncVo;
import com.atguigu.gmall.order.vo.PayVo;
import com.atguigu.gmall.order.vo.SeckillVO;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.naming.directory.SearchResult;
import java.io.PushbackInputStream;


@RestController
@RequestMapping("order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private AlipayTemplate alipayTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    RedissonClient redissonClient;


    @GetMapping("confirm")
    private Resp<OrderConfirmVO> confirm(){//点击去结算发送请求，获取订单确认信息
        OrderConfirmVO orderConfirmVO= this.orderService.confirm();
        return Resp.ok(orderConfirmVO);
    }

    @PostMapping("submit")
    public Resp<Object> submit(@RequestBody OrderSubmitVO orderSubmitVO){
        String form =null;
        try {
            OrderEntity orderEntity = this.orderService.submit(orderSubmitVO);
            PayVo payVo = new PayVo();
            payVo.setBody("谷粒商城支付系统");
            payVo.setSubject("支付平台");
            payVo.setTotal_amount(orderEntity.getTotalAmount().toString());//总价格
            payVo.setOut_trade_no(orderEntity.getOrderSn());//商户订单号 必填
            form = this.alipayTemplate.pay(payVo);//生成支付表单
            System.out.println(form);

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
            return Resp.ok(form);
    }
    @PostMapping("/pay/success")
    public Resp<Object> paySuccess(PayAsyncVo payAsyncVo){

        System.out.println("=======================支付成功==========================");
        // 订单状态的修改和库存的扣除
        orderService.paySuccess(payAsyncVo.getOut_trade_no());

        return Resp.ok(null);
    }


   /* @RequestMapping("seckill/{skuId}")//秒杀Controller方法
    public Resp<Object> seckill(@PathVariable("skuId") Long skuId) throws InterruptedException {
        String stockJson = this.redisTemplate.opsForValue().get("seckill:stock:" + skuId);
        if (StringUtils.isEmpty(stockJson)){
            return Resp.ok("该秒杀商品不存在！！！！！！！");
        }
        Integer stock = Integer.valueOf(stockJson);
        RSemaphore semaphore = this.redissonClient.getSemaphore("seckill:stock:" + skuId);//获取信号锁，以"seckill:stock:skuId为名字
        semaphore.trySetPermits(stock);//设置信号总数

        semaphore.acquire(1);
        UserInfo userInfo = LoginInterceptor.get();
        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("seckill:stock:" + userInfo.getUserId());
        countDownLatch.trySetCount(1);

        SeckillVO seckillVO = new SeckillVO();
        seckillVO.setSkuId(skuId);
        seckillVO.setUserId(userInfo.getUserId());
        seckillVO.setCount(1);

        this.amqpTemplate.convertAndSend("SECKILL-EXCHANGE","seckill.create",seckillVO);
        this.redisTemplate.opsForValue().set("seckill:stock:" + skuId,String.valueOf(--stock));
        return Resp.ok(null);

    }

    @GetMapping
    public Resp<OrderEntity> queryOrder() throws InterruptedException {//查询秒杀订单

        UserInfo userInfo = LoginInterceptor.get();

        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("seckill:stock:"+userInfo.getUserId());
        countDownLatch.await();
        OrderEntity orderEntity=   this.orderService.queryOrder();
        return  Resp.ok(orderEntity);
    }*/
}
