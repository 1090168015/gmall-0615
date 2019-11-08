package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

@Data
public class CategoryVO extends CategoryEntity {
    private List<CategoryEntity> subs;//响应的数据包含子分类，子分类与CategoryEntity字段相同
}
