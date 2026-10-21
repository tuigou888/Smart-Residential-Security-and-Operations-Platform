package com.srs.modules.patrol.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.patrol.entity.PatPlan;
import com.srs.modules.patrol.entity.PatRecord;
import com.srs.modules.patrol.entity.PatRoutePoint;
import com.srs.modules.patrol.mapper.PatPlanMapper;
import com.srs.modules.patrol.mapper.PatRecordMapper;
import com.srs.modules.patrol.mapper.PatRoutePointMapper;
import com.srs.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 巡更记录: 打卡、漏巡判定、当日统计
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatRecordService extends ServiceImpl<PatRecordMapper, PatRecord> {

    private final PatPlanMapper planMapper;
    private final PatRoutePointMapper routePointMapper;

    public IPage<PatRecord> page(PageQuery q, Long communityId, Long planId, Long userId, Integer status) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<PatRecord>()
                        .eq(communityId != null, PatRecord::getCommunityId, communityId)
                        .eq(planId != null, PatRecord::getPlanId, planId)
                        .eq(userId != null, PatRecord::getUserId, userId)
                        .eq(status != null, PatRecord::getStatus, status)
                        .orderByDesc(PatRecord::getCheckTime));
    }

    /** 巡更打卡: 从计划补全小区/路线, 并记录当前登录人 */
    @Transactional(rollbackFor = Exception.class)
    public PatRecord checkIn(PatRecord record) {
        if (record.getPlanId() == null || record.getPointId() == null) {
            throw BizException.badRequest("计划ID与点位ID不能为空");
        }
        PatPlan plan = planMapper.selectById(record.getPlanId());
        if (plan == null) {
            throw BizException.notFound("巡更计划不存在: " + record.getPlanId());
        }
        record.setCommunityId(plan.getCommunityId());
        record.setRouteId(plan.getRouteId());
        if (record.getUserId() == null) {
            record.setUserId(SecurityUtils.getUserId());
        }
        if (!StringUtils.hasText(record.getUserName())) {
            record.setUserName(SecurityUtils.getRealName());
        }
        if (record.getCheckTime() == null) {
            record.setCheckTime(LocalDateTime.now());
        }
        if (record.getCheckMethod() == null) {
            record.setCheckMethod(1);
        }
        if (record.getStatus() == null) {
            record.setStatus(1);
        }
        baseMapper.insert(record);
        return record;
    }

    /**
     * 漏巡判定(幂等): 对指定日期, 按启用计划的路线点位与已打卡记录比对, 缺失则写入 status=3 的漏巡记录
     *
     * @return 新增漏巡记录条数
     */
    @Transactional(rollbackFor = Exception.class)
    public int markMissing(LocalDate date) {
        LocalDateTime begin = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        LocalDateTime deadline = date.atTime(23, 59, 59);

        List<PatPlan> plans = planMapper.selectList(new LambdaQueryWrapper<PatPlan>()
                .eq(PatPlan::getStatus, 1));
        int inserted = 0;
        for (PatPlan plan : plans) {
            List<PatRoutePoint> points = routePointMapper.selectList(new LambdaQueryWrapper<PatRoutePoint>()
                    .eq(PatRoutePoint::getRouteId, plan.getRouteId())
                    .orderByAsc(PatRoutePoint::getOrderNum));
            Long userId = firstUserId(plan.getAssignUserIds());
            for (PatRoutePoint point : points) {
                Long checked = baseMapper.selectCount(new LambdaQueryWrapper<PatRecord>()
                        .eq(PatRecord::getPlanId, plan.getId())
                        .eq(PatRecord::getPointId, point.getPointId())
                        .ge(PatRecord::getCheckTime, begin)
                        .lt(PatRecord::getCheckTime, end)
                        .in(PatRecord::getStatus, List.of(1, 2)));
                if (checked != null && checked > 0) {
                    continue;
                }
                Long missing = baseMapper.selectCount(new LambdaQueryWrapper<PatRecord>()
                        .eq(PatRecord::getPlanId, plan.getId())
                        .eq(PatRecord::getPointId, point.getPointId())
                        .ge(PatRecord::getCheckTime, begin)
                        .lt(PatRecord::getCheckTime, end)
                        .eq(PatRecord::getStatus, 3));
                if (missing != null && missing > 0) {
                    continue;
                }
                PatRecord record = new PatRecord();
                record.setCommunityId(plan.getCommunityId());
                record.setPlanId(plan.getId());
                record.setRouteId(plan.getRouteId());
                record.setPointId(point.getPointId());
                record.setUserId(userId);
                record.setCheckMethod(1);
                record.setCheckTime(deadline);
                record.setStatus(3);
                record.setExceptionDesc("系统判定漏巡");
                baseMapper.insert(record);
                inserted++;
            }
        }
        if (inserted > 0) {
            log.info("[patrol] {} 判定漏巡 {} 条", date, inserted);
        }
        return inserted;
    }

    /** 当日巡更统计 */
    public Map<String, Object> stat(Long communityId, LocalDate date) {
        LocalDate statDate = date == null ? LocalDate.now() : date;
        LocalDateTime begin = statDate.atStartOfDay();
        LocalDateTime end = statDate.plusDays(1).atStartOfDay();

        long expected = 0;
        List<PatPlan> plans = planMapper.selectList(new LambdaQueryWrapper<PatPlan>()
                .eq(communityId != null, PatPlan::getCommunityId, communityId)
                .eq(PatPlan::getStatus, 1));
        for (PatPlan plan : plans) {
            Long count = routePointMapper.selectCount(new LambdaQueryWrapper<PatRoutePoint>()
                    .eq(PatRoutePoint::getRouteId, plan.getRouteId()));
            expected += count == null ? 0 : count;
        }

        long actual = count(communityId, begin, end, List.of(1, 2));
        long missing = count(communityId, begin, end, List.of(3));
        long abnormal = count(communityId, begin, end, List.of(2));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("date", statDate.toString());
        data.put("expected", expected);
        data.put("actual", actual);
        data.put("missing", missing);
        data.put("abnormal", abnormal);
        return data;
    }

    private long count(Long communityId, LocalDateTime begin, LocalDateTime end, List<Integer> statuses) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<PatRecord>()
                .eq(communityId != null, PatRecord::getCommunityId, communityId)
                .ge(PatRecord::getCheckTime, begin)
                .lt(PatRecord::getCheckTime, end)
                .in(PatRecord::getStatus, statuses));
        return count == null ? 0 : count;
    }

    private Long firstUserId(String assignUserIds) {
        if (!StringUtils.hasText(assignUserIds)) {
            return null;
        }
        String first = assignUserIds.split(",")[0].trim();
        try {
            return Long.valueOf(first);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
