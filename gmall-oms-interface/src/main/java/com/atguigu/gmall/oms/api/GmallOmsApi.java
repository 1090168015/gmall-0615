package com.atguigu.gmall.oms.api;

import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface GmallOmsApi {

    @PostMapping("oms/order")//根据传的请求提交参数，创建订单，订单实例OrderEntity
    public Resp<OrderEntity> createOrder(@RequestBody OrderSubmitVO submitVO);
}
