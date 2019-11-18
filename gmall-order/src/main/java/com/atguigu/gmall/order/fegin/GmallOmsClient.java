package com.atguigu.gmall.order.fegin;

import com.atguigu.gmall.cart.api.GmallCartApi;
import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {

}
