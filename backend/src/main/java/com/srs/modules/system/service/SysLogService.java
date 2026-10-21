package com.srs.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.util.IpUtils;
import com.srs.modules.system.entity.SysLoginLog;
import com.srs.modules.system.entity.SysOperLog;
import com.srs.modules.system.mapper.SysLoginLogMapper;
import com.srs.modules.system.mapper.SysOperLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysLogService extends ServiceImpl<SysOperLogMapper, SysOperLog> {

    private final SysLoginLogMapper loginLogMapper;

    public IPage<SysOperLog> pageOperLogs(PageQuery q, String operName, String module, Integer status,
                                          LocalDateTime begin, LocalDateTime end) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<SysOperLog>()
                        .like(StringUtils.hasText(operName), SysOperLog::getOperName, operName)
                        .eq(StringUtils.hasText(module), SysOperLog::getModule, module)
                        .eq(status != null, SysOperLog::getStatus, status)
                        .ge(begin != null, SysOperLog::getOperTime, begin)
                        .le(end != null, SysOperLog::getOperTime, end)
                        .orderByDesc(SysOperLog::getOperTime));
    }

    public IPage<SysLoginLog> pageLoginLogs(PageQuery q, String username, Integer status) {
        return loginLogMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<SysLoginLog>()
                        .like(StringUtils.hasText(username), SysLoginLog::getUsername, username)
                        .eq(status != null, SysLoginLog::getStatus, status)
                        .orderByDesc(SysLoginLog::getLoginTime));
    }

    public void cleanOperLogs() {
        baseMapper.delete(new LambdaQueryWrapper<>());
    }

    /** 记录登录日志(成功/失败) */
    public void recordLogin(String username, boolean success, String msg, HttpServletRequest request) {
        try {
            SysLoginLog log = new SysLoginLog();
            log.setUsername(username);
            log.setStatus(success ? 1 : 0);
            log.setMsg(msg);
            log.setLoginTime(LocalDateTime.now());
            if (request != null) {
                String ua = request.getHeader("User-Agent");
                log.setLoginIp(IpUtils.getIp(request));
                log.setBrowser(IpUtils.parseBrowser(ua));
                log.setOs(IpUtils.parseOs(ua));
            }
            loginLogMapper.insert(log);
        } catch (Exception e) {
            // 审计日志写入失败不影响主流程
        }
    }
}
