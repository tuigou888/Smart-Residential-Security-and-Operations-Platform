package com.srs.modules.sec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sec_alarm_handle")
@Schema(description = "告警处置记录")
public class SecAlarmHandle extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "告警ID")
    private Long alarmId;

    @Schema(description = "1派单 2电话 3现场 4复核")
    private Integer handleType;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "处理人姓名")
    private String handlerName;

    @Schema(description = "处理结果")
    private String handleResult;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "关联工单ID")
    private Long workOrderId;

    @Schema(description = "附件")
    private String attachments;
}
