package com.srs.modules.repair.entity;

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
@TableName("biz_repair")
@Schema(description = "报修工单")
public class BizRepair extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "报修编号")
    private String repairNo;

    @Schema(description = "报修业主ID")
    private Long ownerId;

    @Schema(description = "房屋ID")
    private Long roomId;

    @Schema(description = "1水电 2门窗 3电梯 4其他")
    private Integer repairType;

    @Schema(description = "报修标题")
    private String title;

    @Schema(description = "报修内容")
    private String content;

    @Schema(description = "图片URL")
    private String images;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "期望上门时间")
    private LocalDateTime expectTime;

    @Schema(description = "0待派单 1已派单 2处理中 3已完成 4已评价")
    private Integer status;

    @Schema(description = "关联工单ID")
    private Long workOrderId;
}
