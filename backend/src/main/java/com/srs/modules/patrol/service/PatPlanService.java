package com.srs.modules.patrol.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.patrol.entity.PatPlan;
import com.srs.modules.patrol.entity.PatRoute;
import com.srs.modules.patrol.mapper.PatPlanMapper;
import com.srs.modules.patrol.mapper.PatRouteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 巡更计划管理
 */
@Service
@RequiredArgsConstructor
public class PatPlanService extends ServiceImpl<PatPlanMapper, PatPlan> {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final PatRouteMapper routeMapper;

    public IPage<PatPlan> page(PageQuery q, Long communityId, String planName) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<PatPlan>()
                        .eq(communityId != null, PatPlan::getCommunityId, communityId)
                        .like(StringUtils.hasText(planName), PatPlan::getPlanName, planName)
                        .orderByDesc(PatPlan::getId));
    }

    public void savePlan(PatPlan plan) {
        if (!StringUtils.hasText(plan.getPlanName())) {
            throw BizException.badRequest("计划名称不能为空");
        }
        if (plan.getRouteId() == null) {
            throw BizException.badRequest("请选择巡更路线");
        }
        PatRoute route = routeMapper.selectById(plan.getRouteId());
        if (route == null) {
            throw BizException.badRequest("巡更路线不存在");
        }
        validateTime(plan.getStartTime());
        validateTime(plan.getEndTime());
        if (plan.getIntervalMinutes() == null || plan.getIntervalMinutes() <= 0) {
            throw BizException.badRequest("巡更间隔必须大于 0 分钟");
        }
        if (plan.getStatus() == null) {
            plan.setStatus(1);
        }
        if (plan.getId() == null) {
            baseMapper.insert(plan);
        } else {
            baseMapper.updateById(plan);
        }
    }

    public void removePlans(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw BizException.badRequest("请选择要删除的巡更计划");
        }
        baseMapper.deleteBatchIds(ids);
    }

    private void validateTime(String value) {
        if (!StringUtils.hasText(value)) {
            throw BizException.badRequest("巡更时间不能为空");
        }
        try {
            LocalTime.parse(value, TIME_FMT);
        } catch (DateTimeParseException e) {
            throw BizException.badRequest("巡更时间格式应为 HH:mm");
        }
    }
}
