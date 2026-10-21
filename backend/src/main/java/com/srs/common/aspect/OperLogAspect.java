package com.srs.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srs.common.anno.OperLog;
import com.srs.common.util.IpUtils;
import com.srs.modules.system.entity.SysOperLog;
import com.srs.modules.system.mapper.SysOperLogMapper;
import com.srs.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志切面(审计要求: 操作留痕, 保存 ≥6 个月)
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final SysOperLogMapper operLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint point, OperLog operLog) throws Throwable {
        long start = System.currentTimeMillis();
        SysOperLog entity = new SysOperLog();
        entity.setModule(operLog.module());
        entity.setBusinessType(operLog.businessType());
        entity.setOperName(SecurityUtils.getUsername());
        entity.setOperTime(LocalDateTime.now());

        HttpServletRequest request = IpUtils.getRequest();
        if (request != null) {
            entity.setRequestMethod(request.getMethod());
            entity.setOperUrl(request.getRequestURI());
            entity.setOperIp(IpUtils.getIp(request));
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        entity.setMethod(signature.getDeclaringType().getName() + "." + signature.getName());
        entity.setOperParam(truncate(toJson(point.getArgs())));

        try {
            Object result = point.proceed();
            entity.setStatus(1);
            entity.setJsonResult(truncate(toJson(result)));
            return result;
        } catch (Throwable e) {
            entity.setStatus(0);
            entity.setErrorMsg(truncate(e.getMessage()));
            throw e;
        } finally {
            entity.setJsonResult(entity.getJsonResult() + " (" + (System.currentTimeMillis() - start) + "ms)");
            try {
                operLogMapper.insert(entity);
            } catch (Exception ex) {
                log.warn("[operlog] 写入操作日志失败: {}", ex.getMessage());
            }
        }
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return "";
        }
        try {
            if (obj instanceof Object[] arr) {
                return objectMapper.writeValueAsString(Arrays.stream(arr)
                        .filter(o -> !(o instanceof jakarta.servlet.http.HttpServletRequest)
                                && !(o instanceof jakarta.servlet.http.HttpServletResponse))
                        .toList());
            }
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    private String truncate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > 2000 ? s.substring(0, 2000) : s;
    }
}
