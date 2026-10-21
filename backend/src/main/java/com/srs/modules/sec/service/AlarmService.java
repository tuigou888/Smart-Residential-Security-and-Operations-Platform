package com.srs.modules.sec.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.common.util.NoGenerator;
import com.srs.modules.sec.entity.SecAlarmEvent;
import com.srs.modules.sec.entity.SecAlarmHandle;
import com.srs.modules.sec.entity.SecAlarmRule;
import com.srs.modules.sec.mapper.SecAlarmEventMapper;
import com.srs.modules.sec.mapper.SecAlarmHandleMapper;
import com.srs.modules.sec.mapper.SecAlarmRuleMapper;
import com.srs.modules.workorder.service.WorkOrderService;
import com.srs.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 告警中心: 多源接入 -> 规则匹配 -> 分级派发 -> 处置闭环
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService extends ServiceImpl<SecAlarmEventMapper, SecAlarmEvent> {

    private final SecAlarmHandleMapper handleMapper;
    private final SecAlarmRuleMapper ruleMapper;
    private final WorkOrderService workOrderService;

    @Value("${srs.alarm.auto-dispatch:false}")
    private boolean autoDispatchEnabled;

    public IPage<SecAlarmEvent> pageAlarms(PageQuery q, Long communityId, String alarmType, Integer alarmLevel,
                                           Integer status, Integer sourceType, LocalDateTime begin,
                                           LocalDateTime end) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<SecAlarmEvent>()
                        .eq(communityId != null, SecAlarmEvent::getCommunityId, communityId)
                        .eq(StringUtils.hasText(alarmType), SecAlarmEvent::getAlarmType, alarmType)
                        .eq(alarmLevel != null, SecAlarmEvent::getAlarmLevel, alarmLevel)
                        .eq(status != null, SecAlarmEvent::getStatus, status)
                        .eq(sourceType != null, SecAlarmEvent::getSourceType, sourceType)
                        .ge(begin != null, SecAlarmEvent::getOccurTime, begin)
                        .le(end != null, SecAlarmEvent::getOccurTime, end)
                        .orderByDesc(SecAlarmEvent::getOccurTime));
    }

    public Map<String, Object> detail(Long id) {
        SecAlarmEvent event = baseMapper.selectById(id);
        if (event == null) {
            throw BizException.notFound("告警不存在: " + id);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("event", event);
        data.put("handles", handleMapper.selectList(new LambdaQueryWrapper<SecAlarmHandle>()
                .eq(SecAlarmHandle::getAlarmId, id)
                .orderByDesc(SecAlarmHandle::getHandleTime)));
        return data;
    }

    /**
     * 告警接入(设备/AI/人工): 生成告警 -> 规则匹配 -> 按规则自动派单
     */
    @Transactional(rollbackFor = Exception.class)
    public SecAlarmEvent receive(SecAlarmEvent event) {
        if (event.getAlarmNo() == null || event.getAlarmNo().isBlank()) {
            event.setAlarmNo(NoGenerator.alarmNo());
        }
        if (event.getStatus() == null) {
            event.setStatus(0);
        }
        if (event.getOccurTime() == null) {
            event.setOccurTime(LocalDateTime.now());
        }
        SecAlarmRule rule = matchRule(event.getCommunityId(), event.getAlarmType());
        if (rule != null) {
            if (rule.getAlarmLevel() != null) {
                event.setAlarmLevel(rule.getAlarmLevel());
            }
        }
        baseMapper.insert(event);

        boolean auto = (rule != null && rule.getAutoDispatch() != null && rule.getAutoDispatch() == 1)
                || autoDispatchEnabled;
        if (auto) {
            int slaMinutes = rule == null || rule.getSlaMinutes() == null ? 60 : rule.getSlaMinutes();
            Long orderId = workOrderService.createFromAlarm(event, slaMinutes);
            log.info("[alarm] 告警 {} 自动派单, 工单 {}", event.getAlarmNo(), orderId);
        }
        return event;
    }

    private SecAlarmRule matchRule(Long communityId, String alarmType) {
        if (alarmType == null) {
            return null;
        }
        SecAlarmRule rule = ruleMapper.selectOne(new LambdaQueryWrapper<SecAlarmRule>()
                .eq(SecAlarmRule::getAlarmType, alarmType)
                .and(communityId != null, w -> w.eq(SecAlarmRule::getCommunityId, communityId)
                        .or().isNull(SecAlarmRule::getCommunityId))
                .eq(SecAlarmRule::getStatus, 1)
                .last("limit 1"));
        if (rule == null) {
            rule = ruleMapper.selectOne(new LambdaQueryWrapper<SecAlarmRule>()
                    .eq(SecAlarmRule::getAlarmType, alarmType)
                    .isNull(SecAlarmRule::getCommunityId)
                    .eq(SecAlarmRule::getStatus, 1)
                    .last("limit 1"));
        }
        return rule;
    }

    /** 处置告警: 记录处置流水并更新告警状态 */
    @Transactional(rollbackFor = Exception.class)
    public void handle(Long alarmId, Integer handleType, String handleResult, String attachments,
                       boolean finish) {
        SecAlarmEvent event = baseMapper.selectById(alarmId);
        if (event == null) {
            throw BizException.notFound("告警不存在: " + alarmId);
        }
        SecAlarmHandle handle = new SecAlarmHandle();
        handle.setAlarmId(alarmId);
        handle.setHandleType(handleType == null ? 3 : handleType);
        handle.setHandlerId(SecurityUtils.getUserId());
        handle.setHandlerName(SecurityUtils.getRealName());
        handle.setHandleResult(handleResult);
        handle.setHandleTime(LocalDateTime.now());
        handle.setAttachments(attachments);
        handleMapper.insert(handle);

        event.setStatus(finish ? 2 : 1);
        event.setHandlerId(SecurityUtils.getUserId());
        event.setHandlerName(SecurityUtils.getRealName());
        if (finish) {
            event.setHandleTime(LocalDateTime.now());
        }
        baseMapper.updateById(event);
    }

    /** 标记误报 */
    @Transactional(rollbackFor = Exception.class)
    public void markFalse(Long alarmId, String reason) {
        SecAlarmEvent event = baseMapper.selectById(alarmId);
        if (event == null) {
            throw BizException.notFound("告警不存在: " + alarmId);
        }
        SecAlarmHandle handle = new SecAlarmHandle();
        handle.setAlarmId(alarmId);
        handle.setHandleType(4);
        handle.setHandlerId(SecurityUtils.getUserId());
        handle.setHandlerName(SecurityUtils.getRealName());
        handle.setHandleResult(StringUtils.hasText(reason) ? reason : "复核为误报");
        handle.setHandleTime(LocalDateTime.now());
        handleMapper.insert(handle);

        event.setStatus(3);
        event.setHandlerId(SecurityUtils.getUserId());
        event.setHandlerName(SecurityUtils.getRealName());
        event.setHandleTime(LocalDateTime.now());
        baseMapper.updateById(event);
    }

    /** 告警统计: 今日/未处理/已处理/按级别 */
    public Map<String, Object> summary(Long communityId) {
        LocalDate today = LocalDate.now();
        LocalDateTime begin = today.atStartOfDay();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("todayTotal", count(communityId, begin, null, null));
        data.put("pending", count(communityId, null, null, 0));
        data.put("handling", count(communityId, null, null, 1));
        data.put("handled", count(communityId, begin, null, 2));
        data.put("falseAlarm", count(communityId, begin, null, 3));
        data.put("level1", count(communityId, begin, 1, null));
        data.put("level2", count(communityId, begin, 2, null));
        data.put("level3", count(communityId, begin, 3, null));
        return data;
    }

    private long count(Long communityId, LocalDateTime begin, Integer level, Integer status) {
        return baseMapper.selectCount(new LambdaQueryWrapper<SecAlarmEvent>()
                .eq(communityId != null, SecAlarmEvent::getCommunityId, communityId)
                .ge(begin != null, SecAlarmEvent::getOccurTime, begin)
                .eq(level != null, SecAlarmEvent::getAlarmLevel, level)
                .eq(status != null, SecAlarmEvent::getStatus, status));
    }

    /** 近 N 天告警趋势 */
    public Map<String, Object> trend(Long communityId, int days) {
        List<String> dates = new ArrayList<>();
        List<Long> totals = new ArrayList<>();
        List<Long> handled = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.toString());
            totals.add(countBetween(communityId, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), null));
            handled.add(countBetween(communityId, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), 2));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dates", dates);
        data.put("totals", totals);
        data.put("handled", handled);
        return data;
    }

    private long countBetween(Long communityId, LocalDateTime begin, LocalDateTime end, Integer status) {
        return baseMapper.selectCount(new LambdaQueryWrapper<SecAlarmEvent>()
                .eq(communityId != null, SecAlarmEvent::getCommunityId, communityId)
                .ge(SecAlarmEvent::getOccurTime, begin)
                .lt(SecAlarmEvent::getOccurTime, end)
                .eq(status != null, SecAlarmEvent::getStatus, status));
    }

    /** 按类型分布 */
    public List<Map<String, Object>> typeDistribution(Long communityId) {
        List<Object[]> rows = baseMapper.selectObjs(new LambdaQueryWrapper<SecAlarmEvent>()
                        .select(SecAlarmEvent::getAlarmType)
                        .eq(communityId != null, SecAlarmEvent::getCommunityId, communityId))
                .stream().map(o -> new Object[]{o}).toList();
        Map<String, Long> counter = new LinkedHashMap<>();
        for (Object[] row : rows) {
            if (row[0] != null) {
                counter.merge(String.valueOf(row[0]), 1L, Long::sum);
            }
        }
        return counter.entrySet().stream()
                .map(e -> Map.<String, Object>of("alarmType", e.getKey(), "count", e.getValue()))
                .toList();
    }

    public List<SecAlarmRule> rules(Long communityId) {
        return ruleMapper.selectList(new LambdaQueryWrapper<SecAlarmRule>()
                .eq(communityId != null, SecAlarmRule::getCommunityId, communityId)
                .or().isNull(SecAlarmRule::getCommunityId)
                .orderByAsc(SecAlarmRule::getId));
    }

    public void saveRule(SecAlarmRule rule) {
        if (rule.getId() == null) {
            ruleMapper.insert(rule);
        } else {
            ruleMapper.updateById(rule);
        }
    }

    public void removeRule(Long id) {
        ruleMapper.deleteById(id);
    }

    public List<SecAlarmEvent> latest(Long communityId, int limit) {
        return baseMapper.selectList(new LambdaQueryWrapper<SecAlarmEvent>()
                .eq(communityId != null, SecAlarmEvent::getCommunityId, communityId)
                .orderByDesc(SecAlarmEvent::getOccurTime)
                .last("limit " + Math.max(1, Math.min(limit, 50))));
    }
}
