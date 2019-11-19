package com.atguigu.gmall.order.service;

import VO.ItemSaleVO;
import com.atguigu.gmall.cart.vo.CartItemVO;
import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.core.bean.UserInfo;
import com.atguigu.gmall.entity.WareSkuEntity;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.fegin.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.usm.entity.MemberEntity;
import com.atguigu.gmall.usm.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.vo.SkuLockVO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallUmsClient gmallUmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private GmallCartClient gmallCartClient;
    @Autowired
    private GmallOmsClient gmallOmsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    AmqpTemplate amqpTemplate;

    private static final String TOKEN_PREFIX="order:token:";//防止表单重复提交唯一值前缀

    public OrderConfirmVO confirm() {//点击去结算发送请求，获取订单确认信息处理业务方法
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();//点击去结算生成订单确认信息对象

        UserInfo userInfo = LoginInterceptor.get();//获取用户登录信息
        CompletableFuture<Void> addressFurture = CompletableFuture.runAsync(() -> {
            Resp<List<MemberReceiveAddressEntity>> addressResp = this.gmallUmsClient.queryAddressByUserId(userInfo.getUserId());//登录用户地址信息
            orderConfirmVO.setAddresses(addressResp.getData());//设置用户地址信息

        }, threadPoolExecutor);

        CompletableFuture<Void> cartFuture = CompletableFuture.supplyAsync(() -> {
            Resp<List<CartItemVO>> listResp = this.gmallCartClient.queryCartItemVO(userInfo.getUserId());//购物车商品信息封装的对象，商品id和商品数量
            List<CartItemVO> itemVOS = listResp.getData();
            return itemVOS;

        }, threadPoolExecutor).thenAcceptAsync(itemVOS -> {
            if (CollectionUtils.isEmpty(itemVOS)) {
                return;
            }
            //把购物车选中记录转化为订货清单
            List<OrderItemVO> orderItems = itemVOS.stream().map(cartItemVO -> {
                OrderItemVO orderItemVO = new OrderItemVO();
                //根据skuId查询sku
                Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(cartItemVO.getSkuId());
                SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
                //根据skuId查询销售属性
                Resp<List<SkuSaleAttrValueEntity>> skuSaleResp = this.gmallPmsClient.querSaleAttrBySkuId(cartItemVO.getSkuId());
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = skuSaleResp.getData();

                orderItemVO.setSkuSaleAttrValue(skuSaleAttrValueEntities);//商品规格参数
                orderItemVO.setTitle(skuInfoEntity.getSkuTitle());//标题
                orderItemVO.setSkuId(cartItemVO.getSkuId());//商品id
                orderItemVO.setPrice(skuInfoEntity.getPrice());//价格
                orderItemVO.setDefaultImage(skuInfoEntity.getSkuDefaultImg());//图片
                orderItemVO.setCount(cartItemVO.getCount());//购买数量
                //根据skuId获取营销信息
                Resp<List<ItemSaleVO>> saleResp = this.gmallSmsClient.queryItemSalveVOs(cartItemVO.getSkuId());
                List<ItemSaleVO> itemSaleVOS = saleResp.getData();
                orderItemVO.setSales(itemSaleVOS);
                //根据skuId获取库存信息
                Resp<List<WareSkuEntity>> storePesp = this.gmallWmsClient.queryWareBySkuId(cartItemVO.getSkuId());
                List<WareSkuEntity> wareSkuEntities = storePesp.getData();
                orderItemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));

                orderItemVO.setWeight(skuInfoEntity.getWeight());//商品重量
                return orderItemVO;
            }).collect(Collectors.toList());
            orderConfirmVO.setOrderItems(orderItems);//订单的送货清单

        }, threadPoolExecutor);


        CompletableFuture<Void> boundFuture = CompletableFuture.runAsync(() -> {
            Resp<MemberEntity> memberEntityResp = this.gmallUmsClient.queryUserById(userInfo.getUserId());
            MemberEntity memberEntity = memberEntityResp.getData();
            orderConfirmVO.setBounds(memberEntity.getIntegration());//积分信息（优惠信息）
        }, threadPoolExecutor);
        //获取用户信息（积分信息）

        CompletableFuture<Void> idFuture = CompletableFuture.runAsync(() -> {
            //生成唯一标志，防止重复提交
            String timeId = IdWorker.getTimeId();
            orderConfirmVO.setOrderToken(timeId);//防止表单重复提交
            this.redisTemplate.opsForValue().set(TOKEN_PREFIX+timeId,timeId);//保存到redis中，防止表单重复 提交
        }, threadPoolExecutor);
        CompletableFuture.allOf(addressFurture,cartFuture,boundFuture,idFuture).join();

        return orderConfirmVO;
    }

    public OrderConfirmVO confirm1() {//点击去结算发送请求，获取订单确认信息处理业务方法
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();//点击去结算生成订单确认信息对象
        //获取用户登录信息
        UserInfo userInfo = LoginInterceptor.get();
        Resp<List<MemberReceiveAddressEntity>> addressResp = this.gmallUmsClient.queryAddressByUserId(userInfo.getUserId());//登录用户地址信息
        orderConfirmVO.setAddresses(addressResp.getData());//设置用户地址信息
        Resp<List<CartItemVO>> listResp = this.gmallCartClient.queryCartItemVO(userInfo.getUserId());//购物车商品信息封装的对象，商品id和商品数量
        List<CartItemVO> itemVOS = listResp.getData();
        if(CollectionUtils.isEmpty(itemVOS)){
            return null;
        }
        //把购物车选中记录转化为订货清单
        List<OrderItemVO> orderItems = itemVOS.stream().map(cartItemVO -> {
            OrderItemVO orderItemVO = new OrderItemVO();
            //根据skuId查询sku
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(cartItemVO.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            //根据skuId查询销售属性
            Resp<List<SkuSaleAttrValueEntity>> skuSaleResp = this.gmallPmsClient.querSaleAttrBySkuId(cartItemVO.getSkuId());
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = skuSaleResp.getData();

            orderItemVO.setSkuSaleAttrValue(skuSaleAttrValueEntities);//商品规格参数
            orderItemVO.setTitle(skuInfoEntity.getSkuTitle());//标题
            orderItemVO.setSkuId(cartItemVO.getSkuId());//商品id
            orderItemVO.setPrice(skuInfoEntity.getPrice());//价格
            orderItemVO.setDefaultImage(skuInfoEntity.getSkuDefaultImg());//图片
            orderItemVO.setCount(cartItemVO.getCount());//购买数量
            //根据skuId获取营销信息
            Resp<List<ItemSaleVO>> saleResp = this.gmallSmsClient.queryItemSalveVOs(cartItemVO.getSkuId());
            List<ItemSaleVO> itemSaleVOS = saleResp.getData();
            orderItemVO.setSales(itemSaleVOS);
            //根据skuId获取库存信息
            Resp<List<WareSkuEntity>> storePesp = this.gmallWmsClient.queryWareBySkuId(cartItemVO.getSkuId());
            List<WareSkuEntity> wareSkuEntities = storePesp.getData();
            orderItemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));

            orderItemVO.setWeight(skuInfoEntity.getWeight());//商品重量
            return orderItemVO;
        }).collect(Collectors.toList());
        orderConfirmVO.setOrderItems(orderItems);//订单的送货清单
        //获取用户信息（积分信息）
        Resp<MemberEntity> memberEntityResp = this.gmallUmsClient.queryUserById(userInfo.getUserId());
        MemberEntity memberEntity = memberEntityResp.getData();
        orderConfirmVO.setBounds(memberEntity.getIntegration());//积分信息（优惠信息）

        //生成唯一标志，防止重复提交
        String timeId = IdWorker.getTimeId();
        orderConfirmVO.setOrderToken(timeId);//防止表单重复提交
        return  orderConfirmVO;


    }

    public OrderEntity submit(OrderSubmitVO orderSubmitVO) {
//        1. 验证令牌防止重复提交
        String orderToken = orderSubmitVO.getOrderToken();//获取保存到orderSubmitVO中的唯一值

        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";//redis 的lua脚本，用于删除操作
        //执行脚本删除保存到redis中的唯一值，如果根据orderToken与保存在redis中唯一值进行对比，能够删除则返回1，不能删除返回0，
        // 能够删除说明redis中保存一份唯一值还在，没有提交过订单，不能删除说明已经删除了保存在redis中的唯一值，已经提交过订单
        Long flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(TOKEN_PREFIX + orderToken), orderToken);

        if (flag==0L){
            throw new RuntimeException("请不要重复提交");
        }
//        2. 验证价格
        BigDecimal totalPrice = orderSubmitVO.getTotalPrice();
        List<OrderItemVO> orderItemVOS = orderSubmitVO.getOrderItemVOS();

        if (CollectionUtils.isEmpty(orderItemVOS)){
            throw new RuntimeException("请添加购物清单");
        }
        BigDecimal currentPrice = orderItemVOS.stream().map(orderItemVO -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(orderItemVO.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            return skuInfoEntity.getPrice().multiply(new BigDecimal((orderItemVO.getCount())));//multiply,乘法
        }).reduce((a, b) -> a.add(b)).get();//初始值加后面的值，然后将第一位加第二位的值作为初始值，继续加后面的值，依次类推
        if (totalPrice.compareTo(currentPrice)!=0){//《public int compareTo(BigDecimal val)》eturn -1、0或1，数值形式--小于、等于或大于
            throw new RuntimeException("请刷新页面重试");
        }
//        3. 验证库存，并锁定库存
        List<SkuLockVO> skuLockVOS = orderItemVOS.stream().map(orderItemVO -> {//需要锁库的订单商品集合
            SkuLockVO skuLockVO = new SkuLockVO();//创建验库和锁库VO对象
            skuLockVO.setSkuId(orderItemVO.getSkuId());//设置需要锁库存的商品id
            skuLockVO.setCount(orderItemVO.getCount());//设置需要锁库存的商品数量
            skuLockVO.setOrderToken(orderToken);//生成订单号
            return skuLockVO;//返回锁库存对象
        }).collect(Collectors.toList());
        Resp<Object> objectResp = this.gmallWmsClient.checkAndLock(skuLockVOS);
        if (objectResp.getCode()==1){//由返回对象，获取响应状态码，状态码为1，说明锁定失败
            throw new RuntimeException(objectResp.getMsg());//抛出那些商品锁定失败
        }
//        4. 生成订单
        UserInfo userInfo = LoginInterceptor.get();
        Resp<OrderEntity> orderResp =null;
        try {
            orderSubmitVO.setUserId(userInfo.getUserId());
            Resp<MemberEntity> memberEntityResp = this.gmallUmsClient.queryUserById(userInfo.getUserId());
            MemberEntity memberEntity = memberEntityResp.getData();
            orderSubmitVO.setUserName(memberEntity.getUsername());
            orderResp = this.gmallOmsClient.createOrder(orderSubmitVO);
        } catch (Exception e) {
            e.printStackTrace();
 //           创建订单前要验库锁库，查询资源是否允许购买，锁定资源，防止别的线程争抢，订单创建失败时，可以基于已锁定资源继续创建订单，可以不用释放资源，不解锁也可以
 //           this.amqpTemplate.convertAndSend("WMS-EXCHANGE","wms.ttl",orderToken);//订单创建失败，立即解锁库存，
            throw new RuntimeException("订单创建失败！服务器异常！");
        }

//        5. 删购物车中对应的记录（消息队列）
        Map<String, Object> map = new HashMap<>();
        map.put("userId",userInfo.getUserId());
        List<Long> skuIds = orderItemVOS.stream().map(orderItemVO -> orderItemVO.getSkuId()).collect(Collectors.toList());
        map.put("skuIds",skuIds);

        amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE","cart.delete",map);//发送消息删除购物车
        if (orderResp !=null){
            return orderResp.getData();
        }
        return  null;
    }

    public void paySuccess(String out_trade_no) {//经消息队列将订单号作为发送消息体，用于监听消息放在支付成功后修改订单状态为代发货状态，和扣除库存
        this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE","order.pay",out_trade_no);//发送消息到交换机，监听器在oms.listener;

    }
}
