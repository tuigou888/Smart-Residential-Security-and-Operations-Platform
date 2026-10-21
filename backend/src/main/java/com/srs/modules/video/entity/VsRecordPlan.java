package com.srs.modules.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_record_plan")
@Schema(description = "录像计划")
public class VsRecordPlan extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "计划名称")
    private String planName;

    @Schema(description = "1全天 2定时 3移动侦测")
    private Integer recordType;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "存储天数")
    private Integer storageDays;

    @Schema(description = "1启用 0停用")
    private Integer status;
}
