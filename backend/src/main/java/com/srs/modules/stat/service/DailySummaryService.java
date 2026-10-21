package com.srs.modules.stat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.base.entity.BaseCommunity;
import com.srs.modules.base.mapper.BaseCommunityMapper;
import com.srs.modules.iot.entity.IotDevice;
import com.srs.modules.iot.mapper.IotDeviceMapper;
import com.srs.modules.patrol.entity.PatRecord;
import com.srs.modules.patrol.mapper.PatRecordMapper;
import com.srs.modules.sec.entity.SecAccessRecord;
import com.srs.modules.sec.entity.SecAlarmEvent;
import com.srs.modules.sec.entity.SecVehiclePass;
import com.srs.modules.sec.entity.SecVisitorInvite;
import com.srs.modules.sec.mapper.SecAccessRecordMapper;
import com.srs.modules.sec.mapper.SecAlarmEventMapper;
import com.srs.modules.sec.mapper.SecVehiclePassMapper;
import com.srs.modules.sec.mapper.SecVisitorInviteMapper;
import com.srs.modules.stat.entity.StatDailySummary;
import com.srs.modules.stat.mapper.StatDailySummaryMapper;
import com.srs.modules.workorder.entity.BizWorkOrder;
import com.srs.modules.workorder.mapper.BizWorkOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 日统计汇总: 按小区按天聚合各业务表, 幂等 upsert
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailySummaryService extends ServiceImpl<StatDailySummaryMapper, StatDailySummary> {

    private final BaseCommunityMapper communityMapper;
    private final SecAlarmEventMapper alarmMapper;
    private final SecAccessRecordMapper accessRecordMapper;
    private final SecVehiclePassMapper vehiclePassMapper;
    private final SecVisitorInviteMapper visitorInviteMapper;
    private final BizWorkOrderMapper workOrderMapper;
    private final PatRecordMapper patRecordMapper;
    private final IotDeviceMapper deviceMapper;

    public IPage<StatDailySummary> page(PageQuery q, Long communityId, LocalDate begin, LocalDate end) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<StatDailySummary>()
                        .eq(communityId != null, StatDailySummary::getCommunityId, communityId)
                        .ge(begin != null, StatDailySummary::getStatDate, begin)
                        .le(end != null, StatDailySummary::getStatDate, end)
                        .orderByDesc(StatDailySummary::getStatDate));
    }

    /**
     * 生成(或覆盖)某小区某日的汇总
     */
    @Transactional(rollbackFor = Exception.class)
    public StatDailySummary generate(Long communityId, LocalDate date) {
        if (communityId == null || date == null) {
            throw BizException.badRequest("小区ID与统计日期不能为空");
        }
        if (communityMapper.selectById(communityId) == null) {
            throw BizException.notFound("小区不存在: " + communityId);
        }
        LocalDateTime begin = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        StatDailySummary summary = baseMapper.selectOne(new LambdaQueryWrapper<StatDailySummary>()
                .eq(StatDailySummary::getCommunityId, communityId)
                .eq(StatDailySummary::getStatDate, date)
                .last("limit 1"));
        boolean exists = summary != null;
        if (!exists) {
            summary = new StatDailySummary();
        }
        summary.setCommunityId(communityId);
        summary.setStatDate(date);

        summary.setAlarmTotal((int) count(alarmMapper.selectCount(new LambdaQueryWrapper<SecAlarmEvent>()
                .eq(SecAlarmEvent::getCommunityId, communityId)
                .ge(SecAlarmEvent::getOccurTime, begin)
                .lt(SecAlarmEvent::getOccurTime, end))));
        summary.setAlarmHandled((int) count(alarmMapper.selectCount(new LambdaQueryWrapper<SecAlarmEvent>()
                .eq(SecAlarmEvent::getCommunityId, communityId)
                .eq(SecAlarmEvent::getStatus, 2)
                .ge(SecAlarmEvent::getOccurTime, begin)
                .lt(SecAlarmEvent::getOccurTime, end))));
        summary.setAccessTotal((int) count(accessRecordMapper.selectCount(new LambdaQueryWrapper<SecAccessRecord>()
                .eq(SecAccessRecord::getCommunityId, communityId)
                .ge(SecAccessRecord::getPassTime, begin)
                .lt(SecAccessRecord::getPassTime, end))));
        summary.setVehicleInTotal((int) count(vehiclePassMapper.selectCount(new LambdaQueryWrapper<SecVehiclePass>()
                .eq(SecVehiclePass::getCommunityId, communityId)
                .eq(SecVehiclePass::getDirection, 1)
                .ge(SecVehiclePass::getPassTime, begin)
                .lt(SecVehiclePass::getPassTime, end))));
        summary.setVehicleOutTotal((int) count(vehiclePassMapper.selectCount(new LambdaQueryWrapper<SecVehiclePass>()
                .eq(SecVehiclePass::getCommunityId, communityId)
                .eq(SecVehiclePass::getDirection, 2)
                .ge(SecVehiclePass::getPassTime, begin)
                .lt(SecVehiclePass::getPassTime, end))));
        summary.setVisitorTotal((int) count(visitorInviteMapper.selectCount(new LambdaQueryWrapper<SecVisitorInvite>()
                .eq(SecVisitorInvite::getCommunityId, communityId)
                .ge(SecVisitorInvite::getVisitStart, begin)
                .lt(SecVisitorInvite::getVisitStart, end))));
        summary.setWorkOrderTotal((int) count(workOrderMapper.selectCount(new LambdaQueryWrapper<BizWorkOrder>()
                .eq(BizWorkOrder::getCommunityId, communityId)
                .ge(BizWorkOrder::getCreateTime, begin)
                .lt(BizWorkOrder::getCreateTime, end))));
        summary.setWorkOrderFinished((int) count(workOrderMapper.selectCount(new LambdaQueryWrapper<BizWorkOrder>()
                .eq(BizWorkOrder::getCommunityId, communityId)
                .eq(BizWorkOrder::getStatus, 4)
                .ge(BizWorkOrder::getFinishTime, begin)
                .lt(BizWorkOrder::getFinishTime, end))));
        summary.setPatrolTotal((int) count(patRecordMapper.selectCount(new LambdaQueryWrapper<PatRecord>()
                .eq(PatRecord::getCommunityId, communityId)
                .ge(PatRecord::getCheckTime, begin)
                .lt(PatRecord::getCheckTime, end))));
        summary.setPatrolMissing((int) count(patRecordMapper.selectCount(new LambdaQueryWrapper<PatRecord>()
                .eq(PatRecord::getCommunityId, communityId)
                .eq(PatRecord::getStatus, 3)
                .ge(PatRecord::getCheckTime, begin)
                .lt(PatRecord::getCheckTime, end))));

        long deviceTotal = count(deviceMapper.selectCount(new LambdaQueryWrapper<IotDevice>()
                .eq(IotDevice::getCommunityId, communityId)));
        long deviceOnline = count(deviceMapper.selectCount(new LambdaQueryWrapper<IotDevice>()
                .eq(IotDevice::getCommunityId, communityId)
                .eq(IotDevice::getOnlineStatus, 1)));
        summary.setDeviceOnlineRate(deviceTotal == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(deviceOnline * 100.0 / deviceTotal).setScale(2, RoundingMode.HALF_UP));

        if (exists) {
            baseMapper.updateById(summary);
        } else {
            baseMapper.insert(summary);
        }
        return summary;
    }

    /** 生成指定日期全部小区的汇总 */
    @Transactional(rollbackFor = Exception.class)
    public int generateAll(LocalDate date) {
        List<BaseCommunity> communities = communityMapper.selectList(null);
        for (BaseCommunity community : communities) {
            generate(community.getId(), date);
        }
        return communities.size();
    }

    private long count(Long value) {
        return value == null ? 0 : value;
    }
}
