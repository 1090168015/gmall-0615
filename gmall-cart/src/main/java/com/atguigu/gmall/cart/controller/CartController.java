package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.config.Jwtproperties;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.cart.vo.Cart;
import com.atguigu.gmall.cart.vo.UserInfo;
import com.atguigu.gmall.core.bean.Resp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@RequestMapping("cart")
public class CartController {

    @Autowired
    private CartService cartService;
    @PostMapping
    public Resp<Object> addCart(@RequestBody Cart cart){//添加购物车
        this.cartService.addCart(cart);

        return  Resp.ok(null);
    }

    @GetMapping
    public Resp<List<Cart>> queryCarts(){//查询所有购物车
        List<Cart> carts = this.cartService.queryCarts();
        return Resp.ok(carts);
    }

    @PostMapping("update")//修改购物车
    public Resp<Object> updateCart(@RequestBody Cart cart){
        this.cartService.updateCart(cart);
        return Resp.ok(null);
    }
    @PostMapping("{skuId}")//删除指定商品购物车
    public Resp<Object> deleteCart(@PathVariable("skuId") String SkuId){
        this.cartService.deleteCart(SkuId);
        return Resp.ok(null);
    }

    @PostMapping("check")//更新购物车选中状态
    public Resp<Object> checkCart(@RequestBody List<Cart> carts){
        this.cartService.checkCart(carts);
        return Resp.ok(null);
    }

  /*  @GetMapping
    public String test(HttpServletRequest request){

        return request.getAttribute("userId")+"+++++"+request.getAttribute("userkey");
    }*/

    @Autowired
    Jwtproperties jwtproperties;
    @GetMapping("test")
    public UserInfo test(HttpServletRequest request){
        return LoginInterceptor.get();
    }
}
