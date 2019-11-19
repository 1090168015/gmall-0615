package com.atguigu.gmall.oms.service.impl;

import VO.ItemSaleVO;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.oms.dao.OrderDao;
import com.atguigu.gmall.oms.dao.OrderItemDao;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.fegin.GmallPmsClient;
import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.usm.entity.MemberReceiveAddressEntity;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.Query;
import com.atguigu.gmall.core.bean.QueryCondition;

import com.atguigu.gmall.oms.service.OrderService;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    OrderItemDao orderItemDao;
    @Autowired
    GmallPmsClient gmallPmsClient;
    @Autowired
    private OrderDao orderDao;

    @Autowired
    private AmqpTemplate amqpTemplate;



    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public OrderEntity createOrder(OrderSubmitVO submitVO) {
        //新增订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(submitVO.getOrderToken());//订单号
        orderEntity.setMemberId(submitVO.getUserId());//member_id
        orderEntity.setMemberUsername(submitVO.getUserName());//用户名
        orderEntity.setTotalAmount(submitVO.getTotalPrice());//订单总额
        orderEntity.setPayType(submitVO.getPayType());//支付方式【1->支付宝；2->微信；3->银联； 4->货到付款；】
        orderEntity.setCreateTime(new Date());//创建时间
        orderEntity.setSourceType(1);//订单来源[0->PC订单；1->app订单]'
        orderEntity.setStatus(0);//'订单状态【0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单】
        orderEntity.setDeliveryCompany(submitVO.getDeliveryCompany());//物流公司(配送方式)'
        orderEntity.setAutoConfirmDay(15);//自动确认时间（天）
        orderEntity.setModifyTime(orderEntity.getCreateTime());//修改时间
        orderEntity.setConfirmStatus(0);//确认收货状态[0->未确认；1->已确认]
        orderEntity.setDeleteStatus(0);//删除状态【0->未删除；1->已删除】
        //根据订单明细查询营销信息获取成长积分和赠送积分
        orderEntity.setGrowth(100);//可以获得的成长值
        orderEntity.setIntegration(200);//可以获得的积分
        //查询营销信息：店铺，spu，sku，品类  没做

        //地址信息
        MemberReceiveAddressEntity address = submitVO.getAddress();
        if (address != null) {
            orderEntity.setReceiverCity(address.getCity());//城市
            orderEntity.setReceiverDetailAddress(address.getDetailAddress());//详细地址
            orderEntity.setReceiverName(address.getName());//收货人姓名
            orderEntity.setReceiverPhone(address.getPhone());//收货人电话
            orderEntity.setReceiverPostCode(address.getPostCode());//收货人邮编
            orderEntity.setReceiverProvince(address.getProvince());//省份/直辖市
            orderEntity.setReceiverRegion(address.getRegion());//区
        }
        this.save(orderEntity);//新增订单详情
        System.out.println("1"+orderEntity);
        List<OrderItemVO> orderItemVOS = submitVO.getOrderItemVOS();
        if (!CollectionUtils.isEmpty(orderItemVOS)){
            orderItemVOS.forEach(itemVO->{
                Resp<SkuInfoEntity> skuInfoEntityResp = gmallPmsClient.querySkuById(itemVO.getSkuId());
                SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
                OrderItemEntity itemEntity = new OrderItemEntity();
                itemEntity.setSkuQuantity(itemVO.getCount());//商品购买的数量
                itemEntity.setSkuPic(itemVO.getDefaultImage());//商品sku图片
                itemEntity.setSkuName(itemVO.getTitle());//商品sku名字
                itemEntity.setSkuId(itemVO.getSkuId());//商品sku编号
                itemEntity.setSpuId(skuInfoEntity.getSpuId());//spu_id
                itemEntity.setOrderSn(submitVO.getOrderToken());//订单编号
                itemEntity.setOrderId(orderEntity.getId());
                itemEntity.setCategoryId(skuInfoEntity.getCatalogId());//商品分类id
                itemEntity.setSkuAttrsVals(JSON.toJSONString(itemVO.getSkuSaleAttrValue()));//商品销售属性组合（JSON）
                itemEntity.setSkuPrice(skuInfoEntity.getPrice());//商品sku价格
                orderItemDao.insert(itemEntity);

            });
        }
        //System.out.println(orderEntity);
        /*订单创建完成，但是如果还没有来的及响应，挂掉了，那么如果不解锁库存，也会造成库存锁死，所以也要在创建订单完成时创建延时任务，用于超时后关闭订单，释放锁定的资源解锁库存。订单功能创建方法，是在oms里创建额，所以要在oms里创建延时任务解锁库存*/
        //发送消息到交换机，并指定延时队列routingKey：oms.close，而绑定此routingKey的消息队列为延时队列，延时队列到期后会发送消息到死信交换机，然后发到死信队列，所以编写监听器，要监听死信队列
        //订单创建成功需要修改订单状态，将订单状态改为4，订单关闭状态，--关单
        this.amqpTemplate.convertAndSend("OMS-EXCHANGE","oms.close",submitVO.getOrderToken());// 所发信息内容,内容是订单号,发送消息到
        return orderEntity;         //发送消息之后要有监听器监听消息，所以要编写监听器接收消息
    }

    @Override
    public int closeOrder(String orderToken) {
//        OrderEntity orderEntity=  this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn",orderToken));
//        if (orderEntity.getStatus()==0){    //update oms_order set `status`=4  where order_sn=#{orderToken} and `status`=0
           return this.orderDao.closeOrder(orderToken);//返回数据库的影响条数，如果返回1说明订单状态已修改
//        }
//        return 0;
    }

    @Override
    public int success(String orderToken) {
        return this.orderDao.success(orderToken);
    }

}
/*
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
        `coupon_id` bigint(20) DEFAULT NULL COMMENT '使用的优惠券',
        `pay_amount` decimal(10,0) DEFAULT NULL COMMENT '应付总额',
        `freight_amount` decimal(10,0) DEFAULT NULL COMMENT '运费金额',
        `promotion_amount` decimal(10,0) DEFAULT NULL COMMENT '促销优化金额（促销价、满减、阶梯价）',
        `integration_amount` decimal(10,0) DEFAULT NULL COMMENT '积分抵扣金额',
        `coupon_amount` decimal(10,0) DEFAULT NULL COMMENT '优惠券抵扣金额',
        `discount_amount` decimal(10,0) DEFAULT NULL COMMENT '后台调整订单使用的折扣金额',
        `delivery_sn` varchar(64) DEFAULT NULL COMMENT '物流单号',
        `bill_type` tinyint(4) DEFAULT NULL COMMENT '发票类型[0->不开发票；1->电子发票；2->纸质发票]',
        `bill_header` varchar(255) DEFAULT NULL COMMENT '发票抬头',
        `bill_content` varchar(255) DEFAULT NULL COMMENT '发票内容',
        `bill_receiver_email` varchar(64) DEFAULT NULL COMMENT '收票人邮箱',
        `receiver_phone` varchar(32) DEFAULT NULL COMMENT '收货人电话',
        `receiver_city` varchar(32) DEFAULT NULL COMMENT '城市',
        `note` varchar(500) DEFAULT NULL COMMENT '订单备注',
        `use_integration` int(11) DEFAULT NULL COMMENT '下单时使用的积分',
        `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
        `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
        `receive_time` datetime DEFAULT NULL COMMENT '确认收货时间',
        `comment_time` datetime DEFAULT NULL COMMENT '评价时间',
        PRIMARY KEY (`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';*/
