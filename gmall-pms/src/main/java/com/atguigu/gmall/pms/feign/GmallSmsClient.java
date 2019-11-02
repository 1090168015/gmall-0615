package com.atguigu.gmall.pms.feign;


import com.atguigu.gmall.smsapi.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-server")
public interface GmallSmsClient extends GmallSmsApi {

}
