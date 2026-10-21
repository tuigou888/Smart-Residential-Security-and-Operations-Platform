package com.srs.modules.patrol.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.patrol.entity.PatPoint;
import com.srs.modules.patrol.mapper.PatPointMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 巡更点位管理
 */
@Service
@RequiredArgsConstructor
public class PatPointService extends ServiceImpl<PatPointMapper, PatPoint> {

    public IPage<PatPoint> page(PageQuery q, Long communityId, String pointName) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<PatPoint>()
                        .eq(communityId != null, PatPoint::getCommunityId, communityId)
                        .like(StringUtils.hasText(pointName), PatPoint::getPointName, pointName)
                        .orderByAsc(PatPoint::getPointCode));
    }

    public List<PatPoint> options(Long communityId) {
        return baseMapper.selectList(new LambdaQueryWrapper<PatPoint>()
                .eq(communityId != null, PatPoint::getCommunityId, communityId)
                .eq(PatPoint::getStatus, 1)
                .orderByAsc(PatPoint::getPointCode));
    }

    public void savePoint(PatPoint point) {
        if (!StringUtils.hasText(point.getPointCode())) {
            throw BizException.badRequest("点位编码不能为空");
        }
        if (!StringUtils.hasText(point.getPointName())) {
            throw BizException.badRequest("点位名称不能为空");
        }
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<PatPoint>()
                .eq(PatPoint::getPointCode, point.getPointCode())
                .ne(point.getId() != null, PatPoint::getId, point.getId()));
        if (count != null && count > 0) {
            throw BizException.conflict("点位编码已存在");
        }
        if (point.getStatus() == null) {
            point.setStatus(1);
        }
        if (point.getId() == null) {
            baseMapper.insert(point);
        } else {
            baseMapper.updateById(point);
        }
    }

    public void removePoints(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw BizException.badRequest("请选择要删除的点位");
        }
        baseMapper.deleteBatchIds(ids);
    }
}
