package com.srs.modules.ai.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.ai.entity.AiEventRecord;
import com.srs.modules.ai.service.AiEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "AI事件")
@RestController
@RequestMapping("/api/v1/ai/event")
@RequiredArgsConstructor
public class AiEventController {

    private final AiEventService eventService;

    @Operation(summary = "AI事件列表")
    @PreAuthorize("@ss.has('ai:event:list')")
    @GetMapping("/list")
    public R<PageResult<AiEventRecord>> list(PageQuery query,
                                             @RequestParam(required = false) Long communityId,
                                             @RequestParam(required = false) String eventType,
                                             @RequestParam(required = false) Integer handleStatus) {
        return R.data(PageResult.of(eventService.page(query, communityId, eventType, handleStatus)));
    }

    @Operation(summary = "AI事件回调(算法引擎接入)")
    @PreAuthorize("@ss.has('ai:event:callback')")
    @PostMapping("/callback")
    public R<AiEventRecord> callback(@RequestBody AiEventRecord event) {
        event.setId(null);
        return R.data("事件已接收", eventService.receive(event));
    }

    @Operation(summary = "处置AI事件")
    @PreAuthorize("@ss.has('ai:event:handle')")
    @OperLog(module = "AI事件", businessType = 2)
    @PutMapping("/handle")
    public R<Void> handle(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(String.valueOf(body.get("id")));
        Integer status = body.get("handleStatus") == null ? 1 : Integer.valueOf(String.valueOf(body.get("handleStatus")));
        eventService.handle(id, status);
        return R.ok("处置成功");
    }
}
