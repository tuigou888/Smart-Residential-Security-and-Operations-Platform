package com.srs.modules.stat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 日汇总定时任务: 每日 00:10 生成前一日全部小区汇总
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailySummaryJob {

    private final DailySummaryService summaryService;

    @Scheduled(cron = "0 10 0 * * ?")
    public void generateYesterday() {
        try {
            int count = summaryService.generateAll(LocalDate.now().minusDays(1));
            log.info("[stat] 日汇总生成完成, 小区数 {}", count);
        } catch (Exception e) {
            log.error("[stat] 日汇总生成失败", e);
        }
    }
}
