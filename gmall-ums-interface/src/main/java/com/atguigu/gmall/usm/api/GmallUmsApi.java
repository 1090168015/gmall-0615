package com.atguigu.gmall.usm.api;

import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.usm.entity.MemberEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface GmallUmsApi {
    @GetMapping("ums/member/query")//用户其他工程远程调用，用户登录
    public Resp<MemberEntity> queryUser(@RequestParam("username")String username, @RequestParam("password")String password);
}
