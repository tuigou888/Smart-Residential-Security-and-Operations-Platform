package com.srs.security;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.JWKSet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.interfaces.RSAPublicKey;

/**
 * JWT(RS256) 密钥管理: 优先从文件加载, 不存在时自动生成并持久化, 避免重启后令牌失效
 */
@Slf4j
@Configuration
public class JwtKeyConfig {

    @Value("${srs.security.jwk-path:./data/jwt-jwk.json}")
    private String jwkPath;

    @Bean
    public RSAKey rsaKey() throws Exception {
        Path path = Paths.get(jwkPath);
        if (Files.exists(path)) {
            try {
                return RSAKey.parse(new String(Files.readAllBytes(path)));
            } catch (Exception e) {
                log.warn("[jwt] 密钥文件解析失败, 将重新生成: {}", e.getMessage());
            }
        }
        RSAKey key = new RSAKeyGenerator(2048).keyID("srs-key").generate();
        Files.createDirectories(path.getParent() == null ? Paths.get(".") : path.getParent());
        Files.writeString(path, key.toJSONString());
        log.info("[jwt] 已生成新的 RS256 密钥对: {}", path.toAbsolutePath());
        return key;
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaKey) {
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) throws Exception {
        RSAPublicKey publicKey = rsaKey.toPublicJWK().toRSAKey().toRSAPublicKey();
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
}
