package com.srs.modules.sec.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.iot.entity.IotDevice;
import com.srs.modules.iot.mapper.IotDeviceMapper;
import com.srs.modules.sec.entity.SecAccessPermission;
import com.srs.modules.sec.entity.SecAccessRecord;
import com.srs.modules.sec.mapper.SecAccessPermissionMapper;
import com.srs.modules.sec.mapper.SecAccessRecordMapper;
import com.srs.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 门禁管理: 设备/通行记录/授权/远程开门
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccessService extends ServiceImpl<SecAccessRecordMapper, SecAccessRecord> {

    private final IotDeviceMapper deviceMapper;
    private final SecAccessPermissionMapper permissionMapper;

    public List<IotDevice> accessDevices(Long communityId) {
        return deviceMapper.selectList(new LambdaQueryWrapper<IotDevice>()
                .eq(communityId != null, IotDevice::getCommunityId, communityId)
                .in(IotDevice::getDeviceType, List.of("guard", "face"))
                .orderByAsc(IotDevice::getDeviceCode));
    }

    public IPage<SecAccessRecord> pageRecords(PageQuery q, Long communityId, Long deviceId, Long ownerId,
                                              String personName, Integer openMethod, Integer passResult,
                                              LocalDateTime begin, LocalDateTime end) {
        Page<SecAccessRecord> page = new Page<>(q.getPageNum(), q.getPageSize());
        IPage<SecAccessRecord> result = baseMapper.selectPage(page, new LambdaQueryWrapper<SecAccessRecord>()
                .eq(communityId != null, SecAccessRecord::getCommunityId, communityId)
                .eq(deviceId != null, SecAccessRecord::getDeviceId, deviceId)
                .eq(ownerId != null, SecAccessRecord::getOwnerId, ownerId)
                .like(StringUtils.hasText(personName), SecAccessRecord::getPersonName, personName)
                .eq(openMethod != null, SecAccessRecord::getOpenMethod, openMethod)
                .eq(passResult != null, SecAccessRecord::getPassResult, passResult)
                .ge(begin != null, SecAccessRecord::getPassTime, begin)
                .le(end != null, SecAccessRecord::getPassTime, end)
                .orderByDesc(SecAccessRecord::getPassTime));
        Map<Long, IotDevice> devices = deviceMapper.selectList(null).stream()
                .collect(Collectors.toMap(IotDevice::getId, Function.identity(), (a, b) -> a));
        for (SecAccessRecord r : result.getRecords()) {
            IotDevice d = devices.get(r.getDeviceId());
            if (d != null) {
                r.setDeviceName(d.getDeviceName());
            }
        }
        return result;
    }

    /**
     * 远程开门: 校验授权 -> 下发开门指令 -> 记录通行
     * 一期指令下发为记录式(接入层对接后替换为真实下发)
     */
    public void remoteOpen(Long deviceId, Long communityId, String reason) {
        IotDevice device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw BizException.notFound("门禁设备不存在: " + deviceId);
        }
        if (!List.of("guard", "face").contains(device.getDeviceType())) {
            throw BizException.badRequest("该设备不是门禁设备");
        }
        SecAccessRecord record = new SecAccessRecord();
        record.setCommunityId(communityId == null ? device.getCommunityId() : communityId);
        record.setDeviceId(deviceId);
        record.setPersonName(SecurityUtils.getRealName());
        record.setOpenMethod(5);
        record.setPassResult(1);
        record.setPassTime(LocalDateTime.now());
        baseMapper.insert(record);
        log.info("[access] 远程开门 deviceId={} operator={} reason={}", deviceId, SecurityUtils.getUsername(), reason);
    }

    public List<SecAccessPermission> permissions(Long deviceId, Long ownerId, Long communityId) {
        return permissionMapper.selectList(new LambdaQueryWrapper<SecAccessPermission>()
                .eq(deviceId != null, SecAccessPermission::getDeviceId, deviceId)
                .eq(ownerId != null, SecAccessPermission::getOwnerId, ownerId)
                .eq(communityId != null, SecAccessPermission::getCommunityId, communityId)
                .orderByDesc(SecAccessPermission::getId));
    }

    public void grant(SecAccessPermission permission) {
        long exists = permissionMapper.selectCount(new LambdaQueryWrapper<SecAccessPermission>()
                .eq(SecAccessPermission::getDeviceId, permission.getDeviceId())
                .eq(SecAccessPermission::getOwnerId, permission.getOwnerId()));
        if (exists > 0) {
            throw BizException.conflict("该人员已授权该门禁");
        }
        if (permission.getStatus() == null) {
            permission.setStatus(1);
        }
        if (permission.getTimeStart() == null) {
            permission.setTimeStart("00:00");
        }
        if (permission.getTimeEnd() == null) {
            permission.setTimeEnd("23:59");
        }
        permissionMapper.insert(permission);
    }

    public void revoke(Long deviceId, Long ownerId) {
        permissionMapper.delete(new LambdaQueryWrapper<SecAccessPermission>()
                .eq(SecAccessPermission::getDeviceId, deviceId)
                .eq(SecAccessPermission::getOwnerId, ownerId));
    }

    /** 通行记录写入(门禁设备上报入口) */
    public void record(SecAccessRecord record) {
        if (record.getPassTime() == null) {
            record.setPassTime(LocalDateTime.now());
        }
        if (record.getPassResult() == null) {
            record.setPassResult(1);
        }
        baseMapper.insert(record);
    }
}
