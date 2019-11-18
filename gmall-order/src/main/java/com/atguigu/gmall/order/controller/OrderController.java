package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("confirm")
    private Resp<OrderConfirmVO> confirm(){//点击去结算发送请求，获取订单确认信息
        OrderConfirmVO orderConfirmVO= this.orderService.confirm();
        return Resp.ok(orderConfirmVO);
    }

    @PostMapping("submit")
    public Resp<Object> submit(@RequestBody OrderSubmitVO orderSubmitVO){

        this.orderService.submit(orderSubmitVO);
        return Resp.ok(null);

    }


}
