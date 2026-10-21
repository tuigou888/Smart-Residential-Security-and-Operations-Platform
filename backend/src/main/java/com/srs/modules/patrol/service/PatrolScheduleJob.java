package com.srs.modules.patrol.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 巡更定时任务: 每日 00:05 判定前一日漏巡
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PatrolScheduleJob {

    private final PatRecordService recordService;

    @Scheduled(cron = "0 5 0 * * ?")
    public void detectMissing() {
        try {
            int count = recordService.markMissing(LocalDate.now().minusDays(1));
            log.info("[patrol] 每日漏巡判定完成, 新增 {} 条", count);
        } catch (Exception e) {
            log.error("[patrol] 漏巡判定失败", e);
        }
    }
}
