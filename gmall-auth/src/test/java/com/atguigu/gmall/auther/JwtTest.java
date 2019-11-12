package com.atguigu.gmall.auther;

import com.atguigu.gmall.core.utils.JwtUtils;
import com.atguigu.gmall.core.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;


import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
@SpringBootTest
public class JwtTest {
	private static final String pubKeyPath = "D:\\develop_code\\tmp\\rsa.pub";

    private static final String priKeyPath = "D:\\develop_code\\tmp\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "45q5e445q544f5a");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1NzM1NTAwNjR9.ih2WKw1R-0TML-XWT5D5iUscwnDSLbbjJSRSgvSK0YGZfBffK3WwdIk_eSvEp7GoPWpB9SVyuHqtqo0E8i11xKM8SVbdbFGOsaf6IzNEVmJf3_S6ny1FdzmutUNrAtamnAZ_6v-GNdO_qqxr__a2HILOrNQulPIfckfP6A7Nd1ZdLMI6OqputIr42CGxnEmKjteNIVd9Y7ORW2JVShROONiuLoSXFHFfECJpjZE6zdHJyVB6dw0dRPGbd9nfP3pNRzZppyeWD7R20FuBP1gOf3iwoioroj9-KpyfrE_C518fZFUeXLGEBnhruJ-IpH80rwe0yKojRiSk3KnNm8VApw";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}