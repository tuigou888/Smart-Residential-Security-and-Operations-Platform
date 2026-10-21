package com.srs.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 获取客户端真实 IP
 */
public class IpUtils {

    private static final String[] HEADERS = {
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP"
    };

    private IpUtils() {
    }

    public static HttpServletRequest getRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getIp() {
        HttpServletRequest request = getRequest();
        return request == null ? "127.0.0.1" : getIp(request);
    }

    public static String getIp(HttpServletRequest request) {
        for (String header : HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        String ip = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ignored) {
                ip = "127.0.0.1";
            }
        }
        return ip;
    }

    /** 简单 UA 解析(不引入第三方解析库) */
    public static String parseBrowser(String ua) {
        if (ua == null) {
            return "Unknown";
        }
        String u = ua.toLowerCase();
        if (u.contains("edg/")) return "Edge";
        if (u.contains("chrome/")) return "Chrome";
        if (u.contains("firefox/")) return "Firefox";
        if (u.contains("safari/") && !u.contains("chrome")) return "Safari";
        if (u.contains("postman")) return "Postman";
        return "Other";
    }

    public static String parseOs(String ua) {
        if (ua == null) {
            return "Unknown";
        }
        String u = ua.toLowerCase();
        if (u.contains("windows")) return "Windows";
        if (u.contains("mac os x") || u.contains("macintosh")) return "MacOS";
        if (u.contains("android")) return "Android";
        if (u.contains("iphone") || u.contains("ipad")) return "iOS";
        if (u.contains("linux")) return "Linux";
        return "Other";
    }
}
