package com.srs.modules.iot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.iot.entity.IotDevice;
import com.srs.modules.iot.entity.IotDeviceEvent;
import com.srs.modules.iot.mapper.IotDeviceEventMapper;
import com.srs.modules.iot.mapper.IotDeviceMapper;
import com.srs.modules.sec.entity.SecAlarmEvent;
import com.srs.modules.sec.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 设备事件: 设备侧事件上报与重要事件转告警
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IotDeviceEventService extends ServiceImpl<IotDeviceEventMapper, IotDeviceEvent> {

    private final IotDeviceMapper deviceMapper;
    private final AlarmService alarmService;

    public IPage<IotDeviceEvent> page(PageQuery q, Long communityId, Long deviceId,
                                      String eventCode, Integer handleStatus) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<IotDeviceEvent>()
                        .eq(communityId != null, IotDeviceEvent::getCommunityId, communityId)
                        .eq(deviceId != null, IotDeviceEvent::getDeviceId, deviceId)
                        .eq(StringUtils.hasText(eventCode), IotDeviceEvent::getEventCode, eventCode)
                        .eq(handleStatus != null, IotDeviceEvent::getHandleStatus, handleStatus)
                        .orderByDesc(IotDeviceEvent::getEventTime));
    }

    /**
     * 设备事件上报: OFFLINE / FAULT 自动转告警
     */
    @Transactional(rollbackFor = Exception.class)
    public IotDeviceEvent report(IotDeviceEvent event) {
        if (event.getDeviceId() == null) {
            throw BizException.badRequest("设备ID不能为空");
        }
        if (!StringUtils.hasText(event.getEventCode())) {
            throw BizException.badRequest("事件编码不能为空");
        }
        IotDevice device = deviceMapper.selectById(event.getDeviceId());
        if (device == null) {
            throw BizException.notFound("设备不存在: " + event.getDeviceId());
        }
        if (event.getCommunityId() == null) {
            event.setCommunityId(device.getCommunityId());
        }
        if (event.getEventTime() == null) {
            event.setEventTime(LocalDateTime.now());
        }
        if (event.getHandleStatus() == null) {
            event.setHandleStatus(0);
        }
        if (!StringUtils.hasText(event.getEventName())) {
            event.setEventName(eventName(event.getEventCode()));
        }
        baseMapper.insert(event);

        if ("OFFLINE".equals(event.getEventCode()) || "FAULT".equals(event.getEventCode())) {
            SecAlarmEvent alarm = new SecAlarmEvent();
            alarm.setCommunityId(event.getCommunityId());
            alarm.setDeviceId(device.getId());
            alarm.setAlarmType("OFFLINE".equals(event.getEventCode()) ? "device_offline" : "device_fault");
            alarm.setAlarmLevel("FAULT".equals(event.getEventCode()) ? 1 : 2);
            alarm.setSourceType(1);
            alarm.setTitle("【设备事件】" + device.getDeviceName() + " " + event.getEventName());
            alarm.setContent("设备 " + device.getDeviceCode() + " 上报事件 " + event.getEventCode());
            alarm.setOccurTime(event.getEventTime());
            alarmService.receive(alarm);
            log.info("[iot] 设备事件 {} 已转告警", event.getEventCode());
        }
        return event;
    }

    public void handle(Long id) {
        IotDeviceEvent event = baseMapper.selectById(id);
        if (event == null) {
            throw BizException.notFound("设备事件不存在: " + id);
        }
        event.setHandleStatus(1);
        baseMapper.updateById(event);
    }

    private String eventName(String code) {
        return switch (code) {
            case "OFFLINE" -> "设备离线";
            case "ONLINE" -> "设备上线";
            case "FAULT" -> "设备故障";
            case "REBOOT" -> "设备重启";
            default -> code;
        };
    }
}
