package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.usm.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSubmitVO {
    private Long userId;//用户Id
    private String userName;
    private MemberReceiveAddressEntity address;//收获地址
    private Integer payType;//支付方式
    private String deliveryCompany;//配送公司
    private List<OrderItemVO> orderItemVOS;//订单详情
    private Integer useIntegration;//下单时使用的积分
    private BigDecimal totalPrice;//总价，用于验价
    private String orderToken;//防重，订单编号



}
