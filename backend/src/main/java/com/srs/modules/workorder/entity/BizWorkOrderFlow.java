package com.srs.modules.workorder.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_work_order_flow")
@Schema(description = "工单流转记录")
public class BizWorkOrderFlow extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "工单ID")
    private Long orderId;

    @Schema(description = "create/assign/accept/handle/verify/evaluate/upgrade")
    private String action;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作详情")
    private String actionDetail;

    @Schema(description = "附件URL")
    private String attachments;
}
