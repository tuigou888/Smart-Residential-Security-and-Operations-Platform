package com.srs.modules.repair.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.repair.entity.BizRepair;
import com.srs.modules.repair.service.RepairService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "报修投诉")
@RestController
@RequestMapping("/api/v1/repair")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;

    @Operation(summary = "报修列表")
    @PreAuthorize("@ss.has('repair:list')")
    @GetMapping("/list")
    public R<PageResult<BizRepair>> list(PageQuery query,
                                         @RequestParam(required = false) Long communityId,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(required = false) Integer repairType,
                                         @RequestParam(required = false) String keyword) {
        return R.data(PageResult.of(repairService.page(query, communityId, status, repairType, keyword)));
    }

    @Operation(summary = "报修详情")
    @PreAuthorize("@ss.has('repair:query')")
    @GetMapping("/{id}")
    public R<BizRepair> detail(@PathVariable Long id) {
        return R.data(repairService.detail(id));
    }

    @Operation(summary = "新增报修")
    @PreAuthorize("@ss.has('repair:add')")
    @OperLog(module = "报修管理", businessType = 1)
    @PostMapping
    public R<Long> create(@RequestBody BizRepair repair) {
        repair.setId(null);
        return R.data("提交成功", repairService.createRepair(repair));
    }

    @Operation(summary = "报修派单")
    @PreAuthorize("@ss.has('repair:handle')")
    @OperLog(module = "报修管理", businessType = 2)
    @PostMapping("/dispatch")
    public R<Long> dispatch(@RequestBody DispatchBody body) {
        return R.data("派单成功", repairService.dispatch(body.id(), body.assigneeId(), body.assigneeName()));
    }

    @Operation(summary = "开始处理")
    @PreAuthorize("@ss.has('repair:handle')")
    @OperLog(module = "报修管理", businessType = 2)
    @PutMapping("/start")
    public R<Void> start(@RequestBody Map<String, Object> body) {
        repairService.start(Long.valueOf(String.valueOf(body.get("id"))));
        return R.ok("已开始处理");
    }

    @Operation(summary = "完成报修")
    @PreAuthorize("@ss.has('repair:handle')")
    @OperLog(module = "报修管理", businessType = 2)
    @PutMapping("/finish")
    public R<Void> finish(@RequestBody Map<String, Object> body) {
        repairService.finish(Long.valueOf(String.valueOf(body.get("id"))),
                body.get("remark") == null ? null : String.valueOf(body.get("remark")));
        return R.ok("已完成");
    }

    @Operation(summary = "评价报修")
    @PreAuthorize("@ss.has('repair:handle')")
    @OperLog(module = "报修管理", businessType = 2)
    @PutMapping("/evaluate")
    public R<Void> evaluate(@RequestBody Map<String, Object> body) {
        repairService.evaluate(Long.valueOf(String.valueOf(body.get("id"))),
                body.get("content") == null ? null : String.valueOf(body.get("content")));
        return R.ok("评价成功");
    }

    /** 派单请求 */
    public record DispatchBody(Long id, Long assigneeId, String assigneeName) {
    }
}
