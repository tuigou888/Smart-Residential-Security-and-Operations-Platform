package com.srs.modules.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.ai.entity.AiEventRecord;
import com.srs.modules.ai.entity.AiSceneConfig;
import com.srs.modules.ai.mapper.AiEventRecordMapper;
import com.srs.modules.ai.mapper.AiSceneConfigMapper;
import com.srs.modules.sec.entity.SecAlarmEvent;
import com.srs.modules.sec.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 事件: 接收算法回调并按阈值转告警
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiEventService extends ServiceImpl<AiEventRecordMapper, AiEventRecord> {

    private final AiSceneConfigMapper sceneMapper;
    private final AlarmService alarmService;

    public IPage<AiEventRecord> page(PageQuery q, Long communityId, String eventType, Integer handleStatus) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<AiEventRecord>()
                        .eq(communityId != null, AiEventRecord::getCommunityId, communityId)
                        .eq(StringUtils.hasText(eventType), AiEventRecord::getEventType, eventType)
                        .eq(handleStatus != null, AiEventRecord::getHandleStatus, handleStatus)
                        .orderByDesc(AiEventRecord::getEventTime));
    }

    /**
     * AI 引擎回调: 事件入库, 置信度达到场景阈值时生成告警
     */
    @Transactional(rollbackFor = Exception.class)
    public AiEventRecord receive(AiEventRecord event) {
        if (event.getCommunityId() == null) {
            throw BizException.badRequest("小区ID不能为空");
        }
        if (!StringUtils.hasText(event.getEventType())) {
            throw BizException.badRequest("事件类型不能为空");
        }
        if (event.getEventTime() == null) {
            event.setEventTime(LocalDateTime.now());
        }
        if (event.getHandleStatus() == null) {
            event.setHandleStatus(0);
        }
        baseMapper.insert(event);

        AiSceneConfig scene = event.getSceneId() == null ? null : sceneMapper.selectById(event.getSceneId());
        if (scene != null && reachedThreshold(event.getConfidence(), scene.getThreshold())) {
            SecAlarmEvent alarm = new SecAlarmEvent();
            alarm.setCommunityId(event.getCommunityId());
            alarm.setChannelId(event.getChannelId());
            alarm.setSnapshotUrl(event.getSnapshotUrl());
            alarm.setAlarmType("ai_" + event.getEventType());
            alarm.setAlarmLevel(scene.getAlarmLevel() == null ? 2 : scene.getAlarmLevel());
            alarm.setSourceType(2);
            alarm.setTitle("【AI】" + scene.getSceneName());
            alarm.setContent("置信度 " + event.getConfidence() + "，事件类型 " + event.getEventType());
            alarm.setOccurTime(event.getEventTime());
            alarmService.receive(alarm);
            log.info("[ai] 事件 {} 触发告警, 场景 {}", event.getId(), scene.getSceneCode());
        }
        return event;
    }

    public void handle(Long id, Integer handleStatus) {
        AiEventRecord event = baseMapper.selectById(id);
        if (event == null) {
            throw BizException.notFound("AI 事件不存在: " + id);
        }
        event.setHandleStatus(handleStatus == null ? 1 : handleStatus);
        baseMapper.updateById(event);
    }

    public List<AiEventRecord> latest(Long communityId, int limit) {
        return baseMapper.selectList(new LambdaQueryWrapper<AiEventRecord>()
                .eq(communityId != null, AiEventRecord::getCommunityId, communityId)
                .orderByDesc(AiEventRecord::getEventTime)
                .last("limit " + Math.max(1, Math.min(limit, 100))));
    }

    private boolean reachedThreshold(BigDecimal confidence, BigDecimal threshold) {
        if (confidence == null) {
            return false;
        }
        BigDecimal target = threshold == null ? new BigDecimal("0.80") : threshold;
        return confidence.compareTo(target) >= 0;
    }
}
