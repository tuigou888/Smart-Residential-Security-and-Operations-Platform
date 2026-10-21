package com.srs.modules.stat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stat_daily_summary")
@Schema(description = "日统计汇总")
public class StatDailySummary extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "统计日期")
    private LocalDate statDate;

    @Schema(description = "告警总数")
    private Integer alarmTotal;

    @Schema(description = "已处理告警")
    private Integer alarmHandled;

    @Schema(description = "门禁通行总数")
    private Integer accessTotal;

    @Schema(description = "车辆进入总数")
    private Integer vehicleInTotal;

    @Schema(description = "车辆离开总数")
    private Integer vehicleOutTotal;

    @Schema(description = "访客总数")
    private Integer visitorTotal;

    @Schema(description = "工单总数")
    private Integer workOrderTotal;

    @Schema(description = "已完成工单")
    private Integer workOrderFinished;

    @Schema(description = "巡更打卡总数")
    private Integer patrolTotal;

    @Schema(description = "漏巡数")
    private Integer patrolMissing;

    @Schema(description = "设备在线率")
    private BigDecimal deviceOnlineRate;
}
