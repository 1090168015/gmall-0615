package com.atguigu.gmall.item.fegin;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-server")
public interface GmallSmsClient extends GmallSmsApi {
}
