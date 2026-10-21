package com.srs.modules.stat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.srs.modules.base.mapper.BaseCommunityMapper;
import com.srs.modules.base.mapper.BaseOwnerMapper;
import com.srs.modules.base.mapper.BaseRoomMapper;
import com.srs.modules.iot.entity.IotDevice;
import com.srs.modules.iot.mapper.IotDeviceMapper;
import com.srs.modules.sec.entity.SecAccessRecord;
import com.srs.modules.sec.entity.SecAlarmEvent;
import com.srs.modules.sec.entity.SecVehiclePass;
import com.srs.modules.sec.entity.SecVisitorInvite;
import com.srs.modules.sec.mapper.SecAccessRecordMapper;
import com.srs.modules.sec.mapper.SecAlarmEventMapper;
import com.srs.modules.sec.mapper.SecVehiclePassMapper;
import com.srs.modules.sec.mapper.SecVisitorInviteMapper;
import com.srs.modules.workorder.entity.BizWorkOrder;
import com.srs.modules.workorder.mapper.BizWorkOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计分析: 工作台总览 / 趋势 / 安全指数 / 运营大屏
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatService {

    private final SecAlarmEventMapper alarmMapper;
    private final BizWorkOrderMapper workOrderMapper;
    private final IotDeviceMapper deviceMapper;
    private final SecAccessRecordMapper accessMapper;
    private final SecVehiclePassMapper vehiclePassMapper;
    private final SecVisitorInviteMapper visitorMapper;
    private final BaseCommunityMapper communityMapper;
    private final BaseRoomMapper roomMapper;
    private final BaseOwnerMapper ownerMapper;

    /** 总览: 今日告警/待办工单/设备在线率/今日通行 */
    public Map<String, Object> overview(Long communityId) {
        LocalDate today = LocalDate.now();
        LocalDateTime begin = today.atStartOfDay();
        LocalDateTime end = begin.plusDays(1);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("todayAlarm", countAlarm(communityId, begin, end, null));
        data.put("pendingAlarm", countAlarm(communityId, null, null, 0));
        data.put("pendingWorkOrder", countOrder(communityId, null));
        data.put("overdueWorkOrder", workOrderMapper.selectCount(new LambdaQueryWrapper<BizWorkOrder>()
                .eq(communityId != null, BizWorkOrder::getCommunityId, communityId)
                .lt(BizWorkOrder::getSlaDeadline, LocalDateTime.now())
                .notIn(BizWorkOrder::getStatus, List.of(4, 5))));
        data.put("todayAccess", countAccess(communityId, begin, end, null));
        data.put("todayVehicleIn", countVehicle(communityId, begin, end, 1));
        data.put("todayVehicleOut", countVehicle(communityId, begin, end, 2));
        data.put("todayVisitor", countVisitor(communityId, begin, end));
        data.put("deviceOnlineRate", deviceOnlineRate(communityId));
        data.put("communityCount", communityMapper.selectCount(null));
        LambdaQueryWrapper<com.srs.modules.base.entity.BaseRoom> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(communityId != null, com.srs.modules.base.entity.BaseRoom::getCommunityId, communityId);
        data.put("roomCount", roomMapper.selectCount(roomWrapper));
        data.put("ownerCount", ownerMapper.selectCount(null));
        return data;
    }

    public Map<String, Object> alarmTrend(Long communityId, int days) {
        List<String> dates = new ArrayList<>();
        List<Long> totals = new ArrayList<>();
        List<Long> handled = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            dates.add(d.toString());
            totals.add(countAlarm(communityId, d.atStartOfDay(), d.plusDays(1).atStartOfDay(), null));
            handled.add(countAlarm(communityId, d.atStartOfDay(), d.plusDays(1).atStartOfDay(), 2));
        }
        return Map.of("dates", dates, "totals", totals, "handled", handled);
    }

    public Map<String, Object> accessTrend(Long communityId, int days) {
        List<String> dates = new ArrayList<>();
        List<Long> totals = new ArrayList<>();
        List<Long> denied = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            dates.add(d.toString());
            totals.add(countAccess(communityId, d.atStartOfDay(), d.plusDays(1).atStartOfDay(), null));
            denied.add(countAccess(communityId, d.atStartOfDay(), d.plusDays(1).atStartOfDay(), 0));
        }
        return Map.of("dates", dates, "totals", totals, "denied", denied);
    }

    public Map<String, Object> vehicleTrend(Long communityId, int days) {
        List<String> dates = new ArrayList<>();
        List<Long> inList = new ArrayList<>();
        List<Long> outList = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            dates.add(d.toString());
            inList.add(countVehicle(communityId, d.atStartOfDay(), d.plusDays(1).atStartOfDay(), 1));
            outList.add(countVehicle(communityId, d.atStartOfDay(), d.plusDays(1).atStartOfDay(), 2));
        }
        return Map.of("dates", dates, "inTotal", inList, "outTotal", outList);
    }

    public Map<String, Object> workOrderSummary(Long communityId) {
        Map<String, Object> data = new LinkedHashMap<>();
        for (int s = 0; s <= 5; s++) {
            int status = s;
            data.put("status" + s, countOrder(communityId, status));
        }
        data.put("total", countOrder(communityId, null));
        return data;
    }

    public Map<String, Object> deviceStat(Long communityId) {
        Map<String, Object> data = new LinkedHashMap<>();
        long total = deviceMapper.selectCount(new LambdaQueryWrapper<IotDevice>()
                .eq(communityId != null, IotDevice::getCommunityId, communityId));
        long online = deviceMapper.selectCount(new LambdaQueryWrapper<IotDevice>()
                .eq(communityId != null, IotDevice::getCommunityId, communityId)
                .eq(IotDevice::getOnlineStatus, 1));
        long fault = deviceMapper.selectCount(new LambdaQueryWrapper<IotDevice>()
                .eq(communityId != null, IotDevice::getCommunityId, communityId)
                .eq(IotDevice::getRunStatus, 2));
        data.put("total", total);
        data.put("online", online);
        data.put("offline", total - online);
        data.put("fault", fault);
        data.put("onlineRate", total == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(online)
                .multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));

        List<IotDevice> devices = deviceMapper.selectList(new LambdaQueryWrapper<IotDevice>()
                .eq(communityId != null, IotDevice::getCommunityId, communityId));
        Map<String, Long> byType = new LinkedHashMap<>();
        for (IotDevice d : devices) {
            byType.merge(d.getDeviceType() == null ? "unknown" : d.getDeviceType(), 1L, Long::sum);
        }
        data.put("byType", byType.entrySet().stream()
                .map(e -> Map.<String, Object>of("name", e.getKey(), "value", e.getValue())).toList());
        return data;
    }

    /**
     * 小区安全指数(0~100): 综合未处理告警、设备离线率、超时工单
     * score = 100 - 未处理告警*2 - 设备离线率*0.3 - 超时工单*3
     */
    public Map<String, Object> safetyIndex(Long communityId) {
        long pendingAlarm = countAlarm(communityId, null, null, 0);
        long overdue = workOrderMapper.selectCount(new LambdaQueryWrapper<BizWorkOrder>()
                .eq(communityId != null, BizWorkOrder::getCommunityId, communityId)
                .lt(BizWorkOrder::getSlaDeadline, LocalDateTime.now())
                .notIn(BizWorkOrder::getStatus, List.of(4, 5)));
        BigDecimal onlineRate = deviceOnlineRate(communityId);
        BigDecimal offlineRate = BigDecimal.valueOf(100).subtract(onlineRate);

        BigDecimal score = BigDecimal.valueOf(100)
                .subtract(BigDecimal.valueOf(pendingAlarm * 2))
                .subtract(offlineRate.multiply(BigDecimal.valueOf(0.3)))
                .subtract(BigDecimal.valueOf(overdue * 3));
        score = score.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);

        String level = score.compareTo(BigDecimal.valueOf(90)) >= 0 ? "优秀"
                : score.compareTo(BigDecimal.valueOf(75)) >= 0 ? "良好"
                : score.compareTo(BigDecimal.valueOf(60)) >= 0 ? "一般" : "预警";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("score", score);
        data.put("level", level);
        data.put("pendingAlarm", pendingAlarm);
        data.put("overdueWorkOrder", overdue);
        data.put("deviceOfflineRate", offlineRate.setScale(2, RoundingMode.HALF_UP));
        return data;
    }

    public BigDecimal deviceOnlineRate(Long communityId) {
        long total = deviceMapper.selectCount(new LambdaQueryWrapper<IotDevice>()
                .eq(communityId != null, IotDevice::getCommunityId, communityId));
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        long online = deviceMapper.selectCount(new LambdaQueryWrapper<IotDevice>()
                .eq(communityId != null, IotDevice::getCommunityId, communityId)
                .eq(IotDevice::getOnlineStatus, 1));
        return BigDecimal.valueOf(online).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private long countAlarm(Long communityId, LocalDateTime begin, LocalDateTime end, Integer status) {
        return alarmMapper.selectCount(new LambdaQueryWrapper<SecAlarmEvent>()
                .eq(communityId != null, SecAlarmEvent::getCommunityId, communityId)
                .ge(begin != null, SecAlarmEvent::getOccurTime, begin)
                .lt(end != null, SecAlarmEvent::getOccurTime, end)
                .eq(status != null, SecAlarmEvent::getStatus, status));
    }

    private long countOrder(Long communityId, Integer status) {
        return workOrderMapper.selectCount(new LambdaQueryWrapper<BizWorkOrder>()
                .eq(communityId != null, BizWorkOrder::getCommunityId, communityId)
                .eq(status != null, BizWorkOrder::getStatus, status));
    }

    private long countAccess(Long communityId, LocalDateTime begin, LocalDateTime end, Integer passResult) {
        return accessMapper.selectCount(new LambdaQueryWrapper<SecAccessRecord>()
                .eq(communityId != null, SecAccessRecord::getCommunityId, communityId)
                .ge(begin != null, SecAccessRecord::getPassTime, begin)
                .lt(end != null, SecAccessRecord::getPassTime, end)
                .eq(passResult != null, SecAccessRecord::getPassResult, passResult));
    }

    private long countVehicle(Long communityId, LocalDateTime begin, LocalDateTime end, Integer direction) {
        return vehiclePassMapper.selectCount(new LambdaQueryWrapper<SecVehiclePass>()
                .eq(communityId != null, SecVehiclePass::getCommunityId, communityId)
                .ge(begin != null, SecVehiclePass::getPassTime, begin)
                .lt(end != null, SecVehiclePass::getPassTime, end)
                .eq(SecVehiclePass::getDirection, direction));
    }

    private long countVisitor(Long communityId, LocalDateTime begin, LocalDateTime end) {
        return visitorMapper.selectCount(new LambdaQueryWrapper<SecVisitorInvite>()
                .eq(communityId != null, SecVisitorInvite::getCommunityId, communityId)
                .ge(begin != null, SecVisitorInvite::getVisitStart, begin)
                .lt(end != null, SecVisitorInvite::getVisitStart, end));
    }
}
