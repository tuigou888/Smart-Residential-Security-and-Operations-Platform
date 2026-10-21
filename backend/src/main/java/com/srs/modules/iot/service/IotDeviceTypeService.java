package com.srs.modules.iot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.iot.entity.IotDeviceType;
import com.srs.modules.iot.mapper.IotDeviceTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 设备类型管理: 编码唯一校验、父子层级保护
 */
@Service
@RequiredArgsConstructor
public class IotDeviceTypeService extends ServiceImpl<IotDeviceTypeMapper, IotDeviceType> {

    public IPage<IotDeviceType> page(PageQuery q, Long communityId, String typeName) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<IotDeviceType>()
                        // 社区可见"本小区类型 + 全局类型(community_id 为空)"
                        .and(communityId != null, w -> w.eq(IotDeviceType::getCommunityId, communityId)
                                .or().isNull(IotDeviceType::getCommunityId))
                        .like(StringUtils.hasText(typeName), IotDeviceType::getTypeName, typeName)
                        .orderByAsc(IotDeviceType::getOrderNum)
                        .orderByAsc(IotDeviceType::getId));
    }

    /** 下拉选项: 仅启用的类型(含全局类型) */
    public List<IotDeviceType> options() {
        return baseMapper.selectList(new LambdaQueryWrapper<IotDeviceType>()
                .eq(IotDeviceType::getStatus, 1)
                .orderByAsc(IotDeviceType::getOrderNum)
                .orderByAsc(IotDeviceType::getId));
    }

    public void saveType(IotDeviceType entity) {
        if (!StringUtils.hasText(entity.getTypeCode())) {
            throw BizException.badRequest("类型编码不能为空");
        }
        if (!StringUtils.hasText(entity.getTypeName())) {
            throw BizException.badRequest("类型名称不能为空");
        }
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<IotDeviceType>()
                .eq(IotDeviceType::getTypeCode, entity.getTypeCode())
                .ne(entity.getId() != null, IotDeviceType::getId, entity.getId()));
        if (count != null && count > 0) {
            throw BizException.conflict("类型编码已存在");
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getOrderNum() == null) {
            entity.setOrderNum(0);
        }
        if (entity.getId() == null) {
            baseMapper.insert(entity);
        } else {
            baseMapper.updateById(entity);
        }
    }

    public void removeTypes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw BizException.badRequest("请选择要删除的设备类型");
        }
        Long children = baseMapper.selectCount(new LambdaQueryWrapper<IotDeviceType>()
                .in(IotDeviceType::getParentId, ids));
        if (children != null && children > 0) {
            throw BizException.conflict("存在子类型，无法删除");
        }
        baseMapper.deleteBatchIds(ids);
    }
}
