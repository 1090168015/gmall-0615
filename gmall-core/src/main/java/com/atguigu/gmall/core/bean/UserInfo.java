package com.atguigu.gmall.core.bean;

import lombok.Data;

@Data
public class UserInfo {//用于保存userId和userKey
    private Long userId;//用户登录id
    private String userKey; //用户未登录时，系统分配的游客id
}
