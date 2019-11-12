package com.atgui.gmall.gateway.config;

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
public class JwtProperties {

    private String publicKeyPath;
    private String cookieName;
    private PublicKey publicKey;//公钥对象

    @PostConstruct
    public void inti(){
        try {

            //3.读取秘钥
            publicKey = RsaUtils.getPublicKey(publicKeyPath);//生成公钥对象

        } catch (Exception e) {
            log.error("初始化公钥和私钥失败");
            e.printStackTrace();
        }

    }

}

