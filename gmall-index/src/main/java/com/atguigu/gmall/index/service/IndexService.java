package com.atguigu.gmall.index.service;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;

import java.util.List;

public interface IndexService {
    List<CategoryEntity> queryLevel1Category();

    List<CategoryVO> queryCategoryVO(Long pid);

    String testLock();

    String testRead();

    String testWrite();
}
