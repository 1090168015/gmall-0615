package com.atguigu.gmall.order.fegin;

import com.atguigu.gmall.api.GmallWmsApi;
import com.atguigu.gmall.usm.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
