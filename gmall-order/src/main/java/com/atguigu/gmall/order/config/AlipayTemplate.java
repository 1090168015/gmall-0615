package com.atguigu.gmall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.order.vo.PayVo;
import lombok.Data;
import org.springframework.stereotype.Component;


@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2016101200670957";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDHiBi6rWKswSkLuLyAcX4GYgKHFdSUViNSLm+S4r3dhqtvAXTYXcMHo3LPCJaa3YCQvgRJd/Wofeq94AboFEkStRXNuUnio2hno56qpqEHRUYYQZGzuQvZ8Sph6UzW4P0pd0ZO1SoaQSR4C1XMJUnMwZlwkCIH1qnMedu3+GbCufSEAyTfS/VHc6w5AnULiBpzj+jxW1d2nJo0JNcyPdXj6PiO0fb0Tvv4v8UCVlZ55LfWkdZdZcyePtHRsXOAg1NHtKhsvCRWVJ/ObVAOa4MnaaD4YXgSIo02sxgEtJ6rT5y49Nu7B0mz0gW02QjxLleqLX6qjE+42NxqxlYh3CubAgMBAAECggEAVHiwh29Z4+sOlnDKFNJ3WPprOYcrbPUelO8luxiU6tDViPQj5GmlZHl05GMtZzi48g4PwI6xiHhuRZ4vLldQLERPi72Sowks7Rte074hU/Om3iP8LAr+EnE/0R20sw+i/cgKEtu5rE3Bw7SAySFMIgwgCoBqd8kQL4mzVCosTukspRw8V5X3aqVHqSF08zNT0L4hwiOmvJgv3jCdGShYtDy8yfuGybYnO8IfX/m5d/8XScloPTpiMLt8iqD+59OnEOUWGCg+tCCfbC2ENeamvzk7JRi8ur4zDXEFM4frURDM0LLNiYJZxC5B8Kjh2gxK/xOsL8pa9sl3jbjfgIcbKQKBgQD2Gpq3wC7AHCgSFFulHivk1t014rSR9XStsRBGxGJAV6mIkennuDLE7+rR3oYF9ISpyaoDz0G8l6Agijd7Xdb/9uvqBI5vrSP+gu3Btm04HvOx57MxZgjJR5NMSBtl1FS2iQwkAO/rEiJ0g8a2I/MSZ0ErSXQFL5tk5/8w6az4/QKBgQDPjhOggfoza2pvgH62H2w3yfvtun+awQsrKxj2dlCQXb1+tRc4YnByn1/b/R2AqQXSkNf4b1qukgXPRlYp/W2jk0DU8iw4sbjJQibo5H4PoaoEJ6EON72zjOmcEMY5FG4ZE/uX/iZcXbhnyDzA0IPUlEn1LX49xiSPcZM24qCGdwKBgQCURRsJsaN44n3RxqogJLlVOY40tM0NUtBlBNRFjnOTSD0/polBrdwIgnL61hHw7IwwrurbOLbmJIO4lw0uKi8qL12as/wMlEenQsJzfrD2qs8vU+TF0i9g9NptjtPS2cXf41mJJ3dkLJzNjcbeXXJImCTPK4XWygPZG1zH30DzEQKBgQCngIfCb2MWgEfDeLXKEQ3q8C1Kq8ozgDudOQjKTq2x9JTrXwZAUOFi/9AC4AEhRkcWqiJBssxCOkJKBv+en5IOAta9hDu//V2dvq9cIJrDzrpA5CKMMNv9vWkCtF7kzeIUhxXykf8vZ8tcpjKxgRYDRzFwlqhc/j2fLg+aqcbW/QKBgB0Tj4Xfvy5cLVhuT1+98jhRQXmpfh67FWy3AYSt06R7iaOWkn9/eLoq1MSWXe9SmMof+pG8j8SIDI2dFeyb+uZZXD1S03wwb5JZNHHcMtMU/fBRlRprN/52yKpB7Tl+qjTQRplQdPQH9m0IbaMhxElCrfHOOa5w9Ox1gUtv3bY8";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxceLePM159fjtdRD7+D2vvftx98TCkb0E9/2L8gF9YahDFf/P/QoA18rUiOyNoRSLGGuV6nBJDZQwQ67U7rWCv4BJTY0N5SU5PtiMc5oqPbNTzo7DSyN+hUilufjwwSzm63LRxFzaNsxf1q9HctMdjdm1dofwtmLWs1EEKW6uHQ0Fiz1SiKfFBbgsGNh3vgi9SZ6QL5eAljteYbJDlpSTJZf5Db6cbYAwPR11M8i7n/svCnPoE2WvIY8J5aV/p7+PAngD/oAbM/nO0JO/6/53V2RnKY5luAaCYSFYvp8wFBDNYqYV2nrnzaI9ZrABqRmXCAohKxPut1LkOJqIQifMQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url="http://saew062tmf.52http.net/api/order/pay/success";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url = null;

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}