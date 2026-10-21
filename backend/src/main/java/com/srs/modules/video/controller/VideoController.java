package com.srs.modules.video.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.video.entity.VsChannel;
import com.srs.modules.video.entity.VsRecordPlan;
import com.srs.modules.video.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "视频监控")
@RestController
@RequestMapping("/api/v1/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @Operation(summary = "通道列表")
    @PreAuthorize("@ss.has('video:channel:list')")
    @GetMapping("/channel/list")
    public R<PageResult<VsChannel>> channelList(PageQuery query,
                                                @RequestParam(required = false) Long communityId,
                                                @RequestParam(required = false) String channelName,
                                                @RequestParam(required = false) Integer status) {
        return R.data(PageResult.of(videoService.pageChannels(query, communityId, channelName, status)));
    }

    @Operation(summary = "通道树")
    @PreAuthorize("@ss.has('video:channel:list')")
    @GetMapping("/channel/tree")
    public R<List<Map<String, Object>>> channelTree(@RequestParam(required = false) Long communityId) {
        return R.data(videoService.channelTree(communityId));
    }

    @Operation(summary = "新增/修改通道")
    @PreAuthorize("@ss.has('video:channel:add')")
    @OperLog(module = "视频监控", businessType = 1)
    @PostMapping("/channel")
    public R<Void> saveChannel(@RequestBody VsChannel entity) {
        videoService.saveChannel(entity);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除通道")
    @PreAuthorize("@ss.has('video:channel:remove')")
    @OperLog(module = "视频监控", businessType = 3)
    @DeleteMapping("/channel/{ids}")
    public R<Void> removeChannel(@PathVariable List<Long> ids) {
        videoService.removeChannels(ids);
        return R.ok("删除成功");
    }

    @Operation(summary = "获取实时流地址")
    @PreAuthorize("@ss.has('video:live:view')")
    @GetMapping("/live/{channelId}")
    public R<Map<String, Object>> live(@PathVariable Long channelId) {
        return R.data(videoService.liveUrl(channelId));
    }

    @Operation(summary = "获取回放流地址")
    @PreAuthorize("@ss.has('video:playback:view')")
    @GetMapping("/playback/{channelId}")
    public R<Map<String, Object>> playback(@PathVariable Long channelId,
                                           @RequestParam(required = false) String start,
                                           @RequestParam(required = false) String end) {
        return R.data(videoService.playbackUrl(channelId, start, end));
    }

    @Operation(summary = "云台控制")
    @PreAuthorize("@ss.has('video:ptz:control')")
    @OperLog(module = "视频监控", businessType = 2)
    @PostMapping("/ptz/control")
    public R<Void> ptz(@RequestBody Map<String, Object> body) {
        videoService.ptzControl(Long.valueOf(String.valueOf(body.get("channelId"))),
                (String) body.get("direction"),
                body.get("speed") == null ? 4 : Integer.valueOf(String.valueOf(body.get("speed"))));
        return R.ok("云台指令已下发");
    }

    @Operation(summary = "视频截图")
    @PreAuthorize("@ss.has('video:snapshot:capture')")
    @PostMapping("/snapshot/{channelId}")
    public R<Map<String, Object>> snapshot(@PathVariable Long channelId) {
        return R.data(videoService.snapshot(channelId));
    }

    @Operation(summary = "录像计划列表")
    @PreAuthorize("@ss.has('video:record:list')")
    @GetMapping("/record/list")
    public R<List<VsRecordPlan>> recordPlans() {
        return R.data(videoService.recordPlans());
    }

    @Operation(summary = "新增/修改录像计划")
    @PreAuthorize("@ss.has('video:channel:edit')")
    @PostMapping("/record/plan")
    public R<Void> savePlan(@RequestBody VsRecordPlan plan) {
        videoService.saveRecordPlan(plan);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除录像计划")
    @PreAuthorize("@ss.has('video:channel:remove')")
    @DeleteMapping("/record/plan/{id}")
    public R<Void> removePlan(@PathVariable Long id) {
        videoService.removeRecordPlan(id);
        return R.ok("删除成功");
    }
}
