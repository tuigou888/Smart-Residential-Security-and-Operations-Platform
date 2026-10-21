package com.srs.modules.system.controller;

import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.system.entity.SysLoginLog;
import com.srs.modules.system.entity.SysOperLog;
import com.srs.modules.system.service.SysLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "日志审计")
@RestController
@RequestMapping("/api/v1/sys/log")
@RequiredArgsConstructor
public class SysLogController {

    private final SysLogService logService;

    @Operation(summary = "操作日志列表")
    @PreAuthorize("@ss.has('sys:log:list')")
    @GetMapping("/oper/list")
    public R<PageResult<SysOperLog>> operList(PageQuery query,
                                              @RequestParam(required = false) String operName,
                                              @RequestParam(required = false) String module,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime begin,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        return R.data(PageResult.of(logService.pageOperLogs(query, operName, module, status, begin, end)));
    }

    @Operation(summary = "登录日志列表")
    @PreAuthorize("@ss.has('sys:log:list')")
    @GetMapping("/login/list")
    public R<PageResult<SysLoginLog>> loginList(PageQuery query,
                                                @RequestParam(required = false) String username,
                                                @RequestParam(required = false) Integer status) {
        return R.data(PageResult.of(logService.pageLoginLogs(query, username, status)));
    }

    @Operation(summary = "清空操作日志")
    @PreAuthorize("@ss.has('sys:log:remove')")
    @DeleteMapping("/oper/clean")
    public R<Void> clean() {
        logService.cleanOperLogs();
        return R.ok("操作日志已清空");
    }
}
