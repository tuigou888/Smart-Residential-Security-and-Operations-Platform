package com.srs.modules.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.modules.base.entity.BaseBuilding;
import com.srs.modules.base.entity.BaseCommunity;
import com.srs.modules.base.entity.BaseUnit;
import com.srs.modules.base.mapper.BaseBuildingMapper;
import com.srs.modules.base.mapper.BaseCommunityMapper;
import com.srs.modules.base.mapper.BaseUnitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 楼栋 / 单元 管理
 */
@Service
@RequiredArgsConstructor
public class BaseStructureService extends ServiceImpl<BaseBuildingMapper, BaseBuilding> {

    private final BaseUnitMapper unitMapper;
    private final BaseCommunityMapper communityMapper;

    public IPage<BaseBuilding> pageBuildings(PageQuery q, Long communityId, String buildingName) {
        Page<BaseBuilding> page = new Page<>(q.getPageNum(), q.getPageSize());
        IPage<BaseBuilding> result = baseMapper.selectPage(page, new LambdaQueryWrapper<BaseBuilding>()
                .eq(communityId != null, BaseBuilding::getCommunityId, communityId)
                .like(buildingName != null && !buildingName.isBlank(), BaseBuilding::getBuildingName, buildingName)
                .orderByAsc(BaseBuilding::getBuildingNo));
        fillCommunityName(result.getRecords());
        return result;
    }

    public IPage<BaseUnit> pageUnits(PageQuery q, Long communityId, Long buildingId, String unitName) {
        Page<BaseUnit> page = new Page<>(q.getPageNum(), q.getPageSize());
        IPage<BaseUnit> result = unitMapper.selectPage(page, new LambdaQueryWrapper<BaseUnit>()
                .eq(communityId != null, BaseUnit::getCommunityId, communityId)
                .eq(buildingId != null, BaseUnit::getBuildingId, buildingId)
                .like(unitName != null && !unitName.isBlank(), BaseUnit::getUnitName, unitName)
                .orderByAsc(BaseUnit::getId));
        Map<Long, BaseBuilding> buildings = baseMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseBuilding::getId, Function.identity(), (a, b) -> a));
        for (BaseUnit u : result.getRecords()) {
            BaseBuilding b = buildings.get(u.getBuildingId());
            if (b != null) {
                u.setBuildingName(b.getBuildingName());
            }
        }
        return result;
    }

    public List<BaseBuilding> buildingsOf(Long communityId) {
        return baseMapper.selectList(new LambdaQueryWrapper<BaseBuilding>()
                .eq(communityId != null, BaseBuilding::getCommunityId, communityId)
                .orderByAsc(BaseBuilding::getBuildingNo));
    }

    public List<BaseUnit> unitsOf(Long buildingId) {
        return unitMapper.selectList(new LambdaQueryWrapper<BaseUnit>()
                .eq(buildingId != null, BaseUnit::getBuildingId, buildingId)
                .orderByAsc(BaseUnit::getId));
    }

    public void saveBuilding(BaseBuilding entity) {
        if (entity.getId() == null) {
            baseMapper.insert(entity);
        } else {
            baseMapper.updateById(entity);
        }
    }

    public void saveUnit(BaseUnit entity) {
        if (entity.getId() == null) {
            unitMapper.insert(entity);
        } else {
            unitMapper.updateById(entity);
        }
    }

    public void removeBuildings(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            baseMapper.deleteByIds(ids);
        }
    }

    public void removeUnits(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            unitMapper.deleteByIds(ids);
        }
    }

    private void fillCommunityName(List<BaseBuilding> list) {
        if (list.isEmpty()) {
            return;
        }
        Map<Long, BaseCommunity> map = communityMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseCommunity::getId, Function.identity(), (a, b) -> a));
        for (BaseBuilding b : list) {
            BaseCommunity c = map.get(b.getCommunityId());
            if (c != null) {
                b.setCommunityName(c.getCommunityName());
            }
        }
    }
}
