package com.srs.common.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 敏感数据加解密(手机号/身份证等): AES/ECB/PKCS5Padding + HEX
 * 解密失败时降级返回原值, 兼容历史明文数据, 不影响业务读取。
 */
@Slf4j
@Component
public class AesCrypto {

    private final boolean enabled;
    private final AES aes;

    public AesCrypto(@Value("${srs.crypto.enabled:true}") boolean enabled,
                     @Value("${srs.crypto.aes-key}") String key) {
        this.enabled = enabled;
        this.aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String encrypt(String plain) {
        if (plain == null || !enabled) {
            return plain;
        }
        return aes.encryptHex(plain);
    }

    public String decrypt(String cipher) {
        if (cipher == null || !enabled) {
            return cipher;
        }
        try {
            return aes.decryptStr(cipher);
        } catch (Exception e) {
            log.warn("[crypto] 解密失败, 按明文返回: {}", cipher);
            return cipher;
        }
    }

    /** 手机号脱敏: 138****8001 */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 身份证脱敏: 440***********1234 */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
    }

    /** 姓名脱敏: 陈*国 */
    public static String maskName(String name) {
        if (name == null || name.length() < 2) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        StringBuilder sb = new StringBuilder(name);
        for (int i = 1; i < name.length() - 1; i++) {
            sb.setCharAt(i, '*');
        }
        return sb.toString();
    }
}
