package com.srs.common.core;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应体: {code, msg, data}
 * code 复用 HTTP 语义: 200成功 400参数错误 401未认证 403无权限 404不存在 500服务器错误
 */
@Data
@NoArgsConstructor
public class R<T> implements Serializable {

    private int code;

    private String msg;

    private T data;

    private Long timestamp = System.currentTimeMillis();

    private R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(200, "操作成功", null);
    }

    public static <T> R<T> ok(String msg) {
        return new R<>(200, msg, null);
    }

    public static <T> R<T> data(T data) {
        return new R<>(200, "操作成功", data);
    }

    public static <T> R<T> data(String msg, T data) {
        return new R<>(200, msg, data);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(500, msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public boolean isSuccess() {
        return this.code == 200;
    }
}
