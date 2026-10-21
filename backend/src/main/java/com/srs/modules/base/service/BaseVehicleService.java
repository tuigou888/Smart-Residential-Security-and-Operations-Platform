package com.srs.modules.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.base.entity.BaseOwner;
import com.srs.modules.base.entity.BaseVehicle;
import com.srs.modules.base.mapper.BaseOwnerMapper;
import com.srs.modules.base.mapper.BaseVehicleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaseVehicleService extends ServiceImpl<BaseVehicleMapper, BaseVehicle> {

    private final BaseOwnerMapper ownerMapper;

    public IPage<BaseVehicle> pageVehicles(PageQuery q, Long communityId, String plateNo,
                                           Integer vehicleType, Integer status) {
        Page<BaseVehicle> page = new Page<>(q.getPageNum(), q.getPageSize());
        IPage<BaseVehicle> result = baseMapper.selectPage(page, new LambdaQueryWrapper<BaseVehicle>()
                .eq(communityId != null, BaseVehicle::getCommunityId, communityId)
                .like(StringUtils.hasText(plateNo), BaseVehicle::getPlateNo, plateNo)
                .eq(vehicleType != null, BaseVehicle::getVehicleType, vehicleType)
                .eq(status != null, BaseVehicle::getStatus, status)
                .orderByAsc(BaseVehicle::getPlateNo));
        Map<Long, BaseOwner> owners = ownerMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseOwner::getId, Function.identity(), (a, b) -> a));
        for (BaseVehicle v : result.getRecords()) {
            if (v.getOwnerId() != null && owners.containsKey(v.getOwnerId())) {
                v.setOwnerName(owners.get(v.getOwnerId()).getOwnerName());
            }
        }
        return result;
    }

    public void saveVehicle(BaseVehicle entity) {
        if (entity.getId() == null) {
            if (baseMapper.selectCount(new LambdaQueryWrapper<BaseVehicle>()
                    .eq(BaseVehicle::getPlateNo, entity.getPlateNo())) > 0) {
                throw BizException.conflict("车牌号已存在: " + entity.getPlateNo());
            }
            baseMapper.insert(entity);
        } else {
            baseMapper.updateById(entity);
        }
    }

    public void remove(java.util.List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            baseMapper.deleteByIds(ids);
        }
    }
}
