package com.srs.common.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 业务单号生成器: 前缀 + yyyyMMddHHmmss + 4位随机
 * 例: AL20260910120000
 */
@Component
public class NoGenerator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static String next(String prefix) {
        int rand = (int) (Math.random() * 9000) + 1000;
        return prefix + LocalDateTime.now().format(FMT) + rand;
    }

    public static String alarmNo() {
        return next("AL");
    }

    public static String orderNo() {
        return next("WO");
    }

    public static String inviteNo() {
        return next("V");
    }

    public static String repairNo() {
        return next("RP");
    }

    public static String deviceCode(String type) {
        return type.toUpperCase() + "-" + System.currentTimeMillis() % 1000000;
    }
}
