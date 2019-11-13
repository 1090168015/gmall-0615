package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.core.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
@Slf4j
@Data
@ConfigurationProperties(prefix = "auth.jwt")
public class Jwtproperties {
    /*
   *  publicKeyPath: D:\\develop_code\\tmp\\rsa.pub
       privateKeyPath: D:\\develop_code\\tmp\\rsa.pri
       expire: 180 #单位分钟
       cookieName: GMALL_TOKEN
       secret: s159ho12qhggn
   * */
    private String publicKeyPath;//公钥路径
    private String cookieName;
    private PublicKey publicKey;//公钥对象
    private String userKeyName;
    private Integer expire;//过期时间
    @PostConstruct
    public void inti(){
        try {
            // 3. 读取密钥
            publicKey = RsaUtils.getPublicKey(publicKeyPath);//根据公钥路径生成公钥对象

        } catch (Exception e) {
            log.error("初始化公钥和私钥失败");
            e.printStackTrace();
        }
    }
}
