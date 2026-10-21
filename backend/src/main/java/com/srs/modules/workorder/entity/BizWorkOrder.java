package com.srs.modules.workorder.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_work_order")
@Schema(description = "工单")
public class BizWorkOrder extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "工单编号")
    private String orderNo;

    @Schema(description = "alarm/repair/complaint/patrol/fire")
    private String orderType;

    @Schema(description = "工单标题")
    private String title;

    @Schema(description = "工单内容")
    private String content;

    @Schema(description = "来源ID")
    private Long sourceId;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "1紧急 2高 3中 4低")
    private Integer priority;

    @Schema(description = "0待派发 1待接单 2处理中 3待验收 4已完成 5已关闭")
    private Integer status;

    @Schema(description = "处理人ID")
    private Long assigneeId;

    @Schema(description = "处理人姓名")
    private String assigneeName;

    @Schema(description = "SLA时限")
    private LocalDateTime slaDeadline;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "评价分数1-5")
    private Integer evaluateScore;

    @Schema(description = "评价内容")
    private String evaluateContent;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String communityName;
}
