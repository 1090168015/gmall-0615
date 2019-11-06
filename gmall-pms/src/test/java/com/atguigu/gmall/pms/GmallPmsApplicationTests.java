package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.dao.BrandDao;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.service.BrandService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallPmsApplicationTests {

    @Autowired
    private BrandDao brandDao;
    @Autowired
    private BrandService brandService;

    @Test
    void contextLoads() {
    }

    @Test
    public void test(){
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setDescript("尚硅谷");
//        brandEntity.setFirstLetter("S");
//        brandEntity.setLogo("www.shangguigu.com/log.gif");
//        brandEntity.setName("s上古");
//        brandEntity.setShowStatus(0);
//        brandEntity.setSort(1);
//        this.brandDao.insert(brandEntity);
//        HashMap<String, Object> hashMap = new HashMap<>();
//        hashMap.put("name","s上古");
//
//        this.brandDao.deleteByMap(hashMap);

//        System.out.println(brandDao.selectList(new QueryWrapper<BrandEntity>().eq("name","fds发多少")));
//        IPage<BrandEntity> page = this.brandDao.selectPage(new Page<BrandEntity>(2, 2), null);
//        System.out.println(page.getRecords());
//        System.out.println(page.getTotal());
//        System.out.println(page.getSize());
        IPage<BrandEntity> page1 =  brandService.page(new Page<BrandEntity>(2L,2L),null);

        System.out.println(page1.getRecords());
        System.out.println(page1.getTotal());
        System.out.println(page1.getSize());

    }

}
