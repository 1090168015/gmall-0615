package com.atguigu.gmall.order.fegin;

import com.atguigu.gmall.api.GmallWmsApi;
import com.atguigu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("cart-service")
public interface GmallCartClient extends GmallCartApi {

}
