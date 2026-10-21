package com.srs.modules.sec.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.sec.entity.SecBlacklist;
import com.srs.modules.sec.mapper.SecBlacklistMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 黑名单管理: 人员/车辆布控与拦截判定
 */
@Service
@RequiredArgsConstructor
public class BlacklistService extends ServiceImpl<SecBlacklistMapper, SecBlacklist> {

    public IPage<SecBlacklist> page(PageQuery q, Long communityId, Integer blacklistType, String keyword) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<SecBlacklist>()
                        .eq(communityId != null, SecBlacklist::getCommunityId, communityId)
                        .eq(blacklistType != null, SecBlacklist::getBlacklistType, blacklistType)
                        .and(StringUtils.hasText(keyword),
                                w -> w.like(SecBlacklist::getTargetName, keyword)
                                        .or().like(SecBlacklist::getTargetId, keyword))
                        .orderByDesc(SecBlacklist::getId));
    }

    public void saveBlacklist(SecBlacklist item) {
        if (item.getCommunityId() == null) {
            throw BizException.badRequest("小区ID不能为空");
        }
        if (item.getBlacklistType() == null) {
            throw BizException.badRequest("黑名单类型不能为空");
        }
        if (!StringUtils.hasText(item.getTargetId())) {
            throw BizException.badRequest("姓名/车牌标识不能为空");
        }
        Long exists = baseMapper.selectCount(new LambdaQueryWrapper<SecBlacklist>()
                .eq(SecBlacklist::getCommunityId, item.getCommunityId())
                .eq(SecBlacklist::getBlacklistType, item.getBlacklistType())
                .eq(SecBlacklist::getTargetId, item.getTargetId())
                .eq(SecBlacklist::getStatus, 1)
                .ne(item.getId() != null, SecBlacklist::getId, item.getId()));
        if (exists != null && exists > 0) {
            throw BizException.conflict("该目标已在启用中的黑名单内");
        }
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        if (item.getId() == null) {
            baseMapper.insert(item);
        } else {
            baseMapper.updateById(item);
        }
    }

    public void removeBlacklist(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw BizException.badRequest("请选择要删除的黑名单记录");
        }
        baseMapper.deleteBatchIds(ids);
    }

    /**
     * 拦截判定
     *
     * @param communityId 小区ID
     * @param type        1人员 2车辆
     * @param targetId    身份证/车牌号
     * @return true 表示命中启用中的黑名单
     */
    public boolean isBlocked(Long communityId, Integer type, String targetId) {
        if (communityId == null || type == null || !StringUtils.hasText(targetId)) {
            return false;
        }
        LocalDate today = LocalDate.now();
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<SecBlacklist>()
                .eq(SecBlacklist::getCommunityId, communityId)
                .eq(SecBlacklist::getBlacklistType, type)
                .eq(SecBlacklist::getTargetId, targetId)
                .eq(SecBlacklist::getStatus, 1)
                .and(w -> w.isNull(SecBlacklist::getEffectiveStart)
                        .or().le(SecBlacklist::getEffectiveStart, today))
                .and(w -> w.isNull(SecBlacklist::getEffectiveEnd)
                        .or().ge(SecBlacklist::getEffectiveEnd, today)));
        return count != null && count > 0;
    }
}
