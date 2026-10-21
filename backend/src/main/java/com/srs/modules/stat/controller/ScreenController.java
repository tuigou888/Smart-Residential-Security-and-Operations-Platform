package com.srs.modules.stat.controller;

import com.srs.common.core.R;
import com.srs.modules.sec.entity.SecAlarmEvent;
import com.srs.modules.sec.service.AlarmService;
import com.srs.modules.stat.service.StatService;
import com.srs.modules.video.entity.VsChannel;
import com.srs.modules.video.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "运营大屏")
@RestController
@RequestMapping("/api/v1/screen")
@RequiredArgsConstructor
public class ScreenController {

    private final StatService statService;
    private final AlarmService alarmService;
    private final VideoService videoService;

    @Operation(summary = "大屏实时数据")
    @PreAuthorize("@ss.has('screen:view')")
    @GetMapping("/realtime")
    public R<Map<String, Object>> realtime(@RequestParam(required = false) Long communityId) {
        Map<String, Object> data = new LinkedHashMap<>(statService.overview(communityId));
        data.put("safetyIndex", statService.safetyIndex(communityId));
        data.put("alarmTrend", statService.alarmTrend(communityId, 7));
        data.put("accessTrend", statService.accessTrend(communityId, 7));
        return R.data(data);
    }

    @Operation(summary = "最新告警")
    @PreAuthorize("@ss.has('screen:view')")
    @GetMapping("/alarm/latest")
    public R<List<SecAlarmEvent>> latestAlarm(@RequestParam(required = false) Long communityId,
                                              @RequestParam(defaultValue = "10") int limit) {
        return R.data(alarmService.latest(communityId, limit));
    }

    @Operation(summary = "设备状态")
    @PreAuthorize("@ss.has('screen:view')")
    @GetMapping("/device/status")
    public R<Map<String, Object>> deviceStatus(@RequestParam(required = false) Long communityId) {
        return R.data(statService.deviceStat(communityId));
    }

    @Operation(summary = "视频轮巡列表")
    @PreAuthorize("@ss.has('screen:view')")
    @GetMapping("/video/rotation")
    public R<Map<String, Object>> rotation(@RequestParam(required = false) Long communityId,
                                           @RequestParam(defaultValue = "4") int size) {
        List<VsChannel> channels = videoService.pageChannels(
                        new com.srs.common.core.PageQuery() {{ setPageNum(1L); setPageSize((long) size); }},
                        communityId, null, 1).getRecords();
        List<Map<String, Object>> list = channels.stream()
                .map(c -> videoService.liveUrl(c.getId()))
                .toList();
        return R.data(Map.of("channels", list));
    }
}
