package com.srs.modules.sec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sec_access_permission")
@Schema(description = "门禁授权")
public class SecAccessPermission extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "门禁设备ID")
    private Long deviceId;

    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "业主姓名")
    private String ownerName;

    @Schema(description = "允许通行开始时段")
    private String timeStart;

    @Schema(description = "允许通行结束时段")
    private String timeEnd;

    @Schema(description = "生效开始")
    private LocalDate effectiveStart;

    @Schema(description = "生效结束")
    private LocalDate effectiveEnd;

    @Schema(description = "1启用 0停用")
    private Integer status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String deviceName;
}
