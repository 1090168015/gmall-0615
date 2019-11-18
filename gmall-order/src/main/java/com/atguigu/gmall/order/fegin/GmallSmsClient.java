package com.atguigu.gmall.order.fegin;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-server")
public interface GmallSmsClient extends GmallSmsApi {
}
