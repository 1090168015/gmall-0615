package com.atguigu.gmall.cart.fegin;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-server")
public interface GmallSmsClient extends GmallSmsApi {
}
