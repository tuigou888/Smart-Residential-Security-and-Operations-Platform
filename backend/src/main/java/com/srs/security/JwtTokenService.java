package com.srs.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

/**
 * JWT 签发/解析: 访问令牌(短期) + 刷新令牌(长期)
 */
@Component
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;

    @Value("${srs.security.access-token-ttl:2h}")
    private String accessTokenTtl;

    @Value("${srs.security.refresh-token-ttl:24h}")
    private String refreshTokenTtl;

    public String createAccessToken(LoginUser user) {
        return build(user, "access", accessTokenTtl);
    }

    public String createRefreshToken(LoginUser user) {
        return build(user, "refresh", refreshTokenTtl);
    }

    private String build(LoginUser user, String type, String ttl) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .issuer("srs-platform")
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(parseTtl(ttl), ChronoUnit.SECONDS))
                .claim("type", type)
                .claim("userId", user.getUserId())
                .claim("realName", user.getRealName())
                .claim("deptId", user.getDeptId())
                .claim("userType", user.getUserType())
                .claim("dataScope", user.getDataScope())
                .claim("roles", user.getRoles() == null ? "" : String.join(",", user.getRoles()))
                .claim("communityIds", user.getCommunityIds() == null ? "" :
                        user.getCommunityIds().stream().map(String::valueOf).collect(Collectors.joining(",")));

        if ("access".equals(type) && user.getPermissions() != null) {
            builder.claim("authorities", user.getPermissions());
        }
        return jwtEncoder.encode(JwtEncoderParameters.from(builder.build())).getTokenValue();
    }

    /** 支持 2h / 30m / 7200s / 纯数字(秒) */
    private long parseTtl(String ttl) {
        if (ttl == null || ttl.isBlank()) {
            return 7200L;
        }
        String v = ttl.trim().toLowerCase();
        try {
            if (v.endsWith("h")) return Long.parseLong(v.substring(0, v.length() - 1)) * 3600;
            if (v.endsWith("m")) return Long.parseLong(v.substring(0, v.length() - 1)) * 60;
            if (v.endsWith("s")) return Long.parseLong(v.substring(0, v.length() - 1));
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            return 7200L;
        }
    }

    public JwtDecoder decoderHolder() {
        return null;
    }
}
