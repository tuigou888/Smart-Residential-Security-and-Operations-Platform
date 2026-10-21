package com.srs.modules.iot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.iot.entity.IotDevice;
import com.srs.modules.iot.entity.IotDeviceStatusLog;
import com.srs.modules.iot.mapper.IotDeviceMapper;
import com.srs.modules.iot.mapper.IotDeviceStatusLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IotDeviceService extends ServiceImpl<IotDeviceMapper, IotDevice> {

    private final IotDeviceStatusLogMapper statusLogMapper;

    public IPage<IotDevice> pageDevices(PageQuery q, Long communityId, String deviceType, String deviceName,
                                        Integer onlineStatus, Integer runStatus) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<IotDevice>()
                        .eq(communityId != null, IotDevice::getCommunityId, communityId)
                        .eq(StringUtils.hasText(deviceType), IotDevice::getDeviceType, deviceType)
                        .like(StringUtils.hasText(deviceName), IotDevice::getDeviceName, deviceName)
                        .eq(onlineStatus != null, IotDevice::getOnlineStatus, onlineStatus)
                        .eq(runStatus != null, IotDevice::getRunStatus, runStatus)
                        .orderByAsc(IotDevice::getDeviceCode));
    }

    public void saveDevice(IotDevice entity) {
        if (entity.getId() == null) {
            if (baseMapper.selectCount(new LambdaQueryWrapper<IotDevice>()
                    .eq(IotDevice::getDeviceCode, entity.getDeviceCode())) > 0) {
                throw BizException.conflict("设备编码已存在: " + entity.getDeviceCode());
            }
            baseMapper.insert(entity);
        } else {
            baseMapper.updateById(entity);
        }
    }

    public void removeDevices(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            baseMapper.deleteByIds(ids);
        }
    }

    /** 设备状态统计: 总数/在线/离线/故障 */
    public Map<String, Object> statusStat(Long communityId) {
        long total = baseMapper.selectCount(new LambdaQueryWrapper<IotDevice>()
                .eq(communityId != null, IotDevice::getCommunityId, communityId));
        long online = baseMapper.selectCount(new LambdaQueryWrapper<IotDevice>()
                .eq(communityId != null, IotDevice::getCommunityId, communityId)
                .eq(IotDevice::getOnlineStatus, 1));
        long fault = baseMapper.selectCount(new LambdaQueryWrapper<IotDevice>()
                .eq(communityId != null, IotDevice::getCommunityId, communityId)
                .eq(IotDevice::getRunStatus, 2));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("total", total);
        map.put("online", online);
        map.put("offline", total - online);
        map.put("fault", fault);
        map.put("onlineRate", total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(online).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
        return map;
    }

    /** 按类型分组统计 */
    public List<Map<String, Object>> typeStat(Long communityId) {
        List<IotDevice> devices = baseMapper.selectList(new LambdaQueryWrapper<IotDevice>()
                .eq(communityId != null, IotDevice::getCommunityId, communityId));
        Map<String, Long> counter = new LinkedHashMap<>();
        for (IotDevice d : devices) {
            counter.merge(d.getDeviceType() == null ? "unknown" : d.getDeviceType(), 1L, Long::sum);
        }
        return counter.entrySet().stream()
                .map(e -> Map.<String, Object>of("deviceType", e.getKey(), "count", e.getValue()))
                .toList();
    }

    public List<IotDeviceStatusLog> statusLogs(Long deviceId, Integer eventType) {
        return statusLogMapper.selectList(new LambdaQueryWrapper<IotDeviceStatusLog>()
                .eq(deviceId != null, IotDeviceStatusLog::getDeviceId, deviceId)
                .eq(eventType != null, IotDeviceStatusLog::getEventType, eventType)
                .orderByDesc(IotDeviceStatusLog::getEventTime)
                .last("limit 200"));
    }

    /**
     * 设备状态上报(MQTT/HTTP 接入入口)
     * 一期未接入 EMQX 时, 可由边缘网关或前端调试直接调用
     */
    public void reportStatus(String deviceCode, Integer onlineStatus, Integer runStatus, String extra) {
        IotDevice device = baseMapper.selectOne(new LambdaQueryWrapper<IotDevice>()
                .eq(IotDevice::getDeviceCode, deviceCode));
        if (device == null) {
            throw BizException.notFound("设备不存在: " + deviceCode);
        }
        Integer oldOnline = device.getOnlineStatus();
        device.setOnlineStatus(onlineStatus);
        if (runStatus != null) {
            device.setRunStatus(runStatus);
        }
        if (onlineStatus != null && onlineStatus == 1) {
            device.setLastOnlineTime(LocalDateTime.now());
        }
        baseMapper.updateById(device);

        IotDeviceStatusLog logEntity = new IotDeviceStatusLog();
        logEntity.setDeviceId(device.getId());
        logEntity.setCommunityId(device.getCommunityId());
        logEntity.setEventTime(LocalDateTime.now());
        logEntity.setExtra(extra);
        if (onlineStatus != null) {
            logEntity.setEventType(onlineStatus == 1 ? 1 : 2);
        } else if (runStatus != null) {
            logEntity.setEventType(runStatus == 2 ? 3 : 4);
        } else {
            logEntity.setEventType(4);
        }
        statusLogMapper.insert(logEntity);
        log.info("[device] {} 状态变更 {} -> {}", deviceCode, oldOnline, onlineStatus);
    }

    /** 远程重启(下发指令, 一期为指令落地+记录, 不直连设备) */
    public void restart(Long id) {
        IotDevice device = baseMapper.selectById(id);
        if (device == null) {
            throw BizException.notFound("设备不存在: " + id);
        }
        IotDeviceStatusLog logEntity = new IotDeviceStatusLog();
        logEntity.setDeviceId(id);
        logEntity.setCommunityId(device.getCommunityId());
        logEntity.setEventType(4);
        logEntity.setEventTime(LocalDateTime.now());
        logEntity.setExtra("下发重启指令");
        statusLogMapper.insert(logEntity);
    }

    public IotDevice byCode(String deviceCode) {
        return baseMapper.selectOne(new LambdaQueryWrapper<IotDevice>()
                .eq(IotDevice::getDeviceCode, deviceCode));
    }
}
