package com.srs.modules.patrol.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.patrol.entity.PatPlan;
import com.srs.modules.patrol.entity.PatPoint;
import com.srs.modules.patrol.entity.PatRoute;
import com.srs.modules.patrol.entity.PatRoutePoint;
import com.srs.modules.patrol.mapper.PatPlanMapper;
import com.srs.modules.patrol.mapper.PatPointMapper;
import com.srs.modules.patrol.mapper.PatRouteMapper;
import com.srs.modules.patrol.mapper.PatRoutePointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 巡更路线管理: 路线与点位编排
 */
@Service
@RequiredArgsConstructor
public class PatRouteService extends ServiceImpl<PatRouteMapper, PatRoute> {

    private final PatRoutePointMapper routePointMapper;
    private final PatPointMapper pointMapper;
    private final PatPlanMapper planMapper;

    public IPage<PatRoute> page(PageQuery q, Long communityId, String routeName) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<PatRoute>()
                        .eq(communityId != null, PatRoute::getCommunityId, communityId)
                        .like(StringUtils.hasText(routeName), PatRoute::getRouteName, routeName)
                        .orderByAsc(PatRoute::getRouteCode));
    }

    public List<PatRoute> options(Long communityId) {
        return baseMapper.selectList(new LambdaQueryWrapper<PatRoute>()
                .eq(communityId != null, PatRoute::getCommunityId, communityId)
                .eq(PatRoute::getStatus, 1)
                .orderByAsc(PatRoute::getRouteCode));
    }

    /** 路线详情: 含按巡更顺序排列的点位 */
    public Map<String, Object> detail(Long id) {
        PatRoute route = baseMapper.selectById(id);
        if (route == null) {
            throw BizException.notFound("巡更路线不存在: " + id);
        }
        List<PatRoutePoint> relations = routePointMapper.selectList(new LambdaQueryWrapper<PatRoutePoint>()
                .eq(PatRoutePoint::getRouteId, id)
                .orderByAsc(PatRoutePoint::getOrderNum));
        List<Long> pointIds = relations.stream().map(PatRoutePoint::getPointId).toList();
        Map<Long, PatPoint> pointMap = pointIds.isEmpty() ? Map.of()
                : pointMapper.selectBatchIds(pointIds).stream()
                        .collect(Collectors.toMap(PatPoint::getId, Function.identity(), (a, b) -> a));
        List<PatPoint> points = new ArrayList<>();
        for (Long pid : pointIds) {
            PatPoint p = pointMap.get(pid);
            if (p != null) {
                points.add(p);
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("route", route);
        data.put("points", points);
        data.put("pointIds", pointIds);
        return data;
    }

    /**
     * 保存路线及其点位编排
     *
     * @param route    路线信息
     * @param pointIds 按巡更顺序排列的点位 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRoute(PatRoute route, List<Long> pointIds) {
        if (!StringUtils.hasText(route.getRouteCode())) {
            throw BizException.badRequest("路线编码不能为空");
        }
        if (!StringUtils.hasText(route.getRouteName())) {
            throw BizException.badRequest("路线名称不能为空");
        }
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<PatRoute>()
                .eq(PatRoute::getRouteCode, route.getRouteCode())
                .ne(route.getId() != null, PatRoute::getId, route.getId()));
        if (count != null && count > 0) {
            throw BizException.conflict("路线编码已存在");
        }

        List<Long> ids = pointIds == null ? List.of() : pointIds;
        if (!ids.isEmpty()) {
            Long exists = pointMapper.selectCount(new LambdaQueryWrapper<PatPoint>()
                    .in(PatPoint::getId, ids));
            if (exists == null || exists < ids.size()) {
                throw BizException.badRequest("存在无效的巡更点位");
            }
        }

        route.setPointCount(ids.size());
        if (route.getStatus() == null) {
            route.setStatus(1);
        }
        if (route.getId() == null) {
            baseMapper.insert(route);
        } else {
            baseMapper.updateById(route);
        }

        // 关联表使用物理删除，避免唯一索引 (route_id, point_id) 与逻辑删除冲突
        routePointMapper.deleteByRouteId(route.getId());
        for (int i = 0; i < ids.size(); i++) {
            PatRoutePoint rp = new PatRoutePoint();
            rp.setRouteId(route.getId());
            rp.setPointId(ids.get(i));
            rp.setOrderNum(i + 1);
            rp.setCommunityId(route.getCommunityId());
            routePointMapper.insert(rp);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeRoutes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw BizException.badRequest("请选择要删除的路线");
        }
        Long used = planMapper.selectCount(new LambdaQueryWrapper<PatPlan>()
                .in(PatPlan::getRouteId, ids));
        if (used != null && used > 0) {
            throw BizException.conflict("路线已被巡更计划引用，无法删除");
        }
        for (Long id : ids) {
            routePointMapper.deleteByRouteId(id);
        }
        baseMapper.deleteBatchIds(ids);
    }
}
