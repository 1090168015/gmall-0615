package com.atguigu.gmall.order.fegin;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.usm.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}