package com.srs.modules.workorder.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.workorder.entity.BizWorkOrder;
import com.srs.modules.workorder.entity.BizWorkOrderFlow;
import com.srs.modules.workorder.mapper.BizWorkOrderFlowMapper;
import com.srs.modules.workorder.service.WorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "工单中心")
@RestController
@RequestMapping("/api/v1/workorder")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final BizWorkOrderFlowMapper flowMapper;

    @Operation(summary = "工单列表")
    @PreAuthorize("@ss.has('workorder:list')")
    @GetMapping("/list")
    public R<PageResult<BizWorkOrder>> list(PageQuery query,
                                            @RequestParam(required = false) Long communityId,
                                            @RequestParam(required = false) String orderType,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) Integer priority,
                                            @RequestParam(required = false) Long assigneeId,
                                            @RequestParam(required = false) String title) {
        return R.data(PageResult.of(workOrderService.pageOrders(query, communityId, orderType, status,
                priority, assigneeId, title)));
    }

    @Operation(summary = "工单详情(含流转记录)")
    @PreAuthorize("@ss.has('workorder:query')")
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.data(workOrderService.detail(id));
    }

    @Operation(summary = "我的工单")
    @PreAuthorize("@ss.has('workorder:list')")
    @GetMapping("/my")
    public R<List<BizWorkOrder>> my(@RequestParam Long assigneeId) {
        return R.data(workOrderService.myOrders(assigneeId));
    }

    @Operation(summary = "创建工单")
    @PreAuthorize("@ss.has('workorder:add')")
    @OperLog(module = "工单中心", businessType = 1)
    @PostMapping
    public R<Map<String, Object>> create(@RequestBody BizWorkOrder order) {
        return R.data(Map.of("orderId", workOrderService.create(order)));
    }

    @Operation(summary = "派单")
    @PreAuthorize("@ss.has('workorder:assign')")
    @OperLog(module = "工单中心", businessType = 2)
    @PutMapping("/assign")
    public R<Void> assign(@RequestBody ActionBody body) {
        workOrderService.assign(body.getOrderId(), body.getAssigneeId(), body.getAssigneeName());
        return R.ok("派单成功");
    }

    @Operation(summary = "接单")
    @PreAuthorize("@ss.has('workorder:accept')")
    @OperLog(module = "工单中心", businessType = 2)
    @PutMapping("/accept")
    public R<Void> accept(@RequestBody ActionBody body) {
        workOrderService.accept(body.getOrderId(), body.getDetail());
        return R.ok("已接单");
    }

    @Operation(summary = "处理工单")
    @PreAuthorize("@ss.has('workorder:handle')")
    @OperLog(module = "工单中心", businessType = 2)
    @PutMapping("/handle")
    public R<Void> handle(@RequestBody ActionBody body) {
        workOrderService.handle(body.getOrderId(), body.getDetail(), body.getAttachments());
        return R.ok("已记录处理");
    }

    @Operation(summary = "处理完成(提交验收)")
    @PreAuthorize("@ss.has('workorder:handle')")
    @OperLog(module = "工单中心", businessType = 2)
    @PutMapping("/finish")
    public R<Void> finish(@RequestBody ActionBody body) {
        workOrderService.finish(body.getOrderId(), body.getDetail(), body.getAttachments());
        return R.ok("已提交验收");
    }

    @Operation(summary = "验收")
    @PreAuthorize("@ss.has('workorder:verify')")
    @OperLog(module = "工单中心", businessType = 2)
    @PutMapping("/verify")
    public R<Void> verify(@RequestBody ActionBody body) {
        workOrderService.verify(body.getOrderId(), body.isPass(), body.getDetail());
        return R.ok("验收已提交");
    }

    @Operation(summary = "评价")
    @PreAuthorize("@ss.has('workorder:evaluate')")
    @OperLog(module = "工单中心", businessType = 2)
    @PutMapping("/evaluate")
    public R<Void> evaluate(@RequestBody ActionBody body) {
        workOrderService.evaluate(body.getOrderId(), body.getScore(), body.getDetail());
        return R.ok("评价成功");
    }

    @Operation(summary = "关闭工单")
    @PreAuthorize("@ss.has('workorder:close')")
    @OperLog(module = "工单中心", businessType = 2)
    @PutMapping("/close")
    public R<Void> close(@RequestBody ActionBody body) {
        workOrderService.close(body.getOrderId(), body.getDetail());
        return R.ok("工单已关闭");
    }

    @Operation(summary = "工单流转记录")
    @PreAuthorize("@ss.has('workorder:query')")
    @GetMapping("/flow/{orderId}")
    public R<List<BizWorkOrderFlow>> flows(@PathVariable Long orderId) {
        return R.data(flowMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizWorkOrderFlow>()
                        .eq(BizWorkOrderFlow::getOrderId, orderId)
                        .orderByAsc(BizWorkOrderFlow::getCreateTime)));
    }

    @Operation(summary = "工单统计")
    @PreAuthorize("@ss.has('workorder:stat:view')")
    @GetMapping("/stat/summary")
    public R<Map<String, Object>> stat(@RequestParam(required = false) Long communityId) {
        return R.data(workOrderService.summary(communityId));
    }

    @Data
    public static class ActionBody {
        private Long orderId;
        private Long assigneeId;
        private String assigneeName;
        private String detail;
        private String attachments;
        private boolean pass;
        private Integer score;
    }
}
