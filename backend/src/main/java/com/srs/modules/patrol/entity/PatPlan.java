package com.srs.modules.patrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pat_plan")
@Schema(description = "巡更计划")
public class PatPlan extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "计划名称")
    private String planName;

    @Schema(description = "路线ID")
    private Long routeId;

    @Schema(description = "1日常 2夜间 3节假日")
    private Integer patrolType;

    @Schema(description = "开始时间 HH:mm")
    private String startTime;

    @Schema(description = "结束时间 HH:mm")
    private String endTime;

    @Schema(description = "巡更间隔(分钟)")
    private Integer intervalMinutes;

    @Schema(description = "指定人员ID(逗号分隔)")
    private String assignUserIds;

    @Schema(description = "1启用 0停用")
    private Integer status;
}
