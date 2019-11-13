package com.atguigu.gmall.cart.service;

import VO.ItemSaleVO;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.fegin.GmallPmsClient;
import com.atguigu.gmall.cart.fegin.GmallSmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.vo.Cart;
import com.atguigu.gmall.cart.vo.UserInfo;
import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;

    private static final String KEY_PREFIX="cart:key:";//在redis中保存购物车时，保存购物车的键的前缀

    public void addCart(Cart cart) {
        /*String key =KEY_PREFIX;
        //判断用户登录状态
        UserInfo userInfo = LoginInterceptor.get();//调用拦截器获取封装有用户id与游客id的对象
        if (userInfo.getUserId()!=null){//能够从对象中获取用户id说明用户已登录
            key +=userInfo.getUserId();//使用用户id拼接成此用户在redis中保存购物车对应的键
        }else {
            key+=userInfo.getUserKey();//使用游客id拼接成此游客在redis中保存购物车对应的键
        }*/
        //判断用户登录状态，是用户登录还是游客
        String key = getKey();

        //判断购物车中是否有该记录
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);//使用保存用户、游客id拼接生成的键，获取BoundHashOperations对象，用于操作键里对应的数据，就像操作map集合一样操作
        //取出用户新增购物车商品数量
        Integer count = cart.getCount();
        String skuId = cart.getSkuId().toString();//将商品id转化为字符串

        //有记录，添加商品是更新数量
        if (hashOps.hasKey(cart.getSkuId().toString())){//判断redis中是否有购物车中的商品id
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();//有就获取redis中购物车，
            cart = JSON.parseObject(cartJson, Cart.class);//将redis中获取的购物车json字符串转化为redis中购物车对象
            //更新购物车中商品的数量
            cart.setCount(cart.getCount()+count);//将添加购物车的数量与redis中的数量相加
            //同步到redis中     //将对应商品的购物车同步到redis中
          //  hashOps.put(skuId,JSON.toJSON(cart));//可以在外部统一更新

        }else {
            //没有记录，新增商品记录
            //查询商品信息
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(cart.getSkuId());//根据购物车中skuId商品id获取商品信息
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            cart.setTitle(skuInfoEntity.getSkuTitle());//商品标题
            cart.setCheck(true);//勾选
            cart.setPrice(skuInfoEntity.getPrice());
            cart.setDefaultImage(skuInfoEntity.getSkuDefaultImg());//默认图片
            //查询销售属性
            Resp<List<SkuSaleAttrValueEntity>> listResp = gmallPmsClient.querSaleAttrBySkuId(cart.getSkuId());
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = listResp.getData();
            cart.setSkuAttrValue(skuSaleAttrValueEntities);

            //查询营销信息,优惠信息
            Resp<List<ItemSaleVO>> listResp1 = this.gmallSmsClient.queryItemSalveVOs(cart.getSkuId());
            cart.setSales(listResp1.getData());
            //将新增商品购物车保存到redis中
     //       hashOps.put(skuId,JSON.toJSONString(cart));//可以在外部统一更新
        }
        //将添加的skuI的商品的购物车同步到redis中
        hashOps.put(skuId,JSON.toJSONString(cart));//将对应商品的购物车同步到redis中
    }


    public List<Cart> queryCarts() {
        //查询未登录状态的购物车（游客购物车）    //游客保存cookie的键对应的属性是userKey
        UserInfo userInfo = LoginInterceptor.get();
        String userKey = userInfo.getUserKey();
        String key1 =KEY_PREFIX+userKey;
        BoundHashOperations<String, Object, Object> userKeyOps = this.redisTemplate.boundHashOps(key1);
        List<Object> cartJsonList = userKeyOps.values();
       /* if (CollectionUtils.isEmpty(cartJsonList)){
            return  null;//不能直接返回，因为未登录是游客有可能不添加购物车，而用户有可能添加购物车，这时直接返回，下面查询登录用户购物车就无法查询，直接判断不为空时，反序列化即可
        }*/
        List<Cart> userKeyCarts =null;
        if (!CollectionUtils.isEmpty(cartJsonList)){
            //游客购物车集合
            userKeyCarts = cartJsonList.stream().map(cartJson -> JSON.parseObject(cartJson.toString(), Cart.class)).collect(Collectors.toList());
        }
        //判断登录状态
        if (userInfo.getUserId()==null){
            //未登录直接返回(返回游客购物车)
            return userKeyCarts;
        }
        //登录时，查询登录状态购物车
        String  key2 =KEY_PREFIX+userInfo.getUserId();
        BoundHashOperations<String, Object, Object> userIdOps = this.redisTemplate.boundHashOps(key2);
        //判断未登录的购物车是否为空
        if (!CollectionUtils.isEmpty(userKeyCarts)){
            //不为空，合并
            userKeyCarts.forEach(cart -> {//遍历游客购物场车cart，
                //有更新数量
                if (userIdOps.hasKey(cart.getSkuId().toString())){//判断登录用户是否有游客购物车里的商品id即skuId，如果有直接更新数量
                    String cartJson = userIdOps.get(cart.getSkuId().toString()).toString();
                    Cart idCart = JSON.parseObject(cartJson, Cart.class);//获取登录用户商品购物车
                    //更新数量
                    idCart.setCount(idCart.getCount()+cart.getCount());
                    userIdOps.put(cart.getSkuId().toString(),JSON.toJSONString(idCart));
                }else {
                    //没有，新增记录
                    userIdOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
                }
            });
            this.redisTemplate.delete(key1);

        }
        //未登录状态购物车为空（游客购物车为空），直接返回登录状态购物车
        List<Object> userIdCartJsonList = userIdOps.values();
        System.out.println(userIdCartJsonList);
        return userIdCartJsonList.stream().map(userIdCartJson->JSON.parseObject(userIdCartJson.toString(),Cart.class)).collect(Collectors.toList());
    }


    public void updateCart(Cart cart) {
        //判断用户登录状态，是用户登录还是游客
        String key = getKey();
        Integer count = cart.getCount();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(cart.getSkuId().toString())){
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart= JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            hashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
        }
    }


    public void deleteCart(String skuId) {//删除指定商品购物车
        String key = getKey();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId)){
            hashOps.delete(skuId.toString());
        }
    }

    public void checkCart(List<Cart> carts) {//更新购物车选中状态
        String key = getKey();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        carts.forEach(cart -> {
            Boolean check = cart.getCheck();
            if (hashOps.hasKey(cart.getSkuId().toString())){
                //获取购物车中跟更数量的购物记录
                String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
                JSON.parseObject(cartJson,Cart.class);
                cart.setCheck(check);
                hashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
            }
        });
    }

    private String getKey(){
        String key =KEY_PREFIX;
        //判断用户登录状态
        UserInfo userInfo = LoginInterceptor.get();//调用拦截器获取封装有用户id与游客id的对象
        if (userInfo.getUserId()!=null){//能够从对象中获取用户id说明用户已登录
            key +=userInfo.getUserId();//使用用户id拼接成此用户在redis中保存购物车对应的键
        }else {
            key+=userInfo.getUserKey();//使用游客id拼接成此游客在redis中保存购物车对应的键
        }
        return key;

    }
}
