package com.atguigu.gmall.sms.api;

import VO.ItemSaleVO;
import VO.SaleVO;
import com.atguigu.gmall.core.bean.Resp;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


public interface GmallSmsApi {
    @PostMapping("sms/skubounds/item/sales/{skuId}")
    public Resp<List<ItemSaleVO>> queryItemSalveVOs(@PathVariable("skuId")Long skuId);
    @PostMapping("sms/skubounds/sale")
    public Resp<Object> saveSale(@RequestBody SaleVO saleVO);
}
