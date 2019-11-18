package com.atguigu.gmall.oms.fegin;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-server")
public interface GmallPmsClient extends GmallPmsApi {
}
