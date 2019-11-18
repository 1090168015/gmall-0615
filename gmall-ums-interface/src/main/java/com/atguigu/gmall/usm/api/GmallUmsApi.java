package com.atguigu.gmall.usm.api;

import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.usm.entity.MemberEntity;
import com.atguigu.gmall.usm.entity.MemberReceiveAddressEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GmallUmsApi {
    @GetMapping("ums/member/query")//用户其他工程远程调用，用户登录
    public Resp<MemberEntity> queryUser(@RequestParam("username")String username, @RequestParam("password")String password);
    @GetMapping("ums/memberreceiveaddress/{userId}")//获取用户地址列表接口
    public Resp<List<MemberReceiveAddressEntity>> queryAddressByUserId(@PathVariable("userId") Long userId);
    @GetMapping("ums/member/info/{id}")//用户详情查询
    public Resp<MemberEntity> queryUserById(@PathVariable("id") Long id);
}
