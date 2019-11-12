package com.atguigu.gmall.auth.config;

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
    /*
    *  publicKeyPath: D:\\develop_code\\tmp\\rsa.pub
        privateKeyPath: D:\\develop_code\\tmp\\rsa.pri
        expire: 180 #单位分钟
        cookieName: GMALL_TOKEN
        secret: s159ho12qhggn
    * */
    private String publicKeyPath;
    private String privateKeyPath;
    private int expire;
    private String cookieName;
    private String secret;
    private PublicKey publicKey;//公钥对象
    private PrivateKey privateKey;//私钥对象

    @PostConstruct
    public void inti(){
        try {
            //1.初始化公钥私钥文件
            File publicFile = new File(publicKeyPath);
            File privateFile = new File(privateKeyPath);
            //2.检查文件是否为空,判断文件是否存在
            if(!publicFile.exists()||!privateFile.exists()){
                RsaUtils.generateKey(publicKeyPath,privateKeyPath,secret);
            }
            //3.读取秘钥
            publicKey = RsaUtils.getPublicKey(publicKeyPath);//生成公钥对象
             privateKey = RsaUtils.getPrivateKey(privateKeyPath);//生成私钥对象


        } catch (Exception e) {
            log.error("初始化公钥和私钥失败");
            e.printStackTrace();
        }

    }

}
