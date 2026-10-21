package com.srs.modules.sec.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.base.entity.BaseVehicle;
import com.srs.modules.base.mapper.BaseVehicleMapper;
import com.srs.modules.iot.entity.IotDevice;
import com.srs.modules.iot.mapper.IotDeviceMapper;
import com.srs.modules.sec.entity.SecVehiclePass;
import com.srs.modules.sec.mapper.SecVehiclePassMapper;
import com.srs.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 车辆通行: 出入口记录 + 固定车自动放行判定 + 手动抬杆
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehiclePassService extends ServiceImpl<SecVehiclePassMapper, SecVehiclePass> {

    private final IotDeviceMapper deviceMapper;
    private final BaseVehicleMapper vehicleMapper;

    public IPage<SecVehiclePass> pagePass(PageQuery q, Long communityId, String plateNo, Integer direction,
                                          Integer vehicleType, LocalDateTime begin, LocalDateTime end) {
        Page<SecVehiclePass> page = new Page<>(q.getPageNum(), q.getPageSize());
        IPage<SecVehiclePass> result = baseMapper.selectPage(page, new LambdaQueryWrapper<SecVehiclePass>()
                .eq(communityId != null, SecVehiclePass::getCommunityId, communityId)
                .like(StringUtils.hasText(plateNo), SecVehiclePass::getPlateNo, plateNo)
                .eq(direction != null, SecVehiclePass::getDirection, direction)
                .eq(vehicleType != null, SecVehiclePass::getVehicleType, vehicleType)
                .ge(begin != null, SecVehiclePass::getPassTime, begin)
                .le(end != null, SecVehiclePass::getPassTime, end)
                .orderByDesc(SecVehiclePass::getPassTime));
        for (SecVehiclePass p : result.getRecords()) {
            IotDevice d = deviceMapper.selectById(p.getGateDeviceId());
            if (d != null) {
                p.setGateName(d.getDeviceName());
            }
        }
        return result;
    }

    /** 每日进出统计 */
    public Map<String, Object> dailyStat(Long communityId, int days) {
        List<String> dates = new ArrayList<>();
        List<Long> inList = new ArrayList<>();
        List<Long> outList = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.toString());
            inList.add(count(communityId, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), 1));
            outList.add(count(communityId, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), 2));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dates", dates);
        data.put("inTotal", inList);
        data.put("outTotal", outList);
        return data;
    }

    private long count(Long communityId, LocalDateTime begin, LocalDateTime end, Integer direction) {
        return baseMapper.selectCount(new LambdaQueryWrapper<SecVehiclePass>()
                .eq(communityId != null, SecVehiclePass::getCommunityId, communityId)
                .ge(SecVehiclePass::getPassTime, begin)
                .lt(SecVehiclePass::getPassTime, end)
                .eq(SecVehiclePass::getDirection, direction));
    }

    /** 手动抬杆 */
    public void openGate(Long gateDeviceId, String plateNo, String reason) {
        IotDevice device = deviceMapper.selectById(gateDeviceId);
        if (device == null) {
            throw BizException.notFound("道闸设备不存在: " + gateDeviceId);
        }
        SecVehiclePass pass = new SecVehiclePass();
        pass.setCommunityId(device.getCommunityId());
        pass.setGateDeviceId(gateDeviceId);
        pass.setPlateNo(plateNo == null ? "手动抬杆" : plateNo);
        pass.setDirection(1);
        pass.setVehicleType(3);
        pass.setPassTime(LocalDateTime.now());
        baseMapper.insert(pass);
        log.info("[vehicle] 手动抬杆 gateId={} operator={} reason={}",
                gateDeviceId, SecurityUtils.getUsername(), reason);
    }

    /** 车牌入场: 命中固定车/月租车自动放行 */
    public Map<String, Object> enter(Long gateDeviceId, String plateNo) {
        IotDevice device = deviceMapper.selectById(gateDeviceId);
        if (device == null) {
            throw BizException.notFound("道闸设备不存在: " + gateDeviceId);
        }
        BaseVehicle vehicle = vehicleMapper.selectOne(new LambdaQueryWrapper<BaseVehicle>()
                .eq(BaseVehicle::getPlateNo, plateNo)
                .eq(BaseVehicle::getCommunityId, device.getCommunityId())
                .eq(BaseVehicle::getStatus, 1)
                .last("limit 1"));

        boolean allowed = vehicle != null;
        Integer vehicleType = 3;
        if (vehicle != null && vehicle.getVehicleType() != null) {
            vehicleType = vehicle.getVehicleType();
            LocalDate today = LocalDate.now();
            if (vehicle.getEndDate() != null && today.isAfter(vehicle.getEndDate())) {
                allowed = false;
            }
        }
        if (allowed) {
            SecVehiclePass pass = new SecVehiclePass();
            pass.setCommunityId(device.getCommunityId());
            pass.setGateDeviceId(gateDeviceId);
            pass.setPlateNo(plateNo);
            pass.setDirection(1);
            pass.setVehicleType(vehicleType);
            pass.setPassTime(LocalDateTime.now());
            baseMapper.insert(pass);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("plateNo", plateNo);
        data.put("allowed", allowed);
        data.put("vehicleType", vehicleType);
        data.put("reason", allowed ? "白名单自动放行" : "非登记车辆, 需人工确认");
        return data;
    }

    public List<IotDevice> gates(Long communityId) {
        return deviceMapper.selectList(new LambdaQueryWrapper<IotDevice>()
                .eq(communityId != null, IotDevice::getCommunityId, communityId)
                .eq(IotDevice::getDeviceType, "vehicle_gate")
                .orderByAsc(IotDevice::getDeviceCode));
    }
}
