package com.srs.modules.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.base.entity.BaseCommunity;
import com.srs.modules.base.mapper.BaseCommunityMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class BaseCommunityService extends ServiceImpl<BaseCommunityMapper, BaseCommunity> {

    public IPage<BaseCommunity> pageCommunities(PageQuery q, String communityName, String communityCode, Integer status) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<BaseCommunity>()
                        .like(StringUtils.hasText(communityName), BaseCommunity::getCommunityName, communityName)
                        .like(StringUtils.hasText(communityCode), BaseCommunity::getCommunityCode, communityCode)
                        .eq(status != null, BaseCommunity::getStatus, status)
                        .orderByAsc(BaseCommunity::getId));
    }

    /** 下拉选项(受数据权限控制) */
    public List<BaseCommunity> options() {
        return baseMapper.selectList(new LambdaQueryWrapper<BaseCommunity>()
                .eq(BaseCommunity::getStatus, 1)
                .orderByAsc(BaseCommunity::getId));
    }

    public void create(BaseCommunity entity) {
        if (baseMapper.selectCount(new LambdaQueryWrapper<BaseCommunity>()
                .eq(BaseCommunity::getCommunityCode, entity.getCommunityCode())) > 0) {
            throw BizException.conflict("小区编码已存在: " + entity.getCommunityCode());
        }
        baseMapper.insert(entity);
    }

    public void modify(BaseCommunity entity) {
        baseMapper.updateById(entity);
    }

    public void remove(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        baseMapper.deleteByIds(ids);
    }
}
