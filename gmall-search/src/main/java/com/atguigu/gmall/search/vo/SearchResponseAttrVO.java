package com.atguigu.gmall.search.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data       // 可以封装品牌和分类对应数据库字段，用于ES查询响应数据，可以代表品牌Vo也可以代表分类VO,也可以作为搜索属性的VO
public class SearchResponseAttrVO implements Serializable {

    private Long productAttributeId;//1
    //当前属性值的所有值
    private List<String> value = new ArrayList<>();
    //属性名称
    private String name;//网络制式，分类
}
